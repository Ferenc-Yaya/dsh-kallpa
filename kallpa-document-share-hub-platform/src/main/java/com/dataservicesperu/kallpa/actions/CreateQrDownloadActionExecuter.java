package com.dataservicesperu.kallpa.actions;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CreateQrDownloadActionExecuter extends ActionExecuterAbstractBase {

    private static Log logger = LogFactory.getLog(CreateQrDownloadActionExecuter.class);

    private final ServiceRegistry serviceRegistry;
    private final String baseUrl;

    public CreateQrDownloadActionExecuter(ServiceRegistry serviceRegistry, String baseUrl) {
        this.serviceRegistry = serviceRegistry;
        this.baseUrl = baseUrl;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        // No parameters needed
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        ContentReader reader = serviceRegistry.getContentService().getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
        if (reader == null || !"application/pdf".equals(reader.getMimetype())) {
            logger.warn("El nodo no es un PDF, saltando la generación del código QR");
            return;
        }

        try {
            Map<QName, Serializable> props = serviceRegistry.getNodeService().getProperties(actionedUponNodeRef);
            String fileName = (String) props.get(ContentModel.PROP_NAME);

            if (fileName == null || fileName.trim().isEmpty()) {
                logger.error("El nombre del archivo es null o vacío");
                return;
            }

            String nodeId = actionedUponNodeRef.getId();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

            String downloadUrl = String.format("%s/alfresco/service/kallpa/download/workspace/SpacesStore/%s/%s",
                    getBaseUrl(), nodeId, encodedFileName);

            logger.info("Generando QR con URL de descarga: " + downloadUrl);

            ContentWriter writer = serviceRegistry.getContentService().getWriter(actionedUponNodeRef,
                    ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/pdf");

            PdfReader pdfReader = new PdfReader(reader.getContentInputStream());
            PdfStamper stamper = new PdfStamper(pdfReader, writer.getContentOutputStream());

            if (pdfReader.getNumberOfPages() < 1) {
                logger.error("El PDF no tiene páginas");
                stamper.close();
                pdfReader.close();
                return;
            }

            int pageNo = 1;
            PdfContentByte over = stamper.getOverContent(pageNo);

            BarcodeQRCode barcodeQRCode = new BarcodeQRCode(downloadUrl, 200, 200, null);
            Image qrcodeImage = barcodeQRCode.getImage();

            float pageWidth = pdfReader.getPageSize(pageNo).getWidth();
            float pageHeight = pdfReader.getPageSize(pageNo).getHeight();

            qrcodeImage.setAbsolutePosition(pageWidth - 110, pageHeight - 110);
            qrcodeImage.scaleAbsolute(100, 100);

            over.addImage(qrcodeImage);
            stamper.close();
            pdfReader.close();

            logger.info("Código QR agregado exitosamente al documento: " + fileName);

        } catch (Exception e) {
            logger.error("Error inesperado al generar QR", e);
            throw new RuntimeException("Error inesperado al generar QR", e);
        }
    }

    private String getBaseUrl() {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            logger.info("Usando URL base desde properties: " + baseUrl);
            return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        }

        logger.error("alfresco.base.url no está configurado");
        throw new RuntimeException("alfresco.base.url requerido en properties");
    }
}