package com.example.codea;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Backend db;
    private static String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("mainact", "backend init");
        Backend.init(this);  // ✅ Global initialization
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text);

        Button yourButton = (Button) findViewById(R.id.goToButtonAct);

        EditText myEditText = findViewById(R.id.editTextText);
        EditText myEditTextPassword = findViewById(R.id.editTextTextPassword);



        yourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("mainact", "start");
                    JSONObject json = new JSONObject();
                    json.put("id", myEditText.getText().toString());
                    json.put("password", myEditTextPassword.getText().toString());
                    Backend.request("/auth/signin", "POST", json,
                            new Backend.ApiCallback() {
                                @Override
                                public void onSuccess(String response) throws JSONException {
                                    Log.d("mainact", "success"+response);
                                    JSONObject obj = new JSONObject(response);
                                    String token = obj.getString("accessToken");
                                    id = obj.getString("id");

                                    // Save it to SharedPreferences
                                    Backend.saveToken(MainActivity.this, token);
                                    // Parse token and save it:
                                    // BackendService.saveToken(context, token);
                                    JSONObject json2 = new JSONObject();
                                    json2.put("app", "addwdw");
                                    json2.put("collectionName", "a");
                                    json2.put("collectionKey", "a");
                                    json2.put("key", "a");
                                    json2.put("value", "a");
                                    json2.put("getAccess", id);
                                    json2.put("setAccess", id);
                                    json2.put("removeAccess", id);

                                    Backend.request("/setItem", "post", json2,
                                            new Backend.ApiCallback() {
                                                @Override
                                                public void onSuccess(String response) throws JSONException {
                                                    Log.d("mainact", "success"+response);
                                                    JSONObject obj2 = new JSONObject(response);
                                                    Log.d("mainact", obj2.toString());
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    Log.d("mainact", error);
                                                }
                                            }
                                    );
                                    Backend.request("/getItem", "post", json2,
                                            new Backend.ApiCallback() {
                                                @Override
                                                public void onSuccess(String response) throws JSONException {
                                                    Log.d("mainact", "success"+response);
                                                    JSONObject obj2 = new JSONObject(response);
                                                    Log.d("mainact", obj2.toString());
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    Log.d("mainact", "error");
                                                }
                                            }
                                    );
                                    JSONObject json3 = new JSONObject();
                                    json3.put("msg", "hi");
                                    json3.put("context", "you are chatgpt");
                                    Backend.request("/process/generator/aiTXTGenerator", "post", json3,
                                            new Backend.ApiCallback() {
                                                @Override
                                                public void onSuccess(String response) throws JSONException {
                                                    Log.d("mainact", "success"+response);
                                                    JSONObject obj2 = new JSONObject(response);
                                                    Log.d("mainact", obj2.toString());
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    Log.d("mainact", "error");
                                                }
                                            }
                                    );
                                }

                                @Override
                                public void onError(String error) {
                                    Log.d("mainact", "error"+error);
                                }
                            });
                } catch (Exception e) {
                    Log.d("mainact", e.toString());
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}