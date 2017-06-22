# CallbackFragment
Add annotation to make your fragment callbackable without writing boilerplate code.

[![](https://jitpack.io/v/zeroarst/callbackfragment.svg)](https://jitpack.io/#zeroarst/callbackfragment)

Step 1. Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
    repositories {
    ...
    maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency
```gradle
dependencies {
    compile 'com.github.zeroarst:callbackfragment:0.2.1'
}
```

When we want to make Fragment interactive we often cast the host into the interface in `onAttach` like below.
```java
public interface FragmentActionListener {
    void onClickButton(ProductReceiptFragment fragment);    
}

FragmentActionListener mListener;

@Override
public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mListener = (FragmentActionListener) context;
    } catch (ClassCastException e) {
        throw new ClassCastException(context.toString() + " must implement FragmentActionListener");
    }
}

```


With this library all you need to do is to give annotations:
```java
@CallbackFragment
public class MyFragment extends Fragment {

    @Callback
    interface FragmentCallback {
        void onClickButton(MyFragment fragment);
    }
    
    private FragmentCallback mCallback;
    
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


```

