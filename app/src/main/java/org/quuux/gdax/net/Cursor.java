package org.quuux.gdax.net;

import org.greenrobot.eventbus.EventBus;
import org.quuux.gdax.events.APIError;
import org.quuux.gdax.events.CursorUpdated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Cursor<T> implements API.PaginatedResponseListener<T[]> {

    public enum State {init, loaded, loading, error}

    String before, after;
    List<T> items = new ArrayList<>();
    State state = State.init;

    public List<T> getItems() {
        return items;
    }

    abstract public Class<T[]> getPageClass();

    public void load() {
        state = State.loading;
        API api = API.getInstance();
        api.loadPage(getEndpoint(), this, getPageClass());
        EventBus.getDefault().post(new CursorUpdated(this));
    }

    public void reset() {
        before = after = null;
        items.clear();
        load();
    }

    public abstract String getEndpoint();

    @Override
    public void onSuccess(final T[] result, final String before, final String after) {
        state = State.loaded;
        Collections.addAll(items, result);
        this.before = before;
        this.after = after;
        EventBus.getDefault().post(new CursorUpdated(this));
    }

    @Override
    public void onError(final APIError error) {
        state = State.error;
        EventBus.getDefault().post(new CursorUpdated(this, error));
    }

    public State getState() {
        return state;
    }
}
