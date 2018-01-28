package org.quuux.gdax.net;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.model.Fill;


public class FillsCursor extends Cursor<Fill> {
    @Override
    public Class<Fill[]> getPageClass() {
        return Fill[].class;
    }

    @Override
    public String getEndpoint() {
        return API.GDAX_FILLS_ENDPOINT + "?product_id=" + Datastore.getInstance().getSelectedProduct().id;
    }
}
