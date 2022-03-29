package edu.duke.ece568.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

//@XmlType(propOrder = {""})
@XmlRootElement(name = "create")
public class XMLClientCreateRequest {
List symbols;

@XmlElement(name = "symbol")
public void setSymbols(List symbols)
{
    this.symbols = symbols;
}

}
