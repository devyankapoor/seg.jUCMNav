//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.9-03/31/2009 04:14 PM(snajper)-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.07.19 at 07:21:12 PM EDT 
//


package seg.jUCMNav.importexport.z151.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IntentionalElementType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="IntentionalElementType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Softgoal"/>
 *     &lt;enumeration value="Goal"/>
 *     &lt;enumeration value="Task"/>
 *     &lt;enumeration value="Resource"/>
 *     &lt;enumeration value="Belief"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "IntentionalElementType")
@XmlEnum
public enum IntentionalElementType {

    @XmlEnumValue("Softgoal")
    SOFTGOAL("Softgoal"),
    @XmlEnumValue("Goal")
    GOAL("Goal"),
    @XmlEnumValue("Task")
    TASK("Task"),
    @XmlEnumValue("Resource")
    RESOURCE("Resource"),
    @XmlEnumValue("Belief")
    BELIEF("Belief");
    private final String value;

    IntentionalElementType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IntentionalElementType fromValue(String v) {
        for (IntentionalElementType c: IntentionalElementType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}