package edu.duke.ece568.server.Request;


import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "symbol")
@XmlAccessorType(XmlAccessType.FIELD)
public class Symbol {
    @XmlAttribute
    private String sym;
    @XmlElement(name = "account")
    private AccountValue accountValue;

}
