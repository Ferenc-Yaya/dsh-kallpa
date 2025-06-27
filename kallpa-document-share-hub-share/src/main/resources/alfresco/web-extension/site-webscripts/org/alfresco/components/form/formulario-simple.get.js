// Controlador GET simplificado para el formulario
try {
   // Obtener el sitio desde la URL
   var siteId = null;

   if (args.site) {
      siteId = args.site;
   } else if (url.templateArgs.site) {
      siteId = url.templateArgs.site;
   } else {
      // Extraer sitio de la URL completa
      var fullUrl = url.full || "";
      var siteMatch = fullUrl.match(/\/site\/([^\/\?]+)/);
      if (siteMatch) {
         siteId = siteMatch[1];
      }
   }

   // Intentar cargar datos del repository
   try {
      var connector = remote.connect("alfresco");
      var serviceUrl = "/formulario/cargar";

      if (siteId) {
         serviceUrl += "?site=" + encodeURIComponent(siteId);
      }

      var result = connector.get(serviceUrl);

      if (result.status == 200) {
         var data = JSON.parse(result);

         // Solo procesar si la respuesta es exitosa
         if (data.success !== false) {
            model.siteId = siteId;
            model.tieneArchivo = data.tieneArchivo || false;
            model.nombreArchivo = data.nombreArchivo || "";
            model.nodeRefArchivo = data.nodeRefArchivo || "";
            model.campo1 = data.campo1 || "";
            model.campo2 = data.campo2 || "";
            model.campo3 = data.campo3 || "";
            model.fechaCreacion = data.fechaCreacion || "";
            model.fechaModificacion = data.fechaModificacion || "";

            if (data.error) {
               model.error = data.error;
            }
         } else {
            // Error en el repository
            model.error = data.error || "Error desconocido del repositorio";
            setValoresVacios();
         }
      } else {
         // Error de conexión - valores por defecto
         model.error = "Error al conectar con el repositorio: " + result.status;
         setValoresVacios();
      }
   } catch (fetchError) {
      // Error de fetch - valores por defecto
      model.error = "Error de conexión: " + fetchError.message;
      setValoresVacios();
   }

   function setValoresVacios() {
      model.siteId = siteId;
      model.tieneArchivo = false;
      model.nombreArchivo = "";
      model.nodeRefArchivo = "";
      model.campo1 = "";
      model.campo2 = "";
      model.campo3 = "";
      model.fechaCreacion = "";
      model.fechaModificacion = "";
   }

} catch (error) {
   // Error general - valores por defecto
   model.siteId = siteId;
   model.tieneArchivo = false;
   model.nombreArchivo = "";
   model.nodeRefArchivo = "";
   model.campo1 = "";
   model.campo2 = "";
   model.campo3 = "";
   model.fechaCreacion = "";
   model.fechaModificacion = "";
   model.error = "Error al cargar formulario: " + error.message;
}