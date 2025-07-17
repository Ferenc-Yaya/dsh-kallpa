package com.dataservicesperu.kallpa.webscripts;

import com.dataservicesperu.kallpa.services.DocumentSubmissionService;
import com.dataservicesperu.kallpa.services.DocumentSubmissionService.DocumentSubmissionResult;
import com.dataservicesperu.kallpa.interceptors.CSRFWebScriptInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

    private final DocumentSubmissionService documentSubmissionService;
    private final ServiceRegistry serviceRegistry;

    public DocumentSubmissionWebScript(DocumentSubmissionService documentSubmissionService,
                                       ServiceRegistry serviceRegistry) {
        this.documentSubmissionService = documentSubmissionService;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            logger.info("=== DocumentSubmissionWebScript - REQUEST INFO ===");
            logger.info("URL: " + req.getURL());
            logger.info("Content-Type: " + req.getHeader("Content-Type"));
            logger.info("Referer: " + req.getHeader("Referer"));

            if (!CSRFWebScriptInterceptor.validateCSRFToken(req, res)) {
                logger.warn("🚫 CSRF: Validación fallida");
                CSRFWebScriptInterceptor.sendCSRFError(res);
                return;
            }

            logger.info("✅ CSRF: Validación exitosa");

            final String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();
            logger.info("Usuario actual autenticado: " + currentUser);

            AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
                @Override
                public Void doWork() throws Exception {
                    executeWithCurrentUser(req, res, currentUser);
                    return null;
                }
            });

        } catch (Exception e) {
            logger.error("❌ Error en DocumentSubmissionWebScript", e);
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");
            Writer writer = res.getWriter();
            writer.write("{\"success\": false, \"message\": \"Error interno del servidor: " +
                    e.getMessage().replace("\"", "\\\"") + "\"}");
            writer.flush();
        }
    }

    private void executeWithCurrentUser(WebScriptRequest req, WebScriptResponse res, String currentUser) throws IOException {
        String siteId = req.getParameter("site");

        logger.info("Procesando envío de documentos para usuario: " + currentUser +
                (siteId != null ? " en sitio: " + siteId : " en carpeta personal"));

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
        String jsonResponse = buildJsonResponse(result, currentUser);
        writer.write(jsonResponse);
        writer.flush();
    }

    private String buildJsonResponse(DocumentSubmissionResult result, String currentUser) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(result.isSuccess()).append(",");
        json.append("\"message\": \"").append(escapeJson(result.getMessage())).append("\",");
        json.append("\"timestamp\": \"").append(java.time.Instant.now().toString()).append("\"");
        json.append(",\"user\": \"").append(escapeJson(currentUser)).append("\"");

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