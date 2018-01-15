package org.quuux.gdax.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.quuux.gdax.R;
import org.quuux.gdax.model.Product;

import java.util.List;


public class ProductAdapater extends SimpleArrayAdapter<Product> {
    static class ProductTag {
        TextView product;
    }

    public ProductAdapater(final Context context, final List<Product> items) {
        super(context, items);
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
