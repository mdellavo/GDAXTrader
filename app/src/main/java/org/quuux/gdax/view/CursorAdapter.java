package org.quuux.gdax.view;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.events.PageLoadError;
import org.quuux.gdax.events.PageLoaded;

public abstract class CursorAdapter<T> extends SimpleArrayAdapter<T> {
    private final Datastore.Cursor cursor;

    public CursorAdapter(Context context, Datastore.Cursor<T> cursor) {
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
    public void onPageLoaded(PageLoaded event) {
        if (event.cursor != cursor)
            return;

        notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPageLoadError(PageLoadError event) {
        if (event.cursor != cursor)
            return;

        notifyDataSetChanged();
    }
}
