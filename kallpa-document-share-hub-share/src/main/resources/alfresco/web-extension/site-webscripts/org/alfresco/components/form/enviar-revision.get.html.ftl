<!DOCTYPE html>
<html>
<head>
   <title>ENVIAR A REVISIÓN - DSH</title>
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
         padding: 12px 25px;
         margin: 0 8px;
         border: none;
         border-radius: 6px;
         cursor: pointer;
         font-size: 14px;
         font-weight: 500;
         min-width: 140px;
         height: 44px;
         display: inline-flex;
         align-items: center;
         justify-content: center;
         text-decoration: none;
         transition: all 0.2s ease;
      }
      .btn-primary {
         background: #667eea;
         color: white;
      }
      .btn-primary:hover {
         background: #5a67d8;
         transform: translateY(-1px);
         box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
      }
      .btn-secondary {
         background: #6c757d;
         color: white;
      }
      .btn-secondary:hover {
         background: #5a6268;
         transform: translateY(-1px);
         box-shadow: 0 4px 8px rgba(108, 117, 125, 0.3);
      }
   </style>
</head>
<body>
   <div class="form-container">
      <div class="form-header">
         <h1>📤 ENVIAR A REVISIÓN</h1>
         <p>Generar archivo de confirmación</p>
      </div>

      <div class="info-box">
         <h3>ℹ️ Proceso de Revisión de Documentos</h3>
         <ul>
            <li><strong>Al enviar los documentos:</strong> El supervisor DSH procederá a <span class="highlight">proteger los documentos subidos</span> para garantizar la integridad durante el proceso de evaluación.</li>
            <li><strong>Seguimiento del proceso:</strong> Recibirá actualizaciones constantes mediante <span class="highlight">notificaciones por correo electrónico</span> y podrá revisar el progreso a través de los <span class="highlight">comentarios del archivo de seguimiento</span> que se generará automáticamente.</li>
            <li><strong>Resultado exitoso:</strong> Una vez que todos los documentos hayan sido aprobados satisfactoriamente, recibirá el documento oficial <span class="highlight">"INFORME DE HABILITACIÓN"</span> como certificación del cumplimiento de todos los requisitos.</li>
         </ul>
      </div>

      <div id="message"></div>

      <div style="text-align: center; margin-top: 30px;">
         <button type="button" onclick="enviarRevision()" class="btn btn-primary">
            📤 Enviar
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

         // DEBUG: Log información
         console.log('🔍 URL actual:', currentUrl);
         console.log('🔍 Site ID:', siteId);
         console.log('🔍 URL de solicitud:', url);
         console.log('🔍 Datos a enviar:', requestData);

         fetch(url, {
            method: 'POST',
            headers: {
               'Content-Type': 'application/json',
               'Referer': window.location.href
            },
            body: JSON.stringify(requestData)
         })
         .then(response => {
            console.log('🔍 Response status:', response.status);
            console.log('🔍 Response URL:', response.url);
            console.log('🔍 Response headers:', Array.from(response.headers.entries()));

            // Verificar si la respuesta es OK
            if (!response.ok) {
               throw new Error('HTTP ' + response.status + ': ' + response.statusText);
            }

            // Verificar Content-Type
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
               return response.text().then(text => {
                  console.error('🚨 Respuesta no es JSON:', text);
                  throw new Error('Respuesta del servidor no es JSON válido: ' + text.substring(0, 100));
               });
            }

            return response.json();
         })
         .then(data => {
            console.log('✅ Datos recibidos:', data);

            // Mostrar información sobre la configuración del servidor
            if (data.baseUrl) {
               console.log('🌐 Base URL del servidor:', data.baseUrl);
               console.log('🏠 Entorno detectado:', data.baseUrl.includes('localhost') ? 'DESARROLLO' : 'PRODUCCIÓN');

               // Guardar baseUrl para uso futuro si es necesario
               window.alfrescoBaseUrl = data.baseUrl;
            }

            if (data.success) {
               var locationText = data.location ? ' en ' + data.location : '';

               // Mensaje mejorado que incluye información del entorno
               var environmentInfo = data.baseUrl ? '<br><small>Entorno: ' + data.baseUrl + '</small>' : '';

               document.getElementById('message').innerHTML =
                  '<div style="color: green; padding: 10px;">✅ ' + data.message + locationText + environmentInfo + '</div>';

               setTimeout(function() {
                  if (siteId) {
                     window.location.href = '/share/page/site/' + siteId + '/documentlibrary';
                  } else {
                     window.location.href = '/share/page/context/mine/myfiles';
                  }
               }, 2000);
            } else {
               // Mostrar error con información adicional del servidor
               var environmentInfo = data.baseUrl ? '<br><small>Servidor: ' + data.baseUrl + '</small>' : '';

               document.getElementById('message').innerHTML =
                  '<div style="color: red; padding: 10px;">❌ Error: ' + data.message + environmentInfo + '</div>';
            }
         })
         .catch(error => {
            console.error('🚨 Error completo:', error);
            document.getElementById('message').innerHTML =
               '<div style="color: red; padding: 10px;">❌ Error: ' + error.message + '<br><small>Revisa la consola para más detalles</small></div>';
         });
      }

      // FUNCIÓN ADICIONAL: Para usar la baseUrl en otras partes si es necesario
      function getAlfrescoBaseUrl() {
         return window.alfrescoBaseUrl || window.location.origin;
      }

      // FUNCIÓN DE DEBUG: Para probar la configuración
      function testServerConfig() {
         console.log('🧪 Probando configuración del servidor...');

         fetch('/share/proxy/alfresco/revision/procesar', {
            method: 'POST',
            headers: {
               'Content-Type': 'application/json'
            },
            body: JSON.stringify({test: true})
         })
         .then(response => response.json())
         .then(data => {
            console.log('🔧 Configuración del servidor:', {
               baseUrl: data.baseUrl,
               environment: data.baseUrl && data.baseUrl.includes('localhost') ? 'DESARROLLO' : 'PRODUCCIÓN',
               success: data.success,
               message: data.message
            });
         })
         .catch(error => {
            console.error('❌ Error al probar configuración:', error);
         });
      }
   </script>
</body>
</html>