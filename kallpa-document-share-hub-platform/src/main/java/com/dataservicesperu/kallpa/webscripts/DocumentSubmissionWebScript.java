package com.dataservicesperu.kallpa.webscripts;

import com.dataservicesperu.kallpa.services.DocumentSubmissionService;
import com.dataservicesperu.kallpa.services.DocumentSubmissionService.DocumentSubmissionResult;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.Status;

import java.io.IOException;
import java.io.Writer;

public class DocumentSubmissionWebScript extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(DocumentSubmissionWebScript.class);

    private DocumentSubmissionService documentSubmissionService;
    private ServiceRegistry serviceRegistry;

    public void setDocumentSubmissionService(DocumentSubmissionService documentSubmissionService) {
        this.documentSubmissionService = documentSubmissionService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            // Obtener parámetros
            String siteId = req.getParameter("site");
            String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();

            logger.info("Procesando envío de documentos para usuario: " + currentUser +
                    (siteId != null ? " en sitio: " + siteId : " en carpeta personal"));

            // Procesar la solicitud
            DocumentSubmissionResult result = documentSubmissionService.processSubmission(siteId, currentUser);

            // Configurar respuesta
            res.setContentType("application/json");
            res.setContentEncoding("UTF-8");

            if (result.isSuccess()) {
                res.setStatus(Status.STATUS_OK);
            } else {
                res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            }

            // Escribir respuesta JSON
            Writer writer = res.getWriter();
            writer.write(buildJsonResponse(result));
            writer.flush();

        } catch (Exception e) {
            logger.error("Error en DocumentSubmissionWebScript", e);

            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");

            Writer writer = res.getWriter();
            writer.write("{\"success\": false, \"message\": \"Error interno del servidor: " +
                    e.getMessage().replace("\"", "\\\"") + "\"}");
            writer.flush();
        }
    }

    private String buildJsonResponse(DocumentSubmissionResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(result.isSuccess()).append(",");
        json.append("\"message\": \"").append(escapeJson(result.getMessage())).append("\"");

        if (result.getFilename() != null) {
            json.append(",\"filename\": \"").append(escapeJson(result.getFilename())).append("\"");
        }

        if (result.getNodeRef() != null) {
            json.append(",\"nodeRef\": \"").append(escapeJson(result.getNodeRef())).append("\"");
        }

        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
