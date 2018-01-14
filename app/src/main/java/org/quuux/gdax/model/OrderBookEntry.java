package org.quuux.gdax.model;

import java.math.BigDecimal;

public class OrderBookEntry {
    public final String order_id;
    public final BigDecimal size;
    public final BigDecimal price;

    public OrderBookEntry(final String order_id, final BigDecimal price, final BigDecimal size) {
        this.order_id = order_id;
        this.size = size;
        this.price = price;
    }

    public OrderBookEntry(final String order_id, final String price, final String size) {
        this(order_id, new BigDecimal(price), new BigDecimal(size));
    }
}
