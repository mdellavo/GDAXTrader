package org.quuux.gdax.events;


import org.quuux.gdax.net.Cursor;

public class CursorUpdated {

    public final Cursor cursor;
    public final APIError error;

    public CursorUpdated(final Cursor cursor, final APIError error) {
        this.cursor = cursor;
        this.error = error;
    }

    public CursorUpdated(Cursor cursor) {
        this(cursor, null);
    }

}
