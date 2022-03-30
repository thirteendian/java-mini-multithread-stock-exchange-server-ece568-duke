package edu.duke.ece568.server.Request;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "order")
@XmlAccessorType(XmlAccessType.FIELD)
public class Order {
    @XmlAttribute
    private String sym;
    @XmlAttribute
    private String amount;
    @XmlAttribute
    private String limit;

    public String getSym() {
        return sym;
    }

    public String getAmount() {
        return amount;
    }

    public String getLimit() {
        return limit;
    }

    public void setSym(String sym) {
        this.sym = sym;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
}
