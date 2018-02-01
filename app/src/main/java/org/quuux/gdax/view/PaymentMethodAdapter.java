package org.quuux.gdax.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.quuux.gdax.R;
import org.quuux.gdax.model.PaymentMethod;
import org.quuux.gdax.net.Cursor;


public class PaymentMethodAdapter extends CursorAdapter<PaymentMethod> {

    static class PaymentMethodTag {
        TextView name;
    }

    public PaymentMethodAdapter(final Context context, final Cursor<PaymentMethod> cursor) {
        super(context, cursor);
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
    public void bindView(final int position, final View view, final PaymentMethod paymentMethod) {
        PaymentMethodTag tag = (PaymentMethodTag) view.getTag();
        tag.name.setText(paymentMethod.name);
    }

    @Override
    public View newView(final int position, final ViewGroup parent) {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.payment_method_item, parent, false);
        PaymentMethodTag tag = new PaymentMethodTag();
        tag.name = v.findViewById(R.id.payment_method);
        v.setTag(tag);
        return v;
    }

}
