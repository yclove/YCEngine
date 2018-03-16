/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ycengine.tourist;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ycengine.yclibrary.util.LogUtil;
import com.ycengine.yclibrary.util.thread.RunnableThread;
import com.ycengine.yclibrary.widget.ProgressDialog;

import java.util.HashMap;

/**
 * Generic(포괄적인) Type
 * E - Element
 * K - Key
 * N - Number
 * T - Type
 * V - Value
 * S, U, V etc. - 2nd, 3rd, 4th types
 */
public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment {
    public T mBinding;
    public View mRootView;

    public ProgressDialog mProgressDialog = new ProgressDialog();
    public HashMap<Integer, RunnableThread> mThreads = null;

    /**
     * Required empty public constructor. Subclasses do not
     * need to implement their own.
     */
    public BaseFragment() {
    }

    /**
     * @return the layout resource for this Fragment
     */
    @LayoutRes
    protected abstract int contentLayout();

    /**
     * 1)
     * Fragment가 Activity에 추가될 때 한번 호출됩니다. 아직 Activity와 Fragment의 초기화가 끝나지 않은 시점입니다.
     * Fragment에서 Activity에 대한 참조를 얻기 위해 사용되어 집니다.
     * API 23 이상에서는 onAttach(Context context)와 onAttach(Activity activity) 둘다 호출되지만 그외 버전에서는 onAttach(Activity activity)만 호출됩니다.
     */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        LogUtil.e("onAttach(Context context)");
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
            //this.listener = (CreateLayerListener) a;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        LogUtil.e("onAttach(Activity activity)");
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            //this.listener = (CreateLayerListener) activity;
        }
    }

    /**
     * 2)
     * Fragment가 생성된 시점에 호출됩니다.
     * Activity의 onCreate메소드가 아직 완료된 시점이 아니라서 유저 인터페이스와 관련있는 것을 제외한 Fragment에서 사용되는 리소스들이 초기화됩니다.
     * 유저 인터페이스와 관련된 처리는 onActivityCreated 메소드에서 해주어야 합니다.
     * Fragment가 paused 또는 stop되었다가 다시 resume되었을 때 유지하고 싶은 Fragment의 컴포넌트들를 여기서 초기화 해주어야 합니다.
     * setRetainInstance(ture)를 호출하여 fragment의 인스턴스를 유지하도록 할 수 있습니다. 이 때 다음 세가지가 기존과 달라집니다.
     * - Activity가 재생성되어도 Fragment가 유지되기 때문에 onCreate는 호출되지 않습니다.
     * - onDestroy()는 호출되지 않지만, Activity로부터 fragment가 detach될때 onDetach()는 호출됩니다.
     * - onAttach(Activity)와 onActivityCreated(Bundle)는 호출됩니다.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.e("onCreate()");
        super.onCreate(savedInstanceState);
    }

    /**
     * 3)
     * Fragment의 유저 인터페이스가 화면에 그려지는 시점에 호출됩니다.
     * XML 레이아웃을 inflate하여 Fragment를 위한 View를 생성하고 Fragment 레이아웃의 root에 해당되는 View를 Activity에게 리턴해야 합니다.
     * inflate란 XML 레이아웃에 정의된 뷰나 레이아웃을 읽어서 메모리상의 view 객체를 생성해주는 겁니다. 여기서 view를 리턴했다면, view가 release될때 onDestroyView()가 호출됩니다.
     * 유저 인터페이스 없는 Fragment의 경우에는 null을 리턴하면 됩니다.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.e("onCreateView()");
        mBinding = DataBindingUtil.inflate(inflater, contentLayout(), container, false);
        mRootView = mBinding.getRoot();
        return mRootView;
    }

    /**
     * 4)
     * Activity의 onCreate()를 완료되고 fragment의 View 생성이 완료했을때 호출됩니다.
     * Activity와 Fragment의 View가 모두 생성된 시점이라 findViewById()를 사용하여 View 객체에 접근하는게 가능합니다.
     * - inflate된 레이아웃 내에서 R.java에 할당되어있는 주어진 ID를 가지고 view를 찾습니다.
     * 또한 Context객체를 요구하는 객체를 초기화 할 수 있습니다.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogUtil.e("onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 5)
     * view의 저장되었던 상태가 복구되었음을 fragment에게 알려줍니다. 저장되었던 상태를 바탕으로 초기화시 사용됩니다.
     * 예를 들어 fragment의 유저 인터페이스에 있는 체크박스가 선택 상태를 복구하는데 사용할 수 있습니다.
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        LogUtil.e("onViewStateRestored()");
        super.onViewStateRestored(savedInstanceState);
    }

    /**
     * 6)
     * Fragment가 사용자에게 보여질때 호출되며 아직 사용자와 상호작용은 불가능한 상태입니다. (fragment가 속한 activity가 start된거랑 관련있습니다.)
     */
    @Override
    public void onStart() {
        LogUtil.e("onStart()");
        super.onStart();
    }

    /**
     * 7)
     * fragment가 사용자에게 보여지고 실행 중일때 호출되며 사용자와 상호작용할 수 있는 상태입니다. (fragment가 속한 activity가 resume된거랑 관련있습니다.)
     * 보통 onResume에서 자원을 할당하고 onPause에서 자원을 해제해줍니다.
     */
    @Override
    public void onResume() {
        LogUtil.e("onResume()");
        super.onResume();
    }

    /**
     * 8) fragment가 더이상 사용되지 않을때 호출됩니다.
     * Activity가 pause되어 fragment가 더이상 사용자와 상호작용하지 않을 때이다.
     */
    @Override
    public void onPause() {
        LogUtil.e("onPause()");
        super.onPause();
    }

    /**
     * 9) fragment가 더이상 사용되지 않을때 호출됩니다.
     * Activity가 stop되었거나 fragment의 operation이 activity에 의해 수정되었을 경우로 fragment가 더이상 사용자에게 보여지지 않을 때입니다.
     */
    @Override
    public void onStop() {
        LogUtil.e("onStop()");
        super.onStop();
    }

    /**
     * 10) fragment가 destroy될때 다음 순서대로 호출됩니다.
     * fragment가 화면에서 안보이고 view의 현재상태가 저장된후 호출됩니다. 이 메소드가 호출된 후에 fragment의 모든 View가 destroy됩니다. onCreateView에서 초기화했던 UI들을 여기서 해제하면 됩니다.
     */
    @Override
    public void onDestroyView() {
        LogUtil.e("onDestroyView()");
        super.onDestroyView();
    }

    /**
     * 11)
     * fragment를 더 이상 사용하지 않을때 호출되며 activity와 연결이 끊어진 상태는 아니지만 fragment는 동작을 하지 않는 상태입니다.
     * 시스템에서 onDestroy가 항상 호출되는 것을 보장해주지 않습니다.
     */
    @Override
    public void onDestroy() {
        LogUtil.e("onDestroy()");
        super.onDestroy();
    }

    /**
     * 12)
     * fragment가 activity와의 연결이 끊어지기 전에 호출되며 fragment의 view hierarchy가 더 이상 존재하지 않게 됩니다.
     * 부모 activity가 full 라이프사이클을 완료하지 않고 종료되었다면 onDetach()는 호출되지 않습니다.
     */
    @Override
    public void onDetach() {
        LogUtil.e("onDetach()");
        super.onDetach();
    }

    /**
     * Callback invoked when this Fragment is part of a ViewPager and it has been
     * selected, as indicated by {@link android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
     * onPageSelected(int)}.
     */
    public void onPageSelected() {
        // TODO: Consider making this abstract. The reason it wasn't abstract in the first place
        // is not all Fragments in our ViewPager need to do things upon being selected. As such,
        // those Fragments' classes would just end up stubbing this implementation.
    }

}
