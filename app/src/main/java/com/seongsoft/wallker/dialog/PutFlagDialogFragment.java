package com.seongsoft.wallker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.seongsoft.wallker.beans.Zone;
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

/**
 * Created by BeINone on 2016-11-13.
 */

public class PutFlagDialogFragment extends DialogFragment {

    public static final String KEY_ZONE = "zone";

    private Context mContext;

    public static PutFlagDialogFragment newInstance(Context context, Zone zone) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_ZONE, zone);
        PutFlagDialogFragment fragment = new PutFlagDialogFragment();
        fragment.setArguments(args);
        fragment.mContext = context;

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Zone zone = getArguments().getParcelable(KEY_ZONE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setMessage(
                new StringBuilder().append("깃발 ")
                        .append(zone.getNumFlags())
                        .append("개를 사용하시겠습니까?"))
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new HttpPutFlagTask().execute(zone);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    private class HttpPutFlagTask extends AsyncTask<Zone, Void, String> {

        private static final String MSG_SUCCEED = "깃발을 꽂았습니다.";
        private static final String MSG_FAILED = "한 발 늦었습니다.";
        private static final String MSG_ERROR = "깃발을 꽂지 못했습니다.";

        private int usedNumFlags;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Zone... params) {
            HttpURLConnection conn = null;
            String result = null;
            usedNumFlags = params[0].getNumFlags();
            try {
                conn = (HttpURLConnection) new URL(HttpConst.SERVER_URL + "/putflag/index.jsp")
                        .openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                JSONObject dataJObject = new JSONObject();
                dataJObject.put("latitude", params[0].getLatitude());
                dataJObject.put("longitude", params[0].getLongitude());
                dataJObject.put("numFlags", params[0].getNumFlags());
                dataJObject.put("userid", params[0].getUserid());
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
            if (message == null) {
                Toast.makeText(mContext, MSG_ERROR, Toast.LENGTH_SHORT).show();
            } else {
                if (message.equals(MSG_SUCCEED)) {
                    SharedPreferences userPref =
                            mContext.getSharedPreferences(PrefConst.USER_PREF, 0);
                    userPref.edit()
                            .putInt(PrefConst.NUM_FLAGS,
                                    userPref.getInt(PrefConst.NUM_FLAGS, 0) - usedNumFlags)
                            .apply();
                }
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
            dismiss();
        }

    }

}
