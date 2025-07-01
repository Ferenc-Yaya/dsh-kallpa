package com.dataservicesperu.kallpa.webscripts;

import com.dataservicesperu.kallpa.model.EmpleadoData;
import com.dataservicesperu.kallpa.service.EmpleadoService;
import org.springframework.extensions.webscripts.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormularioWebScript extends AbstractWebScript {

    private EmpleadoService empleadoService;
    private AuthenticationService authenticationService;

    public void setEmpleadoService(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
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
            e.printStackTrace(); // Para debug
        }

        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");

        JSONObject jsonResponse = new JSONObject(response);
        res.getWriter().write(jsonResponse.toString());
    }

    private void handleGet(String siteId, Map<String, Object> response) {
        List<EmpleadoData> empleados = empleadoService.cargarDatos(siteId);

        response.put("tieneArchivo", !empleados.isEmpty());
        response.put("siteId", siteId != null ? siteId : "");
        response.put("totalEmpleados", empleados.size());

        JSONArray empleadosArray = new JSONArray();
        String nodeRefArchivo = "";
        String nombreArchivo = "";

        if (!empleados.isEmpty()) {
            nodeRefArchivo = empleados.get(0).getNodeRef() != null ? empleados.get(0).getNodeRef() : "";
            nombreArchivo = empleados.get(0).getNombreArchivo() != null ? empleados.get(0).getNombreArchivo() : "";

            for (EmpleadoData empleado : empleados) {
                JSONObject empJson = new JSONObject();
                empJson.put("numero", empleado.getNumero() != null ? empleado.getNumero() : "");
                empJson.put("nombreApellidos", empleado.getNombreApellidos() != null ? empleado.getNombreApellidos() : "");
                empJson.put("dni", empleado.getDni() != null ? empleado.getDni() : "");
                empJson.put("puestoTrabajo", empleado.getPuestoTrabajo() != null ? empleado.getPuestoTrabajo() : "");
                empleadosArray.put(empJson);
            }
        }

        response.put("empleados", empleadosArray.toList());
        response.put("nodeRefArchivo", nodeRefArchivo);
        response.put("nombreArchivo", nombreArchivo);
    }

    private void handlePost(WebScriptRequest req, String siteId, String usuario, Map<String, Object> response) throws IOException {
        String body = req.getContent().getContent();
        JSONObject requestData = new JSONObject(body);

        String operacion = requestData.optString("operacion", "procesar");

        if ("eliminar".equals(operacion)) {
            String nodeRef = requestData.optString("nodeRefArchivo");
            boolean eliminado = empleadoService.eliminarArchivo(nodeRef);

            if (eliminado) {
                response.put("message", "Archivo eliminado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Error al eliminar el archivo");
            }
        } else {
            EmpleadoData datos = new EmpleadoData();
            datos.setNumero(requestData.optString("numero"));
            datos.setNombreApellidos(requestData.optString("nombreApellidos"));
            datos.setDni(requestData.optString("dni"));
            datos.setPuestoTrabajo(requestData.optString("puestoTrabajo"));

            EmpleadoData resultado = empleadoService.guardarDatos(datos, siteId, usuario);

            response.put("message", "Empleado agregado exitosamente");
            response.put("filename", resultado.getNombreArchivo());
            response.put("nodeRef", resultado.getNodeRef());
            response.put("siteId", siteId);
            response.put("numeroAsignado", resultado.getNumero());
        }
    }
}