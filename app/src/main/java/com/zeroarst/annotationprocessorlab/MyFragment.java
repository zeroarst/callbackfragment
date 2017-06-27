package com.zeroarst.annotationprocessorlab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zeroarst.library.Callback;
import com.zeroarst.library.CallbackFragment;


@CallbackFragment
public class MyFragment extends Fragment implements View.OnClickListener {

    @Callback
    interface FragmentCallback {
        void onClickButton(MyFragment fragment);
    }

    private FragmentCallback mCallback;

    private TextView mFragmNameTv;
    private TextView mMsgTv;
    private Button mBt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vw = inflater.inflate(R.layout.fragm_my, container, false);

        mFragmNameTv = (TextView) vw.findViewById(R.id.tv_fragm_name);
        mFragmNameTv.setText(getTag());

        mMsgTv = (TextView) vw.findViewById(R.id.tv_msg);

        mBt = (Button) vw.findViewById(R.id.bt);
        mBt.setOnClickListener(this);

        return vw;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updateText(CharSequence text) {
        if (mMsgTv != null)
            mMsgTv.setText(text);
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
