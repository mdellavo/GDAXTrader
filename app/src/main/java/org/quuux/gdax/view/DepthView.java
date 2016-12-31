package org.quuux.gdax.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import org.quuux.feller.Log;
import org.quuux.gdax.model.OrderBook;

import java.math.BigDecimal;

public class DepthView extends View {

    private static final String TAG = Log.buildTag(DepthView.class);
    private static final int POINTS = 10000;

    private OrderBook orderBook;
    private float[] buyPoints = new float[POINTS * 4];
    private float[] sellPoints = new float[POINTS * 4];
    private int width;
    private int height;
    private Paint buyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint sellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int numBuyPoints;
    private int numSellPoints;

    public DepthView(final Context context) {
        super(context);
        init();
    }

    public DepthView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DepthView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DepthView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLines(buyPoints, 0, numBuyPoints, buyPaint);
        canvas.drawLines(sellPoints, 0, numSellPoints, sellPaint);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void init() {
        buyPaint.setColor(Color.RED);
        buyPaint.setStrokeWidth(10);

        sellPaint.setColor(Color.BLUE);
        sellPaint.setStrokeWidth(10);
    }

    public void setOrderBook(OrderBook orderBook) {
        this.orderBook = orderBook;
        invalidate();
    }

    private int renderSide(final OrderBook.Side side, float[] points) {
        double maxPrice = orderBook.getMaxPrice(side).doubleValue();
        double maxSize = orderBook.getMaxSize(side).doubleValue();
        int numPrices = orderBook.numPrices(side);

        int i = 0;
        for (BigDecimal price : orderBook.prices(side)) {
            OrderBook.OrderBookBin bin = orderBook.getBin(side, price);
            double x = price.doubleValue() / maxPrice * width;
            double y = height - (bin.size.doubleValue() / maxSize * height);
            points[i] = (float) x;
            points[i+1] = (float) y;
            //Log.d(TAG, "price=%s / size=%s -> %s,%s", price.doubleValue(), bin.size.doubleValue(), x, y);
            i += 2;
        }

        return numPrices;
    }

    public void update() {
        if (orderBook != null) {
            numBuyPoints = renderSide(OrderBook.Side.buy, buyPoints);
            numSellPoints = renderSide(OrderBook.Side.sell, sellPoints);
        }
        invalidate();
    }
}
