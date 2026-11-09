# Mihane

Mihane 是基于 [Mishka](https://github.com/YuKongA/Mishka) 二次开发的 Android 代理客户端，使用 Kotlin、Jetpack Compose 和 [uwuSDK](https://github.com/uwuAOSP/platform_uwu-sdk) 重写前端，采用 [Material 3 Expressive](https://m3.material.io/) 风格，保留 Mihomo 核心及原有代理后端。

支持 Android 12 及以上系统，提供 `arm64-v8a`、`armeabi-v7a`、`x86_64` 和 `x86` 安装包。

## 功能

- VPN、ROOT TUN 和 ROOT TPROXY 模式，支持分应用代理。
- URL、本地文件和二维码订阅导入，支持自动更新、覆盖配置和 age 加密订阅。
- 节点选择、排序与延迟测试，实时流量、连接和日志查看。
- DNS 查询、Provider 管理、YAML 编辑、备份与恢复。
- 系统动态取色、种子色、深浅主题和手机／Pad 自适应布局。
- 流量通知、快捷设置磁贴、Wi-Fi 自动切换和外部应用控制。

## 使用

1. 在“订阅”页面导入配置并选择要使用的订阅。
2. 在“设置”中选择隧道模式。
3. 返回首页启动代理。VPN 模式首次启动时需要确认系统连接请求，ROOT 模式需要 Root 权限。

安装包见 [Releases](https://github.com/Jelly10086/Mihane/releases)，开发构建见 [GitHub Actions](https://github.com/Jelly10086/Mihane/actions/workflows/build.yml)。

## 构建

需要 JDK 21、Go 1.26.10、Android SDK 37.2、Build Tools 36.0.0、NDK 28.2.13676358 和 CMake 3.22.1。

```sh
git clone --recurse-submodules https://github.com/Jelly10086/Mihane.git
cd Mihane
export ANDROID_HOME=/path/to/android-sdk
export MISANS_ARCHIVE="$PWD/fonts/misans/MiSans.zip"
bash .github/scripts/prepare-misans.sh
./gradlew :app:downloadGeoFiles
./gradlew -I .github/ci/android.init.gradle :app:assembleDebug :app:assembleRelease
```

产物位于 `app/build/outputs/apk/debug/` 和 `app/build/outputs/apk/release/`。Release 签名使用 `KEYSTORE_PATH`、`KEYSTORE_PASS`、`KEY_ALIAS` 和 `KEY_PASSWORD` 环境变量。

### CI

推送到 `main`、提交 PR 或手动运行工作流可触发构建。CI 下载官方 MiSans 字体包，并校验压缩包及 `MiSansVF.ttf` 的 SHA-256。

PR 运行编译、lint 和四种 ABI 的产物检查。`main` 和手动构建发布使用固定签名的 Debug、Release APK，发布前核对签名证书、包名、版本和原生库架构。CI 的 `versionCode` 按工作流运行编号递增。

仓库 Secrets 使用 `SIGNING_KEY`（Base64 编码的密钥库）、`KEY_STORE_PASSWORD`、`ALIAS` 和 `KEY_PASSWORD`。签名证书指纹固定在 `.github/ci/signing-certificate.sha256`。

发布版本时，将 `ProjectConfig.kt` 中的 `VERSION_NAME` 更新为目标版本，再推送对应的 `v版本号` 标签；也可在 `main` 手动运行工作流并勾选 `publish_release`。构建和校验通过后，发布四种 ABI 的 Release APK 与 `SHA256SUMS`。

## 字体与许可

应用代码使用 [GPL-3.0](LICENSE)。英文和数字使用 Outfit，中文使用原版 MiSans；字体保留各自许可。版权和完整协议可在“关于 → 开源许可”中查看。

## 致谢

- [Mishka](https://github.com/YuKongA/Mishka)：上游客户端与代理实现。
- [Mihomo](https://github.com/MetaCubeX/mihomo)：代理核心。
- [uwuSDK](https://github.com/uwuAOSP/platform_uwu-sdk) 和 [AndroidX](https://github.com/androidx/androidx)：界面组件。
- [scripta](https://github.com/YuKongA/scripta)：配置编辑器。
