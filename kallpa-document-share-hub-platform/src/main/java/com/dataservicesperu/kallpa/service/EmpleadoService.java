package com.dataservicesperu.kallpa.service;

import com.dataservicesperu.kallpa.model.EmpleadoData;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.model.ContentModel;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.Serializable;
import java.util.*;

public class EmpleadoService {

    private NodeService nodeService;
    private SearchService searchService;
    private SiteService siteService;
    private ContentService contentService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public List<EmpleadoData> cargarDatos(String siteId) {
        NodeRef carpeta = obtenerCarpetaDestino(siteId);
        NodeRef archivoExistente = buscarArchivoEmpleados(carpeta);

        if (archivoExistente != null) {
            return leerDatosArchivo(archivoExistente);
        }

        return new ArrayList<>();
    }

    public EmpleadoData guardarDatos(EmpleadoData datos, String siteId, String usuario) {
        NodeRef carpeta = obtenerCarpetaDestino(siteId);
        NodeRef archivoExistente = buscarArchivoEmpleados(carpeta);

        datos.setUsuario(usuario);
        datos.setSitio(siteId != null ? siteId : "personal");
        datos.setFechaModificacion(new Date());

        List<EmpleadoData> empleados;
        if (archivoExistente != null) {
            empleados = leerDatosArchivo(archivoExistente);
        } else {
            empleados = new ArrayList<>();
        }

        // Generar número automático
        if (datos.getNumero() == null || datos.getNumero().trim().isEmpty()) {
            datos.setNumero(generarSiguienteNumero(empleados));
        }

        datos.setFechaCreacion(new Date());
        empleados.add(datos);

        if (archivoExistente != null) {
            escribirDatosArchivo(archivoExistente, empleados);
            datos.setNodeRef(archivoExistente.toString());
            datos.setNombreArchivo((String) nodeService.getProperty(archivoExistente, ContentModel.PROP_NAME));
        } else {
            String nombreArchivo = "empleados_" + System.currentTimeMillis() + ".json";
            NodeRef nuevoArchivo = crearArchivo(carpeta, nombreArchivo, empleados);
            datos.setNodeRef(nuevoArchivo.toString());
            datos.setNombreArchivo(nombreArchivo);
        }

        return datos;
    }

    public boolean eliminarArchivo(String nodeRefStr) {
        try {
            NodeRef nodeRef = new NodeRef(nodeRefStr);
            if (nodeService.exists(nodeRef)) {
                nodeService.deleteNode(nodeRef);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String generarSiguienteNumero(List<EmpleadoData> empleados) {
        int maxNumero = 0;
        for (EmpleadoData emp : empleados) {
            try {
                int num = Integer.parseInt(emp.getNumero());
                if (num > maxNumero) {
                    maxNumero = num;
                }
            } catch (NumberFormatException e) {
                // Ignorar
            }
        }
        return String.valueOf(maxNumero + 1);
    }

    private NodeRef obtenerCarpetaDestino(String siteId) {
        if (siteId != null && !siteId.isEmpty()) {
            try {
                return siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private NodeRef buscarArchivoEmpleados(NodeRef carpeta) {
        if (carpeta == null) return null;

        List<ChildAssociationRef> children = nodeService.getChildAssocs(carpeta);
        for (ChildAssociationRef child : children) {
            NodeRef nodeRef = child.getChildRef();
            String nombre = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

            if (nombre != null && nombre.startsWith("empleados_") && nombre.endsWith(".json")) {
                return nodeRef;
            }
        }
        return null;
    }

    private List<EmpleadoData> leerDatosArchivo(NodeRef nodeRef) {
        List<EmpleadoData> empleados = new ArrayList<>();

        try {
            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            String contenido = reader.getContentString();

            JSONObject json = new JSONObject(contenido);
            JSONArray empleadosArray = json.optJSONArray("empleados");

            if (empleadosArray != null) {
                for (int i = 0; i < empleadosArray.length(); i++) {
                    JSONObject empJson = empleadosArray.getJSONObject(i);
                    EmpleadoData empleado = new EmpleadoData();

                    if (empJson.has("numero")) empleado.setNumero(empJson.getString("numero"));
                    if (empJson.has("nombreApellidos")) empleado.setNombreApellidos(empJson.getString("nombreApellidos"));
                    if (empJson.has("dni")) empleado.setDni(empJson.getString("dni"));
                    if (empJson.has("puestoTrabajo")) empleado.setPuestoTrabajo(empJson.getString("puestoTrabajo"));
                    if (empJson.has("usuario")) empleado.setUsuario(empJson.getString("usuario"));
                    if (empJson.has("sitio")) empleado.setSitio(empJson.getString("sitio"));

                    empleado.setNodeRef(nodeRef.toString());
                    empleado.setNombreArchivo((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
                    empleados.add(empleado);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return empleados;
    }

    private void escribirDatosArchivo(NodeRef nodeRef, List<EmpleadoData> empleados) {
        try {
            JSONObject json = new JSONObject();
            json.put("fechaCreacion", new Date().toString());
            json.put("fechaModificacion", new Date().toString());
            json.put("totalEmpleados", empleados.size());

            JSONArray empleadosArray = new JSONArray();
            for (EmpleadoData empleado : empleados) {
                JSONObject empJson = new JSONObject();
                empJson.put("numero", empleado.getNumero() != null ? empleado.getNumero() : "");
                empJson.put("nombreApellidos", empleado.getNombreApellidos() != null ? empleado.getNombreApellidos() : "");
                empJson.put("dni", empleado.getDni() != null ? empleado.getDni() : "");
                empJson.put("puestoTrabajo", empleado.getPuestoTrabajo() != null ? empleado.getPuestoTrabajo() : "");
                empJson.put("usuario", empleado.getUsuario() != null ? empleado.getUsuario() : "");
                empJson.put("sitio", empleado.getSitio() != null ? empleado.getSitio() : "");
                empJson.put("fechaCreacion", empleado.getFechaCreacion() != null ? empleado.getFechaCreacion().toString() : "");
                empJson.put("fechaModificacion", empleado.getFechaModificacion() != null ? empleado.getFechaModificacion().toString() : "");
                empleadosArray.put(empJson);
            }
            json.put("empleados", empleadosArray);

            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/json");
            writer.putContent(json.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NodeRef crearArchivo(NodeRef carpeta, String nombre, List<EmpleadoData> empleados) {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, nombre);

        NodeRef archivo = nodeService.createNode(
                carpeta,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(ContentModel.USER_MODEL_URI, nombre),
                ContentModel.TYPE_CONTENT,
                props
        ).getChildRef();

        escribirDatosArchivo(archivo, empleados);
        return archivo;
    }
}
