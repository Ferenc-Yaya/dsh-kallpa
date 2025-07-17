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
      .folders-section {
         background: #f8f9fa;
         border: 1px solid #dee2e6;
         border-radius: 8px;
         padding: 20px;
         margin-bottom: 25px;
      }
      .folders-section h3 {
         margin-top: 0;
         margin-bottom: 15px;
         color: #495057;
         font-size: 16px;
         font-weight: bold;
      }
      .folder-item {
         display: flex;
         align-items: flex-start;
         margin-bottom: 15px;
         padding: 10px;
         background: white;
         border: 1px solid #e9ecef;
         border-radius: 6px;
      }
      .folder-item:last-child {
         margin-bottom: 0;
      }
      .folder-item input[type="radio"] {
         margin-right: 12px;
         margin-top: 2px;
         transform: scale(1.2);
      }
      .folder-info {
         flex: 1;
      }
      .folder-name {
         font-weight: bold;
         color: #495057;
         margin-bottom: 4px;
      }
      .folder-description {
         font-size: 14px;
         color: #6c757d;
         line-height: 1.4;
      }
      .loading {
         text-align: center;
         color: #6c757d;
         font-style: italic;
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
            <li><strong>Al enviar los documentos:</strong> El supervisor DSH procederá a proteger los documentos subidos para garantizar la integridad durante el proceso de evaluación.</li>
            <li><strong>Seguimiento del proceso:</strong> Recibirá actualizaciones constantes mediante notificaciones por correo electrónico y podrá revisar el progreso a través de los comentarios del archivo de seguimiento que se generará automáticamente.</li>
            <li><strong>Resultado exitoso:</strong> Una vez que todos los documentos hayan sido aprobados satisfactoriamente, recibirá el documento oficial "INFORME DE HABILITACIÓN" como certificación del cumplimiento de todos los requisitos.</li>
         </ul>
      </div>

      <div class="folders-section">
         <h3>📁 Seleccionar Carpeta a Incluir</h3>
         <div id="folders-list" class="loading">
            Cargando carpetas del sitio...
         </div>
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

      var siteFolders = [];

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
         }
      };

      // === CARGAR CARPETAS ===
      function loadSiteFolders() {
         var currentUrl = window.location.href;
         var siteMatch = currentUrl.match(/\/site\/([^\/]+)/);
         var siteId = siteMatch ? siteMatch[1] : null;

         if (!siteId) {
            document.getElementById('folders-list').innerHTML =
               '<div style="color: red;">No se pudo determinar el sitio actual</div>';
            return;
         }

         var url = '/share/proxy/alfresco/kallpa/folders/site/' + siteId;

         fetch(url)
            .then(function(response) {
               if (!response.ok) {
                  throw new Error('HTTP ' + response.status);
               }
               return response.json();
            })
            .then(function(data) {
               if (data.success) {
                  siteFolders = data.folders;
                  displayFolders(data.folders);
               } else {
                  document.getElementById('folders-list').innerHTML =
                     '<div style="color: red;">Error: ' + data.message + '</div>';
               }
            })
            .catch(function(error) {
               console.error('Error cargando carpetas:', error);
               document.getElementById('folders-list').innerHTML =
                  '<div style="color: red;">Error cargando carpetas: ' + error.message + '</div>';
            });
      }

      // === MOSTRAR CARPETAS ===
      function displayFolders(folders) {
         var html = '';

         if (folders.length === 0) {
            html = '<div style="color: #6c757d;">No hay carpetas en este sitio</div>';
         } else {
            folders.forEach(function(folder) {
               html += '<div class="folder-item">';
               html += '<input type="radio" name="selectedFolder" id="folder_' + folder.id + '" value="' + folder.id + '">';
               html += '<div class="folder-info">';
               html += '<div class="folder-name">' + escapeHtml(folder.name) + '</div>';
               html += '</div>';
               html += '</div>';
            });
         }

         document.getElementById('folders-list').innerHTML = html;
      }

      // === FUNCIÓN PRINCIPAL ===
      function enviarRevision() {
         console.log('🚀 Iniciando envío a revisión...');

         var selectedFolders = getSelectedFolders();

         if (selectedFolders.length === 0) {
            document.getElementById('message').innerHTML =
               '<div style="color: red; padding: 10px;">❌ Por favor selecciona una carpeta antes de enviar</div>';
            return;
         }

         console.log('📁 Carpetas seleccionadas:', selectedFolders);

         document.getElementById('message').innerHTML =
            '<div style="color: blue; padding: 10px;">Enviando a revisión...</div>';

         var currentUrl = window.location.href;
         var siteMatch = currentUrl.match(/\/site\/([^\/]+)/);
         var siteId = siteMatch ? siteMatch[1] : null;

         var requestData = {
            selectedFolders: selectedFolders
         };
         if (siteId) {
            requestData.site = siteId;
         }

         var url = '/share/proxy/alfresco/revision/procesar';
         if (siteId) {
            url += '?site=' + encodeURIComponent(siteId);
         }

         KallpaCSRF.fetch(url, {
            method: 'POST',
            body: JSON.stringify(requestData)
         })
         .then(function(response) {
            if (!response.ok) {
               throw new Error('HTTP ' + response.status + ': ' + response.statusText);
            }

            var contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
               return response.text().then(function(text) {
                  throw new Error('Respuesta no es JSON: ' + text.substring(0, 100));
               });
            }

            return response.json();
         })
         .then(function(data) {
            if (data.success) {
               var locationText = data.filename ? ' (Archivo: ' + data.filename + ')' : '';

               document.getElementById('message').innerHTML =
                  '<div style="color: green; padding: 10px;">✅ Archivo de confirmación enviado exitosamente' + locationText + '</div>';

               setTimeout(function() {
                  if (window.history && window.history.length > 1) {
                     window.history.back();
                  } else if (siteId) {
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

      // === UTILIDADES ===
      function getSelectedFolders() {
         var selected = [];
         var radioButtons = document.getElementsByName('selectedFolder');

         for (var i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].checked) {
               var folderId = radioButtons[i].value;
               var folder = siteFolders.find(function(f) { return f.id === folderId; });
               if (folder) {
                  selected.push({
                     id: folder.id,
                     name: folder.name,
                     description: folder.description || ''
                  });
               }
               break;
            }
         }

         return selected;
      }

      function escapeHtml(text) {
         if (!text) return '';
         var div = document.createElement('div');
         div.textContent = text;
         return div.innerHTML;
      }

      // === INICIALIZACIÓN ===
      window.onload = function() {
         console.log('📄 Página cargada');
         loadSiteFolders();
      };

      //]]>
   </script>
</body>
</html>