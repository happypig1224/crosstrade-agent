<#
.SYNOPSIS
    CrossTrade Agent - 5个月时间回退提交辅助脚本
.DESCRIPTION
    保持当前时分秒同步，将提交时间的年月日自动回退5个月。
.EXAMPLE
    .\scripts\git-commit-5m.ps1 -Message "feat: add new feature"
    .\scripts\git-commit-5m.ps1 -Message "fix: fix bug" -Push
#>
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Message,

    [switch]$Push
)

$targetDate = (Get-Date).AddMonths(-5).ToString("yyyy-MM-dd HH:mm:ss zzz")
Write-Host "[Git Backdate] 设置提交时间为: $targetDate" -ForegroundColor Cyan

$env:GIT_COMMITTER_DATE = $targetDate
$env:GIT_AUTHOR_DATE = $targetDate

try {
    git commit -m "$Message" --date="$targetDate"
    if ($LASTEXITCODE -eq 0 -and $Push) {
        Write-Host "[Git Push] 正在推送到远程 main 分支..." -ForegroundColor Green
        git push origin main
    }
}
finally {
    Remove-Item env:GIT_COMMITTER_DATE -ErrorAction SilentlyContinue
    Remove-Item env:GIT_AUTHOR_DATE -ErrorAction SilentlyContinue
}
