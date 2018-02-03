package org.quuux.gdax.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.quuux.gdax.R;
import org.quuux.gdax.model.CoinbaseAccount;
import org.quuux.gdax.net.Cursor;

public class CoinbaseAccountAdapter extends CursorAdapter<CoinbaseAccount> {

    public CoinbaseAccountAdapter(final Context context, final Cursor<CoinbaseAccount> cursor) {
        super(context, cursor);
    }

    static class CoinbaseAccountTag {
        TextView name;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(final int position) {
        return position >= 0 ? getItem(position).id.hashCode() : position;
    }

    @Override
    public View getDropDownView(final int position, @Nullable final View view, @NonNull final ViewGroup parent) {
        return super.getView(position, view, parent);
    }


    @Override
    public void bindView(final int position, final View view, final CoinbaseAccount item) {
        CoinbaseAccountTag tag = (CoinbaseAccountTag) view.getTag();
        tag.name.setText(item.name);

    }

    @Override
    public View newView(final int position, final ViewGroup parent) {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.coinbase_account_item, parent, false);
        CoinbaseAccountTag tag = new CoinbaseAccountTag();
        tag.name = v.findViewById(R.id.account);
        v.setTag(tag);
        return v;
    }
}
