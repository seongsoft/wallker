package com.seongsoft.wallker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by BeINone on 2016-10-04.
 */

public class GetTreasureDialogFragment extends DialogFragment {

    GetTreasureDialogListener mListener;

    public static GetTreasureDialogFragment newInstance(GetTreasureDialogListener listener) {
        GetTreasureDialogFragment fragment = new GetTreasureDialogFragment();
        fragment.mListener = listener;

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        return new AlertDialog.Builder(getContext())
                .setPositiveButton("획득", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onGet();
                    }
                })
                .create();
    }

    public interface GetTreasureDialogListener {
        void onGet();
    }

}
