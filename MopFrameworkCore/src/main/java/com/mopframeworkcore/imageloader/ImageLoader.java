package com.mopframeworkcore.imageloader;

import android.content.Context;
import android.widget.ImageView;

import com.mopframeworkcore.imageloader.listener.FileProgressListener;
import com.mopframeworkcore.imageloader.listener.ImageLoadListener;
import com.mopframeworkcore.imageloader.progress.ProgressListener;


public class ImageLoader {
    private static ImageLoader mInstance;
    private IStrategy mStrategy;

    public ImageLoader() {
        mStrategy = new GlideStrategy();
    }

    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader();
                    return mInstance;
                }
            }
        }
        return mInstance;
    }

    public void clearImageDiskCache(Context context) {
        mStrategy.clearImageDiskCache(context);
    }

    //清除内存缓存
    public void clearImageMemoryCache(Context context) {
        mStrategy.clearImageMemoryCache(context);
    }

    //根据不同的内存状态，来响应不同的内存释放策略
    public void trimMemory(Context context, int level) {
        mStrategy.trimMemory(context, level);
    }

    //获取缓存大小
    public String getCacheSize(Context context) {
        return mStrategy.getCacheSize(context);
    }

    public void loadBitmap(Context context, String url, ImageLoaderConfiguration imageLoaderConfiguration, ImageLoadListener imageLoadListener) {
        mStrategy.loadBitmap(context, url, imageLoaderConfiguration, imageLoadListener);
    }

    public void displayImage(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration) {
        mStrategy.displayImage(url, imageView, imageLoaderConfiguration);
    }

    public void displayImageRound(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration, int roundSize) {
        mStrategy.displayImageRound(url, imageView, imageLoaderConfiguration, roundSize);
    }

    public void displayImageWithProgress(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration, ProgressListener listener) {
        mStrategy.displayImageWithProgress(url, imageView, imageLoaderConfiguration, listener);
    }

    public void downloadOnlyImage(Context ctx, String url, int width, int height, FileProgressListener fileProgressListener) {
        mStrategy.downloadOnlyImage(ctx, url, width, height, fileProgressListener);
    }

    public void saveImage(final Context context, final String url, final String savePath, final String saveFileName, final FileProgressListener fileProgressListener) {
        mStrategy.saveImage(context, url, savePath, saveFileName, fileProgressListener);
    }
}
