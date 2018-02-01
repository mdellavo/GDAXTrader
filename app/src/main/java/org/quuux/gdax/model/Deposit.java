package org.quuux.gdax.model;


import java.math.BigDecimal;
import java.util.Date;

public class Deposit {
    String id;
    Date payout_at;

    BigDecimal amount;
    String currency;
    String payment_method_id;

    public static Deposit newPaymentMethod(PaymentMethod method, BigDecimal amount) {
        Deposit rv = new Deposit();
        rv.payment_method_id = method.id;
        rv.amount = amount;
        rv.currency = method.currency;
        return rv;
    }
}
