package eu.dariolucia.reatmetric.driver.socket.configuration;

import eu.dariolucia.reatmetric.api.value.ValueTypeEnum;
import eu.dariolucia.reatmetric.api.value.ValueUtil;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class SymbolTypeFormat {

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute(required = true)
    private ValueTypeEnum type;

    @XmlAttribute
    private RadixEnum radix = RadixEnum.DEC;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ValueTypeEnum getType() {
        return type;
    }

    public void setType(ValueTypeEnum type) {
        this.type = type;
    }

    public RadixEnum getRadix() {
        return radix;
    }

    public void setRadix(RadixEnum radix) {
        this.radix = radix;
    }

    public Object parse(String valueString) {
        if(type == ValueTypeEnum.ENUMERATED) {
            // Check the radix
            if (radix == RadixEnum.DEC) {
                return ValueUtil.parse(type, valueString);
            } else {
                return Integer.parseInt(valueString, radix.getRadix());
            }
        } else if(type == ValueTypeEnum.UNSIGNED_INTEGER || type == ValueTypeEnum.SIGNED_INTEGER) {
            // Check the radix
            if (radix == RadixEnum.DEC) {
                return ValueUtil.parse(type, valueString);
            } else {
                return Long.parseLong(valueString, radix.getRadix());
            }
        } else {
            return ValueUtil.parse(type, valueString);
        }
    }

    public String dump(Object value) {
        if(type == ValueTypeEnum.ENUMERATED) {
            // Check the radix
            if (radix == RadixEnum.DEC) {
                return ValueUtil.toString(type, value);
            } else {
                return Integer.toString((Integer) value, radix.getRadix());
            }
        } else if(type == ValueTypeEnum.UNSIGNED_INTEGER || type == ValueTypeEnum.SIGNED_INTEGER) {
            // Check the radix
            if (radix == RadixEnum.DEC) {
                return ValueUtil.toString(type, value);
            } else {
                return Long.toString((Long)value, radix.getRadix());
            }
        } else {
            return ValueUtil.toString(type, value);
        }
    }
}