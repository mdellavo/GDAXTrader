package org.quuux.gdax;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    public static final DecimalFormat LONG_FORMAT = new DecimalFormat("#0.00000000");
    public static final DecimalFormat SHORT_FORMAT = new DecimalFormat("#0.00");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public static String currencyFormat(BigDecimal n) {
        return NumberFormat.getCurrencyInstance().format(n);
    }

    public static String shortDecimalFormat(BigDecimal n) {
        return SHORT_FORMAT.format(n);
    }

    public static String longDecimalFormat(BigDecimal n) {
        return LONG_FORMAT.format(n);
    }

    public static String dateFormat(Date d) {
        return DATE_FORMAT.format(d);
    }

}
