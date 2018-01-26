package cl.idatum.idatumterreno.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Crea el documento XML para establecer la conexion con el webservice
 *
 * @author Jesus Cañizales <jcanizales@siigsa.cl>
 * @version 2
 * @package cl.idatum.idatumterreno Proyecto IdatumTerreno
 * @link http://www.siigsa.cl
 * @copyright SIIGSA, Propiedad Intelectual y Derechos Patrimoniales de Software y Base de Datos i-datum. Registro Propiedad Intelectual Nº 211.351 y 211.352 respectivamente, con fecha 22 de noviembre del 2011
 * @since 25/10/2017
 */
public class XmlDoc {

    Document doc;

    /**
     * Constructor de la clase XmlDoc
     *
     * @throws ParserConfigurationException
     */
    public XmlDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        this.doc = docBuilder.newDocument();
    }

    /**
     * Se encarga de generar la estructura XML que hace la comunicación con el WebService.
     *
     * @param texto_libre1            Texto usado para buscar contenidos.
     * @param accion1                 Accion a ejecutar.
     * @param usuarioText             Nombre de usuario.
     * @param claveText               Clave de usuario.
     * @param id_contenido            Id de contenido de la ficha.
     * @param id_relacion             Id de relacion UG.
     * @param latitudSend             Latitud que se enviara para georeferenciar un contenido.
     * @param longitudSend            Longitud que se enviara para georeferenciar un contenido.
     * @param id_tipo_cont            Id de tipo de contenido de la ficha.
     * @param tipo_rel                Tipo de relacion UG de la ficha.
     * @param nombre_contenido        Nombre de la ficha de contenido.
     * @param descripcion_contenido   Descripcion de la ficha de contenido.
     * @param codigo_insti            Codigo institucional de la ficha.
     * @param observacion1
     * @param listaAtributosDinamicos @return Retorna la estructura Xml.
     */
    public StringWriter generateXML(String texto_libre1,
                                    String accion1,
                                    String usuarioText,
                                    String claveText,
                                    String id_contenido,
                                    String id_relacion,
                                    Double latitudSend,
                                    Double longitudSend,
                                    String id_tipo_cont,
                                    String tipo_rel,
                                    String nombre_contenido,
                                    String descripcion_contenido,
                                    String codigo_insti,
                                    String observacion1,
                                    ArrayList<AtributosDinamicos> listaAtributosDinamicos) {
        StringWriter writer = null;


        try {
            Element rootElement = doc.createElement("idatum");
            doc.appendChild(rootElement);


            // staff elements
            Element ent_ext_cod = doc.createElement("ent_ext_cod");
            ent_ext_cod.appendChild(doc.createTextNode(""));
            rootElement.appendChild(ent_ext_cod);

            Element accion = doc.createElement("accion");
            accion.appendChild(doc.createTextNode(accion1));
            rootElement.appendChild(accion);

            Element interno = doc.createElement("interno");
            interno.appendChild(doc.createTextNode("3"));
            rootElement.appendChild(interno);

            Element ejecutar = doc.createElement("ejecutar");
            rootElement.appendChild(ejecutar);

            Element llamada = doc.createElement("llamada");
            ejecutar.appendChild(llamada);

            // set attribute to staff element
            Attr attr = doc.createAttribute("clase");
            attr.setValue("");
            llamada.setAttributeNode(attr);

            llamada.setAttribute("funcion", "");
            llamada.setAttribute("modaccion", "");
            llamada.setAttribute("accion", "");
            llamada.setAttribute("modulo", "");

            Element parametros = doc.createElement("parametros");
            llamada.appendChild(parametros);

            Element param = doc.createElement("param");
            param.appendChild(doc.createTextNode(""));
            parametros.appendChild(param);

            param.setAttribute("a1", "");
            param.setAttribute("a2", "");

            Element usuario = doc.createElement("usuario");
            usuario.appendChild(doc.createTextNode(usuarioText));
            rootElement.appendChild(usuario);

            Element contrasena = doc.createElement("contrasena");
            contrasena.appendChild(doc.createTextNode(claveText));
            rootElement.appendChild(contrasena);

            Element contenido = doc.createElement("contenido");
            rootElement.appendChild(contenido);
            contenido.setAttribute("id", id_contenido);
            contenido.setAttribute("tipo", id_tipo_cont);

            Element nombre = doc.createElement("nombre");
            nombre.appendChild(doc.createTextNode(nombre_contenido));
            contenido.appendChild(nombre);
            Element descripcion = doc.createElement("descripcion");
            descripcion.appendChild(doc.createTextNode(descripcion_contenido));
            contenido.appendChild(descripcion);
            Element observacion = doc.createElement("observacion");
            observacion.appendChild(doc.createTextNode(observacion1));
            contenido.appendChild(observacion);
            Element codigo_inst = doc.createElement("codigo_inst");
            codigo_inst.appendChild(doc.createTextNode(codigo_insti));
            contenido.appendChild(codigo_inst);
            Element estado = doc.createElement("estado");
            estado.appendChild(doc.createTextNode(""));
            contenido.appendChild(estado);
            Element publico = doc.createElement("publico");
            publico.appendChild(doc.createTextNode(""));
            contenido.appendChild(publico);
            Element destacado = doc.createElement("destacado");
            destacado.appendChild(doc.createTextNode(""));
            contenido.appendChild(destacado);
            Element control_estado = doc.createElement("control_estado");
            control_estado.appendChild(doc.createTextNode(""));
            contenido.appendChild(control_estado);
            Element archivo = doc.createElement("archivo");
            archivo.appendChild(doc.createTextNode(""));
            contenido.appendChild(archivo);
            Element texto_libre = doc.createElement("texto_libre");
            texto_libre.appendChild(doc.createTextNode(texto_libre1));
            contenido.appendChild(texto_libre);
            Element georeferencia = doc.createElement("georeferencia");
            georeferencia.appendChild(doc.createTextNode(""));
            contenido.appendChild(georeferencia);

            //ATRIBUTOS DINAMICOS
            Element atributos_dinamicos = doc.createElement("atributos_dinamicos");
            contenido.appendChild(atributos_dinamicos);
            for (int i = 0; i < listaAtributosDinamicos.size(); i++) {
                AtributosDinamicos atributo = listaAtributosDinamicos.get(i);
                Element atr = doc.createElement("atr");
                atributos_dinamicos.appendChild(atr);
                atr.setAttribute("id", String.valueOf(atributo.getId()));
                atr.setAttribute("tipo", String.valueOf(atributo.getTipo()));
                atr.setAttribute("fecha", "");
                atr.setAttribute("ultima_fecha", "");

                if (atributo.getMultiple() == true) {
                    List<AtributosDinamicos.AtributosConceptualMultiple> atributosConceptualMultipleList = atributo.getList();

                    for (int j = 0; j <atributosConceptualMultipleList.size(); j++) {
                        AtributosDinamicos.AtributosConceptualMultiple atributoConceptualMultiple = atributosConceptualMultipleList.get(j);

                        Element atributos = doc.createElement("atributos");
                        atr.appendChild(atributos);

                        Element valor = doc.createElement("valor");
                        valor.appendChild(doc.createTextNode(atributoConceptualMultiple.getAtributoValor()));
                        atributos.appendChild(valor);
                        Element concepto = doc.createElement("concepto");
                        concepto.appendChild(doc.createTextNode(""));
                        atributos.appendChild(concepto);
                        Element fecha = doc.createElement("fecha");
                        fecha.appendChild(doc.createTextNode(atributo.getFecha()));
                        atributos.appendChild(fecha);
                    }
                }else {
                    Element atributos = doc.createElement("atributos");
                    atr.appendChild(atributos);

                    Element valor = doc.createElement("valor");
                    valor.appendChild(doc.createTextNode(atributo.getValor()));
                    atributos.appendChild(valor);
                    Element concepto = doc.createElement("concepto");
                    concepto.appendChild(doc.createTextNode(""));
                    atributos.appendChild(concepto);
                    Element fecha = doc.createElement("fecha");
                    fecha.appendChild(doc.createTextNode(atributo.getFecha()));
                    atributos.appendChild(fecha);
                }
            }

            Element relaciones = doc.createElement("relaciones");
            contenido.appendChild(relaciones);

            Element rel = doc.createElement("rel");
            relaciones.appendChild(rel);
            rel.setAttribute("id", id_relacion);
            rel.setAttribute("tipo", tipo_rel);
            rel.setAttribute("multiple", "");
            rel.setAttribute("jerarquia", "");
            rel.setAttribute("campo", "0");
            rel.setAttribute("valor", "");
            Element registros = doc.createElement("registros");
            rel.appendChild(registros);

            Element id = doc.createElement("id");
            id.appendChild(doc.createTextNode(""));
            registros.appendChild(id);
            nombre = doc.createElement("nombre");
            nombre.appendChild(doc.createTextNode(""));
            registros.appendChild(nombre);
            descripcion = doc.createElement("descripcion");
            descripcion.appendChild(doc.createTextNode(""));
            registros.appendChild(descripcion);
            Element cod_inst = doc.createElement("cod_inst");
            cod_inst.appendChild(doc.createTextNode(""));
            registros.appendChild(cod_inst);
            Element coordenadas = doc.createElement("coordenadas");
            coordenadas.appendChild(doc.createTextNode(longitudSend + " " + latitudSend));
            registros.appendChild(coordenadas);
            coordenadas.setAttribute("srid", "4326");

            Element respuesta = doc.createElement("respuesta");
            contenido.appendChild(respuesta);

            Element mensaje = doc.createElement("mensaje");
            mensaje.appendChild(doc.createTextNode(""));
            respuesta.appendChild(mensaje);
            estado = doc.createElement("estado");
            estado.appendChild(doc.createTextNode(""));
            respuesta.appendChild(estado);
            archivo = doc.createElement("archivo");
            archivo.appendChild(doc.createTextNode(""));
            respuesta.appendChild(archivo);

            //write the content into xml fi
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();

        }


        return writer;
    }


}
