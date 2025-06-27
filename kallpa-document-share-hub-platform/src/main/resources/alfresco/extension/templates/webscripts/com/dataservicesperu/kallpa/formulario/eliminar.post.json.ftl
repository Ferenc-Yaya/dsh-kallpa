{
   "success": ${success?string("true", "false")},
   "message": "${message}",
   <#if archivoEliminado??>"archivoEliminado": "${archivoEliminado}"</#if>
}