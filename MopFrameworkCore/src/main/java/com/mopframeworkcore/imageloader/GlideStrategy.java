package com.mopframeworkcore.imageloader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mopframeworkcore.imageloader.listener.FileProgressListener;
import com.mopframeworkcore.imageloader.listener.ImageLoadListener;
import com.mopframeworkcore.imageloader.progress.ProgressListener;
import com.mopframeworkcore.imageloader.progress.ProgressManager;
import com.mopframeworkcore.imageloader.progress.body.ProgressInfo;
import com.mopframeworkcore.imageloader.transformation.BlurTransformation;
import com.mopframeworkcore.imageloader.transformation.RadiusTransformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GlideStrategy implements IStrategy {
    @Override
    public void clearImageDiskCache(final Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(context.getApplicationContext()).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(context.getApplicationContext()).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearImageMemoryCache(Context context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context.getApplicationContext()).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trimMemory(Context context, int level) {
        Glide.get(context).trimMemory(level);
    }

    @Override
    public String getCacheSize(Context context) {
        try {
            return CommonUtils.getFormatSize(CommonUtils.getFolderSize(Glide.getPhotoCacheDir(context.getApplicationContext())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void loadBitmap(Context context, String url, final ImageLoaderConfiguration imageLoaderConfiguration, final ImageLoadListener imageLoadListener) {
        int resizeWidth = imageLoaderConfiguration == null || imageLoaderConfiguration.getResizeWidth() == -1 ? Target.SIZE_ORIGINAL : imageLoaderConfiguration.getResizeWidth();
        int resizeHeight = imageLoaderConfiguration == null || imageLoaderConfiguration.getResizeHeight() == -1 ? Target.SIZE_ORIGINAL : imageLoaderConfiguration.getResizeHeight();
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>(resizeWidth, resizeHeight) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (imageLoadListener != null)
                            imageLoadListener.onResourceReady(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        if (imageLoadListener != null)
                            imageLoadListener.onLoadCleared(placeholder);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        if (imageLoadListener != null)
                            imageLoadListener.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (imageLoadListener != null)
                            imageLoadListener.onLoadFailed();
                    }
                });
    }

    @Override
    public void displayImage(@NonNull String url, @NonNull ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration) {
        boolean isGif = CommonUtils.isGif(url);
        boolean isFitCenter = imageLoaderConfiguration != null && imageLoaderConfiguration.getSacleType() == ImageLoaderConfiguration.SACLE_FIT_CENTER;
        DiskCacheStrategy diskCacheStrategy = imageLoaderConfiguration != null ? imageLoaderConfiguration.getDiskCacheStrategy() : DiskCacheStrategy.DATA;
        boolean isShowPlace = imageLoaderConfiguration != null && imageLoaderConfiguration.isShowPlace();
        boolean isBlur = imageLoaderConfiguration != null && imageLoaderConfiguration.isBlur();
        int blurProgress = imageLoaderConfiguration == null ? 15 : imageLoaderConfiguration.getBlurProgress();
        Drawable placeHolder = imageLoaderConfiguration == null || !isShowPlace ? imageView.getDrawable() : imageView.getContext().getResources().getDrawable(imageLoaderConfiguration.getPlaceHolder());
        Drawable error = imageLoaderConfiguration == null || !isShowPlace ? imageView.getDrawable() : imageView.getContext().getResources().getDrawable(imageLoaderConfiguration.getError());
        RequestManager requestManager = Glide.with(imageView.getContext());
        if (isGif)
            requestManager.asGif();
        else
            requestManager.asDrawable();

        RequestOptions requestOptions = new RequestOptions();
        if (isFitCenter)
            requestOptions.fitCenter();
        else
            requestOptions.centerCrop();
        if (isBlur)
            requestOptions.transform(new BlurTransformation(imageView.getContext(), blurProgress));

        requestManager.load(url)
                .apply(requestOptions.diskCacheStrategy(diskCacheStrategy)
                        .skipMemoryCache(true)
                        .dontAnimate()
                        .placeholder(placeHolder)
                        .error(error))
                .into(imageView);
    }

    @Override
    public void displayImageRound(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration, int roundSize) {
        displayImageRound(url, imageView, imageLoaderConfiguration, roundSize, 0, RadiusTransformation.CornerType.ALL);
    }


    public void displayImageRound(String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration, int radius, int margin, RadiusTransformation.CornerType cornerType) {
        boolean isGif = CommonUtils.isGif(url);
        boolean isFitCenter = imageLoaderConfiguration != null && imageLoaderConfiguration.getSacleType() == ImageLoaderConfiguration.SACLE_FIT_CENTER;
        DiskCacheStrategy diskCacheStrategy = imageLoaderConfiguration != null ? imageLoaderConfiguration.getDiskCacheStrategy() : DiskCacheStrategy.DATA;
        boolean isShowPlace = imageLoaderConfiguration != null && imageLoaderConfiguration.isShowPlace();
        boolean isBlur = imageLoaderConfiguration != null && imageLoaderConfiguration.isBlur();
        int blurProgress = imageLoaderConfiguration == null ? 15 : imageLoaderConfiguration.getBlurProgress();
        Drawable placeHolder = imageLoaderConfiguration == null || !isShowPlace ? imageView.getDrawable() : imageView.getContext().getResources().getDrawable(imageLoaderConfiguration.getPlaceHolder());
        Drawable error = imageLoaderConfiguration == null || !isShowPlace ? imageView.getDrawable() : imageView.getContext().getResources().getDrawable(imageLoaderConfiguration.getError());
        RequestManager requestManager = Glide.with(imageView.getContext());
        if (isGif)
            requestManager.asGif();
        else
            requestManager.asDrawable();

        RequestOptions requestOptions = new RequestOptions();
        if (isFitCenter)
            requestOptions.fitCenter();
        else
            requestOptions.centerCrop();
        if (isBlur)
            requestOptions.transforms(new BlurTransformation(imageView.getContext(), blurProgress), new RadiusTransformation(radius, margin, cornerType));
        else
            requestOptions.transform(new RadiusTransformation(radius, margin, cornerType));

        requestManager.load(url)
                .apply(requestOptions.diskCacheStrategy(diskCacheStrategy)
                        .skipMemoryCache(true)
                        .dontAnimate()
                        .placeholder(placeHolder)
                        .error(error))
                .into(imageView);
    }

    @Override
    public void displayImageWithProgress(final String url, ImageView imageView, ImageLoaderConfiguration imageLoaderConfiguration, ProgressListener listener) {
        boolean isGif = CommonUtils.isGif(url);
        boolean isFitCenter = imageLoaderConfiguration != null && imageLoaderConfiguration.getSacleType() == ImageLoaderConfiguration.SACLE_FIT_CENTER;
        DiskCacheStrategy diskCacheStrategy = imageLoaderConfiguration != null ? imageLoaderConfiguration.getDiskCacheStrategy() : DiskCacheStrategy.DATA;
        boolean isShowPlace = imageLoaderConfiguration != null && imageLoaderConfiguration.isShowPlace();
        boolean isBlur = imageLoaderConfiguration != null && imageLoaderConfiguration.isBlur();
        int blurProgress = imageLoaderConfiguration == null ? 15 : imageLoaderConfiguration.getBlurProgress();
        Drawable placeHolder = imageLoaderConfiguration == null || !isShowPlace ? imageView.getDrawable() : imageView.getContext().getResources().getDrawable(imageLoaderConfiguration.getPlaceHolder());
        Drawable error = imageLoaderConfiguration == null || !isShowPlace ? imageView.getDrawable() : imageView.getContext().getResources().getDrawable(imageLoaderConfiguration.getError());
        RequestManager requestManager = Glide.with(imageView.getContext());
        if (isGif)
            requestManager.asGif();
        else
            requestManager.asDrawable();

        RequestOptions requestOptions = new RequestOptions();
        if (isFitCenter)
            requestOptions.fitCenter();
        else
            requestOptions.centerCrop();
        if (isBlur)
            requestOptions.transforms(new BlurTransformation(imageView.getContext(), blurProgress));

        if (listener != null)
            ProgressManager.getInstance().addResponseListener(url, listener);
        requestManager.load(url)
                .apply(requestOptions.diskCacheStrategy(diskCacheStrategy)
                        .skipMemoryCache(true)
                        .dontAnimate()
                        .placeholder(placeHolder)
                        .error(error))
                .into(new DrawableImageViewTarget(imageView) {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        ProgressManager.getInstance().notifyOnErorr(url, new Exception("load image fail"));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        ProgressManager.getInstance().notifyOnSuccess(url);
                    }
                });
    }

    @Override
    public void downloadOnlyImage(Context ctx, final String url, int width, int height, final FileProgressListener fileProgressListener) {
        int resizeWidth = width == 0 ? Target.SIZE_ORIGINAL : width;
        int resizeHeight = height == 0 ? Target.SIZE_ORIGINAL : height;
        ProgressListener progressListener = null;
        if (fileProgressListener != null)
            progressListener = new ProgressListener() {
                @Override
                public void onProgress(ProgressInfo progressInfo) {
                    fileProgressListener.onProgress(progressInfo);
                }

                @Override
                public void onError(long id, Exception e) {
                    fileProgressListener.onError(id, e);
                }
            };
        if (progressListener != null)
            ProgressManager.getInstance().addResponseListener(url, progressListener);
        try {
            Glide.with(ctx)
                    .downloadOnly()
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .override(resizeWidth, resizeHeight))
                    .load(url)
                    .into(new FutureTarget<File>() {
                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            ProgressManager.getInstance().notifyOnErorr(url, new Exception("load image fail"));
                        }

                        @Override
                        public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                            ProgressManager.getInstance().notifyOnSuccess(url);
                            if (fileProgressListener != null)
                                fileProgressListener.onFile(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void getSize(@NonNull SizeReadyCallback cb) {

                        }

                        @Override
                        public void removeCallback(@NonNull SizeReadyCallback cb) {

                        }

                        @Override
                        public void setRequest(@Nullable Request request) {

                        }

                        @Nullable
                        @Override
                        public Request getRequest() {
                            return null;
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onStop() {

                        }

                        @Override
                        public void onDestroy() {

                        }

                        @Override
                        public boolean cancel(boolean mayInterruptIfRunning) {
                            return false;
                        }

                        @Override
                        public boolean isCancelled() {
                            return false;
                        }

                        @Override
                        public boolean isDone() {
                            return false;
                        }

                        @Override
                        public File get() throws InterruptedException, ExecutionException {
                            return null;
                        }

                        @Override
                        public File get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                            return null;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveImage(final Context context, final String url, final String savePath, final String saveFileName, final FileProgressListener fileProgressListener) {
        if (!CommonUtils.isSDCardExsit() || TextUtils.isEmpty(url)) {
            fileProgressListener.onError(1L, new Exception("sdCard not exsit or url is empty"));
            return;
        }
        InputStream fromStream = null;
        OutputStream toStream = null;
        downloadOnlyImage(context, url, 0, 0, new FileProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                if (fileProgressListener != null)
                    fileProgressListener.onProgress(progressInfo);
            }

            @Override
            public void onError(long id, Exception e) {
                if (fileProgressListener != null)
                    fileProgressListener.onError(id, e);
            }

            @Override
            public void onFile(File cacheFile) {
                if (fileProgressListener != null)
                    fileProgressListener.onFile(cacheFile);
                InputStream fromStream = null;
                OutputStream toStream = null;
                try {
                    if (cacheFile == null || !cacheFile.exists()) {
                        return;
                    }
                    File dir = new File(savePath);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    File file = new File(dir, saveFileName + CommonUtils.getPicType(cacheFile.getAbsolutePath()));

                    fromStream = new FileInputStream(cacheFile);
                    toStream = new FileOutputStream(file);
                    byte length[] = new byte[1024];
                    int count;
                    while ((count = fromStream.read(length)) > 0) {
                        toStream.write(length, 0, count);
                    }
                    //用广播通知相册进行更新相册
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri = Uri.fromFile(file);
                    intent.setData(uri);
                    context.sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (fileProgressListener != null)
                        fileProgressListener.onError(1L, e);
                } finally {
                    if (fromStream != null) {
                        try {
                            fromStream.close();
                            toStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            fromStream = null;
                            toStream = null;
                        }
                    }
                }
            }
        });
    }
}
