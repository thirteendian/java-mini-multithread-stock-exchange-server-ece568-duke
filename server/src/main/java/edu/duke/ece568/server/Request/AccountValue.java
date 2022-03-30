package edu.duke.ece568.server.Request;


import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "account")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountValue {
    @XmlAttribute
    private String id;
    @XmlValue
    protected String accountNumber;
}
