package org.quuux.gdax.net;

import org.quuux.gdax.model.PaymentMethod;

public class PaymentMethodsCursor extends Cursor<PaymentMethod> {
    @Override
    public Class<PaymentMethod[]> getPageClass() {
        return PaymentMethod[].class;
    }

    @Override
    public String getEndpoint() {
        return API.GDAX_PAYMENT_METHODS_ENDPOINT;
    }
}
