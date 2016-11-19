package com.seongsoft.wallker.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.seongsoft.wallker.beans.Zone;

/**
 * Created by BeINone on 2016-11-13.
 */

public class PutFlagDialogFragment extends DialogFragment {

    public static final String KEY_ZONE = "zone";

    private OnOKButtonClickListener mListener;

    public static PutFlagDialogFragment newInstance(Zone zone, OnOKButtonClickListener listener) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_ZONE, zone);
        PutFlagDialogFragment fragment = new PutFlagDialogFragment();
        fragment.setArguments(args);
        fragment.mListener = listener;

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
                        mListener.onOKButtonClick(zone);
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

    public interface OnOKButtonClickListener {
        void onOKButtonClick(Zone zone);
    }

}
