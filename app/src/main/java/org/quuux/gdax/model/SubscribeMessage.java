package org.quuux.gdax.model;


public class SubscribeMessage {
    final String type = "subscribe";
    final String product_id;

    public  SubscribeMessage(final String product_id) {
        this.product_id = product_id;
    }
}
