package org.quuux.gdax.model;

import java.math.BigDecimal;

public class CoinbaseAccount {
    public enum Type {fiat, wallet}

    String id;
    String name;
    BigDecimal balance;
    String currency;
    Type type;
    boolean primary;
    boolean active;
}
