package com.example.dinus.vitamiocamera.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yixia.camera.demo.log.Logger;
import com.yixia.weibo.sdk.util.StringUtils;

public class BitmapImageView extends ImageView {

	private Bitmap mBitmap;
	private String mPath;

	public BitmapImageView(Context context) {
		super(context);
	}

	public BitmapImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setImagePath(String path) {
		release();
		if (StringUtils.isNotEmpty(path)) {
			try {
				mBitmap = BitmapFactory.decodeFile(path);
				if (mBitmap != null && !mBitmap.isRecycled()) {
					mPath = path;
					setImageBitmap(mBitmap);
				}
			} catch (OutOfMemoryError e) {
				Logger.e(e);
			} catch (Exception e) {
				Logger.e(e);
			}
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (StringUtils.isNotEmpty(mPath)) {
			setImagePath(mPath);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		release();
	}

	public void release() {
		if (mBitmap != null) {
			if (!mBitmap.isRecycled())
				mBitmap.recycle();
			mBitmap = null;
		}
	}
}