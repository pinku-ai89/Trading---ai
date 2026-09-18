package com.traderpink.ai;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

public class MarketAnalysisView extends LinearLayout {

    private final TextView analysisText;

    public MarketAnalysisView(Context context) {

        super(context);

        setOrientation(VERTICAL);

        setPadding(
                20,
                20,
                20,
                20
        );

        setBackgroundColor(
                Color.rgb(20, 24, 30)
        );

        analysisText =
                new TextView(context);

        analysisText.setTextSize(16);

        analysisText.setTextColor(
                Color.WHITE
        );

        analysisText.setGravity(
                Gravity.START
        );

        addView(
                analysisText,
                new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT
                )
        );

        analysisText.setText(
                "🤖 AI ANALYSIS\n\n" +
                "Loading analysis..."
        );
    }

    public void setSignal(JSONObject json) {

        String signal =
                json.optString(
                        "signal",
                        "WAIT"
                );

        String confidence =
                json.optString(
                        "confidence",
                        "--"
                );

        String trend =
                json.optString(
                        "trend",
                        "--"
                );

        String support =
                json.optString(
                        "support",
                        "--"
                );

        String resistance =
                json.optString(
                        "resistance",
                        "--"
                );

        String structure =
                json.optString(
                        "structure",
                        "--"
                );

        String liquidity =
                json.optString(
                        "liquidity",
                        "--"
                );

        String fvg =
                json.optString(
                        "fvg",
                        "--"
                );

        String candlePattern =
                json.optString(
                        "candle_pattern",
                        "--"
                );

        String message =
                json.optString(
                        "message",
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

        String signalIcon = "🟡";

        if ("BUY".equalsIgnoreCase(signal)) {

            signalIcon = "🟢";

        } else if ("SELL".equalsIgnoreCase(signal)) {

            signalIcon = "🔴";
        }

        String display =

                "🤖 AI ANALYSIS\n\n" +

                signalIcon +
                " " +
                signal +
                "\n\n" +

                "Confidence    " +
                confidence +
                "\n" +

                "Trend         " +
                trend +
                "\n\n" +

                "Support       " +
                support +
                "\n" +

                "Resistance    " +
                resistance +
                "\n\n" +

                "Structure     " +
                structure +
                "\n" +

                "Liquidity     " +
                liquidity +
                "\n" +

                "FVG           " +
                fvg +
                "\n" +

                "Candle Pattern\n" +
                candlePattern +
                "\n\n" +

                "TIMEFRAME CONFIRMATION\n\n" +

                "1M    " +
                m1 +
                "\n" +

                "5M    " +
                m5 +
                "\n" +

                "15M   " +
                m15 +
                "\n" +

                "1H    " +
                h1 +
                "\n\n" +

                "MESSAGE\n" +
                message;

        analysisText.setText(
                display
        );
    }
}
