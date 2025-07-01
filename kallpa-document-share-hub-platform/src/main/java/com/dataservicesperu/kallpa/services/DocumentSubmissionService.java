package com.dataservicesperu.kallpa.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocumentSubmissionService {

    private static final Log logger = LogFactory.getLog(DocumentSubmissionService.class);

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public DocumentSubmissionResult processSubmission(String siteId, String username) {
        try {
            // Determinar carpeta destino y obtener info del sitio
            SiteInfo siteInfo = null;
            NodeRef targetFolder = getTargetFolder(siteId);

            // Obtener información del sitio si existe
            if (siteId != null && !siteId.trim().isEmpty()) {
                SiteService siteService = serviceRegistry.getSiteService();
                siteInfo = siteService.getSite(siteId);
            }

            // Generar nombre del archivo
            String filename = generateFilename(siteInfo);

            // Verificar si ya existe el archivo
            NodeRef existingFile = serviceRegistry.getNodeService()
                    .getChildByName(targetFolder, ContentModel.ASSOC_CONTAINS, filename);

            if (existingFile != null) {
                return new DocumentSubmissionResult(true,
                        "El archivo de confirmación ya fue creado anteriormente",
                        filename, existingFile.toString());
            }

            // Crear el PDF
            byte[] pdfContent = createConfirmationPdf(username, siteInfo);

            // Crear el archivo en Alfresco
            NodeRef pdfFile = serviceRegistry.getFileFolderService()
                    .create(targetFolder, filename, ContentModel.TYPE_CONTENT).getNodeRef();

            // Escribir contenido
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
            // Limpiar caracteres especiales del nombre del sitio
            siteName = siteName.replaceAll("[^a-zA-Z0-9\\s-_]", "").trim();
            return "ARCHIVOS SUBIDOS - " + siteName + ".pdf";
        } else {
            return "ARCHIVOS SUBIDOS.pdf";
        }
    }

    private NodeRef getTargetFolder(String siteId) {
        if (siteId != null && !siteId.trim().isEmpty()) {
            SiteService siteService = serviceRegistry.getSiteService();
            SiteInfo site = siteService.getSite(siteId);
            if (site != null) {
                return siteService.getContainer(siteId, "documentLibrary");
            }
            throw new RuntimeException("El sitio '" + siteId + "' no existe.");
        } else {
            // Carpeta personal del usuario
            PersonService personService = serviceRegistry.getPersonService();
            NodeRef person = personService.getPerson(serviceRegistry.getAuthenticationService().getCurrentUserName());
            return serviceRegistry.getNodeService()
                    .getChildByName(person, ContentModel.ASSOC_CONTAINS, "Documents");
        }
    }

    private byte[] createConfirmationPdf(String username, SiteInfo siteInfo) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("ARCHIVOS SUBIDOS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Línea separadora
        Paragraph separator = new Paragraph("=====================================");
        separator.setAlignment(Element.ALIGN_CENTER);
        separator.setSpacingAfter(30);
        document.add(separator);

        // Información
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDate = dateFormat.format(new Date());

        document.add(new Paragraph("Fecha: " + currentDate, normalFont));
        document.add(new Paragraph("Usuario: " + username, normalFont));

        // Agregar información del sitio si existe
        if (siteInfo != null) {
            String siteName = siteInfo.getTitle();
            if (siteName == null || siteName.trim().isEmpty()) {
                siteName = siteInfo.getShortName();
            }
            document.add(new Paragraph("Sitio Colaborativo: " + siteName, normalFont));
        }

        document.add(new Paragraph(" ", normalFont)); // Espacio

        // Mensaje de confirmación
        Font confirmFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLUE);
        Paragraph confirmation = new Paragraph(
                "Este archivo confirma que se han enviado los documentos.", confirmFont);
        confirmation.setSpacingBefore(20);
        document.add(confirmation);

        // Footer
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

    // Clase interna para el resultado
    public static class DocumentSubmissionResult {
        private boolean success;
        private String message;
        private String filename;
        private String nodeRef;

        public DocumentSubmissionResult(boolean success, String message, String filename, String nodeRef) {
            this.success = success;
            this.message = message;
            this.filename = filename;
            this.nodeRef = nodeRef;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getFilename() { return filename; }
        public String getNodeRef() { return nodeRef; }
    }
}
