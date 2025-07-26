<@markup id="css" >
   <@link href="${url.context}/res/components/dashlets/document-stats.css" group="dashlets"/>
   <style>
      .site-stats-container {
         margin-bottom: 25px;
      }
      .site-header {
         background: linear-gradient(135deg, #667eea, #764ba2);
         color: white;
         padding: 15px;
         border-radius: 8px 8px 0 0;
         font-size: 16px;
         font-weight: bold;
         text-align: center;
      }
      .site-stats-grid {
         display: grid;
         grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
         gap: 10px;
         padding: 15px;
         background: #f8f9fa;
         border-radius: 0 0 8px 8px;
         border: 1px solid #e9ecef;
      }
      .mini-stat-card {
         text-align: center;
         padding: 10px;
         border-radius: 6px;
         font-weight: bold;
         color: white;
         box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      }
      .mini-approved { background: #28a745; }
      .mini-disapproved { background: #dc3545; }
      .mini-unclassified { background: #6c757d; }
      .mini-total { background: #007bff; }
      .mini-stat-value { font-size: 18px; }
      .mini-stat-label { font-size: 11px; margin-top: 4px; }
   </style>
</@>

<@markup id="widgets">
   <@createWidgets group="dashlets"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="dashlet document-stats">
         <div class="title">📊 Resumen de Documentos por Sitio</div>
         <div class="body scrollableList" >
            <div id="${el}-stats" class="stats-container">
               <div class="loading">Cargando estadísticas...</div>
            </div>
         </div>
      </div>

      <script type="text/javascript">
         (function() {
            var elId = "${el}";

            function loadStats() {
               var statsContainer = document.getElementById(elId + "-stats");

               var xhr = new XMLHttpRequest();
               xhr.open('GET', '/share/proxy/alfresco/kallpa/stats/documents', true);
               xhr.setRequestHeader('Content-Type', 'application/json');

               xhr.onreadystatechange = function() {
                  if (xhr.readyState === 4) {
                     if (xhr.status === 200) {
                        try {
                           var data = JSON.parse(xhr.responseText);
                           if (data.success && data.sites) {
                              var html = '';

                              // Ordenar sitios por total de documentos (descendente)
                              var sites = data.sites.sort(function(a, b) {
                                 return b.stats.total - a.stats.total;
                              });

                              for (var i = 0; i < sites.length; i++) {
                                 var site = sites[i];
                                 var stats = site.stats;

                                 html += '<div class="site-stats-container">';
                                 html += '<div class="site-header">';
                                 html += '🏢 ' + site.siteName;
                                 html += '</div>';
                                 html += '<div class="site-stats-grid">';

                                 html += '<div class="mini-stat-card mini-approved">';
                                 html += '<div class="mini-stat-value">' + stats.approved + '</div>';
                                 html += '<div class="mini-stat-label">✅ APROBADOS</div>';
                                 html += '</div>';

                                 html += '<div class="mini-stat-card mini-disapproved">';
                                 html += '<div class="mini-stat-value">' + stats.disapproved + '</div>';
                                 html += '<div class="mini-stat-label">❌ DESAPROBADOS</div>';
                                 html += '</div>';

                                 html += '<div class="mini-stat-card mini-unclassified">';
                                 html += '<div class="mini-stat-value">' + stats.unclassified + '</div>';
                                 html += '<div class="mini-stat-label">⏳ SIN CLASIFICAR</div>';
                                 html += '</div>';

                                 html += '<div class="mini-stat-card mini-total">';
                                 html += '<div class="mini-stat-value">' + stats.total + '</div>';
                                 html += '<div class="mini-stat-label">📈 TOTAL</div>';
                                 html += '</div>';

                                 html += '</div>';
                                 html += '</div>';
                              }

                              var updateTime = new Date().toLocaleString();
                              html += '<div class="last-updated">Última actualización: ' + updateTime + '</div>';

                              statsContainer.innerHTML = html;
                           } else {
                              statsContainer.innerHTML = '<div class="error">Error al cargar estadísticas</div>';
                           }
                        } catch(e) {
                           statsContainer.innerHTML = '<div class="error">Error procesando respuesta</div>';
                        }
                     } else {
                        statsContainer.innerHTML = '<div class="error">Error al cargar estadísticas (HTTP ' + xhr.status + ')</div>';
                     }
                  }
               };

               xhr.send();
            }

            if (document.readyState === 'loading') {
               document.addEventListener('DOMContentLoaded', loadStats);
            } else {
               loadStats();
            }
         })();
      </script>
   </@>
</@>