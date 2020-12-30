package com.qiniu.droid.rtc.live.demo.im;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.RTCLiveApplication;
import com.qiniu.droid.rtc.live.demo.im.model.Gift;
import com.qiniu.droid.rtc.live.demo.utils.Config;
import com.qiniu.droid.rtc.live.demo.utils.QNAppServer;
import com.qiniu.droid.rtc.live.demo.utils.ThreadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

import static io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED;
import static io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING;

/**
 * 数据接口
 * 由于本 demo 没有App Server，用户信息，聊天室信息 等全部通过DataInterface的接口返回，目前都是写死的数据。 开发者可以修改这些接口，去自己的app server取数据。
 */
public class DataInterface {
    private static final String TAG = "DataInterface";

    public static final String NAV_SERVER = "https://nav.cn.ronghub.com";
    public static final String FILE_SERVER = "up.qbox.me";

    /*appkey   需要改成开发者自己的appKey*/
    public static final String APP_KEY = "qd46yzrfqx8xf";

    private static final String CONFIG_NAME = "chatroom_config";

    public static final String KEY_USER_TOKEN = "userToken";
    public static final String KEY_USER_ID = "userId";

    public static final int DEFAULT_AVATAR = R.drawable.avatar_1;
    private static final int[] AVATARS = {R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6,
            R.drawable.avatar_7,
            R.drawable.avatar_8,
            R.drawable.avatar_9,
            R.drawable.avatar_10
    };

    private static SharedPreferences mSP;
    private static AtomicBoolean mGetTokening = new AtomicBoolean(false);
    private static String mUserName;

    /*是否禁言*/
    private static boolean banStatus = false;

    public static boolean isBanStatus() {
        return banStatus;
    }

    public static void setBanStatus(boolean banStatus) {
        DataInterface.banStatus = banStatus;
    }

    public static void init(Context context) {
        mSP = context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
    }

    public static void putString(String key, String value) {
        editor()
                .putString(key, value)
                .apply();
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String defaultVale) {
        return mSP.getString(key, defaultVale);
    }

    public static SharedPreferences.Editor editor() {
        return mSP.edit();
    }

    public static String getUserName() {
        return mUserName;
    }

    /**
     * 模拟登陆后的逻辑
     */
    public static void setLogin(String userName) {
        mUserName = userName;
        ChatroomKit.setCurrentUser(new UserInfo(getUserId(), getUserName(), Uri.parse(String.valueOf(getRandomNum(AVATARS.length)))));
    }

    public static String getUserId() {
        return getString(KEY_USER_ID);
    }

    public static boolean isLogin() {
        //TODO 仅仅用于展示，没有登录逻辑，所以目前只是做了一个假登录，以是否设置过用户名为判断依据
        return !TextUtils.isEmpty(getUserName());
    }

    /**
     * IM是否连接中
     *
     * @return
     */
    public static boolean isImConnecting() {
        return mGetTokening.get() || RongIMClient.getInstance().getCurrentConnectionStatus() == CONNECTING;
    }

    /**
     * IM是否已连接
     *
     * @return
     */
    public static boolean isImConnected() {
        return RongIMClient.getInstance().getCurrentConnectionStatus() == CONNECTED;
    }

