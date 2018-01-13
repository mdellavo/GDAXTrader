package org.quuux.gdax.model;


import java.io.Serializable;
import java.math.BigDecimal;

public class Account implements Serializable {
    public String id;
    public String currency;
    public BigDecimal balance;
    public BigDecimal available;
    public BigDecimal hold;
    public String profile_id;
}
