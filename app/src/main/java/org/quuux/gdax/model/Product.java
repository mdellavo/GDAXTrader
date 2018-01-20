package org.quuux.gdax.model;


import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {
    public String id;
    public String base_currency;
    public String quote_currency;
    public BigDecimal base_min_size;
    public BigDecimal base_max_size;
    public BigDecimal quote_increment;

    public String getName() {
        return String.format("%s/%s", base_currency, quote_currency);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Product product = (Product) o;

        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
