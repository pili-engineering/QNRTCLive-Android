package com.qiniu.droid.rtc.live.demo.utils;

import android.content.Context;

import com.qiniu.android.dns.Domain;
import com.qiniu.android.http.dns.Dns;
import com.qiniu.android.http.dns.IDnsNetworkAddress;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.GlobalConfiguration;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.droid.rtc.live.demo.model.QNDnsNetworkAddress;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.WorkerThread;

public class QNUploadManager {
    private static final String TAG = "QNUploadManager";

    private Context mContext;
    private UploadManager mUploadManager;

    private volatile boolean mIsCancelled = false;

    private UpCancellationSignal mUpLoadCancellationSignal = () -> mIsCancelled;

    public interface OnUploadFeedbackCallback {
        void onUploadProgress(double percent);
        void onUploadSuccess();
        void onUploadFailed(String reason);
    }

    public QNUploadManager(Context context) {
        this(context, null);
    }

    public QNUploadManager(Context context, Configuration configuration) {
        if (configuration == null) {
            //config配置上传参数
            configuration = new Configuration.Builder()
                    .connectTimeout(10)
                    .responseTimeout(60)
                    .build();
            GlobalConfiguration.getInstance().dns = buildDefaultDns();
        }
        mContext = context;
        mUploadManager = new UploadManager(configuration);
    }


    @WorkerThread
    public void uploadFeedbackAttachment(File attachment, String token, OnUploadFeedbackCallback callback) {
        mIsCancelled = false;
        UploadOptions uploadOptions = new UploadOptions(null, null, true, (key, percent) -> {
            if (callback != null) {
                callback.onUploadProgress(percent);
            }
        }, mUpLoadCancellationSignal);

        mUploadManager.put(attachment, attachment.getName(), token, (key, info, response) -> {
            if (callback != null) {
                if (info.isOK()) {
                    callback.onUploadSuccess();
                } else {
                    callback.onUploadFailed(info.error);
                }
            }
        }, uploadOptions);
    }

    public void cancelUpload() {
        mIsCancelled = true;
    }

    private Dns buildDefaultDns() {
        return hostname -> {
            InetAddress[] ips;
            try {
                ips = Utils.getDefaultDnsManager().queryInetAdress(new Domain(hostname));
            } catch (IOException e) {
                e.printStackTrace();
                throw new UnknownHostException(e.getMessage());
            }
            if (ips.length == 0) {
                throw new UnknownHostException(hostname + " resolve failed.");
            }

            ArrayList<IDnsNetworkAddress> addressList = new ArrayList<>();
            for (InetAddress inetAddress : ips) {
                QNDnsNetworkAddress address = new QNDnsNetworkAddress(inetAddress.getHostName(), inetAddress.getHostAddress(), 120L, null, (new Date()).getTime());
                addressList.add(address);
            }
            return addressList;
        };
    }
}
