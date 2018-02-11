package org.quuux.gdax;


import android.text.TextUtils;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    public static final DecimalFormat SHORT_FORMAT = new DecimalFormat("###,###,##0.00######");
    public static final DecimalFormat LONG_FORMAT = new DecimalFormat("###,###,##0.00###############");
    public static final DecimalFormat PERCENTAGE = new DecimalFormat("##0.00%");
    public static final DecimalFormat INT_FORMAT = new DecimalFormat("###,###,##0");
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public static String format(BigDecimal n, NumberFormat format, String defaultValue) {
        if (n == null)
            return defaultValue;
        return format.format(n);
    }

    public static String format(BigDecimal n, NumberFormat format) {
        return format(n, format, "");
    }

    public static String currencyFormat(BigDecimal n) {
        return format(n, CURRENCY_FORMAT);
    }

    public static String shortFormat(BigDecimal n) {
        return format(n, SHORT_FORMAT);
    }

    public static String longFormat(BigDecimal n) {
        return format(n, LONG_FORMAT);
    }

    public static String percentageFormat(BigDecimal n) {
        return format(n, PERCENTAGE);
    }

    public static String intFormat(final BigDecimal n) {
        return format(n,  INT_FORMAT);
    }

    public static String dateFormat(Date d) {
        return DATE_FORMAT.format(d);
    }

    public static BigDecimal cleanDecimalInput(EditText v, boolean validate) {
        String value = v.getText().toString();
        if (TextUtils.isEmpty(value)) {
            if (validate)
                v.setError(v.getContext().getString(R.string.error_amount_not_valid));
            return null;
        }

        BigDecimal amount = null;

        try {
            amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                if (validate)
                    v.setError(v.getContext().getString(R.string.error_amount_not_greater_than_zero));
                amount = null;
            }
        } catch (NumberFormatException e) {
            if (validate)
                v.setError(v.getContext().getString(R.string.error_amount_not_valid));
        }

        return amount;
    }

}
