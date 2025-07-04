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
      //<![CDATA[

      // === UTILIDAD CSRF INLINE ===
      var KallpaCSRF = {
         TOKEN_COOKIE_NAME: 'Alfresco-CSRFToken',
         TOKEN_HEADER_NAME: 'Alfresco-CSRFToken',

         getToken: function() {
            return this.getCookie(this.TOKEN_COOKIE_NAME);
         },

         getHeaders: function(additionalHeaders) {
            var headers = {
               'Content-Type': 'application/json'
            };

            var token = this.getToken();
            if (token) {
               headers[this.TOKEN_HEADER_NAME] = token;
               console.log('🔒 Token CSRF agregado: ' + token.substring(0, 8) + '...');
            } else {
               console.log('⚠️ No hay token CSRF disponible');
            }

            if (additionalHeaders) {
               for (var key in additionalHeaders) {
                  headers[key] = additionalHeaders[key];
               }
            }

            return headers;
         },

         fetch: function(url, options) {
            options = options || {};
            options.headers = this.getHeaders(options.headers);

            console.log('📡 Enviando request con headers:', options.headers);

            return fetch(url, options)
               .then(function(response) {
                  if (response.status === 403) {
                     console.warn('🚫 CSRF: Request bloqueada (403)');
                  }
                  return response;
               });
         },

         getCookie: function(name) {
            var nameEQ = name + "=";
            var ca = document.cookie.split(';');
            for(var i = 0; i < ca.length; i++) {
               var c = ca[i];
               while (c.charAt(0) == ' ') c = c.substring(1, c.length);
               if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
            }
            return null;
         },

         checkStatus: function() {
            var token = this.getToken();
            console.log('🔍 CSRF Status:', {
               tokenPresent: !!token,
               tokenValue: token ? token.substring(0, 8) + '...' : 'None',
               allCookies: document.cookie
            });
            return !!token;
         }
      };

      // === FUNCIÓN PRINCIPAL ===
      function enviarRevision() {
         console.log('🚀 Iniciando envío a revisión...');

         document.getElementById('message').innerHTML =
            '<div style="color: blue; padding: 10px;">Enviando a revisión...</div>';

         var currentUrl = window.location.href;
         var siteMatch = currentUrl.match(/\/site\/([^\/]+)/);
         var siteId = siteMatch ? siteMatch[1] : null;

         var requestData = {};
         if (siteId) {
            requestData.site = siteId;
         }

         var url = '/share/proxy/alfresco/revision/procesar';
         if (siteId) {
            url += '?site=' + encodeURIComponent(siteId);
         }

         console.log('🔍 URL:', url);
         console.log('🔍 Site ID:', siteId);
         console.log('🔍 Request Data:', requestData);

         // Verificar estado CSRF
         KallpaCSRF.checkStatus();

         // Usar fetch con CSRF
         KallpaCSRF.fetch(url, {
            method: 'POST',
            body: JSON.stringify(requestData)
         })
         .then(function(response) {
            console.log('📡 Response status:', response.status);
            console.log('📡 Response headers:', Array.from(response.headers.entries()));

            if (!response.ok) {
               throw new Error('HTTP ' + response.status + ': ' + response.statusText);
            }

            var contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
               return response.text().then(function(text) {
                  console.error('🚨 Respuesta no es JSON:', text);
                  throw new Error('Respuesta no es JSON: ' + text.substring(0, 100));
               });
            }

            return response.json();
         })
         .then(function(data) {
            console.log('✅ Datos recibidos:', data);

            if (data.success) {
               var locationText = data.filename ? ' (Archivo: ' + data.filename + ')' : '';

               document.getElementById('message').innerHTML =
                  '<div style="color: green; padding: 10px;">✅ Archivo de confirmación enviado exitosamente' + locationText + '</div>';

               setTimeout(function() {
                  if (siteId) {
                     window.location.href = '/share/page/site/' + siteId + '/documentlibrary';
                  } else {
                     window.location.href = '/share/page/context/mine/myfiles';
                  }
               }, 2000);
            } else {
               document.getElementById('message').innerHTML =
                  '<div style="color: red; padding: 10px;">❌ Error: ' + data.message + '</div>';
            }
         })
         .catch(function(error) {
            console.error('🚨 Error completo:', error);
            document.getElementById('message').innerHTML =
               '<div style="color: red; padding: 10px;">❌ Error: ' + error.message + '</div>';
         });
      }

      // === INICIALIZACIÓN ===
      window.onload = function() {
         console.log('📄 Página cargada');
         setTimeout(function() {
            KallpaCSRF.checkStatus();
            console.log('✅ KallpaCSRF inicializado');
         }, 1000);
      };

      //]]>
   </script>
</body>
</html>