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

    private Context mContext;

    private RelativeLayout mMenuWrapper;
    private View mMoreItemView;

    private List<Integer> mMenuObjects;
    private AnimatorSet mAnimatorSetHideMenu;
    private AnimatorSet mAnimatorSetShowMenu;

    private int screenWidth;
    private int screenHeight;
    private int childHeight;
    private int childMargin;
    private int translateBaseX;
    private int translateBaseY;
    private boolean mIsMenuOpen = false;
    private boolean mIsAnimationRun = false;

    private OnItemClickListener mItemClickListener;
    private OnItemClickListener mItemClickListenerCallBack;
    private View mClickedView;

    private View.OnClickListener menuItemClick = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mItemClickListenerCallBack = mItemClickListener;
            viewClicked(v);
        }
    };

    public interface OnMenuItemClickListener {
        void onMenuItemClick(View clickedView, int position);
    }

    public interface OnItemClickListener{
        void onItemClick(View clickView);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public MenuAdapter(Context mContext, List<Integer> mMenuObjects, RelativeLayout mMenuWrapper, View moreItemView) {
        this.mMenuObjects = mMenuObjects;
        this.mMenuWrapper = mMenuWrapper;
        this.mMoreItemView = moreItemView;
        this.mContext = mContext;

        init();
        setViews();
        resetAnimations();
        mAnimatorSetHideMenu = setOpenCloseAnimation(true);
        mAnimatorSetShowMenu = setOpenCloseAnimation(false);
    }

    private void init() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = manager.getDefaultDisplay();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        if (getItemCount() <= 4) {
            childHeight = screenHeight / 7;
        } else {
            childHeight = screenHeight / (getItemCount() + 3);
        }
        childMargin = childHeight / 5;

        translateBaseX = (screenWidth + childHeight) / 2;
        translateBaseY = (screenHeight - childHeight * getItemCount() - childMargin * (getItemCount() - 1)) / 2 + childHeight;
    }

    private void setViews() {
        for (int i = 0; i < mMenuObjects.size(); i++) {
            ImageView menuItem = new ImageView(mContext);
            menuItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            menuItem.setImageResource(mMenuObjects.get(i));
            menuItem.setOnClickListener(menuItemClick);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(childHeight, childHeight);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.rightMargin = -childHeight;
            layoutParams.topMargin = -childHeight;
            mMenuWrapper.addView(menuItem, layoutParams);
        }

        mMoreItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListenerCallBack = mItemClickListener;
                menuToggle();
            }
        });
    }

    public long getAnimationDuration(){
        return ANIMATION_DURATION_MILLIS;
    }

    private void viewClicked(View view){
        if (mIsMenuOpen && !mIsAnimationRun){
            mClickedView = view;
            int childIndex = mMenuWrapper.indexOfChild(view);
            if (childIndex == -1){
                return ;
            }
            menuToggle();
        }
    }

    public int getItemCount() {
        return mMenuObjects.size();
    }

    public boolean menuToggle() {
        if (!mIsAnimationRun) {
            resetAnimations();
            toggleIsAnimationRun();
            if (mIsMenuOpen) {
                mAnimatorSetHideMenu.start();
            } else {
                mAnimatorSetShowMenu.start();
            }
            toggleIsMenuOpen();
            return true;
        }

        return false;
    }

    private void resetAnimations() {

    }


    private AnimatorSet setOpenCloseAnimation(boolean isCloseAnimation) {
        List<Animator> imageAnimators = new ArrayList<>();

        if (isCloseAnimation) {
            for (int i = getItemCount() - 1; i >= 0; i--) {
                fillOpenClosingAnimations(true, imageAnimators, i);
            }
        } else {
            for (int i = 0; i < getItemCount(); i++) {
                fillOpenClosingAnimations(false, imageAnimators, i);
            }
        }

        Animator moreAnimator = AnimatorUtils.rotate(mMoreItemView, isCloseAnimation ? 45.0f : 0.0f, isCloseAnimation? 0.0f : 45.0f);
        moreAnimator.setInterpolator(new OvershootInterpolator(2.0f));
        moreAnimator.setDuration(ANIMATION_DURATION_MILLIS);
        imageAnimators.add(moreAnimator);

        AnimatorSet imageCloseAnimatorSet = new AnimatorSet();
        imageCloseAnimatorSet.playTogether(imageAnimators);
        imageCloseAnimatorSet.addListener(isCloseAnimation ? mCloseAnimatorListener : mOpenAnimatorListener);
        imageCloseAnimatorSet.setDuration(ANIMATION_DURATION_MILLIS);
        imageCloseAnimatorSet.setStartDelay(0);
        imageCloseAnimatorSet.setInterpolator(new OvershootInterpolator());
        return imageCloseAnimatorSet;
    }

    private void fillOpenClosingAnimations(boolean isCloseAnimation, List<Animator> imageAnimations, int wrapperPosition) {
        Animator imageRotation = isCloseAnimation ? getCloseAnimator(wrapperPosition) : getOpenAnimator(wrapperPosition);
        imageAnimations.add(imageRotation);
    }

    private Animator getOpenAnimator(int wrapperPosition) {
        AnimatorSet openFullAnimator = new AnimatorSet();
        ObjectAnimator transXAnimator = AnimatorUtils.translationX(mMenuWrapper.getChildAt(wrapperPosition), 0.0f, -translateBaseX);
        ObjectAnimator transYAnimator = AnimatorUtils.translationY(mMenuWrapper.getChildAt(wrapperPosition), 0.0f,
                translateBaseY + (childHeight + childMargin) * wrapperPosition);
        ObjectAnimator scaleYAnimator = AnimatorUtils.scaleY(mMenuWrapper.getChildAt(wrapperPosition), 0.0f, 1.0f);
        ObjectAnimator scaleXAnimator = AnimatorUtils.scaleX(mMenuWrapper.getChildAt(wrapperPosition), 0.0f, 1.0f);
        openFullAnimator.play(transXAnimator).with(transYAnimator).with(scaleXAnimator).with(scaleYAnimator);
        openFullAnimator.setStartDelay((getItemCount() - wrapperPosition - 1) * 50);
        return openFullAnimator;
    }

    private Animator getCloseAnimator(int wrapperPosition) {
        AnimatorSet closeFullAnimator = new AnimatorSet();
        ObjectAnimator transXAnimator = AnimatorUtils.translationX(mMenuWrapper.getChildAt(wrapperPosition), -translateBaseX, 0);
        ObjectAnimator transYAnimator = AnimatorUtils.translationY(mMenuWrapper.getChildAt(wrapperPosition),
                translateBaseY + (childHeight + childMargin) * wrapperPosition, 0.0f);
        ObjectAnimator scaleYAnimator = AnimatorUtils.scaleY(mMenuWrapper.getChildAt(wrapperPosition), 1.0f, 0.0f);
        ObjectAnimator scaleXAnimator = AnimatorUtils.scaleX(mMenuWrapper.getChildAt(wrapperPosition), 1.0f, 0.0f);
        closeFullAnimator.play(transXAnimator).with(transYAnimator).with(scaleXAnimator).with(scaleYAnimator);
        closeFullAnimator.setStartDelay( wrapperPosition  * 50);
        return closeFullAnimator;
    }

    private void toggleIsAnimationRun() {
        mIsAnimationRun = !mIsAnimationRun;
    }

    private void toggleIsMenuOpen() {
        mIsMenuOpen = !mIsMenuOpen;
    }

    private Animator.AnimatorListener mOpenAnimatorListener = new Animator.AnimatorListener() {
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
    private Animator.AnimatorListener mCloseAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            toggleIsAnimationRun();
            if (mItemClickListenerCallBack != null){
                mItemClickListenerCallBack.onItemClick(mClickedView);
                mItemClickListenerCallBack = null;
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

}
