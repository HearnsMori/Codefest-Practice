package com.example.codea;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class DBStorage {

    private static final String BASE_URL = "http://10.0.2.2:10000";
    // Use 10.0.2.2 instead of localhost for emulator

    private static final String PREF_NAME = "DBStoragePrefs";
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private SharedPreferences prefs;

    private String accessToken;
    private String refreshToken;

    // ============================
    // CONSTRUCTOR
    // ============================

    public DBStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        loadTokens();

        client = new OkHttpClient();
    }

    // ============================
    // TOKEN STORAGE
    // ============================

    private void storeTokens(String access, String refresh) {
        this.accessToken = access;
        this.refreshToken = refresh;

        prefs.edit()
                .putString("accessToken", access)
                .putString("refreshToken", refresh)
                .apply();
    }

    private void loadTokens() {
        accessToken = prefs.getString("accessToken", null);
        refreshToken = prefs.getString("refreshToken", null);
    }

    private void clearTokens() {
        prefs.edit().clear().apply();
        accessToken = null;
        refreshToken = null;
    }

    // ============================
    // AUTH REFRESH (SYNCHRONOUS)
    // ============================

    private synchronized boolean refreshAuth() {

        if (refreshToken == null) return false;

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("token", refreshToken);

            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/refreshToken")
                    .post(RequestBody.create(bodyJson.toString(), JSON))
                    .header("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                clearTokens();
                return false;
            }

            String resBody = response.body().string();
            JSONObject data = new JSONObject(resBody);

            storeTokens(
                    data.getString("accessToken"),
                    data.getString("refreshToken")
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================
    // CORE AUTH FETCH
    // ============================

    private void authFetch(
            String path,
            String method,
            JSONObject bodyJson,
            Callback callback
    ) {

        loadTokens();

        RequestBody body = null;
        if (bodyJson != null) {
            body = RequestBody.create(bodyJson.toString(), JSON);
        }

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + path)
                .header("Content-Type", "application/json");

        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }

        if ("POST".equals(method)) builder.post(body);
        else if ("PUT".equals(method)) builder.put(body);
        else if ("GET".equals(method)) builder.get();

        Request request = builder.build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.code() == 401) {

                    response.close();

                    boolean refreshed = refreshAuth();

                    if (refreshed) {
                        // Retry original request with new token
                        authFetch(path, method, bodyJson, callback);
                        return;
                    }
                }

                String resBody = response.body() != null
                        ? response.body().string()
                        : "";

                try {
                    JSONObject data = new JSONObject(resBody);

                    // Store tokens if returned
                    if (data.has("accessToken") && data.has("refreshToken")) {
                        storeTokens(
                                data.getString("accessToken"),
                                data.getString("refreshToken")
                        );
                    }

                } catch (Exception ignored) {}

                callback.onResponse(call, response);
            }
        });
    }

    // ============================
    // AUTH METHODS
    // ============================

    public void signup(
            String id,
            String password,
            JSONArray role,
            JSONArray contact,
            Callback callback
    ) throws JSONException {

        JSONObject body = new JSONObject();
        body.put("id", id);
        body.put("password", password);
        body.put("role", role);
        body.put("contact", contact);

        authFetch("/auth/signup", "POST", body, callback);
    }

    public void signin(
            String id,
            String password,
            Callback callback
    ) throws JSONException {

        JSONObject body = new JSONObject();
        body.put("id", id);
        body.put("password", password);

        authFetch("/auth/signin", "POST", body, callback);
    }

    // ============================
    // DATA STORAGE
    // ============================

    public void setItem(JSONObject body, Callback callback) {
        authFetch("/setItem", "POST", body, callback);
    }

    public void getItem(JSONObject body, Callback callback) {
        authFetch("/getItem", "POST", body, callback);
    }

    public void removeItem(JSONObject body, Callback callback) {
        authFetch("/removeItem", "POST", body, callback);
    }

    // ============================
    // JSON STORAGE
    // ============================

    public void getJSONItem(JSONObject body, Callback callback) {
        authFetch("/getJSONItem", "POST", body, callback);
    }

    public void setJSONItem(JSONObject body, Callback callback) {
        authFetch("/setJSONItem", "POST", body, callback);
    }

    public void pushJSONItem(JSONObject body, Callback callback) {
        authFetch("/pushJSONItem", "POST", body, callback);
    }

    public void popJSONItem(JSONObject body, Callback callback) {
        authFetch("/popJSONItem", "POST", body, callback);
    }

    public void removeJSONItem(JSONObject body, Callback callback) {
        authFetch("/removeJSONItem", "POST", body, callback);
    }

    // ============================
    // USER MANAGEMENT
    // ============================

    public void getSelfId(Callback callback) {
        authFetch("/user/getSelfId", "GET", null, callback);
    }

    public void setSelfId(String id, Callback callback) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("id", id);
        authFetch("/user/setSelfId", "PUT", body, callback);
    }

    public void setSelfPassword(String password, Callback callback) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("password", password);
        authFetch("/user/setSelfPassword", "PUT", body, callback);
    }

    // ============================
    // AI
    // ============================

    public void aiTXTGenerator(String msg, String context, Callback callback) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("msg", msg);
        body.put("context", context);
        authFetch("/process/generator/aiTXTGenerator", "POST", body, callback);
    }

    public void aiJSONGenerator(JSONObject body, Callback callback) {
        authFetch("/process/generator/aiJSONGenerator", "POST", body, callback);
    }
}