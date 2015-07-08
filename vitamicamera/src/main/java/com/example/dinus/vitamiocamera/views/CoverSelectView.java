package com.example.dinus.vitamiocamera.views;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.dinus.vitamiocamera.R;
import com.yixia.weibo.sdk.util.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

public class CoverSelectView extends FrameLayout {

    private int THUMNAIL_COUNT = 10;
    private int DEFAULT_PREVIEW_IMAGE_PADDING_DP = 3;

    private int previewHeight;
    private int imageHeight;
    private float instricPreviewMargin;
    private float mLastX;
    private int mCurrentIndex;
    private boolean mStartMove;

    private LinearLayout mSlicesGroup;
    private ImageView mPreviewImage;

    private List<Uri> sliceImageList = new ArrayList<>();

    private OnCoverSelectListener mOnCoverSelectListener;

    public CoverSelectView(Context context) {
        this(context, null);
    }

    public CoverSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public float getThumnailHeight(){
        return imageHeight;
    }

    private void init() {

        imageHeight = DeviceUtils.getScreenWidth(getContext()) / (THUMNAIL_COUNT + 2);
        previewHeight = imageHeight * 2;
        instricPreviewMargin = imageHeight / 2;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) previewHeight);
        params.gravity = Gravity.CENTER_VERTICAL;
        mSlicesGroup = new LinearLayout(getContext());
        mSlicesGroup.setLayoutParams(params);
        mSlicesGroup.setGravity(Gravity.CENTER_VERTICAL);
        addView(mSlicesGroup);
        mSlicesGroup.setPadding(imageHeight, 0, imageHeight, 0);

        int previewPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PREVIEW_IMAGE_PADDING_DP,
                getResources().getDisplayMetrics());
        mPreviewImage = new ImageView(getContext());
        mPreviewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mPreviewImage.setBackgroundResource(R.drawable.ic_video_cover_selected_border);
        mPreviewImage.setPadding(previewPadding, previewPadding, previewPadding, previewPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams( previewHeight, previewHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = (int) instricPreviewMargin;
        addView(mPreviewImage, layoutParams);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void addSliceImage(Uri uri) {
        sliceImageList.add(uri);

        addItemUri(sliceImageList.size() - 1);

        if (sliceImageList.size() == 1) {
            mPreviewImage.setImageURI(uri);
            if (mOnCoverSelectListener != null){
                mCurrentIndex = 0;
                mOnCoverSelectListener.onCoverChangedListener(0,  sliceImageList.get(0));
            }
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
                    if (currentIndex >= THUMNAIL_COUNT) {
                        currentIndex = THUMNAIL_COUNT - 1;
                    } else if (currentIndex < 0) {
                        currentIndex = 0;
                    }

                    if (mCurrentIndex != currentIndex && currentIndex < sliceImageList.size()) {
                        if (mOnCoverSelectListener != null){
                            mOnCoverSelectListener.onCoverChangedListener(currentIndex, sliceImageList.get(currentIndex));
                        }

                        mPreviewImage.setImageURI(sliceImageList.get(currentIndex));
                        mCurrentIndex = currentIndex;
                    }

                    mLastX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int currentIndex = (int) ((x - instricPreviewMargin) / imageHeight);
                if (currentIndex >= THUMNAIL_COUNT) {
                    currentIndex = THUMNAIL_COUNT - 1;
                } else if (currentIndex < 0) {
                    currentIndex = 0;
                }
                if (mCurrentIndex != currentIndex && currentIndex < sliceImageList.size()) {
                    if (mOnCoverSelectListener != null && currentIndex < sliceImageList.size()){
                        mOnCoverSelectListener.onCoverChangedListener(currentIndex, sliceImageList.get(currentIndex));
                    }
                    mPreviewImage.setImageURI(sliceImageList.get(currentIndex));
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


    private void addItemUri(int index) {

        if (index >= sliceImageList.size()) {
            return;
        }

        ImageView thumnailView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.view_cover_layout, null);
        thumnailView.setImageURI(sliceImageList.get(index));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( imageHeight,  imageHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mSlicesGroup.addView(thumnailView, layoutParams);

    }

    public interface OnCoverSelectListener{
        void onCoverChangedListener(int index, Uri uri);
    }

    public void setOnCoverSelectListener(OnCoverSelectListener mOnCoverSelectListener) {
        this.mOnCoverSelectListener = mOnCoverSelectListener;
    }
}
