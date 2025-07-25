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

import java.io.Serializable;
import java.util.List;

public class AddApprovedTagActionExecuter extends ActionExecuterAbstractBase {

    private static Log logger = LogFactory.getLog(AddApprovedTagActionExecuter.class);

    private final ServiceRegistry serviceRegistry;
    private final TaggingService taggingService;

    public AddApprovedTagActionExecuter(ServiceRegistry serviceRegistry, TaggingService taggingService) {
        this.serviceRegistry = serviceRegistry;
        this.taggingService = taggingService;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        // No parameters needed
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

            logger.info("Agregando etiqueta APROBADO al documento: " + fileName);

            NodeRef parentRef = serviceRegistry.getNodeService()
                    .getPrimaryParent(actionedUponNodeRef).getParentRef();

            taggingService.addTag(actionedUponNodeRef, "APROBADO");

            logger.info("Etiqueta APROBADO agregada exitosamente al documento: " + fileName);

        } catch (Exception e) {
            logger.error("Error agregando etiqueta APROBADO", e);
            throw new RuntimeException("Error agregando etiqueta APROBADO: " + e.getMessage());
        }
    }
}
