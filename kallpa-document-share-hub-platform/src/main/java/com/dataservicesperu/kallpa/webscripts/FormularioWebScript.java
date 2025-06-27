package com.dataservicesperu.kallpa.webscripts;

import com.dataservicesperu.kallpa.model.FormularioData;
import com.dataservicesperu.kallpa.service.FormularioService;
import org.springframework.extensions.webscripts.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FormularioWebScript extends AbstractWebScript {

    private FormularioService formularioService;
    private AuthenticationService authenticationService;

    public void setFormularioService(FormularioService formularioService) {
        this.formularioService = formularioService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String siteId = req.getParameter("site");
        String usuario = authenticationService.getCurrentUserName();
        String webscriptId = req.getServiceMatch().getWebScript().getDescription().getId();

        Map<String, Object> response = new HashMap<String, Object>();

        try {
            if (webscriptId.contains("formulario.get")) {
                handleGet(siteId, response);
            } else if (webscriptId.contains("formulario.post")) {
                handlePost(req, siteId, usuario, response);
            }

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            res.setStatus(500);
        }

        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");

        JSONObject jsonResponse = new JSONObject(response);
        res.getWriter().write(jsonResponse.toString());
    }

    private void handleGet(String siteId, Map<String, Object> response) {
        FormularioData datos = formularioService.cargarDatos(siteId);

        response.put("tieneArchivo", datos.getNodeRef() != null);
        response.put("siteId", siteId != null ? siteId : "");
        response.put("nombreArchivo", datos.getNombreArchivo() != null ? datos.getNombreArchivo() : "");
        response.put("nodeRefArchivo", datos.getNodeRef() != null ? datos.getNodeRef() : "");
        response.put("campo1", datos.getCampo1() != null ? datos.getCampo1() : "");
        response.put("campo2", datos.getCampo2() != null ? datos.getCampo2() : "");
        response.put("campo3", datos.getCampo3() != null ? datos.getCampo3() : "");
        response.put("fechaCreacion", datos.getFechaCreacion() != null ? datos.getFechaCreacion().toString() : "");
        response.put("fechaModificacion", datos.getFechaModificacion() != null ? datos.getFechaModificacion().toString() : "");
    }

    private void handlePost(WebScriptRequest req, String siteId, String usuario, Map<String, Object> response) throws IOException {
        String body = req.getContent().getContent();
        JSONObject requestData = new JSONObject(body);

        String operacion = requestData.optString("operacion", "procesar");

        if ("eliminar".equals(operacion)) {
            String nodeRef = requestData.optString("nodeRefArchivo");
            boolean eliminado = formularioService.eliminarArchivo(nodeRef);

            if (eliminado) {
                response.put("message", "Archivo eliminado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Error al eliminar el archivo");
            }
        } else {
            FormularioData datos = new FormularioData();
            datos.setCampo1(requestData.optString("campo1"));
            datos.setCampo2(requestData.optString("campo2"));
            datos.setCampo3(requestData.optString("campo3"));

            FormularioData resultado = formularioService.guardarDatos(datos, siteId, usuario);

            response.put("message", "Archivo guardado exitosamente");
            response.put("filename", resultado.getNombreArchivo());
            response.put("nodeRef", resultado.getNodeRef());
            response.put("siteId", siteId);
        }
    }
}