package edu.duke.ece568.server.Request;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name="transactions")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLClientTransactionRequest implements XMLClientRequest{
    @XmlAttribute
    private String id;
    @XmlElement(name = "order")
    private List<Order> orders;
    @XmlElement(name = "query")
    private List<Query> queries;
    @XmlElement(name = "cancel")
    private  List<Cancel> cancels;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public List<Cancel> getCancels() {
        return cancels;
    }

    public void setCancels(List<Cancel> cancels) {
        this.cancels = cancels;
    }
}
