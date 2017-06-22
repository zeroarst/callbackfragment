package com.zeroarst.annotationprocessorlab;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MyFragment.FragmentCallback, MyDialogFragment.DialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
            .add(R.id.lo_fragm_container, MyFragmentCallbackable.create(), "MY_FRAGM")
            .commit();

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialogFragmentCallbackable.create().show(getSupportFragmentManager(), "MY_DIALOG_FRAGM");
            }
        });
    }

    Toast mToast;

    @Override
    public void onClickButton(MyFragment fragment) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, "Callback from " + fragment.getTag(), Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onDismissed(MyDialogFragment fragment) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, fragment.getTag() + " dismissed", Toast.LENGTH_SHORT);
        mToast.show();
    }
}
