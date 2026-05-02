# Weather Now

实时天气查询 — 自动定位，15 种天气动态背景 + 雨雪粒子动画。

**在线使用**: [xxxl71y.github.io/weather-app](https://xxxl71y.github.io/weather-app/)

## 各平台使用

| 平台 | 方式 | 说明 |
|------|------|------|
| **任意浏览器** | 打开上述链接 | 即开即用 |
| **Windows 桌面** | 下载后运行 `install.bat` | 桌面快捷方式，Edge 应用模式（无边框独立窗口） |
| **Android** | Chrome 打开链接 → 菜单 → 添加到主屏幕 | 安装为独立 App，有图标和启动画面，离线可用 |
| **iOS / macOS** | Safari 打开链接 → 分享 → 添加到主屏幕 | 同上 |

## 功能

- GPS 精确定位 + IP 后备定位，显示详细地址
- 温度、湿度、风速、降水量
- 动态背景（晴天 / 多云 / 阴天 / 雨 / 雪 / 雷暴 / 雾）
- Canvas 雨滴和雪花粒子动画
- PWA 支持，可安装到桌面，Service Worker 离线缓存
- 完全免费，无需注册

## 使用的免费 API

| 服务 | 用途 |
|------|------|
| [Open-Meteo](https://open-meteo.com/) | 全球天气预报 |
| [BigDataCloud](https://www.bigdatacloud.com/) | 坐标反查地址 |
| [ipapi](https://ipapi.co/) | IP 定位（后备） |
