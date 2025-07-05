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

    private final DocumentSubmissionService documentSubmissionService;
    private final ServiceRegistry serviceRegistry;
    private final String baseUrl;

    public DocumentSubmissionWebScript(DocumentSubmissionService documentSubmissionService,
                                       ServiceRegistry serviceRegistry,
                                       @Value("${alfresco.base.url}") String baseUrl) {
        this.documentSubmissionService = documentSubmissionService;
        this.serviceRegistry = serviceRegistry;
        this.baseUrl = baseUrl;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            logger.info("=== DocumentSubmissionWebScript - REQUEST INFO ===");
            logger.info("URL: " + req.getURL());
            logger.info("Content-Type: " + req.getHeader("Content-Type"));
            logger.info("Referer: " + req.getHeader("Referer"));

            String csrfToken = req.getHeader("Alfresco-CSRFToken");
            if (csrfToken != null && !csrfToken.trim().isEmpty()) {
                logger.info("🔒 CSRF Token presente: " + csrfToken.substring(0, Math.min(8, csrfToken.length())) + "...");
            } else {
                logger.info("⚠️ CSRF Token: NO PRESENTE");
            }

            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                @Override
                public Void doWork() throws Exception {
                    executeAsSystem(req, res);
                    return null;
                }
            });

        } catch (Exception e) {
            logger.error("❌ Error en DocumentSubmissionWebScript", e);
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
        String siteId = req.getParameter("site");
        String currentUser = "admin";

        logger.info("Procesando envío de documentos para usuario: " + currentUser +
                (siteId != null ? " en sitio: " + siteId : " en carpeta personal"));
        logger.info("Base URL configurada: " + baseUrl);

        DocumentSubmissionResult result = documentSubmissionService.processSubmission(siteId, currentUser);

        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");

        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Alfresco-CSRFToken");

        if (result.isSuccess()) {
            res.setStatus(Status.STATUS_OK);
            logger.info("✅ Procesamiento exitoso: " + result.getMessage());
        } else {
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            logger.error("❌ Error en procesamiento: " + result.getMessage());
        }

        Writer writer = res.getWriter();
        String jsonResponse = buildJsonResponse(result);
        writer.write(jsonResponse);
        writer.flush();
    }

    private String buildJsonResponse(DocumentSubmissionResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(result.isSuccess()).append(",");
        json.append("\"message\": \"").append(escapeJson(result.getMessage())).append("\",");
        json.append("\"timestamp\": \"").append(java.time.Instant.now().toString()).append("\"");

        if (baseUrl != null) {
            json.append(",\"baseUrl\": \"").append(escapeJson(baseUrl)).append("\"");
        } else {
            json.append(",\"baseUrl\": \"\"");
        }

        if (result.getFilename() != null) {
            json.append(",\"filename\": \"").append(escapeJson(result.getFilename())).append("\"");
        }

        if (result.getNodeRef() != null) {
            json.append(",\"nodeRef\": \"").append(escapeJson(result.getNodeRef())).append("\"");
        }

        try {
            String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();
            json.append(",\"user\": \"").append(escapeJson(currentUser)).append("\"");
        } catch (Exception e) {
            logger.warn("No se pudo obtener usuario actual: " + e.getMessage());
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