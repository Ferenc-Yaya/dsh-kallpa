<@markup id="css" >
   <@link href="${url.context}/res/components/guest/login.css" group="login"/>
   <style type="text/css">
      .login input[type="text"], .login input[type="password"] {
         border: 2px solid rgba(102, 126, 234, 0.3) !important;
         border-radius: 6px !important;
         background-color: rgba(255, 255, 255, 0.95) !important;
         outline: none !important;
         font-size: 120% !important;
         width: 316px !important;
         padding: 12px 15px !important;
         transition: all 0.3s ease !important;
         box-sizing: border-box !important;
      }
      .login input[type="text"]:focus, .login input[type="password"]:focus {
         border-color: #667eea !important;
         box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2) !important;
         background-color: white !important;
      }
      .form-fields.login .form-field > span.yui-button > .first-child > button {
          background: #667eea !important;
          background-color: #667eea !important;
          background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
          border: 2px solid #667eea !important;
          border-radius: 8px !important;
          color: white !important;
          font-weight: bold !important;
          text-transform: uppercase !important;
          width: 316px !important;
          height: 50px !important;
          padding: 12px 15px !important;
          box-sizing: border-box !important;
          cursor: pointer !important;
          transition: all 0.3s ease !important;
          box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3) !important;
      }
      .form-fields.login .form-field > span.yui-button > .first-child > button:hover {
          background: #5a67d8 !important;
          background-color: #5a67d8 !important;
          background-image: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%) !important;
          border-color: #5a67d8 !important;
          transform: translateY(-2px) !important;
          box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4) !important;
      }
      .form-fields.login .form-field > span.yui-button {
          width: 316px !important;
          background: none !important;
          border: none !important;
      }
      .form-fields.login .form-field > span.yui-button > .first-child {
          background: none !important;
          border: none !important;
      }
      body.alfresco-share.alfresco-guest {
          background: transparent !important;
      }
      .dsh-title-futuristic {
          color: #667eea;
          font-size: 20px;
          font-weight: 700;
          letter-spacing: 2px;
          font-family: 'Open Sans', Arial, sans-serif;
          text-transform: uppercase;
          margin-top: 12px;
          text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
      }
   </style>
</@>

<@markup id="js">
   <@script src="${url.context}/res/components/guest/login.js" group="login"/>
</@>

<@markup id="widgets">
   <@createWidgets group="login"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>

      <div style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
                  background: url('${url.context}/res/images/fondo-corporativo.png') no-repeat center center;
                  background-size: cover; z-index: -2;"></div>

      <div style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
                  background: rgba(0, 0, 0, 0.3); z-index: -1;"></div>

      <div id="${el}-body" class="theme-overlay login hidden">

      <@markup id="header">
         <div style="text-align: center; margin-bottom: 30px;">
             <img src="${url.context}/res/images/logo-dsh.png"
                  alt="DSH Document Share Hub"
                  style="max-width: 200px; height: auto; display: block; margin: 0 auto 15px auto;">
             <div class="dsh-title-futuristic">
                 DOCUMENT SHARE HUB
             </div>
         </div>
      </@markup>

      <#if errorDisplay == "container">
      <@markup id="error">
         <#if error>
         <div class="error">${msg("message.loginautherror")}</div>
         <#else>
         <script type="text/javascript">//<![CDATA[
            <#assign cookieHeadersConfig = config.scoped["COOKIES"] />
            <#if cookieHeadersConfig?? && (cookieHeadersConfig.secure.getValue() == "true" || cookieHeadersConfig.secure.getValue() == "false")>
               Alfresco.constants.secureCookie = ${cookieHeadersConfig.secure.getValue()};
               Alfresco.constants.sameSite = "${cookieHeadersConfig.sameSite.getValue()}";
            </#if>

            var cookieDefinition = "_alfTest=_alfTest; Path=/;";
            if(Alfresco.constants.secureCookie)
            {
               cookieDefinition += " Secure;";
            }
            if(Alfresco.constants.sameSite)
            {
               cookieDefinition += " SameSite="+Alfresco.constants.sameSite+";";
            }
            document.cookie = cookieDefinition;

            var cookieEnabled = (document.cookie.indexOf("_alfTest") !== -1);
            if (!cookieEnabled)
            {
               document.write('<div class="error">${msg("message.cookieserror")}</div>');
            }
         //]]></script>
         </#if>
      </@markup>
      </#if>

      <@markup id="form">
         <form id="${el}-form" accept-charset="UTF-8" method="post" action="${loginUrl}" class="form-fields login">
            <@markup id="fields">
            <input type="hidden" id="${el}-success" name="success" value="${successUrl?replace("@","%40")?html}"/>
            <input type="hidden" name="failure" value="${failureUrl?replace("@","%40")?html}"/>
            <div class="form-field">
               <input type="text" id="${el}-username" name="username" maxlength="255" value="<#if lastUsername??>${lastUsername?html}</#if>" placeholder="${msg("label.username")}" />
            </div>
            <div class="form-field">
               <input type="password" id="${el}-password" name="password" maxlength="255" placeholder="${msg("label.password")}" />
            </div>
            </@markup>
            <@markup id="buttons">
            <div class="form-field">
               <input type="submit" id="${el}-submit" class="login-button" value="${msg("button.login")}"/>
            </div>
            </@markup>
         </form>
      </@markup>

      <@markup id="preloader">
         <script type="text/javascript">//<![CDATA[
            window.onload = function()
            {
                setTimeout(function()
                {
                    var xhr;
                    <#list dependencies as dependency>
                       xhr = new XMLHttpRequest();
                       xhr.open('GET', '<@checksumResource src="${url.context}/res/${dependency}"/>');
                       xhr.send('');
                    </#list>
                    <#list images as image>
                       new Image().src = "${url.context?js_string}/res/${image}";
                    </#list>
                }, 1000);
            };
         //]]></script>
      </@markup>

      </div>

      <@markup id="footer">
      <div class="login-copy">
          Sistema de Gestion Documentario Document Share Hub<br>
          Conecta. Comparte. Gestiona.<br>
          powered by Data Services © 2025
      </div>
      </@markup>
   </@>
</@>
