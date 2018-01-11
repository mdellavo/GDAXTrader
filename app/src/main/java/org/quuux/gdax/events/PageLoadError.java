package org.quuux.gdax.events;

import org.quuux.gdax.Datastore;

public class PageLoadError {
    public final Datastore.Cursor cursor;
    public final APIError error;

    public PageLoadError(final Datastore.Cursor cursor, final APIError error) {
        this.cursor = cursor;
        this.error = error;
    }
}
