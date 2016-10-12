package com.seongsoft.wallker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by dsm_025 on 2016-10-04.
 */

public class AddNameDialogFragment extends DialogFragment {
    private EditText mName;
    private NameInputListener listener;

    public static AddNameDialogFragment newInstance(NameInputListener listener) {
        AddNameDialogFragment fragment = new AddNameDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    public interface NameInputListener
    {
        void onNameInputComplete(String name);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_name, null);
        mName = (EditText)view.findViewById(R.id.id_txt_password);
        builder.setView(view)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                listener.onNameInputComplete(mName
                                .getText().toString());
                            }
                        }).setNegativeButton("취소", null);
        return builder.create();
    }
}