try {
   var siteId = args["site"]; // Leer el parámetro 'site' de la URL

   var targetFolder;

   if (siteId) {
      var site = siteService.getSite(siteId);
      if (site != null) {
         targetFolder = site.getContainer("documentLibrary");
      } else {
         throw new Error("El sitio '" + siteId + "' no existe.");
      }
   } else {
      // Si no se pasa un site, usar la carpeta personal del usuario
      targetFolder = userhome;
   }

   // Nombre del archivo
   var filename = "ARCHIVOS SUBIDOS.txt";

   // ✅ Verificar si ya existe el archivo
   var existing = targetFolder.childByNamePath(filename);

   if (existing != null) {
      // Si ya existe, solo informar que ya fue enviado
      model.success = true;
      model.message = "El archivo de confirmación ya fue creado anteriormente";
      model.filename = filename;
      model.nodeRef = existing.nodeRef.toString();
   } else {
      // Crear contenido del archivo TXT (usando formato Windows para mejor compatibilidad)
      var now = new Date();
      var dateStr = now.getFullYear() + "-" +
                   String(now.getMonth() + 1).padStart(2, '0') + "-" +
                   String(now.getDate()).padStart(2, '0') + " " +
                   String(now.getHours()).padStart(2, '0') + ":" +
                   String(now.getMinutes()).padStart(2, '0');

      var txtContent = "ARCHIVOS SUBIDOS\r\n";
      txtContent += "================\r\n\r\n";
      txtContent += "Fecha: " + dateStr + "\r\n";
      txtContent += "Usuario: " + person.properties["cm:userName"] + "\r\n\r\n";
      txtContent += "Este archivo confirma que se han enviado documentos a revision.\r\n";

      // ✅ Crear el archivo usando el método simple que ya funcionaba
      var txtFile = targetFolder.createFile(filename);
      txtFile.content = txtContent;
      txtFile.mimetype = "text/plain";

      // Respuesta exitosa
      model.success = true;
      model.message = "Archivo de confirmación enviado exitosamente";
      model.filename = filename;
      model.nodeRef = txtFile.nodeRef.toString();
   }

} catch (error) {
   status.code = 500;
   model.success = false;
   model.message = "Error interno: " + error.message;
}

