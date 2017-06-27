package com.zeroarst.annotationprocessorlab;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zeroarst.library.Callback;
import com.zeroarst.library.CallbackFragment;


@CallbackFragment
public class MyDialogFragment extends DialogFragment {

    @Callback
    interface DialogListener {
        void onTextClicked(MyDialogFragment fragment);
    }

    private DialogListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vw = inflater.inflate(R.layout.fragm_my_dialog, container, false);
        vw.findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mListener.onTextClicked(MyDialogFragment.this);
            }
        });
        return vw;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
