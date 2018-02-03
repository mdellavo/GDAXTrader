package org.quuux.gdax.model;

import java.math.BigDecimal;

public class Withdraw {
    public String id;
    public BigDecimal amount;
    public String currency;
    public String payment_method_id;
    public String coinbase_account_id;
    public String crypto_address;

    public static Withdraw newPaymentMethod(PaymentMethod method, BigDecimal amount) {
        Withdraw rv = new Withdraw();
        rv.payment_method_id = method.id;
        rv.amount = amount;
        rv.currency = method.currency;
        return rv;
    }

    public static Withdraw newCoinbase(final CoinbaseAccount source, final BigDecimal amount) {
        Withdraw rv = new Withdraw();
        rv.coinbase_account_id = source.id;
        rv.amount = amount;
        rv.currency = source.currency;
        return rv;
    }}
