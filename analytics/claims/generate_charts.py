from pathlib import Path

import pandas as pd
import plotly.graph_objects as go
from plotly.offline import plot


# ============================================================
# CONFIGURATION
# ============================================================

INPUT_FILE = Path(
    "analytics/output/candles/RELIANCE.NS.csv"
)

OUTPUT_DIR = Path("analytics/claims/charts")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

REPORT_FILE = OUTPUT_DIR / "report.html"
FINDINGS_FILE = OUTPUT_DIR / "candidate_findings.csv"

COMPANY_NAME = "Reliance Industries"


# ============================================================
# 1. LOAD TRANSFORMED CSV
# ============================================================

df = pd.read_csv(INPUT_FILE)

required_columns = {
    "date",
    "open",
    "high",
    "low",
    "close",
    "adjclose",
    "volume",
    "synthetic",
}

missing = required_columns - set(df.columns)

if missing:
    raise ValueError(
        f"Missing required columns: {sorted(missing)}"
    )


df["date"] = pd.to_datetime(
    df["date"],
    errors="coerce"
)

numeric_columns = [
    "open",
    "high",
    "low",
    "close",
    "adjclose",
    "volume",
]

for column in numeric_columns:
    df[column] = pd.to_numeric(
        df[column],
        errors="coerce"
    )

df = (
    df
    .sort_values("date")
    .reset_index(drop=True)
)


# ============================================================
# 2. DATA QUALITY CHECKS
# ============================================================

print("=" * 70)
print("DATA QUALITY CHECKS")
print("=" * 70)

print(f"Rows: {len(df)}")

print(
    f"Date range: "
    f"{df['date'].min().date()} "
    f"to "
    f"{df['date'].max().date()}"
)

print("\nMissing values:")
print(df.isna().sum())

duplicate_dates = df[
    df["date"].duplicated(keep=False)
]

print(
    f"\nDuplicate dates: "
    f"{len(duplicate_dates)}"
)

invalid_ohlc = df[
    (df["high"] < df["open"])
    | (df["high"] < df["close"])
    | (df["low"] > df["open"])
    | (df["low"] > df["close"])
]

print(
    f"Invalid OHLC rows: "
    f"{len(invalid_ohlc)}"
)

synthetic_count = (
    df["synthetic"]
    .fillna(False)
    .sum()
)

print(
    f"Synthetic rows: "
    f"{synthetic_count}"
)


# ============================================================
# 3. CREATE DERIVED METRICS
# ============================================================

# Percentage change from opening price to closing price.
df["daily_change_pct"] = (
    (df["close"] - df["open"])
    / df["open"]
    * 100
)

# Absolute movement regardless of direction.
df["absolute_daily_change_pct"] = (
    df["daily_change_pct"].abs()
)

# Difference between high and low as a percentage
# of the opening price.
df["intraday_range_pct"] = (
    (df["high"] - df["low"])
    / df["open"]
    * 100
)

# Price change relative to the first closing price.
df["cumulative_return_pct"] = (
    (
        df["close"]
        / df["close"].iloc[0]
    )
    - 1
) * 100

# Month for any future grouping/analysis.
df["month"] = (
    df["date"]
    .dt.to_period("M")
    .astype(str)
)


# ============================================================
# 4. VALID VOLUME DATA
# ============================================================

# Synthetic observations and observations without volume
# are excluded from volume-based analysis.

volume_df = df[
    df["volume"].notna()
    & (~df["synthetic"].fillna(False))
].copy()


# ============================================================
# 5. CANDIDATE FINDINGS
# ============================================================

findings = []


# ------------------------------------------------------------
# Finding 1: Largest daily gain
# ------------------------------------------------------------

best_day = df.loc[
    df["daily_change_pct"].idxmax()
]

findings.append({
    "candidate": "Largest daily gain",
    "date": best_day["date"].date(),
    "value": best_day["daily_change_pct"],
    "unit": "%"
})


# ------------------------------------------------------------
# Finding 2: Largest daily decline
# ------------------------------------------------------------

worst_day = df.loc[
    df["daily_change_pct"].idxmin()
]

findings.append({
    "candidate": "Largest daily decline",
    "date": worst_day["date"].date(),
    "value": worst_day["daily_change_pct"],
    "unit": "%"
})


# ------------------------------------------------------------
# Finding 3: Largest intraday range
# ------------------------------------------------------------

largest_range_day = df.loc[
    df["intraday_range_pct"].idxmax()
]

findings.append({
    "candidate": "Largest intraday range",
    "date": largest_range_day["date"].date(),
    "value": largest_range_day["intraday_range_pct"],
    "unit": "%"
})


