package com.mopframeworkcore.imageloader.listener;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface ImageLoadListener {
    void onLoadStarted(Drawable placeholder);

    void onLoadFailed();

    void onResourceReady(Bitmap resource);

    void onLoadCleared(Drawable placeholder);

}
