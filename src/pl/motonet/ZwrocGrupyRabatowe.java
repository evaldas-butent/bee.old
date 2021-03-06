package pl.motonet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fiks" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nr_kontrahenta_motonet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fiks",
    "nrKontrahentaMotonet"
})
@XmlRootElement(name = "ZwrocGrupyRabatowe")
public class ZwrocGrupyRabatowe {

  protected String fiks;
  @XmlElement(name = "nr_kontrahenta_motonet")
  protected String nrKontrahentaMotonet;

  /**
   * Gets the value of the fiks property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getFiks() {
    return fiks;
  }

  /**
   * Gets the value of the nrKontrahentaMotonet property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getNrKontrahentaMotonet() {
    return nrKontrahentaMotonet;
  }

  /**
   * Sets the value of the fiks property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setFiks(String value) {
    this.fiks = value;
  }

  /**
   * Sets the value of the nrKontrahentaMotonet property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setNrKontrahentaMotonet(String value) {
    this.nrKontrahentaMotonet = value;
  }

}
