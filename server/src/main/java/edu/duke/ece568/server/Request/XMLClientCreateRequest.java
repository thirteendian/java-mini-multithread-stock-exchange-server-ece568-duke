package edu.duke.ece568.server.Request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

//@XmlType(propOrder = {""})
@XmlRootElement(name = "create")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLClientCreateRequest implements XMLClientRequest {
    @XmlElement(name = "account")
    private List<AccountBalance> accountBalance;

    public List<AccountBalance> getAccountBalance() {
        return accountBalance;
    }
    public void setAccountBalance(List<AccountBalance> accountBalance) {
        this.accountBalance = accountBalance;
    }
    @XmlElement(name = "symbol")
    private List<Symbol> symbols;

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbols) {
        this.symbols = symbols;
    }


}
