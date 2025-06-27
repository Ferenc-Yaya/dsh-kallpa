{
   "success": ${success?string("true", "false")},
"tieneArchivo": ${tieneArchivo?string("true", "false")},
"siteId": "${siteId}",
"nombreArchivo": "${nombreArchivo}",
"nodeRefArchivo": "${nodeRefArchivo}",
"campo1": "${campo1}",
"campo2": "${campo2}",
"campo3": "${campo3}",
"fechaCreacion": "${fechaCreacion}",
"fechaModificacion": "${fechaModificacion}"<#if error??>,
"error": "${error}"</#if>
}