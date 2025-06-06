<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/modules/about-share.css" group="footer"/>
   <@link href="${url.context}/res/components/footer/footer.css" group="footer"/>
</@>
<@markup id="js">
   <@script src="${url.context}/res/modules/about-share.js" group="footer"/>
</@>
<@markup id="widgets">
   <@createWidgets/>
</@>
<@markup id="html">
   <@uniqueIdDiv>
      <#assign fc=config.scoped["Edition"]["footer"]>
      <div class="footer ${fc.getChildValue("css-class")!"footer-com"}">
         <span class="copyright">
            <a href="http://www.dataservicesperu.com" target="_blank"><img src="${url.context}/res/images/logo-dsh.png" alt="Document Share Hub" border="0" height="40" style="margin-right: 10px;"/></a>
            <span>${msg(fc.getChildValue("label")!"label.copyright")}</span>
         </span>
      </div>
   </@>
</@>