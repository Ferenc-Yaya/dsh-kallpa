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
            // Log información básica para debugging CSRF
            logger.info("=== DocumentSubmissionWebScript - REQUEST INFO ===");
            logger.info("URL: " + req.getURL());
            logger.info("Content-Type: " + req.getHeader("Content-Type"));
            logger.info("Referer: " + req.getHeader("Referer"));

            // Log específico del token CSRF
            String csrfToken = req.getHeader("Alfresco-CSRFToken");
            if (csrfToken != null && !csrfToken.trim().isEmpty()) {
                logger.info("🔒 CSRF Token presente: " + csrfToken.substring(0, Math.min(8, csrfToken.length())) + "...");
            } else {
                logger.info("⚠️ CSRF Token: NO PRESENTE");
            }

            // Obtener parámetros
            String siteId = req.getParameter("site");
            String currentUser = serviceRegistry.getAuthenticationService().getCurrentUserName();

            logger.info("Usuario: " + currentUser);
            logger.info("Sitio: " + (siteId != null ? siteId : "carpeta personal"));

            // Procesar la solicitud
            DocumentSubmissionResult result = documentSubmissionService.processSubmission(siteId, currentUser);

            // Configurar respuesta
            res.setContentType("application/json");
            res.setContentEncoding("UTF-8");

            // Headers CORS para desarrollo
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

            // Escribir respuesta JSON
            Writer writer = res.getWriter();
            String jsonResponse = buildJsonResponse(result);
            writer.write(jsonResponse);
            writer.flush();

        } catch (Exception e) {
            logger.error("❌ Error en DocumentSubmissionWebScript", e);

            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");

            Writer writer = res.getWriter();
            writer.write("{\"success\": false, \"message\": \"Error interno: " +
                    escapeJson(e.getMessage()) + "\"}");
            writer.flush();
        }
    }

    private String buildJsonResponse(DocumentSubmissionResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": ").append(result.isSuccess()).append(",");
        json.append("\"message\": \"").append(escapeJson(result.getMessage())).append("\"");
        json.append(",\"timestamp\": \"").append(java.time.Instant.now().toString()).append("\"");

        if (baseUrl != null) {
            json.append(",\"baseUrl\": \"").append(escapeJson(baseUrl)).append("\"");
        }

        if (result.getFilename() != null) {
            json.append(",\"filename\": \"").append(escapeJson(result.getFilename())).append("\"");
        }

        if (result.getNodeRef() != null) {
            json.append(",\"nodeRef\": \"").append(escapeJson(result.getNodeRef())).append("\"");
        }

        // Información del usuario actual
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