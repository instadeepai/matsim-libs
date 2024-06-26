//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.19 at 03:18:45 PM MESZ 
//


package playground.vsp.gexf;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for attribute-content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="attribute-content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.gexf.net/1.2draft}default"/>
 *         &lt;element ref="{http://www.gexf.net/1.2draft}options"/>
 *       &lt;/choice>
 *       &lt;attribute name="id" use="required" type="{http://www.gexf.net/1.2draft}id-type" />
 *       &lt;attribute name="title" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" use="required" type="{http://www.gexf.net/1.2draft}attrtype-type" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attribute-content", propOrder = {
    "defaultOrOptions"
})
public class XMLAttributeContent {

    @XmlElementRefs({
        @XmlElementRef(name = "default", namespace = "http://www.gexf.net/1.2draft", type = JAXBElement.class),
        @XmlElementRef(name = "options", namespace = "http://www.gexf.net/1.2draft", type = JAXBElement.class)
    })
    protected List<JAXBElement<String>> defaultOrOptions;
    @XmlAttribute(required = true)
    protected String id;
    @XmlAttribute(required = true)
    protected String title;
    @XmlAttribute(required = true)
    protected XMLAttrtypeType type;

    /**
     * Gets the value of the defaultOrOptions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the defaultOrOptions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefaultOrOptions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * 
     */
    public List<JAXBElement<String>> getDefaultOrOptions() {
        if (defaultOrOptions == null) {
            defaultOrOptions = new ArrayList<JAXBElement<String>>();
        }
        return this.defaultOrOptions;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link XMLAttrtypeType }
     *     
     */
    public XMLAttrtypeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLAttrtypeType }
     *     
     */
    public void setType(XMLAttrtypeType value) {
        this.type = value;
    }

}
