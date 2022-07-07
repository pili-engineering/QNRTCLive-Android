package com.qiniu.droid.rtc.live.demo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.Record;
import com.qiniu.android.dns.dns.DnsUdpResolver;
import com.qiniu.android.dns.dns.DohResolver;
import com.qiniu.droid.rtc.live.demo.R;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.qiniu.android.dns.IResolver.DNS_DEFAULT_TIMEOUT;

public class Utils {

    public static String packageName(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            // e.printStackTrace();
        }
        return "";
    }

    public static long randomNumberGenerator() {
        Random random = new Random();
        return Math.abs(random.nextLong());
    }

    public static int getUserAvaterResId(@NotNull String userId) {
        int index = userId.hashCode() % 5;
        switch (index) {
            case 1:
                return R.drawable.img_avater_1;
            case 2:
                return R.drawable.img_avater_2;
            case 3:
                return R.drawable.img_avater_3;
            case 4:
                return R.drawable.img_avater_4;
            default:
                return R.drawable.img_avater_0;
        }
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static int dp2px(Context ctx, float dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }

    public static DnsManager getDefaultDnsManager() {
        IResolver[] resolvers = new IResolver[2];
        // 配置自定义 DNS 服务器地址
        String[] udpDnsServers = new String[]{"223.5.5.5", "114.114.114.114", "1.1.1.1", "208.67.222.222"};
        resolvers[0] = new DnsUdpResolver(udpDnsServers, Record.TYPE_A, DNS_DEFAULT_TIMEOUT);
        // 配置 HTTPDNS 地址
        String[] httpDnsServers = new String[]{"https://223.6.6.6/dns-query", "https://8.8.8.8/dns-query"};
        resolvers[1] = new DohResolver(httpDnsServers, Record.TYPE_A, DNS_DEFAULT_TIMEOUT);
        return new DnsManager(NetworkInfo.normal, resolvers);
    }
}
