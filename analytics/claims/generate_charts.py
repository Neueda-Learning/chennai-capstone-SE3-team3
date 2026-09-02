from pathlib import Path

import pandas as pd
import plotly.graph_objects as go
from plotly.offline import plot


# ============================================================
# CONFIGURATION
# ============================================================

INPUT_FILE = Path(
    "analytics/output/candles/AAPL.csv"
)

OUTPUT_DIR = Path("analytics/claims/charts")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# Extract ticker from the input filename.
# AAPL.csv -> AAPL
# RELIANCE.NS.csv -> RELIANCE.NS
TICKER = INPUT_FILE.stem

# Full company names used in chart titles.
COMPANY_NAMES = {
    "AAPL": "Apple Inc.",
    "RELIANCE.NS": "Reliance Industries",
    "INFY.NS": "Infosys Ltd",
    "TATASTEEL.BO": "Tata Steel Ltd",
    "MSFT": "Microsoft Corporation",
    "GOOGL": "Alphabet Inc.",
    "AMZN": "Amazon.com Inc.",
    "TSLA": "Tesla Inc.",
    "META": "Meta Platforms Inc.",
}

COMPANY_NAME = COMPANY_NAMES.get(TICKER, TICKER)

REPORT_FILE = OUTPUT_DIR / f"report_{TICKER}.html"
FINDINGS_FILE = OUTPUT_DIR / f"candidate_findings_{TICKER}.csv"

IMAGE_DIR = OUTPUT_DIR / f"images_{TICKER}"
IMAGE_DIR.mkdir(parents=True, exist_ok=True)


# ============================================================
# 1. LOAD TRANSFORMED CSV
# ============================================================

print("=" * 70)
print("LOADING DATA")
print("=" * 70)

print(f"Input file: {INPUT_FILE}")

if not INPUT_FILE.exists():
    raise FileNotFoundError(
        f"Input file does not exist: {INPUT_FILE}"
    )

df = pd.read_csv(INPUT_FILE)


# ============================================================
# 2. CHECK REQUIRED COLUMNS
# ============================================================

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


# ============================================================
# 3. CLEAN DATA TYPES
# ============================================================

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
# 4. DATA QUALITY CHECKS
# ============================================================

print()
print("=" * 70)
print("DATA QUALITY CHECKS")
print("=" * 70)

print(f"Company: {COMPANY_NAME}")
print(f"Ticker: {TICKER}")
print(f"Rows: {len(df)}")

print(
    f"Date range: "
    f"{df['date'].min().date()} "
    f"to "
    f"{df['date'].max().date()}"
)

print()
print("Missing values:")
print(df.isna().sum())

duplicate_dates = df[
    df["date"].duplicated(keep=False)
]

print(f"\nDuplicate dates: {len(duplicate_dates)}")

invalid_ohlc = df[
    (df["high"] < df["open"])
    | (df["high"] < df["close"])
    | (df["low"] > df["open"])
    | (df["low"] > df["close"])
]

print(f"Invalid OHLC rows: {len(invalid_ohlc)}")

synthetic_count = (
    df["synthetic"]
    .fillna(False)
    .sum()
)

print(f"Synthetic rows: {synthetic_count}")

missing_volume_count = df["volume"].isna().sum()

print(
    f"Rows with missing volume: "
    f"{missing_volume_count}"
)


# ============================================================
# 5. CREATE DERIVED METRICS
# ============================================================

# Open-to-close percentage movement.
df["daily_change_pct"] = (
    (df["close"] - df["open"])
    / df["open"]
    * 100
)

# Absolute size of the open-to-close movement.
df["absolute_daily_change_pct"] = (
    df["daily_change_pct"].abs()
)

# High-to-low intraday range as a percentage of opening price.
df["intraday_range_pct"] = (
    (df["high"] - df["low"])
    / df["open"]
    * 100
)

# Closing price change relative to the first closing price.
first_close = df["close"].iloc[0]

df["cumulative_return_pct"] = (
    (df["close"] / first_close) - 1
) * 100

df["month"] = (
    df["date"]
    .dt.to_period("M")
    .astype(str)
)


# ============================================================
# 6. DATA USED FOR VOLUME ANALYSIS
# ============================================================

# Volume-based analysis excludes:
# - missing volume
# - synthetic observations
volume_df = df[
    df["volume"].notna()
    & (~df["synthetic"].fillna(False))
].copy()

