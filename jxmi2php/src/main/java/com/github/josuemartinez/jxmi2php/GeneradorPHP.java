package com.github.josuemartinez.jxmi2php;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Josué Andrés Hernández Martínez
 */
public final class GeneradorPHP {
    // Mensajes de error generales
    private static final String ERROR_NO_EXPORTADO_POR_STARUML = "ERROR EN CONVERSOR PHP: " +
            "El archivo no fue exportado por StarUML.";

    private static final String ERROR_VERSION_ARCHIVO = "ERROR EN CONVERSOR PHP: " +
            "El archivo se reconoce como una versión menor a la soportada. " +
            "Versión del documento soportada por el conversor: 2.0.";

    private static final String ERROR_DOCUMENTO_DESCONOCIDO = "ERROR EN CONVERSOR PHP: " +
            "El archivo no posee la información necesaria para ser reconocido como un archivo" +
            "exportado por StarUML.";

    private static final String ERROR_IO = "ERROR CRÍTICO EN CONVERSOR PHP: " +
            "No se puede acceder al archivo o no existe.";

    private static final String ERROR_ANALISIS = "ERROR CRÍTICO EN CONVERSOR PHP: " +
            "Conversión cancelada. Se encontraron inconsistencias en la estructura del archivo.";

    /**
     * Convierte un diagrama de clases UML, exportado como código XMI por StarUML, a código PHP.
     *
     * @param rutaAbsolutaDelArchivo Ruta absoluta del archivo que contiene el código XMI a convertir.
     * @return <p>Map con todas las clases encontradas en el código XMI del archivo.
     * <p>Map vacío si el archivo no existe o hubo error en el análisis del código XMI.
     * @throws GeneradorPHPException Si el archivo no contiene código XMI, el código XMI tiene
     *                               errores o está incompleto.
     */
    public final Map<String, String> generarPHP(String rutaAbsolutaDelArchivo) throws GeneradorPHPException {
        Map<String, String> clasesFinales = new HashMap<>();

        try {
            // Apertura y creación del DOM
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document documento = dBuilder.parse(new File(rutaAbsolutaDelArchivo));
            documento.getDocumentElement().normalize();

            // Nodos de todos los tag que contienen la información básica necesaria para validar el código.
            // En realidad solo es uno. Esta es la manera más fácil de obtenerlo.
            NodeList nodosDelTagDocumentation = documento.getElementsByTagName("xmi:Documentation");

            if ((nodosDelTagDocumentation.getLength() > 0)) {
                // Si se encontraron los tags 'Documentation', se procede a verificar que contenga
                // los valores esperados para ser identificado como exportado por StarUML.
                // En caso contrario, se lanza una excepción con un mensaje acorde al error que
                // se encontró.
                //
                // Si no hubo errores en la verificación, se procede inmediatamente al proceso
                // de análisis y extracción de la información de las clases.
                //
                // El análisis fallará sí la estructura del documento posee inconsistencias en el código,
                // tales como:
                //
                // - Falta de tags de cierre (más común).
                // - El nombre del tag no concuerda con su respectivo tag de apertura o cierre.
                // - Presencia de caracteres extraños.
                //
                // Si esto ocurre, se lanzará una excepción con un mensaje indicando que
                // el código posee inconsistencias.

                // Validando el archivo
                validarArchivo(nodosDelTagDocumentation);

                // Nodos de todos los tags referentes a clases y asociaciones
                NodeList nodosDelTagPackageElement = documento.getElementsByTagName("packagedElement");
                NodeList nodosDelTagOwnedMember = documento.getElementsByTagName("ownedMember");

                // Creando representacion de cada una de las clases y asociaciones,
                // y modificar las clases afectadas
                List<Clase> clases = obtenerClases(nodosDelTagPackageElement);
                List<Asociacion> asociaciones = obtenerAsociaciones(nodosDelTagOwnedMember);
                modificarClasesAfectadasPorAsociaciones(asociaciones, clases);

                clasesFinales.put("Main", generarMain(clases));
                for (Clase clase : clases) {
                    clasesFinales.put(clase.getNombre(), clase.generarCodigo());
                }
            } else {
                // Si no se encontró ningún tag, se asume que es código XMI pero no con
                // la estructura esperada.
                throw new GeneradorPHPException(ERROR_DOCUMENTO_DESCONOCIDO);
            }
        } catch (IOException e) {
            throw new GeneradorPHPException(ERROR_IO, e);
        } catch (ParserConfigurationException | SAXException e) {
            if (!e.getMessage().contains("prólogo")) {
                // Si no contiene la palabra 'prologo' en el mensaje de la excepción,
                // es un error de análisis-extracción.
                throw new GeneradorPHPException(ERROR_ANALISIS, e);
            }
        }
        return clasesFinales;
    }

