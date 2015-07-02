package com.example.dinus.androiddemo;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class CoverSelectView extends FrameLayout {

    private int THUMNAIL_COUNT = 10;

    private float previewHeight;
    private float imageHeight;
    private float instricPreviewMargin;
    private float mLastX;
    private boolean mStartMove;
    private int mCurrentIndex;
    private LinearLayout mSlicesGroup;
    private ImageView mPreviewImage;


    private List<Uri> sliceImageList = new ArrayList<>();
    private List<Integer> sliceIdList = new ArrayList<>();

    public CoverSelectView(Context context) {
        this(context, null);
    }

    public CoverSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        imageHeight = metrics.widthPixels / (THUMNAIL_COUNT + 2);
        previewHeight = imageHeight * 2;
        instricPreviewMargin = imageHeight / 2;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) previewHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        mSlicesGroup = new LinearLayout(getContext());
        mSlicesGroup.setLayoutParams(params);
        mSlicesGroup.setBackgroundColor(0xff0000ff);
        mSlicesGroup.setGravity(Gravity.CENTER);
        addView(mSlicesGroup);

        mPreviewImage = new ImageView(getContext());
        mPreviewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mPreviewImage.setBackgroundResource(R.drawable.ic_video_cover_selected_border);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) previewHeight, (int) previewHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = (int) instricPreviewMargin;
        addView(mPreviewImage, layoutParams);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void addSlickImage(Uri uri) {
        sliceImageList.add(uri);

        addItemUri(sliceImageList.size() - 1);

        if (sliceImageList.size() == 1){
            mPreviewImage.setImageURI(uri);
        }
    }

    public void addSliceImage(int imageId) {
        sliceIdList.add(imageId);

        addItemId(sliceIdList.size() - 1);
        if (sliceIdList.size() == 1) {
            mPreviewImage.setImageResource(imageId);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x > mPreviewImage.getLeft() && x < mPreviewImage.getRight()) {
                    mStartMove = true;
                    mLastX = x;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mStartMove) {
                    FrameLayout.LayoutParams layoutParams = (LayoutParams) mPreviewImage.getLayoutParams();
                    layoutParams.leftMargin += (x - mLastX);

                    if (layoutParams.leftMargin < instricPreviewMargin) {
                        layoutParams.leftMargin = (int) instricPreviewMargin;
                    } else {
                        if (getWidth() - mPreviewImage.getRight() < instricPreviewMargin) {
                            layoutParams.leftMargin = (int) (getWidth() - instricPreviewMargin - mPreviewImage.getWidth());
                        }
                    }
                    mPreviewImage.setLayoutParams(layoutParams);

                    int currentIndex = (int) ((mPreviewImage.getLeft() - instricPreviewMargin) / imageHeight);
                    if (currentIndex >= THUMNAIL_COUNT){
                        currentIndex = THUMNAIL_COUNT - 1;
                    } else if (currentIndex < 0){
                        currentIndex = 0;
                    }

                    if (mCurrentIndex != currentIndex) {
                        mPreviewImage.setImageResource(sliceIdList.get(currentIndex));
                        mCurrentIndex = currentIndex;
                    }

                    mLastX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int currentIndex = (int) ((x - instricPreviewMargin) / imageHeight);
                if (currentIndex >= THUMNAIL_COUNT){
                    currentIndex = THUMNAIL_COUNT - 1;
                } else if (currentIndex < 0){
                    currentIndex = 0;
                }
                if (mCurrentIndex != currentIndex ) {
                    mPreviewImage.setImageResource(sliceIdList.get(currentIndex));
                    mCurrentIndex = currentIndex;
                    FrameLayout.LayoutParams layoutParams = (LayoutParams) mPreviewImage.getLayoutParams();
                    layoutParams.leftMargin = (int) (instricPreviewMargin + imageHeight * currentIndex);
                    mPreviewImage.setLayoutParams(layoutParams);
                }

                mStartMove = false;
                mLastX = 0;
                break;
        }

        return true;
    }


    private void addItemId(int index) {

        ImageView thumnailView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.view_cover_layout, null);
        thumnailView.setImageResource(sliceIdList.get(index));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) imageHeight, (int) imageHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mSlicesGroup.addView(thumnailView, layoutParams);

    }


    private void addItemUri(int index) {

        if (index >= sliceImageList.size()){
            return ;
        }

        ImageView thumnailView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.view_cover_layout, null);
        thumnailView.setImageURI(sliceImageList.get(index));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) imageHeight, (int) imageHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mSlicesGroup.addView(thumnailView, layoutParams);

    }
}
