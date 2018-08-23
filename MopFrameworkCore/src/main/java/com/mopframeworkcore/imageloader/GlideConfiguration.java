package com.mopframeworkcore.imageloader;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.mopframeworkcore.imageloader.progress.ProgressManager;

import java.io.InputStream;

import okhttp3.OkHttpClient;

@GlideModule
public class GlideConfiguration extends AppGlideModule {
    public GlideConfiguration() {
        super();
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient okHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder()).build();
        //Glide 底层默认使用 HttpConnection 进行网络请求,这里替换为 Okhttp 后才能使用本框架,进行 Glide 的加载进度监听
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
    }
}
