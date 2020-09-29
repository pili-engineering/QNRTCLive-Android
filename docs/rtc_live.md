# 概述
## 场景说明

互动直播解决方案通过将七牛实时音视频、字节跳动美颜滤镜以及融云 IM 融合到一起，提供音视频直播、PK 直播、高级美颜滤镜、房间消息、刷礼物等功能，帮助开发者快速构建秀场直播等相关应用。

## 场景优势

互动直播解决方案提供高清、流畅、低延时的直播 PK 体验，通过七牛实时音视频的服务端合流等功能，提供了直播到 PK 的无缝衔接体验，同时支持自定义帧率、码率等丰富的自定义配置。通过字节跳动高级美颜滤镜的接入，提供了更丰富、更有趣的体验，为直播增添了更多的乐趣。同时接入融云 IM 实现了主播、观众端间的实时消息同步，使二者可以进行更好的沟通。

## 功能列表

| 主要功能 | 功能描述 |
|------- | --------|
| 音视频、PK 直播 | 高清、流畅、低延时的直播场景，直播、PK 无缝切换 |
| 高级美颜滤镜 | 实时的高级美颜滤镜特效，提供更高的可玩性 |
| IM 消息 | 支持主播、观众间的实时消息同步，沟通更顺畅 |
| 直播播放 | 支持对主播直播流的播放功能 |

# 场景实现
## 开发准备

### 设备以及系统要求

- 系统要求：Android 4.3 (API 18) 及以上

### 开发环境

- Android Studio 开发工具，官方<a href="http://developer.android.com/intl/zh-cn/sdk/index.html" target="_blank">下载地址</a>
- Android 官方开发 SDK，官方<a href="https://developer.android.com/intl/zh-cn/sdk/index.html#Other" target="_blank">下载地址</a>

## 直播 PK 模块场景实现
### SDK 下载集成
#### 下载 SDK

