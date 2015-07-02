package com.example.dinus.androiddemo.contextmenu;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;

import java.util.List;

public class MenuAdapter {
    public static final int ANIMATION_DURATION_MILLIS = 100;

    private OnMenuItemClickListener mOnMenuItemClickListener;

    private List<Integer> mMenuObjects;
    private LinearLayout mMenuWrapper;
    private Context mContext;

    private boolean mIsMenuOpen = false;
    private boolean mIsAnimationRun = false;
    private int mMenuItemSize;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(View clickedView, int position);
    }

    public MenuAdapter(Context mContext, List<Integer> mMenuObjects, LinearLayout mMenuWrapper, int mMenuItemSize) {
        this.mMenuObjects = mMenuObjects;
        this.mMenuWrapper = mMenuWrapper;
        this.mContext = mContext;
        this.mMenuItemSize = mMenuItemSize;

        setViews();

    }

    private void setViews() {
        for (int i = 0; i < mMenuObjects.size(); i++) {
            ImageView menuItem = new ImageView(mContext);
            menuItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            menuItem.setImageResource(mMenuObjects.get(i));
            mMenuWrapper.addView(menuItem);
        }
    }

    public void menuToggle() {
        if (!mIsAnimationRun) {
            resetAnimations();
            mIsAnimationRun = true;
            if (mIsMenuOpen) {
//                mAnimatorSetHideMenu.start();
            } else {
//                mAnimatorSetShowMenu.start();
            }
            toggleIsMenuOpen();
        }
    }

    private void resetAnimations() {
    }

    private void toggleIsAnimationRun() {
        mIsAnimationRun = !mIsAnimationRun;
    }

    private void toggleIsMenuOpen() {
        mIsMenuOpen = !mIsMenuOpen;
    }


    private Animator.AnimatorListener mCloseOpenAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            toggleIsAnimationRun();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    private Animator.AnimatorListener mChosenItemFinishAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            toggleIsAnimationRun();

        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

}
