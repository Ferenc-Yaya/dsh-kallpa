package com.dataservicesperu.kallpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoData implements Serializable {
    private String numero;
    private String nombreApellidos;
    private String dni;
    private String puestoTrabajo;
    private String usuario;
    private String sitio;
    private Date fechaCreacion;
    private Date fechaModificacion;
    private String nodeRef;
    private String nombreArchivo;
}
