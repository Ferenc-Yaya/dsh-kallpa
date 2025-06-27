package com.dataservicesperu.kallpa.service;

import com.dataservicesperu.kallpa.model.FormularioData;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.model.ContentModel;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public class FormularioService {

    private NodeService nodeService;
    private SearchService searchService;
    private SiteService siteService;
    private ContentService contentService;

    // Setters para inyección de dependencias
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

    public FormularioData cargarDatos(String siteId) {
        NodeRef carpeta = obtenerCarpetaDestino(siteId);
        NodeRef archivoExistente = buscarArchivoFormulario(carpeta);

        if (archivoExistente != null) {
            return leerDatosArchivo(archivoExistente);
        }

        FormularioData datos = new FormularioData();
        datos.setSitio(siteId);
        return datos;
    }

    public FormularioData guardarDatos(FormularioData datos, String siteId, String usuario) {
        NodeRef carpeta = obtenerCarpetaDestino(siteId);
        NodeRef archivoExistente = buscarArchivoFormulario(carpeta);

        datos.setUsuario(usuario);
        datos.setSitio(siteId != null ? siteId : "personal");
        datos.setFechaModificacion(new Date());

        if (archivoExistente != null) {
            FormularioData datosExistentes = leerDatosArchivo(archivoExistente);
            datos.setFechaCreacion(datosExistentes.getFechaCreacion());
            escribirDatosArchivo(archivoExistente, datos);
            datos.setNodeRef(archivoExistente.toString());
            datos.setNombreArchivo((String) nodeService.getProperty(archivoExistente, ContentModel.PROP_NAME));
        } else {
            datos.setFechaCreacion(new Date());
            String nombreArchivo = "formulario_" + System.currentTimeMillis() + ".json";
            NodeRef nuevoArchivo = crearArchivo(carpeta, nombreArchivo, datos);
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

    private NodeRef buscarArchivoFormulario(NodeRef carpeta) {
        if (carpeta == null) return null;

        List<ChildAssociationRef> children = nodeService.getChildAssocs(carpeta);
        for (ChildAssociationRef child : children) {
            NodeRef nodeRef = child.getChildRef();
            String nombre = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

            if (nombre != null && nombre.startsWith("formulario_") && nombre.endsWith(".json")) {
                return nodeRef;
            }
        }
        return null;
    }

    private FormularioData leerDatosArchivo(NodeRef nodeRef) {
        try {
            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            String contenido = reader.getContentString();

            JSONObject json = new JSONObject(contenido);
            FormularioData datos = new FormularioData();

            if (json.has("campo1")) datos.setCampo1(json.getString("campo1"));
            if (json.has("campo2")) datos.setCampo2(json.getString("campo2"));
            if (json.has("campo3")) datos.setCampo3(json.getString("campo3"));
            if (json.has("usuario")) datos.setUsuario(json.getString("usuario"));
            if (json.has("sitio")) datos.setSitio(json.getString("sitio"));

            datos.setNodeRef(nodeRef.toString());
            datos.setNombreArchivo((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

            return datos;
        } catch (Exception e) {
            e.printStackTrace();
            return new FormularioData();
        }
    }

    private void escribirDatosArchivo(NodeRef nodeRef, FormularioData datos) {
        try {
            JSONObject json = new JSONObject();
            json.put("campo1", datos.getCampo1() != null ? datos.getCampo1() : "");
            json.put("campo2", datos.getCampo2() != null ? datos.getCampo2() : "");
            json.put("campo3", datos.getCampo3() != null ? datos.getCampo3() : "");
            json.put("usuario", datos.getUsuario() != null ? datos.getUsuario() : "");
            json.put("sitio", datos.getSitio() != null ? datos.getSitio() : "");
            json.put("fechaCreacion", datos.getFechaCreacion() != null ? datos.getFechaCreacion().toString() : "");
            json.put("fechaModificacion", datos.getFechaModificacion() != null ? datos.getFechaModificacion().toString() : "");

            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/json");
            writer.putContent(json.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NodeRef crearArchivo(NodeRef carpeta, String nombre, FormularioData datos) {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, nombre);

        NodeRef archivo = nodeService.createNode(
                carpeta,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(ContentModel.USER_MODEL_URI, nombre),
                ContentModel.TYPE_CONTENT,
                props
        ).getChildRef();

        escribirDatosArchivo(archivo, datos);
        return archivo;
    }
}
