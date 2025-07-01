<!DOCTYPE html>
<html>
<head>
   <title>Enviar a Revisión - DSH</title>
   <meta charset="utf-8">
   <style>
      body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
      .form-container {
         background: white;
         padding: 30px;
         border-radius: 8px;
         box-shadow: 0 2px 10px rgba(0,0,0,0.1);
         max-width: 700px;
         margin: 0 auto;
      }
      .form-header { text-align: center; margin-bottom: 30px; color: #333; }
      .info-text {
         background: #e8f4fd;
         padding: 20px;
         border-radius: 4px;
         margin-bottom: 20px;
         color: #0066cc;
         text-align: center;
      }
      .info-box {
         background: #d1ecf1;
         border: 1px solid #bee5eb;
         border-radius: 8px;
         padding: 20px;
         margin-bottom: 25px;
         color: #0c5460;
      }
      .info-box h3 {
         margin-top: 0;
         margin-bottom: 15px;
         color: #155724;
         font-size: 16px;
         font-weight: bold;
      }
      .info-box ul {
         margin: 10px 0;
         padding-left: 20px;
      }
      .info-box li {
         margin-bottom: 8px;
         line-height: 1.4;
      }
      .highlight {
         background: #bee5eb;
         padding: 2px 4px;
         border-radius: 3px;
         font-weight: bold;
         color: #155724;
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
   </style>
</head>
<body>
   <div class="form-container">
      <div class="form-header">
         <h1>📤 Enviar a Revisión</h1>
         <p>Generar archivo de confirmación</p>
      </div>

      <div class="info-box">
         <h3>ℹ️ Proceso de Revisión de Documentos</h3>
         <ul>
            <li><strong>Al enviar documentos a revisión:</strong> El supervisor DSH procederá a <span class="highlight">proteger los documentos subidos</span> para garantizar la integridad durante el proceso de evaluación.</li>
            <li><strong>Seguimiento del proceso:</strong> Recibirá actualizaciones constantes mediante <span class="highlight">notificaciones por correo electrónico</span> y podrá revisar el progreso a través de los <span class="highlight">comentarios del archivo de seguimiento</span> que se generará automáticamente.</li>
            <li><strong>Resultado exitoso:</strong> Una vez que todos los documentos hayan sido aprobados satisfactoriamente, recibirá el documento oficial <span class="highlight">"INFORME DE HABILITACIÓN"</span> como certificación del cumplimiento de todos los requisitos.</li>
         </ul>
      </div>

      <div id="message"></div>

      <div style="text-align: center; margin-top: 30px;">
         <button type="button" onclick="enviarRevision()" class="btn btn-primary">
            📤 Enviar a Revisión
         </button>
         <button type="button" onclick="window.history.back()" class="btn btn-secondary">
            ❌ Cancelar
         </button>
      </div>
   </div>

   <script>
      function enviarRevision() {
         document.getElementById('message').innerHTML = '<div style="color: blue; padding: 10px;">⏳ Enviando a revisión...</div>';

         // Obtener el sitio actual desde la URL
         var currentUrl = window.location.href;
         var siteMatch = currentUrl.match(/\/site\/([^\/]+)/);
         var siteId = siteMatch ? siteMatch[1] : null;

         // Preparar los datos a enviar
         var requestData = {};
         if (siteId) {
            requestData.site = siteId;
         }

         // Construir la URL con el parámetro del sitio si existe
         var url = '/share/proxy/alfresco/revision/procesar';
         if (siteId) {
            url += '?site=' + encodeURIComponent(siteId);
         }

         fetch(url, {
            method: 'POST',
            headers: {
               'Content-Type': 'application/json',
               'Referer': window.location.href  // Enviar la URL actual como referencia
            },
            body: JSON.stringify(requestData)
         })
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               var locationText = data.location ? ' en ' + data.location : '';
               document.getElementById('message').innerHTML = '<div style="color: green; padding: 10px;">✅ ' + data.message + locationText + '</div>';
               setTimeout(() => {
                  // Regresar a la biblioteca del sitio actual
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
            document.getElementById('message').innerHTML = '<div style="color: red; padding: 10px;">❌ No se pudo procesar la solicitud: ' + error.message + '</div>';
         });
      }
   </script>
</body>
</html>