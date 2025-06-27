<!DOCTYPE html>
<html>
<head>
   <title>Formulario Simple - DSH</title>
   <meta charset="utf-8">
   <style>
      body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
      .form-container {
         background: white;
         padding: 30px;
         border-radius: 8px;
         box-shadow: 0 2px 10px rgba(0,0,0,0.1);
         max-width: 600px;
         margin: 0 auto;
      }
      .form-header { text-align: center; margin-bottom: 30px; color: #333; }
      .form-field { margin-bottom: 20px; }
      .form-field label {
         display: block;
         font-weight: bold;
         margin-bottom: 5px;
         color: #555;
      }
      .form-field input, .form-field textarea {
         width: 100%;
         padding: 10px;
         border: 1px solid #ddd;
         border-radius: 4px;
         box-sizing: border-box;
         font-size: 14px;
      }
      .btn {
         padding: 12px 30px;
         margin: 0 10px;
         border: none;
         border-radius: 4px;
         cursor: pointer;
         font-size: 14px;
      }
      .btn-primary { background: #667eea; color: white; }
      .btn-secondary { background: #6c757d; color: white; }
      .btn-danger { background: #dc3545; color: white; }
      .required { color: red; }
      .info-banner {
         background: #e7f3ff;
         border: 1px solid #b3d9ff;
         border-radius: 4px;
         padding: 10px;
         margin-bottom: 20px;
         font-size: 14px;
      }
      .info-banner.existing {
         background: #fff3cd;
         border-color: #ffecb5;
         color: #856404;
      }
   </style>
</head>
<body>
   <div class="form-container">
      <div class="form-header">
         <h1>📝 Formulario Simple</h1>
         <#if tieneArchivo>
            <p>Editando formulario existente</p>
         <#else>
            <p>Complete los siguientes campos</p>
         </#if>
      </div>

      <#if error??>
         <div style="color: red; padding: 10px; margin-bottom: 20px; background: #ffe6e6; border-radius: 4px;">
            ⚠️ ${error}
         </div>
      </#if>

      <#if tieneArchivo>
         <div class="info-banner existing">
            📄 <strong>Archivo existente:</strong> ${nombreArchivo}<br>
            <#if fechaCreacion != "">📅 <strong>Creado:</strong> ${fechaCreacion}</#if>
            <#if fechaModificacion != "">📅 <strong>Modificado:</strong> ${fechaModificacion}</#if>
         </div>
      <#else>
         <div class="info-banner">
            ℹ️ <strong>Nuevo formulario:</strong> Se creará un nuevo archivo JSON con los datos ingresados.
         </div>
      </#if>

      <div id="message"></div>

      <form id="formulario-simple">
         <!-- Campo oculto para el nodeRef del archivo existente -->
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
            <#if tieneArchivo>
               <button type="button" onclick="procesarFormulario()" class="btn btn-primary">
                  ✏️ Actualizar Archivo
               </button>
               <button type="button" onclick="eliminarArchivo()" class="btn btn-danger">
                  🗑️ Eliminar
               </button>
            <#else>
               <button type="button" onclick="procesarFormulario()" class="btn btn-primary">
                  💾 Crear Archivo
               </button>
            </#if>
            <button type="button" onclick="window.history.back()" class="btn btn-secondary">
               ❌ Cancelar
            </button>
         </div>
      </form>
   </div>

   <script>
      // Función para obtener el ID del sitio actual
      function obtenerSiteId() {
         var url = window.location.href;
         var siteMatch = url.match(/\/site\/([^\/\?]+)/);
         return siteMatch ? siteMatch[1] : null;
      }

      function procesarFormulario() {
         const campo1 = document.getElementById('campo1').value.trim();
         const campo2 = document.getElementById('campo2').value.trim();
         const campo3 = document.getElementById('campo3').value.trim();
         const nodeRefArchivo = document.getElementById('nodeRefArchivo').value;
         const nombreArchivo = document.getElementById('nombreArchivo').value;
         const tieneArchivo = document.getElementById('tieneArchivo').value === 'true';

         if (!campo1 || !campo2) {
            alert('Los campos 1 y 2 son obligatorios');
            return;
         }

         // Mostrar indicador de carga
         const accion = tieneArchivo ? 'Actualizando' : 'Creando';
         document.getElementById('message').innerHTML = '<div style="color: blue; padding: 10px;">⏳ ' + accion + ' archivo...</div>';

         // Obtener el sitio actual
         var siteId = obtenerSiteId();

         // Enviar datos al webscript del repository
         const datos = {
            campo1: campo1,
            campo2: campo2,
            campo3: campo3,
            nodeRefArchivo: nodeRefArchivo || null,
            nombreArchivo: nombreArchivo || null,
            esActualizacion: tieneArchivo
         };

         // Construir la URL incluyendo el sitio si existe
         var url = '/share/proxy/alfresco/formulario/procesar';
         if (siteId) {
            url += '?site=' + encodeURIComponent(siteId);
         }

         fetch(url, {
            method: 'POST',
            headers: {
               'Content-Type': 'application/json',
            },
            body: JSON.stringify(datos)
         })
         .then(response => {
            console.log('Response status:', response.status);
            return response.json();
         })
         .then(data => {
            console.log('Response data:', data);
            if (data.success) {
               const accionTexto = tieneArchivo ? 'actualizado' : 'creado';
               document.getElementById('message').innerHTML = '<div style="color: green; padding: 10px;">✅ Archivo ' + accionTexto + ': ' + data.filename + '</div>';

               // Redireccionar después de 2 segundos
               setTimeout(() => {
                  if (siteId) {
                     window.location.href = '/share/page/site/' + siteId + '/documentlibrary';
                  } else {
                     window.location.href = '/share/page/context/mine/myfiles';
                  }
               }, 2000);
            } else {
               document.getElementById('message').innerHTML = '<div style="color: red; padding: 10px;">❌ Error: ' + data.message + '</div>';
            }
         })
         .catch(error => {
            console.error('Error completo:', error);
            document.getElementById('message').innerHTML = '<div style="color: red; padding: 10px;">❌ Error de conexión: ' + error.message + '</div>';
         });
      }

      function eliminarArchivo() {
         const nombreArchivo = document.getElementById('nombreArchivo').value;
         const nodeRefArchivo = document.getElementById('nodeRefArchivo').value;

         if (!nodeRefArchivo) {
            alert('No hay archivo para eliminar');
            return;
         }

         if (!confirm('¿Está seguro que desea eliminar el archivo "' + nombreArchivo + '"?')) {
            return;
         }

         // Mostrar indicador de carga
         document.getElementById('message').innerHTML = '<div style="color: orange; padding: 10px;">⏳ Eliminando archivo...</div>';

         // Obtener el sitio actual
         var siteId = obtenerSiteId();

         // Construir la URL para eliminar
         var url = '/share/proxy/alfresco/formulario/eliminar';
         if (siteId) {
            url += '?site=' + encodeURIComponent(siteId);
         }

         fetch(url, {
            method: 'POST',
            headers: {
               'Content-Type': 'application/json',
            },
            body: JSON.stringify({
               nodeRefArchivo: nodeRefArchivo,
               nombreArchivo: nombreArchivo
            })
         })
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               document.getElementById('message').innerHTML = '<div style="color: green; padding: 10px;">✅ Archivo eliminado correctamente</div>';

               // Redireccionar después de 1 segundo
               setTimeout(() => {
                  window.location.reload();
               }, 1000);
            } else {
               document.getElementById('message').innerHTML = '<div style="color: red; padding: 10px;">❌ Error: ' + data.message + '</div>';
            }
         })
         .catch(error => {
            console.error('Error:', error);
            document.getElementById('message').innerHTML = '<div style="color: red; padding: 10px;">❌ Error de conexión: ' + error.message + '</div>';
         });
      }
   </script>
</body>
</html>