    private String generarMain(List<Clase> clases) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?php").append('\n');
        sb.append('{').append('\n');
        sb.append("    ").append(salidaPHP("Main.php - SCRIPT PARA PROBAR LAS CLASES GENERADAS<br><br>", true)).append('\n');
        sb.append("    ").append(salidaPHP("Todos los valores a ser asignados a los atributos de las clases seran un string.<br>", true)).append('\n');
        sb.append("    ").append(salidaPHP("Solo usted, el usuario, sabe los verdaderos valores con los que trabajaran estos atributos.<br>", true)).append('\n');
        sb.append("    ").append(salidaPHP("Se recalca que estos valores son solo para probar la funcionalidad basica de las clases.<br>", true)).append('\n');
        sb.append("    ").append(salidaPHP("=============================================================<br>", true)).append('\n');
        sb.append("    ").append(salidaPHP("<br>", true)).append("\n\n");

        // Agregar requires
        sb.append("    ").append("// REQUIRES\n");
        for (Clase c : clases) {
            sb.append("    ").append("require_once '").append(c.getNombre()).append(".php';").append('\n');
        }
        sb.append("\n\n");

        sb.append("    ").append("// INSTANCIACIONES\n");
        for (Clase c : clases) {
            if (!c.esInterfaz() && !c.esAbstracta()) {
                sb.append("    ").append(salidaPHP("INSTANCIANDO CLASE: " + c.getNombre() + "<br>", true)).append('\n');
                sb.append("    ").append('$').append(c.getNombre().toLowerCase()).append(" = ")
                        .append("new ").append(c.getNombre()).append("();\n");
            } else {
                sb.append("    ").append(salidaPHP("LA CLASE: " + c.getNombre() + ", no puede ser instanciada.<br>", true)).append('\n');
                sb.append("    ").append(salidaPHP("Es una interfaz o es una clase abstracta.<br>", true)).append("\n\n");

            }
        }
        sb.append("    ").append(salidaPHP("<br>", true)).append('\n');
        sb.append("    ").append(salidaPHP("=============================================================<br>", true)).append('\n');
        sb.append('\n');
        sb.append('\n');

