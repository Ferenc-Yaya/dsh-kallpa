package com.dataservicesperu.kallpa.model;

import java.util.Date;
import java.io.Serializable;

public class FormularioData implements Serializable {
    private String campo1;
    private String campo2;
    private String campo3;
    private String usuario;
    private String sitio;
    private Date fechaCreacion;
    private Date fechaModificacion;
    private String nodeRef;
    private String nombreArchivo;

    // Constructores
    public FormularioData() {}

    public FormularioData(String campo1, String campo2, String campo3) {
        this.campo1 = campo1;
        this.campo2 = campo2;
        this.campo3 = campo3;
    }

    // Getters y Setters
    public String getCampo1() { return campo1; }
    public void setCampo1(String campo1) { this.campo1 = campo1; }

    public String getCampo2() { return campo2; }
    public void setCampo2(String campo2) { this.campo2 = campo2; }

    public String getCampo3() { return campo3; }
    public void setCampo3(String campo3) { this.campo3 = campo3; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSitio() { return sitio; }
    public void setSitio(String sitio) { this.sitio = sitio; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public String getNodeRef() { return nodeRef; }
    public void setNodeRef(String nodeRef) { this.nodeRef = nodeRef; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
}
