<!DOCTYPE html>
<html>
<head>
   <title>Formulario Simple - DSH</title>
   <meta charset="utf-8">
   <style>
      body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
      .form-container {
         background: white; padding: 30px; border-radius: 8px;
         box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 600px; margin: 0 auto;
      }
      .form-header { text-align: center; margin-bottom: 30px; color: #333; }
      .form-field { margin-bottom: 20px; }
      .form-field label { display: block; font-weight: bold; margin-bottom: 5px; color: #555; }
      .form-field input, .form-field textarea {
         width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;
         box-sizing: border-box; font-size: 14px;
      }
      .btn {
         padding: 12px 30px; margin: 0 10px; border: none; border-radius: 4px;
         cursor: pointer; font-size: 14px;
      }
      .btn-primary { background: #667eea; color: white; }
      .btn-secondary { background: #6c757d; color: white; }
      .btn-danger { background: #dc3545; color: white; }
      .required { color: red; }
      .info-banner {
         background: #e7f3ff; border: 1px solid #b3d9ff; border-radius: 4px;
         padding: 10px; margin-bottom: 20px; font-size: 14px;
      }
      .info-banner.existing { background: #fff3cd; border-color: #ffecb5; color: #856404; }
      .error { background: #ffe6e6; color: red; padding: 10px; margin-bottom: 20px; border-radius: 4px; }
   </style>
</head>
<body>
   <div class="form-container">
      <div class="form-header">
         <h1>📝 Formulario Simple</h1>
         <p>${tieneArchivo?string("Editando formulario existente", "Complete los siguientes campos")}</p>
      </div>

      <#if error??>
         <div class="error">⚠️ ${error}</div>
      </#if>

      <#if tieneArchivo>
         <div class="info-banner existing">
            📄 <strong>Archivo:</strong> ${nombreArchivo}<br>
            <#if fechaCreacion?has_content>📅 <strong>Creado:</strong> ${fechaCreacion}<br></#if>
            <#if fechaModificacion?has_content>📅 <strong>Modificado:</strong> ${fechaModificacion}</#if>
         </div>
      <#else>
         <div class="info-banner">
            ℹ️ <strong>Nuevo formulario:</strong> Se creará un archivo JSON con los datos.
         </div>
      </#if>

      <div id="message"></div>

      <form id="formulario">
         <input type="hidden" id="nodeRefArchivo" value="${nodeRefArchivo!""}">
         <input type="hidden" id="nombreArchivo" value="${nombreArchivo!""}">
         <input type="hidden" id="tieneArchivo" value="${tieneArchivo?string('true', 'false')}">

         <div class="form-field">
            <label for="campo1">Campo 1 <span class="required">*</span></label>
            <input type="text" id="campo1" name="campo1" value="${campo1}" required>
         </div>

         <div class="form-field">
            <label for="campo2">Campo 2 <span class="required">*</span></label>
            <input type="text" id="campo2" name="campo2" value="${campo2}" required>
         </div>

         <div class="form-field">
            <label for="campo3">Campo 3</label>
            <textarea id="campo3" name="campo3" rows="3">${campo3}</textarea>
         </div>

         <div style="text-align: center; margin-top: 30px;">
            <button type="button" onclick="guardar()" class="btn btn-primary">
               ${tieneArchivo?string("✏️ Actualizar", "💾 Crear")} Archivo
            </button>
            <#if tieneArchivo>
               <button type="button" onclick="eliminar()" class="btn btn-danger">🗑️ Eliminar</button>
            </#if>
            <button type="button" onclick="window.history.back()" class="btn btn-secondary">❌ Cancelar</button>
         </div>
      </form>
   </div>

   <script>
      function obtenerSiteId() {
         var match = window.location.href.match(/\/site\/([^\/\?]+)/);
         return match ? match[1] : null;
      }

      function mostrarMensaje(mensaje, tipo) {
         var color = tipo === 'error' ? 'red' : tipo === 'loading' ? 'blue' : 'green';
         document.getElementById('message').innerHTML =
            '<div style="color: ' + color + '; padding: 10px;">' + mensaje + '</div>';
      }

      function guardar() {
         var campo1 = document.getElementById('campo1').value.trim();
         var campo2 = document.getElementById('campo2').value.trim();
         var campo3 = document.getElementById('campo3').value.trim();

         if (!campo1 || !campo2) {
            alert('Los campos 1 y 2 son obligatorios');
            return;
         }

         var tieneArchivo = document.getElementById('tieneArchivo').value === 'true';
         mostrarMensaje('⏳ ' + (tieneArchivo ? 'Actualizando' : 'Creando') + ' archivo...', 'loading');

         var siteId = obtenerSiteId();
         var url = '/share/proxy/alfresco/formulario' + (siteId ? '?site=' + encodeURIComponent(siteId) : '');

         fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
               operacion: 'procesar',
               campo1: campo1,
               campo2: campo2,
               campo3: campo3,
               nodeRefArchivo: document.getElementById('nodeRefArchivo').value,
               nombreArchivo: document.getElementById('nombreArchivo').value,
               esActualizacion: tieneArchivo
            })
         })
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               var accion = tieneArchivo ? 'actualizado' : 'creado';
               mostrarMensaje('✅ Archivo ' + accion + ': ' + data.filename, 'success');
               setTimeout(() => {
                  window.location.href = siteId ?
                     '/share/page/site/' + siteId + '/documentlibrary' :
                     '/share/page/context/mine/myfiles';
               }, 2000);
            } else {
               mostrarMensaje('❌ Error: ' + data.message, 'error');
            }
         })
         .catch(error => mostrarMensaje('❌ Error de conexión: ' + error.message, 'error'));
      }

      function eliminar() {
         var nombre = document.getElementById('nombreArchivo').value;
         var nodeRef = document.getElementById('nodeRefArchivo').value;

         if (!nodeRef || !confirm('¿Eliminar archivo "' + nombre + '"?')) return;

         mostrarMensaje('⏳ Eliminando archivo...', 'loading');

         var siteId = obtenerSiteId();
         var url = '/share/proxy/alfresco/formulario' + (siteId ? '?site=' + encodeURIComponent(siteId) : '');

         fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
               operacion: 'eliminar',
               nodeRefArchivo: nodeRef,
               nombreArchivo: nombre
            })
         })
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               mostrarMensaje('✅ Archivo eliminado', 'success');
               setTimeout(() => window.location.reload(), 1000);
            } else {
               mostrarMensaje('❌ Error: ' + data.message, 'error');
            }
         })
         .catch(error => mostrarMensaje('❌ Error de conexión: ' + error.message, 'error'));
      }
   </script>
</body>
</html>