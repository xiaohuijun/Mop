package com.mopframeworkcore.net.cookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Response;


/**
 * copy by 孟家敏 on 16/12/8 15:51
 * <p>
 * 邮箱：androidformjm@sina.com
 * Created by Tamic on 2016-12-08.
 */
public class ReceivedCookiesInterceptor implements Interceptor {
    private Context context;
    private SharedPreferences sharedPreferences;

    public ReceivedCookiesInterceptor(Context context) {
        super();
        this.context = context;
        sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (chain == null) {
            Log.d("http", "Receivedchain == null");
            return null;
        }
        Response originalResponse = chain.proceed(chain.request());
        Log.d("http", "originalResponse" + originalResponse.toString());
        List<String> strList = originalResponse.headers("set-cookie");
        if (strList != null && !strList.isEmpty()) {
            final StringBuilder cookieBuffer = new StringBuilder();
            for (String s : strList) {
                String[] cookieArray = s.split(";");
                cookieBuffer.append(cookieArray[0]).append(";");
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("cookie", cookieBuffer.toString());
            Log.d("http", "ReceivedCookiesInterceptor" + cookieBuffer.toString());
            editor.apply();
        }

        return originalResponse;
    }
}