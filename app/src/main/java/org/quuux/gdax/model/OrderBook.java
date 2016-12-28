package org.quuux.gdax.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class OrderBook {
    public enum Side {buy, sell}

    public static class OrderBookBin {
        public BigDecimal price;
        public BigDecimal size = new BigDecimal(0);
        public long num_orders;
        public OrderBookBin(BigDecimal price) {
            this.price = price;
        }
    }

    private HashMap<String, Order> orders = new HashMap<>();
    private TreeMap<BigDecimal, OrderBookBin> buyHist = new TreeMap<>();
    private TreeMap<BigDecimal, OrderBookBin> sellHist = new TreeMap<>();

    private Map<BigDecimal, OrderBookBin> getHist(Side side) {
        Map<BigDecimal, OrderBookBin> rv = null;
        if (side == Side.buy)
            rv = buyHist;
        else if (side == Side.sell)
            rv = sellHist;
        return rv;
    }

    public Set<BigDecimal> prices(Side side) {
        return getHist(side).keySet();
    }

    public int numOrders(Side side) {
        return getHist(side).size();
    }

    public OrderBookBin getBin(Side side, BigDecimal price) {
        return getHist(side).get(price);
    }

    public void incDepth(Side side, BigDecimal price, BigDecimal size) {
        Map<BigDecimal, OrderBookBin> hist = getHist(side);

        OrderBookBin bin = hist.get(price);
        if (bin == null) {
            bin = new OrderBookBin(price);
            hist.put(price, bin);
        }

        bin.size = bin.size.add(size);
        bin.num_orders += 1;
    }

    public void addOrder(Order order) {
        orders.put(order.order_id, order);
    }

    public void removeOrder(String order_id) {
        orders.remove(order_id);
    }

    public void decDepth(Side side, BigDecimal price, BigDecimal size) {
        Map<BigDecimal, OrderBookBin> hist = getHist(side);

        OrderBookBin bin = hist.get(price);
        if (bin != null) {
            bin.size = bin.size.subtract(size);
            bin.num_orders -= 1;
        }
    }
}
