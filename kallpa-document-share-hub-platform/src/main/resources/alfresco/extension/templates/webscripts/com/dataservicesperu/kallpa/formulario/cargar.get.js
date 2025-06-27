// Webscript simplificado para cargar datos existentes del formulario
try {
   // Obtener el sitio desde argumentos
   var siteId = args.site || null;

   var targetFolder = null;
   var datosFormulario = null;
   var archivoExistente = null;

   // Debug: Log para ver qué sitio estamos buscando
   logger.info("Buscando datos para sitio: " + siteId);

   if (siteId) {
      // Buscar la carpeta del sitio usando búsqueda simple
      var query = 'TYPE:"cm:folder" AND PATH:"/app:company_home/st:sites/cm:' + siteId + '/cm:documentLibrary"';
      var searchResults = search.query({
         query: query,
         language: "fts-alfresco"
      });

      if (searchResults.length > 0) {
         targetFolder = searchResults[0];
         logger.info("Carpeta encontrada: " + targetFolder.displayPath);
      } else {
         logger.warn("No se encontró la carpeta documentLibrary para el sitio: " + siteId);
         // Intentar buscar solo la carpeta del sitio
         var siteQuery = 'TYPE:"cm:folder" AND PATH:"/app:company_home/st:sites/cm:' + siteId + '"';
         var siteResults = search.query({
            query: siteQuery,
            language: "fts-alfresco"
         });
         if (siteResults.length > 0) {
            targetFolder = siteResults[0];
            logger.info("Carpeta del sitio encontrada: " + targetFolder.displayPath);
         }
      }
   } else {
      // Sin sitio, usar carpeta personal
      targetFolder = userhome;
      logger.info("Usando carpeta personal del usuario");
   }

   // Buscar archivos del formulario
   if (targetFolder) {
      var children = targetFolder.children;
      logger.info("Buscando en carpeta con " + children.length + " elementos");

      for (var i = 0; i < children.length; i++) {
         var child = children[i];
         if (child.isDocument &&
             child.name.indexOf("formulario_") === 0 &&
             child.name.indexOf(".json") > -1) {

            logger.info("Archivo encontrado: " + child.name);
            try {
               var contenido = child.content;
               var jsonData = JSON.parse(contenido);

               if (jsonData.campo1 !== undefined || jsonData.campo2 !== undefined) {
                  datosFormulario = jsonData;
                  archivoExistente = child;
                  logger.info("Datos del formulario cargados exitosamente");
                  break;
               }
            } catch (parseError) {
               logger.warn("Error al parsear archivo " + child.name + ": " + parseError.message);
               continue;
            }
         }
      }
   }

   // Preparar respuesta
   model.success = true;
   model.siteId = siteId || "";
   model.tieneArchivo = (datosFormulario !== null);
   model.nombreArchivo = archivoExistente ? archivoExistente.name : "";
   model.nodeRefArchivo = archivoExistente ? archivoExistente.nodeRef.toString() : "";

   // Datos del formulario
   model.campo1 = datosFormulario ? (datosFormulario.campo1 || "") : "";
   model.campo2 = datosFormulario ? (datosFormulario.campo2 || "") : "";
   model.campo3 = datosFormulario ? (datosFormulario.campo3 || "") : "";
   model.fechaCreacion = datosFormulario ? (datosFormulario.fechaCreacion || "") : "";
   model.fechaModificacion = datosFormulario ? (datosFormulario.fechaModificacion || "") : "";

   logger.info("Respuesta preparada - tieneArchivo: " + model.tieneArchivo);

} catch (error) {
   logger.error("Error en cargar.get.js: " + error.message);

   // Respuesta de error
   model.success = false;
   model.siteId = "";
   model.tieneArchivo = false;
   model.nombreArchivo = "";
   model.nodeRefArchivo = "";
   model.campo1 = "";
   model.campo2 = "";
   model.campo3 = "";
   model.fechaCreacion = "";
   model.fechaModificacion = "";
   model.error = "Error al cargar datos: " + error.message;
}