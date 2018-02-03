package org.quuux.gdax.view;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.net.Cursor;
import org.quuux.gdax.events.CursorUpdated;

public abstract class CursorAdapter<T> extends SimpleArrayAdapter<T> {
    public final Cursor cursor;

    public CursorAdapter(Context context, Cursor<T> cursor) {
        super(context, cursor.getItems());
        this.cursor = cursor;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }

    public void unregister() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCursorUpdated(CursorUpdated event) {
        if (event.cursor != cursor)
            return;

        notifyDataSetChanged();
    }
}
