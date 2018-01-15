package org.quuux.gdax.view;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleArrayAdapter<T> extends ArrayAdapter<T> {

    public SimpleArrayAdapter(Context context, List<T> items) {
        super(context, 0, 0, items);
        setNotifyOnChange(true);
    }

    public SimpleArrayAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View view, @NonNull final ViewGroup parent) {
        if (view == null)
            view = newView(position, parent);

        T item = getItem(position);
        bindView(position, view, item);

        return view;
    }

    @Override
    public View getDropDownView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public abstract void bindView(final int position, final View view, final T item);
    public abstract View newView(final int position, final ViewGroup parent);
}
