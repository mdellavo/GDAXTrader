package org.quuux.gdax.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class Order {
    public enum Type {market, limit, stop}
    public enum Side {buy, sell}
    public enum Status {pending, open, active, done, settled, canceled}

    public String client_oid;

    public String id;
    public Type type;
    public Side side;
    public BigDecimal size;
    public String product_id;
    public BigDecimal price;
    public Date created_at;
    public Status status;

    public static Order newOrder() {
        Order order = new Order();
        order.client_oid = UUID.randomUUID().toString();
        return order;
    }

    public static Order newMarketOrder(Product product, Side side, BigDecimal size) {
        Order order = newOrder();

        order.type = Type.market;
        order.product_id = product.id;
        order.side = side;
        order.size = size;

        return order;
    }
}