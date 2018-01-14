package org.quuux.gdax.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.NavigableMap;
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

    private long sequence;
    private HashMap<String, OrderBookEntry> orders = new HashMap<>();
    private TreeMap<BigDecimal, OrderBookBin> buyHist = new TreeMap<>();
    private TreeMap<BigDecimal, OrderBookBin> sellHist = new TreeMap<>();

    public void setSequence(final long sequence) {
        this.sequence = sequence;
    }

    public long getSequence() {
        return sequence;
    }

    private TreeMap<BigDecimal, OrderBookBin> getHist(Side side) {
        TreeMap<BigDecimal, OrderBookBin> rv = null;
        if (side == Side.buy)
            rv = buyHist;
        else if (side == Side.sell)
            rv = sellHist;
        return rv;
    }

    public Set<BigDecimal> prices(Side side) {
        return getHist(side).keySet();
    }

    public int numPrices(Side side) {
        return getHist(side).size();
    }

    public BigDecimal roundPrice(BigDecimal price) {
        return  price.setScale(2, RoundingMode.CEILING);
    }

    public OrderBookBin getBin(Side side, BigDecimal price) {
        return getHist(side).get(roundPrice(price));
    }

    public void incDepth(Side side, BigDecimal price, BigDecimal size) {
        OrderBookBin bin = getBin(side, price);
        if (bin == null) {
            bin = new OrderBookBin(price);
            getHist(side).put(roundPrice(price), bin);
        }

        bin.size = bin.size.add(size);
        bin.num_orders += 1;
    }

    public void addOrder(OrderBookEntry order) {
        orders.put(order.order_id, order);
    }

    public void removeOrder(String order_id) {
        orders.remove(order_id);
    }

    public void decDepth(Side side, BigDecimal price, BigDecimal size) {
        OrderBookBin bin = getBin(side, price);
        if (bin != null) {
            bin.size = bin.size.subtract(size);
            bin.num_orders -= 1;
            if (bin.size.intValue() <= 0 || bin.num_orders <= 0) {
                getHist(side).remove(price);
            }
        }
    }

    public BigDecimal getMinPrice(Side side) {
        TreeMap<BigDecimal, OrderBookBin> hist = getHist(side);
        return hist.size() > 0 ? hist.firstKey() : null;
    }

    public BigDecimal getMinAsk() {
        return getMinPrice(Side.sell);
    }

    public BigDecimal getMaxPrice(Side side) {
        TreeMap<BigDecimal, OrderBookBin> hist = getHist(side);
        return hist.size() > 0 ? hist.lastKey() : null;
    }

    public BigDecimal getMaxBid() {
        return getMaxPrice(Side.buy);
    }

    public BigDecimal getMaxSizeInRange(Side side, BigDecimal lower, BigDecimal upper) {
        BigDecimal rv = BigDecimal.valueOf(-1);
        for (OrderBookBin bin : getHist(side).subMap(roundPrice(lower), true, roundPrice(upper), true).values()) {
            rv = rv.max(bin.size);
        }
        return rv;
    }

    public BigDecimal getSpread() {
        return getMinAsk().subtract(getMaxBid());
    }

    public BigDecimal getMidpointPrice() {
        BigDecimal maxBid = getMaxBid();
        BigDecimal spread = getSpread();
        return maxBid.add(spread.divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP));
    }

    public OrderBookBin sumRange(Side side, BigDecimal a, BigDecimal b) {
        BigDecimal mid = a.add(b).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP);
        OrderBookBin rv = new OrderBookBin(mid);

        NavigableMap<BigDecimal, OrderBookBin> submap = getHist(side).subMap(roundPrice(a), true, roundPrice(b), true);
        for (BigDecimal key : submap.keySet()) {
            OrderBookBin bin = submap.get(key);
            rv.size = rv.size.add(bin.size);
            rv.num_orders += bin.num_orders;
        }

        return rv;
    }
}