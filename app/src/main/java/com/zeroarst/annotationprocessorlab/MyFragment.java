package com.zeroarst.annotationprocessorlab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zeroarst.library.Callback;
import com.zeroarst.library.CallbackFragment;


@CallbackFragment
public class MyFragment extends Fragment implements View.OnClickListener {

    @Callback
    interface FragmentCallback {
        void onClickButton(MyFragment fragment);
    }

    private FragmentCallback mCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vw = inflater.inflate(R.layout.fragm_my, container, false);
        vw.findViewById(R.id.bt).setOnClickListener(this);
        return vw;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt:
                mCallback.onClickButton(this);
                break;
        }
    }

}
