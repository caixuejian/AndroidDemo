package com.example.dinus.androiddemo.contextmenu;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
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
    private ImageView innerMoreItem;
    private View outerMoreItem;
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
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        //intercept back event
        setCancelable(false);
        innerMoreItem = (ImageView) view.findViewById(R.id.more_item);
        mWrapperButtons = (RelativeLayout) view.findViewById(R.id.wrapper_buttons);
        mDropDownMenuAdapter = new MenuAdapter(getActivity(), mMenuParams, mWrapperButtons, innerMoreItem);
        mDropDownMenuAdapter.setOnItemClickListener(this);
        mDropDownMenuAdapter.menuToggle();

        layoutExapndMenuItem();
        resetBackEvent();

        outerMoreItem.setVisibility(View.GONE);

    }

    private void layoutExapndMenuItem() {
        if (outerMoreItem == null) {
            return;
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) innerMoreItem.getLayoutParams();
        layoutParams.width = outerMoreItem.getWidth();
        layoutParams.height = outerMoreItem.getHeight();
        innerMoreItem.setPadding(outerMoreItem.getPaddingLeft(), outerMoreItem.getPaddingTop(),
                outerMoreItem.getPaddingRight(), outerMoreItem.getPaddingBottom());
        innerMoreItem.setLayoutParams(layoutParams);
        innerMoreItem.setImageResource(expandItemIconId);
    }


    private void resetBackEvent(){
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode,
                                 android.view.KeyEvent event) {

                if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                    if (event.getAction() != KeyEvent.ACTION_UP) {
                        return true;
                    } else {
                        if (mDropDownMenuAdapter.menuToggle()) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dismiss();
                                }
                            }, mDropDownMenuAdapter.getAnimationDuration());
                        }
                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onItemClick(View clickView) {
        if (mOnMenuItemClickListener != null) {
            if (mWrapperButtons.indexOfChild(clickView) != -1) {
                mOnMenuItemClickListener.onMenuItemClick(clickView, mWrapperButtons.indexOfChild(clickView));
            }
        }
        dismiss();
    }

    public void setOuterMoreItem(View outerMoreItem) {
        this.outerMoreItem = outerMoreItem;
    }

    public void setInnerMoreItemIconId(int expandItemIconId) {
        this.expandItemIconId = expandItemIconId;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        outerMoreItem.setVisibility(View.VISIBLE);
    }

}
