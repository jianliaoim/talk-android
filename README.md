简聊 Android 客户端
------------------
## 开发环境
- Android Studio 2.1
- Android Ndk r10e
## 配置
- app/build.gradle配置
如果需要release运行,首先需要在```signingConfigs```中配置您的keystore文件,在```gradle.properties```中配置```RELEASE_STORE_PASSWORD=```您的keystore的storePassword,```RELEASE_KEY_ALIAS=```您的keystore的KeyAlias, ```RELEASE_KEY_PASSWORD=```您的keystore的keyPassword,然后如果需要你还需要在buildTypes里配置```umeng_app_key```,```baidu_api_key```
- Constant.java配置
```XIAOMI_APP_ID```小米推送需要的APP_ID,```XIAOMI_APP_KEY```小米推送需要的APP_KEY, ```WECHAT_APP_ID``` 微信的APP_ID, ```WECHAT_APP_SECRET```微信的SECRET,```MIXPANEL_TOKEN``` Mixpanel需要的token,```BUGLY_APP_ID```腾讯bugly需要使用的APP_ID

#### 以上配置看自己需求按需配置,当然如果你不配置也不影响运行

## fir.im 测试版
发布 fir.im 测试版可以利用 fir 的 gradle 插件来实现一键自动打包上传。需要注意的是在需要在gradle.properties 文件中加上如下2行：
```
BUILD_FOR_FIR=true
FIR_TOKEN= //这个是gradle中使用插件发布到fir需要使用到的token
```
每次上传前需要更新项目的 versionCode 和 versionName，并将更新日志则填写在项目根目录的 `fir-changelog.txt` 文件中。运行 `./gradlew publishApkRelease` 即可完成打包上传，teambition 的简聊话题内还配置有 fir.im 的 webhook,一旦 fir.im 有新的测试版发布就会有聚合消息。

## 渠道包打包

渠道分包采用 [packer-ng-plugin](https://github.com/mcxiaoke/packer-ng-plugin) 插件来实现快速打包。打包前需要在 gradle.properties 文件中加入商店配置文件路径 `market=markets.txt`
markets.txt 语法如下：
```
anzhi#注释：安智市场
appchina
baidu
baiduAd#百度市场推广渠道
```
打渠道包的 gradle 命令是 ./gradlew apkProdRelease。在代码中读取渠道信息使用如下接口：
```java
final String market = PackerNg.getMarket(Context);
AnalyticsConfig.setChannel(market); // 设置友盟渠道
```
Jenkins 使用 Talk-Android-Release 任务来打发布包

## 开发调试相关设置
由于应用开启了 MultiDex，所以在 build.gradle 文件中配置了2个 productFlavor: dev 和 prod。其中 dev 的 minSdkVersion 为 21，在编译时 Android gradle plugin 会执行 pre-dexing 操作并跳过 dex 合并这一步，编译的效率相对更高。所以在开发时需要在 Android Studio 的 Build Variants 窗口选择devDebug 或 devBeta，但是只能在 5.0版本以上的机器上测试。如需在低版本上测试则需要选择 prodDebug。
