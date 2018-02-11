package org.quuux.gdax.model;

import java.math.BigDecimal;
import java.util.Date;

public class ProductStat {
    public BigDecimal open, low, high, volume, last;
    public Date created_at = new Date();
}
