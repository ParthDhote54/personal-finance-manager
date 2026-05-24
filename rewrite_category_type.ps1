$ErrorActionPreference = "Stop"

$files = Get-ChildItem -Path "p:\Personal Finance Manager\personal-finance-manager\src\main\java\com\personal\finance\manager\category" -Recurse -Filter "*.java"
$testFiles = Get-ChildItem -Path "p:\Personal Finance Manager\personal-finance-manager\src\test\java\com\personal\finance\manager\category" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue

$allFiles = $files + $testFiles

foreach ($file in $allFiles) {
    if ($file -ne $null) {
        $content = Get-Content -Path $file.FullName -Raw
        $content = $content.Replace("com.personal.finance.manager.transaction.entity.TransactionType", "com.personal.finance.manager.category.entity.CategoryType")
        $content = $content.Replace("TransactionType", "CategoryType")
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}
Write-Host "Category module rewritten to use CategoryType."