- [Android 体验 Demo 以及 SDK 下载地址](https://github.com/pili-engineering/QNRTC-Android)
- [Android 接口参考 Demo](https://github.com/pili-engineering/QNRTC-SampleCode-Video-Basic)

#### 导入 SDK
SDK 主要包含 demo 代码、SDK jar 包，以及 SDK 依赖的动态库文件。
其中，release 目录下是需要拷贝到您的 Android 工程的所有文件，以目前主流的 armeabi-v7a 架构为例，具体如下：

| 文件名称               | 功能    | 大小    |       备注           |
| --------------------- | -----  | -----  | -------------------  |
| qndroid-rtc-x.y.z.jar | SDK 库 | 834 KB | 必须依赖               |
| libqndroid_rtc.so     | 连麦   | 5.9 MB | 必须依赖              |
| libqndroid_beauty.so  | 美颜   | 452 KB  | 不用自带美颜，可以不依赖 |
| libqndroid_amix.so    | 混音   | 343 KB  | 不用混音功能，可以不依赖 |

- 将 qndroid-rtc-x.y.z.jar 包拷贝到您的工程的 libs 目录下
- 将动态库拷贝到您的工程对应的目录下，例如：armeabi-v7a 目录下的 so 则拷贝到工程的 jniLibs/armeabi-v7a 目录下

> 导入后生成的 APK 与未导入前相比，大小增加了 4.1M（包含美颜动态库）。需要注意的是，armabi 下的动态库（so 文件）是没有软编能力的，即 armabi 下仅支持硬编码，不支持软编码。若无特殊情况建议使用 armeabi-v7a 下的动态库。

具体可以参考 SDK 包含的 demo 工程，集成后的工程示例如下：

![](http://oyojsr1f8.bkt.clouddn.com/RTCDemo.jpg)

您也可以添加sdk的文档链接方便开发查阅:

- 首先找到工程中.idea/libraries/目录下的对应依赖包配置文件，然后添加如下链接配置

![](http://oyojsr1f8.bkt.clouddn.com/RTCDemo-javadoc.png)

- 然后打开Android Studio设置菜单，开启文档提示:

![](http://oyojsr1f8.bkt.clouddn.com/RTCDemo-javadoc-Setting.png)

当然，您也可以使用之前配置的任何快捷键

- 现在，开发过程中就可以查看对应接口的文档提示了:

![](http://oyojsr1f8.bkt.clouddn.com/RTCDemo-javadoc-hint.png)

下方的蓝色链接还可以跳转到外部查看

#### 修改 build.gradle

打开工程目录下的 build.gradle，确保已经添加了如下依赖，如下所示：

```java
dependencies {
    implementation files('libs/qndroid-rtc-x.y.z.jar')
}
```

#### 添加混淆

如果工程中添加了混淆，则七牛的包也需要添加混淆规则，在 proguard-rules.pro 文件下添加以下代码：

```
-keep class org.webrtc.** {*;}
-dontwarn org.webrtc.**
-keep class com.qiniu.droid.rtc.**{*;}
-keep interface com.qiniu.droid.rtc.**{*;}
```

#### 添加相关权限

在工程的 AndroidManifest.xml 中增加如下 `uses-permission` 声明：

```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_LOGS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

在 Android 6.0 (API 23) 开始，用户需要在应用运行时授予权限，而不是在应用安装时授予，并分为正常权限和危险权限两种类型。在实时音视频 SDK 中，用户需要在进入音视频通话房间前动态申请 `CAMERA`、`RECORD_AUDIO`、`WRITE_EXTERNAL_STORAGE` 权限，具体可参考 [Android 官方文档](https://developer.android.com/training/permissions/requesting?hl=zh-cn)。

**SDK 集成完成后便可以使用七牛实时音视频 SDK 进行直播、PK 场景的实现了，具体使用步骤请参考下文**

### SDK 基础使用
为了实现直播 PK 功能场景，需要您务必先了解七牛实时音视频 SDK 的基础使用，包括`roomToken 的生成`，`加入房间`，`采集`，`发布`，`订阅` 等过程。

> **roomToken 生成过程**可参考[七牛实时音视频云接入指南](https://doc.qnsdk.com/rtn/docs/rtn_startup)，下面将主要介绍加入房间等基本操作。

#### 初始化

首先，在 Application 里，完成 SDK 的初始化操作：

```java
QNRTCEnv.init(getApplicationContext());
```

#### 添加音视频通话需要的渲染控件

用户需要在布局文件中期望的位置添加两个 QNSurfaceView 分别用来做本地视频画面预览窗口和远端视频画面渲染窗口。

示例代码如下：

```java
<com.qiniu.droid.rtc.QNSurfaceView
    android:id="@+id/local_surface_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

<com.qiniu.droid.rtc.QNSurfaceView
    android:id="@+id/remote_surface_view"
    android:layout_width="150dp"
    android:layout_height="150dp" />
```
```java
mLocalWindow = findViewById(R.id.local_surface_view);
mRemoteWindow = findViewById(R.id.remote_surface_view);
```

-----

#### 创建音视频通话核心类

本操作推荐在 Activity 生命周期中的 onCreate() 中完成

```java
mEngine = QNRTCEngine.createEngine(getApplicationContext(), setting);
```

其中，setting 是音视频通话核心类的核心配置项，具体配置可参考 [QNRTCSetting](https://doc.qnsdk.com/rtn/android/docs/api_qnrtcsetting)

#### 设置回调

QNRTCEngineEventListener 包含了音视频通话过程中的所有重要接口，因此需要注册该监听器：

```java
mEngine.setEventListener(/*QNRTCEngineEventListener*/ listener);
```

除了上述设置方式，监听器也可以在创建音视频通话核心类的过程中直接传入：

```java
mEngine = QNRTCEngine.createEngine(getApplicationContext(), setting, /*QNRTCEngineEventListener*/ this);
```

-----

#### 创建 Track

Track 是 v2.x.x 版本中推出的新概念，如果您对 Track 概念不太了解，请查看 SDK 概述中的[概念介绍](https://doc.qnsdk.com/rtn/android/docs/preparation#5)。本文主要介绍互动直播场景的搭建，所以我们创建一条视频 Track 和一条音频 Track，并将它们设置为 master 轨道，代码如下：

```java
QNTrackInfo localVideoTrack = mEngine.createTrackInfoBuilder()
                        .setVideoEncodeFormat(format)
                        .setSourceType(QNSourceType.VIDEO_CAMERA)
                        .setMaster(true)
                        .create();
QNTrackInfo localAudioTrack = mEngine.createTrackInfoBuilder()
                        .setSourceType(QNSourceType.AUDIO)
                        .setMaster(true)
                        .create();
```

创建好本地视频 Track 后即可设置预览窗口，代码如下：

```java
mEngine.setRenderWindow(localVideoTrack, mLocalWindow);
```

#### 加入房间

上文提到过，SDK 所有的功能都是从 `RoomToken` 开始的，所以加入房间只需要将 `RoomToken` 作为参数传给 SDK 就可以了。代码如下：

```java
mEngine.joinRoom(roomToken);
```

加入房间成功后会触发 `onRoomStateChanged(QNRoomState state)` 回调，状态会从 QNRoomState.CONNECTING 变为 QNRoomState.CONNECTED。此时即可进行发布、订阅等操作。

在进入音视频通话房间之后，用户可以根据业务场景的需求在适当的时间调用离开房间的接口退出连麦，详情请见[房间管理](https://doc.qnsdk.com/rtn/android/docs/room_management)。

#### 发布本地 Tracks

成功加入房间后，即可在 `onRoomStateChanged` 回调中调用以下代码进行本地 Track 的发布：

```java
@Override
public void onRoomStateChanged(QNRoomState state) {
    switch (state) {
        case CONNECTED:
            mEngine.publishTracks(Arrays.asList(localVideoTrack, localAudioTrack));
            break;
    }
}
```

发布成功后，本地会收到 `onLocalPublished(List<QNTrackInfo> trackInfoList)` 回调。远端用户会收到 `onRemotePublished(String remoteUserId, List<QNTrackInfo> trackInfoList)` 回调。

> SDK 提供了 `QNRTCEngine#publish` / `QNRTCEngine#publishVideo` / `QNRTCEngine#publishAudio` 接口，可利用这些接口快速发布作为 master 的音视频/视频/音频 Track，无需自行创建及管理 Track。

#### 订阅远端 Tracks

SDK 默认会进行自动订阅，订阅成功后将会收到 `onSubscribed(String remoteUserId, List<QNTrackInfo> trackInfoList)` 的回调，在此回调内则可进行对 Track 的渲染窗口设置的操作：

```java
@Override
public void onSubscribed(String remoteUserId, List<QNTrackInfo> trackInfoList) {
    for(QNTrackInfo track : trackInfoList) {
        if (track.getTrackKind().equals(QNTrackKind.VIDEO)) {
            mEngine.setRenderWindow(track, mRemoteWindow);
        }
    }
}
```

在成功订阅之后，用户可以根据业务场景的需求在适当的时间调用取消订阅的接口取消订阅相应的 Track，详情请见[发布与订阅](https://doc.qnsdk.com/rtn/android/docs/publish_subscribe)

#### 单路转推任务

单路转推任务是指服务端对单独的一路音视频流进行转推的工作，主要适用于不包含连麦的`秀场直播`、`连麦中需要将某一路流单独转推落存储`等场景。详细使用接口可参考 [QNForwardJob](https://doc.qnsdk.com/rtn/android/docs/api_forwardjob)

#### 合流转推任务

多路流合流直播场景，主要适用于`连麦互动直播`、`PK 直播`以及`单主播需要两路以上视频合流转推`等场景。简单来说，就是对连麦各方的视频画面进行合流，然后转推。

#### 离开房间

当音视频通话结束，调用以下代码离开房间：

```java
mEngine.leaveRoom()
```

#### 销毁

在整个 Activity 销毁时，用户需要调用以下代码对资源进行释放，一般此操作建议在 Activity 生命周期的 `onDestroy()` 中进行，示例代码如下：

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    mRTCEngine.destroy();
}
```

> 七牛实时音视频 SDK 提供了丰富灵活的拓展接口，更多接口配置可参考[Android 开发手册](https://doc.qnsdk.com/rtn/android)

### 直播场景
直播场景，即单主播直播的场景，仅将一路音视频流直接转推到直播服务器。适用于秀场直播、电商直播等场景。

场景示意图如下：

![单路流直播](https://docs.qnsdk.com/forward_job.jpg)

为了实现上述场景，您可以参考如下实现方式：

#### 创建单路转推任务

QNForwardJob 用于配置单路转推的相关信息，包括推流地址、参与合流的音视频轨，详细的接口设置可参考 [QNForwardJob](https://doc.qnsdk.com/rtn/android/docs/api_forwardjob)。

创建转推任务的示例代码如下：

```java
// 创建单路转推任务对象
mForwardJob = new QNForwardJob();
// 设置 ForwardJob id
mForwardJob.setForwardJobId(mRoomName);
// 设置推流地址
mForwardJob.setPublishUrl(String.format(getString(R.string.publish_url), mRoomName));
// 设置单路流中的音频轨，仅支持一路音频的设置，重复设置会被覆盖
mForwardJob.setAudioTrack(mLocalAudioTrackInfo);
// 设置单路流中的视频轨，仅支持一路视频的设置，重复设置会被覆盖
mForwardJob.setVideoTrack(mLocalCameraTrackInfo);
// 创建单路流转推任务
mEngine.createForwardJob(mForwardJob);
```

单路转推任务创建成功后，会触发如下回调接口：

```java
/**
  * 当单路流转推任务创建成功的时候会回调此方法
  *
  * @param forwardJobId 转推任务 ID
  */
 @Override
 public void onCreateForwardJobSuccess(String forwardJobId) {
     // forwardJobId 即为创建成功的任务 id
     ToastUtils.s(RoomActivity.this, "单路转推任务 " + forwardJobId + " 创建成功！");
 }
```
创建成功即开启了单路流转推，可以通过相应的播放链接拉取直播流进行观看

**注意：**

**1. QNForwardJob 仅支持配置一路视频轨和一路音频轨，重复设置会被覆盖**

**2. 单路转推的场景下，务必保证配置了 "固定分辨率" `QNRTCSetting#setMaintainResolution` 选项的开启，否则会出现不可预期的问题！！！**

#### 停止单路转推任务

可以通过如下方式实现单路转推任务的停止：

```java
mEngine.stopForwardJob(mForwardJob.getForwardJobId());
```

### PK 场景
主播连麦 PK 场景指的是主播在直播时，可以对另外一个直播间的主播发起 PK 挑战，一旦挑战被接受，两个直播间的主播就会加入到同一个房间，并开始进行连麦互动。

与常规 1v1 连麦场景不同的是，PK 场景下直播界面会一分为二，每位主播各自的粉丝观看链接不会改变，但是可以同时看到两位主播的画面。

**为了更浅显易懂的让您了解从直播到 PK 场景切换的实现方式，我们预先设置如下背景：**

1. 主播 A、主播 B 在各自房间进行直播
2. 主播 A 对主播 B 发起直播请求，主播 B 接受主播 A 的 PK 请求并进行处理
3. 主播 B 在同意主播 A 的 PK 请求后，切换推流任务为合流转推任务
4. 主播 A 收到主播 B 同意 PK 的请求后，离开自己房间并加入到主播 B 的房间开启合流转推任务进行 PK
4. PK 结束后，主播 A 需要离开主播 B 的直播间并回到自己的直播间进行直播，两位主播需要切换推流任务为单路转推任务

基于上述背景，您可以参考如下实现方式：

#### 直播场景切换到 PK 场景

在上述背景中，每一个主播在单独直播的时候都会维护一个 `QNForwardJob` 任务实例，在主播切换到 PK 场景时，主播 A 和主播 B 的实现逻辑如下：

##### 主播 A
主播 A 在接收到主播 B 同意 PK 的请求之后，按照如下步骤进行操作：

- 停止本地维护的 QNForwardJob

```java
if (mForwardJob != null) {
    mEngine.stopForwardJob(mForwardJob.getForwardJobId());
    mForwardJob = null;
}
```

- 离开自己的房间，并在成功离开房间的回调里面加入主播 B 的房间：

```java
// 离开自己的房间
mEngine.leaveRoom();

// 在离开房间回调中加入主播 B 的目标房间
@Override
public void onRoomLeft() {
    if (mIsPkMode) {
        joinRoom(mTargetPkRoomToken);
    }
}
```

- 在成功加入主播 B 的房间后，创建并开启合流转推任务（**注意：单路转推任务和合流转推任务的推流地址不能改变**）

```java
// 在加入房间之后，创建合流转推任务
@Override
public void onRoomStateChanged(QNRoomState qnRoomState) {
    switch (qnRoomState) {
        case CONNECTED:
             if (mIsPkMode) {
                // 创建合流任务对象
                if (mQNMergeJob == null) {
                    mQNMergeJob = new QNMergeJob();
                }
                // 设置合流任务 id，该 id 为合流任务的唯一标识符
                mQNMergeJob.setMergeJobId(mRoomId);
                // 设置合流任务的推流地址，该场景下需保持一致
                mQNMergeJob.setPublishUrl(String.format(getString(R.string.publish_url), mRoomId));
                mQNMergeJob.setWidth(Config.STREAMING_WIDTH);
                mQNMergeJob.setHeight(Config.STREAMING_HEIGHT);
                // QNMergeJob 中码率单位为 bps，所以，若期望码率为 1200kbps，则实际传入的参数值应为 1200 * 1000
                mQNMergeJob.setBitrate(2000 * 1000);
                mQNMergeJob.setFps(30);
                mQNMergeJob.setStretchMode(QNStretchMode.ASPECT_FIT);
                
                // 设置合流背景
                QNBackGround qnBackGround = new QNBackGround();
                qnBackGround.setFile(Config.STREAMING_BACKGROUND);
                qnBackGround.setX(0);
                qnBackGround.setY(0);
                qnBackGround.setH(Config.STREAMING_HEIGHT);
                qnBackGround.setW(Config.STREAMING_WIDTH);
                mQNMergeJob.setBackground(qnBackGround);
                // 创建合流任务
                mEngine.createMergeJob(mQNMergeJob);
             }
             break;
     }
}

// 合流任务创建成功后会触发此回调
@Override
public void onCreateMergeJobSuccess(String mergeJobId) {
    mMergeJobId = mergeJobId;
    Log.i(TAG, "合流任务创建成功：" + mergeJobId + " url = " + mQNMergeJob.getPublishUrl());
}

// 合流任务创建成功后，需要配置两个主播的 QNMergeTrackOption，详细使用可参考 demo 实现
mEngine.setMergeStreamLayouts(mMergeTrackOptions, mMergeJobId);
```

**经过上述步骤，主播 A 即可加入到主播 B 的直播间并进行互动 PK 了。其中，更详细的合流配置选项，可参考[合流配置](https://doc.qnsdk.com/rtn/android/docs/merge_stream)文档**

##### 主播 B
主播 B 在同意主播 A 的 PK 请求后，由于主播 B 无需切换房间，所以步骤会先谷底简单些，具体步骤如下：

- 创建合流转推任务
- 在创建合流任务成功的回调中，停止单路转推任务
- 在适当的时机配置自己和主播 A 的合流布局进行合流直播

**经过上述操作，两端即可成功进入 PK 直播场景进行互动直播了**

#### PK 场景切换到直播场景

当主播 A 或者主播 B 有一端想要停止 PK 时，实现逻辑如下：

##### 主播 A
主播 A 退出 PK 时，首先要通知主播 B 自己要结束 PK，然后可按照如下步骤切换回单独的直播场景

- 停止合流转推任务

```java
// 1. 首先要停止合流转推
if (mQNMergeJob != null) {
    Log.i(TAG, "停止合流任务：" + mMergeJobId);
    mEngine.stopMergeStream(mMergeJobId);
    mMergeJobId = null;
    mQNMergeJob = null;
}

```

- 离开主播 B 的房间，并加入到自己原有的房间

```java
// 离开主播 B 的房间
mEngine.leaveRoom();

// 在离开房间的回调里更新本地 UI 为单直播场景，并重新加入原有的房间
@Override
public void onRoomLeft() {
    relayoutLocalSurfaceView(false);
    updateBtnsSources(false);
    if (mEngine != null) {
        mEngine.joinRoom(mOriginalRoomToken);
    }
}
```

- 成功回到自己房间之后，重新发布音视频 Track，并创建单路转推任务（**注意：单路转推任务和合流转推任务的推流地址不能改变**）

```java
// 发布音视频 Tracks
@Override
public void onRoomStateChanged(QNRoomState qnRoomState) {
    mEngine.publishTracks(mLocalTrackList);
}

// 发布 Tracks 成功之后，创建单路转推任务
@Override
public void onLocalPublished(List<QNTrackInfo> trackInfoList) {
    if (mForwardJob == null) {
        mForwardJob = new QNForwardJob();
        mForwardJob.setForwardJobId(mRoomId);
        mForwardJob.setPublishUrl(String.format(getString(R.string.publish_url), mRoomId));
        mForwardJob.setAudioTrack(mLocalAudioTrack);
        mForwardJob.setVideoTrack(mLocalVideoTrack);
        mForwardJob.setInternalForward(true);
    }
    mEngine.createForwardJob(mForwardJob);
}
```

**经过上述步骤，主播 A 即可成功回到自己房间进行单主播直播了**

##### 主播 B
主播 B 退出 PK 时，由于并没有切换房间，所以步骤会相对简单些：

- 通知主播 A 要退出 PK 直播
- 创建单路转推任务
- 在单路转推任务创建成功后，停止合流转推任务

**经过上述步骤，主播 B 即可成功回到自己房间进行单主播直播了**

> **上述步骤是以伪代码的形式描述如何进行 PK 场景的直播，更详细的处理逻辑可参考 [QNRTCLive-Android](https://github.com/pili-engineering/QNRTCLive-Android)**

## 美颜模块
### SDK 下载集成
#### 下载和导入 SDK
请下载以下列表中包含的 jar 与 so ,并将其导入到项目中。

| 文件名称                              | 功能              | 大小     | 备注  |
| ----------------------------------- | ----------------- | ------- | ----- |
| pldroid-bytedance-effect-x.y.z.jar  | 特效插件 SDK 库     | 98KB    | 必须依赖 |
| libeffect.so                        | 高级特效插件核心库   | 8.2MB   | 必须依赖 |
| libeffect\_proxy.so                 | 高级特效插件接口层   | 22KB    | 必须依赖 |
| libc++_shared.so                    | c++ 静态链接库      | 657KB   | 必须依赖 |

#### 修改 build.gradle
打开您的工程目录下的 `build.gradle` ，确保已经添加了如下依赖（代码中的`x.y.z`为具体的版本号），如下所示：

```java
dependencies {
    implementation files('libs/pldroid-bytedance-effect-x.y.z.jar')
}
```

#### 修改代码混淆规则
如果工程中添加了混淆，则七牛的包也需要添加混淆规则，在 proguard-rules.pro 文件下添加以下代码：

```java
-keep class com.qiniu.bytedanceplugin.**{*;}
```

#### 添加特效素材
购买的资源不同，相应的资源文件大小和特效数量也不同，下面以 demo 的资源包举例：   

| 文件名称                         | 文件类型                | 大小     | 备注                                                    |
| ------------------------------ | ---------------------- | ------- | ------------------------------------------------------- |
| LicenseBag.bundle              | 授权文件                | 426字节  | 该包内应包含有一个与包名所对应的授权文件，文件名内包含了所绑定的包名和授权的起止日期 |
| ComposeMakeup.bundle           | 高级美颜、美型、美妆素材   | 4.2MB   | 包含二十余款美颜、美型特效                                   |
| FilterResource.bundle          | 高级滤镜素材             | 12.3MB  | 包含 48 款滤镜                                            |
| StickerResource.bundle         | 动态贴纸素材             | 39.4MBB  | 包含 20 款动态贴纸                                        |
| ModelResource.bundle           | 模型文件                | 13.8MB   | 用于人脸识别、手势识别                                      |

- 如用户需要更多款式的美颜、美型、滤镜、动态贴纸素材，可在特效君 APP 上选择，联系七牛商务咨询进行购买。  
- **鉴权文件是有到期时间的，如果时间过期，需要替换 LicenseBag.bundle 文件为新申请的鉴权文件。所以需要在授权文件过期前，替换 apk 中的 LicenseBag.bundle 文件（建议支持 LicenseBag.bundle 的云端下发功能，此功能需要您自行实现）。**

#### 资源的配置处理
为了方便的获取特效的信息列表，首先应该对字节跳动的资源进行配置处理，分别为高级美颜、微整形、美妆、美体素材（ComposeMakeup.bundle）、高级滤镜素材（FilterResource.bundle）和动态贴纸素材（StickerResource.bundle）配置 config.json 文件与 icons 文件夹。此项配置是为了后面可以通过调用类似于 `getStickerList()` 的方法快速获取特效信息，投放入 Adapter 来生成视图，也是为了可以通过云端下发特效文件和配置文件的方式在不更新 APP 的情况下更新特效资源。

由于资源配置的过程较为繁琐，我们为您提供了一个**处理脚本**，您只需将字节提供的 resource 和 icons 文件夹拷入脚本同级目录，在脚本所在目录下运行脚本即可，具体的使用方式请参见上级目录的 [ResourceTools](https://github.com/pili-engineering/QNRTC-ByteDance-Android/tree/master/ResourceTools) 文件夹，运行脚本成功后您可更改对应素材文件下的 config.json 文件来修改特效图标、特效名称、特效初始强度甚至特效所在类别等信息。

### 快速开始
#### 把资源从 assets 拷贝到手机本地目录
由于配置好的资源默认由 AssetManager 管理，存储于 apk 中，在安装后并不会解压到手机本地，无法取得绝对路径，所以为了更好的管理资源，需要把资源从 apk 拷贝到手机本地目录下。这里可以参考 demo 中的 LoadResourcesActivity 和 LoadResourcesTask 类。

#### 在程序中加载和使用资源
如果拷贝成功，就可以传入 ByteDancePlugin 的初始化方法中，从而完成 ByteDancePlugin 的初始化：

```java
//该路径为上一步特效资源文件拷贝到外部存储中的路径
String resourcePath = getExternalFilesDir("assets") + File.separator + "resource";
mByteDancePlugin = new ByteDancePlugin(context, ByteDancePlugin.PluginType.record); // 此处创建的是一个 record 类型的插件
mByteDancePlugin.init(resourcePath) // 初始化操作应在渲染线程中调用
```
ByteDancePlugin 是提供特效相关接口的核心类，您可以通过它获取特效资源信息列表：

```java
//获取所有滤镜信息
ByteDancePlugin.getFilterList()
//获取所有贴纸信息
ByteDancePlugin.getStickerList()
//获取所有美颜信息
ByteDancePlugin.getComposerList(ComposerType.BEAUTY);
//获取所有微整形信息
ByteDancePlugin.getComposerList(ComposerType.RESHAPE);
//获取所有美体信息
ByteDancePlugin.getComposerList(ComposerType.BODY);
//获取所有美妆信息
ByteDancePlugin.getMakeupList();
``` 
需要说明的是，获取美妆信息的接口有所不同，因为美妆资源是二级列表，其结构如下：

```
├── 美妆模块
│   └── 口红
│       ├── 复古红
│       ├── 少女粉
│       ├── 西柚色
│       ├── 西瓜红
│       └── ***
│   └── 染发
│       ├── 暗蓝
│       ├── 墨绿
│       ├── 深棕
│       └── ***
│   └── 腮红
│       ├── 微醺
│       ├── 日常
│       ├── 蜜桃
│       └── ***
│   └── ***
```

`getMakeupList()` 返回的是一个 List<MakeupModel> ,可调用 `MakeupMode.getEffects()` 来取得该类别的具体特效列表。

#### 特效处理
特效处理是我们的核心步骤，建议参考 demo 中的 LiveRoomActivity 类。特效添加的原理是通过摄像机采集视频的回调函数得到每一帧视频的纹理，然后把该纹理送到 ByteDancePlugin 中去做特效处理，最后把处理完成的纹理再传入实时音视频 SDK 中。我们需要通过 QNRTCEngine 的 setCaptureVideoCallBack(QNCaptureVideoCallback callback) 方法来添加摄像机采集回调接口，从而获取 OpenGL 环境和摄像头采集到的纹理：

```java
mEngine.setCaptureVideoCallBack(mCaptureVideoCallback);

private QNCaptureVideoCallback mCaptureVideoCallback = new QNCaptureVideoCallback() {
        @Override
        public void onCaptureStarted() {
            mByteDancePlugin.init(mEffectResourcePath);
            // 由于切换到后台时会 destroy mByteDancePlugin，所以从后台切回来的时候需要先恢复特效
            mByteDancePlugin.recoverEffects();
            updateProcessTypes();
        }

        @Override
        public void onRenderingFrame(VideoFrame.TextureBuffer textureBuffer, long timestampNs) {
            if (mGLHandler == null) {
                mGLHandler = new Handler();
            }

            if (mByteDancePlugin.isUsingEffect()) {
                boolean isOES = textureBuffer.getType() == VideoFrame.TextureBuffer.Type.OES;
                int newTexture = mByteDancePlugin.drawFrame(textureBuffer.getTextureId(), textureBuffer.getWidth(), textureBuffer.getHeight(), timestampNs, mProcessTypes, isOES);
                if (newTexture != textureBuffer.getTextureId()) {
	                // 特效处理完之后的纹理类型为 RGB 类型
	                textureBuffer.setType(VideoFrame.TextureBuffer.Type.RGB);
	                textureBuffer.setTextureId(newTexture);
                }            
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, int width, int height, int rotation, int fmt, long timestampNs) {
            if (mTextureRotation != rotation) {
                mTextureRotation = rotation;
                updateProcessTypes();
            }
        }

        @Override
        public void onCaptureStopped() {
            //停止连麦或者切到后台时候会调用
            mByteDancePlugin.destroy();
            mTextureRotation = 0;
            mIsFrontCamera = true;
        }
    };
```

需要注意的是：  

-  ByteDancePlugin 处理纹理的时候，需要传入 ProcessType 类型的列表，该列表表明了增加特效时需要首先把纹理旋转和镜像的操作，这是因为前后置摄像头返回的纹理在不同的手机上，方向或者镜像有可能不同，所以我们需要根据摄像头的前后置和回调纹理的方向来配置该列表。（可参看 demo 中的 updateProcessTypes 方法）
-  onCaptureStarted, onRenderingFrame 和 onCaptureStopped 的回调都是在 OpenGL 线程的回调，onPreviewFrame 回调可以获取视频帧的旋转角度。

#### 设置特效
特效的设置或更新是通过 ByteDancePlugin 的一系列方法实现的，需要特别注意的是，设置或更新特效的方法需要在 OpenGL 线程中调用。

``` java
// 设置美颜、微整形、美妆、美体等特效
mByteDancePlugin.setComposerNodes(nodes)
// 更新美颜、微整形、美妆、美体特效的强度
mByteDancePlugin.updateComposerNode(filePath, key, value)
// 设置滤镜特效
mByteDancePlugin.setFilter(filePath)
// 更新滤镜特效强度
mByteDancePlugin.updateFilterIntensity(intensity)
// 设置动态贴纸特效
mByteDancePlugin.setSticker(filePath)
// 设置 composer 类型特效（美颜、微整形、美妆、美体）可以与贴纸特效叠加
mByteDancePlugin.setComposerMode(ComposerMode.SHARE);
```
**凡是与特效相关的操作，包括设置、更新、处理等皆需要在渲染线程 (OpenGL 线程) 中调用**

```java
// 如果因为某些原因导致特效消失，可调用此方法来恢复之前设置的特效
mByteDancePlugin.recoverEffects();
// 确定不再使用特效可以使用此方法释放特效资源
mByteDancePlugin.destroy();
```

**如需了解具体的接口设计，可查看[此文档](https://github.com/pili-engineering/QNRTC-ByteDance-Android/blob/master/docs/QNRTC-ByteDance.md#5-%E6%8E%A5%E5%8F%A3%E8%AE%BE%E8%AE%A1)**

## IM 模块
直播互动解决方案的 IM 模块主要使用了融云 IM 的 IMLib SDK。在方案中内置了融云 IM 聊天室所使用的 AppKey 和服务器地址，如果您需要接入到您自己的配置，您需要做以下操作。  

1. [注册融云开发者](https://developer.rongcloud.cn/signup/?utm_source=demogithub&utm_term=demosign)，创建应用后获取 APPKey。
2. 部署业务服务器，可参看 [SealLive-Server](https://github.com/rongcloud/demo-chatroom/tree/v2.0/app-server)，以实现 IM 模块的验签功能。
3. 服务部署完毕之后，请分别将源码中的 `APP_KEY`,`APPSERVER` 改为您自己的。
参见源码中文件  `com.qiniu.droid.rtc.live.demo.im.DataInterface`

### 修改 build.gradle
打开您的工程目录下的 `build.gradle` ，确保已经添加了如下依赖：

```java
dependencies {
    // 融云 SDK 核心库
    implementation 'cn.rongcloud.sdk:im_lib:2.10.5'
    // 以下为业务层可能会用到的外部库
    implementation 'de.greenrobot:eventbus:2.4.0'
    implementation 'com.github.hust201010701:XDanmuku:-SNAPSHOT'
}
```


### 功能模块介绍
融云 IM 聊天室相关代码目录是在 com.qiniu.droid.rtc.live.demo.im 包中，由 adapter、 danmu、 gift、 like、 message、messageview、model、panel、utils、DataInterface、ChatroomKit 等模块组成

* **adapter:** 聊天列表和礼物框适配器。
* **danmu:** 弹幕展示相关内容。
* **gift：** 礼物选择和展示相关内容。
* **like:** 点赞相关内容。
* **message：**  进入直播间、点赞等自定义消息信令。
* **messageview：** 自定义消息展示View。
* **model：**  礼物信息、角色信息、房间信息等数据模型。
* **panel：** 底部操作栏相关内容。
* **utils：** 通用的一些工具类。
* **DataInterface** 直播功能中所有用到的数据和 AppKey 等信息。
* **ChatroomKit：** 对融云 IM 引擎的封装方便调用。

### 使用到的融云产品
* **即时通讯 IMLib SDK**  可详细查看 [IMLib SDK 开发指南](https://www.rongcloud.cn/docs/android.html)
* 主要请参考方案中 **DataInterface** 和 **ChatroomKit** 的用法。


# 常见错误码

| 错误枚举 | 错误码 | 描述 |
| -------- | -------- | -------- |
| REQUEST_TIMEOUT | -1 | 请求超时 |
| NETWORK_UNREACHABLE | -2 | 网络不可达 |
| INTERNAL_ERROR | -3 | 内部错误 |
| BAD_TOKEN | 401003 | token 错误，通常出现于账号重复登录 |
| NO_SUCH_ROOM | 404002 | 房间不存在 |
| UNKNOWN_MESSAGE | 10001 | 消息不属于已知类型，无法解析 |
| TOKEN_ERROR | 10002 | 信令认证用的 token 错误 |
| NO_PERMISSION | 10003 | 没有权限（观众发起请求等情况） |
| ROOM_NOT_EXIST | 10011 | 信令房间不存在 |
| ROOM_IN_PK | 10012 | 房间正在 PK 连麦直播中，不能发起 PK |
| ROOM_NOT_IN_PK | 10013 | 房间未在 PK 中，不能结束 PK |