print()
print(
    f"Valid volume rows: "
    f"{len(volume_df)}"
)


# ============================================================
# 7. CANDIDATE FINDINGS
# ============================================================

findings = []


# Largest daily gain.
best_day = df.loc[
    df["daily_change_pct"].idxmax()
]

findings.append({
    "candidate": "Largest daily gain",
    "date": best_day["date"].date(),
    "value": best_day["daily_change_pct"],
    "unit": "%"
})


# Largest daily decline.
worst_day = df.loc[
    df["daily_change_pct"].idxmin()
]

findings.append({
    "candidate": "Largest daily decline",
    "date": worst_day["date"].date(),
    "value": worst_day["daily_change_pct"],
    "unit": "%"
})


# Largest intraday range.
largest_range_day = df.loc[
    df["intraday_range_pct"].idxmax()
]

findings.append({
    "candidate": "Largest intraday range",
    "date": largest_range_day["date"].date(),
    "value": largest_range_day["intraday_range_pct"],
    "unit": "%"
})


# Smallest intraday range.
smallest_range_day = df.loc[
    df["intraday_range_pct"].idxmin()
]

findings.append({
    "candidate": "Smallest intraday range",
    "date": smallest_range_day["date"].date(),
    "value": smallest_range_day["intraday_range_pct"],
    "unit": "%"
})


# Highest-volume day.
top_volume = pd.DataFrame()

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


# Top 10 volume concentration.
if not volume_df.empty:

    top_n = min(10, len(volume_df))

    top_volume = (
        volume_df
        .nlargest(top_n, "volume")
    )

    total_volume = volume_df["volume"].sum()
    top_volume_total = top_volume["volume"].sum()

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
        "candidate": f"Top {top_n} days volume share",
        "date": "",
        "value": top_volume_share,
        "unit": "%"
    })

    findings.append({
        "candidate": f"Top {top_n} days share of valid-volume days",
        "date": "",
        "value": top_days_share,
        "unit": "%"
    })


# High-volume days versus remaining days.
if not volume_df.empty and not top_volume.empty:

    top_volume_average_move = (
        top_volume["absolute_daily_change_pct"].mean()
    )

    remaining = volume_df.drop(top_volume.index)

    if not remaining.empty:

        remaining_average_move = (
            remaining["absolute_daily_change_pct"].mean()
        )

        findings.append({
            "candidate": "Top-volume average absolute movement",
            "date": "",
            "value": top_volume_average_move,
            "unit": "%"
        })

        findings.append({
            "candidate": "Remaining-days average absolute movement",
            "date": "",
            "value": remaining_average_move,
            "unit": "%"
        })

        if remaining_average_move != 0:

            movement_ratio = (
                top_volume_average_move
                / remaining_average_move
            )

            movement_difference_pct = (
                (
                    top_volume_average_move
                    / remaining_average_move
                )
                - 1
            ) * 100

            findings.append({
                "candidate": "High-volume movement ratio",
                "date": "",
                "value": movement_ratio,
                "unit": "x"
            })

            findings.append({
                "candidate": "High-volume movement difference",
                "date": "",
                "value": movement_difference_pct,
                "unit": "%"
            })


# Volume / price movement correlation.
if len(volume_df) >= 2:

    correlation = (
        volume_df[
            [
                "volume",
                "absolute_daily_change_pct"
            ]
        ]
        .corr()
        .iloc[0, 1]
    )

    findings.append({
        "candidate": "Volume / price movement correlation",
        "date": "",
        "value": correlation,
        "unit": "correlation"
    })


# Positive / negative / flat days.
positive_days = df[
    df["close"] > df["open"]
]

negative_days = df[
    df["close"] < df["open"]
]

flat_days = df[
    df["close"] == df["open"]
]

findings.append({
    "candidate": "Number of positive days",
    "date": "",
    "value": len(positive_days),
    "unit": "days"
})

findings.append({
    "candidate": "Number of negative days",
    "date": "",
    "value": len(negative_days),
    "unit": "days"
})

findings.append({
    "candidate": "Number of flat days",
    "date": "",
    "value": len(flat_days),
    "unit": "days"
})


if not positive_days.empty:

    findings.append({
        "candidate": "Average positive-day movement",
        "date": "",
        "value": positive_days["daily_change_pct"].mean(),
        "unit": "%"
    })


