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

    public static final DecimalFormat SHORT_FORMAT = new DecimalFormat("#0.00######");
    public static final DecimalFormat LONG_FORMAT = new DecimalFormat("#0.00###############");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public static String currencyFormat(BigDecimal n) {
        return NumberFormat.getCurrencyInstance().format(n);
    }

    public static String shortFormat(BigDecimal n) {
        if (n == null)
            return "";
        return SHORT_FORMAT.format(n);
    }

    public static String longFormat(BigDecimal n) {
        if (n == null)
            return "";
        return LONG_FORMAT.format(n);
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
