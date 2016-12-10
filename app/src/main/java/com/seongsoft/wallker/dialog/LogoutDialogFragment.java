package com.seongsoft.wallker.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.seongsoft.wallker.activity.LoginActivity;
import com.seongsoft.wallker.constants.PrefConst;

/**
 * Created by BeINone on 2016-11-17.
 */

public class LogoutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getSharedPreferences(PrefConst.USER_PREF, 0).edit()
                                .remove(PrefConst.ID)
                                .remove(PrefConst.PASSWORD)
                                .remove(PrefConst.WEIGHT)
                                .remove(PrefConst.NUM_FLAGS)
                                .apply();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

}
