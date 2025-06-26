// Obtener datos del formulario
var requestData = JSON.parse(requestbody.content);
var campo1 = requestData.campo1 || "";
var campo2 = requestData.campo2 || "";
var campo3 = requestData.campo3 || "";

try {
   // Validar datos
   if (!campo1.trim() || !campo2.trim()) {
      status.code = 400;
      model.success = false;
      model.message = "Los campos 1 y 2 son obligatorios";
   } else {
      // Obtener carpeta del usuario (Mis Ficheros)
      var userHome = userhome;

      // Generar contenido JSON
      var jsonContent = {
         "campo1": campo1.trim(),
         "campo2": campo2.trim(),
         "campo3": campo3.trim(),
         "fechaCreacion": new Date().toISOString(),
         "usuario": person.properties["cm:userName"]
      };

      // Crear nombre único para el archivo
      var timestamp = new Date().getTime();
      var filename = "formulario_" + timestamp + ".json";

      // Crear el archivo JSON
      var jsonFile = userHome.createFile(filename);
      jsonFile.content = JSON.stringify(jsonContent, null, 2);
      jsonFile.mimetype = "application/json";

      // Respuesta exitosa
      model.success = true;
      model.message = "Archivo creado exitosamente";
      model.filename = filename;
      model.nodeRef = jsonFile.nodeRef.toString();
   }

} catch (error) {
   status.code = 500;
   model.success = false;
   model.message = "Error interno: " + error.message;
}