package io.github.joanhermay.jxmi2php;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Josué Andrés Hernández Martínez
 */
class Metodo {
    private final List<String> parametros;
    private String nombre;
    private String visibilidad;
    private boolean abstracto;
    private boolean estatico;

    public Metodo() {
        this.nombre = "";
        this.visibilidad = "";
        this.parametros = new ArrayList<>();
    }

    // SETS-GETS
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(String visibilidad) {
        this.visibilidad = visibilidad;
    }

    public boolean esAbstracto() {
        return abstracto;
    }

    public void setAbstracto(boolean abstracto) {
        this.abstracto = abstracto;
    }

    public boolean esEstatico() {
        return estatico;
    }

    public void setEstatico(boolean estatico) {
        this.estatico = estatico;
    }

    public void agregarParametro(String nombreParametro) {
        this.parametros.add(nombreParametro);
    }

    public List<String> getParametros() {
        return parametros;
    }
}
