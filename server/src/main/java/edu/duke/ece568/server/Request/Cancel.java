package edu.duke.ece568.server.Request;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cancel")
public class Cancel {
    @XmlAttribute(name = "id")
    private String cancelId;

    public String getId() {
        return cancelId;
    }

    public void setId(String cancelId) {
        this.cancelId = cancelId;
    }
}
