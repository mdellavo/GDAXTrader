package org.quuux.gdax.events;

public class ProductsLoadError {
    public final APIError error;

    public ProductsLoadError(APIError error) {
        this.error = error;
    }
}
