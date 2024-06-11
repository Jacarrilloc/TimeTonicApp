package com.jacarrilloc.timetonicapp.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jacarrilloc.timetonicapp.util.ConfigUtil;

import java.io.IOException;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthRepositoryImpl implements AuthRepository {
    private String loginUrl;
    private String req;
    private String version;
    private String appName;

    public AuthRepositoryImpl(Context context) {
        Properties properties = ConfigUtil.loadProperties(context);
        loginUrl = properties.getProperty("api_url");
        req = properties.getProperty("req");
        version = properties.getProperty("version");
        appName = properties.getProperty("appname");

        // Agregar mensajes de depuración para verificar las propiedades cargadas
        if (loginUrl == null || req == null || version == null || appName == null) {
            throw new IllegalStateException("One or more properties are missing in config.properties");
        }
    }

    @Override
    public void login(String email, String password, LoginCallback callback) {
        if (email == null || password == null) {
            callback.onFailure("Email or password is null");
            return;
        }

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("req", req)
                .addFormDataPart("version", version)
                .addFormDataPart("appname", appName)
                .addFormDataPart("email", email)
                .addFormDataPart("password", password)
                .build();

        Request request = new Request.Builder()
                .url(loginUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Login Failed: " + response.message());
                }
            }
        });
    }
}
