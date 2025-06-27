// Webscript para eliminar archivo del formulario
var requestData = JSON.parse(requestbody.content);
var nodeRefArchivo = requestData.nodeRefArchivo || null;
var nombreArchivo = requestData.nombreArchivo || null;

try {
   if (!nodeRefArchivo) {
      status.code = 400;
      model.success = false;
      model.message = "NodeRef del archivo es requerido";
   } else {
      // Buscar el nodo por nodeRef
      var nodoAEliminar = search.findNode(nodeRefArchivo);

      if (nodoAEliminar && nodoAEliminar.exists()) {
         // Verificar que el usuario tenga permisos para eliminar
         if (nodoAEliminar.hasPermission("Delete")) {
            // Obtener información antes de eliminar
            var nombreReal = nodoAEliminar.name;

            // Eliminar el archivo
            nodoAEliminar.remove();

            // Respuesta exitosa
            model.success = true;
            model.message = "Archivo '" + nombreReal + "' eliminado exitosamente";
            model.archivoEliminado = nombreReal;
         } else {
            status.code = 403;
            model.success = false;
            model.message = "No tiene permisos para eliminar este archivo";
         }
      } else {
         status.code = 404;
         model.success = false;
         model.message = "El archivo no existe o no se puede acceder";
      }
   }

} catch (error) {
   status.code = 500;
   model.success = false;
   model.message = "Error interno: " + error.message;
}