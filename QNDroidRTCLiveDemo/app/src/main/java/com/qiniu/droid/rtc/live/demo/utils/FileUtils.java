package com.qiniu.droid.rtc.live.demo.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okio.BufferedSink;
import okio.Okio;

public class FileUtils {

    private static long sDirCountFileNumber;
    private static long sCurrentCopiedFileNumber;

    public interface CopyFileListener {
        void onProgress(float progress, String currentFileName);
    }

    public static void copyAssets(AssetManager assets, String path, String rootDir, @Nullable CopyFileListener listener) throws IOException {
        sDirCountFileNumber = getAssetsPathList(path).size();
        sCurrentCopiedFileNumber = 0;
        recursionCopy(assets, path, rootDir, listener);
    }

    /**
     * 递归拷贝 Asset 目录中的文件到 rootDir 中
     *
     * @param assets
     * @param path
     * @param rootDir
     * @throws IOException
     */
    private static void recursionCopy(AssetManager assets, String path, String rootDir, CopyFileListener listener) throws IOException {
        List<String> filePaths = getAssetsPathList(path);
        for (String filePath : filePaths) {
            InputStream input = assets.open(filePath.substring(7));
            File dest = new File(rootDir, filePath.substring(7));
            outputToFile(input, dest);
            sCurrentCopiedFileNumber++;
            if (listener != null) {
                listener.onProgress(sCurrentCopiedFileNumber * 100.0f / sDirCountFileNumber, filePath);
            }
        }
    }

    private static List<String> getAssetsPathList(String path) {
        List<String> paths = new ArrayList<>();
        try {
            ZipFile zf = new ZipFile(AppUtils.getApp().getApplicationInfo().sourceDir);
            try {
                for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements(); ) {
                    ZipEntry ze = e.nextElement();
                    String name = ze.getName();
                    if (name.startsWith("assets/" + path)) {
                        paths.add(name);
                    }
                }
            } finally {
                zf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * 从输入流中输出到指定文件
     *
     * @param inputStream 输入流
     * @param destFile    目标文件
     * @throws IOException
     */
    public static void outputToFile(InputStream inputStream, File destFile) throws IOException {
        if (destFile.exists()) {
            return;
        }
        File file = destFile.getParentFile();
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        try (BufferedSink bufferedSink = Okio.buffer(Okio.sink(destFile))){
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                bufferedSink.write(buffer, 0, bytesRead);
            }
        } finally {
            try {
                inputStream.close();
            }catch (IOException ignored){
            }
        }
    }

    /**
     * 清空文件夹
     *
     * @param dir 目标文件夹
     * @return 是否成功
     */
    public static boolean clearDir(File dir) {
        if (!dir.exists()) {
            return true;
        }
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                clearDir(file);
                file.delete();
            } else {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        return true;
    }

    public static String getFilePathByUri(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // Good job vivo
                if ("5D68-9217".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
