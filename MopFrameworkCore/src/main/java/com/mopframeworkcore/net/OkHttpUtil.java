package com.mopframeworkcore.net;

import android.content.Context;
import android.text.TextUtils;

import com.mopframeworkcore.net.callback.ResponseCallback;

import java.util.Map;

import io.reactivex.Observable;

public class OkHttpUtil {
    private static OkHttpUtil instance;
    private Novate novate;

    public static OkHttpUtil getInstance() {
        if (instance == null) {
            synchronized (OkHttpUtil.class) {
                if (instance == null) {
                    instance = new OkHttpUtil();
                }
            }
        }
        return instance;
    }

    public void init(Context context, Map<String, String> headers, Map<String, Object> commonParameters, String baseUrl, boolean isDebug) {
        if (context == null) {
            throw new NullPointerException("context can not be null");
        }
        Novate.Builder builder = new Novate.Builder(context);
        if (!TextUtils.isEmpty(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        if (headers != null && headers.size() > 0) {
            builder.addHeader(headers);
        }
        if (commonParameters != null && commonParameters.size() > 0) {
            builder.addParameters(commonParameters);
        }
        builder.addLog(isDebug);
        novate = builder.build();
    }

    public Novate createANovate(Context context, Map<String, String> headers, Map<String, Object> commonParameters, String baseUrl, boolean isDebug) {
        if (context == null) {
            throw new NullPointerException("context can not be null");
        }
        Novate.Builder builder = new Novate.Builder(context);
        if (!TextUtils.isEmpty(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        if (headers != null && headers.size() > 0) {
            builder.addHeader(headers);
        }
        if (commonParameters != null && commonParameters.size() > 0) {
            builder.addParameters(commonParameters);
        }
        return builder.addLog(isDebug).build();
    }

    private void checkInit() {
        if (novate == null)
            throw new NullPointerException("OkHttpUtil未初始化，请调用init方法初始化");
    }

    public Novate getClient() {
        checkInit();
        return novate;
    }

    public <T> T createApiService(final Class<T> service) {
        checkInit();
        return novate.create(service);
    }

    public <T> void request(Observable<T> observable, ResponseCallback callback) {
        checkInit();
        novate.call(observable, callback);
    }

    public void get(Object tag, String url, Map<String, Object> params, ResponseCallback callback) {
        checkInit();
        novate.rxGet(tag, url, params, callback);
    }


    public void post(Object tag, String url, Map<String, Object> params, ResponseCallback callback) {
        checkInit();
        novate.rxPost(tag, url, params, callback);
    }

    public void downLoad(Object tag, String url, Map<String, Object> params, ResponseCallback callback) {
        checkInit();
        if (params == null || params.size() == 0) {
            novate.rxDownload(tag, url, callback);
            return;
        }
        novate.rxDownload(tag, url, params, callback);
    }

    public void cancelRequestByTag(Object tag) {
        RxApiManager.get().cancel(tag);
    }
}
