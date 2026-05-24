$ErrorActionPreference = "Stop"

$replacements = @{
    "com.personal.finance.manager.dto.LoginRequest" = "com.personal.finance.manager.auth.dto.LoginRequest"
    "com.personal.finance.manager.dto.UserRegistrationRequest" = "com.personal.finance.manager.auth.dto.UserRegistrationRequest"
    "com.personal.finance.manager.dto.UserLoginRequest" = "com.personal.finance.manager.auth.dto.UserLoginRequest"
    "com.personal.finance.manager.controller.AuthController" = "com.personal.finance.manager.auth.controller.AuthController"
    "com.personal.finance.manager.service.AuthService" = "com.personal.finance.manager.auth.service.AuthService"
    
    "com.personal.finance.manager.entity.User" = "com.personal.finance.manager.user.entity.User"
    "com.personal.finance.manager.repository.UserRepository" = "com.personal.finance.manager.user.repository.UserRepository"
    
    "com.personal.finance.manager.entity.Transaction" = "com.personal.finance.manager.transaction.entity.Transaction"
    "com.personal.finance.manager.entity.TransactionType" = "com.personal.finance.manager.transaction.entity.TransactionType"
    "com.personal.finance.manager.dto.TransactionRequest" = "com.personal.finance.manager.transaction.dto.TransactionRequest"
    "com.personal.finance.manager.dto.TransactionResponse" = "com.personal.finance.manager.transaction.dto.TransactionResponse"
    "com.personal.finance.manager.repository.TransactionRepository" = "com.personal.finance.manager.transaction.repository.TransactionRepository"
    "com.personal.finance.manager.service.TransactionService" = "com.personal.finance.manager.transaction.service.TransactionService"
    "com.personal.finance.manager.controller.TransactionController" = "com.personal.finance.manager.transaction.controller.TransactionController"
    
    "com.personal.finance.manager.entity.Category" = "com.personal.finance.manager.category.entity.Category"
    "com.personal.finance.manager.entity.CategoryType" = "com.personal.finance.manager.category.entity.CategoryType"
    "com.personal.finance.manager.dto.CategoryRequest" = "com.personal.finance.manager.category.dto.CategoryRequest"
    "com.personal.finance.manager.dto.CategoryResponse" = "com.personal.finance.manager.category.dto.CategoryResponse"
    "com.personal.finance.manager.dto.CategorySumDTO" = "com.personal.finance.manager.category.dto.CategorySumDTO"
    "com.personal.finance.manager.repository.CategoryRepository" = "com.personal.finance.manager.category.repository.CategoryRepository"
    "com.personal.finance.manager.service.CategoryService" = "com.personal.finance.manager.category.service.CategoryService"
    "com.personal.finance.manager.controller.CategoryController" = "com.personal.finance.manager.category.controller.CategoryController"
    
    "com.personal.finance.manager.entity.Goal" = "com.personal.finance.manager.goal.entity.Goal"
    "com.personal.finance.manager.dto.GoalRequest" = "com.personal.finance.manager.goal.dto.GoalRequest"
    "com.personal.finance.manager.dto.GoalResponse" = "com.personal.finance.manager.goal.dto.GoalResponse"
    "com.personal.finance.manager.repository.GoalRepository" = "com.personal.finance.manager.goal.repository.GoalRepository"
    "com.personal.finance.manager.service.GoalService" = "com.personal.finance.manager.goal.service.GoalService"
    "com.personal.finance.manager.controller.GoalController" = "com.personal.finance.manager.goal.controller.GoalController"
    
    "com.personal.finance.manager.dto.ReportResponse" = "com.personal.finance.manager.report.dto.ReportResponse"
    "com.personal.finance.manager.service.ReportService" = "com.personal.finance.manager.report.service.ReportService"
    "com.personal.finance.manager.controller.ReportController" = "com.personal.finance.manager.report.controller.ReportController"
    
    "com.personal.finance.manager.dto.ApiResponse" = "com.personal.finance.manager.common.dto.ApiResponse"
}

$files = Get-ChildItem -Path "p:\Personal Finance Manager\personal-finance-manager\src" -Recurse -Filter "*.java"

foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    
    # 1. Update Package Declaration based on current directory structure
    $relPath = $file.FullName.Substring($file.FullName.IndexOf("com\personal\finance\manager"))
    $parentDir = (Split-Path $relPath -Parent) -replace "\\", "."
    $newPackage = "package " + $parentDir + ";"
    
    $content = $content -replace 'package com\.personal\.finance\.manager(.*?);', $newPackage
    
    # 2. Update Imports
    foreach ($key in $replacements.Keys) {
        $val = $replacements[$key]
        $content = $content.Replace($key, $val)
    }
    
    Set-Content -Path $file.FullName -Value $content -NoNewline
}

Write-Host "Rewrite completed."
