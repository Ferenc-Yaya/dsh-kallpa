package com.dataservicesperu.kallpa.actions;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class AddDisapprovedTagActionExecuter extends ActionExecuterAbstractBase {

    private static Log logger = LogFactory.getLog(AddDisapprovedTagActionExecuter.class);

    private final ServiceRegistry serviceRegistry;
    private final TaggingService taggingService;

    public AddDisapprovedTagActionExecuter(ServiceRegistry serviceRegistry, TaggingService taggingService) {
        this.serviceRegistry = serviceRegistry;
        this.taggingService = taggingService;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        try {
            if (!serviceRegistry.getNodeService().exists(actionedUponNodeRef)) {
                logger.warn("El nodo no existe: " + actionedUponNodeRef);
                return;
            }

            String fileName = (String) serviceRegistry.getNodeService()
                    .getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);

            logger.info("Procesando etiqueta DESAPROBADO para documento: " + fileName);

            List<String> currentTags = taggingService.getTags(actionedUponNodeRef);
            for (String tag : currentTags) {
                if ("APROBADO".equalsIgnoreCase(tag) || "DESAPROBADO".equalsIgnoreCase(tag)) {
                    taggingService.removeTag(actionedUponNodeRef, tag);
                    logger.info("Etiqueta eliminada: " + tag);
                }
            }

            taggingService.addTag(actionedUponNodeRef, "DESAPROBADO");

            logger.info("Etiqueta DESAPROBADO agregada exitosamente al documento: " + fileName);

        } catch (Exception e) {
            logger.error("Error agregando etiqueta DESAPROBADO", e);
            throw new RuntimeException("Error agregando etiqueta DESAPROBADO: " + e.getMessage());
        }
    }
}
