package com.dataservicesperu.kallpa.actions;

import com.itextpdf.text.DocumentException;
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
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CreateQrDownloadActionExecuter extends ActionExecuterAbstractBase {
    private static Log logger = LogFactory.getLog(CreateQrDownloadActionExecuter.class);
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
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
            // Obtener información del documento
            Map<QName, Serializable> props = serviceRegistry.getNodeService().getProperties(actionedUponNodeRef);
            String fileName = (String) props.get(ContentModel.PROP_NAME);

            // Crear URL de descarga pública (sin autenticación)
            // Formato: http://servidor:puerto/alfresco/d/d/workspace/SpacesStore/node-id/filename
            String nodeId = actionedUponNodeRef.getId();
            String baseUrl = getBaseUrl(); // Método helper para obtener la URL base
            String downloadUrl = String.format("%s/alfresco/s/kallpa/download/workspace/SpacesStore/%s/%s",
                    baseUrl, nodeId, fileName);

            logger.info("Generando QR con URL de descarga: " + downloadUrl);

            // Generar QR con la URL de descarga
            ContentWriter writer = serviceRegistry.getContentService().getWriter(actionedUponNodeRef,
                    ContentModel.PROP_CONTENT, true);

            PdfReader pdfReader = new PdfReader(reader.getContentInputStream());
            PdfStamper stamper = new PdfStamper(pdfReader, writer.getContentOutputStream());

            int pageNo = 1;
            PdfContentByte over = stamper.getOverContent(pageNo);

            // Crear QR code con la URL de descarga
            BarcodeQRCode barcodeQRCode = new BarcodeQRCode(downloadUrl, 1, 1, null);
            Image qrcodeImage = barcodeQRCode.getImage();

            // Posicionar el QR en la esquina superior derecha
            qrcodeImage.setAbsolutePosition(pdfReader.getPageSize(pageNo).getWidth() - 110,
                    pdfReader.getPageSize(pageNo).getHeight() - 110);
            qrcodeImage.scaleAbsolute(100, 100);

            over.addImage(qrcodeImage);
            stamper.close();
            pdfReader.close();

            logger.info("Código QR agregado exitosamente al documento: " + fileName);

        } catch (ContentIOException e) {
            logger.error("Error de contenido al procesar el PDF", e);
            throw new RuntimeException("Error al procesar el contenido del PDF", e);
        } catch (IOException e) {
            logger.error("Error de E/S al procesar el PDF", e);
            throw new RuntimeException("Error de E/S al procesar el PDF", e);
        } catch (DocumentException e) {
            logger.error("Error de documento PDF al agregar QR", e);
            throw new RuntimeException("Error al manipular el documento PDF", e);
        }
    }

    /**
     * Método helper para obtener la URL base del servidor
     * Puedes configurar esto como una propiedad del sistema o hardcodearlo según tu configuración
     */
    private String getBaseUrl() {
        // Opción 1: Obtener desde propiedades del sistema
        String baseUrl = System.getProperty("alfresco.base.url");
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }

        // Opción 2: Obtener desde configuración de Alfresco
        try {
            String alfrescoHost = System.getProperty("alfresco.host", "localhost");
            String alfrescoPort = System.getProperty("alfresco.port", "8080");
            String alfrescoProtocol = System.getProperty("alfresco.protocol", "http");
            return String.format("%s://%s:%s", alfrescoProtocol, alfrescoHost, alfrescoPort);
        } catch (Exception e) {
            logger.warn("No se pudo obtener la configuración del servidor, usando valor por defecto");
        }

        // Opción 3: Valor por defecto (ajustar según tu configuración)
        return "http://localhost:8080";
    }
}
