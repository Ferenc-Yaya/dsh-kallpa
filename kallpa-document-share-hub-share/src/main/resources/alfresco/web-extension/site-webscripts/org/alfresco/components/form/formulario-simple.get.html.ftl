<!DOCTYPE html>
<html>
<head>
   <title>Gestión de Empleados - DSH</title>
   <meta charset="utf-8">
   <style>
      body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
      .form-container {
         background: white; padding: 30px; border-radius: 8px;
         box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 900px; margin: 0 auto;
      }
      .form-header { text-align: center; margin-bottom: 30px; color: #333; }
      .form-field { margin-bottom: 20px; }
      .form-field label { display: block; font-weight: bold; margin-bottom: 5px; color: #555; }
      .form-field input {
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
      .empleados-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
      .empleados-table th, .empleados-table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
      .empleados-table th { background-color: #f8f9fa; font-weight: bold; }
      .empleados-table tr:hover { background-color: #f5f5f5; }
      .two-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
      @media (max-width: 768px) { .two-columns { grid-template-columns: 1fr; } }
   </style>
</head>
<body>
   <div class="form-container">
      <div class="form-header">
         <h1>👥 Gestión de Empleados</h1>
         <p>Sistema de registro de empleados</p>
      </div>

      <div id="message"></div>

      <div class="two-columns">
         <div>
            <h3>📝 Agregar Empleado</h3>

            <form id="formularioEmpleado">
               <div class="form-field">
                  <label for="numero">N°</label>
                  <input type="text" id="numero" name="numero" placeholder="Se genera automáticamente" readonly>
               </div>

               <div class="form-field">
                  <label for="nombreApellidos">Nombre y Apellidos <span class="required">*</span></label>
                  <input type="text" id="nombreApellidos" name="nombreApellidos" required>
               </div>

               <div class="form-field">
                  <label for="dni">DNI <span class="required">*</span></label>
                  <input type="text" id="dni" name="dni" maxlength="8" required>
               </div>

               <div class="form-field">
                  <label for="puestoTrabajo">Puesto de Trabajo <span class="required">*</span></label>
                  <input type="text" id="puestoTrabajo" name="puestoTrabajo" required>
               </div>

               <div style="text-align: center; margin-top: 30px;">
                  <button type="button" onclick="agregarEmpleado()" class="btn btn-primary">
                     ➕ Agregar
                  </button>
                  <button type="button" onclick="limpiarFormulario()" class="btn btn-secondary">
                     🗑️ Limpiar
                  </button>
               </div>
            </form>
         </div>

         <div>
            <h3>📋 Lista de Empleados</h3>
            <div id="empleadosContainer">
               <div class="info-banner">ℹ️ Cargando...</div>
            </div>
            <div style="text-align: center; margin-top: 20px;">
               <button type="button" onclick="cargarEmpleados()" class="btn btn-secondary">
                  🔄 Recargar
               </button>
               <button type="button" onclick="eliminarTodo()" class="btn btn-danger" id="btnEliminarTodo" style="display: none;">
                  🗑️ Eliminar Todo
               </button>
            </div>
         </div>
      </div>
   </div>

   <script>
      let datosGlobales = {
         nodeRefArchivo: '',
         empleados: []
      };

      function obtenerSiteId() {
         var match = window.location.href.match(/\/site\/([^\/\?]+)/);
         return match ? match[1] : null;
      }

      function mostrarMensaje(mensaje, tipo) {
         var color = tipo === 'error' ? 'red' : tipo === 'loading' ? 'blue' : 'green';
         document.getElementById('message').innerHTML =
            '<div style="color: ' + color + '; padding: 10px; border-radius: 4px; background: ' +
            (tipo === 'error' ? '#ffe6e6' : tipo === 'loading' ? '#e7f3ff' : '#d4edda') + ';">' + mensaje + '</div>';
      }

      function cargarEmpleados() {
         mostrarMensaje('⏳ Cargando empleados...', 'loading');

         var siteId = obtenerSiteId();
         var url = '/share/proxy/alfresco/formulario' + (siteId ? '?site=' + encodeURIComponent(siteId) : '');

         fetch(url)
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               datosGlobales.nodeRefArchivo = data.nodeRefArchivo || '';
               datosGlobales.empleados = data.empleados || [];

               mostrarListaEmpleados(data.empleados);
               document.getElementById('btnEliminarTodo').style.display = data.tieneArchivo ? 'inline-block' : 'none';

               if (data.empleados.length > 0) {
                  mostrarMensaje('✅ ' + data.empleados.length + ' empleados cargados', 'success');
               } else {
                  mostrarMensaje('ℹ️ No hay empleados registrados', 'info');
               }
            } else {
               mostrarMensaje('❌ Error: ' + data.message, 'error');
            }
         })
         .catch(error => mostrarMensaje('❌ Error: ' + error.message, 'error'));
      }

      function mostrarListaEmpleados(empleados) {
         var container = document.getElementById('empleadosContainer');

         if (!empleados || empleados.length === 0) {
            container.innerHTML = '<div class="info-banner">📋 No hay empleados</div>';
            return;
         }

         var html = '<table class="empleados-table">';
         html += '<thead><tr><th>N°</th><th>Nombre y Apellidos</th><th>DNI</th><th>Puesto</th></tr></thead><tbody>';

         empleados.forEach(function(emp) {
            html += '<tr>';
            html += '<td>' + (emp.numero || '') + '</td>';
            html += '<td>' + (emp.nombreApellidos || '') + '</td>';
            html += '<td>' + (emp.dni || '') + '</td>';
            html += '<td>' + (emp.puestoTrabajo || '') + '</td>';
            html += '</tr>';
         });

         html += '</tbody></table>';
         container.innerHTML = html;
      }

      function agregarEmpleado() {
         var nombreApellidos = document.getElementById('nombreApellidos').value.trim();
         var dni = document.getElementById('dni').value.trim();
         var puestoTrabajo = document.getElementById('puestoTrabajo').value.trim();

         if (!nombreApellidos || !dni || !puestoTrabajo) {
            alert('Todos los campos son obligatorios');
            return;
         }

         if (dni.length !== 8 || !/^\d+$/.test(dni)) {
            alert('El DNI debe tener 8 dígitos');
            return;
         }

         mostrarMensaje('⏳ Agregando empleado...', 'loading');

         var siteId = obtenerSiteId();
         var url = '/share/proxy/alfresco/formulario' + (siteId ? '?site=' + encodeURIComponent(siteId) : '');

         fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
               operacion: 'procesar',
               numero: '',
               nombreApellidos: nombreApellidos,
               dni: dni,
               puestoTrabajo: puestoTrabajo
            })
         })
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               mostrarMensaje('✅ ' + data.message + ' (N° ' + data.numeroAsignado + ')', 'success');
               limpiarFormulario();
               setTimeout(() => cargarEmpleados(), 1000);
            } else {
               mostrarMensaje('❌ Error: ' + data.message, 'error');
            }
         })
         .catch(error => mostrarMensaje('❌ Error: ' + error.message, 'error'));
      }

      function limpiarFormulario() {
         document.getElementById('nombreApellidos').value = '';
         document.getElementById('dni').value = '';
         document.getElementById('puestoTrabajo').value = '';
      }

      function eliminarTodo() {
         if (!datosGlobales.nodeRefArchivo || !confirm('¿Eliminar TODOS los empleados?')) return;

         mostrarMensaje('⏳ Eliminando...', 'loading');

         var siteId = obtenerSiteId();
         var url = '/share/proxy/alfresco/formulario' + (siteId ? '?site=' + encodeURIComponent(siteId) : '');

         fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
               operacion: 'eliminar',
               nodeRefArchivo: datosGlobales.nodeRefArchivo
            })
         })
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               mostrarMensaje('✅ Archivo eliminado', 'success');
               setTimeout(() => cargarEmpleados(), 1000);
            } else {
               mostrarMensaje('❌ Error: ' + data.message, 'error');
            }
         })
         .catch(error => mostrarMensaje('❌ Error: ' + error.message, 'error'));
      }

      document.addEventListener('DOMContentLoaded', function() {
         cargarEmpleados();
      });
   </script>
</body>
</html>