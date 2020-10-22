package com.qiniu.droid.rtc.live.demo.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static net.lingala.zip4j.model.enums.CompressionMethod.DEFLATE;

public class ZipManager {
    private static final int WHAT_START = 100;
    private static final int WHAT_FINISH = 101;
    private static final int WHAT_PROGRESS = 102;

    private static volatile boolean mIsCanceled = false;

    private static Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            switch (msg.what) {
                case WHAT_START:
                    ((ZipCallback) msg.obj).onStart();
                    break;
                case WHAT_PROGRESS:
                    ((ZipCallback) msg.obj).onProgress(msg.arg1);
                    break;
                case WHAT_FINISH:
                    ((ZipCallback) msg.obj).onFinish(true);
                    break;
            }
        }
    };

    public interface ZipCallback {
        /**
         * 开始
         */
        void onStart();

        /**
         * 进度回调
         *
         * @param percent 完成百分比
         */
        void onProgress(int percent);

        /**
         * 完成
         *
         * @param success 是否成功
         */
        void onFinish(boolean success);
    }

    /**
     * 压缩文件或者文件夹
     *
     * @param targetPath          被压缩的文件路径
     * @param destinationFilePath 压缩后生成的文件路径
     * @param callback            压缩进度回调
     */
    public static void zip(String targetPath, String destinationFilePath, ZipCallback callback) {
        if (!Zip4jUtil.isStringNotNullAndNotEmpty(targetPath) || !Zip4jUtil.isStringNotNullAndNotEmpty(destinationFilePath)) {
            if (callback != null) callback.onFinish(false);
            return;
        }
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);
            ZipFile zipFile = new ZipFile(destinationFilePath);
            zipFile.setRunInThread(true);
            File targetFile = new File(targetPath);
            if (targetFile.isDirectory()) {
                zipFile.addFolder(targetFile, parameters);
            } else {
                zipFile.addFile(targetFile, parameters);
            }
            handleZipProgress(callback, zipFile);
        } catch (Exception e) {
            if (callback != null) callback.onFinish(false);
        }
    }

    /**
     * 压缩多个文件
     *
     * @param list                被压缩的文件集合
     * @param destinationFilePath 压缩后生成的文件路径
     * @param callback            回调
     */
    public static void zip(List<File> list, String destinationFilePath, final ZipCallback callback) {
        if (list == null || list.isEmpty() || !Zip4jUtil.isStringNotNullAndNotEmpty(destinationFilePath)) {
            if (callback != null) {
                callback.onFinish(false);
            }
            return;
        }
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);
            ZipFile zipFile = new ZipFile(destinationFilePath);
            zipFile.setRunInThread(true);
            zipFile.addFiles(list, parameters);
            handleZipProgress(callback, zipFile);
        } catch (Exception e) {
            if (callback != null) {
                callback.onFinish(false);
            }
        }
    }

    public static void cancel() {
        mIsCanceled = true;
    }

    //Handler send msg
    private static void handleZipProgress(final ZipCallback callback, ZipFile zipFile) {
        if (callback == null) {
            return;
        }
        mIsCanceled = false;
        mUIHandler.obtainMessage(WHAT_START, callback).sendToTarget();
        final ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mIsCanceled) {
                    progressMonitor.setCancelAllTasks(true);
                    timer.cancel();
                    timer.purge();
                    zipFile.getFile().delete();
                    callback.onFinish(false);
                    return;
                }
                mUIHandler.obtainMessage(WHAT_PROGRESS, progressMonitor.getPercentDone(), 0, callback).sendToTarget();
                if (progressMonitor.getResult() == ProgressMonitor.Result.SUCCESS) {
                    mUIHandler.obtainMessage(WHAT_FINISH, callback).sendToTarget();
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 300);
    }
}
