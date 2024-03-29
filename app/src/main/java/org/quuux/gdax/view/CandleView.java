package org.quuux.gdax.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;
import org.quuux.gdax.Util;
import org.quuux.gdax.net.API;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CandleView extends CombinedChart {
    private Datastore.Candles mCandles;

    class DateFormatter implements IAxisValueFormatter {

        private final DateFormat dateFormat;

        public DateFormatter(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public String getFormattedValue(final float value, final AxisBase axis) {
            int ival = (int)value;
            if (ival >= mCandles.candles.length)
                ival = mCandles.candles.length - 1;
            else if(ival < 0)
                ival = 0;

            float time = mCandles.candles[ival][0];
            return dateFormat.format(new Date((long) time * 1000));
        }
    }

    public CandleView(final Context context) {
        super(context);
    }

    public CandleView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

    }

    public CandleView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void init() {
        super.init();

        this.setDrawGridBackground(false);
        this.setDrawBorders(false);
        this.getDescription().setEnabled(false);
        this.getLegend().setEnabled(false);

        int color = ContextCompat.getColor(getContext(), android.R.color.primary_text_dark);

        YAxis yRight = getAxisRight();
        yRight.setTextColor(color);
        yRight.setDrawZeroLine(false);
        yRight.setDrawAxisLine(false);
        yRight.setDrawGridLines(false);
        yRight.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(final float value, final AxisBase axis) {
                return Util.currencyFormat(BigDecimal.valueOf(value));
            }
        });

        YAxis yLeft = getAxisLeft();
        yLeft.setTextColor(color);
        yLeft.setDrawZeroLine(false);
        yLeft.setDrawAxisLine(false);
        yLeft.setDrawGridLines(false);
        yLeft.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(final float value, final AxisBase axis) {
                return Util.intFormat(BigDecimal.valueOf(value));
            }
        });

        XAxis xAxis = this.getXAxis();
        setXAxisRenderer(new XAxisRenderer(mViewPortHandler, mXAxis, mLeftAxisTransformer) {

            /**
             * draws the x-labels on the specified y-position
             *
             * @param pos
             */
            protected void drawLabels(Canvas c, float pos, MPPointF anchor) {

                final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
                boolean centeringEnabled = mXAxis.isCenterAxisLabelsEnabled();

                float[] positions = new float[mXAxis.mEntryCount * 2];

                for (int i = 0; i < positions.length; i += 2) {

                    // only fill x values
                    if (centeringEnabled) {
                        positions[i] = mXAxis.mCenteredEntries[i / 2];
                    } else {
                        positions[i] = mXAxis.mEntries[i / 2];
                    }
                }

                mTrans.pointValuesToPixel(positions);

                for (int i = 0; i < positions.length; i += 2) {

                    float x = positions[i];

                    if (mViewPortHandler.isInBoundsX(x)) {

                        String label = mXAxis.getValueFormatter().getFormattedValue(mXAxis.mEntries[i / 2], mXAxis);

                        if (mXAxis.isAvoidFirstLastClippingEnabled()) {

                            // avoid clipping of the last
                            if (i/2 == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
                                float width = Utils.calcTextWidth(mAxisLabelPaint, label);

                                if (width > mViewPortHandler.offsetRight() * 2
                                        && x + width > mViewPortHandler.getChartWidth())
                                    x -= width / 2;

                                // avoid clipping of the first
                            } else if (i == 0) {

                                float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                                x += width / 2;
                            }
                        }

                        drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees);
                    }
                }
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setLabelRotationAngle(-60);
        xAxis.setTextColor(color);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setLabelCount(4, true);
        getRendererXAxis().getPaintAxisLabels().setTextAlign(Paint.Align.LEFT);
    }

    public DateFormat getDateFormat(final int granularity) {
        if (granularity >= API.ONE_DAY)
            return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
        else
            return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
    }

    public void update(Datastore.Candles candles) {
        mCandles = candles;

        Arrays.sort(candles.candles, new Comparator<float[]>() {
            @Override
            public int compare(final float[] a, final float[] b) {
                return Float.compare(a[0], b[0]);
            }
        });

        List<CandleEntry> candleValues = new ArrayList<>();
        List<BarEntry> barValues = new ArrayList<>();

        for (int i=0; i<candles.candles.length; i++) {
            float low, high, open, close, volume;
            low = candles.candles[i][1];
            high = candles.candles[i][2];
            open = candles.candles[i][3];
            close = candles.candles[i][4];
            volume = candles.candles[i][5];

            CandleEntry candleValue = new CandleEntry(i, high, low, open, close);
            candleValues.add(candleValue);

            BarEntry barEntry = new BarEntry(i, volume);
            barValues.add(barEntry);
        }

        CandleDataSet candleSet = new CandleDataSet(candleValues, "activity");
        candleSet.setDecreasingColor(Color.RED);
        candleSet.setDecreasingPaintStyle(Paint.Style.FILL);
        candleSet.setIncreasingColor(Color.rgb(122, 242, 84));
        candleSet.setIncreasingPaintStyle(Paint.Style.STROKE);
        candleSet.setDrawValues(false);
        candleSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        candleSet.setShadowColorSameAsCandle(true);
        candleSet.setShadowWidth(1);

        BarDataSet barSet = new BarDataSet(barValues, "volume");
        barSet.setColor(R.color.cardview_light_background);
        barSet.setDrawValues(false);
        barSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        CombinedData data = getData();
        boolean initial = data == null;
        if (initial) {
            data = new CombinedData();
        } else {
            data.clearValues();
        }
        data.setData(new CandleData(candleSet));
        data.setData(new BarData(barSet));
        if (initial)
            setData(data);

        setVisibleYRangeMinimum(0, YAxis.AxisDependency.LEFT);
        setVisibleYRangeMinimum(0, YAxis.AxisDependency.RIGHT);

        getXAxis().setValueFormatter(new DateFormatter(getDateFormat(candles.granularity)));

        notifyDataSetChanged();
        invalidate();
    }
}
