<@markup id="css" >
   <style>
      .dashlet.document-stats .stats-container {
         padding: 12px;
      }

      .dashlet.document-stats .site-section {
         margin-bottom: 16px;
         border: 1px solid #e0e0e0;
         border-radius: 3px;
      }

      .dashlet.document-stats .site-section:last-of-type {
         margin-bottom: 0;
      }

      .dashlet.document-stats .site-header {
         background: #f8f9fa;
         padding: 8px 12px;
         border-bottom: 1px solid #e0e0e0;
         font-size: 12px;
         font-weight: bold;
         color: #555;
         cursor: pointer;
         display: flex;
         justify-content: space-between;
         align-items: center;
         transition: background-color 0.2s ease;
      }

      .dashlet.document-stats .site-header:hover {
         background: #e9ecef;
      }

      .dashlet.document-stats .site-header.expanded {
         background: #e3f2fd;
         border-bottom-color: #90caf9;
      }

      .dashlet.document-stats .site-info {
         display: flex;
         align-items: center;
         gap: 8px;
      }

      .dashlet.document-stats .total-badge {
         background: #2b5797;
         color: white;
         padding: 2px 6px;
         border-radius: 10px;
         font-size: 10px;
         font-weight: bold;
      }

      .dashlet.document-stats .expand-icon {
         font-size: 10px;
         color: #666;
         transition: transform 0.2s ease;
      }

      .dashlet.document-stats .expand-icon.expanded {
         transform: rotate(90deg);
      }

      .dashlet.document-stats .stats-content {
         max-height: 0;
         overflow: hidden;
         transition: max-height 0.3s ease;
         background: white;
      }

      .dashlet.document-stats .stats-content.expanded {
         max-height: 200px;
      }

      .dashlet.document-stats .stats-table {
         width: 100%;
         border-collapse: collapse;
      }

      .dashlet.document-stats .stats-table td {
         padding: 6px 12px;
         font-size: 11px;
         border-bottom: 1px solid #f0f0f0;
      }

      .dashlet.document-stats .stats-table tr:last-child td {
         border-bottom: none;
      }

      .dashlet.document-stats .stats-table td:last-child {
         text-align: right;
         font-weight: bold;
      }

      .dashlet.document-stats .stat-icon {
         width: 12px;
         height: 12px;
         display: inline-block;
         margin-right: 6px;
         border-radius: 2px;
      }

      .dashlet.document-stats .approved { background: #5a8a3a; }
      .dashlet.document-stats .disapproved { background: #c74545; }
      .dashlet.document-stats .unclassified { background: #777; }
      .dashlet.document-stats .total { background: #2b5797; }

      .dashlet.document-stats .site-summary {
         padding: 6px 12px;
         background: #f9f9f9;
         font-size: 11px;
         color: #666;
         display: flex;
         justify-content: space-between;
      }

      .dashlet.document-stats .summary-item {
         display: flex;
         align-items: center;
         gap: 4px;
      }

      .dashlet.document-stats .summary-icon {
         width: 8px;
         height: 8px;
         border-radius: 1px;
      }

      .dashlet.document-stats .loading {
         text-align: center;
         color: #6c757d;
         font-style: italic;
         padding: 40px 20px;
      }

      .dashlet.document-stats .error {
         text-align: center;
         color: #dc3545;
         padding: 40px 20px;
      }

      .dashlet.document-stats .last-updated {
         text-align: center;
         font-size: 10px;
         color: #999;
         margin-top: 12px;
         padding-top: 8px;
         border-top: 1px solid #f0f0f0;
      }
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

                              var sites = data.sites.sort(function(a, b) {
                                 return b.stats.total - a.stats.total;
                              });

                              for (var i = 0; i < sites.length; i++) {
                                 var site = sites[i];
                                 var stats = site.stats;
                                 var siteId = 'site-' + i;

                                 html += '<div class="site-section">';

                                 html += '<div class="site-header" data-site="' + siteId + '">';
                                 html += '<div class="site-info">';
                                 html += '<span>🏢 ' + site.siteName + '</span>';
                                 html += '<span class="total-badge">' + stats.total + '</span>';
                                 html += '</div>';
                                 html += '<span class="expand-icon">▶</span>';
                                 html += '</div>';

                                 html += '<div class="site-summary">';
                                 html += '<div class="summary-item">';
                                 html += '<span class="summary-icon approved"></span>';
                                 html += '<span>' + stats.approved + ' Aprobados</span>';
                                 html += '</div>';
                                 html += '<div class="summary-item">';
                                 html += '<span class="summary-icon disapproved"></span>';
                                 html += '<span>' + stats.disapproved + ' Desaprobados</span>';
                                 html += '</div>';
                                 html += '<div class="summary-item">';
                                 html += '<span class="summary-icon unclassified"></span>';
                                 html += '<span>' + stats.unclassified + ' Sin clasificar</span>';
                                 html += '</div>';
                                 html += '</div>';

                                 html += '<div class="stats-content" data-content="' + siteId + '">';
                                 html += '<table class="stats-table">';
                                 html += '<tr>';
                                 html += '<td><span class="stat-icon approved"></span>Aprobados</td>';
                                 html += '<td>' + stats.approved + '</td>';
                                 html += '</tr>';
                                 html += '<tr>';
                                 html += '<td><span class="stat-icon disapproved"></span>Desaprobados</td>';
                                 html += '<td>' + stats.disapproved + '</td>';
                                 html += '</tr>';
                                 html += '<tr>';
                                 html += '<td><span class="stat-icon unclassified"></span>Sin clasificar</td>';
                                 html += '<td>' + stats.unclassified + '</td>';
                                 html += '</tr>';
                                 html += '<tr>';
                                 html += '<td><span class="stat-icon total"></span>Total</td>';
                                 html += '<td>' + stats.total + '</td>';
                                 html += '</tr>';
                                 html += '</table>';
                                 html += '</div>';

                                 html += '</div>';
                              }

                              var updateTime = new Date().toLocaleString();
                              html += '<div class="last-updated">Última actualización: ' + updateTime + '</div>';

                              statsContainer.innerHTML = html;

                              // Agregar event listeners después de insertar el HTML
                              var headers = statsContainer.querySelectorAll('.site-header');
                              for (var j = 0; j < headers.length; j++) {
                                 headers[j].addEventListener('click', function() {
                                    var siteId = this.getAttribute('data-site');
                                    var content = statsContainer.querySelector('[data-content="' + siteId + '"]');
                                    var icon = this.querySelector('.expand-icon');

                                    var isExpanded = this.classList.contains('expanded');

                                    if (isExpanded) {
                                       this.classList.remove('expanded');
                                       content.classList.remove('expanded');
                                       icon.classList.remove('expanded');
                                    } else {
                                       this.classList.add('expanded');
                                       content.classList.add('expanded');
                                       icon.classList.add('expanded');
                                    }
                                 });
                              }

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