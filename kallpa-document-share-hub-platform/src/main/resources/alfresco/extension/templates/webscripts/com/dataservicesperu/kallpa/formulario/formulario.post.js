// Combina procesar.post.js + eliminar.post.js
var requestData = JSON.parse(requestbody.content);
var operacion = requestData.operacion || "procesar"; // "procesar" o "eliminar"

try {
   if (operacion === "eliminar") {
      eliminarArchivo(requestData);
   } else {
      procesarFormulario(requestData);
   }
} catch (error) {
   status.code = 500;
   model.success = false;
   model.message = "Error interno: " + error.message;
}

function eliminarArchivo(data) {
   var nodeRefArchivo = data.nodeRefArchivo || null;

   if (!nodeRefArchivo) {
      status.code = 400;
      model.success = false;
      model.message = "NodeRef del archivo es requerido";
      return;
   }

   var nodoAEliminar = search.findNode(nodeRefArchivo);

   if (nodoAEliminar && nodoAEliminar.exists()) {
      if (nodoAEliminar.hasPermission("Delete")) {
         var nombreReal = nodoAEliminar.name;
         nodoAEliminar.remove();

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

function procesarFormulario(data) {
   var campo1 = data.campo1 || "";
   var campo2 = data.campo2 || "";
   var campo3 = data.campo3 || "";
   var nodeRefArchivo = data.nodeRefArchivo || null;
   var nombreArchivo = data.nombreArchivo || null;
   var esActualizacion = data.esActualizacion || false;

   // Validar datos
   if (!campo1.trim() || !campo2.trim()) {
      status.code = 400;
      model.success = false;
      model.message = "Los campos 1 y 2 son obligatorios";
      return;
   }

   // Obtener sitio
   var siteId = args.site || url.templateArgs.site;
   if (!siteId) {
      var referer = headers.referer;
      if (referer && referer.indexOf('/site/') > -1) {
         var siteMatch = referer.match(/\/site\/([^\/]+)/);
         if (siteMatch) siteId = siteMatch[1];
      }
   }

   var targetFolder = buscarCarpeta(siteId);
   var fechaActual = new Date().toISOString();

   var jsonContent = {
      "campo1": campo1.trim(),
      "campo2": campo2.trim(),
      "campo3": campo3.trim(),
      "usuario": person.properties["cm:userName"],
      "sitio": siteId || "personal"
   };

   var jsonFile;

   if (esActualizacion && nodeRefArchivo) {
      // Actualizar existente
      var nodoExistente = search.findNode(nodeRefArchivo);
      if (nodoExistente && nodoExistente.exists()) {
         var contenidoActual = JSON.parse(nodoExistente.content);
         jsonContent.fechaCreacion = contenidoActual.fechaCreacion || fechaActual;
         jsonContent.fechaModificacion = fechaActual;

         nodoExistente.content = JSON.stringify(jsonContent, null, 2);
         nodoExistente.save();
         jsonFile = nodoExistente;
         model.message = "Archivo actualizado exitosamente";
      } else {
         crearNuevoArchivo();
      }
   } else {
      crearNuevoArchivo();
   }

   function crearNuevoArchivo() {
      jsonContent.fechaCreacion = fechaActual;

      // Buscar archivo existente
      var archivoExistente = buscarArchivoFormulario(targetFolder);

      if (archivoExistente) {
         var contenidoExistente = JSON.parse(archivoExistente.content);
         jsonContent.fechaCreacion = contenidoExistente.fechaCreacion || fechaActual;
         jsonContent.fechaModificacion = fechaActual;

         archivoExistente.content = JSON.stringify(jsonContent, null, 2);
         archivoExistente.save();
         jsonFile = archivoExistente;
         model.message = "Archivo existente actualizado exitosamente";
      } else {
         var timestamp = new Date().getTime();
         var filename = "formulario_" + timestamp + ".json";

         jsonFile = targetFolder.createFile(filename);
         jsonFile.content = JSON.stringify(jsonContent, null, 2);
         jsonFile.mimetype = "application/json";
         model.message = "Archivo creado exitosamente";
      }
   }

   model.success = true;
   model.message += " en " + (siteId ? "sitio: " + siteId : "carpeta personal");
   model.filename = jsonFile.name;
   model.nodeRef = jsonFile.nodeRef.toString();
   model.siteId = siteId;
}

// Funciones auxiliares
function buscarCarpeta(siteId) {
   if (siteId) {
      try {
         var query = 'TYPE:"cm:folder" AND PATH:"/app:company_home/st:sites/cm:' + siteId + '/cm:documentLibrary"';
         var results = search.query({query: query, language: "fts-alfresco"});
         return results.length > 0 ? results[0] : userhome;
      } catch (e) {
         return userhome;
      }
   }
   return userhome;
}

function buscarArchivoFormulario(carpeta) {
   if (!carpeta) return null;

   for (var i = 0; i < carpeta.children.length; i++) {
      var child = carpeta.children[i];
      if (child.isDocument &&
          child.name.indexOf("formulario_") === 0 &&
          child.name.indexOf(".json") > -1) {
         try {
            var datos = JSON.parse(child.content);
            if (datos.campo1 !== undefined || datos.campo2 !== undefined) {
               return child;
            }
         } catch (e) {
            continue;
         }
      }
   }
   return null;
}