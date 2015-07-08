package com.example.dinus.vitamiocamera.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.dinus.vitamiocamera.R;
import com.yixia.weibo.sdk.model.MediaObject;
import com.yixia.weibo.sdk.util.DeviceUtils;

import java.util.Iterator;

public class RecordProgressView extends View {

 	private Paint mProgressPaint;
	private Paint mPausePaint;
	private Paint mRemovePaint;
	private Paint mThreePaint;
	private Paint mOverflowPaint;

 	private MediaObject mMediaObject;

 	private int mMaxDuration, mVLineWidth;

	public RecordProgressView(Context paramContext) {
		super(paramContext);
		init();
	}

	public RecordProgressView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		init();
	}

	public RecordProgressView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		init();
	}

	private void init() {
		mProgressPaint = new Paint();
		mPausePaint = new Paint();
		mRemovePaint = new Paint();
		mThreePaint = new Paint();
		mOverflowPaint = new Paint();

		mVLineWidth = DeviceUtils.dipToPX(getContext(), 1);

		setBackgroundColor(getResources().getColor(R.color.camera_bg));
		mProgressPaint.setColor(getResources().getColor(R.color.title_background_color));
		mProgressPaint.setStyle(Paint.Style.FILL);

		mPausePaint.setColor(getResources().getColor(R.color.camera_progress_split));
		mPausePaint.setStyle(Paint.Style.FILL);

		mRemovePaint.setColor(getResources().getColor(R.color.camera_progress_delete));
		mRemovePaint.setStyle(Paint.Style.FILL);

		mThreePaint.setColor(getResources().getColor(R.color.camera_progress_three));
		mThreePaint.setStyle(Paint.Style.FILL);

		mOverflowPaint.setColor(getResources().getColor(R.color.camera_progress_overflow));
		mOverflowPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width 			= getMeasuredWidth();
		int height 			= getMeasuredHeight();
		int left 			= 0;
		int right 			= 0;
		int duration		= 0;

		if (mMediaObject != null && mMediaObject.getMedaParts() != null) {
 			Iterator<MediaObject.MediaPart> iterator = mMediaObject.getMedaParts().iterator();
			int maxDuration = mMaxDuration;
 			int currentDuration = mMediaObject.getDuration();
			boolean hasOutDuration = currentDuration > mMaxDuration;

			if (hasOutDuration)
				maxDuration = currentDuration;

			while (iterator.hasNext()) {
                MediaObject.MediaPart vp = iterator.next();
				int partDuration = vp.getDuration();
				left = right;
				right = left + (int) (partDuration * 1.0F / maxDuration * width);

				if (vp.remove) {
					canvas.drawRect(left, 0.0F, right, height, mRemovePaint);
				} else {
					if (hasOutDuration) {
						right = left + (int) ((mMaxDuration - duration) * 1.0F / maxDuration * width);
						canvas.drawRect(left, 0.0F, right, height, mProgressPaint);

						left = right;
						right = left + (int) ((partDuration - (mMaxDuration - duration)) * 1.0F / maxDuration * width);
						canvas.drawRect(left, 0.0F, right, height, mOverflowPaint);
					} else {
						canvas.drawRect(left, 0.0F, right, height, mProgressPaint);
					}
				}

 				if (iterator.hasNext()) {
					canvas.drawRect(right - mVLineWidth, 0.0F, right, height, mPausePaint);
				}

				duration += partDuration;
			}
		}

		//画三秒
		if (duration < 3000) {
			left = (int) (3000F / mMaxDuration * width);
			canvas.drawRect(left, 0.0F, left + mVLineWidth, height, mThreePaint);
		}

	}

	public void setData(MediaObject mMediaObject) {
		this.mMediaObject = mMediaObject;
	}

	public void setMaxDuration(int duration) {
		this.mMaxDuration = duration;
	}

}
