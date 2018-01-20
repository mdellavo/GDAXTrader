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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Account account = (Account) o;

        return id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