        sb.append("    ").append("// PRUEBA SETS ATRIBUTOS NORMALES\n");
        for (Clase c : clases) {
            if (!c.esAbstracta() && !c.esInterfaz()) {
                int contador = 0;
                sb.append("    ").append(salidaPHP("PRUEBA DE METODOS SET PARA LOS ATRIBUTOS DE LA CLASE: " + c.getNombre() + "<br>", true)).append('\n');
                for (Atributo a : c.getAtributos()) {
                    if (!a.EsReferenciaAUnaClase()) {
                        sb.append("    ").append(salidaPHP("Usando metodo set para agregar el valor de prueba al atributo: " + a.getNombre().toLowerCase() + "<br>", true)).append('\n');
                        sb.append("    ").append('$');
                        sb.append(c.getNombre().toLowerCase()).append("->");
                        sb.append("set").append(hacerPalabraPrimeraLetraMayuscula(a.getNombre()));
                        sb.append('(').append("\"dato").append(++contador).append("\")").append(';');
                        sb.append('\n');
                        sb.append("    ").append(salidaPHP("Valor de prueba usado: dato" + contador, true)).append('\n');
                    } else {
                        sb.append("    ").append(salidaPHP("Usando metodo set para agregar un objeto al atributo: " + a.getNombre().toLowerCase() + "<br>", true)).append('\n');
                        sb.append("    ").append('$');
                        sb.append(c.getNombre().toLowerCase()).append("->");
                        sb.append("set").append(hacerPalabraPrimeraLetraMayuscula(a.getNombre()));
                        sb.append("($").append(a.getNombre().toLowerCase()).append(");").append('\n');
                        sb.append("    ").append(salidaPHP("Valor usado: El objeto " + a.getNombre() + " previamente creado", true)).append('\n');
                    }
                    sb.append("    ").append(salidaPHP("<br>", true)).append('\n');
                }
                sb.append("    ").append(salidaPHP("<br>", true)).append('\n');
            }
            sb.append("    ").append(salidaPHP("<br>", true)).append('\n');
            sb.append('\n');
            sb.append('\n');
        }

