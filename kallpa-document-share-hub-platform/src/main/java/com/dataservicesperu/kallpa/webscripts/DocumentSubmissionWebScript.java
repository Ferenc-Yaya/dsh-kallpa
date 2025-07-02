package com.dataservicesperu.kallpa.webscripts;

import com.dataservicesperu.kallpa.services.DocumentSubmissionService;
import com.dataservicesperu.kallpa.services.DocumentSubmissionService.DocumentSubmissionResult;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.Status;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.Writer;

public class DocumentSubmissionWebScript extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(DocumentSubmissionWebScript.class);

    private DocumentSubmissionService documentSubmissionService;
    private ServiceRegistry serviceRegistry;

    @Value("${alfresco.base.url}")
    private String baseUrl;

    public void setDocumentSubmissionService(DocumentSubmissionService documentSubmissionService) {
        this.documentSubmissionService = documentSubmissionService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            // Ejecutar como system user ya que no hay autenticación
            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                @Override
                public Void doWork() throws Exception {
                    executeAsSystem(req, res);
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Error en DocumentSubmissionWebScript", e);
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");
            Writer writer = res.getWriter();
            writer.write("{\"success\": false, \"message\": \"Error interno del servidor: " +
                    e.getMessage().replace("\"", "\\\"") + "\", \"baseUrl\": \"" +
                    escapeJson(baseUrl) + "\"}");
            writer.flush();
        }
    }

    private void executeAsSystem(WebScriptRequest req, WebScriptResponse res) throws IOException {
        // Todo tu código actual del execute() va aquí
        String siteId = req.getParameter("site");
        String currentUser = "admin"; // Ya que ejecutamos como system, usar admin como usuario

        logger.info("Procesando envío de documentos para usuario: " + currentUser +
                (siteId != null ? " en sitio: " + siteId : " en carpeta personal"));
        logger.info("Base URL configurada: " + baseUrl);

        DocumentSubmissionResult result = documentSubmissionService.processSubmission(siteId, currentUser);

        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");

        if (result.isSuccess()) {
            res.setStatus(Status.STATUS_OK);
        } else {
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
        }

        Writer writer = res.getWriter();
        writer.write(buildJsonResponse(result));
        writer.flush();
    }

    private String buildJsonResponse(DocumentSubmissionResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(result.isSuccess()).append(",");
        json.append("\"message\": \"").append(escapeJson(result.getMessage())).append("\",");
        json.append("\"baseUrl\": \"").append(escapeJson(baseUrl)).append("\"");

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
