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
      .required { color: red; }
   </style>
</head>
<body>
   <div class="form-container">
      <div class="form-header">
         <h1>📝 Formulario Simple</h1>
         <p>Complete los siguientes campos</p>
      </div>

      <div id="message"></div>

      <form id="formulario-simple">
         <div class="form-field">
            <label for="campo1">Campo 1 <span class="required">*</span></label>
            <input type="text" id="campo1" name="campo1" required>
         </div>

         <div class="form-field">
            <label for="campo2">Campo 2 <span class="required">*</span></label>
            <input type="text" id="campo2" name="campo2" required>
         </div>

         <div class="form-field">
            <label for="campo3">Campo 3</label>
            <textarea id="campo3" name="campo3" rows="3"></textarea>
         </div>

         <div style="text-align: center; margin-top: 30px;">
            <button type="button" onclick="procesarFormulario()" class="btn btn-primary">
               💾 Crear Archivo
            </button>
            <button type="button" onclick="window.history.back()" class="btn btn-secondary">
               ❌ Cancelar
            </button>
         </div>
      </form>
   </div>

   <script>
      function procesarFormulario() {
         const campo1 = document.getElementById('campo1').value.trim();
         const campo2 = document.getElementById('campo2').value.trim();
         const campo3 = document.getElementById('campo3').value.trim();

         if (!campo1 || !campo2) {
            alert('Los campos 1 y 2 son obligatorios');
            return;
         }

         // Mostrar indicador de carga
         document.getElementById('message').innerHTML = '<div style="color: blue; padding: 10px;">⏳ Creando archivo...</div>';

         // Enviar datos al webscript del repository
         const datos = {
            campo1: campo1,
            campo2: campo2,
            campo3: campo3
         };

         fetch('/share/proxy/alfresco/service/formulario/procesar', {
            method: 'POST',
            headers: {
               'Content-Type': 'application/json',
            },
            body: JSON.stringify(datos)
         })
         .then(response => response.json())
         .then(data => {
            if (data.success) {
               document.getElementById('message').innerHTML = '<div style="color: green; padding: 10px;">✅ Archivo creado: ' + data.filename + '</div>';
               // Limpiar formulario
               document.getElementById('formulario-simple').reset();
               // Redireccionar después de 2 segundos
               setTimeout(() => {
                  window.location.href = '/share/page/context/mine/myfiles';
               }, 2000);
            } else {
               document.getElementById('message').innerHTML = '<div style="color: red; padding: 10px;">❌ Error: ' + data.message + '</div>';
            }
         })
         .catch(error => {
            document.getElementById('message').innerHTML = '<div style="color: red; padding: 10px;">❌ Error de conexión: ' + error.message + '</div>';
         });
      }
   </script>
</body>
</html>