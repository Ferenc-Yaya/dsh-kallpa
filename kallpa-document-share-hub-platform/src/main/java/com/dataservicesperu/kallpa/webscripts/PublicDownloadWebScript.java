package com.dataservicesperu.kallpa.webscripts;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class PublicDownloadWebScript extends AbstractWebScript {

    private static Log logger = LogFactory.getLog(PublicDownloadWebScript.class);

    private final ServiceRegistry serviceRegistry;
    private boolean guestAccess = true;

    public PublicDownloadWebScript(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setGuestAccess(boolean guestAccess) {
        this.guestAccess = guestAccess;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
                executeDownload(req, res);
                return null;
            });

        } catch (Exception e) {
            logger.error("Error en descarga pública", e);
            try {
                res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
                res.getWriter().write("Error interno del servidor: " + e.getMessage());
            } catch (IOException ioException) {
                logger.error("Error adicional al escribir respuesta de error", ioException);
            }
        }
    }

    private void executeDownload(WebScriptRequest req, WebScriptResponse res) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String storeType = templateVars.get("store_type");
            String storeId = templateVars.get("store_id");
            String nodeId = templateVars.get("id");
            String filename = templateVars.get("filename");

            logger.info("Solicitud de descarga pública recibida:");
            logger.info("  - Store Type: " + storeType);
            logger.info("  - Store ID: " + storeId);
            logger.info("  - Node ID: " + nodeId);
            logger.info("  - Filename: " + filename);

            if (storeType == null || storeId == null || nodeId == null || filename == null) {
                logger.error("Parámetros faltantes en la URL");
                res.setStatus(Status.STATUS_BAD_REQUEST);
                res.getWriter().write("Parámetros faltantes en la URL");
                return;
            }

            String decodedFilename;
            try {
                decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                logger.error("Error al decodificar filename: " + filename, e);
                decodedFilename = filename;
            }

            StoreRef storeRef = new StoreRef(storeType, storeId);
            NodeRef nodeRef = new NodeRef(storeRef, nodeId);

            if (!serviceRegistry.getNodeService().exists(nodeRef)) {
                logger.warn("Nodo no encontrado: " + nodeRef);
                res.setStatus(Status.STATUS_NOT_FOUND);
                res.getWriter().write("Documento no encontrado");
                return;
            }

            if (!serviceRegistry.getDictionaryService().isSubClass(
                    serviceRegistry.getNodeService().getType(nodeRef), ContentModel.TYPE_CONTENT)) {
                logger.warn("El nodo no es un documento: " + nodeRef);
                res.setStatus(Status.STATUS_BAD_REQUEST);
                res.getWriter().write("No es un documento válido");
                return;
            }

            ContentReader reader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
            if (reader == null || !reader.exists()) {
                logger.warn("Contenido no encontrado para el nodo: " + nodeRef);
                res.setStatus(Status.STATUS_NOT_FOUND);
                res.getWriter().write("Contenido no disponible");
                return;
            }

            String actualName = (String) serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
            if (actualName != null) {
                decodedFilename = actualName;
            }

            String mimetype = reader.getMimetype();
            long size = reader.getSize();

            logger.info("Configurando descarga: " + decodedFilename + " (" + size + " bytes)");

            res.setContentType(mimetype);
            res.setHeader("Content-Disposition", "attachment; filename=\"" + decodedFilename + "\"");
            res.setHeader("Content-Length", String.valueOf(size));
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "0");
            res.setHeader("Access-Control-Allow-Origin", "*");
            res.setHeader("Access-Control-Allow-Methods", "GET");
            res.setStatus(Status.STATUS_OK);

            inputStream = reader.getContentInputStream();
            outputStream = res.getOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            outputStream.flush();
            logger.info("Descarga completada: " + decodedFilename + " (" + totalBytesRead + " bytes)");

        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { logger.error("Error cerrando inputStream", e); }
            }
            if (outputStream != null) {
                try { outputStream.close(); } catch (IOException e) { logger.error("Error cerrando outputStream", e); }
            }
        }
    }
}