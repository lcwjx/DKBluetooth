package com.jsbd.btphone.module.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.jsbd.bluetooth.BTController;
import com.jsbd.btphone.R;
import com.jsbd.btphone.config.MainApp;

/**
 * <h1>懒加载Fragment</h1> 只有创建并显示的时候才会调用onCreateViewLazy方法<br>
 * <br>
 * <p/>
 * 懒加载的原理onCreateView的时候Fragment有可能没有显示出来。<br>
 * 但是调用到setUserVisibleHint(boolean isVisibleToUser),isVisibleToUser =
 * true的时候就说明有显示出来<br>
 * 但是要考虑onCreateView和setUserVisibleHint的先后问题所以才有了下面的代码
 * <p/>
 * 注意：<br>
 * 《1》原先的Fragment的回调方法名字后面要加个Lazy，比如Fragment的onCreateView方法， 就写成onCreateViewLazy <br>
 * 《2》使用该LazyFragment会导致多一层布局深度
 */
public class LazyBaseFragment extends BaseFragment {
    private static final String TAG = "LazyBaseFragment";
    private boolean isInit = false;//真正要显示的View是否已经被初始化（正常加载）
    private Bundle savedInstanceState;
    public static final String INTENT_BOOLEAN_LAZYLOAD = "intent_boolean_lazyLoad";
    private boolean isLazyLoad = true;
    private FrameLayout layout;
    private boolean isStart = false;//是否处于可见状态，in the screen

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        //L.d(TAG, "LazyBaseFragment >> onCreateView");
        super.onCreateView(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            isLazyLoad = bundle.getBoolean(INTENT_BOOLEAN_LAZYLOAD, isLazyLoad);
        }
        //判断是否懒加载
        if (isLazyLoad) {
            //一旦isVisibleToUser==true即可对真正需要的显示内容进行加载
            if (getUserVisibleHint() && !isInit) {
                this.savedInstanceState = savedInstanceState;
                onCreateViewLazy(savedInstanceState);
                isInit = true;
            } else {
                //进行懒加载
                layout = new FrameLayout(getApplicationContext());
                layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.fragment_lazy_loading, null);
                layout.addView(view);
                super.setContentView(layout);
            }
        } else {
            //不需要懒加载，开门江山，调用onCreateViewLazy正常加载显示内容即可
            onCreateViewLazy(savedInstanceState);
            isInit = true;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindBtService();
    }

    private void bindBtService() {
        if (BTController.getInstance().isBindService()) {
            onBtServiceConnected();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //L.d(TAG, "LazyBaseFragment >> setUserVisibleHint called with: " + this + "isVisibleToUser = [" + isVisibleToUser + "]");
        //一旦isVisibleToUser==true即可进行对真正需要的显示内容的加载

        //可见，但还没被初始化
        if (isVisibleToUser && !isInit && getContentView() != null) {
            onCreateViewLazy(savedInstanceState);
            isInit = true;
            onResumeLazy();
        }
        //已经被初始化（正常加载）过了
        if (isInit && getContentView() != null) {
            if (isVisibleToUser) {
                isStart = true;
                onFragmentStartLazy();
            } else {
                isStart = false;
                onFragmentStopLazy();
            }
        }

        onHiddenChanged(!isStart);
    }

    protected boolean isShowing() {
        return isStart;
    }

    @Override
    public void setContentView(int layoutResID) {
        //判断若isLazyLoad==true,移除所有lazy view，加载真正要显示的view
        if (isLazyLoad && getContentView() != null && getContentView().getParent() != null) {
            layout.removeAllViews();
            View view = inflater.inflate(layoutResID, layout, false);
            layout.addView(view);
        }
        //否则，开门见山，直接加载
        else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        //判断若isLazyLoad==true,移除所有lazy view，加载真正要显示的view
        if (isLazyLoad && getContentView() != null && getContentView().getParent() != null) {
            layout.removeAllViews();
            layout.addView(view);
        }
        //否则，开门见山，直接加载
        else {
            super.setContentView(view);
        }
    }

    @Deprecated
    @Override
    public void onStart() {
        //L.d(TAG, "LazyBaseFragment >> onStart >> getUserVisibleHint:" + getUserVisibleHint());
        super.onStart();
        if (isInit && !isStart && getUserVisibleHint()) {
            isStart = true;
            onHiddenChanged(!isStart);
            onFragmentStartLazy();
        }
    }

    @Deprecated
    @Override
    public void onStop() {
        super.onStop();
        //L.d(TAG, "LazyBaseFragment >> onStop >> getUserVisibleHint:" + getUserVisibleHint());
        if (isInit && isStart && getUserVisibleHint()) {
            isStart = false;
            onHiddenChanged(!isStart);
            onFragmentStopLazy();
        }
    }

    //当Fragment被滑到可见的位置时，调用
    protected void onFragmentStartLazy() {
        //L.d(TAG, "LazyBaseFragment >> onFragmentStartLazy");
    }

    //当Fragment被滑到不可见的位置，offScreen时，调用
    protected void onFragmentStopLazy() {
        //L.d(TAG, "LazyBaseFragment >> onFragmentStopLazy");
    }

    protected void onCreateViewLazy(Bundle savedInstanceState) {
        //L.d(TAG, "LazyBaseFragment >> onCreateViewLazy >> savedInstanceState = [" + savedInstanceState + "]");
    }

    protected void onResumeLazy() {
        //L.d(TAG, "LazyBaseFragment >> onResumeLazy");
    }

    protected void onPauseLazy() {
        //L.d(TAG, "LazyBaseFragment >> onPauseLazy");
    }

    protected void onDestroyViewLazy() {
        //跟踪fragment的内存泄漏
        MainApp.getRefWatcher().watch(this);
    }

    protected void onBtServiceConnected() {

    }

    @Override
    @Deprecated
    public void onResume() {
        //L.d(TAG, "LazyBaseFragment >> onResume getUserVisibleHint:" + getUserVisibleHint());
        super.onResume();
        if (isInit) {
            onResumeLazy();
        }
    }

    @Override
    @Deprecated
    public void onPause() {
        //L.d(TAG, "LazyBaseFragment >> onPause >> getUserVisibleHint:" + getUserVisibleHint());
        super.onPause();
        if (isInit) {
            onPauseLazy();
        }
    }

    @Override
    @Deprecated
    public void onDestroyView() {
        //L.d(TAG, "LazyBaseFragment >> onDestroyView >> getUserVisibleHint:" + getUserVisibleHint());
        super.onDestroyView();
        if (isInit) {
            onDestroyViewLazy();
        }
        isInit = false;
    }
}