package com.github.josuemartinez.jxmi2php;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Josué Andrés Hernández Martínez
 */
class Clase {
    private String id;
    private String nombre;
    private boolean abstracta;
    private boolean interfaz;
    private List<Metodo> metodos;
    private List<Atributo> atributos;
    private boolean poseeConstructor;

    public Clase() {
        this.id = "";
        this.nombre = "";
        this.metodos = new ArrayList<>();
        this.atributos = new ArrayList<>();
    }

    // SETS-GETS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean esAbstracta() {
        return abstracta;
    }

    public void setAbstracta(boolean abstracta) {
        this.abstracta = abstracta;
    }

    public boolean esInterfaz() {
        return interfaz;
    }

    public void setInterfaz(boolean interfaz) {
        this.interfaz = interfaz;
    }

    public List<Metodo> getMetodos() {
        return metodos;
    }

    public void setMetodos(List<Metodo> metodos) {
        this.metodos = metodos;
    }

    public void agregarAtributo(Atributo atributo) {
        this.atributos.add(atributo);
    }

    public List<Atributo> getAtributos() {
        return atributos;
    }

    public void setAtributos(List<Atributo> atributos) {
        this.atributos = atributos;
    }

    public boolean seEncontroConstructor() {
        return poseeConstructor;
    }

    public void setPoseeConstructor(boolean poseeConstructor) {
        this.poseeConstructor = poseeConstructor;
    }

    // Métodos utilitarios
    private void generarAtributos(StringBuilder sb) {
        if (!getAtributos().isEmpty()) {
            sb.append("    // ATRIBUTOS").append('\n');
            for (Atributo a : getAtributos()) {
                if (!a.getNombre().isEmpty()) {
                    sb.append("    ");
                    if (a.esEstatico()) {
                        sb.append(a.getVisibilidad()).append(" static $").append(a.getNombre().toLowerCase()).append(';');
                    } else {
                        sb.append(a.getVisibilidad()).append(" $").append(a.getNombre().toLowerCase()).append(';');
                    }
                    sb.append('\n');
                }
            }
            sb.append('\n');
        }
    }

    private void generarSetsGets(StringBuilder sb) {
        if (!getAtributos().isEmpty()) {
            sb.append("    // SETS-GETS").append('\n');
            for (Atributo a : getAtributos()) {
                if (!a.getNombre().isEmpty()) {
                    sb.append("    ")
                            .append("public function set")
                            .append(hacerPalabraPrimeraLetraMayuscula(a.getNombre().toLowerCase()))
                            .append("($").append(a.getNombre().toLowerCase()).append(')').append("\n")
                            .append("    ").append('{').append('\n')
                            .append("        ").append("$this->").append(a.getNombre().toLowerCase()).append(" = ")
                            .append("$").append(a.getNombre().toLowerCase()).append(';').append('\n')
                            .append("    ").append('}').append('\n');
                    sb.append('\n');
                    sb.append("    ")
                            .append("public function get")
                            .append(hacerPalabraPrimeraLetraMayuscula(a.getNombre().toLowerCase())).append("()").append('\n')
                            .append("    ").append("{").append('\n')
                            .append("        ")
                            .append("return $this->").append(a.getNombre().toLowerCase()).append(';').append('\n')
                            .append("    ").append('}').append('\n');
                    sb.append('\n');
                }
            }
        }
    }

    private void generarMetodos(StringBuilder sb) {
        if (!getMetodos().isEmpty()) {
            sb.append("    // MÉTODOS").append('\n');
            for (Metodo m : getMetodos()) {
                if (!m.getNombre().isEmpty()) {
                    if (getNombre().equals(m.getNombre())) {
                        if (!esAbstracta() && !esInterfaz()) {
                            setPoseeConstructor(true);
                            sb.append("\n\n");
                            generarConstructor(sb, m);
                        }
                    } else {
                        sb.append("    ").append(m.getVisibilidad());
                        if (m.esAbstracto()) {
                            sb.append(" abstract ");
                        } else if (m.esEstatico()) {
                            sb.append(" static ");
                        } else {
                            sb.append(' ');
                        }
                        sb.append("function ").append(m.getNombre());
                        if (!m.getParametros().isEmpty()) {
                            sb.append('(');
                            for (String parametro : m.getParametros()) {
                                if (!parametro.isEmpty()) {
                                    sb.append('$').append(parametro).append(", ");
                                }
                            }
                            sb.replace(sb.length() - 2, sb.length(), ")");
                        } else {
                            sb.append("()");
                        }
                        sb.append('\n');
                        sb.append("    ");
                        sb.append('{').append('\n')
                                .append("        ")
                                .append("// Inserte su código aquí...").append('\n')
                                .append("    ")
                                .append('}');
                        sb.append("\n\n");
                    }
                }
            }
        }
    }

    private void generarConstructor(StringBuilder sb, Metodo metodoConstructor) {
        sb.append("    // CONSTRUCTOR SEGÚN DIAGRAMA").append('\n');
        sb.append("    ").append("public function __construct");
        if (!metodoConstructor.getParametros().isEmpty()) {
            sb.append('(');
            for (String parametro : metodoConstructor.getParametros()) {
                if (!parametro.isEmpty()) {
                    sb.append(parametro).append(", ");
                }
            }
            sb.replace(sb.length() - 2, sb.length(), ")");
        } else {
            sb.append("()");
        }
        sb.append('\n');
        sb.append("    ").append('{').append('\n');
        if (!metodoConstructor.getParametros().isEmpty()) {
            for (String p : metodoConstructor.getParametros()) {
                if (!p.isEmpty()) {
                    sb.append("        ");
                    sb.append("$this->").append(p).append(" = ").append('$').append(p).append(';').append('\n');
                }
            }
        }
        sb.append("    ").append('}');
    }

    private void generarConstructorSiNoSeEncuentraDefinido(StringBuilder sb) {
        if (!seEncontroConstructor()) {
            if (!getAtributos().isEmpty()) {
                sb.append("    // CONSTRUCTOR GENERADO AUTOMATICAMENTE\n");
                sb.append("    ").append("public function __construct");
                sb.append('(');
                for (Atributo a : getAtributos()) {
                    if (!a.getNombre().isEmpty()) {
                        sb.append('$').append(a.getNombre().toLowerCase()).append(", ");
                    }
                }
                sb.replace(sb.length() - 2, sb.length(), ")");
                sb.append('\n');
                sb.append("    ").append('{').append('\n');
                for (Atributo a : getAtributos()) {
                    if (!a.getNombre().isEmpty()) {
                        if (a.EsReferenciaAUnaClase()) {
                            sb.append("        ")
                                    .append("$this->").append(a.getNombre().toLowerCase()).append(" = ")
                                    .append("new $").append(a.getNombre())
                                    .append("();").append('\n');
                        } else {
                            sb.append("        ");
                            sb.append("$this->").append(a.getNombre().toLowerCase()).append(" = ")
                                    .append('$').append(a.getNombre().toLowerCase()).append(';').append('\n');
                        }
                    }
                }
                sb.append("    ").append('}');
            }
        }
        sb.append('\n');
    }

    private String hacerPalabraPrimeraLetraMayuscula(String texto) {
        return texto.substring(0, 1).toUpperCase().concat(texto.substring(1));
    }

    public final String generarCodigo() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?php").append('\n');
        if (!getAtributos().isEmpty()) {
            // Los 'require' que necesita la clase, si hay atributos marcados
            for (Atributo a : getAtributos()) {
                if (!a.getNombre().isEmpty()) {
                    if (a.EsReferenciaAUnaClase()) {
                        sb.append("require ")
                                .append("'")
                                .append(hacerPalabraPrimeraLetraMayuscula(a.getNombre())).append(".php")
                                .append("';")
                                .append('\n');
                    }
                }
            }
        }
        sb.append('\n');

        // Información básica de la clase
        if (esAbstracta()) {
            sb.append("abstract class ");
            sb.append(getNombre()).append("\n{\n\n");
            generarAtributos(sb);
            generarSetsGets(sb);
            generarMetodos(sb);
        } else {
            if (esInterfaz()) {
                sb.append("interface ");
                sb.append(getNombre()).append("\n{\n\n");
                generarMetodos(sb);
            } else {
                sb.append("class ");
                sb.append(getNombre()).append("\n{\n\n");
                generarAtributos(sb);
                sb.append('\n');
                generarSetsGets(sb);
                generarMetodos(sb);

                // Crear constructor si no se encontró uno definido
                generarConstructorSiNoSeEncuentraDefinido(sb);
            }
        }
        sb.append("}\n");
        sb.append("?>");
        return sb.toString();
    }
}
