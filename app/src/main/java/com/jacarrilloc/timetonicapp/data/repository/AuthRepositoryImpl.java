package com.jacarrilloc.timetonicapp.data.repository;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.jacarrilloc.timetonicapp.util.ConfigUtil;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    private String version;
    private String appName;
    private String email;
    private String password;
    private Context context;

    public AuthRepositoryImpl(Context context) {
        this.context = context;
        Properties properties = ConfigUtil.loadProperties(context);
        loginUrl = properties.getProperty("api_url");
        version = properties.getProperty("version");
        appName = properties.getProperty("appname");

        if (loginUrl == null || version == null || appName == null) {
            throw new IllegalStateException("One or more properties are missing in config.properties");
        }
    }

    @Override
    public void login(String email, String password, LoginCallback callback) {
        if (email == null || password == null) {
            callback.onFailure("Email or password is null");
            return;
        }

        this.email = email;
        this.password = password;

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("req", "createAppkey")
                .addFormDataPart("version", this.version)
                .addFormDataPart("appname", this.appName)
                .addFormDataPart("email",  this.email)
                .addFormDataPart("password", this.password)
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
                    String result = response.body().string();
                    try {
                        createOauthkey(result, callback);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    callback.onFailure("Login Failed: " + response.message());
                }
            }
        });
    }

    private void createOauthkey(String jsonResult, LoginCallback callback) throws JSONException, IOException {
        Map<String, String> dataResponse = getJsonMap(jsonResult);

        OkHttpClient clientOauthkey = new OkHttpClient()
                .newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("req", "createOauthkey")
                .addFormDataPart("version", this.version)
                .addFormDataPart("appkey", dataResponse.get("appkey"))
                .addFormDataPart("login", this.email)
                .addFormDataPart("pwd", this.password)
                .build();

        Request oAuthRequest = new Request.Builder().url(this.loginUrl)
                .method("POST", body)
                .build();
        Response response = clientOauthkey.newCall(oAuthRequest).execute();

        String responseBody = response.body().string();

        createSesskey(responseBody);

        callback.onSuccess(responseBody);
    }

    private void createSesskey(String jsonData) throws JSONException, IOException {
        Map<String, String> jsonMapResultOauth = getJsonMap(jsonData);

        OkHttpClient clientSesskey = new OkHttpClient()
                .newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("req", "createSesskey")
                .addFormDataPart("version", this.version)
                .addFormDataPart("oauthkey", jsonMapResultOauth.get("oauthkey"))
                .addFormDataPart("o_u", jsonMapResultOauth.get("o_u"))
                .addFormDataPart("u_c", jsonMapResultOauth.get("o_u"))
                .addFormDataPart("restrictions", "")
                .build();

        Request sesskeyRequest = new Request.Builder().url(this.loginUrl)
                .method("POST", body)
                .build();
        Response response = clientSesskey.newCall(sesskeyRequest).execute();

        String resultFinal = response.body().string();

        Map<String, String> jsonMapResultSesskey = getJsonMap(resultFinal);

        saveAuthToken( jsonMapResultSesskey.get("sesskey")); //saveToken

        Log.i("FINAL AUTH", jsonMapResultSesskey.get("sesskey"));

    }

    private Map<String, String> getJsonMap(String json) throws JSONException {
        Map<String, String> jsonMap = new HashMap<>();
        JSONObject jsonObject = new JSONObject(json);
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonObject.getString(key);
            jsonMap.put(key, value);
        }
        return jsonMap;
    }

    private void saveAuthToken(String token) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("token", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getAuthToken () {
        SharedPreferences sharedPreferences = context.getSharedPreferences("token", MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }
}
