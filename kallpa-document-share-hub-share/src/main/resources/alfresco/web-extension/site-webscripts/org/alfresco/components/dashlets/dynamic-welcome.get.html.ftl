<@markup id="css" >
   <style>
      .dashlet.document-stats .stats-container {
         padding: 12px;
      }

      .dashlet.document-stats .site-section {
         margin-bottom: 12px;
         border: 1px solid #e0e0e0;
         border-radius: 6px;
         background: white;
         transition: all 0.3s ease;
      }

      .dashlet.document-stats .site-header {
         background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
         color: white;
         padding: 12px 16px;
         border-radius: 6px 6px 0 0;
         display: flex;
         justify-content: space-between;
         align-items: center;
         cursor: pointer;
         transition: all 0.2s ease;
      }

      .dashlet.document-stats .site-header:hover {
         background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
      }

      .dashlet.document-stats .site-info {
         display: flex;
         align-items: center;
         gap: 8px;
         font-size: 14px;
         font-weight: 600;
      }

      .dashlet.document-stats .total-badge {
         background: rgba(255, 255, 255, 0.2);
         color: white;
         padding: 4px 12px;
         border-radius: 12px;
         font-size: 13px;
         font-weight: bold;
         display: flex;
         align-items: center;
         gap: 4px;
      }

      .dashlet.document-stats .expand-icon {
         font-size: 10px;
         transition: transform 0.2s ease;
      }

      .dashlet.document-stats .expand-icon.expanded {
         transform: rotate(180deg);
      }

      .dashlet.document-stats .stats-summary {
         padding: 12px 16px;
         background: #f8f9fa;
         border-bottom: 1px solid #e9ecef;
         display: grid;
         grid-template-columns: 1fr 1fr 1fr;
         gap: 8px;
         font-size: 13px;
         font-weight: 600;
      }

      .dashlet.document-stats .summary-item {
         display: flex;
         align-items: center;
         gap: 8px;
         padding: 6px 8px;
         border-radius: 20px;
         background: white;
         box-shadow: 0 2px 8px rgba(0,0,0,0.1);
         transition: all 0.2s ease;
         justify-content: flex-start;
         min-width: 0;
      }

      .dashlet.document-stats .summary-item:hover {
         transform: translateY(-1px);
         box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      }

      .dashlet.document-stats .summary-icon {
         width: 16px;
         height: 16px;
         border-radius: 50%;
         box-shadow: 0 2px 6px rgba(0,0,0,0.2);
         flex-shrink: 0;
      }

      .dashlet.document-stats .summary-icon.approved {
         background: linear-gradient(135deg, #28a745, #20c997);
      }
      .dashlet.document-stats .summary-icon.disapproved {
         background: linear-gradient(135deg, #dc3545, #fd7e14);
      }
      .dashlet.document-stats .summary-icon.unclassified {
         background: linear-gradient(135deg, #6c757d, #adb5bd);
      }

      .dashlet.document-stats .summary-text {
         font-weight: 700;
         color: #2c3e50;
         font-size: 12px;
         white-space: nowrap;
         overflow: hidden;
         text-overflow: ellipsis;
         flex: 1;
      }

      .dashlet.document-stats .summary-text.approved-text {
         color: #28a745;
      }

      .dashlet.document-stats .summary-text.disapproved-text {
         color: #dc3545;
      }

      .dashlet.document-stats .summary-text.unclassified-text {
         color: #6c757d;
      }

      .dashlet.document-stats .stats-content {
         max-height: 0;
         overflow: hidden;
         transition: max-height 0.3s ease;
         background: white;
      }

      .dashlet.document-stats .stats-content.expanded {
         max-height: 250px;
      }

      .dashlet.document-stats .stats-detail {
         padding: 20px;
         display: flex;
         justify-content: space-around;
         gap: 16px;
      }

      .dashlet.document-stats .metric-item {
         text-align: center;
         flex: 1;
      }

      .dashlet.document-stats .metric-circle {
         width: 70px;
         height: 70px;
         border-radius: 50%;
         margin: 0 auto 12px;
         position: relative;
         display: flex;
         align-items: center;
         justify-content: center;
         background: #e9ecef;
      }

      .dashlet.document-stats .metric-circle::before {
         content: '';
         position: absolute;
         width: 45px;
         height: 45px;
         background: white;
         border-radius: 50%;
         z-index: 1;
      }

      .dashlet.document-stats .metric-percentage {
         position: relative;
         z-index: 2;
         font-size: 13px;
         font-weight: bold;
         color: #2c3e50;
      }

      .dashlet.document-stats .metric-label {
         font-size: 12px;
         color: #6c757d;
         text-transform: uppercase;
         font-weight: 500;
         margin-bottom: 8px;
      }

      .dashlet.document-stats .metric-count {
         font-size: 13px;
         font-weight: 600;
         color: #2c3e50;
      }

      .dashlet.document-stats .loading {
         text-align: center;
         color: #6c757d;
         font-style: italic;
         padding: 20px;
      }

      .dashlet.document-stats .error {
         text-align: center;
         color: #dc3545;
         padding: 20px;
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
                              var sites = data.sites.sort(function(a, b) {
                                 return b.stats.total - a.stats.total;
                              });
                              renderStats(sites, statsContainer);
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

            function renderStats(sites, container) {
               var html = '';

               for (var i = 0; i < sites.length; i++) {
                  var site = sites[i];
                  var stats = site.stats;
                  var siteId = 'site-' + i;

                  var approvedPercent = stats.total > 0 ? Math.round((stats.approved / stats.total) * 100) : 0;
                  var disapprovedPercent = stats.total > 0 ? Math.round((stats.disapproved / stats.total) * 100) : 0;
                  var unclassifiedPercent = stats.total > 0 ? Math.round((stats.unclassified / stats.total) * 100) : 0;

                  html += '<div class="site-section">';

                  html += '<div class="site-header" data-site="' + siteId + '">';
                  html += '<div class="site-info">';
                  html += '<span class="site-name">🏢 ' + escapeHtml(site.siteName) + '</span>';
                  html += '</div>';
                  html += '<div class="total-badge">';
                  html += '<span>' + stats.total + '</span>';
                  html += '<span class="expand-icon">▼</span>';
                  html += '</div>';
                  html += '</div>';

                  html += '<div class="stats-summary">';
                  html += '<div class="summary-item">';
                  html += '<span class="summary-icon approved"></span>';
                  html += '<span class="summary-text approved-text">' + stats.approved + ' Aprobados (' + approvedPercent + '%)</span>';
                  html += '</div>';
                  html += '<div class="summary-item">';
                  html += '<span class="summary-icon disapproved"></span>';
                  html += '<span class="summary-text disapproved-text">' + stats.disapproved + ' Desaprobados (' + disapprovedPercent + '%)</span>';
                  html += '</div>';
                  html += '<div class="summary-item">';
                  html += '<span class="summary-icon unclassified"></span>';
                  html += '<span class="summary-text unclassified-text">' + stats.unclassified + ' Sin clasificar (' + unclassifiedPercent + '%)</span>';
                  html += '</div>';
                  html += '</div>';

                  html += '<div class="stats-content" data-content="' + siteId + '">';
                  html += '<div class="stats-detail">';

                  html += '<div class="metric-item">';
                  html += '<div class="metric-label">Aprobados</div>';
                  html += '<div class="metric-circle approved-circle" data-percent="' + approvedPercent + '">';
                  html += '<div class="metric-percentage">' + approvedPercent + '%</div>';
                  html += '</div>';
                  html += '<div class="metric-count">' + stats.approved + ' documento' + (stats.approved !== 1 ? 's' : '') + '</div>';
                  html += '</div>';

                  html += '<div class="metric-item">';
                  html += '<div class="metric-label">Desaprobados</div>';
                  html += '<div class="metric-circle disapproved-circle" data-percent="' + disapprovedPercent + '">';
                  html += '<div class="metric-percentage">' + disapprovedPercent + '%</div>';
                  html += '</div>';
                  html += '<div class="metric-count">' + stats.disapproved + ' documento' + (stats.disapproved !== 1 ? 's' : '') + '</div>';
                  html += '</div>';

                  html += '<div class="metric-item">';
                  html += '<div class="metric-label">Sin clasificar</div>';
                  html += '<div class="metric-circle unclassified-circle" data-percent="' + unclassifiedPercent + '">';
                  html += '<div class="metric-percentage">' + unclassifiedPercent + '%</div>';
                  html += '</div>';
                  html += '<div class="metric-count">' + stats.unclassified + ' documento' + (stats.unclassified !== 1 ? 's' : '') + '</div>';
                  html += '</div>';

                  html += '</div>';
                  html += '</div>';
                  html += '</div>';
               }

               var updateTime = new Date().toLocaleString();
               html += '<div class="last-updated">Última actualización: ' + updateTime + '</div>';

               container.innerHTML = html;

               addClickHandlers(container);
               animateCircles(container);
            }

            function addClickHandlers(container) {
               var headers = container.querySelectorAll('.site-header');
               for (var j = 0; j < headers.length; j++) {
                  headers[j].addEventListener('click', function() {
                     var siteId = this.getAttribute('data-site');
                     var content = container.querySelector('[data-content="' + siteId + '"]');
                     var icon = this.querySelector('.expand-icon');

                     var allContents = container.querySelectorAll('.stats-content');
                     var allIcons = container.querySelectorAll('.expand-icon');

                     for (var k = 0; k < allContents.length; k++) {
                        if (allContents[k] !== content) {
                           allContents[k].classList.remove('expanded');
                           allIcons[k].classList.remove('expanded');
                        }
                     }

                     content.classList.toggle('expanded');
                     icon.classList.toggle('expanded');

                     if (content.classList.contains('expanded')) {
                        setTimeout(function() {
                           animateCirclesInSection(content);
                        }, 100);
                     }
                  });
               }
            }

            function animateCircles(container) {
               var expandedSections = container.querySelectorAll('.stats-content.expanded');
               expandedSections.forEach(function(section) {
                  animateCirclesInSection(section);
               });
            }

            function animateCirclesInSection(section) {
               setTimeout(function() {
                  var approvedCircles = section.querySelectorAll('.approved-circle');
                  var disapprovedCircles = section.querySelectorAll('.disapproved-circle');
                  var unclassifiedCircles = section.querySelectorAll('.unclassified-circle');

                  approvedCircles.forEach(function(circle) {
                     var percent = parseInt(circle.getAttribute('data-percent'));
                     var degrees = (percent / 100) * 360;
                     circle.style.background = 'conic-gradient(#28a745 0deg ' + degrees + 'deg, #e9ecef ' + degrees + 'deg 360deg)';
                  });

                  disapprovedCircles.forEach(function(circle) {
                     var percent = parseInt(circle.getAttribute('data-percent'));
                     var degrees = (percent / 100) * 360;
                     circle.style.background = 'conic-gradient(#dc3545 0deg ' + degrees + 'deg, #e9ecef ' + degrees + 'deg 360deg)';
                  });

                  unclassifiedCircles.forEach(function(circle) {
                     var percent = parseInt(circle.getAttribute('data-percent'));
                     var degrees = (percent / 100) * 360;
                     circle.style.background = 'conic-gradient(#6c757d 0deg ' + degrees + 'deg, #e9ecef ' + degrees + 'deg 360deg)';
                  });
               }, 300);
            }

            function escapeHtml(text) {
               var div = document.createElement('div');
               div.textContent = text;
               return div.innerHTML;
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