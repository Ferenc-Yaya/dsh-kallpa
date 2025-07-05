/**
 * Utilidad CSRF automática para Document Share Hub
 */
var KallpaCSRF = {

    TOKEN_COOKIE_NAME: 'Alfresco-CSRFToken',
    TOKEN_HEADER_NAME: 'Alfresco-CSRFToken',

    /**
     * Obtiene el token CSRF actual
     */
    getToken: function() {
        return this.getCookie(this.TOKEN_COOKIE_NAME);
    },

    /**
     * Genera headers automáticos con CSRF
     */
    getHeaders: function(additionalHeaders) {
        var headers = {
            'Content-Type': 'application/json'
        };

        var token = this.getToken();
        if (token) {
            headers[this.TOKEN_HEADER_NAME] = token;
        }

        // Fusionar headers adicionales
        if (additionalHeaders) {
            for (var key in additionalHeaders) {
                headers[key] = additionalHeaders[key];
            }
        }

        return headers;
    },

    /**
     * Fetch automático con CSRF
     */
    fetch: function(url, options) {
        options = options || {};
        options.headers = this.getHeaders(options.headers);

        console.log('🔒 KallpaCSRF: Enviando request con token CSRF');

        return fetch(url, options)
            .then(function(response) {
                if (response.status === 403) {
                    console.warn('🚫 CSRF: Request bloqueada (403)');
                }
                return response;
            });
    },

    /**
     * Utilidad para obtener cookies
     */
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

    /**
     * Verificar estado CSRF
     */
    checkStatus: function() {
        var token = this.getToken();
        console.log('🔍 CSRF Status:', {
            tokenPresent: !!token,
            tokenValue: token ? token.substring(0, 8) + '...' : 'None'
        });
        return !!token;
    }
};

// Hacer disponible globalmente
window.KallpaCSRF = KallpaCSRF;