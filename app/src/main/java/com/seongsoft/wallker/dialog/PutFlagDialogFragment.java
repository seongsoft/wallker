package com.seongsoft.wallker.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BeINone on 2016-11-13.
 */

public class PutFlagDialogFragment extends DialogFragment {

    public static final String KEY_JSON = "jsonString";

    private JSONObject mJSONObject;
    private OnOKButtonClickListener mListener;

    public static PutFlagDialogFragment newInstance(JSONObject jsonObject, OnOKButtonClickListener listener) {
        PutFlagDialogFragment fragment = new PutFlagDialogFragment();
        fragment.mJSONObject = jsonObject;
        fragment.mListener = listener;

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        try {
            builder.setMessage(
                    new StringBuilder().append("깃발 ")
                            .append(mJSONObject.getInt("numFlags"))
                            .append("개를 사용하시겠습니까?"))
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onOKButtonClick(mJSONObject);
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return builder.create();
    }

    public interface OnOKButtonClickListener {
        void onOKButtonClick(JSONObject jsonObject);
    }

}
