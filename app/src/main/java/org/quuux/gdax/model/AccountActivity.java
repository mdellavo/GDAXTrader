package org.quuux.gdax.model;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class AccountActivity implements Serializable {
    public String id;
    public Date created_at;
    public String type;
    public String trade_id;
    public String product_id;
    public BigDecimal amount;
    public BigDecimal balance;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AccountActivity that = (AccountActivity) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
