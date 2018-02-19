package org.quuux.gdax.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
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
    int mLookback = -1;

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

        this.getAxisRight().setEnabled(false);
        this.getAxisLeft().setTextColor(color);

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

        int start = mLookback > 0 ? candles.candles.length - mLookback : 0;

        for (int i=start; i<candles.candles.length; i++) {
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

        BarDataSet barSet = new BarDataSet(barValues, "volume");
        barSet.setColor(R.color.cardview_light_background);
        barSet.setDrawValues(false);

        CombinedData data = new CombinedData();
        data.setData(new CandleData(candleSet));
        data.setData(new BarData(barSet));

        setData(data);
        invalidate();
    }

    public void setLookback(final int lookback) {
        mLookback = lookback;
    }
}
