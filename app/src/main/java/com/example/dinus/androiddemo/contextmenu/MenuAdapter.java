package com.example.dinus.androiddemo.contextmenu;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

public class MenuAdapter {
    public static final int ANIMATION_DURATION_MILLIS = 300;

    private OnMenuItemClickListener mOnMenuItemClickListener;

    private List<Integer> mMenuObjects;
    private RelativeLayout mMenuWrapper;
    private Context mContext;
    private boolean mIsMenuOpen = false;
    private boolean mIsAnimationRun = false;
    private float screenWidth;
    private float screenHeight;
    private AnimatorSet mAnimatorSetHideMenu;
    private AnimatorSet mAnimatorSetShowMenu;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(View clickedView, int position);
    }

    public MenuAdapter(Context mContext, List<Integer> mMenuObjects, RelativeLayout mMenuWrapper) {
        this.mMenuObjects = mMenuObjects;
        this.mMenuWrapper = mMenuWrapper;
        this.mContext = mContext;

        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = manager.getDefaultDisplay();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        setViews();
        resetAnimations();
        mAnimatorSetHideMenu = setOpenCloseAnimation(true);
        mAnimatorSetShowMenu = setOpenCloseAnimation(false);

    }

    private void setViews() {
        for (int i = 0; i < mMenuObjects.size(); i++) {
            ImageView menuItem = new ImageView(mContext);
            menuItem.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            menuItem.setImageResource(mMenuObjects.get(i));

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(300, 300);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.rightMargin = -150;
            mMenuWrapper.addView(menuItem, layoutParams);
        }
    }

    public int getItemCount(){
        return mMenuObjects.size();
    }

    public void menuToggle() {
        if (!mIsAnimationRun) {
            resetAnimations();
            mIsAnimationRun = true;
            if (mIsMenuOpen) {
                mAnimatorSetHideMenu.start();
            } else {
                mAnimatorSetShowMenu.start();
            }
            toggleIsMenuOpen();
        }
    }

    private void resetAnimations() {
        for (int i = 0; i < getItemCount(); i++) {
//            mMenuWrapper.getChildAt(i).setX(0);
//            mMenuWrapper.getChildAt(i).setY(0);
        }
    }


    private AnimatorSet setOpenCloseAnimation(boolean isCloseAnimation) {
        List<Animator> imageAnimations = new ArrayList<>();

        if (isCloseAnimation) {
            for (int i = getItemCount() - 1; i >= 0; i--) {
                fillOpenClosingAnimations(true, imageAnimations, i);
            }
        } else {
            for (int i = 0; i < getItemCount(); i++) {
                fillOpenClosingAnimations(false, imageAnimations, i);
            }
        }

        AnimatorSet imageCloseAnimatorSet = new AnimatorSet();
        imageCloseAnimatorSet.playSequentially(imageAnimations);

        imageCloseAnimatorSet.setDuration(5000);
        imageCloseAnimatorSet.addListener(mCloseOpenAnimatorListener);
        imageCloseAnimatorSet.setStartDelay(0);
        imageCloseAnimatorSet.setInterpolator(new OvershootInterpolator());
        return imageCloseAnimatorSet;
    }

    private void fillOpenClosingAnimations(boolean isCloseAnimation, List<Animator> imageAnimations, int wrapperPosition) {
        Animator imageRotation = isCloseAnimation ? getCloseAnimator(wrapperPosition) : getOpenAnimator(wrapperPosition);
        imageAnimations.add(imageRotation);
    }

    private Animator getOpenAnimator(int wrapperPosition){
        AnimatorSet openFullAnimator = new AnimatorSet();
        ObjectAnimator transXAnimator = AnimatorUtils.translationX(mMenuWrapper.getChildAt(wrapperPosition), 0.0f, -screenWidth / 2.0f);
        ObjectAnimator transYAnimator = AnimatorUtils.translationY(mMenuWrapper.getChildAt(wrapperPosition), 0.0f, screenHeight * wrapperPosition / getItemCount());
        ObjectAnimator scaleYAnimator = AnimatorUtils.scaleY(mMenuWrapper.getChildAt(wrapperPosition), 0.0f, 1.0f);
        ObjectAnimator scaleXAnimator = AnimatorUtils.scaleX(mMenuWrapper.getChildAt(wrapperPosition), 0.0f, 1.0f);

        openFullAnimator.play(transXAnimator).with(transYAnimator).with(scaleXAnimator).with(scaleYAnimator);
        return openFullAnimator;
    }

    private ObjectAnimator getCloseAnimator(int wrapperPosition){
        ObjectAnimator closeAnimator = AnimatorUtils.translationRight(mMenuWrapper.getChildAt(wrapperPosition), screenWidth / 2.0f);

        return closeAnimator;
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
