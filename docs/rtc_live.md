# 概述
## 场景说明

互动直播解决方案通过将七牛实时音视频、字节跳动美颜滤镜以及融云 IM 融合到一起，提供音视频直播、PK 直播、语音房、高级美颜滤镜、房间消息、刷礼物等功能，帮助开发者快速构建秀场直播等相关应用。

## 场景优势

互动直播解决方案提供高清、流畅、低延时的直播 PK 体验，通过七牛实时音视频的服务端合流等功能，提供了直播到 PK 的无缝衔接体验，同时支持自定义帧率、码率等丰富的自定义配置。通过字节跳动高级美颜滤镜的接入，提供了更丰富、更有趣的体验，为直播增添了更多的乐趣。同时接入融云 IM 实现了主播、观众端间的实时消息同步，使二者可以进行更好的沟通。

除此之外，还提供了语音房直播的交互场景，实现了稳定、流畅、低延时的语音沟通。

## 功能列表

| 主要功能 | 功能描述 |
|------- | --------|
| 音视频、PK 直播 | 高清、流畅、低延时的直播场景，直播、PK 无缝切换 |
| 语音房  |  稳定、流畅、低延时的语音直播连麦 |
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

## 直播互动模块

直播互动模块的核心是**七牛实时音视频云 SDK**，集成方式如下：

### SDK 下载集成
#### 下载 SDK

