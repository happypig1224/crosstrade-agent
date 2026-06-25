<#
.SYNOPSIS
    CrossTrade Agent - 5个月时间回退提交辅助脚本
.DESCRIPTION
    保持当前时分秒同步，将提交时间的年月日自动回退5个月。
.EXAMPLE
    git c5m "feat: add new feature"
    git c5m -m "fix: bug fix" -Push
    git c5m "update doc" -a -p
#>
param(
    [Parameter(Mandatory = $false, Position = 0)]
    [Alias("m")]
    [string]$Message,

    [Alias("p")]
    [switch]$Push,

    [Alias("a")]
    [switch]$All
)

# 若未提供提交信息，给出清晰提示
if ([string]::IsNullOrWhiteSpace($Message)) {
    Write-Host "[Git Backdate] 错误: 请提供提交信息，例如: git c5m `"feat: 提交内容`"" -ForegroundColor Red
    Write-Host "常用语法:" -ForegroundColor Yellow
    Write-Host "  git c5m `"feat: 提交说明`"           # 提交已暂存的修改"
    Write-Host "  git c5m `"feat: 提交说明`" -Push     # 提交并直接推送至远程"
    Write-Host "  git c5m `"feat: 提交说明`" -a -p     # 暂存全部已跟踪修改、提交并推送"
    exit 1
}

# 计算 5 个月前的时间：时分秒与当前一致，年月日减 5 个月
$targetDate = (Get-Date).AddMonths(-5).ToString("yyyy-MM-dd HH:mm:ss zzz")
Write-Host "[Git Backdate] 自动对齐提交时间: $targetDate" -ForegroundColor Cyan

# 若指定了 -a / -All，自动暂存已修改与删除的文件
if ($All) {
    Write-Host "[Git Stage] 自动暂存已跟踪文件修改 (git add -u)..." -ForegroundColor Gray
    git add -u
}

$env:GIT_COMMITTER_DATE = $targetDate
$env:GIT_AUTHOR_DATE = $targetDate

try {
    # 执行 git commit
    git commit -m "$Message" --date="$targetDate"
    $commitCode = $LASTEXITCODE

    if ($commitCode -eq 0) {
        Write-Host "[Git Commit] 本地提交成功！" -ForegroundColor Green
        if ($Push) {
            Write-Host "[Git Push] 正在推送到远程 main 分支..." -ForegroundColor Green
            git push origin main
            if ($LASTEXITCODE -eq 0) {
                Write-Host "[Git Push] 远程推送成功！" -ForegroundColor Green
            }
        }
    }
}
finally {
    Remove-Item env:GIT_COMMITTER_DATE -ErrorAction SilentlyContinue
    Remove-Item env:GIT_AUTHOR_DATE -ErrorAction SilentlyContinue
}
