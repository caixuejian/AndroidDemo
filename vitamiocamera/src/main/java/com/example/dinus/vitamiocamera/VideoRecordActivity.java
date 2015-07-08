package com.example.dinus.vitamiocamera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.dinus.vitamiocamera.common.CommonIntentExtra;
import com.example.dinus.vitamiocamera.views.RecordProgressView;
import com.yixia.camera.demo.log.Logger;
import com.yixia.camera.demo.utils.ConvertToUtils;
import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.MediaRecorderBase;
import com.yixia.weibo.sdk.MediaRecorderBase.OnErrorListener;
import com.yixia.weibo.sdk.MediaRecorderBase.OnPreparedListener;
import com.yixia.weibo.sdk.MediaRecorderNative;
import com.yixia.weibo.sdk.MediaRecorderSystem;
import com.yixia.weibo.sdk.VCamera;
import com.yixia.weibo.sdk.VideoProcessEngine;
import com.yixia.weibo.sdk.model.MediaObject;
import com.yixia.weibo.sdk.model.MediaObject.MediaPart;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;

import java.io.File;

/**
 * @author dinus
 */
public class VideoRecordActivity extends BaseActivity implements OnErrorListener, OnClickListener, OnPreparedListener, MediaRecorderBase.OnEncodeListener {

    public static final String INPUT_VIDEO_PATH = VideoRecordActivity.class.getName() + "video_path";
    public static final String OUTPUT_VIDEO_PATH = VideoRecordActivity.class.getName() + "output_path";
    public static final String OUTPUT_COVER_PATH = VideoRecordActivity.class.getName() + "thumanil_path";
    public static final String INPUT_VIDEO_DURATION = VideoRecordActivity.class.getName() + "video_duration";

    //record max time
    public final static int RECORD_TIME_MAX = 15 * 1000;
    //record min time
    public final static int RECORD_TIME_MIN = 3 * 1000;
    //refresh progress
    private static final int HANDLE_INVALIDATE_PROGRESS = 0;
    //delay record stop
    private static final int HANDLE_STOP_RECORD = 1;
    //record focus
    private static final int HANDLE_HIDE_RECORD_FOCUS = 2;

    private static final int REQUEST_CODE_FOR_PICK_VIDEO = 1;
    private static final int REQEUST_CODE_FOR_PROCESS_VIDEO = 2;
    //next step
    private ImageView mRecordNext;
    private ImageView mFocusImage;
    private ImageView mRecordClose;
    private ImageView mSelectVideo;
    private CheckBox mCameraSwitch;
    private CheckedTextView mRecordDelete;
    private CheckBox mRecordLed;
    //record button
    private ImageView mRecordController;

    private LinearLayout mBottomLayout;
    private SurfaceView mSurfaceView;
    private RecordProgressView mProgressView;
    //focus animation
    private Animation mFocusAnimation;

    //mediareocder
    private MediaRecorderBase mMediaRecorder;

    //media message
    private MediaObject mMediaObject;