        sb.append("    ").append(salidaPHP("=============================================================<br>", true)).append('\n');
        sb.append("    ").append("//PRUEBAS GET\n");
        for (Clase c : clases) {
            if(!c.esInterfaz() && !c.esAbstracta()) {
                sb.append("    ").append(salidaPHP("PRUEBAS DE METODOS GET DE LA CLASE: " + c.getNombre() + "<br>", true)).append('\n');
                for (Atributo a : c.getAtributos()) {
                    sb.append("    ").append(salidaPHP("Usando metodo get para obtener el atributo: " + a.getNombre().toLowerCase() + "<br>", true)).append('\n');
                    sb.append("    ").append(salidaPHP("Resultado: ", true)).append('\n');
                    sb.append("    ").append(salidaPHP(
                            "$" + c.getNombre().toLowerCase(Locale.ROOT) + "->" + "get" +
                                    hacerPalabraPrimeraLetraMayuscula(a.getNombre()) + "()",
                            false)
                    ).append('\n');
                    sb.append("    ").append(salidaPHP("<br>", true)).append('\n');
                }
                sb.append("    ").append(salidaPHP("<br>", true)).append('\n');
            }
        }
        sb.append('}').append('\n');
        sb.append("?>").append('\n');
        return sb.toString();
    }

    private String hacerPalabraPrimeraLetraMayuscula(String texto) {
        return texto.substring(0, 1).toUpperCase().concat(texto.substring(1));
    }

    private String salidaPHP(String comentario, boolean conComillas) {
        if (conComillas) {
            return "echo \"" + comentario + "\";";
        } else {
            return "echo " + comentario + ";";
        }

    }

    /**
     * Pasa toda la información de cada clase contenida en el Map, obtenido
     * exclusivamente por {@code generarPHP(String)}, en su respectivo
     * archivo '.php'.
     *
     * <p>Este proceso podría fallar y no todas las clases presentes en el
     * Map serán generados.
     *
     * @param clases            Map que contiene la información de las clases.
     * @param rutaDeGuardadoPHP Ruta donde se guardará la información
     *                          de las clases, contenida en el map.
     * @throws GeneradorPHPException Si un archivo no puede ser creado o no
     *                               no puede escribirse en el.
     */
    public final void generarArchivosPHP(Map<String, String> clases, String rutaDeGuardadoPHP)
            throws GeneradorPHPException {
        for (Map.Entry<String, String> clase : clases.entrySet()) {
            if (clase.getKey() != null && clase.getValue() != null) {
                String nombreArchivo = clase.getKey() + ".php";
                try {
                    File archivo = new File(rutaDeGuardadoPHP, nombreArchivo);
                    if (!archivo.exists()) {
                        if (archivo.createNewFile()) {
                            try (FileWriter fw = new FileWriter(archivo); BufferedWriter bw = new BufferedWriter(fw)) {
                                bw.write(clase.getValue());
                            } catch (IOException e) {
                                throw new GeneradorPHPException("ADVERTENCIA CONVERSOR PHP: " +
                                        "Proceso de generación de los archivos detenida. " +
                                        "No se puede leer o escribir en el archivo " + clase.getKey() + ". " +
                                        "No existe o no es accesible. " +
                                        "Presionar 'Siguiente' solo mostrará las clases que se" +
                                        "lograron generar antes del error.", e);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new GeneradorPHPException("ADVERTENCIA CONVERSOR PHP: " +
                            "Proceso de generación de los archivos detenida. " +
                            "No se pudo crear el archivo: " + clase.getKey() + ". " +
                            "Presionar 'Siguiente' solo mostrará las clases que se" +
                            "lograron generar antes del error", e);
                }
            }
        }
    }

    // Métodos utilitarios

    /**
     * @param nodosDelTagDocumentation Nodos que contienen la información para verificar el archivo.
     * @throws GeneradorPHPException Si el archivo no puede determinarse que es un archivo
     *                               exportado por StarUML.
     */
    private void validarArchivo(NodeList nodosDelTagDocumentation) throws GeneradorPHPException {
        for (int i = 0; i < nodosDelTagDocumentation.getLength(); i++) {
            Node nodoDocumentation = nodosDelTagDocumentation.item(i);
            Element elementoDocumentation = (Element) nodoDocumentation;
            if (!elementoDocumentation.getAttribute("exporter").equals("StarUML")) {
                throw new GeneradorPHPException(ERROR_NO_EXPORTADO_POR_STARUML);
            }
            if (!elementoDocumentation.getAttribute("exporterVersion").equals("2.0")) {
                throw new GeneradorPHPException(ERROR_VERSION_ARCHIVO);
            }
        }
    }

    /**
     * @param nodosDelTagPackageElement Nodos del tag 'packageElement" que contienen la
     *                                  información de cada una de las clases.
     * @return Lista con todas las clases encontradas en el archivo.
     * Lista vacía, si no se encuetra ninguna.
     */
    private List<Clase> obtenerClases(NodeList nodosDelTagPackageElement) {
        List<Clase> clases = new ArrayList<>();
        for (int i = 0; i < nodosDelTagPackageElement.getLength(); i++) {
            Node nodoPackageElement = nodosDelTagPackageElement.item(i);
            if (nodoPackageElement.getNodeType() == Node.ELEMENT_NODE) {
                Element elementoPackageElement = (Element) nodoPackageElement;
                if (!elementoPackageElement.getAttribute("xmi:type").equals("uml:Model")
                        && !elementoPackageElement.getAttribute("xmi:type").equals("uml:DataType")) {
                    Clase clase = new Clase();
                    clase.setId(elementoPackageElement.getAttribute("xmi:id"));
                    if (elementoPackageElement.getAttribute("isAbstract").equals("true")) {
                        clase.setAbstracta(true);
                    } else if (elementoPackageElement.getAttribute("xmi:type")
                            .equalsIgnoreCase("uml:Interface")) {
                        clase.setInterfaz(true);
                    }
                    clase.setNombre(elementoPackageElement.getAttribute("name"));
                    clase.setAtributos(obtenerAtributosDeClase(elementoPackageElement
                            .getElementsByTagName("ownedAttribute")));
                    clase.setMetodos(obtenerMetodosDeClase(elementoPackageElement
                            .getElementsByTagName("ownedOperation")));

                    clases.add(clase);
                }
            }
        }
        return clases;
    }

    /**
     * @param nodosDelTagOwnedAttribute Nodos del tag "ownedAttribute" que contiene la
     *                                  información de los atributos presentes dentro
     *                                  de cada clase.
     * @return Lista con todas los atributos encontrados de cada clase.
     * Lista vacía, si no se encuetra ninguno.
     */
    private List<Atributo> obtenerAtributosDeClase(NodeList nodosDelTagOwnedAttribute) {
        List<Atributo> atributos = new ArrayList<>();
        for (int i = 0; i < nodosDelTagOwnedAttribute.getLength(); i++) {
            Node nodoOwnedAttribute = nodosDelTagOwnedAttribute.item(i);
            Element elementoOwnedAttribute = (Element) nodoOwnedAttribute;
            if (nodoOwnedAttribute.getNodeType() == Node.ELEMENT_NODE) {
                Atributo atributo = new Atributo();
                atributo.setVisibilidad(elementoOwnedAttribute.getAttribute("visibility"));
                atributo.setNombre(elementoOwnedAttribute.getAttribute("name"));
                atributo.setEstatico(!"false".equals(elementoOwnedAttribute.getAttribute("isStatic")));
                atributos.add(atributo);
            }
        }
        return atributos;
    }

    /**
     * @param nodosDelTagOwnedOperation Nodos del tag "ownedOperation" que contienen la
     *                                  información de los métodos presentes dentro de
     *                                  cada clase.
     * @return Lista con todos los métodos encontrados de cada clase.
     * Lista vacía, si no se encuetra ninguno.
     */
    private List<Metodo> obtenerMetodosDeClase(NodeList nodosDelTagOwnedOperation) {
        List<Metodo> metodos = new ArrayList<>();
        for (int i = 0; i < nodosDelTagOwnedOperation.getLength(); i++) {
            Node nodoOwnedOperation = nodosDelTagOwnedOperation.item(i);
            Element elementoOwnedOperation = (Element) nodoOwnedOperation;
            if (nodoOwnedOperation.getNodeType() == Node.ELEMENT_NODE) {
                Metodo metodo = new Metodo();
                metodo.setVisibilidad(elementoOwnedOperation.getAttribute("visibility"));
                metodo.setNombre(elementoOwnedOperation.getAttribute("name"));
                metodo.setEstatico(!"false".equals(elementoOwnedOperation.getAttribute("isStatic")));
                metodo.setAbstracto(!"false".equals(elementoOwnedOperation.getAttribute("isAbstract")));
                obtenerParametrosDeMetodo(elementoOwnedOperation.getElementsByTagName("ownedParameter"), metodo);
                metodos.add(metodo);
            }
        }
        return metodos;
    }

    /**
     * @param nodosDelTagOwnedParameter  Nodos del tag "ownedParameter" que pertenecen a un tag "ownedOperation"
     *                                   y qué contienen la información de los parámetros del método.
     * @param metodoAAgregarleParametros Método al que se le agregarán los parámetros.
     */
    private void obtenerParametrosDeMetodo(NodeList nodosDelTagOwnedParameter, Metodo metodoAAgregarleParametros) {
        for (int i = 0; i < nodosDelTagOwnedParameter.getLength(); i++) {
            Node nodoOwnedParameter = nodosDelTagOwnedParameter.item(i);
            Element elementoOwnedParameter = (Element) nodoOwnedParameter;
            if (nodoOwnedParameter.getNodeType() == Node.ELEMENT_NODE) {
                metodoAAgregarleParametros.agregarParametro(elementoOwnedParameter.getAttribute("name"));
            }
        }
    }

    /**
     * @param nodosDelTagOwnedMember Nodos del tag "ownedMember" que contienen la información
     *                               de las asociaciones y las los ID de las clases a las
     *                               que afecta.
     * @return Lista de todas las asociaciones encontradas en el archivo.
     * Lista vacía, si no se encuentra ninguna.
     */
    private List<Asociacion> obtenerAsociaciones(NodeList nodosDelTagOwnedMember) {
        List<Asociacion> asociaciones = new ArrayList<>();
        for (int i = 0; i < nodosDelTagOwnedMember.getLength(); i++) {
            Node nodoOwnedMember = nodosDelTagOwnedMember.item(i);
            Element elementoOwnedMember = (Element) nodoOwnedMember;
            if (nodoOwnedMember.getNodeType() == Node.ELEMENT_NODE) {
                Asociacion asociacion = new Asociacion();
                NodeList nodosDelTagOwnedEnd = elementoOwnedMember.getElementsByTagName("ownedEnd");
                for (int j = 0; j < nodosDelTagOwnedEnd.getLength(); j++) {
                    Node nodoOwnedEnd = nodosDelTagOwnedEnd.item(j);
                    Element elementoOwnedEnd = (Element) nodoOwnedEnd;
                    if (nodoOwnedEnd.getNodeType() == Node.ELEMENT_NODE) {
                        if (j == 0) {
                            // Lado A de la asociación
                            asociacion.setIdOrigen(elementoOwnedEnd.getAttribute("type"));
                        } else {
                            // Lado B de la asociación
                            asociacion.setIdDestino(elementoOwnedEnd.getAttribute("type"));
                        }
                    }
                }
                asociaciones.add(asociacion);
            }
        }
        return asociaciones;
    }

    /**
     * @param id     ID de la clase a buscar.
     * @param clases Lista de todas las clases donde se buscará la clase deseada.
     * @return {@code Optional} con la clase encontrada.
     * <p>{@code Optional} vacío, si no se encuentra.
     */
    private Optional<Clase> buscarClase(String id, List<Clase> clases) {
        Optional<Clase> clase = Optional.empty();
        for (Clase c : clases) {
            if (id.equals(c.getId())) {
                clase = Optional.of(c);
                break;
            }
        }
        return clase;
    }

    /**
     * @param idClase    ID de la clase a reemplazar.
     * @param clases     Las clases encontradas en el archivo.
     * @param nuevaClase Misma clase pero modificada para admitir asociaciones.
     */
    private void reemplazarClaseAfectadaPorAsociacion(String idClase, List<Clase> clases, Clase nuevaClase) {
        for (int i = 0; i < clases.size(); i++) {
            if (idClase.equals(clases.get(i).getId())) {
                clases.set(i, nuevaClase);
                break;
            }
        }
    }

    /**
     * Añade los atributos correspondientes a las clases afectadas por las
     * asociaciones.
     *
     * @param asociaciones Las asociaciones encontradas en el archivo.
     * @param clases       Las clases encontradas en el archivo.
     */
    private void modificarClasesAfectadasPorAsociaciones(List<Asociacion> asociaciones, List<Clase> clases) {
        for (Asociacion a : asociaciones) {
            Optional<Clase> claseAfectadaA = buscarClase(a.getIdOrigen(), clases);
            Optional<Clase> claseAfectadaB = buscarClase(a.getIdDestino(), clases);
            if (claseAfectadaA.isPresent() && claseAfectadaB.isPresent()) {
                Atributo atributoInfoClaseA = new Atributo();
                atributoInfoClaseA.setNombre(claseAfectadaA.get().getNombre());
                atributoInfoClaseA.setVisibilidad("private");
                atributoInfoClaseA.setEstatico(false);
                atributoInfoClaseA.setEsReferenciaAUnaClase(true);

                Atributo atributoInfoClaseB = new Atributo();
                atributoInfoClaseB.setNombre(claseAfectadaB.get().getNombre());
                atributoInfoClaseB.setVisibilidad("private");
                atributoInfoClaseB.setEstatico(false);
                atributoInfoClaseB.setEsReferenciaAUnaClase(true);

                claseAfectadaA.get().agregarAtributo(atributoInfoClaseB);
                claseAfectadaB.get().agregarAtributo(atributoInfoClaseA);

                reemplazarClaseAfectadaPorAsociacion(a.getIdOrigen(), clases, claseAfectadaA.get());
                reemplazarClaseAfectadaPorAsociacion(a.getIdDestino(), clases, claseAfectadaB.get());
            }
        }
    }
}
