package com.dataservicesperu.kallpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

}
