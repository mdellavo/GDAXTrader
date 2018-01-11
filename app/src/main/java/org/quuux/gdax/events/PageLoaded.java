package org.quuux.gdax.events;


import org.quuux.gdax.Datastore;

public class PageLoaded {
    public final Datastore.Cursor cursor;

    public PageLoaded(Datastore.Cursor cursor) {
        this.cursor = cursor;
    }
}