if not negative_days.empty:

    findings.append({
        "candidate": "Average absolute negative-day movement",
        "date": "",
        "value": negative_days["daily_change_pct"].abs().mean(),
        "unit": "%"
    })


# ============================================================
# 8. PRINT FINDINGS
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
# 9. CHART 1 - CLOSING PRICE
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
        f"{COMPANY_NAME}: "
        "Closing price over time"
    ),
    xaxis_title="Trading date",
    yaxis_title="Closing price (local currency per share)",
    template="plotly_white",
    height=600
)


# ============================================================
# 10. CHART 2 - DAILY PRICE MOVEMENT
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
        f"{COMPANY_NAME}: "
        "Daily price movement"
    ),
    xaxis_title="Trading date",
    yaxis_title="Change from open to close (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 11. CHART 3 - DAILY TRADING VOLUME
# ============================================================

fig3 = go.Figure()

fig3.add_trace(
    go.Bar(
        x=volume_df["date"],
        y=volume_df["volume"],
        hovertemplate=(
            "Date: %{x|%d %b %Y}"
            "<br>Trading volume: %{y:,.0f} shares"
            "<extra></extra>"
        )
    )
)

fig3.update_layout(
    title=(
        f"{COMPANY_NAME}: "
        "Daily trading volume"
    ),
    xaxis_title="Trading date",
    yaxis_title="Trading volume (shares)",
    template="plotly_white",
    height=600
)


# ============================================================
# 12. CHART 4 - INTRADAY VOLATILITY
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
        f"{COMPANY_NAME}: "
        "Daily intraday price range"
    ),
    xaxis_title="Trading date",
    yaxis_title="Intraday price range (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 13. CHART 5 - VOLUME VS DAILY PRICE MOVEMENT
# ============================================================

fig5 = go.Figure()

fig5.add_trace(
    go.Scatter(
        x=volume_df["absolute_daily_change_pct"],
        y=volume_df["volume"],
        mode="markers",
        text=volume_df["date"].dt.strftime("%d %b %Y"),
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
        f"{COMPANY_NAME}: "
        "Trading volume versus daily price movement"
    ),
    xaxis_title="Absolute daily price movement (%)",
    yaxis_title="Trading volume (shares)",
    template="plotly_white",
    height=600
)


# ============================================================
# 14. CHART 6 - VOLUME CONCENTRATION
# ============================================================

ranked = (
    volume_df
    .sort_values("volume", ascending=False)
    .reset_index(drop=True)
)

ranked["volume_rank"] = ranked.index + 1

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
        y=ranked["cumulative_volume_share"],
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
        f"{COMPANY_NAME}: "
        "Concentration of trading volume"
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
# 15. CHART 7 - CLOSED HIGHER VS CLOSED LOWER
# ============================================================

# This chart uses ALL available OHLC rows.
# It does not depend on volume because the question is:
# "Did the stock close higher or lower than it opened?"

closed_higher_days = df[
    df["close"] > df["open"]
]

closed_lower_days = df[
    df["close"] < df["open"]
]

closed_unchanged_days = df[
    df["close"] == df["open"]
]

higher_count = len(closed_higher_days)
lower_count = len(closed_lower_days)
unchanged_count = len(closed_unchanged_days)

total_days = (
    higher_count
    + lower_count
    + unchanged_count
)

higher_percentage = (
    higher_count / total_days * 100
    if total_days > 0
    else 0
)

lower_percentage = (
    lower_count / total_days * 100
    if total_days > 0
    else 0
)

unchanged_percentage = (
    unchanged_count / total_days * 100
    if total_days > 0
    else 0
)

direction_counts = pd.DataFrame({
    "direction": [
        "Closed higher",
        "Closed lower",
        "Closed unchanged"
    ],
    "days": [
        higher_count,
        lower_count,
        unchanged_count
    ],
    "percentage": [
        higher_percentage,
        lower_percentage,
        unchanged_percentage
    ]
})

fig7 = go.Figure()

fig7.add_trace(
    go.Bar(
        x=direction_counts["direction"],
        y=direction_counts["days"],
        text=[
            f"{days} days ({percentage:.1f}%)"
            for days, percentage
            in zip(
                direction_counts["days"],
                direction_counts["percentage"]
            )
        ],
        textposition="auto",
        hovertemplate=(
            "%{x}"
            "<br>Trading days: %{y}"
            "<br>Percentage of days: %{customdata:.1f}%"
            "<extra></extra>"
        ),
        customdata=direction_counts["percentage"]
    )
)

