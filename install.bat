@echo off
chcp 65001 >nul
title Weather Now - 安装

set "URL=https://xxxl71y.github.io/weather-app/"
set "DESKTOP=%USERPROFILE%\Desktop"
set "STARTMENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs"

echo.
echo   ⛅ Weather Now
echo   ─────────────
echo.

:: 检测 Edge
set "EDGE="
if exist "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" set "EDGE=C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
if exist "C:\Program Files\Microsoft\Edge\Application\msedge.exe" set "EDGE=C:\Program Files\Microsoft\Edge\Application\msedge.exe"
if "%EDGE%"=="" (
    echo   未找到 Edge 浏览器，请先安装 Microsoft Edge。
    echo   https://www.microsoft.com/edge
    pause
    exit /b 1
)

echo   浏览器: Edge ✓

:: 图标
set "ICON=%~dp0icon.svg"
set "ICON_ARG="
if exist "%ICON%" echo   图标: icon.svg ✓

:: 桌面快捷方式
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$WScriptShell = New-Object -ComObject WScript.Shell; ^
   $Shortcut = $WScriptShell.CreateShortcut('%DESKTOP%\Weather.lnk'); ^
   $Shortcut.TargetPath = '%EDGE%'; ^
   $Shortcut.Arguments = '--app=%URL%'; ^
   $Shortcut.WorkingDirectory = '%%USERPROFILE%%'; ^
   $Shortcut.Description = '实时天气查询'; ^
   $Shortcut.Save()"
echo   桌面快捷方式: ✓

:: 开始菜单
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$WScriptShell = New-Object -ComObject WScript.Shell; ^
   $Shortcut = $WScriptShell.CreateShortcut('%STARTMENU%\Weather.lnk'); ^
   $Shortcut.TargetPath = '%EDGE%'; ^
   $Shortcut.Arguments = '--app=%URL%'; ^
   $Shortcut.WorkingDirectory = '%%USERPROFILE%%'; ^
   $Shortcut.Description = '实时天气查询'; ^
   $Shortcut.Save()"
echo   开始菜单: ✓

echo.
echo   安装完成！
echo.
echo   之后通过以下方式打开：
echo     - 桌面双击「Weather」图标
echo     - 开始菜单搜索「Weather」
echo.
pause
