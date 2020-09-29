package com.jsbd.btphone.module.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

public class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";

    protected LayoutInflater inflater;
    private View contentView;
    private Context context;
    private ViewGroup container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
    }

    //子类通过重写onCreateView，调用setOnContentView进行布局设置，否则contentView==null，返回null
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        onCreateView(savedInstanceState);
        if (contentView == null)
            return super.onCreateView(inflater, container, savedInstanceState);
        return contentView;
    }

    protected void onCreateView(Bundle savedInstanceState) {
        //L.d(TAG, "BaseFragment >> onCreateView:" + hashCode());
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //L.d(TAG, "BaseFragment >> onAttach");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        contentView = null;
        container = null;
        inflater = null;
    }

    public Context getApplicationContext() {
        return context;
    }

    public void setContentView(int layoutResID) {
        setContentView((ViewGroup) inflater.inflate(layoutResID, container, false));
    }

    public void setContentView(View view) {
        contentView = view;
    }

    public View getContentView() {
        return contentView;
    }

    public View findViewById(int id) {
        if (contentView != null)
            return contentView.findViewById(id);
        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //L.d(TAG, "BaseFragment >> onDetach");
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean onBackPressed() {
        return false;
    }

    protected boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onDestroy() {
        //L.d(TAG, "BaseFragment >> onDestroy");
        super.onDestroy();
    }

    protected <T extends View> T bindView(@IdRes int id) {
        return (T) findViewById(id);
    }

    protected <T extends View> T bindView(@IdRes int id, View.OnClickListener l) {
        T t = (T) findViewById(id);
        if (t != null) {
            t.setOnClickListener(l);
        }

        return t;
    }
}