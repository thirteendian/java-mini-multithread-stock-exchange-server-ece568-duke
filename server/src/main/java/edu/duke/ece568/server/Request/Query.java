package edu.duke.ece568.server.Request;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "query")
public class Query {
    @XmlAttribute(name = "id")
    private String queryId;

    public String getId() {
        return queryId;
    }

    public void setId(String id) {
        this.queryId = id;
    }
}
