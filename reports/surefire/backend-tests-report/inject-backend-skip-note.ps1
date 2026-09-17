$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$reportHtml = Join-Path $scriptDir 'surefire.html'
$summaryMarker = 'id="backend-skip-note"'
$linkMarker = 'id="backend-skip-note-link"'

if (-not (Test-Path -LiteralPath $reportHtml)) {
    Write-Error "surefire.html not found at $reportHtml"
    exit 1
}

$content = Get-Content -LiteralPath $reportHtml -Raw
if ($content.Contains($summaryMarker) -and $content.Contains($linkMarker)) {
    Write-Host 'Skip explanation and link already exist. No changes made.'
    exit 0
}

$summarySnippet = @'
<section id="backend-skip-note" class="skip-explanation-card">
<h4 class="skip-explanation-title">Why BackendApplicationTests is skipped</h4>
<p class="skip-explanation-text">BackendApplicationTests is intentionally skipped because it is a minimal context-load smoke test, while container-backed integration and characterization tests already validate real runtime behavior against PostgreSQL, security filters, and API flows. Keeping this test disabled avoids redundant execution noise while preserving stronger, behavior-focused coverage.</p>
</section>
'@

$linkSnippet = @'
<p id="backend-skip-note-link" class="skip-explanation-link"><a href="#backend-skip-note">Why this test is skipped</a></p>
'@

if (-not $content.Contains($summaryMarker)) {
    $summaryAnchor = '<p>Note: failures are anticipated and checked for with assertions while errors are unanticipated.</p><br />'
    if (-not $content.Contains($summaryAnchor)) {
        Write-Error 'Could not find Summary note anchor in surefire.html'
        exit 2
    }
    $content = $content.Replace($summaryAnchor, "$summaryAnchor`r`n$summarySnippet")
}

if (-not $content.Contains($linkMarker)) {
    $testAnchor = '<h3>BackendApplicationTests</h3>'
    if (-not $content.Contains($testAnchor)) {
        Write-Error 'Could not find BackendApplicationTests section in surefire.html'
        exit 2
    }
    $content = $content.Replace($testAnchor, "$testAnchor`r`n$linkSnippet")
}

Set-Content -LiteralPath $reportHtml -Value $content -Encoding UTF8

Write-Host 'Inserted skip explanation under Summary and link under BackendApplicationTests in surefire.html'