# ------------------------------------------------------------
# Finding 4: Smallest intraday range
# ------------------------------------------------------------

smallest_range_day = df.loc[
    df["intraday_range_pct"].idxmin()
]

findings.append({
    "candidate": "Smallest intraday range",
    "date": smallest_range_day["date"].date(),
    "value": smallest_range_day["intraday_range_pct"],
    "unit": "%"
})


# ------------------------------------------------------------
# Finding 5: Highest volume day
# ------------------------------------------------------------

if not volume_df.empty:

    highest_volume_day = volume_df.loc[
        volume_df["volume"].idxmax()
    ]

    findings.append({
        "candidate": "Highest volume day",
        "date": highest_volume_day["date"].date(),
        "value": highest_volume_day["volume"],
        "unit": "shares"
    })


# ------------------------------------------------------------
# Finding 6: Volume concentration
# ------------------------------------------------------------

if not volume_df.empty:

    top_n = min(
        10,
        len(volume_df)
    )

    top_volume = volume_df.nlargest(
        top_n,
        "volume"
    )

    total_volume = volume_df["volume"].sum()

    top_volume_total = top_volume[
        "volume"
    ].sum()

    top_volume_share = (
        top_volume_total
        / total_volume
        * 100
    )

    top_days_share = (
        top_n
        / len(volume_df)
        * 100
    )

    findings.append({
        "candidate":
            f"Top {top_n} days volume share",
        "date":
            "",
        "value":
            top_volume_share,
        "unit":
            "%"
    })

    findings.append({
        "candidate":
            f"Top {top_n} days share of valid-volume days",
        "date":
            "",
        "value":
            top_days_share,
        "unit":
            "%"
    })


# ------------------------------------------------------------
# Finding 7: High volume vs price movement
# ------------------------------------------------------------

if len(volume_df) >= 2:

    top_volume_average_move = (
        top_volume[
            "absolute_daily_change_pct"
        ].mean()
    )

    remaining = volume_df.drop(
        top_volume.index
    )

    if not remaining.empty:

        remaining_average_move = (
            remaining[
                "absolute_daily_change_pct"
            ].mean()
        )

        findings.append({
            "candidate":
                "Top-volume average absolute movement",
            "date":
                "",
            "value":
                top_volume_average_move,
            "unit":
                "%"
        })

        findings.append({
            "candidate":
                "Remaining-days average absolute movement",
            "date":
                "",
            "value":
                remaining_average_move,
            "unit":
                "%"
        })

        if remaining_average_move != 0:

            findings.append({
                "candidate":
                    "High-volume movement ratio",
                "date":
                    "",
                "value":
                    (
                        top_volume_average_move
                        / remaining_average_move
                    ),
                "unit":
                    "x"
            })


# ------------------------------------------------------------
# Finding 8: Volume / price movement correlation
# ------------------------------------------------------------

if len(volume_df) >= 2:

    correlation = volume_df[
        [
            "volume",
            "absolute_daily_change_pct"
        ]
    ].corr().iloc[0, 1]

    findings.append({
        "candidate":
            "Volume / price movement correlation",
        "date":
            "",
        "value":
            correlation,
        "unit":
            "correlation"
    })


# ------------------------------------------------------------
# Finding 9: Positive and negative days
# ------------------------------------------------------------

positive_days = volume_df[
    volume_df["daily_change_pct"] > 0
]

negative_days = volume_df[
    volume_df["daily_change_pct"] < 0
]

flat_days = volume_df[
    volume_df["daily_change_pct"] == 0
]

findings.append({
    "candidate":
        "Number of positive days",
    "date":
        "",
    "value":
        len(positive_days),
    "unit":
        "days"
})

findings.append({
    "candidate":
        "Number of negative days",
    "date":
        "",
    "value":
        len(negative_days),
    "unit":
        "days"
})

findings.append({
    "candidate":
        "Number of flat days",
    "date":
        "",
    "value":
        len(flat_days),
    "unit":
        "days"
})

if not positive_days.empty:

    findings.append({
        "candidate":
            "Average positive-day movement",
        "date":
            "",
        "value":
            positive_days[
                "daily_change_pct"
            ].mean(),
        "unit":
            "%"
    })

if not negative_days.empty:

    findings.append({
        "candidate":
            "Average absolute negative-day movement",
        "date":
            "",
        "value":
            negative_days[
                "daily_change_pct"
            ].abs().mean(),
        "unit":
            "%"
    })


# ============================================================
# 6. PRINT ALL FINDINGS
# ============================================================

print()
print("=" * 70)
print("CANDIDATE FINDINGS")
print("=" * 70)