    //need rebuild or delete
    private boolean mRebuild;
    private boolean mCreated;
    private volatile boolean mPressedStatus;
    private volatile boolean mReleased;
    private int mFocusWidth;
    private int mWindowWidth;
    private String mOutputVideoPath;
    private String mOutputCoverPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCreated = false;
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        loadParams();
        loadViews();
        mCreated = true;

    }


    private void loadParams() {
        mWindowWidth = DeviceUtils.getScreenWidth(this);
        mFocusWidth = ConvertToUtils.dipToPX(this, 64);
        mOutputCoverPath = getIntent().getStringExtra(VideoRecordActivity.OUTPUT_COVER_PATH);
        mOutputVideoPath = getIntent().getStringExtra(VideoRecordActivity.OUTPUT_VIDEO_PATH);
    }

    private void loadViews() {
        setContentView(R.layout.activity_video_recorder);
        // ~~~ bind view
        mSurfaceView = (SurfaceView) findViewById(R.id.record_preview);
        mCameraSwitch = (CheckBox) findViewById(R.id.record_camera_switcher);
        mRecordNext = (ImageView) findViewById(R.id.title_next);
        mFocusImage = (ImageView) findViewById(R.id.record_focusing);
        mProgressView = (RecordProgressView) findViewById(R.id.record_progress);
        mRecordDelete = (CheckedTextView) findViewById(R.id.record_delete);
        mRecordController = (ImageView) findViewById(R.id.record_controller);
        mRecordLed = (CheckBox) findViewById(R.id.record_camera_led);
        mBottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        mRecordClose = (ImageView) findViewById(R.id.close_record);
        mSelectVideo = (ImageView) findViewById(R.id.select_local_video);

        mRecordNext.setEnabled(false);

        mRecordNext.setOnClickListener(this);
        mRecordClose.setOnClickListener(this);
        mRecordDelete.setOnClickListener(this);
        mSelectVideo.setOnClickListener(this);
        mRecordController.setOnTouchListener(mOnVideoControllerTouchListener);

        if (DeviceUtils.hasICS()) {
            mSurfaceView.setOnTouchListener(mOnSurfaveViewTouchListener);
        }

        if (MediaRecorderBase.isSupportFrontCamera()) {
            mCameraSwitch.setOnClickListener(this);
        } else {
            mCameraSwitch.setVisibility(View.GONE);
        }

        if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
            mRecordLed.setOnClickListener(this);
        } else {
            mRecordLed.setVisibility(View.GONE);
        }

        try {
            mFocusImage.setImageResource(R.drawable.ic_video_focus);
        } catch (OutOfMemoryError e) {
            Logger.e(e);
        }

        mProgressView.setMaxDuration(RECORD_TIME_MAX);
        initSurfaceView();
    }

    // init surface
    private void initSurfaceView() {
        final int w = DeviceUtils.getScreenWidth(this);
        // DeviceUtils.dipToPX(this, 55f) is operation layout height
        ((RelativeLayout.LayoutParams) mBottomLayout.getLayoutParams()).topMargin = w - DeviceUtils.dipToPX(this, 55f);
        int width = w;
        int height = w * 4 / 3;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mSurfaceView.setLayoutParams(lp);
    }

    private void initMediaRecorder() {

        mMediaRecorder = new MediaRecorderNative() {
            @Override
            public boolean onTouch(MotionEvent e, AutoFocusCallback autoFocusCallback) {
                if (e.getY() > mWindowWidth) {
                    return false;
                }
                mOnSurfaveViewTouchListener.onTouch(mSurfaceView, e);
                autoFocusCallback = new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        mFocusImage.setVisibility(View.GONE);
                    }
                };

                return super.onTouch(e, autoFocusCallback);
            }
        };
        mRebuild = true;

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnEncodeListener(this);

        File f = new File(VCamera.getVideoCachePath());
        if (!FileUtils.checkFile(f)) {
            f.mkdirs();
        }
        String key = String.valueOf(System.currentTimeMillis());
        mMediaObject = mMediaRecorder.setOutputDirectory(key, VCamera.getVideoCachePath() + key);
        mMediaRecorder.setOnSurfaveViewTouchListener(mSurfaceView);
        mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
        mMediaRecorder.prepare();
    }

    private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d("debug", "onTouch ---");
            if (mMediaRecorder == null || !mCreated) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mFocusImage.setVisibility(View.VISIBLE);
                    showFocusImage(event);
                    mMediaRecorder.setAutoFocus();
                    break;
            }
            return true;
        }

    };

    private View.OnTouchListener mOnVideoControllerTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mMediaRecorder == null) {
                return false;
            }

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                        return true;
                    }
                    if (cancelDelete())
                        return true;

                    startRecord();
                    break;

                case MotionEvent.ACTION_UP:
                    if (mPressedStatus) {
                        stopRecord();
                        if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                            mRecordNext.performClick();
                        }
                    }
                    break;
            }
            return true;
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        UtilityAdapter.freeFilterParser();
        UtilityAdapter.initFilterParser();
        mSurfaceView.setVisibility(View.VISIBLE);
        if (mMediaRecorder == null) {
            initMediaRecorder();
            Log.d("debug", "initMediarecorder");
        } else {
            Log.d("debug", "init prepare");
            mRecordLed.setChecked(false);
            mMediaRecorder.prepare();
            mProgressView.setData(mMediaObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecord();
        UtilityAdapter.freeFilterParser();
        if (!mReleased) {
            releaseResource();
        }
        mSurfaceView.setVisibility(View.INVISIBLE);
        mReleased = false;
        Log.d("debug", "onpause");

    }

    private void releaseResource() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
    }

    private void showFocusImage(MotionEvent e) {

        int x = Math.round(e.getX());
        int y = Math.round(e.getY());
        int focusWidth = 100;
        int focusHeight = 100;
        int previewWidth = mSurfaceView.getWidth();
        Rect touchRect = new Rect();

        mMediaRecorder.calculateTapArea(focusWidth, focusHeight, 1f, x, y, previewWidth, previewWidth, touchRect);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFocusImage.getLayoutParams();
        int left = touchRect.left - (mFocusWidth / 2);//(int) x - (focusingImage.getWidth() / 2);
        int top = touchRect.top - (mFocusWidth / 2);//(int) y - (focusingImage.getHeight() / 2);
        if (left < 0.25f * mFocusWidth) {
            left = (int) (0.25f * mFocusWidth);
        }
        if (top < 0.25f * mFocusWidth) {
            top = (int) (0.25f * mFocusWidth);
        }
        if (left + mFocusWidth * 1.25f > mWindowWidth) {
            left = (int) (mWindowWidth - mFocusWidth * 1.25f);
        }
        if (top + mFocusWidth * 1.25f > mWindowWidth) {
            top = (int) (mWindowWidth - mFocusWidth * 1.25f);
        }

        lp.leftMargin = left;
        lp.topMargin = top;

        mFocusImage.setLayoutParams(lp);
        mFocusImage.setVisibility(View.VISIBLE);

        if (mFocusAnimation == null)
            mFocusAnimation = AnimationUtils.loadAnimation(this, R.anim.record_focus);

        mFocusImage.startAnimation(mFocusAnimation);

        mHandler.sendEmptyMessageDelayed(HANDLE_HIDE_RECORD_FOCUS, 3500);
    }

    private void startRecord() {
        if (mMediaRecorder != null) {
            MediaObject.MediaPart part = mMediaRecorder.startRecord();
            if (part == null) {
                return;
            }

            //if use MediaRecorderSystem，should't not switch camera
            if (mMediaRecorder instanceof MediaRecorderSystem) {
                mCameraSwitch.setVisibility(View.GONE);
            }
            mProgressView.setData(mMediaObject);
        }

        mRebuild = true;
        mPressedStatus = true;
        mRecordController.setImageResource(R.drawable.ic_record_pressed);
        mRecordDelete.setVisibility(View.VISIBLE);
        mSelectVideo.setVisibility(View.GONE);

        if (mHandler != null) {
            mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
            mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);

            mHandler.removeMessages(HANDLE_STOP_RECORD);
            mHandler.sendEmptyMessageDelayed(HANDLE_STOP_RECORD, RECORD_TIME_MAX - mMediaObject.getDuration());
        }

        disableView();
    }

    private void disableView() {
        mRecordDelete.setEnabled(false);
        mCameraSwitch.setEnabled(false);
        mRecordLed.setEnabled(false);
        mRecordNext.setEnabled(false);
        mRecordClose.setEnabled(false);
    }

    private void enableView() {
        mRecordDelete.setEnabled(true);
        mCameraSwitch.setEnabled(true);
        mRecordLed.setEnabled(true);
        mRecordNext.setEnabled(true);
        mRecordClose.setEnabled(true);
    }

    @Override
    public void onBackPressed() {

        if (mRecordDelete != null && mRecordDelete.isChecked()) {
            cancelDelete();
            return;
        }

        if (mMediaObject != null && mMediaObject.getDuration() > 1) {

            new AlertDialog.Builder(this).setTitle(R.string.hint).setMessage(R.string.record_camera_exit_dialog_message).setNegativeButton(R.string.record_camera_cancel_dialog_yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMediaObject.delete();
                    releaseResource();
                    finish();
                }

            }).setPositiveButton(R.string.record_camera_cancel_dialog_no, null).setCancelable(false).show();
            return;
        }

        if (mMediaObject != null) {
            mMediaObject.delete();
            releaseResource();
        }
        finish();
    }

    private void stopRecord() {
        mPressedStatus = false;
        mRecordController.setImageResource(R.drawable.ic_record_normal);
        if (mMediaRecorder != null) {
            mMediaRecorder.stopRecord();
        }
        mHandler.removeMessages(HANDLE_STOP_RECORD);

        enableView();
        checkStatus();
    }

    private void startEncoding() {
        VideoProcessEngine videoProcessEngine = VideoProcessEngine.createVideoProcessEngine(mMediaObject.getOutputTempVideoPath(), mSurfaceView.getHolder(), VideoRecordActivity.this);
        // 检测是否需要重新编译
        showProgress("", getString(R.string.record_preview_encoding));
        videoProcessEngine
                .saveVideoToPath(mMediaObject.getOutputVideoPath(), new VideoProcessEngine.OnVideoEncodingListener() {
                    @Override
                    public void onProgressChanged(int i) {

                    }

                    @Override
                    public void onSuccess() {
                        hideProgress();
                        Intent intent = new Intent(VideoRecordActivity.this, VideoProcessActivity.class);
                        Bundle bundle = getIntent().getExtras();
                        if (bundle == null)
                            bundle = new Bundle();
                        bundle.putSerializable(CommonIntentExtra.EXTRA_MEDIA_OBJECT, mMediaObject);
                        bundle.putString("output", mMediaObject.getOutputTempVideoPath());
                        bundle.putBoolean("Rebuild", mRebuild);

                        intent.putExtra("path", mMediaObject.getOutputVideoPath());
                        intent.putExtra("duration", mMediaObject.getDuration());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        mRebuild = false;
                    }

                    @Override
                    public void onFailed(int i) {

                    }
                });

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (mHandler.hasMessages(HANDLE_STOP_RECORD)) {
            mHandler.removeMessages(HANDLE_STOP_RECORD);
        }

        if (id != R.id.record_delete) {
            if (mMediaObject != null) {
                MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                if (part != null) {
                    if (part.remove) {
                        part.remove = false;
                        mRecordDelete.setChecked(false);
                        if (mProgressView != null)
                            mProgressView.invalidate();
                    }
                }
            }
        }

        switch (id) {
            case R.id.close_record:
                onBackPressed();
                break;
            case R.id.record_camera_switcher:
                if (mRecordLed.isChecked()) {
                    if (mMediaRecorder != null) {
                        mMediaRecorder.toggleFlashMode();
                    }
                    mRecordLed.setChecked(false);
                }

                if (mMediaRecorder != null) {
                    mMediaRecorder.switchCamera();
                }

                if (mMediaRecorder.isFrontCamera()) {
                    mRecordLed.setEnabled(false);
                } else {
                    mRecordLed.setEnabled(true);
                }
                break;
            case R.id.record_camera_led:
                if (mMediaRecorder != null) {
                    if (mMediaRecorder.isFrontCamera()) {
                        return;
                    }
                }

                if (mMediaRecorder != null) {
                    mMediaRecorder.toggleFlashMode();
                }
                break;
            case R.id.title_next:// stop record
                mMediaRecorder.startEncoding();
                break;
            case R.id.select_local_video:
                pickLocalVideo();
                break;
            case R.id.record_delete:

                if (mMediaObject != null) {
                    MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                    if (part != null) {
                        if (part.remove) {
                            mRebuild = true;
                            part.remove = false;
                            backRemove();
                            mRecordDelete.setChecked(false);
                        } else {
                            part.remove = true;
                            mRecordDelete.setChecked(true);
                        }
                    }
                    if (mProgressView != null)
                        mProgressView.invalidate();

                    checkStatus();
                }
                break;
        }
    }

    public boolean backRemove() {
        if (mMediaObject != null && mMediaObject.mediaList != null) {
            int size = mMediaObject.mediaList.size();
            if (size > 0) {
                MediaPart part = mMediaObject.mediaList.get(size - 1);
                mMediaObject.removePart(part, true);

                if (mMediaObject.mediaList.size() > 0)
                    mMediaObject.mCurrentPart = mMediaObject.mediaList.get(mMediaObject.mediaList.size() - 1);
                else
                    mMediaObject.mCurrentPart = null;
                return true;
            }
        }
        return false;
    }

    private boolean cancelDelete() {
        if (mMediaObject != null) {
            MediaObject.MediaPart part = mMediaObject.getCurrentPart();
            if (part != null && part.remove) {
                part.remove = false;
                mRecordDelete.setChecked(false);

                if (mProgressView != null)
                    mProgressView.invalidate();

                return true;
            }
        }
        return false;
    }


    private int checkStatus() {
        int duration = 0;
        if (!isFinishing() && mMediaObject != null) {
            duration = mMediaObject.getDuration();
            if (duration < RECORD_TIME_MIN) {
                if (duration == 0) {
                    mCameraSwitch.setVisibility(View.VISIBLE);
                    mRecordDelete.setEnabled(false);
                    mRecordDelete.setVisibility(View.GONE);
                    mSelectVideo.setVisibility(View.VISIBLE);
                }

                if (mRecordNext.isEnabled())
                    mRecordNext.setEnabled(false);
            } else {

                if (!mRecordNext.isEnabled()) {
                    mRecordNext.setEnabled(true);
                }
            }
        }
        return duration;
    }

    private void pickLocalVideo() {
        Intent mediaPicker = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        ActivityInfo activityInfo = mediaPicker.resolveActivityInfo(getPackageManager(), mediaPicker.getFlags());
        if (activityInfo.exported) {
            startActivityForResult(mediaPicker, REQUEST_CODE_FOR_PICK_VIDEO);
        } else {
            Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
            mediaChooser.setType("video/*");
            startActivityForResult(mediaChooser, REQUEST_CODE_FOR_PICK_VIDEO);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_STOP_RECORD:
                    stopRecord();
                    mRecordNext.performClick();
                    break;
                case HANDLE_INVALIDATE_PROGRESS:
                    if (mMediaRecorder != null && !isFinishing()) {
                        if (mProgressView != null)
                            mProgressView.invalidate();
                        if (mPressedStatus)
                            sendEmptyMessageDelayed(0, 30);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FOR_PICK_VIDEO:
                    Intent intent = new Intent(this, VideoProcessActivity.class);
                    String[] proj = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION};
                    @SuppressWarnings("deprecation")
                    Cursor cursor = managedQuery(data.getData(), proj, null, null, null);
                    int videoPath = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    int videoDuration = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    cursor.moveToFirst();
                    intent.putExtra("path", cursor.getString(videoPath));
                    intent.putExtra("duration", cursor.getInt(videoDuration));
                    startActivity(intent);
                    break;
                case REQEUST_CODE_FOR_PROCESS_VIDEO:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onEncodeStart() {
        showProgress("", getString(R.string.record_camera_progress_message));
    }

    @Override
    public void onEncodeProgress(int progress) {
    }

    @Override
    public void onEncodeComplete() {
        hideProgress();
        Intent intent = new Intent(this, VideoProcessActivity.class);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            bundle = new Bundle();
        bundle.putSerializable(CommonIntentExtra.EXTRA_MEDIA_OBJECT, mMediaObject);
        intent.putExtra(OUTPUT_VIDEO_PATH, mOutputVideoPath != null ? mOutputVideoPath
                : mMediaObject.getOutputTempVideoPath().replace(".mp4", "_cut.mp4"));
        intent.putExtra(INPUT_VIDEO_PATH, mMediaObject.getOutputTempVideoPath());
        intent.putExtra(OUTPUT_COVER_PATH, mOutputCoverPath != null ? mOutputVideoPath
                : mMediaObject.getOutputTempVideoPath().replace(".mp4", "_cover.jpeg"));
        intent.putExtra(INPUT_VIDEO_DURATION, mMediaObject.getDuration());

        intent.putExtras(bundle);
        startActivityForResult(intent, REQEUST_CODE_FOR_PROCESS_VIDEO);
        mRebuild = false;
    }

    @Override
    public void onEncodeError() {
        hideProgress();
        Toast.makeText(this, R.string.record_video_transcoding_faild, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVideoError(int what, int extra) {

    }

    @Override
    public void onAudioError(int what, String message) {

    }

    @Override
    public void onPrepared() {

    }

}
