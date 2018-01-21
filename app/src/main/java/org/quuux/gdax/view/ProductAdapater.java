package org.quuux.gdax.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.quuux.feller.Log;
import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.model.Product;

import java.util.List;


public class ProductAdapater extends SimpleArrayAdapter<Product> {
    private static final String TAG = Log.buildTag(ProductAdapater.class);

    static class ProductTag {
        TextView product;
    }

    public ProductAdapater(final Context context, final List<Product> items) {
        super(context, items);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(final int position) {
        return position >= 0 ? getItem(position).id.hashCode() : position;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View view, @NonNull final ViewGroup parent) {
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.product_spinner_item, parent, false);
        TextView tv = view.findViewById(R.id.product);
        Product product = getItem(position);
        tv.setText(product.getName());
        return view;
    }

    @Override
    public View getDropDownView(final int position, @Nullable final View view, @NonNull final ViewGroup parent) {
        return super.getView(position, view, parent);
    }

    @Override
    public void bindView(final int position, final View view, final Product product) {
        ProductTag tag = (ProductTag) view.getTag();
        tag.product.setText(product.getName());
    }

    @Override
    public View newView(final int position, final ViewGroup parent) {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.product_spinner_item, parent, false);
        ProductTag tag = new ProductTag();
        tag.product = v.findViewById(R.id.product);
        v.setTag(tag);
        return v;
    }
}