    /**
     * 连接IM
     *
     * @param callback
     */
    public static void connectIM(final RongIMClient.ConnectCallback callback) {
        mGetTokening.set(false);
        String token = getString(KEY_USER_TOKEN);

        if (TextUtils.isEmpty(token)) {
            getToken(callback);
            return;
        }

        RongIMClient.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus status) {
                switch (status) {
                    case CONNECTED://连接成功。
                        Log.i(TAG, "连接成功");
                        break;
                    case DISCONNECTED://断开连接。
                        Log.i(TAG, "断开连接");
                        break;
                    case CONNECTING://连接中。
                        Log.i(TAG, "连接中");
                        break;
                    case NETWORK_UNAVAILABLE://网络不可用。
                        Log.i(TAG, "网络不可用");
                        break;
                    case KICKED_OFFLINE_BY_OTHER_CLIENT://用户账户在其他设备登录，本机会被踢掉线
                        Log.i(TAG, "用户账户在其他设备登录");
                        break;
                }
            }
        });
        ChatroomKit.connect(getString(KEY_USER_TOKEN), new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                Log.i(TAG, "onTokenIncorrect");
                putString(KEY_USER_TOKEN, null);
                if (callback != null) {
                    callback.onTokenIncorrect();
                }
            }

            @Override
            public void onSuccess(String s) {
                putString(KEY_USER_ID, s);
                if (isLogin()) {
                    setLogin(mUserName);
                }
                Log.i(TAG, "connectSuccess");
                if (callback != null)
                    callback.onSuccess(s);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                Log.i(TAG, "connect error code = " + e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    private static void getToken(final RongIMClient.ConnectCallback callback) {
        ThreadUtils.getSingleThreadExecutor().execute(() -> QNAppServer.getInstance().getChatToken(new QNAppServer.OnRequestResultCallback() {
            @Override
            public void onRequestSuccess(String responseMsg) {
                try {
                    JSONObject jsonObject = new JSONObject(responseMsg);
                    String token = jsonObject.optString(Config.KEY_CHAT_TOKEN);
                    putString(KEY_USER_TOKEN, token);
                    connectIM(callback);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mGetTokening.set(false);
            }

            @Override
            public void onRequestFailed(int code, String reason) {
                mGetTokening.set(false);
                if (callback != null) {
                    callback.onTokenIncorrect();
                }
            }
        }));
    }

    /**
     * 退出登录
     */
    public static void logout() {
        ChatroomKit.logout();
        mUserName = null;
    }

    /**
     * 由于服务器全没有存储用户信息，所以使用本地图片模拟获取用户头像
     * @param uri
     * @return
     */
    public static Uri getAvatarUri(Uri uri){
        if (uri == null || TextUtils.isEmpty(uri.toString()))
            return getUri(RTCLiveApplication.getContext(),AVATARS[0]);
        int index = 0;
        try {
            index = Integer.valueOf(uri.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        index = index >= AVATARS.length ? 0 : index;
        return getUri(RTCLiveApplication.getContext(),AVATARS[index]);
    }

    /*获取礼物列表*/
    public static ArrayList<Gift> getGiftList() {
        ArrayList<Gift> gifts = new ArrayList<>();
        String[] giftNames = new String[]{"蛋糕", "气球", "花儿", "项链", "戒指"};
        int[] giftRes = new int[]{R.drawable.gift_cake, R.drawable.gift_ballon, R.drawable.gift_flower, R.drawable.gift_necklace, R.drawable.gift_ring};

        for (int i = 0; i < giftNames.length; i++) {
            Gift gift = new Gift();
            gift.setGiftId("GiftId_" + (i + 1));
            gift.setGiftName(giftNames[i]);
            gift.setGiftRes(giftRes[i]);
            gifts.add(gift);
        }
        return gifts;
    }

    /*获取礼物名*/
    public static String getGiftNameById(String giftId) {
        switch (giftId) {
            case "GiftId_1":
                return "蛋糕";
            case "GiftId_2":
                return "气球";
            case "GiftId_3":
                return "花儿";
            case "GiftId_4":
                return "项链";
            case "GiftId_5":
                return "戒指";
        }
        return null;
    }

    /*根据giftId获取礼物信息*/
    public static Gift getGiftInfo(String giftId) {
        ArrayList<Gift> gifts = getGiftList();
        for (int i = 0; i < gifts.size(); i++) {
            if (gifts.get(i).getGiftId().equals(giftId)) {
                return gifts.get(i);
            }
        }
        return null;
    }

    /*生成随机数*/
    public static int getRandomNum(int max) {
        Random r = new Random();
        return r.nextInt(max);
    }

    public static Uri getUri(Context context, int res) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getResources().getResourcePackageName(res) + "/"
                + context.getResources().getResourceTypeName(res) + "/"
                + context.getResources().getResourceEntryName(res));
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            TrustAllManager trustAllManager = new TrustAllManager();
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{trustAllManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
