{
   "success": ${success?string("true", "false")},
   "message": "${message}"<#if filename??>,
   "filename": "${filename}"</#if><#if nodeRef??>,
   "nodeRef": "${nodeRef}"</#if><#if siteId??>,
   "siteId": "${siteId}"</#if><#if archivoEliminado??>,
   "archivoEliminado": "${archivoEliminado}"</#if>
}