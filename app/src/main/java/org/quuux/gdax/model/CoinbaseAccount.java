package org.quuux.gdax.model;

import java.math.BigDecimal;

public class CoinbaseAccount {
    public enum Type {fiat, wallet}

    public String id;
    public String name;
    public BigDecimal balance;
    public String currency;
    public Type type;
    public boolean primary;
    public boolean active;
}