fig7.update_layout(
    title=(
        f"{COMPANY_NAME}: "
        "Most trading days closed above the opening price"
    ),
    xaxis_title="Daily closing direction",
    yaxis_title="Number of trading days",
    template="plotly_white",
    height=600
)


# ============================================================
# 16. CHART 8 - VOLUME VS INTRADAY VOLATILITY
# ============================================================

fig8 = go.Figure()

fig8.add_trace(
    go.Scatter(
        x=volume_df["intraday_range_pct"],
        y=volume_df["volume"],
        mode="markers",
        text=volume_df["date"].dt.strftime("%d %b %Y"),
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
        f"{COMPANY_NAME}: "
        "Trading volume versus intraday volatility"
    ),
    xaxis_title="Intraday price range (%)",
    yaxis_title="Trading volume (shares)",
    template="plotly_white",
    height=600
)


# ============================================================
# 17. CHART 9 - EXTREME DAILY MOVEMENTS
# ============================================================

number_of_extremes = min(5, len(df))

extreme_days = pd.concat([
    df.nlargest(
        number_of_extremes,
        "daily_change_pct"
    ),
    df.nsmallest(
        number_of_extremes,
        "daily_change_pct"
    )
])

extreme_days = (
    extreme_days
    .drop_duplicates(subset=["date"])
    .sort_values("daily_change_pct")
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
        f"{COMPANY_NAME}: "
        "Largest daily price movements"
    ),
    xaxis_title="Trading date",
    yaxis_title="Daily price change (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 18. CHART 10 - CUMULATIVE PRICE CHANGE
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
        f"{COMPANY_NAME}: "
        "Cumulative price change "
        "from the start of the period"
    ),
    xaxis_title="Trading date",
    yaxis_title="Cumulative price change (%)",
    template="plotly_white",
    height=600
)


# ============================================================
# 19. CHART TITLES AND EXPLANATIONS
# ============================================================

charts = [

    (
        "closing-price",
        "Closing Price",
        (
            "This chart shows the closing share price "
            f"of {COMPANY_NAME} for each trading day "
            "in the analysis period. It helps identify "
            "the overall direction and magnitude of "
            "price changes over time."
        ),
        fig1
    ),

    (
        "daily-price-movement",
        "Daily Price Movement",
        (
            "This chart shows the percentage change "
            "from the opening price to the closing price "
            "for each trading day. Positive values mean "
            "the share price finished the day above its "
            "opening price, while negative values mean "
            "it finished below its opening price."
        ),
        fig2
    ),

    (
        "daily-volume",
        "Daily Trading Volume",
        (
            "This chart shows the number of shares "
            f"recorded as traded for {COMPANY_NAME} "
            "on each valid-volume trading day. It helps "
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
            "each trading day, expressed as a percentage "
            "of the opening price. A larger percentage "
            "means the share price moved through a wider "
            "range during that day."
        ),
        fig4
    ),

    (
        "volume-price-movement",
        "Trading Volume Versus Daily Price Movement",
        (
            "This chart compares the number of shares "
            f"traded for {COMPANY_NAME} with the absolute "
            "size of the open-to-close price movement. "
            "Each point represents one trading day. "
            "It helps assess whether days with greater "
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
            "small number of unusually busy days."
        ),
        fig6
    ),

    (
        "positive-negative-days",
        "Closed Higher Versus Closed Lower",
        (
            "This chart compares the number of trading "
            f"days on which {COMPANY_NAME} closed above "
            "its opening price with the number of days "
            "on which it closed below its opening price. "
            "The percentages show what proportion of "
            "the trading period each direction represents."
        ),
        fig7
    ),

    (
        "volume-volatility",
        "Trading Volume Versus Intraday Volatility",
        (
            "This chart compares trading volume with "
            "the size of the intraday price range. "
            "Each point represents one trading day. "
            "It helps identify whether higher trading "
            "activity occurred on days when the share "
            "price moved through a wider intraday range."
        ),
        fig8
    ),

    (
        "extreme-movements",
        "Largest Daily Price Movements",
        (
            "This chart displays the trading days with "
            "the largest upward and downward movements "
            f"for {COMPANY_NAME}, measured from opening "
            "price to closing price. It highlights the "
            "most significant daily price changes in "
            "the analysis period."
        ),
        fig9
    ),

    (
        "cumulative-return",
        "Cumulative Price Change From the Start of the Period",
        (
            "This chart shows how the closing price "
            f"of {COMPANY_NAME} changed relative to "
            "the first recorded closing price in the "
            "dataset. It provides a view of the overall "
            "price change across the analysis period."
        ),
        fig10
    ),
]


