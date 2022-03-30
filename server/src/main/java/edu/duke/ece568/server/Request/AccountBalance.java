package edu.duke.ece568.server.Request;


import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "account")
public class AccountBalance {
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String balance;
}
