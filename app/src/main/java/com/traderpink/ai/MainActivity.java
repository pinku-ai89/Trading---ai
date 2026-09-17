package com.traderpink.ai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String API_URL =
            "https://pk-forex-ai.bijondebnath51.workers.dev/api/signal";

    private final Handler handler = new Handler();
    private TextView text;

    private final Runnable refreshTask = new Runnable() {

        @Override
        public void run() {

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
                0,
                0,
                0,
                10
        );

        Button marketButton =
                new Button(this);

        marketButton.setText(
                "📊 MARKET VIEW"
        );

        marketButton.setTextSize(16);

        marketButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    MarketActivity.class
                            );

                    startActivity(intent);
                }
        );

        ScrollView scrollView =
                new ScrollView(this);

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        text = new TextView(this);

        text.setTextSize(18);

        text.setPadding(
                30,
                40,
                30,
                40
        );

        text.setText(
                "🤖 Trader Pink AI\n\n" +
                "EURUSD • 1 MIN\n\n" +
                "Loading analysis..."
        );

        content.addView(
                text
        );

        scrollView.addView(
                content
        );

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        root.addView(
                marketButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);
    }

    private void loadSignal() {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(API_URL);

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
                            "No response from Worker"
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

                final String signal =
                        json.optString(
                                "signal",
                                "WAIT"
                        );

                final String confidence =
                        json.optString(
                                "confidence",
                                "--"
                        );

                final String trend =
                        json.optString(
                                "trend",
                                "--"
                        );

                final String support =
                        json.optString(
                                "support",
                                "--"
                        );

                final String resistance =
                        json.optString(
                                "resistance",
                                "--"
                        );

                final String rsi =
                        json.optString(
                                "rsi",
                                "--"
                        );

                final String adx =
                        json.optString(
                                "adx",
                                "--"
                        );

                JSONObject confirmation =
                        json.optJSONObject(
                                "confirmation"
                        );

                String m1 = "--";
                String m5 = "--";
                String m15 = "--";
                String h1 = "--";

                if (confirmation != null) {

                    m1 =
                            confirmation.optString(
                                    "1M",
                                    "--"
                            );

                    m5 =
                            confirmation.optString(
                                    "5M",
                                    "--"
                            );

                    m15 =
                            confirmation.optString(
                                    "15M",
                                    "--"
                            );

                    h1 =
                            confirmation.optString(
                                    "1H",
                                    "--"
                            );
                }

                final String finalM1 = m1;
                final String finalM5 = m5;
                final String finalM15 = m15;
                final String finalH1 = h1;

                final String closedCandle =
                        json.optString(
                                "closed_candle",
                                "--"
                        );

                final String candleTime =
                        json.optString(
                                "candle_time",
                                "--"
                        );

                final String structure =
                        json.optString(
                                "structure",
                                "--"
                        );

                final String liquidity =
                        json.optString(
                                "liquidity",
                                "--"
                        );

                final String fvg =
                        json.optString(
                                "fvg",
                                "--"
                        );

                final String candlePattern =
                        json.optString(
                                "candle_pattern",
                                "--"
                        );

                final String message =
                        json.optString(
                                "message",
                                ""
                        );

                runOnUiThread(() -> {

                    String display =

                            "🤖 Trader Pink AI\n\n" +

                            "EURUSD • 1 MIN\n\n" +

                            "SIGNAL: " +
                            signal +
                            "\n\n" +

                            "CONFIDENCE: " +
                            confidence +
                            "\n\n" +

                            "TREND: " +
                            trend +
                            "\n\n" +

                            "SUPPORT: " +
                            support +
                            "\n" +

                            "RESISTANCE: " +
                            resistance +
                            "\n\n" +

                            "RSI: " +
                            rsi +
                            "\n" +

                            "ADX: " +
                            adx +
                            "\n\n" +

                            "TIMEFRAME CONFIRMATION\n\n" +

                            "1M : " +
                            finalM1 +
                            "\n" +

                            "5M : " +
                            finalM5 +
                            "\n" +

                            "15M : " +
                            finalM15 +
                            "\n" +

                            "1H : " +
                            finalH1 +
                            "\n\n" +

                            "STRUCTURE: " +
                            structure +
                            "\n" +

                            "LIQUIDITY: " +
                            liquidity +
                            "\n" +

                            "FVG: " +
                            fvg +
                            "\n" +

                            "CANDLE PATTERN: " +
                            candlePattern +
                            "\n\n" +

                            "MESSAGE: " +
                            message +
                            "\n\n" +

                            "CLOSED CANDLE: " +
                            closedCandle +
                            "\n\n" +

                            "CANDLE TIME: " +
                            candleTime;

                    text.setText(
                            display
                    );
                });

            } catch (Exception e) {

                final String error =

                        "🤖 Trader Pink AI\n\n" +

                        "EURUSD • 1 MIN\n\n" +

                        "SIGNAL: WAIT\n\n" +

                        "Connection error:\n" +

                        e.getMessage();

                runOnUiThread(() ->
                        text.setText(
                                error
                        )
                );

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
