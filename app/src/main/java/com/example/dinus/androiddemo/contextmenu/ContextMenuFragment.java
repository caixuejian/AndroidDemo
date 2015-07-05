package com.example.dinus.androiddemo.contextmenu;


import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.dinus.androiddemo.R;

import java.util.ArrayList;

public class ContextMenuFragment extends DialogFragment implements MenuAdapter.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    public static final String TAG = ContextMenuFragment.class.getName() + "TAG";
    private static final String ARG_MENU_PARAMS = "ARG_MENU_PARAMS";

    private ArrayList<Integer> mMenuParams;

    private RelativeLayout mWrapperButtons;
    private MenuAdapter mDropDownMenuAdapter;
    private ImageView itemView;
    private View expandItem;
    private int expandItemIconId;

    private MenuAdapter.OnMenuItemClickListener mOnMenuItemClickListener;

    public static ContextMenuFragment newInstance(ArrayList<Integer> mMenuParams) {
        ContextMenuFragment fragment = new ContextMenuFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_MENU_PARAMS, mMenuParams);
        fragment.setArguments(args);
        return fragment;
    }

    public ContextMenuFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MenuAdapter.OnMenuItemClickListener) {
            mOnMenuItemClickListener = (MenuAdapter.OnMenuItemClickListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.MenuFragmentStyle);
        if (getArguments() != null) {
            mMenuParams = getArguments().getIntegerArrayList(ARG_MENU_PARAMS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_context_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWrapperButtons = (RelativeLayout) view.findViewById(R.id.wrapper_buttons);
        mDropDownMenuAdapter = new MenuAdapter(getActivity(), mMenuParams, mWrapperButtons);
        itemView = (ImageView) view.findViewById(R.id.menu_item);
        mDropDownMenuAdapter.setOnItemClickListener(this);

        layoutExapndMenuItem();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDropDownMenuAdapter.menuToggle();
            }
        }, 0);
    }

    private void layoutExapndMenuItem() {
        if (expandItem == null){
            return ;
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) itemView.getLayoutParams();
        layoutParams.width = expandItem.getWidth();
        layoutParams.height = expandItem.getHeight();
        itemView.setPadding(expandItem.getPaddingLeft(), expandItem.getPaddingTop(),
                expandItem.getPaddingRight(), expandItem.getPaddingBottom());
        itemView.setLayoutParams(layoutParams);
        itemView.setImageResource(expandItemIconId);
    }

    @Override
    public void onItemClick(View clickView) {
        if (mOnMenuItemClickListener != null) {
            if (mWrapperButtons.indexOfChild(clickView) != -1) {
                mOnMenuItemClickListener.onMenuItemClick(clickView, mWrapperButtons.indexOfChild(clickView));
            }
        }
        close();
    }

    private void close() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        });
    }

    public void setExpandItem(View expandItem) {
        this.expandItem = expandItem;
    }

    public void setExpandItemIconId(int expandItemIconId){
        this.expandItemIconId = expandItemIconId;
    }
}
