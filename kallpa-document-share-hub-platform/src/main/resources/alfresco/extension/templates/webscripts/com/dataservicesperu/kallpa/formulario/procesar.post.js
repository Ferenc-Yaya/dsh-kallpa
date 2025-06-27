// Obtener datos del formulario
var requestData = JSON.parse(requestbody.content);
var campo1 = requestData.campo1 || "";
var campo2 = requestData.campo2 || "";
var campo3 = requestData.campo3 || "";
var nodeRefArchivo = requestData.nodeRefArchivo || null;
var nombreArchivo = requestData.nombreArchivo || null;
var esActualizacion = requestData.esActualizacion || false;

try {
   // Validar datos
   if (!campo1.trim() || !campo2.trim()) {
      status.code = 400;
      model.success = false;
      model.message = "Los campos 1 y 2 son obligatorios";
   } else {
      // Obtener el sitio actual desde los argumentos de la URL
      var siteId = null;

      // Método 1: Desde los argumentos de la URL
      if (args.site) {
         siteId = args.site;
      }

      // Método 2: Desde el contexto de la request
      if (!siteId && url.templateArgs.site) {
         siteId = url.templateArgs.site;
      }

      // Método 3: Desde el referer header (si viene del Share)
      if (!siteId) {
         var referer = headers.referer;
         if (referer && referer.indexOf('/site/') > -1) {
            var siteMatch = referer.match(/\/site\/([^\/]+)/);
            if (siteMatch) {
               siteId = siteMatch[1];
            }
         }
      }

      var targetFolder;

      if (siteId) {
         // Usar búsqueda directa por path para encontrar la carpeta del sitio
         try {
            var luceneQuery = 'PATH:"/app:company_home/st:sites/cm:' + siteId + '/cm:documentLibrary"';
            var nodes = search.luceneSearch(luceneQuery);
            if (nodes.length > 0) {
               targetFolder = nodes[0];
            } else {
               // Fallback: buscar la carpeta del sitio sin documentLibrary
               var siteQuery = 'PATH:"/app:company_home/st:sites/cm:' + siteId + '"';
               var siteNodes = search.luceneSearch(siteQuery);
               if (siteNodes.length > 0) {
                  targetFolder = siteNodes[0];
               } else {
                  throw new Error("No se pudo encontrar el sitio: " + siteId);
               }
            }
         } catch (searchError) {
            // Si falla la búsqueda, usar XPath como alternativa
            try {
               var xpathQuery = '/app:company_home/st:sites/cm:' + siteId + '/cm:documentLibrary';
               var xpathNodes = search.xpathSearch(xpathQuery);
               if (xpathNodes.length > 0) {
                  targetFolder = xpathNodes[0];
               } else {
                  throw new Error("No se pudo encontrar el sitio: " + siteId);
               }
            } catch (xpathError) {
               throw new Error("No se pudo encontrar el sitio: " + siteId);
            }
         }
      } else {
         // Fallback: usar la carpeta personal del usuario
         targetFolder = userhome;
      }

      var jsonFile;
      var esNuevoArchivo = false;
      var fechaActual = new Date().toISOString();

      // Generar contenido JSON base
      var jsonContent = {
         "campo1": campo1.trim(),
         "campo2": campo2.trim(),
         "campo3": campo3.trim(),
         "usuario": person.properties["cm:userName"],
         "sitio": siteId || "personal"
      };

      if (esActualizacion && nodeRefArchivo) {
         // ACTUALIZAR archivo existente
         try {
            var nodoExistente = search.findNode(nodeRefArchivo);
            if (nodoExistente && nodoExistente.exists()) {
               // Leer contenido actual para preservar fechaCreacion
               var contenidoActual = JSON.parse(nodoExistente.content);

               // Preservar fecha de creación y agregar fecha de modificación
               jsonContent.fechaCreacion = contenidoActual.fechaCreacion || fechaActual;
               jsonContent.fechaModificacion = fechaActual;

               // Actualizar contenido
               nodoExistente.content = JSON.stringify(jsonContent, null, 2);
               nodoExistente.save();

               jsonFile = nodoExistente;
               model.message = "Archivo actualizado exitosamente";
            } else {
               throw new Error("El archivo a actualizar no existe o no se puede acceder");
            }
         } catch (updateError) {
            // Si falla la actualización, crear nuevo archivo
            esNuevoArchivo = true;
         }
      } else {
         // CREAR nuevo archivo
         esNuevoArchivo = true;
      }

      if (esNuevoArchivo) {
         // Agregar fecha de creación
         jsonContent.fechaCreacion = fechaActual;

         // Buscar si ya existe un archivo del formulario
         var archivoExistente = null;
         var children = targetFolder.children;
         for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.isDocument &&
                child.name.indexOf("formulario_") === 0 &&
                child.name.indexOf(".json") > -1) {
               archivoExistente = child;
               break;
            }
         }

         if (archivoExistente) {
            // Actualizar archivo existente encontrado
            var contenidoExistente = JSON.parse(archivoExistente.content);
            jsonContent.fechaCreacion = contenidoExistente.fechaCreacion || fechaActual;
            jsonContent.fechaModificacion = fechaActual;

            archivoExistente.content = JSON.stringify(jsonContent, null, 2);
            archivoExistente.save();

            jsonFile = archivoExistente;
            model.message = "Archivo existente actualizado exitosamente";
         } else {
            // Crear archivo completamente nuevo
            var timestamp = new Date().getTime();
            var filename = "formulario_" + timestamp + ".json";

            jsonFile = targetFolder.createFile(filename);
            jsonFile.content = JSON.stringify(jsonContent, null, 2);
            jsonFile.mimetype = "application/json";

            model.message = "Archivo creado exitosamente";
         }
      }

      // Respuesta exitosa
      model.success = true;
      model.message += " en " + (siteId ? "sitio: " + siteId : "carpeta personal");
      model.filename = jsonFile.name;
      model.nodeRef = jsonFile.nodeRef.toString();
      model.siteId = siteId;
      model.esActualizacion = !esNuevoArchivo;
   }

} catch (error) {
   status.code = 500;
   model.success = false;
   model.message = "Error interno: " + error.message;
}