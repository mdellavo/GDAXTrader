package org.quuux.gdax.model;

import java.math.BigDecimal;
import java.util.Date;

public class Fill {
    public enum Liquidity {M, T}

    public long trade_id;
    public String product_id;
    public BigDecimal price;
    public BigDecimal size;
    public String order_id;
    public Date created_at;
    public Liquidity liquidity;
    public BigDecimal fee;
    public boolean settled;
    public Side side;
}
