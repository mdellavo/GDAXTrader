package org.quuux.gdax.model;


import java.math.BigDecimal;
import java.util.Date;

public class Deposit {
    public String id;
    public Date payout_at;
    public BigDecimal amount;
    public String currency;
    public String payment_method_id;
    public String coinbase_account_id;

    public static Deposit newPaymentMethod(PaymentMethod method, BigDecimal amount) {
        Deposit rv = new Deposit();
        rv.payment_method_id = method.id;
        rv.amount = amount;
        rv.currency = method.currency;
        return rv;
    }

    public static Deposit newCoinbase(final CoinbaseAccount source, final BigDecimal amount) {
        Deposit rv = new Deposit();
        rv.coinbase_account_id = source.id;
        rv.amount = amount;
        rv.currency = source.currency;
        return rv;
    }
}
