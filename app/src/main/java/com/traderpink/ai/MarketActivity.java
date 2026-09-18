package com.traderpink.ai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.view.Gravity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MarketActivity extends Activity {

    private static final String MARKET_API_URL =
            "https://pk-forex-ai.bijondebnath51.workers.dev/api/market?interval=1min";

    private static final String SIGNAL_API_URL =
            "https://pk-forex-ai.bijondebnath51.workers.dev/api/signal";

    private final Handler handler = new Handler();

    private MarketChartView chartView;
    private MarketAnalysisView analysisView;

    private TextView priceText;
    private TextView statusText;

    private final Runnable refreshTask = new Runnable() {

        @Override
        public void run() {

            loadMarket();
            loadSignal();

            handler.postDelayed(
                    this,
                    30000
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        buildScreen();

        loadMarket();
        loadSignal();

        handler.postDelayed(
                refreshTask,
                30000
        );
    }

    private void buildScreen() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                20,
                25,
                20,
                20
        );

        root.setBackgroundColor(
                0xFF101318
        );

        TextView title =
                new TextView(this);

        title.setText(
                "🤖 Trader Pink AI\nEURUSD • 1 MIN"
        );

        title.setTextSize(21);

        title.setTextColor(
                0xFFFFFFFF
        );

        title.setPadding(
                10,
                10,
                10,
                15
        );

        root.addView(title);

        priceText =
                new TextView(this);

        priceText.setText(
                "Price: Loading..."
        );

        priceText.setTextSize(19);

        priceText.setTextColor(
                0xFFFFC107
        );

        priceText.setPadding(
                10,
                5,
                10,
                10
        );

        root.addView(priceText);

        /*
         * Market chart
         *
         * আগের weight-based chart-এর পরিবর্তে
         * নির্দিষ্ট height দেওয়া হয়েছে যাতে
         * chart-এর নিচে AI Analysis দেখা যায়।
         */
        chartView =
                new MarketChartView(this);

        LinearLayout.LayoutParams chartParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        360
                );

        chartParams.setMargins(
                0,
                10,
                0,
                10
        );

        root.addView(
                chartView,
                chartParams
        );

        statusText =
                new TextView(this);

        statusText.setText(
                "Market data loading..."
        );

        statusText.setTextSize(15);

        statusText.setTextColor(
                0xFFB8C0CC
        );

        statusText.setGravity(
                Gravity.CENTER
        );

        statusText.setPadding(
                10,
                10,
                10,
                10
        );

        root.addView(statusText);

        /*
         * AI Analysis section
         */
        ScrollView analysisScroll =
                new ScrollView(this);

        analysisView =
                new MarketAnalysisView(this);

        analysisScroll.addView(
                analysisView
        );

        LinearLayout.LayoutParams analysisParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        analysisParams.setMargins(
                0,
                10,
                0,
                0
        );

        root.addView(
                analysisScroll,
                analysisParams
        );

        setContentView(root);
    }

    private void loadMarket() {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(
                                MARKET_API_URL
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "GET"
                );

                connection.setConnectTimeout(
                        10000
                );

                connection.setReadTimeout(
                        10000
                );

                int responseCode =
                        connection.getResponseCode();

                InputStream stream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    stream =
                            connection.getInputStream();

                } else {

                    stream =
                            connection.getErrorStream();
                }

                if (stream == null) {

                    throw new Exception(
                            "No response"
                    );
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream
                                )
                        );

                StringBuilder result =
                        new StringBuilder();

                String line;

                while (
                        (line = reader.readLine())
                                != null
                ) {

                    result.append(line);
                }

                reader.close();

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    throw new Exception(
                            "HTTP " +
                                    responseCode
                    );
                }

                JSONObject json =
                        new JSONObject(
                                result.toString()
                        );

                final double price =
                        json.optDouble(
                                "price",
                                0.0
                        );

                final String interval =
                        json.optString(
                                "interval",
                                "1min"
                        );

                final String dataStatus =
                        json.optString(
                                "data_status",
                                "--"
                        );

                final JSONObject marketCandle =
                        json.optJSONObject(
                                "candle"
                        );

                String candleTime = "--";

                if (marketCandle != null) {

                    candleTime =
                            marketCandle.optString(
                                    "time",
                                    "--"
                            );
                }

                final String finalCandleTime =
                        candleTime;

                runOnUiThread(() -> {

                    chartView.setMarketData(
                            json
                    );

                    priceText.setText(
                            String.format(
                                    Locale.US,
                                    "EURUSD  %.5f",
                                    price
                            )
                    );

                    statusText.setText(
                            "Market: " +
                                    interval +
                                    "\n" +
                                    "Candle: " +
                                    finalCandleTime +
                                    "\n" +
                                    "Data: " +
                                    dataStatus
                    );
                });

            } catch (Exception e) {

                final String error =
                        e.getMessage() == null
                                ? "Unknown error"
                                : e.getMessage();

                runOnUiThread(() -> {

                    statusText.setText(
                            "Market data error:\n" +
                                    error
                    );
                });

            } finally {

                if (connection != null) {

                    connection.disconnect();
                }
            }

        }).start();
    }

    private void loadSignal() {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(
                                SIGNAL_API_URL
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "GET"
                );

                connection.setConnectTimeout(
                        10000
                );

                connection.setReadTimeout(
                        10000
                );

                int responseCode =
                        connection.getResponseCode();

                InputStream stream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    stream =
                            connection.getInputStream();

                } else {

                    stream =
                            connection.getErrorStream();
                }

                if (stream == null) {

                    throw new Exception(
                            "No signal response"
                    );
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream
                                )
                        );

                StringBuilder result =
                        new StringBuilder();

                String line;

                while (
                        (line = reader.readLine())
                                != null
                ) {

                    result.append(line);
                }

                reader.close();

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    throw new Exception(
                            "HTTP " +
                                    responseCode
                    );
                }

                JSONObject json =
                        new JSONObject(
                                result.toString()
                        );

                runOnUiThread(() -> {

                    analysisView.setSignal(
                            json
                    );
                });

            } catch (Exception e) {

                final String error =
                        e.getMessage() == null
                                ? "Unknown error"
                                : e.getMessage();

                runOnUiThread(() -> {

                    JSONObject fallback =
                            new JSONObject();

                    try {

                        fallback.put(
                                "signal",
                                "WAIT"
                        );

                        fallback.put(
                                "message",
                                "Signal data unavailable: " +
                                        error
                        );

                    } catch (Exception ignored) {
                    }

                    analysisView.setSignal(
                            fallback
                    );
                });

            } finally {

                if (connection != null) {

                    connection.disconnect();
                }
            }

        }).start();
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(
                refreshTask
        );

        super.onDestroy();
    }
}
