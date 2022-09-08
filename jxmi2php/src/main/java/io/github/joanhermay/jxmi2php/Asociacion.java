package io.github.joanhermay.jxmi2php;

/**
 * @author Josué Andrés Hernández Martínez
 */
class Asociacion {
    private String idOrigen;
    private String idDestino;

    public Asociacion() {
        this.idOrigen = "";
        this.idDestino = "";
    }

    // SETS-GETS
    public String getIdOrigen() {
        return idOrigen;
    }

    public void setIdOrigen(String idOrigen) {
        this.idOrigen = idOrigen;
    }

    public String getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(String idDestino) {
        this.idDestino = idDestino;
    }
}
