package org.quuux.gdax;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Util {
    public static String currencyFormat(BigDecimal n) {
        return NumberFormat.getCurrencyInstance().format(n);
    }

    public static String decimalFormat(BigDecimal n) {
        return new DecimalFormat("#0.##").format(n);
    }
}
