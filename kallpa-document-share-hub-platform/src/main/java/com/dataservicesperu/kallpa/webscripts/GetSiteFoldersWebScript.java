package com.dataservicesperu.kallpa.webscripts;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.Status;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetSiteFoldersWebScript extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(GetSiteFoldersWebScript.class);

    private final ServiceRegistry serviceRegistry;

    public GetSiteFoldersWebScript(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String siteId = templateVars.get("siteId");

            logger.info("Obteniendo carpetas para sitio: " + siteId);

            if (siteId == null || siteId.trim().isEmpty()) {
                res.setStatus(Status.STATUS_BAD_REQUEST);
                res.getWriter().write("{\"success\": false, \"message\": \"Site ID requerido\"}");
                return;
            }

            List<FolderInfo> folders = getSiteFolders(siteId);

            res.setContentType("application/json");
            res.setContentEncoding("UTF-8");
            res.setStatus(Status.STATUS_OK);

            Writer writer = res.getWriter();
            String jsonResponse = buildJsonResponse(folders);
            writer.write(jsonResponse);
            writer.flush();

        } catch (Exception e) {
            logger.error("Error obteniendo carpetas del sitio", e);
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");
            res.getWriter().write("{\"success\": false, \"message\": \"Error interno: " +
                    e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    private List<FolderInfo> getSiteFolders(String siteId) {
        List<FolderInfo> folders = new ArrayList<>();

        try {
            SiteService siteService = serviceRegistry.getSiteService();
            SiteInfo site = siteService.getSite(siteId);

            if (site == null) {
                throw new RuntimeException("El sitio '" + siteId + "' no existe");
            }

            NodeRef documentLibrary = getDocumentLibrary(siteId);

            if (documentLibrary != null) {
                List<ChildAssociationRef> children = serviceRegistry.getNodeService()
                        .getChildAssocs(documentLibrary);

                for (ChildAssociationRef child : children) {
                    NodeRef childNode = child.getChildRef();

                    if (serviceRegistry.getDictionaryService().isSubClass(
                            serviceRegistry.getNodeService().getType(childNode),
                            ContentModel.TYPE_FOLDER)) {

                        String folderName = (String) serviceRegistry.getNodeService()
                                .getProperty(childNode, ContentModel.PROP_NAME);
                        String folderDescription = (String) serviceRegistry.getNodeService()
                                .getProperty(childNode, ContentModel.PROP_DESCRIPTION);

                        folders.add(new FolderInfo(childNode.getId(), folderName, folderDescription));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error obteniendo carpetas para sitio: " + siteId, e);
            throw e;
        }

        return folders;
    }

    private NodeRef getDocumentLibrary(String siteId) {
        try {
            SearchParameters searchParams = new SearchParameters();
            searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            searchParams.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            searchParams.setQuery("PATH:\"/app:company_home/st:sites/cm:" + siteId + "/cm:documentLibrary\"");
            searchParams.setLimit(1);

            ResultSet resultSet = serviceRegistry.getSearchService().query(searchParams);
            try {
                if (resultSet.length() > 0) {
                    return resultSet.getNodeRef(0);
                }
            } finally {
                resultSet.close();
            }

            return null;
        } catch (Exception e) {
            logger.error("Error buscando documentLibrary para sitio: " + siteId, e);
            return null;
        }
    }

    private String buildJsonResponse(List<FolderInfo> folders) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": true,");
        json.append("\"folders\": [");

        for (int i = 0; i < folders.size(); i++) {
            FolderInfo folder = folders.get(i);
            json.append("{");
            json.append("\"id\": \"").append(escapeJson(folder.getId())).append("\",");
            json.append("\"name\": \"").append(escapeJson(folder.getName())).append("\",");
            json.append("\"description\": \"").append(escapeJson(folder.getDescription())).append("\"");
            json.append("}");

            if (i < folders.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
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

    private static class FolderInfo {
        private final String id;
        private final String name;
        private final String description;

        public FolderInfo(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description != null ? description : "";
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}
