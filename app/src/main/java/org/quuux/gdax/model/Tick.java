package org.quuux.gdax.model;


import java.math.BigDecimal;
import java.util.Date;

public class Tick {
    public long trade_id;
    public BigDecimal price;
    public BigDecimal size;
    public BigDecimal bid;
    public BigDecimal ask;
    public BigDecimal volume;
    public Date time;
}
