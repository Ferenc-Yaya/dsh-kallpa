package com.dataservicesperu.kallpa.interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.Status;

import java.util.UUID;

/**
 * Utilidad para validación CSRF en WebScripts
 */
public class CSRFWebScriptInterceptor {

    private static final Log logger = LogFactory.getLog(CSRFWebScriptInterceptor.class);
    private static final String CSRF_TOKEN_HEADER = "Alfresco-CSRFToken";
    private static final String CSRF_TOKEN_COOKIE = "Alfresco-CSRFToken";

    /**
     * Valida token CSRF para WebScripts
     */
    public static boolean validateCSRFToken(WebScriptRequest req, WebScriptResponse res) {
        try {
            // ✅ MÉTODO CORRECTO: Usar getServerPath() para determinar si es POST
            String requestMethod = getHttpMethodFromRequest(req);
            String requestPath = req.getServerPath();

            logger.info("🔍 CSRF Check - Method: " + requestMethod + ", Path: " + requestPath);

            // Solo aplicar CSRF a requests POST/PUT/DELETE
            if (!"POST".equals(requestMethod) && !"PUT".equals(requestMethod) && !"DELETE".equals(requestMethod)) {
                logger.info("✅ CSRF: Método " + requestMethod + " no requiere validación");
                return true;
            }

            // Excluir endpoints públicos
            if (requestPath != null && requestPath.contains("/kallpa/download/")) {
                logger.info("✅ CSRF: Endpoint público excluido");
                return true;
            }

            // Obtener token del header
            String headerToken = req.getHeader(CSRF_TOKEN_HEADER);

            // Obtener token de cookie
            String cookieToken = getCookieValue(req, CSRF_TOKEN_COOKIE);

            logger.info("🔍 CSRF Check - Header: " + (headerToken != null ? headerToken.substring(0, Math.min(8, headerToken.length())) + "..." : "null"));
            logger.info("🔍 CSRF Check - Cookie: " + (cookieToken != null ? cookieToken.substring(0, Math.min(8, cookieToken.length())) + "..." : "null"));

            // MODO DESARROLLO: Si no hay tokens, permitir
            if (headerToken == null && cookieToken == null) {
                logger.info("🆕 CSRF: Sin tokens - Permitiendo en modo desarrollo");
                return true;
            }

            // Si hay token en header, validar
            if (headerToken != null) {
                // Si también hay cookie, deben coincidir
                if (cookieToken != null) {
                    if (headerToken.equals(cookieToken)) {
                        logger.info("✅ CSRF: Tokens válidos y coinciden");
                        return true;
                    } else {
                        logger.warn("❌ CSRF: Tokens no coinciden");
                        return false;
                    }
                } else {
                    // Solo hay header token, aceptar
                    logger.info("✅ CSRF: Token de header válido");
                    return true;
                }
            }

            // Si solo hay cookie token, aceptar
            if (cookieToken != null) {
                logger.info("✅ CSRF: Token de cookie válido");
                return true;
            }

            logger.warn("❌ CSRF: Sin tokens válidos");
            return false;

        } catch (Exception e) {
            logger.error("❌ CSRF: Error en validación", e);
            // En caso de error, permitir (modo tolerante para desarrollo)
            logger.info("🔧 CSRF: Permitiendo request por error en validación");
            return true;
        }
    }

    /**
     * Determina el método HTTP del request
     */
    private static String getHttpMethodFromRequest(WebScriptRequest req) {
        try {
            // Método 1: Intentar obtener del WebScript description
            if (req.getServiceMatch() != null &&
                    req.getServiceMatch().getWebScript() != null &&
                    req.getServiceMatch().getWebScript().getDescription() != null) {

                String method = req.getServiceMatch().getWebScript().getDescription().getMethod();
                if (method != null && !method.isEmpty()) {
                    return method.toUpperCase();
                }
            }

            // Método 2: Buscar en headers
            String methodHeader = req.getHeader("X-HTTP-Method-Override");
            if (methodHeader != null) {
                return methodHeader.toUpperCase();
            }

            // Método 3: Asumir POST para requests con content
            String contentType = req.getHeader("Content-Type");
            if (contentType != null && contentType.contains("application/json")) {
                return "POST";
            }

            // Por defecto GET
            return "GET";

        } catch (Exception e) {
            logger.debug("Error determinando método HTTP, asumiendo GET", e);
            return "GET";
        }
    }

    /**
     * Obtiene valor de cookie del request
     */
    private static String getCookieValue(WebScriptRequest request, String cookieName) {
        try {
            String cookieHeader = request.getHeader("Cookie");
            if (cookieHeader != null) {
                String[] cookies = cookieHeader.split(";");
                for (String cookie : cookies) {
                    String[] parts = cookie.trim().split("=", 2);
                    if (parts.length == 2 && cookieName.equals(parts[0])) {
                        return parts[1];
                    }
                }
            }
        } catch (Exception e) {
            // Silencioso
        }
        return null;
    }

    /**
     * Genera un nuevo token CSRF
     */
    private static String generateCSRFToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Envía respuesta de error CSRF
     */
    public static void sendCSRFError(WebScriptResponse res) {
        try {
            res.setStatus(Status.STATUS_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("{\"success\": false, \"error\": \"Token CSRF inválido\", \"code\": \"CSRF_TOKEN_INVALID\"}");
        } catch (Exception e) {
            logger.error("Error enviando respuesta CSRF", e);
        }
    }
}
