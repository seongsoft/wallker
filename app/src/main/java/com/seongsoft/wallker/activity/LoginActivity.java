package com.seongsoft.wallker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.seongsoft.wallker.R;
import com.seongsoft.wallker.constants.HttpConst;
import com.seongsoft.wallker.constants.PrefConst;
import com.seongsoft.wallker.manager.JSONManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences mLoginPref;
    private SharedPreferences.Editor mPrefEditor;

    private EditText mIDEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mSignUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginPref = getSharedPreferences(PrefConst.USER_PREF, 0);
        mPrefEditor = mLoginPref.edit();

        mIDEditText = (EditText) findViewById(R.id.et_id);
        mPasswordEditText = (EditText) findViewById(R.id.et_password);
        mLoginButton = (Button) findViewById(R.id.btn_login);
        mSignUpLink = (TextView) findViewById(R.id.link_signup);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mIDEditText.getText())) {
                    if (!TextUtils.isEmpty(mPasswordEditText.getText())) {
                        login(mIDEditText.getText().toString(),
                                mPasswordEditText.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "아이디를 입력하세요.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.startActivity(
                        new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void login(String id, String password) {
        new HttpLoginTask().execute(id, password);
    }

    private class HttpLoginTask extends AsyncTask<String, Void, String> {

        private static final String MSG_WRONG = "아이디 또는 비밀번호를 잘못 입력하셨습니다.";
        private static final String MSG_SUCCEED = "";

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("로그인 중입니다. 잠시만 기다려주세요.");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;
            JSONObject memberJObject = null;
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/login/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                JSONObject dataJObject = new JSONObject();
                dataJObject.put("id", params[0]);
                dataJObject.put("password", params[1]);
                writer.write(JSONManager.getPostDataString(dataJObject));
                writer.flush();
                os.close();
                writer.close();

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String memberJString = reader.readLine();
                memberJObject = new JSONObject(memberJString);
                is.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            try {
                if (memberJObject != null) {
                    if (memberJObject.length() == 0) {
                        return MSG_WRONG;
                    } else {
                        mPrefEditor.putString(PrefConst.ID, params[0])
                                .putString(PrefConst.PASSWORD, params[1])
                                .putInt(PrefConst.WEIGHT, memberJObject.getInt("weight"))
                                .putInt(PrefConst.NUM_FLAGS, memberJObject.getInt("numFlags"))
                                .apply();
                        return MSG_SUCCEED;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            mProgressDialog.dismiss();
            if (message == null) {
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
            } else if (message.equals(MSG_SUCCEED)) {
                Toast.makeText(getApplicationContext(),
                        mLoginPref.getString("id", "") + "님, 환영합니다.",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MapActivity.class));
                finish();
            } else if (message.equals(MSG_WRONG)) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }

    }

}
