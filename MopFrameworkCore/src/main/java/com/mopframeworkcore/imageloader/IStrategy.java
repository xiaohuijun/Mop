package com.mopframeworkcore.imageloader;

import android.content.Context;
import android.widget.ImageView;

import com.mopframeworkcore.imageloader.listener.FileProgressListener;
import com.mopframeworkcore.imageloader.listener.ImageLoadListener;
import com.mopframeworkcore.imageloader.progress.ProgressListener;


public interface IStrategy {
    //清除硬盘缓存
    void clearImageDiskCache(Context context);

    //清除内存缓存
    void clearImageMemoryCache(Context context);

    //根据不同的内存状态，来响应不同的内存释放策略
    void trimMemory(Context context, int level);

    //获取缓存大小
    String getCacheSize(Context context);

    void loadBitmap(Context context, String url, ImageLoaderConfiguration imageLoaderConfiguration, ImageLoadListener imageLoadListener);

    void displayImage(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration);

    void displayImageRound(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration, int roundSize);

    void displayImageWithProgress(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration, ProgressListener listener);

    void downloadOnlyImage(Context ctx, String url, int width, int height, FileProgressListener fileProgressListener);

    void saveImage(final Context context, final String url, final String savePath, final String saveFileName, final FileProgressListener fileProgressListener);
}
