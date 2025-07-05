package com.dataservicesperu.kallpa.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class DocumentSubmissionService {

    private static final Log logger = LogFactory.getLog(DocumentSubmissionService.class);

    private final ServiceRegistry serviceRegistry;

    public DocumentSubmissionService(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public DocumentSubmissionResult processSubmission(String siteId, String username) {
        try {
            SiteInfo siteInfo = null;
            NodeRef targetFolder = getTargetFolder(siteId);

            if (siteId != null && !siteId.trim().isEmpty()) {
                SiteService siteService = serviceRegistry.getSiteService();
                siteInfo = siteService.getSite(siteId);
            }

            String filename = generateFilename(siteInfo);

            NodeRef existingFile = serviceRegistry.getNodeService()
                    .getChildByName(targetFolder, ContentModel.ASSOC_CONTAINS, filename);

            if (existingFile != null) {
                return new DocumentSubmissionResult(true,
                        "El archivo de confirmación ya fue creado anteriormente",
                        filename, existingFile.toString());
            }

            byte[] pdfContent = createConfirmationPdf(username, siteInfo);

            NodeRef pdfFile = serviceRegistry.getFileFolderService()
                    .create(targetFolder, filename, ContentModel.TYPE_CONTENT).getNodeRef();

            ContentWriter writer = serviceRegistry.getContentService()
                    .getWriter(pdfFile, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/pdf");
            writer.putContent(new ByteArrayInputStream(pdfContent));

            logger.info("Archivo PDF de confirmación creado exitosamente: " + filename);

            return new DocumentSubmissionResult(true,
                    "Archivo de confirmación enviado exitosamente",
                    filename, pdfFile.toString());

        } catch (Exception e) {
            logger.error("Error al procesar envío de documentos", e);
            return new DocumentSubmissionResult(false,
                    "Error interno: " + e.getMessage(), null, null);
        }
    }

    private String generateFilename(SiteInfo siteInfo) {
        if (siteInfo != null) {
            String siteName = siteInfo.getTitle();
            if (siteName == null || siteName.trim().isEmpty()) {
                siteName = siteInfo.getShortName();
            }
            siteName = siteName.replaceAll("[^a-zA-Z0-9\\s-_]", "").trim();
            return "ARCHIVOS SUBIDOS - " + siteName + ".pdf";
        } else {
            return "ARCHIVOS SUBIDOS.pdf";
        }
    }

    private NodeRef getTargetFolder(String siteId) {
        try {
            if (siteId != null && !siteId.trim().isEmpty()) {
                logger.info("Obteniendo carpeta para sitio: " + siteId);

                SearchParameters searchParams = new SearchParameters();
                searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                searchParams.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
                searchParams.setQuery("PATH:\"/app:company_home/st:sites/cm:" + siteId + "/cm:documentLibrary\"");
                searchParams.setLimit(1);

                ResultSet resultSet = serviceRegistry.getSearchService().query(searchParams);
                try {
                    if (resultSet.length() > 0) {
                        NodeRef documentLibrary = resultSet.getNodeRef(0);
                        logger.info("documentLibrary encontrada usando búsqueda: " + documentLibrary);
                        return documentLibrary;
                    }
                } finally {
                    resultSet.close();
                }

                SiteService siteService = serviceRegistry.getSiteService();
                SiteInfo site = siteService.getSite(siteId);
                if (site == null) {
                    throw new RuntimeException("El sitio '" + siteId + "' no existe.");
                }

                NodeRef siteNodeRef = site.getNodeRef();
                List<ChildAssociationRef> children = serviceRegistry.getNodeService()
                        .getChildAssocs(siteNodeRef);

                for (ChildAssociationRef child : children) {
                    String childName = (String) serviceRegistry.getNodeService()
                            .getProperty(child.getChildRef(), ContentModel.PROP_NAME);
                    if ("documentLibrary".equals(childName)) {
                        logger.info("documentLibrary encontrada como hijo: " + child.getChildRef());
                        return child.getChildRef();
                    }
                }

                throw new RuntimeException("No se puede encontrar documentLibrary para el sitio: " + siteId);

            } else {
                logger.info("Usando carpeta Company Home");

                SearchParameters searchParams = new SearchParameters();
                searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                searchParams.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
                searchParams.setQuery("PATH:\"/app:company_home\"");
                searchParams.setLimit(1);

                ResultSet resultSet = serviceRegistry.getSearchService().query(searchParams);
                try {
                    if (resultSet.length() > 0) {
                        NodeRef companyHome = resultSet.getNodeRef(0);
                        logger.info("Company Home encontrada: " + companyHome);
                        return companyHome;
                    }
                } finally {
                    resultSet.close();
                }

                throw new RuntimeException("No se puede encontrar Company Home");
            }
        } catch (Exception e) {
            logger.error("Error en getTargetFolder para sitio: " + siteId, e);
            throw new RuntimeException("Error obteniendo carpeta destino: " + e.getMessage());
        }
    }

    private byte[] createConfirmationPdf(String username, SiteInfo siteInfo) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("ARCHIVOS SUBIDOS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Paragraph separator = new Paragraph("=====================================");
        separator.setAlignment(Element.ALIGN_CENTER);
        separator.setSpacingAfter(30);
        document.add(separator);

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDate = dateFormat.format(new Date());

        document.add(new Paragraph("Fecha: " + currentDate, normalFont));
        document.add(new Paragraph("Usuario: " + username, normalFont));

        if (siteInfo != null) {
            String siteName = siteInfo.getTitle();
            if (siteName == null || siteName.trim().isEmpty()) {
                siteName = siteInfo.getShortName();
            }
            document.add(new Paragraph("Sitio Colaborativo: " + siteName, normalFont));
        }

        document.add(new Paragraph(" ", normalFont));

        Font confirmFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLUE);
        Paragraph confirmation = new Paragraph(
                "Este archivo confirma que se han enviado los documentos.", confirmFont);
        confirmation.setSpacingBefore(20);
        document.add(confirmation);

        Paragraph footer = new Paragraph(" ", normalFont);
        footer.setSpacingBefore(50);
        document.add(footer);

        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY);
        Paragraph footerText = new Paragraph("Generado automáticamente por Document Share Hub", footerFont);
        footerText.setAlignment(Element.ALIGN_CENTER);
        document.add(footerText);

        document.close();
        return baos.toByteArray();
    }

    public static class DocumentSubmissionResult {
        private final boolean success;
        private final String message;
        private final String filename;
        private final String nodeRef;

        public DocumentSubmissionResult(boolean success, String message, String filename, String nodeRef) {
            this.success = success;
            this.message = message;
            this.filename = filename;
            this.nodeRef = nodeRef;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getFilename() { return filename; }
        public String getNodeRef() { return nodeRef; }
    }
}
