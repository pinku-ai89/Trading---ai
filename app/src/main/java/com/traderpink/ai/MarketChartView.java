package com.traderpink.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarketChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Candle> candles = new ArrayList<>();

    private double currentPrice = 0.0;

    public MarketChartView(Context context) {
        super(context);

        paint.setStrokeWidth(2f);
        setBackgroundColor(0xFF101318);
    }

    public void setMarketData(JSONObject json) {

        try {

            candles.clear();

            currentPrice = json.optDouble("price", 0.0);

            JSONArray array = json.optJSONArray("candles");

            if (array != null) {

                for (int i = 0; i < array.length(); i++) {

                    JSONObject item = array.getJSONObject(i);

                    Candle candle = new Candle();

                    candle.open = item.optDouble("open");
                    candle.high = item.optDouble("high");
                    candle.low = item.optDouble("low");
                    candle.close = item.optDouble("close");
                    candle.time = item.optString("time", "");

                    candles.add(candle);
                }
            }

            invalidate();

        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (candles.isEmpty()) {

            paint.setTextSize(40);
            paint.setColor(0xFFFFFFFF);

            canvas.drawText(
                    "Loading market...",
                    40,
                    80,
                    paint
            );

            return;
        }

        float width = getWidth();
        float height = getHeight();

        float left = 30;
        float right = width - 20;
        float top = 20;
        float bottom = height - 30;

        double minPrice = Double.MAX_VALUE;
        double maxPrice = -Double.MAX_VALUE;

        for (Candle c : candles) {

            minPrice = Math.min(minPrice, c.low);
            maxPrice = Math.max(maxPrice, c.high);
        }

        double range = maxPrice - minPrice;

        if (range <= 0) {
            range = 0.0001;
        }

        // Small padding around candles
        minPrice -= range * 0.08;
        maxPrice += range * 0.08;
        range = maxPrice - minPrice;

        int visibleCount = Math.min(candles.size(), 80);

        int start =
                Math.max(0, candles.size() - visibleCount);

        float chartWidth = right - left;

        float candleSpace =
                chartWidth / visibleCount;

        float bodyWidth =
                Math.max(4f, candleSpace * 0.55f);

        // Horizontal grid lines
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(0xFF252A31);

        for (int i = 1; i < 6; i++) {

            float y =
                    top +
                    (bottom - top) * i / 6f;

            canvas.drawLine(
                    left,
                    y,
                    right,
                    y,
                    paint
            );
        }

        // Candles
        for (int i = start; i < candles.size(); i++) {

            Candle c = candles.get(i);

            int position = i - start;

            float x =
                    left +
                    position * candleSpace +
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

            // Green bullish / Red bearish
            paint.setColor(
                    bullish
                            ? 0xFF20C878
                            : 0xFFFF4D5A
            );

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);

            // Wick
            canvas.drawLine(
                    x,
                    highY,
                    x,
                    lowY,
                    paint
            );

            // Body
            float bodyTop =
                    Math.min(openY, closeY);

            float bodyBottom =
                    Math.max(openY, closeY);

            if (bodyBottom - bodyTop < 2f) {
                bodyBottom = bodyTop + 2f;
            }

            paint.setStyle(Paint.Style.FILL);

            RectF body =
                    new RectF(
                            x - bodyWidth / 2f,
                            bodyTop,
                            x + bodyWidth / 2f,
                            bodyBottom
                    );

            canvas.drawRect(body, paint);
        }

        // Current price line
        if (currentPrice > 0) {

            float priceY =
                    priceToY(
                            currentPrice,
                            minPrice,
                            range,
                            top,
                            bottom
                    );

            paint.setColor(0xFFFFC107);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);

            canvas.drawLine(
                    left,
                    priceY,
                    right,
                    priceY,
                    paint
            );

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(28);
            paint.setColor(0xFFFFC107);

            String priceText =
                    String.format(
                            java.util.Locale.US,
                            "%.5f",
                            currentPrice
                    );

            canvas.drawText(
                    priceText,
                    left,
                    Math.max(
                            top + 25,
                            priceY - 8
                    ),
                    paint
            );
        }

        // Latest candle time
        Candle last =
                candles.get(candles.size() - 1);

        paint.setTextSize(24);
        paint.setColor(0xFFB8C0CC);

        canvas.drawText(
                last.time,
                left,
                height - 5,
                paint
        );
    }

    private float priceToY(
            double price,
            double minPrice,
            double range,
            float top,
            float bottom
    ) {

        double position =
                (price - minPrice) / range;

        return (float)
                (bottom -
                        position *
                                (bottom - top));
    }

    private static class Candle {

        double open;
        double high;
        double low;
        double close;

        String time;
    }
}
