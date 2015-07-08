package com.example.dinus.vitamiocamera.views;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.dinus.vitamiocamera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dinus
 */
public class CutProgressView extends RelativeLayout {
    private static final int BORDER_WIDTH_DP = 12;
    private static final int HANDLE_WIDTH_DP = 12;
    private static final int MAX_VIDEO_TIME = 15;
    private static final int THUMNAIL_HEIGHT_DP = 80;

    private LinearLayout slicesGroup;
    private ImageView border;
    private ImageView handle;
    private View leftUnselectedOverlay;
    private View rightUnselectedOverlay;

    private float minInterval;
    private float mTotalVideoTime;
    private int borderWidth;
    private  int thumnailHeight;
    private boolean draggingHandle;
    private boolean draggingStartProgress;
    private boolean draggingEndProgress;
    private int draggingLastTimeX;
    private List<ImageView> sliceViews;
    private OnValueChangedListener mOnValueChangedListener;
    public boolean isNeedLayout = true;

    public CutProgressView(Context context) {
        super(context);
        initLayout();
    }

    public CutProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public CutProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout();
    }

    private void initLayout() {
        if (slicesGroup != null) {
            return;
        }

        assert getResources() != null;

        float screenScale = getResources().getDisplayMetrics().density;
        borderWidth = (int) (BORDER_WIDTH_DP * screenScale);
        thumnailHeight = (int) (THUMNAIL_HEIGHT_DP * screenScale);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        slicesGroup = new LinearLayout(getContext());
        slicesGroup.setLayoutParams(params);
        slicesGroup.setBackgroundColor(Color.parseColor("#00000000"));
        slicesGroup.setPadding(borderWidth, 0, borderWidth, 0);
        addView(slicesGroup);

        leftUnselectedOverlay = new View(getContext());
        params = new LayoutParams(0, 0);
        params.leftMargin = borderWidth;
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftUnselectedOverlay.setLayoutParams(params);
        leftUnselectedOverlay.setBackgroundColor(Color.parseColor("#90000000"));
        addView(leftUnselectedOverlay);

        border = new ImageView(getContext());
        border.setImageResource(R.drawable.cut_video_selector_border);
        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        border.setLayoutParams(params);
        border.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(border);

        rightUnselectedOverlay = new View(getContext());
        params = new LayoutParams(0, 0);
        params.rightMargin = borderWidth;
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightUnselectedOverlay.setLayoutParams(params);
        rightUnselectedOverlay.setBackgroundColor(Color.parseColor("#90000000"));
        addView(rightUnselectedOverlay);

        handle = new ImageView(getContext());
        handle.setImageResource(R.drawable.ic_video_play_progress_handle);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        handle.setLayoutParams(params);
        handle.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(handle);

        sliceViews = new ArrayList<>();
    }

    public float getBorderWidth(){
        return borderWidth;
    }

    public float getThumnailHeight(){
        return thumnailHeight;
    }

    public void addSliceImage(Uri uri) {
        ImageView newSliceView = new ImageView(getContext());
        newSliceView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        newSliceView.setImageURI(uri);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(thumnailHeight, thumnailHeight);
        slicesGroup.addView(newSliceView, layoutParams);

    }

    public void insertPreview(int copyFrom) {
        ImageView newSliceView = new ImageView(getContext());
        newSliceView.setImageDrawable(sliceViews.get(copyFrom).getDrawable());
        sliceViews.add(copyFrom, newSliceView);
        slicesGroup.addView(newSliceView, copyFrom);

        reLayout();
    }

    public void reLayout() {
        if (sliceViews.size() <= 0){
            return ;
        }
        final int sliceWidth = getWidth() / sliceViews.size();
        final int sliceHeight = getHeight();

        post(new Runnable() {
            @Override
            public void run() {
                for (ImageView sliceView : sliceViews) {
                    LinearLayout.LayoutParams sliceParams =
                            new LinearLayout.LayoutParams(sliceWidth, sliceHeight);
                    sliceView.setLayoutParams(sliceParams);
                    sliceView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        });
    }

    public void setVideoDuration(float totalVideoTime){
        this.mTotalVideoTime = totalVideoTime;
        isNeedLayout = true;
        minInterval = 3.0f / totalVideoTime;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("Cut", event.toString());

        assert getResources() != null;

        float screenScale = getResources().getDisplayMetrics().density;

        int x = (int) event.getX();
        LayoutParams borderLayoutParams = (LayoutParams) border.getLayoutParams();
        assert borderLayoutParams != null;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (x < borderWidth + borderLayoutParams.leftMargin
                    && x > borderLayoutParams.leftMargin - borderWidth) {

                if (mOnValueChangedListener != null){
                    mOnValueChangedListener.onChangeLeftProgress();
                }

                draggingStartProgress = true;
                draggingHandle = false;
                draggingEndProgress = false;
                handle.setVisibility(INVISIBLE);

            } else if (x > getWidth() - borderWidth - borderLayoutParams.rightMargin
                    && x < getWidth() - borderLayoutParams.rightMargin + borderWidth) {

                if (mOnValueChangedListener != null){
                    mOnValueChangedListener.onChangeRightProgress();
                }

                draggingStartProgress = false;
                draggingHandle = false;
                draggingEndProgress = true;
                handle.setVisibility(INVISIBLE);
            } else {
                LayoutParams handleParams = (LayoutParams) handle.getLayoutParams();
                assert handleParams != null;
                if (handle.getVisibility() == VISIBLE
                        && x > handleParams.leftMargin + borderWidth - HANDLE_WIDTH_DP * screenScale
                        && x < handleParams.leftMargin + borderWidth + HANDLE_WIDTH_DP * screenScale) {

                    draggingStartProgress = false;
                    draggingHandle = true;
                    draggingEndProgress = false;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (draggingStartProgress) {
                int marginLeft = borderLayoutParams.leftMargin + x - draggingLastTimeX;
                if (marginLeft < 0)
                    marginLeft = 0;

                float previewProgress = marginLeft * 1.0f / (getWidth() - 2 * borderWidth);

                boolean overLine = minInterval > 0 && endProgress() - previewProgress < minInterval;
                if (overLine) {
                    previewProgress = endProgress() - minInterval;
                    marginLeft = (int) (previewProgress * (getWidth() - 2 * borderWidth));
                }

                borderLayoutParams.setMargins(marginLeft, 0, borderLayoutParams.rightMargin, 0);
                border.setLayoutParams(borderLayoutParams);

                LayoutParams leftOverlayParams = (LayoutParams) leftUnselectedOverlay.getLayoutParams();
                leftOverlayParams.width = marginLeft - borderWidth;
                leftOverlayParams.height = getHeight();
                leftUnselectedOverlay.setLayoutParams(leftOverlayParams);

                if (mOnValueChangedListener != null) {
                    if (previewProgress > 0 && previewProgress < 1) {
                        mOnValueChangedListener.onChangePreviewProgress(previewProgress);
                    }
                }

                if (overLine) {
                    return false;
                }
            } else if (draggingHandle) {
                LayoutParams handleLayoutParams = (LayoutParams) handle.getLayoutParams();
                assert handleLayoutParams != null;
                int marginLeft = handleLayoutParams.leftMargin + x - draggingLastTimeX;
                if (marginLeft < 0)
                    marginLeft = 0;
                handleLayoutParams.setMargins(marginLeft, 0, 0, 0);
                handle.setLayoutParams(handleLayoutParams);

                if (mOnValueChangedListener != null) {
                    float previewProgress = (marginLeft * 1.0f + handle.getWidth() / 2 - borderWidth) / (getWidth() - 2 * borderWidth);
                    if (previewProgress > 0 && previewProgress < 1) {
                        mOnValueChangedListener.onChangePreviewProgress(previewProgress);
                    }
                }
            } else if (draggingEndProgress) {
                int marginRight = borderLayoutParams.rightMargin + draggingLastTimeX - x;
                if (marginRight < 0)
                    marginRight = 0;

                float previewProgress = 1 - marginRight * 1.0f / (getWidth() - 2 * borderWidth);
                boolean overLine = minInterval > 0 && previewProgress - startProgress() < minInterval;
                if (overLine) {
                    previewProgress = minInterval + startProgress();
                    marginRight = (int) ((1 - previewProgress) * (getWidth() - 2 * borderWidth));
                }

                borderLayoutParams.setMargins(borderLayoutParams.leftMargin, 0, marginRight, 0);
                border.setLayoutParams(borderLayoutParams);

                LayoutParams rightOverlayParams = (LayoutParams) rightUnselectedOverlay.getLayoutParams();
                rightOverlayParams.width = marginRight - borderWidth;
                rightOverlayParams.height = getHeight();
                rightUnselectedOverlay.setLayoutParams(rightOverlayParams);

                if (mOnValueChangedListener != null) {
                    if (previewProgress > 0 && previewProgress < 1) {
                        mOnValueChangedListener.onChangePreviewProgress(previewProgress);
                    }
                }

                if (overLine) {
                    return false;
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {

            if (mOnValueChangedListener != null){
                mOnValueChangedListener.onCompleteProgress();
            }

            draggingStartProgress = false;
            draggingHandle = false;
            draggingEndProgress = false;
            draggingLastTimeX = 0;
            if (borderLayoutParams.leftMargin == 0 && borderLayoutParams.rightMargin == 0) {
                handle.setVisibility(VISIBLE);
            }
        }

        draggingLastTimeX = x;

        return true;
    }

    public void setOnValueChangedListener(OnValueChangedListener mOnValueChangedListener) {
        this.mOnValueChangedListener = mOnValueChangedListener;
    }

    public float startProgress() {
        LayoutParams borderLayoutParams = (LayoutParams) border.getLayoutParams();
        assert borderLayoutParams != null;
        return borderLayoutParams.leftMargin * 1.0f / (getWidth() - 2 * borderWidth);
    }

    public float endProgress() {
        LayoutParams borderLayoutParams = (LayoutParams) border.getLayoutParams();
        assert borderLayoutParams != null;
        return 1 - borderLayoutParams.rightMargin * 1.0f / (getWidth() - 2 * borderWidth);
    }

    public interface OnValueChangedListener {
        void onChangePreviewProgress(float previewProgress);

        void onCompleteProgress();

        void onChangeLeftProgress();

        void onChangeRightProgress();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measureWidth(int measureSpec){
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);

        if (mode == MeasureSpec.EXACTLY){
            if (mTotalVideoTime / MAX_VIDEO_TIME > 1){
                return size;
            }

            return (int) (size * mTotalVideoTime / MAX_VIDEO_TIME);
        }

        int result = size +  getPaddingLeft() + getPaddingRight() ;
        if (mTotalVideoTime / MAX_VIDEO_TIME > 1){
            return result;
        }

        return (int) (result * mTotalVideoTime / MAX_VIDEO_TIME);
    }

    private int measureHeight(int measureSpec){
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);

        if (mode == MeasureSpec.EXACTLY){
             return size;
         }

        int result = size +  getPaddingLeft() + getPaddingRight() ;

        return result;
    }

    public void setProgress(double progress) {
        handle.setVisibility(VISIBLE);
        LayoutParams handleLayoutParams = (LayoutParams) handle.getLayoutParams();
        int marginLeft = (int) (progress * (getWidth() - 2 * borderWidth) + borderWidth - handle.getMeasuredWidth() / 2);
        if (marginLeft < 0)
            marginLeft = 0;
        handleLayoutParams.setMargins(marginLeft, 0, 0, 0);
        handle.setLayoutParams(handleLayoutParams);
     }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        reLayout();
    }
}