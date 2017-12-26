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
    private static final int RANGE = 50;
    private static final float LINE_WIDTH = 3;
    private static final int BINS = 100;

    private int width;
    private int height;

    private OrderBook orderBook;

    private float[] buyPoints = new float[BINS * 2 * 4];
    private float[] sellPoints = new float[BINS * 2 * 4];
    private Paint buyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint sellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
        canvas.drawLines(buyPoints, 0, BINS * 2, buyPaint);
        canvas.drawLines(sellPoints, 0, BINS * 2, sellPaint);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void init() {
        buyPaint.setColor(Color.RED);
        buyPaint.setStrokeWidth(LINE_WIDTH);

        sellPaint.setColor(Color.BLUE);
        sellPaint.setStrokeWidth(LINE_WIDTH);
    }

    public void setOrderBook(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    private void renderSide(final OrderBook.Side side, float[] points) {
        double midpointPrice = orderBook.getMidpointPrice().doubleValue();
        double minPrice = midpointPrice - RANGE;
        double maxPrice = midpointPrice + RANGE;
        double range = maxPrice - minPrice;
        double maxSize = orderBook.getMaxSizeInRange(side, new BigDecimal(minPrice), new BigDecimal(maxPrice)).doubleValue();

        int offset = 0;
        for (int i=0; i<BINS; i++) {
            double a = (double)i / (double)BINS;
            double b = (double)(i + 1) / (double)BINS;

            double priceLower = (a * range) + minPrice;
            double priceUpper = (b * range) + minPrice;

            OrderBook.OrderBookBin bin = orderBook.sumRange(side, new BigDecimal(priceLower), new BigDecimal(priceUpper));
            if (bin != null) {
                double size = bin.size.doubleValue();
                double x1 = width * a;
                double x2 = width * b;
                double y = height - (size / maxSize * height);

                if (offset > 0) {
                    points[offset] = points[offset - 2];
                    points[offset + 1] = points[offset - 1];
                    points[offset + 2] = (float) x1;
                    points[offset + 3] = (float) y;
                    offset += 4;
                }

                points[offset] = (float) x1;
                points[offset + 1] = (float) y;
                points[offset + 2] = (float) x2;
                points[offset + 3] = (float) y;

                offset += 4;

            }
        }
    }

    public void update() {
        if (orderBook == null)
            return;

        renderSide(OrderBook.Side.buy, buyPoints);
        renderSide(OrderBook.Side.sell, sellPoints);

        invalidate();
    }
}
