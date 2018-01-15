package org.quuux.gdax.model;


import java.math.BigDecimal;

public class Product {
    String id;
    String base_currency;
    String quote_currency;
    BigDecimal base_min_size;
    BigDecimal base_max_size;
    BigDecimal quote_increment;

    public String getName() {
        return String.format("%s/%s", base_currency, quote_currency);
    }

}