for finding in findings:

    print(
        f"{finding['candidate']}: "
        f"{finding['value']} "
        f"{finding['unit']}"
    )


# ============================================================
# 7. CHART 1
# CLOSING PRICE
# ============================================================

fig1 = go.Figure()

fig1.add_trace(
    go.Scatter(
        x=df["date"],
        y=df["close"],
        mode="lines+markers",
        hovertemplate=(
            "Date: %{x|%d %b %Y}"
            "<br>Closing price: %{y:.2f}"
            "<extra></extra>"
        )
    )
)

fig1.update_layout(
    title=(
        f"{COMPANY_NAME}: Closing price over time"
    ),
    xaxis_title="Trading date",
    yaxis_title="Closing price (INR per share)",
    template="plotly_white",
    height=600
)


# ============================================================
# 8. CHART 2
# DAILY PRICE MOVEMENT
# ============================================================

fig2 = go.Figure()

fig2.add_trace(
    go.Bar(
        x=df["date"],
        y=df["daily_change_pct"],
        hovertemplate=(
            "Date: %{x|%d %b %Y}"
            "<br>Daily change: %{y:.2f}%"
            "<extra></extra>"
        )
    )
)

fig2.update_layout(
    title=(
        f"{COMPANY_NAME}: Daily price movement"
    ),
    xaxis_title="Trading date",
    yaxis_title="Change from open to close (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 9. CHART 3
# DAILY VOLUME
# ============================================================

fig3 = go.Figure()

fig3.add_trace(
    go.Bar(
        x=volume_df["date"],
        y=volume_df["volume"],
        hovertemplate=(
            "Date: %{x|%d %b %Y}"
            "<br>Volume: %{y:,.0f} shares"
            "<extra></extra>"
        )
    )
)

fig3.update_layout(
    title=(
        f"{COMPANY_NAME}: Daily trading volume"
    ),
    xaxis_title="Trading date",
    yaxis_title="Trading volume (shares)",
    template="plotly_white",
    height=600
)


# ============================================================
# 10. CHART 4
# INTRADAY RANGE
# ============================================================

fig4 = go.Figure()

fig4.add_trace(
    go.Bar(
        x=df["date"],
        y=df["intraday_range_pct"],
        hovertemplate=(
            "Date: %{x|%d %b %Y}"
            "<br>Intraday range: %{y:.2f}%"
            "<extra></extra>"
        )
    )
)

fig4.update_layout(
    title=(
        f"{COMPANY_NAME}: Daily intraday price range"
    ),
    xaxis_title="Trading date",
    yaxis_title="Intraday price range (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 11. CHART 5
# VOLUME VS PRICE MOVEMENT
# ============================================================

fig5 = go.Figure()

fig5.add_trace(
    go.Scatter(
        x=volume_df[
            "absolute_daily_change_pct"
        ],
        y=volume_df["volume"],
        mode="markers",
        text=volume_df[
            "date"
        ].dt.strftime("%d %b %Y"),
        hovertemplate=(
            "Date: %{text}"
            "<br>Absolute price movement: %{x:.2f}%"
            "<br>Trading volume: %{y:,.0f} shares"
            "<extra></extra>"
        )
    )
)

fig5.update_layout(
    title=(
        f"{COMPANY_NAME}: Trading volume versus "
        "daily price movement"
    ),
    xaxis_title="Absolute daily price movement (%)",
    yaxis_title="Trading volume (shares)",
    template="plotly_white",
    height=600
)


# ============================================================
# 12. CHART 6
# VOLUME CONCENTRATION
# ============================================================

ranked = (
    volume_df
    .sort_values(
        "volume",
        ascending=False
    )
    .reset_index(drop=True)
)

ranked["volume_rank"] = (
    ranked.index + 1
)

ranked["cumulative_volume_share"] = (
    ranked["volume"].cumsum()
    / ranked["volume"].sum()
    * 100
)

fig6 = go.Figure()

fig6.add_trace(
    go.Bar(
        x=ranked["volume_rank"],
        y=ranked["volume"],
        name="Daily trading volume",
        hovertemplate=(
            "Volume rank: %{x}"
            "<br>Volume: %{y:,.0f} shares"
            "<extra></extra>"
        )
    )
)

fig6.add_trace(
    go.Scatter(
        x=ranked["volume_rank"],
        y=ranked[
            "cumulative_volume_share"
        ],
        name="Cumulative volume share",
        yaxis="y2",
        hovertemplate=(
            "Volume rank: %{x}"
            "<br>Cumulative volume share: %{y:.2f}%"
            "<extra></extra>"
        )
    )
)

fig6.update_layout(
    title=(
        f"{COMPANY_NAME}: Concentration of trading volume"
    ),
    xaxis_title="Trading days ranked by volume",
    yaxis_title="Daily trading volume (shares)",
    yaxis2=dict(
        title="Cumulative volume share (%)",
        overlaying="y",
        side="right",
        range=[0, 100]
    ),
    template="plotly_white",
    height=600
)


# ============================================================
# 13. CHART 7
# POSITIVE VS NEGATIVE DAYS
# ============================================================

direction_counts = pd.DataFrame({
    "direction": [
        "Positive",
        "Negative",
        "Flat"
    ],
    "days": [
        len(positive_days),
        len(negative_days),
        len(flat_days)
    ]
})

fig7 = go.Figure()

fig7.add_trace(
    go.Bar(
        x=direction_counts["direction"],
        y=direction_counts["days"],
        hovertemplate=(
            "%{x} days: %{y}"
            "<extra></extra>"
        )
    )
)

fig7.update_layout(
    title=(
        f"{COMPANY_NAME}: Positive versus negative "
        "trading days"
    ),
    xaxis_title="Daily price direction",
    yaxis_title="Number of trading days",
    template="plotly_white",
    height=600
)


# ============================================================
# 14. CHART 8
# VOLUME VS INTRADAY VOLATILITY
# ============================================================

fig8 = go.Figure()

fig8.add_trace(
    go.Scatter(
        x=volume_df[
            "intraday_range_pct"
        ],
        y=volume_df["volume"],
        mode="markers",
        text=volume_df[
            "date"
        ].dt.strftime("%d %b %Y"),
        hovertemplate=(
            "Date: %{text}"
            "<br>Intraday range: %{x:.2f}%"
            "<br>Trading volume: %{y:,.0f} shares"
            "<extra></extra>"
        )
    )
)

fig8.update_layout(
    title=(
        f"{COMPANY_NAME}: Trading volume versus "
        "intraday volatility"
    ),
    xaxis_title="Intraday price range (%)",
    yaxis_title="Trading volume (shares)",
    template="plotly_white",
    height=600
)


# ============================================================
# 15. CHART 9
# EXTREME DAILY MOVEMENTS
# ============================================================

extreme_days = pd.concat([
    df.nlargest(
        min(5, len(df)),
        "daily_change_pct"
    ),
    df.nsmallest(
        min(5, len(df)),
        "daily_change_pct"
    )
]).drop_duplicates(
    subset=["date"]
).sort_values(
    "daily_change_pct"
)

fig9 = go.Figure()

fig9.add_trace(
    go.Bar(
        x=extreme_days["date"],
        y=extreme_days["daily_change_pct"],
        hovertemplate=(
            "Date: %{x|%d %b %Y}"
            "<br>Daily change: %{y:.2f}%"
            "<extra></extra>"
        )
    )
)

fig9.update_layout(
    title=(
        f"{COMPANY_NAME}: Largest daily price movements"
    ),
    xaxis_title="Trading date",
    yaxis_title="Daily price change (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 16. CHART 10
# CUMULATIVE RETURN
# ============================================================

fig10 = go.Figure()

fig10.add_trace(
    go.Scatter(
        x=df["date"],
        y=df["cumulative_return_pct"],
        mode="lines+markers",
        hovertemplate=(
            "Date: %{x|%d %b %Y}"
            "<br>Cumulative price change: %{y:.2f}%"
            "<extra></extra>"
        )
    )
)

fig10.update_layout(
    title=(
        f"{COMPANY_NAME}: Cumulative price change "
        "from the start of the period"
    ),
    xaxis_title="Trading date",
    yaxis_title="Cumulative price change (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 17. CHART DESCRIPTIONS
# ============================================================

charts = [

    (
        "closing-price",
        "Closing Price",
        (
            "This chart shows the closing share price of "
            "Reliance Industries for each trading day in "
            "the analysis period. It helps identify the "
            "overall direction and magnitude of price "
            "changes over time."
        ),
        fig1
    ),

    (
        "daily-price-movement",
        "Daily Price Movement",
        (
            "This chart shows the percentage change from "
            "the opening price to the closing price for "
            "each trading day. Positive values indicate "
            "an increase during the day, while negative "
            "values indicate a decline."
        ),
        fig2
    ),

    (
        "daily-volume",
        "Daily Trading Volume",
        (
            "This chart shows the number of Reliance "
            "Industries shares recorded as traded on "
            "each valid-volume trading day. It helps "
            "identify days when trading activity was "
            "unusually high or low."
        ),
        fig3
    ),

    (
        "intraday-volatility",
        "Daily Intraday Price Range",
        (
            "This chart measures the difference between "
            "the highest and lowest recorded price during "
            "each trading day as a percentage of the "
            "opening price. A larger percentage indicates "
            "greater intraday price movement."
        ),
        fig4
    ),

    (
        "volume-price-movement",
        "Trading Volume Versus Daily Price Movement",
        (
            "This chart compares trading volume with the "
            "absolute size of the daily price movement. "
            "Each point represents one trading day and "
            "helps assess whether days with greater "
            "trading activity also experienced larger "
            "price movements."
        ),
        fig5
    ),

    (
        "volume-concentration",
        "Trading Volume Concentration",
        (
            "This chart ranks trading days from highest "
            "to lowest trading volume and shows the "
            "cumulative share of total recorded volume. "
            "It helps determine whether a large proportion "
            "of trading activity was concentrated in a "
            "small number of days."
        ),
        fig6
    ),

    (
        "positive-negative-days",
        "Positive Versus Negative Trading Days",
        (
            "This chart compares the number of trading "
            "days on which the closing price was above, "
            "below, or equal to the opening price. It "
            "shows whether upward or downward daily "
            "movements were more common during the "
            "analysis period."
        ),
        fig7
    ),

    (
        "volume-volatility",
        "Trading Volume Versus Intraday Volatility",
        (
            "This chart compares trading volume with the "
            "size of the intraday price range. Each point "
            "represents one trading day and helps identify "
            "whether higher trading activity occurred on "
            "days with wider price movements."
        ),
        fig8
    ),

    (
        "extreme-movements",
        "Largest Daily Price Movements",
        (
            "This chart displays the trading days with "
            "the largest upward and downward movements "
            "from opening to closing price. It highlights "
            "the most significant daily price changes "
            "in the analysis period."
        ),
        fig9
    ),

    (
        "cumulative-return",
        "Cumulative Price Change From the Start of the Period",
        (
            "This chart shows how the closing price changed "
            "relative to the first recorded closing price "
            "in the dataset. It provides a view of the "
            "overall price change accumulated across the "
            "analysis period."
        ),
        fig10
    ),
]


# ============================================================
# 18. CREATE SELF-CONTAINED HTML
# ============================================================

html_sections = []

for index, (
    anchor,
    title,
    description,
    figure
) in enumerate(charts):

    chart_html = plot(
        figure,
        include_plotlyjs=(
            True if index == 0 else False
        ),
        output_type="div",
    )

    html_sections.append(
        f"""
        <section id="{anchor}">

            <h2>
                {title}
            </h2>

            <p class="chart-description">
                {description}
            </p>

            {chart_html}

        </section>
        """
    )


report = f"""
<!DOCTYPE html>

<html lang="en">

<head>

<meta charset="UTF-8">

<title>
{COMPANY_NAME} Analytics Report
</title>

<style>

body {{
    font-family: Arial, sans-serif;
    max-width: 1200px;
    margin: 0 auto;
    padding: 30px;
    line-height: 1.5;
}}

h1 {{
    margin-bottom: 10px;
}}

section {{
    margin-top: 60px;
    scroll-margin-top: 20px;
}}

.chart-description {{
    font-size: 16px;
    color: #444;
    margin-bottom: 20px;
    max-width: 1000px;
}}

.note {{
    padding: 15px;
    background: #f4f4f4;
    border-radius: 5px;
    margin-bottom: 30px;
}}

</style>

</head>

<body>

<h1>
{COMPANY_NAME} Analytics Report
</h1>

<p>
Candidate charts and findings generated from the
transformed market-data CSV.
</p>

<div class="note">

<strong>Data treatment:</strong>

Synthetic observations and rows without recorded
volume are excluded from volume-based calculations.
OHLC-derived measures use the available open, high,
and low values.

</div>

{"".join(html_sections)}

</body>

</html>
"""


REPORT_FILE.write_text(
    report,
    encoding="utf-8"
)


# ============================================================
# 19. SAVE CANDIDATE FINDINGS
# ============================================================

findings_df = pd.DataFrame(findings)

findings_df.to_csv(
    FINDINGS_FILE,
    index=False
)


# ============================================================
# 20. FINAL OUTPUT
# ============================================================

print()
print("=" * 70)
print("OUTPUT")
print("=" * 70)

print(
    f"HTML report: {REPORT_FILE}"
)

print(
    f"Candidate findings: {FINDINGS_FILE}"
)

print()
print("Charts generated:")

for anchor, title, _, _ in charts:

    print(
        f"  {title}"
    )

    print(
        f"    report.html#{anchor}"
    )

print()
print("Analysis complete.")