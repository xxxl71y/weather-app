# Weather Now — Windows 安装脚本
# 用法: 右键 setup.ps1 → 使用 PowerShell 运行

$ErrorActionPreference = "Stop"
$AppDir = "$env:LOCALAPPDATA\Weather Now"
$Desktop = [Environment]::GetFolderPath("Desktop")
$StartMenu = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Weather Now"

Write-Host "  Installing Weather Now..." -ForegroundColor Cyan

# 1. 复制文件
New-Item -ItemType Directory -Force -Path $AppDir | Out-Null
Copy-Item -Path "$PSScriptRoot\index.html", "$PSScriptRoot\manifest.json", "$PSScriptRoot\sw.js", "$PSScriptRoot\icon.svg" -Destination $AppDir -Force
Write-Host "  Files copied" -ForegroundColor Green

# 2. 找 Edge
$EdgePaths = @(
    "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
    "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe"
)
$Edge = $null
foreach ($p in $EdgePaths) { if (Test-Path $p) { $Edge = $p; break } }
if (-not $Edge) {
    Write-Host "  Microsoft Edge not found. Please install Edge first." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "  Edge found" -ForegroundColor Green

# 3. 开始菜单
New-Item -ItemType Directory -Force -Path $StartMenu | Out-Null
$WshShell = New-Object -ComObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("$StartMenu\Weather.lnk")
$Shortcut.TargetPath = $Edge
$Shortcut.Arguments = "--app=https://xxxl71y.github.io/weather-app/"
$Shortcut.WorkingDirectory = $AppDir
$Shortcut.Description = "实时天气查询"
$Shortcut.Save()
Write-Host "  Start menu entry created" -ForegroundColor Green

# 4. 桌面
$Shortcut = $WshShell.CreateShortcut("$Desktop\Weather.lnk")
$Shortcut.TargetPath = $Edge
$Shortcut.Arguments = "--app=https://xxxl71y.github.io/weather-app/"
$Shortcut.WorkingDirectory = $AppDir
$Shortcut.Description = "实时天气查询"
$Shortcut.Save()
Write-Host "  Desktop shortcut created" -ForegroundColor Green

Write-Host "`n  Done! Double-click 'Weather' on your desktop." -ForegroundColor Cyan
Read-Host "Press Enter to exit"
