package org.quuux.gdax.net;

import org.quuux.gdax.model.Product;

public class ProductsCursor extends Cursor<Product> {
    @Override
    public Class<Product[]> getPageClass() {
        return Product[].class;
    }

    @Override
    public String getEndpoint() {
        return API.GDAX_PRODUCTS_ENDPOINT;
    }
}
