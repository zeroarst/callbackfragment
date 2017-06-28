# CallbackFragment
Add annotation to cast your fragment host into you defined types without writing boilerplate.

[![](https://jitpack.io/v/zeroarst/callbackfragment.svg)](https://jitpack.io/#zeroarst/callbackfragment)

Step1. Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Step2. Add the dependency
```gradle
dependencies {
    provided 'com.github.zeroarst.callbackfragment:library:0.3'
    annotationProcessor 'com.github.zeroarst.callbackfragment:compiler:0.3'
}
```
# Usage

When we want to make Fragment interactive we often cast the host into types in `onAttach` like below.
```java
public interface FragmentInteractionListener {
    void onClickButton(MyFragment fragment);    
}

FragmentInteractionListener mListener;

@Override
public void onAttach(Context context) {
    super.onAttach(context);
    try {
      mListener = (FragmentInteractionListener) context;
    } catch (ClassCastException e) {
        throw new ClassCastException(context.toString() + " must implement FragmentInteractionListener");
    }
}

```

With this library all you need to do is to give annotations. It does all the cast for you. Add `@CallbackFragment` to your Fragment and add `@Callback` to your type.
```java
@CallbackFragment
public class MyFragment extends Fragment {

    @Callback
    interface FragmentCallback {
        void onClickButton(MyFragment fragment);
    }    
    private FragmentCallback mCallback;
    
    @Callback(mandatory = false)
    interface FragmentCallbackNotForce {
        void onClickButton(MyFragment fragment);
    }
    private FragmentCallbackNotForce mCallbackNotForce;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View vw = inflater.inflate(R.layout.fragm_my, container, false);
        vw.findViewById(R.id.bt1).setOnClickListener(this);
        vw.findViewById(R.id.bt2).setOnClickListener(this);
        return vw;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt1
                mCallback.onClickButton(this);
                break;
            case R.id.bt2
                // Because we give mandatory = false so this might be null if not implemented by the host.
                if (mCallbackNotForce != null)
                    mCallbackNotForce.onClickButton(this);
                break;
        }
    }

}
```

The library generates a subclass of the annotated `Fragment` with this format: `YourFragmentCallbackable`. In your host class, such as Activity, or another Fragment, create an instance and add to `FragmentManager`. It also comes with a static `.create()` method that you can just use.

```java
public class MainActivity extends AppCompatActivity implements MyFragment.FragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
            .add(R.id.lo_fragm_container, MyFragmentCallbackable.create(), "MY_FRAGM")
            .commit();
    }

    Toast mToast;

    @Override
    public void onClickButton(MyFragment fragment) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, "Callback from " + fragment.getTag(), Toast.LENGTH_SHORT);
        mToast.show();
    }
}
```

One thing to note is that when casting types the library checks the host in following order:

`getTargetFragment()` > `getParentFragment()` > `getContext()`