# ============================================================
# 20. SAVE EACH CHART AS PNG
# ============================================================

print()
print("=" * 70)
print("SAVING CHART IMAGES")
print("=" * 70)

for (
    anchor,
    title,
    description,
    figure
) in charts:

    image_file = (
        IMAGE_DIR
        / f"{anchor}.png"
    )

    figure.write_image(
        image_file,
        width=1400,
        height=800,
        scale=2
    )

    print(f"Saved: {image_file}")


# ============================================================
# 21. CREATE SELF-CONTAINED HTML REPORT
# ============================================================

print()
print("=" * 70)
print("CREATING HTML REPORT")
print("=" * 70)

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
            True
            if index == 0
            else False
        ),
        output_type="div"
    )

    html_sections.append(
        f"""
        <section id="{anchor}">

            <h2>{title}</h2>

            <p class="chart-description">
                {description}
            </p>

            {chart_html}

        </section>
        """
    )


# ============================================================
# 22. HTML REPORT
# ============================================================

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
    font-family:
        Arial,
        Helvetica,
        sans-serif;

    max-width:
        1200px;

    margin:
        0 auto;

    padding:
        30px;

    line-height:
        1.5;
}}

h1 {{
    margin-bottom:
        10px;
}}

h2 {{
    margin-bottom:
        10px;
}}

section {{
    margin-top:
        60px;

    scroll-margin-top:
        20px;
}}

.chart-description {{
    font-size:
        16px;

    color:
        #444;

    margin-bottom:
        20px;

    max-width:
        1000px;
}}

.note {{
    padding:
        15px;

    background:
        #f4f4f4;

    border-radius:
        5px;

    margin-bottom:
        30px;
}}

.summary {{
    padding:
        15px;

    border:
        1px solid #ddd;

    border-radius:
        5px;

    margin-bottom:
        30px;
}}

a {{
    color:
        #0645ad;
}}

</style>

</head>

<body>

<h1>
{COMPANY_NAME} Analytics Report
</h1>

<div class="summary">

<p>
<strong>Purpose:</strong>

This report contains candidate charts and findings
generated from the transformed market-data CSV.
The charts are exploratory and are intended to help
identify business claims supported by the data.
</p>

</div>

<div class="note">

<strong>Data treatment:</strong>

<p>
Synthetic observations and rows without recorded
volume are excluded from volume-based calculations.
OHLC-derived measures use the available opening,
high, low and closing prices.
</p>

<p>
The analysis period is:

<strong>{df["date"].min().date()}</strong>

to

<strong>{df["date"].max().date()}</strong>.
</p>

<p>
Total observations:

<strong>{len(df)}</strong>.
</p>

<p>
Valid observations used for volume analysis:

<strong>{len(volume_df)}</strong>.
</p>

</div>

<h2>
Candidate Charts
</h2>

<ul>

{
"".join(
    f'<li><a href="#{anchor}">{title}</a></li>'
    for anchor, title, _, _ in charts
)
}

</ul>

{"".join(html_sections)}

</body>

</html>
"""


# ============================================================
# 23. WRITE HTML REPORT
# ============================================================

REPORT_FILE.write_text(
    report,
    encoding="utf-8"
)


# ============================================================
# 24. SAVE CANDIDATE FINDINGS
# ============================================================

findings_df = pd.DataFrame(
    findings
)

findings_df.to_csv(
    FINDINGS_FILE,
    index=False
)


# ============================================================
# 25. FINAL OUTPUT
# ============================================================

print()
print("=" * 70)
print("OUTPUT")
print("=" * 70)

print("HTML report:")
print(f"  {REPORT_FILE}")

print()
print("Candidate findings:")
print(f"  {FINDINGS_FILE}")

print()
print("Chart images:")
print(f"  {IMAGE_DIR}")

print()
print("Charts generated:")

for (
    anchor,
    title,
    description,
    figure
) in charts:

    print(f"  {title}")
    print(
        f"    HTML: "
        f"report_{TICKER}.html#{anchor}"
    )
    print(
        f"    PNG: "
        f"{IMAGE_DIR / f'{anchor}.png'}"
    )

print()
print("Analysis complete.")
