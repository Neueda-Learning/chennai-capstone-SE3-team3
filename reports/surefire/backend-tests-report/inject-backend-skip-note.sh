#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORT_HTML="$SCRIPT_DIR/surefire.html"
SUMMARY_MARKER='id="backend-skip-note"'
LINK_MARKER='id="backend-skip-note-link"'

if [[ ! -f "$REPORT_HTML" ]]; then
  echo "Error: surefire.html not found at $REPORT_HTML"
  exit 1
fi

HAS_SUMMARY=0
HAS_LINK=0
if grep -q "$SUMMARY_MARKER" "$REPORT_HTML"; then
  HAS_SUMMARY=1
fi
if grep -q "$LINK_MARKER" "$REPORT_HTML"; then
  HAS_LINK=1
fi

if [[ "$HAS_SUMMARY" -eq 1 && "$HAS_LINK" -eq 1 ]]; then
  echo "Skip explanation and link already exist. No changes made."
  exit 0
fi

SUMMARY_SNIPPET=$(cat <<'EOF'
<section id="backend-skip-note" class="skip-explanation-card">
<h4 class="skip-explanation-title">Why BackendApplicationTests is skipped</h4>
<p class="skip-explanation-text">BackendApplicationTests is intentionally skipped because it is a minimal context-load smoke test, while container-backed integration and characterization tests already validate real runtime behavior against PostgreSQL, security filters, and API flows. Keeping this test disabled avoids redundant execution noise while preserving stronger, behavior-focused coverage.</p>
</section>
EOF
)

LINK_SNIPPET=$(cat <<'EOF'
<p id="backend-skip-note-link" class="skip-explanation-link"><a href="#backend-skip-note">Why this test is skipped</a></p>
EOF
)

TMP_FILE="$REPORT_HTML.tmp"

awk -v summarySnippet="$SUMMARY_SNIPPET" -v linkSnippet="$LINK_SNIPPET" -v hasSummary="$HAS_SUMMARY" -v hasLink="$HAS_LINK" '
  {
    print
    if (hasSummary == 0 && $0 ~ /<p>Note: failures are anticipated and checked for with assertions while errors are unanticipated.<\/p><br \/>/) {
      print summarySnippet
      injectedSummary = 1
    }
    if (hasLink == 0 && $0 ~ /<h3>BackendApplicationTests<\/h3>/) {
      print linkSnippet
      injectedLink = 1
    }
  }
  END {
    if (hasSummary == 0 && !injectedSummary) {
      exit 2
    }
    if (hasLink == 0 && !injectedLink) {
      exit 2
    }
  }
' "$REPORT_HTML" > "$TMP_FILE" || {
  status=$?
  rm -f "$TMP_FILE"
  if [[ $status -eq 2 ]]; then
    echo "Error: Could not find Summary note or BackendApplicationTests section in surefire.html"
  fi
  exit $status
}

mv "$TMP_FILE" "$REPORT_HTML"
echo "Inserted skip explanation under Summary and link under BackendApplicationTests in surefire.html"
