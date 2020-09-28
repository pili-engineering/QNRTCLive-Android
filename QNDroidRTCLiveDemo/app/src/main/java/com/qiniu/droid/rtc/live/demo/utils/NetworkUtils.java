package com.qiniu.droid.rtc.live.demo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresPermission;

import com.qiniu.droid.rtc.live.demo.receiver.NetworkChangedReceiver;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

public class NetworkUtils {

    private static boolean sConnected;

    public enum NetworkType {
        NETWORK_ETHERNET,
        NETWORK_WIFI,
        NETWORK_5G,
        NETWORK_4G,
        NETWORK_3G,
        NETWORK_2G,
        NETWORK_UNKNOWN,
        NETWORK_NO
    }

    public static boolean isConnected() {
        return sConnected;
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    static void registerNetworkStatusChangedListener() {
        NetworkChangedReceiver.getInstance().registerListener(new MyNetworkStatusChangedListener());
        sConnected = isAvailableByPing();
    }

    static void unregisterNetworkStatusChangedListener() {
        NetworkChangedReceiver.getInstance().unregisterListener();
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static boolean isEthernet() {
        final ConnectivityManager cm = (ConnectivityManager) AppUtils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        final NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (info == null) {
            return false;
        }
        NetworkInfo.State state = info.getState();
        if (null == state) {
            return false;
        }
        return state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING;
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) AppUtils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return null;
        }
        return cm.getActiveNetworkInfo();
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static NetworkType getNetworkType() {
        if (isEthernet()) {
            return NetworkType.NETWORK_ETHERNET;
        }
        NetworkInfo info = getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NetworkType.NETWORK_2G;

                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NetworkType.NETWORK_3G;

                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NetworkType.NETWORK_4G;

                    case TelephonyManager.NETWORK_TYPE_NR:
                        return NetworkType.NETWORK_5G;
                    default:
                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA") || subtypeName.equalsIgnoreCase("WCDMA") || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            return NetworkType.NETWORK_3G;
                        } else {
                            return NetworkType.NETWORK_UNKNOWN;
                        }
                }
            } else {
                return NetworkType.NETWORK_UNKNOWN;
            }
        }
        return NetworkType.NETWORK_NO;
    }

    /**
     * 使用命令行 ping 阿里云的 DNS 来判断网络是否可用
     *
     * @return 网络是否可用
     */
    private static boolean isAvailableByPing() {
        // 该 ip 是阿里云的 DNS
        final String realIp = "223.5.5.5";
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format("ping -c 1 %s", realIp), false);
        return result.result == 0;
    }

    public interface OnNetworkStatusChangedListener {
        void onDisconnected();

        void onConnected(NetworkType networkType);
    }

    private static class MyNetworkStatusChangedListener implements OnNetworkStatusChangedListener{

        @Override
        public void onDisconnected() {
            sConnected = false;
            ToastUtils.showShortToast("网络已断开");
        }

        @Override
        public void onConnected(NetworkType networkType) {
            sConnected = true;
            // 二次检查
            sConnected = isAvailableByPing();
            if (!sConnected) {
                onDisconnected();
            }
        }
    }
}
