package com.traderpink.ai;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
            handler.postDelayed(this, 30000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        text = new TextView(this);
        text.setTextSize(18);
        text.setPadding(30, 40, 30, 40);
        text.setText("🤖 Trader Pink AI\n\nLoading EURUSD analysis...");

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(text);
        setContentView(scrollView);

        loadSignal();
        handler.postDelayed(refreshTask, 30000);
    }

    private void loadSignal() {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();

                InputStream stream;

                if (responseCode >= 200 && responseCode < 300) {
                    stream = connection.getInputStream();
                } else {
                    stream = connection.getErrorStream();
                }

                if (stream == null) {
                    throw new Exception("No response from Worker");
                }

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(stream));

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception("HTTP " + responseCode);
                }

                JSONObject json = new JSONObject(result.toString());

                final String signal = json.optString("signal", "WAIT");
                final String confidence =
                        json.optString("confidence", "--");
                final String trend =
                        json.optString("trend", "--");

                final String support =
                        json.optString("support", "--");
                final String resistance =
                        json.optString("resistance", "--");

                final String rsi =
                        json.optString("rsi", "--");
                final String adx =
                        json.optString("adx", "--");

                final String m1 =
                        json.optString("1M", "--");
                final String m5 =
                        json.optString("5M", "--");
                final String m15 =
                        json.optString("15M", "--");
                final String h1 =
                        json.optString("1H", "--");

                final String closedCandle =
                        json.optString("closed_candle", "--");

                final String candleTime =
                        json.optString("candle_time", "--");

                runOnUiThread(() -> {

                    String display =
                            "🤖 Trader Pink AI\n\n" +
                            "EURUSD • 1 MIN\n\n" +

                            "SIGNAL: " + signal + "\n" +
                            "CONFIDENCE: " + confidence + "\n" +
                            "TREND: " + trend + "\n\n" +

                            "SUPPORT: " + support + "\n" +
                            "RESISTANCE: " + resistance + "\n\n" +

                            "RSI: " + rsi + "\n" +
                            "ADX: " + adx + "\n\n" +

                            "TIMEFRAME CONFIRMATION\n" +
                            "1M  : " + m1 + "\n" +
                            "5M  : " + m5 + "\n" +
                            "15M : " + m15 + "\n" +
                            "1H  : " + h1 + "\n\n" +

                            "CLOSED CANDLE: " + closedCandle + "\n" +
                            "CANDLE TIME: " + candleTime;

                    text.setText(display);
                });

            } catch (Exception e) {

                final String error =
                        "🤖 Trader Pink AI\n\n" +
                        "EURUSD • 1 MIN\n\n" +
                        "WAIT\n\n" +
                        "Connection error:\n" +
                        e.getMessage();

                runOnUiThread(() -> text.setText(error));

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(refreshTask);
        super.onDestroy();
    }
}
