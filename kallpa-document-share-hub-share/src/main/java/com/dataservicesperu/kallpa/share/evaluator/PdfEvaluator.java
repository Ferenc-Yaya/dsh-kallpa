package com.dataservicesperu.kallpa.share.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.repo.content.MimetypeMap;

public class PdfEvaluator extends BaseEvaluator {
    private static Log logger = LogFactory.getLog(PdfEvaluator.class);

    @Override
    public boolean evaluate(JSONObject jsonObject) {
        try {
            String mimetype = getNodeMimetype(jsonObject);
            logger.debug("PdfEvaluator - Evaluando nodo con mimetype: " + mimetype);

            return mimetype != null && mimetype.equals(MimetypeMap.MIMETYPE_PDF);
        } catch (Exception e) {
            logger.error("PdfEvaluator - Error al evaluar el tipo de archivo", e);
            return false;
        }
    }
}
