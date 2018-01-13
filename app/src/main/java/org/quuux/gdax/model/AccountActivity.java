package org.quuux.gdax.model;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class AccountActivity implements Serializable {
    public Date created_at;
    public String id;
    public String type;
    public String trade_id;
    public String product_id;
    public BigDecimal amount;
    public BigDecimal balance;
}
