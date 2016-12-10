package com.seongsoft.wallker.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignupActivity extends AppCompatActivity {

    private SharedPreferences mLoginPref;

    private EditText mIDEditText;
    private EditText mPasswordEditText;
    private EditText mWeightEditText;
    private Button mSignupButton;
    private TextView mLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mLoginPref = getSharedPreferences(PrefConst.USER_PREF, 0);

        mIDEditText = (EditText) findViewById(R.id.et_id);
        mPasswordEditText = (EditText) findViewById(R.id.et_password);
        mWeightEditText = (EditText) findViewById(R.id.et_weight);
        mSignupButton = (Button) findViewById(R.id.btn_signup);
        mLoginLink = (TextView) findViewById(R.id.link_login);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable id = mIDEditText.getText();
                Editable password = mPasswordEditText.getText();
                Editable weight = mWeightEditText.getText();
                if (!TextUtils.isEmpty(id)) {
                    if (!TextUtils.isEmpty(password)) {
                        if (!TextUtils.isEmpty(weight)) {
                            signup(id.toString(), password.toString(), weight.toString());
                        } else {
                            Toast.makeText(getApplicationContext(), "몸무게를 입력하세요.",
                                    Toast.LENGTH_SHORT).show();
                        }
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
    }

    private void signup(String id, String password, String weight) {
        new HttpSignupTask().execute(id, password, weight);
    }

    private class HttpSignupTask extends AsyncTask<String, Void, String> {

        private static final String MSG_SUCCEED = "계정이 생성되었습니다.";
        private static final String MSG_FAILED = "아이디가 이미 존재합니다.";
        private static final String MSG_ERROR = "계정을 생성하지 못했습니다.";

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(SignupActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("계정 생성 중입니다. 잠시만 기다려주세요.");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;
            String result = null;
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/signup/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setReadTimeout(100000);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                JSONObject dataJObject = new JSONObject();
                dataJObject.put("id", params[0]);
                dataJObject.put("password", params[1]);
                dataJObject.put("weight", Integer.parseInt(params[2]));
                writer.write(JSONManager.getPostDataString(dataJObject));
                writer.flush();
                os.close();
                writer.close();

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                result = reader.readLine();
                is.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }

            if (result != null) {
                switch (result) {
                    case "succeed":
                        return MSG_SUCCEED;
                    case "failed":
                        return MSG_FAILED;
                    case "error":
                        return MSG_ERROR;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            mProgressDialog.dismiss();
            if (message == null) {
                Toast.makeText(SignupActivity.this, MSG_ERROR, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                if (message.equals(MSG_SUCCEED)) finish();
            }
        }

    }

}
