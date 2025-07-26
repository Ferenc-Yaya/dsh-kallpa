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
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.Status;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class DocumentStatsWebScript extends AbstractWebScript {

    private static final Log logger = LogFactory.getLog(DocumentStatsWebScript.class);

    private final ServiceRegistry serviceRegistry;
    private final TaggingService taggingService;
    private final SiteService siteService;


    public DocumentStatsWebScript(ServiceRegistry serviceRegistry, TaggingService taggingService) {
        this.serviceRegistry = serviceRegistry;
        this.taggingService = taggingService;
        this.siteService = serviceRegistry.getSiteService();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        try {
            logger.info("Generando estadísticas de documentos por sitio");

            Map<String, SiteStats> siteStatsMap = generateSiteStats();

            res.setContentType("application/json");
            res.setContentEncoding("UTF-8");
            res.setStatus(Status.STATUS_OK);

            Writer writer = res.getWriter();
            String jsonResponse = buildJsonResponse(siteStatsMap);
            writer.write(jsonResponse);
            writer.flush();

        } catch (Exception e) {
            logger.error("Error generando estadísticas de documentos", e);
            res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
            res.setContentType("application/json");
            res.getWriter().write("{\"success\": false, \"message\": \"Error interno: " +
                    e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    private Map<String, SiteStats> generateSiteStats() {
        Map<String, SiteStats> siteStatsMap = new HashMap<>();
        SiteService siteService = serviceRegistry.getSiteService();

        try {
            List<SiteInfo> sites = siteService.listSites(null, null);

            for (SiteInfo site : sites) {
                String siteName = site.getTitle();
                if (siteName == null || siteName.trim().isEmpty()) {
                    siteName = site.getShortName();
                }

                logger.info("Procesando sitio: " + siteName);

                SiteStats stats = new SiteStats(siteName);
                processDocumentsInSite(site, stats);

                if (stats.getTotal() > 0) {
                    siteStatsMap.put(siteName, stats);
                }
            }

        } catch (Exception e) {
            logger.error("Error en búsqueda de sitios", e);
            throw e;
        }

        return siteStatsMap;
    }

    private void processDocumentsInSite(SiteInfo site, SiteStats stats) {
        try {
            NodeRef documentLibrary = siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

            if (documentLibrary != null && serviceRegistry.getNodeService().exists(documentLibrary)) {
                processDocumentsRecursively(documentLibrary, stats);
            }

        } catch (Exception e) {
            logger.warn("Error procesando documentos del sitio: " + site.getShortName(), e);
        }
    }

    private void processDocumentsRecursively(NodeRef folder, SiteStats stats) {
        try {
            List<ChildAssociationRef> children = serviceRegistry.getNodeService().getChildAssocs(folder);

            for (ChildAssociationRef child : children) {
                NodeRef childNode = child.getChildRef();

                if (serviceRegistry.getNodeService().exists(childNode)) {
                    if (serviceRegistry.getDictionaryService().isSubClass(
                            serviceRegistry.getNodeService().getType(childNode), ContentModel.TYPE_CONTENT)) {

                        analyzeDocument(childNode, stats);

                    } else if (serviceRegistry.getDictionaryService().isSubClass(
                            serviceRegistry.getNodeService().getType(childNode), ContentModel.TYPE_FOLDER)) {

                        processDocumentsRecursively(childNode, stats);
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Error procesando carpeta: " + folder, e);
        }
    }

    private void analyzeDocument(NodeRef nodeRef, SiteStats stats) {
        try {
            List<String> tags = taggingService.getTags(nodeRef);

            boolean hasApproved = false;
            boolean hasDisapproved = false;

            for (String tag : tags) {
                if ("APROBADO".equalsIgnoreCase(tag)) {
                    hasApproved = true;
                } else if ("DESAPROBADO".equalsIgnoreCase(tag)) {
                    hasDisapproved = true;
                }
            }

            if (hasApproved) {
                stats.incrementApproved();
            } else if (hasDisapproved) {
                stats.incrementDisapproved();
            } else {
                stats.incrementUnclassified();
            }

            stats.incrementTotal();

        } catch (Exception e) {
            logger.warn("Error analizando documento: " + nodeRef, e);
        }
    }

    private String buildJsonResponse(Map<String, SiteStats> siteStatsMap) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\": true,");
        json.append("\"sites\": [");

        boolean first = true;
        for (SiteStats stats : siteStatsMap.values()) {
            if (!first) {
                json.append(",");
            }
            first = false;

            json.append("{");
            json.append("\"siteName\": \"").append(escapeJson(stats.getSiteName())).append("\",");
            json.append("\"stats\": {");
            json.append("\"approved\": ").append(stats.getApproved()).append(",");
            json.append("\"disapproved\": ").append(stats.getDisapproved()).append(",");
            json.append("\"unclassified\": ").append(stats.getUnclassified()).append(",");
            json.append("\"total\": ").append(stats.getTotal());
            json.append("}");
            json.append("}");
        }

        json.append("],");
        json.append("\"timestamp\": \"").append(java.time.Instant.now().toString()).append("\"");
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

    private static class SiteStats {
        private final String siteName;
        private int approved = 0;
        private int disapproved = 0;
        private int unclassified = 0;
        private int total = 0;

        public SiteStats(String siteName) {
            this.siteName = siteName;
        }

        public void incrementApproved() { approved++; }
        public void incrementDisapproved() { disapproved++; }
        public void incrementUnclassified() { unclassified++; }
        public void incrementTotal() { total++; }

        public String getSiteName() { return siteName; }
        public int getApproved() { return approved; }
        public int getDisapproved() { return disapproved; }
        public int getUnclassified() { return unclassified; }
        public int getTotal() { return total; }
    }
}
