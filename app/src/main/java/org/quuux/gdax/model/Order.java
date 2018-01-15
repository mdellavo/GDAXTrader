package org.quuux.gdax.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Order {
    public enum Type {market, limit, stop}
    public enum Side {buy, sell}

    public String client_oid;

    public String id;
    public Type type;
    public Side side;
    public BigDecimal size;
    public String product_id;

    public BigDecimal price;  // limit orders

    public static Order newOrder() {
        Order order = new Order();
        order.client_oid = UUID.randomUUID().toString();
        return order;
    }

    public static Order newMarketOrder(String product_id, Side side, BigDecimal size) {
        Order order = newOrder();

        order.type = Type.market;
        order.product_id = product_id;
        order.side = side;
        order.size = size;

        return order;
    }
}
