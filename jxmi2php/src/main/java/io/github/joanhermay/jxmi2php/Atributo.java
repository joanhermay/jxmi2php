package io.github.joanhermay.jxmi2php;

/**
 * @author Josué Andrés Hernández Martínez
 */
class Atributo {
    private String nombre;
    private String visibilidad;
    private boolean estatico;
    private boolean esReferenciaAUnaClase;

    public Atributo() {
        this.nombre = "";
        this.visibilidad = "";
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

    public boolean esEstatico() {
        return estatico;
    }

    public void setEstatico(boolean estatico) {
        this.estatico = estatico;
    }

    public boolean EsReferenciaAUnaClase() {
        return esReferenciaAUnaClase;
    }

    public void setEsReferenciaAUnaClase(boolean esReferenciaAUnaClase) {
        this.esReferenciaAUnaClase = esReferenciaAUnaClase;
    }
}