- [Android 体验 Demo 以及 SDK 下载地址](https://github.com/pili-engineering/QNRTC-Android)
- [Android 接口参考 Demo](https://github.com/pili-engineering/QNRTC-Android/tree/master/QNRTC-API-Examples)

#### 导入 SDK
SDK 主要包含 demo 代码、SDK jar 包，以及 SDK 依赖的动态库文件。
其中，release 目录下是需要拷贝到您的 Android 工程的所有文件，以目前主流的 armeabi-v7a 架构为例，具体如下：

| 文件名称               | 功能    | 大小    |       备注           |
| --------------------- | -----  | -----  | -------------------  |
| qndroid-rtc-x.y.z.jar | SDK 库 | 656 KB | 必须依赖               |
| libqndroid_rtc.so     | 连麦   | 6.71 MB | 必须依赖              |
| libqndroid_beauty.so  | 美颜   | 442 KB | 不用自带美颜，可以不依赖 |
| libqndroid_amix.so    | 混音   | 335 KB | 不用混音功能，可以不依赖 |

- 将 qndroid-rtc-x.y.z.jar 包拷贝到您的工程的 libs 目录下
- 将动态库拷贝到您的工程对应的目录下，例如：armeabi-v7a 目录下的 so 则拷贝到工程的 jniLibs/armeabi-v7a 目录下

具体可以参考 SDK 包含的 demo 工程，集成后的工程示例如下：

![](http://oyojsr1f8.bkt.clouddn.com/RTCDemo.jpg)

#### 修改 build.gradle

打开工程目录下的 build.gradle，确保已经添加了如下依赖，如下所示：

```java
dependencies {
    implementation files('libs/qndroid-rtc-x.y.z.jar')
}
```

**添加对 Java8 语言功能的支持：**

```java
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
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
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

在 Android 6.0 (API 23) 开始，用户需要在应用运行时授予权限，而不是在应用安装时授予，并分为正常权限和危险权限两种类型。在实时音视频 SDK 中，用户需要在进入音视频通话房间前动态申请 `CAMERA`、`RECORD_AUDIO` 权限，具体可参考 [Android 官方文档](https://developer.android.com/training/permissions/requesting?hl=zh-cn)。

**SDK 集成完成后便可以使用七牛实时音视频 SDK 进行直播、PK 场景的实现了，具体使用步骤请参考下文**

### SDK 基础使用
为了实现直播 PK 功能场景，需要您务必先了解七牛实时音视频 SDK 的基础使用，包括`roomToken 的生成`，`加入房间`，`采集`，`发布`，`订阅` 等过程。

> 1. **roomToken 生成过程**可参考[七牛实时音视频云接入指南](https://developer.qiniu.com/rtc/8813/roomToken)，下面将主要介绍加入房间等基本操作。
> 2. SDK 的基础使用可以参考[实现视频通话](https://developer.qiniu.com/rtc/8766/quick-start-android)文档

### 直播 PK 场景实现
在了解直播 PK 场景实现方式之前，需要先了解该场景所需要用到的两个概念：

1. [CDN 转推](https://developer.qiniu.com/rtc/8770/turn-the-cdn-push-android)：用于实现直播流的推送。
2. [跨房媒体转发](https://developer.qiniu.com/rtc/10631/media-relay-android)：用于实现音视频数据的跨房转发

在了解了上述概念之后，我们来看下具体实现。

#### 直播场景

直播场景的流程图可参考如下：

![](https://docs.qnsdk.com/%E7%9B%B4%E6%92%AD%E7%9A%84%E5%BC%80%E5%A7%8B%E7%BB%93%E6%9D%9F.jpg)

从上述流程图可以看出，除了基本的**音视频 Track 创建**、**加入房间**、**发布 Track** 之外，还需要通过**开启单路转推**的方式进行单主播的直播.

伪代码参考如下：

```java
// 初始化
QNRTC.init(this, mRTCEventListener); // 初始化 RTC
QNRTCClientConfig clientConfig = new QNRTCClientConfig(QNClientMode.LIVE, QNClientRole.BROADCASTER);
QNRTCClient client = QNRTC.createClient(clientConfig, clientEventListener); // 创建 QNRTCCLient 对象
client.setLiveStreamingListener(liveStreamingListener); // 设置 CDN 转推事件监听

// 创建音视频采集 Track
QNMicrophoneAduioTrack microphoneAudioTrack = QNRTC.createMicrophoneAudioTrack(); // 创建麦克风采集 Track
QNCameraVideoTrack cameraVideoTrack = QNRTC.createCameraVideoTrack(cameraVideoTrackConfig); // 创建摄像头采集 Track

// 加房间
String roomToken = getRoomToken(); // 通过业务服务获取 roomToken
client.join(roomToken); // 加入房间

// 发布音视频 Track
client.publish(publishResultCallback, cameraVideoTrack, microphoneAudioTrack);

// 开启单路转推
QNDirectLiveStreamingConfig directLiveStreamingConfig = new QNDirectLiveStreamingConfig(); // 创建单路转推配置类对象
directLiveStreamingConfig.setStreamID("direct_stream_ID"); // 设置单路转推唯一标识符
directLiveStreamingConfig.setUrl("publish_url"); // 设置单路转推任务的推流地址，该地址需和合流转推时保持一致，并使得 SerialNum 自增以提高直播流优先级
directLiveStreamingConfig.setAudioTrack(microphoneAudioTrack); // 设置音频 Track
directLiveStreamingConfig.setVideoTrack(cameraVideoTrack); // 设置视频 Track
client.startLiveStreaming(directLiveStreamingConfig); // 开启单路转推

// 停止单路转推
client.stopLiveStreaming(directLiveStreamingConfig);

// 离开房间
client.leave();

// 反初始化
QNRTC.deinit();
```

**注意事项：**

- **QNDirectLiveStreamingConfig仅支持配置一路视频轨和一路音频轨，重复设置会被覆盖**

- **单路转推的场景下，务必保证配置了 "固定分辨率" `QNRTCSetting#setMaintainResolution` 选项的开启，否则会出现不可预期的问题！！！**

#### 直播 PK 切换

直播 PK 切换场景的流程图，可参考如下：

![](https://docs.qnsdk.com/%E7%9B%B4%E6%92%AD%20PK%20%E5%88%87%E6%8D%A2.jpg)

从上述流程图可以看出，直播 PK 切换的场景，主要涉及到**单路转推和合流转推的切换**以及**跨房媒体转发**两个主要的功能点。

直播 PK 场景切换步骤和伪代码参考如下：

1. 首先，直播场景需要设置 CDN 转推的事件监听器，其回调方法和直播 PK 切换场景下所需完成的工作参考如下：

```java
// CDN 转推的事件监听器
QNLiveStreamingListener liveStreamingListener = new QNLiveStreamingListener() {
  @Override
  public void onStarted(String streamID) {
    // 转推任务成功创建时触发此回调
    if (directLiveStreamingConfig != null 
        && streamID.equals(directLiveStreamingConfig.getStreamID()) 
        && isTranscodingStreamingStarted) {   
      mClient.stopLiveStreaming(transcodingLiveStreamingConfig); // 若单路转推成功，且存在合流转推任务，需停止合流转推
    }
    if (transcodingLiveStreamingConfig != null 
        && streamID.equals(transcodingLiveStreamingConfig.getStreamID())) {
      if (isDirectStreamingStarted) {
        mClient.stopLiveStreaming(directLiveStreamingConfig); // 若合流转推成功，且存在单路转推任务，需停止单路转推
      }
      mClient.setTranscodingLiveStreamingTracks(transcodingStreamID, transcodingLiveStreamingTracks); // 配置合流布局
    }
  }

  @Override
  public void onStopped(String streamID) {
		// 转推任务成功停止时触发此回调
  }

  @Override
  public void onTranscodingTracksUpdated(String streamID) {
 		// 转推任务配置更新时触发此回调
  }

  @Override
  public void onError(String streamID, QNLiveStreamingErrorInfo errorInfo) {
    // 转推任务出错时触发此回调
  }
};
client.setLiveStreamingListener(liveStreamingListener); // 设置 CDN 转推事件监听，参考直播场景伪代码，仅需初始化时设置一次即可
```

2. 主播双方通过业务服务器进行 PK 请求的协商，在协商成功后，主播双方需要获取到远端房间的 roomID 和 roomToken，以此来进行跨房媒体转发。伪代码参考如下：

```java
// 此处伪代码仅作为示例，实际的 PK 请求协商需要您的业务服务自行实现
// 假设主播 A 向主播 B 发起 PK 请求

// 主播 A 发起 PK 请求
startPk(remoteRoomID, new PKResultCallback() {
  @Override
  onResult(String remoteRoomID, String remoteRoomToken) {
    // 记录远端房间的 roomID 和 roomToken
    BRoomID = remoteRoomID;
    BRoomToken = remoteRoomToken;
  }
});

// 主播 B 接收并回复 PK 请求
replyPk(remoteRoomID, new PKResultCallback() {
  @Override
  onResult(String remoteRoomID, String remoteRoomToken) {
    // 记录远端房间的 roomID 和 roomToken
    ARoomID = remoteRoomID;
    ARoomToken = remtoeRoomToken;
  }
});
```

3. 在 PK 请求协商完毕后，两个主播开始 PK 流程，该过程需要**开启跨房媒体转发**，并完成**单路转推到合流转推任务的切换**，主播双方的实现逻辑相同，这里以主播 A 为例，伪代码参考如下：

```java
// 初始化跨房媒体转发配置信息
QNMediaRelayInfo srcRoomInfo = new QNMediaRelayInfo(roomID, roomToken); // 初始化源房间信息
QNMediaRelayConfiguration mediaRelayConfiguration = new QNMediaRelayConfiguration(srcRoomInfo); // 初始化跨房媒体转发配置类
QNMediaRelayInfo destRelayRoomInfo = new QNMediaRelayInfo(BRoomID, BRoomToken); // 初始化目标房间信息
mediaRelayConfiguration.addDestRoomInfo(destRelayRoomInfo); // 设置目标房间信息

// 开启跨房媒体转发
// 成功完成跨房媒体转发后，远端用户会收到 QNClientEventListener.onUserPublished 和 QNClientEventListener.onSubscribed 回调，可在回调中开启合流转推任务
client.startMediaRelay(mediaRelayConfiguration, new QNMediaRelayResultCallback() {
  @Override
  public void onResult(Map<String, QNMediaRelayState> map) {
    if (stateMap.containsKey(BRoomID) && stateMap.get(BRoomID) == QNMediaRelayState.SUCCESS) {
        // 成功完成跨房媒体转发，可以在订阅到远端流之后开启合流转推
    }
  }

  @Override
  public void onError(int errorCode, String description) {
		// 跨房媒体转发出现异常
  }
});

// 开启合流转推任务
// 通过监听 QNClientEventListener.onSubscribed 回调订阅到远端音视频流之后，执行此步骤
@Override
public void onSubscribed(String remoteUserID, List<QNRemoteAudioTrack> remoteAudioTracks, List<QNRemoteVideoTrack> remoteVideoTracks) {
  QNTranscodingLiveStreamingConfig transcodingLiveStreamingConfig = new QNTranscodingLiveStreamingConfig(); // 创建合流转推配置类对象
  transcodingLiveStreamingConfig.setStreamID("transcoding_stream_ID"); // 设置合流任务 id，该 id 为合流任务的唯一标识符
  transcodingLiveStreamingConfig.setWidth(Config.STREAMING_WIDTH); // 设置合流画布宽度
  transcodingLiveStreamingConfig.setHeight(Config.STREAMING_HEIGHT); // 设置合流画布高度
  transcodingLiveStreamingConfig.setBitrate(Config.STREAMING_BITRATE); // 设置合流任务的码率，单位：kbps
  transcodingLiveStreamingConfig.setVideoFrameRate(Config.STREAMING_FPS); // 设置合流任务的帧率
  transcodingLiveStreamingConfig.setUrl("publish_url"); // 设置合流任务的推流地址，该地址需和单路转推时保持一致，并使得 SerialNum 自增以提高直播流优先级
  mClient.startLiveStreaming(transcodingLiveStreamingConfig); // 开启合流转推
}

// 停止单路转推任务
// 合流转推成功后，会触发 QNLiveStreamingListener.onStarted 回调接口，在回调中执行此步骤
@Override
public void onStarted(String streamID) {
  client.stopLiveStreaming(directLiveStreamingConfig);
}

// 配置合流布局
// 合流转推成功后，会触发 QNLiveStreamingListener.onStarted 回调接口，在回调中执行此步骤
// 合流布局的位置需要根据需求自行定义，这里以配置布局在左上角，宽高占合流画面一半为例
@Override
public void onStarted(String streamID) {
  QNTranscodingLiveStreamingTrack liveStreamingTrack = new QNTranscodingLiveStreamingTrack();
  liveStreamingTrack.setTrackID(track.getTrackID()); // 设置待合流的 TrackID
  liveStreamingTrack.setX(0); // 设置 Track 在合流布局中位置的左上角 x 坐标，仅视频需要
  liveStreamingTrack.setY(0); // 设置 Track 在合流布局中位置的左上角 y 坐标，仅视频需要
  liveStreamingTrack.setZOrder(0); // 设置合流层级，值越大，画面层级越高，仅视频需要
  liveStreamingTrack.setWidth(streamingWidth / 2); // 设置 Track 在合流布局中的宽度，仅视频需要
  liveStreamingTrack.setHeight(streamingHeight / 2); // 设置 Track 在合流布局中的高度，仅视频需要
  List<QNTranscodingLiveStreamingTrack> transcodingLiveStreamingTracks = new ArrayList<>();
  transcodingLiveStreamingTracks.add(liveStreamingTrack); // 添加合流布局，可以添加多个
  client.setTranscodingLiveStreamingTracks(transcodingStreamID, transcodingLiveStreamingTracks); // 配置合流布局到合流任务中
}
```

4. PK 结束时，主播切换回单独直播的场景，主播双方需要**停止跨房媒体转发**，并将**合流转推任务切换到单路转推任务**，主播双方的实现逻辑相同，同样以主播 A 为例，伪代码参考如下：

```java
// 停止跨房媒体转发
client.stopMediaRelay(new QNMediaRelayResultCallback() {
  @Override
  public void onResult(Map<String, QNMediaRelayState> map) {
    if (stateMap.containsKey(BRoomID) && stateMap.get(BRoomID) == QNMediaRelayState.STOPPED) {
      // 成功结束跨房媒体转发后，开启单路转推任务
      client.startLiveStreaming(directLiveStreamingConfig);
    }
  }

  @Override
  public void onError(int errorCode, String description) {
		// 跨房媒体转发出现异常
  }
});

// 停止合流转推任务
// 单路转推成功后，会触发 QNLiveStreamingListener.onStarted 回调接口，在回调中执行此步骤
@Override
public void onStarted(String streamID) {
  client.stopLiveStreaming(transcodingLiveStreamingConfig);
}
```

经过上述步骤，即可实现一个完整的直播 PK 的切换场景，其中需要注意如下几点：

- **单路转推和合流转推的推流地址需要保证相同，且需要在推流地址后面加上 `serialnum` 的参数，如 "rtmp://domain/app/streamName?serialnum=xxx"，其中，serialnum 决定流的优先级，从 1 开始递增，值越大，优先级越高，优先级低的流会被停止掉，这样既可以使切换变得平滑，也可以避免出现两路流不停抢流的现象。**

> **上述步骤是以伪代码的形式描述如何进行直播 PK 场景的实现，更详细的处理逻辑可参考 [QNRTCLive-Android](https://github.com/pili-engineering/QNRTCLive-Android)**

### 语音房
语音房场景，即纯语音互动直播，观众可随时上麦和主播进行语音交互。适用于剧本杀、狼人杀等场景。

该场景的实现对于连麦的操作是非常简单的，简单来说就是对房间内用户音频发布的控制，复杂的则是信令业务上的交互。这里我们仅基于连麦的操作进行阐述，信令交互可以参考我们的服务端示例，也可以基于您的业务场景自行实现。

为了实现上述场景，连麦模块您可以参考如下实现方式：

#### 观众观看直播
语音房的直播场景，其实本质是观众加入到 RTC 房间并只进行订阅操作即可

#### 观众上下麦
创建本地音频采集 Track：

```java
QNMicrophoneAduioTrack microphoneAudioTrack = QNRTC.createMicrophoneAudioTrack();
```

在加入房间之后，观众可以通过信令向主播发起上麦请求，主播同意后，观众调用如下接口进行音频的发布即可完成上麦操作：

```java
client.publish(publishResultCallback, microphoneAudioTrack);
```

下麦场景同样是在信令交互完成后，取消发布音频即可：

```java
client.unpublish(microphoneAudioTrack);
```

#### 静音操作
观众连麦过程中，可能需要暂时性的将麦克风静音，可通过调用如下接口实现：

```java
microphoneAudioTrack.setVolume(0.0f);
microphoneAudioTrack.setVolume(1.0f);
```

> **上述步骤是以伪代码的形式描述如何进行语音房的实现，更详细的处理逻辑可参考 [QNRTCLive-Android](https://github.com/pili-engineering/QNRTCLive-Android)**

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
