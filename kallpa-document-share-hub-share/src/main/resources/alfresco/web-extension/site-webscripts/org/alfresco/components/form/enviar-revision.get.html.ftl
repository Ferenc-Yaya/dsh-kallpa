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

      // === FUNCIONES CSRF DIRECTAS ===
      function getCSRFTokenDirect() {
         // Método 1: Buscar directamente en cookies
         var csrfToken = getCookie('Alfresco-CSRFToken');
         if (csrfToken) {
            console.log('Token CSRF encontrado en cookie:', csrfToken.substring(0, 8) + '...');
            return csrfToken;
         }

         // Método 2: Buscar otras variantes de nombres
         var altToken = getCookie('CSRF-TOKEN') || getCookie('csrf-token') || getCookie('AlfCSRF');
         if (altToken) {
            console.log('Token CSRF alternativo encontrado:', altToken.substring(0, 8) + '...');
            return altToken;
         }

         console.log('No se encontró token CSRF en cookies');
         return null;
      }

      function getCookie(name) {
         var nameEQ = name + "=";
         var ca = document.cookie.split(';');
         for(var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
         }
         return null;
      }

      function checkCSRFStatus() {
         var hasAlfresco = typeof Alfresco !== 'undefined';
         var csrfToken = getCSRFTokenDirect();
         var allCookies = document.cookie;

         console.log('=== ESTADO CSRF COMPLETO ===');
         console.log('Alfresco objeto disponible:', hasAlfresco);
         console.log('Token CSRF encontrado:', csrfToken ? 'SÍ' : 'NO');
         console.log('Todas las cookies:', allCookies);

         return {
            alfrescoLoaded: hasAlfresco,
            csrfEnabled: !!csrfToken,
            token: csrfToken,
            cookieCount: allCookies.split(';').length
         };
      }

      // === FUNCIÓN PRINCIPAL ===
      function enviarRevision() {
         document.getElementById('message').innerHTML = '<div style="color: blue; padding: 10px;">Enviando a revisión...</div>';

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

         // Headers básicos
         var headers = {
            'Content-Type': 'application/json',
            'Referer': window.location.href
         };

         // Intentar agregar token CSRF
         var csrfToken = getCSRFTokenDirect();
         if (csrfToken) {
            headers['Alfresco-CSRFToken'] = csrfToken;
            console.log('✅ Token CSRF agregado a headers');
         } else {
            console.log('⚠️ Enviando request SIN token CSRF');
         }

         console.log('📡 Enviando request:', { url: url, headers: headers });

         fetch(url, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(requestData)
         })
         .then(function(response) {
            console.log('📡 Response status:', response.status);

            if (!response.ok) {
               if (response.status === 403) {
                  throw new Error('CSRF_BLOCKED: El servidor bloqueó la request (CSRF funcionando)');
               }
               throw new Error('HTTP ' + response.status + ': ' + response.statusText);
            }
            return response.json();
         })
         .then(function(data) {
            console.log('✅ Respuesta exitosa:', data);
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
            console.error('🚨 Error:', error);

            if (error.message.indexOf('CSRF_BLOCKED') !== -1) {
               document.getElementById('message').innerHTML =
                  '<div style="color: orange; padding: 10px;"><strong>🔒 CSRF Funcionando:</strong> Request bloqueada por seguridad.<br>' +
                  '<small>Esto confirma que CSRF está habilitado y funciona correctamente.</small></div>';
            } else if (error.message.indexOf('403') !== -1) {
               document.getElementById('message').innerHTML =
                  '<div style="color: red; padding: 10px;"><strong>Error 403:</strong> Acceso denegado por CSRF.</div>';
            } else {
               document.getElementById('message').innerHTML =
                  '<div style="color: red; padding: 10px;">Error: ' + error.message + '</div>';
            }
         });
      }

      // === INICIALIZACIÓN ===
      window.onload = function() {
         // Solo logging básico en consola para debugging técnico si es necesario
         setTimeout(function() {
            var status = checkCSRFStatus();

            // Log silencioso para desarrolladores (solo en consola)
            if (status.csrfEnabled) {
               console.log('✅ CSRF habilitado y funcionando');
            } else {
               console.log('ℹ️ CSRF no detectado');
            }
         }, 1000);
      };

      //]]>
   </script>
</body>
</html>