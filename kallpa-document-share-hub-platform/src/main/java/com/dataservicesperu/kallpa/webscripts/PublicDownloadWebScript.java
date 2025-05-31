package com.dataservicesperu.kallpa.webscripts;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class PublicDownloadWebScript extends AbstractWebScript {
    private static Log logger = LogFactory.getLog(PublicDownloadWebScript.class);
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            // Obtener parámetros de la URL usando template variables
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String storeType = templateVars.get("store_type");
            String storeId = templateVars.get("store_id");
            String nodeId = templateVars.get("id");
            String filename = templateVars.get("filename");

            logger.info("Solicitud de descarga pública para: " + filename + " (NodeID: " + nodeId + ")");

            // Crear NodeRef
            StoreRef storeRef = new StoreRef(storeType, storeId);
            NodeRef nodeRef = new NodeRef(storeRef, nodeId);

            // Verificar que el nodo existe
            if (!serviceRegistry.getNodeService().exists(nodeRef)) {
                logger.warn("Nodo no encontrado: " + nodeRef);
                res.setStatus(Status.STATUS_NOT_FOUND);
                res.getWriter().write("Documento no encontrado");
                return;
            }

            // Verificar que es un documento (no una carpeta)
            if (!serviceRegistry.getDictionaryService().isSubClass(
                    serviceRegistry.getNodeService().getType(nodeRef), ContentModel.TYPE_CONTENT)) {
                logger.warn("El nodo no es un documento: " + nodeRef);
                res.setStatus(Status.STATUS_BAD_REQUEST);
                res.getWriter().write("No es un documento válido");
                return;
            }

            // TODO: Aquí podrías agregar validación adicional para verificar que el documento tiene QR
            // Por ejemplo, verificar si existe una propiedad custom que indique que tiene QR
            // if (!hasQRCode(nodeRef)) { ... }

            // Obtener el contenido
            ContentReader reader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
            if (reader == null || !reader.exists()) {
                logger.warn("Contenido no encontrado para el nodo: " + nodeRef);
                res.setStatus(Status.STATUS_NOT_FOUND);
                res.getWriter().write("Contenido no disponible");
                return;
            }

            // Configurar headers para descarga
            String mimetype = reader.getMimetype();
            long size = reader.getSize();

            res.setContentType(mimetype);
            res.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            res.setHeader("Content-Length", String.valueOf(size));
            res.setStatus(Status.STATUS_OK);

            // Transmitir el contenido
            InputStream inputStream = reader.getContentInputStream();
            OutputStream outputStream = res.getOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.flush();

            logger.info("Descarga completada exitosamente: " + filename);

        } catch (Exception e) {
            logger.error("Error en descarga pública", e);
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Error interno del servidor");
        }
    }
}