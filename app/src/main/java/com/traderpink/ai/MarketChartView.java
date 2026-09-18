package com.traderpink.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MarketChartView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<Candle> candles =
            new ArrayList<>();

    private double currentPrice = 0.0;

    private double support = 0.0;
    private double resistance = 0.0;
    private double watchLevel = 0.0;

    private double entry = 0.0;
    private double sl = 0.0;
    private double tp1 = 0.0;
    private double tp2 = 0.0;

    private String signal = "WAIT";

    private float zoom = 1.0f;
    private float panX = 0f;

    private float lastTouchX;
    private boolean dragging = false;

    private ScaleGestureDetector scaleDetector;

    private final Runnable timerRunnable =
            new Runnable() {

                @Override
                public void run() {

                    invalidate();

                    postDelayed(
                            this,
                            1000
                    );
                }
            };

    public MarketChartView(Context context) {

        super(context);

        paint.setStrokeWidth(2f);

        setBackgroundColor(
                0xFF101318
        );

        scaleDetector =
                new ScaleGestureDetector(
                        context,
                        new ScaleListener()
                );

        post(
                timerRunnable
        );
    }

    public void setMarketData(JSONObject json) {

        try {

            candles.clear();

            currentPrice =
                    json.optDouble(
                            "price",
                            0.0
                    );

            JSONArray array =
                    json.optJSONArray(
                            "candles"
                    );

            if (array != null) {

                for (
                        int i = 0;
                        i < array.length();
                        i++
                ) {

                    JSONObject item =
                            array.getJSONObject(i);

                    Candle candle =
                            new Candle();

                    candle.open =
                            item.optDouble(
                                    "open"
                            );

                    candle.high =
                            item.optDouble(
                                    "high"
                            );

                    candle.low =
                            item.optDouble(
                                    "low"
                            );

                    candle.close =
                            item.optDouble(
                                    "close"
                            );

                    candle.time =
                            item.optString(
                                    "time",
                                    ""
                            );

                    candles.add(
                            candle
                    );
                }
            }

            invalidate();

        } catch (Exception ignored) {
        }
    }

    /*
     * AI SIGNAL DATA
     *
     * MarketActivity থেকে signal JSON এখানে আসবে।
     */
    public void setSignalData(JSONObject json) {

        try {

            signal =
                    json.optString(
                            "signal",
                            "WAIT"
                    ).toUpperCase(
                            Locale.US
                    );

            support =
                    json.optDouble(
                            "support",
                            0.0
                    );

            resistance =
                    json.optDouble(
                            "resistance",
                            0.0
                    );

            /*
             * Watch Level optional.
             *
             * Worker যদি watch_level না পাঠায়,
             * তাহলে 0 থাকবে এবং chart-এ দেখাবে না।
             */
            watchLevel =
                    json.optDouble(
                            "watch_level",
                            0.0
                    );

            entry =
                    json.optDouble(
                            "entry",
                            0.0
                    );

            sl =
                    json.optDouble(
                            "sl",
                            0.0
                    );

            tp1 =
                    json.optDouble(
                            "tp1",
                            0.0
                    );

            tp2 =
                    json.optDouble(
                            "tp2",
                            0.0
                    );

            invalidate();

        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (candles.isEmpty()) {

            paint.setStyle(
                    Paint.Style.FILL
            );

            paint.setTextSize(
                    40
            );

            paint.setColor(
                    0xFFFFFFFF
            );

            canvas.drawText(
                    "Loading market...",
                    40,
                    80,
                    paint
            );

            return;
        }

        float width =
                getWidth();

        float height =
                getHeight();

        /*
         * Chart layout
         */

        float left =
                25;

        float right =
                width - 105;

        float top =
                25;

        float bottom =
                height - 65;

        if (right <= left) {
            right = width - 20;
        }

        /*
         * Visible candle range
         */

        int baseVisible =
                60;

        int visibleCount =
                Math.max(
                        10,
                        Math.min(
                                candles.size(),
                                (int)
                                        (
                                                baseVisible /
                                                        zoom
                                        )
                        )
                );

        int maxStart =
                Math.max(
                        0,
                        candles.size() -
                                visibleCount
                );

        int start =
                Math.max(
                        0,
                        Math.min(
                                maxStart,
                                (int) panX
                        )
                );

        /*
         * Price range
         */

        double minPrice =
                Double.MAX_VALUE;

        double maxPrice =
                -Double.MAX_VALUE;

        for (
                int i = start;
                i < start + visibleCount &&
                        i < candles.size();
                i++
        ) {

            Candle c =
                    candles.get(i);

            minPrice =
                    Math.min(
                            minPrice,
                            c.low
                    );

            maxPrice =
                    Math.max(
                            maxPrice,
                            c.high
                    );
        }

        if (currentPrice > 0) {

            minPrice =
                    Math.min(
                            minPrice,
                            currentPrice
                    );

            maxPrice =
                    Math.max(
                            maxPrice,
                            currentPrice
                    );
        }

        /*
         * Include AI levels in chart price range.
         *
         * এতে Support/Resistance/TP/SL
         * chart-এর বাইরে চলে যাবে না।
         */

        double[] levels = {

                support,
                resistance,
                watchLevel,
                entry,
                sl,
                tp1,
                tp2
        };

        for (
                double level :
                levels
        ) {

            if (level > 0) {

                minPrice =
                        Math.min(
                                minPrice,
                                level
                        );

                maxPrice =
                        Math.max(
                                maxPrice,
                                level
                        );
            }
        }

        double range =
                maxPrice - minPrice;

        if (range <= 0) {
            range = 0.0001;
        }

        minPrice -=
                range * 0.08;

        maxPrice +=
                range * 0.08;

        range =
                maxPrice - minPrice;

        /*
         * Background
         */

        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setColor(
                0xFF101318
        );

        canvas.drawRect(
                0,
                0,
                width,
                height,
                paint
        );

        /*
         * Grid
         */

        paint.setStyle(
                Paint.Style.STROKE
        );

        paint.setStrokeWidth(
                1f
        );

        paint.setColor(
                0xFF252A31
        );

        for (
                int i = 1;
                i < 7;
                i++
        ) {

            float y =
                    top +
                            (
                                    bottom -
                                            top
                            ) *
                                    i /
                                    7f;

            canvas.drawLine(
                    left,
                    y,
                    right,
                    y,
                    paint
            );
        }

        /*
         * Candle spacing
         */

        float chartWidth =
                right - left;

        float candleSpace =
                chartWidth /
                        visibleCount;

        float bodyWidth =
                Math.max(
                        3f,
                        candleSpace * 0.58f
                );

        /*
         * Candles
         */

        for (
                int i = start;
                i < start + visibleCount &&
                        i < candles.size();
                i++
        ) {

            Candle c =
                    candles.get(i);

            int position =
                    i - start;

            float x =
                    left +
                            position *
                                    candleSpace +
                            candleSpace / 2f;

            float openY =
                    priceToY(
                            c.open,
                            minPrice,
                            range,
                            top,
                            bottom
                    );

            float closeY =
                    priceToY(
                            c.close,
                            minPrice,
                            range,
                            top,
                            bottom
                    );

            float highY =
                    priceToY(
                            c.high,
                            minPrice,
                            range,
                            top,
                            bottom
                    );

            float lowY =
                    priceToY(
                            c.low,
                            minPrice,
                            range,
                            top,
                            bottom
                    );

            boolean bullish =
                    c.close >= c.open;

            paint.setColor(
                    bullish
                            ? 0xFF20C878
                            : 0xFFFF4D5A
            );

            /*
             * Wick
             */

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                    2f
            );

            canvas.drawLine(
                    x,
                    highY,
                    x,
                    lowY,
                    paint
            );

            /*
             * Body
             */

            float bodyTop =
                    Math.min(
                            openY,
                            closeY
                    );

            float bodyBottom =
                    Math.max(
                            openY,
                            closeY
                    );

            if (
                    bodyBottom -
                            bodyTop < 2f
            ) {

                bodyBottom =
                        bodyTop + 2f;
            }

            paint.setStyle(
                    Paint.Style.FILL
            );

            RectF body =
                    new RectF(
                            x -
                                    bodyWidth / 2f,
                            bodyTop,
                            x +
                                    bodyWidth / 2f,
                            bodyBottom
                    );

            canvas.drawRect(
                    body,
                    paint
            );
        }

        /*
         * AI LEVELS
         */

        drawLevel(
                canvas,
                "Support",
                support,
                0xFF42A5F5,
                left,
                right,
                minPrice,
                range,
                top,
                bottom
        );

        drawLevel(
                canvas,
                "Resistance",
                resistance,
                0xFFFF7043,
                left,
                right,
                minPrice,
                range,
                top,
                bottom
        );

        drawLevel(
                canvas,
                "Watch",
                watchLevel,
                0xFFAB47BC,
                left,
                right,
                minPrice,
                range,
                top,
                bottom
        );

        /*
         * Entry / SL / TP
         *
         * শুধু BUY বা SELL হলে দেখাবে।
         */

        boolean confirmed =
                signal.equals("BUY") ||
                        signal.equals("SELL");

        if (confirmed) {

            drawLevel(
                    canvas,
                    "Entry",
                    entry,
                    0xFFFFFFFF,
                    left,
                    right,
                    minPrice,
                    range,
                    top,
                    bottom
            );

            drawLevel(
                    canvas,
                    "SL",
                    sl,
                    0xFFFF4D5A,
                    left,
                    right,
                    minPrice,
                    range,
                    top,
                    bottom
            );

            drawLevel(
                    canvas,
                    "TP1",
                    tp1,
                    0xFF20C878,
                    left,
                    right,
                    minPrice,
                    range,
                    top,
                    bottom
            );

            drawLevel(
                    canvas,
                    "TP2",
                    tp2,
                    0xFF00C853,
                    left,
                    right,
                    minPrice,
                    range,
                    top,
                    bottom
            );
        }

        /*
         * Current price line
         */

        if (currentPrice > 0) {

            float priceY =
                    priceToY(
                            currentPrice,
                            minPrice,
                            range,
                            top,
                            bottom
                    );

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                    1.5f
            );

            paint.setColor(
                    0xFFFFC107
            );

            canvas.drawLine(
                    left,
                    priceY,
                    right,
                    priceY,
                    paint
            );

            /*
             * Current price label
             */

            paint.setStyle(
                    Paint.Style.FILL
            );

            paint.setTextSize(
                    24
            );

            paint.setColor(
                    0xFFFFC107
            );

            String priceText =
                    String.format(
                            Locale.US,
                            "%.5f",
                            currentPrice
                    );

            canvas.drawText(
                    priceText,
                    right + 5,
                    Math.max(
                            top + 22,
                            priceY - 5
                    ),
                    paint
            );
        }

        /*
         * Right side PRICE SCALE
         */

        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setTextSize(
                18
        );

        paint.setColor(
                0xFFB8C0CC
        );

        for (
                int i = 0;
                i <= 6;
                i++
        ) {

            double price =
                    maxPrice -
                            (
                                    range *
                                            i /
                                            6.0
                            );

            float y =
                    top +
                            (
                                    bottom -
                                            top
                            ) *
                                    i /
                                    6f;

            String label =
                    String.format(
                            Locale.US,
                            "%.5f",
                            price
                    );

            canvas.drawText(
                    label,
                    right + 5,
                    y + 6,
                    paint
            );
        }

        /*
         * TIME SCALE
         */

        paint.setTextSize(
                17
        );

        paint.setColor(
                0xFF8F99A8
        );

        int timeLabels =
                Math.min(
                        6,
                        visibleCount
                );

        for (
                int i = 0;
                i < timeLabels;
                i++
        ) {

            int index =
                    start +
                            (
                                    i *
                                            (
                                                    visibleCount - 1
                                            )
                            ) /
                                    Math.max(
                                            1,
                                            timeLabels - 1
                                    );

            if (
                    index >=
                            candles.size()
            ) {
                continue;
            }

            Candle c =
                    candles.get(index);

            float x =
                    left +
                            (
                                    index - start
                            ) *
                                    candleSpace +
                            candleSpace / 2f;

            String time =
                    shortTime(
                            c.time
                    );

            canvas.drawText(
                    time,
                    x - 25,
                    height - 20,
                    paint
            );
        }

        /*
         * Latest candle information
         */

        Candle last =
                candles.get(
                        candles.size() - 1
                );

        paint.setTextSize(
                16
        );

        paint.setColor(
                0xFFB8C0CC
        );

        canvas.drawText(
                "Candle: " +
                        last.time,
                left,
                height - 42,
                paint
        );

        /*
         * Next candle countdown
         */

        long now =
                System.currentTimeMillis();

        long seconds =
                (now / 1000) % 60;

        long remaining =
                60 - seconds;

        if (remaining == 60) {
            remaining = 0;
        }

        paint.setColor(
                0xFFFFC107
        );

        paint.setTextSize(
                18
        );

        canvas.drawText(
                "Next Candle: " +
                        String.format(
                                Locale.US,
                                "00:%02d",
                                remaining
                        ),
                right - 125,
                height - 42,
                paint
        );

        /*
         * Chart border
         */

        paint.setStyle(
                Paint.Style.STROKE
        );

        paint.setStrokeWidth(
                1f
        );

        paint.setColor(
                0xFF303640
        );

        canvas.drawRect(
                left,
                top,
                right,
                bottom,
                paint
        );
    }

    /*
     * Draw one AI level line
     */

    private void drawLevel(
            Canvas canvas,
            String label,
            double price,
            int color,
            float left,
            float right,
            double minPrice,
            double range,
            float top,
            float bottom
    ) {

        if (price <= 0) {
            return;
        }

        float y =
                priceToY(
                        price,
                        minPrice,
                        range,
                        top,
                        bottom
                );

        paint.setStyle(
                Paint.Style.STROKE
        );

        paint.setStrokeWidth(
                label.equals("Entry")
                        ? 2.5f
                        : 1.8f
        );

        paint.setColor(
                color
        );

        canvas.drawLine(
                left,
                y,
                right,
                y,
                paint
        );

        /*
         * Level label
         */

        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setTextSize(
                15
        );

        String text =
                label +
                        " " +
                        String.format(
                                Locale.US,
                                "%.5f",
                                price
                        );

        float labelY =
                Math.max(
                        top + 15,
                        Math.min(
                                bottom - 3,
                                y - 4
                        )
                );

        canvas.drawText(
                text,
                left + 5,
                labelY,
                paint
        );
    }

    private String shortTime(
            String value
    ) {

        if (
                value == null ||
                        value.length() == 0
        ) {
            return "--";
        }

        try {

            String[] formats = {

                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "yyyy-MM-dd'T'HH:mm:ss"
            };

            for (
                    String format :
                    formats
            ) {

                try {

                    SimpleDateFormat sdf =
                            new SimpleDateFormat(
                                    format,
                                    Locale.US
                            );

                    Date date =
                            sdf.parse(
                                    value
                            );

                    if (date != null) {

                        return new SimpleDateFormat(
                                "HH:mm",
                                Locale.US
                        ).format(
                                date
                        );
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }

        if (
                value.length() >= 16
        ) {

            return value.substring(
                    11,
                    Math.min(
                            16,
                            value.length()
                    )
            );
        }

        return value;
    }

    private float priceToY(
            double price,
            double minPrice,
            double range,
            float top,
            float bottom
    ) {

        double position =
                (
                        price -
                                minPrice
                ) /
                        range;

        return (float)
                (
                        bottom -
                                position *
                                        (
                                                bottom -
                                                        top
                                        )
                );
    }

    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        scaleDetector.onTouchEvent(
                event
        );

        switch (
                event.getActionMasked()
        ) {

            case MotionEvent.ACTION_DOWN:

                lastTouchX =
                        event.getX();

                dragging = true;

                return true;

            case MotionEvent.ACTION_MOVE:

                if (
                        dragging &&
                                event.getPointerCount()
                                        == 1
                ) {

                    float dx =
                            event.getX() -
                                    lastTouchX;

                    float movement =
                            -dx /
                                    8f;

                    panX += movement;

                    int visibleCount =
                            Math.max(
                                    10,
                                    Math.min(
                                            candles.size(),
                                            (int)
                                                    (
                                                            60 /
                                                                    zoom
                                                    )
                                    )
                            );

                    int maxStart =
                            Math.max(
                                    0,
                                    candles.size() -
                                            visibleCount
                            );

                    panX =
                            Math.max(
                                    0,
                                    Math.min(
                                            panX,
                                            maxStart
                                    )
                            );

                    lastTouchX =
                            event.getX();

                    invalidate();
                }

                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                dragging = false;

                return true;
        }

        return true;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(
                ScaleGestureDetector detector
        ) {

            zoom *=
                    detector.getScaleFactor();

            zoom =
                    Math.max(
                            0.5f,
                            Math.min(
                                    5.0f,
                                    zoom
                            )
                    );

            int visibleCount =
                    Math.max(
                            10,
                            Math.min(
                                    candles.size(),
                                    (int)
                                            (
                                                    60 /
                                                            zoom
                                            )
                            )
                    );

            int maxStart =
                    Math.max(
                            0,
                            candles.size() -
                                    visibleCount
                    );

            panX =
                    Math.max(
                            0,
                            Math.min(
                                    panX,
                                    maxStart
                            )
                    );

            invalidate();

            return true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {

        removeCallbacks(
                timerRunnable
        );

        super.onDetachedFromWindow();
    }

    private static class Candle {

        double open;
        double high;
        double low;
        double close;

        String time;
    }
}
