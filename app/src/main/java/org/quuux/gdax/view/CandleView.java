package org.quuux.gdax.view;

import android.content.Context;
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

import org.quuux.gdax.Datastore;
import org.quuux.gdax.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CandleView extends CombinedChart {
    private Datastore.Candles mCandles;

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

        YAxis yLeft = getAxisLeft();
        yLeft.setTextColor(color);
        yLeft.setDrawZeroLine(false);
        yLeft.setDrawAxisLine(false);
        yLeft.setDrawGridLines(false);

        XAxis xAxis = this.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextColor(color);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(final float value, final AxisBase axis) {
                float time = mCandles.candles[(int)value][0];
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d ha");
                return sdf.format(new Date((long) time * 1000));
            }
        });
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
        candleSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        candleSet.setShadowColorSameAsCandle(true);
        candleSet.setShadowWidth(1);

        BarDataSet barSet = new BarDataSet(barValues, "volume");
        barSet.setColor(R.color.cardview_light_background);
        barSet.setDrawValues(false);
        barSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

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
        notifyDataSetChanged();
        invalidate();
    }
}
