package org.quuux.gdax.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.quuux.gdax.Datastore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CandleView extends CandleStickChart {
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
        this.setDragEnabled(false);
        this.setTouchEnabled(false);
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

        List<CandleEntry> values = new ArrayList<>();

        int lookback = 48;

        for (int i=candles.candles.length-lookback; i<candles.candles.length; i++) {
            float time, low, high, open, close, volume;
            time = candles.candles[i][0];
            low = candles.candles[i][1];
            high = candles.candles[i][2];
            open = candles.candles[i][3];
            close = candles.candles[i][4];
            volume = candles.candles[i][5];

            CandleEntry entry = new CandleEntry(i, high, low, open, close);
            values.add(entry);
        }

        CandleDataSet set = new CandleDataSet(values, "activity");
        set.setDecreasingColor(Color.RED);
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(Color.rgb(122, 242, 84));
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setDrawValues(false);

        setData(new CandleData(set));
        invalidate();
    }

}
