package com.example.dinus.androiddemo.contextmenu;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.dinus.androiddemo.R;

import java.util.ArrayList;

public class ContextMenuFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    public static final String TAG = ContextMenuFragment.class.getName() + "TAG";
    private static final String ARG_MENU_PARAMS = "ARG_MENU_PARAMS";

    private ArrayList<Integer> mMenuParams;

    private RelativeLayout mWrapperButtons;
    private MenuAdapter mDropDownMenuAdapter;

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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDropDownMenuAdapter.menuToggle();
            }
        }, 0);
    }
}
