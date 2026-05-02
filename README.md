# 天气酱

实时天气查询 — 自动定位，15 种天气动态背景 + 雨雪粒子动画。

**在线使用**: [xxxl71y.github.io/weather-app](https://xxxl71y.github.io/weather-app/)

## 下载

| 平台 | 文件 | 说明 |
|------|------|------|
| **Android** | [WeatherNow-Android.apk](https://github.com/xxxl71y/weather-app/releases) | 独立应用，WebView 封装，支持定位权限 |
| **Windows** | [WeatherNow-Windows.zip](https://github.com/xxxl71y/weather-app/releases) | 解压即用，便携免安装 |

## 功能

- GPS 精确定位 + IP 后备定位，显示详细中文地址
- 温度、湿度、风速、降水量
- 动态背景（晴天 / 多云 / 阴天 / 雨 / 雪 / 雷暴 / 雾）
- Canvas 雨滴和雪花粒子动画
- **Android**: HTML 内置于 APK，冷启动即刻加载；日式动漫天气少女图标
- **Windows**: Edge WebView2 启动器，原生桌面窗口体验
- PWA 支持，浏览器可安装到桌面，Service Worker 离线缓存
- 完全免费，无需注册，无需 API Key

## 使用的免费 API

| 服务 | 用途 |
|------|------|
| [Open-Meteo](https://open-meteo.com/) | 全球天气预报 |
| [BigDataCloud](https://www.bigdatacloud.com/) | 坐标反查地址 |
| [ipapi](https://ipapi.co/) | IP 定位（后备） |
