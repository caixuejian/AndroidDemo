package com.example.dinus.vitamiocamera;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dinus.vitamiocamera.common.FFmpegUtils;
import com.example.dinus.vitamiocamera.common.FileUtils;
import com.example.dinus.vitamiocamera.views.CoverSelectView;
import com.example.dinus.vitamiocamera.views.CutProgressView;
import com.example.dinus.vitamiocamera.views.SurfaceVideoView;
import com.yixia.weibo.sdk.util.DeviceUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;


public class VideoProcessFragment extends Fragment implements SurfaceVideoView.OnPlayStateListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener,
        SurfaceVideoView.OnPlayProgressListener, CutProgressView.OnValueChangedListener {

    public static final String ARGUMENT_PATH = VideoProcessFragment.class.getCanonicalName() + "path";
    public static final String ARGUMENT_DURATION = VideoProcessFragment.class.getCanonicalName() + "duration";
    public static final int THUMNAIL_COUNT = 10;
    public static final float MILLI_SECOND = 1000;
    public static final float MAX_RECORD_TIME_MILLISECOND = 15000;

    private SurfaceVideoView mVideoView;
    private ImageView mPreviewCover;
    private CutProgressView mCutProgressView;
    private CoverSelectView mCoverSelectView;
    private TextView videoCutTab;
    private TextView coverSelectTab;
    private TextView stricketAddTab;
    private View mPlayerStatus;
    private View rootView;

    private String mPath;
    private Uri mCurrentUri;
    private float mVideoDuration;
    private boolean mNeedResume;
    private float mIntervalDuration;
    private int mCutIntervalThumnailCount;
    private int hasCreateThumnailCount;
    private int mCurrentThumnailIndex;
    private AsyncTask<Void, Uri, Void> readVideoInfoTask;

    private List<Integer> tabIdList = Arrays.asList(new Integer[]{R.id.cut_tab, R.id.sticket_tab, R.id.cover_tab});
    private List<Integer> tabOperateList = Arrays.asList(new Integer[]{R.id.cut_layout, R.id.sticket_layout, R.id.cover_layout});

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPath = getArguments().getString(ARGUMENT_PATH);
            mVideoDuration = getArguments().getInt(ARGUMENT_DURATION);
        }
    }

    public static VideoProcessFragment newInstance(String path, int duration) {
        VideoProcessFragment fragment = new VideoProcessFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_PATH, path);
        bundle.putInt(ARGUMENT_DURATION, duration);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_process, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        mVideoView = (SurfaceVideoView) findViewById(R.id.videoview);
        mPreviewCover = (ImageView) findViewById(R.id.preview_cover);
        mPlayerStatus = findViewById(R.id.play_status);
        mCutProgressView = (CutProgressView) findViewById(R.id.cut_progress);
        mCoverSelectView = (CoverSelectView) view.findViewById(R.id.cover_select);
        videoCutTab = (TextView) findViewById(R.id.cut_tab);
        coverSelectTab = (TextView) findViewById(R.id.cover_tab);
        stricketAddTab = (TextView) findViewById(R.id.sticket_tab);

        videoCutTab.setSelected(true);
        videoCutTab.setTextColor(getResources().getColor(R.color.media_tab_selected_textcolor));

        videoCutTab.setOnClickListener(this);
        coverSelectTab.setOnClickListener(this);
        stricketAddTab.setOnClickListener(this);

        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnPlayStateListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnClickListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPlayProgressListener(this);

        mPreviewCover.getLayoutParams().height = DeviceUtils.getScreenWidth(getActivity());
        mVideoView.getLayoutParams().height = DeviceUtils.getScreenWidth(getActivity());
        mVideoView.setVideoPath(mPath);
        mIntervalDuration = mVideoDuration / THUMNAIL_COUNT;
        int durationThumnail = (int) Math.ceil(DeviceUtils.getScreenWidth(getActivity()) / mCutProgressView.getThumnailHeight());
        mCutIntervalThumnailCount = (int) Math.floor(THUMNAIL_COUNT / (mVideoDuration / (MAX_RECORD_TIME_MILLISECOND / durationThumnail)));

        mCutProgressView.setVideoDuration(mVideoDuration / MILLI_SECOND);
        mCutProgressView.setOnValueChangedListener(this);
        mCutProgressView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                float cutProgressWidth = mCutProgressView.getWidth() - mCutProgressView.getBorderWidth() * 2;
                cutProgressWidth = mVideoDuration / MAX_RECORD_TIME_MILLISECOND * cutProgressWidth
                        + mCutProgressView.getBorderWidth() * 2;
                ViewGroup.LayoutParams layoutParams = mCutProgressView.getLayoutParams();
                layoutParams.width = (int) cutProgressWidth;
                mCutProgressView.setLayoutParams(layoutParams);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mCutProgressView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mCutProgressView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        mCoverSelectView.setOnCoverSelectListener(new CoverSelectView.OnCoverSelectListener() {
            @Override
            public void onCoverChangedListener(int index, Uri uri) {
                mCurrentThumnailIndex = index;
//                can't locate accurate
//                mVideoView.seekTo(index / 10.0f);
                mCurrentUri = uri;
                mPreviewCover.setImageURI(uri);
            }
        });

        readVideoInfoTask = new ReadVideoInfoTask();
        readVideoInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoView != null && mNeedResume) {
            mNeedResume = false;
            if (mVideoView.isRelease())
                mVideoView.reOpen();
            else
                mVideoView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                mNeedResume = true;
                mVideoView.pause();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        readVideoInfoTask.cancel(true);
    }

    @Override
    public void onDestroy() {
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mVideoView.setVolume(SurfaceVideoView.getSystemVolumn(getActivity()));
        mVideoView.start();
        mVideoView.seekTo((int) (mCutProgressView.startProgress() * mVideoDuration));
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        mPlayerStatus.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videoview:
                if (videoCutTab.isSelected()) {
                    if (mVideoView.isPlaying()) {
                        mVideoView.pause();
                    } else {
                        mVideoView.start();
                    }
                }
                break;
            case R.id.cut_tab:
                if (videoCutTab.isSelected()) {
                    return;
                }
                resetTab(R.id.cut_tab);

                break;
            case R.id.cover_tab:
                if (coverSelectTab.isSelected()) {
                    return;
                }
                resetTab(R.id.cover_tab);

                break;
            case R.id.sticket_tab:
                if (stricketAddTab.isSelected()) {
                    return;
                }
                resetTab(R.id.sticket_tab);

                break;
            default:

                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mVideoView.reOpen();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                //音频和视频数据不正确
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (!getActivity().isFinishing())
                    mVideoView.pause();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (!getActivity().isFinishing())
                    mVideoView.start();
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                if (DeviceUtils.hasJellyBean()) {
                    mVideoView.setBackground(null);
                } else {
                    mVideoView.setBackgroundDrawable(null);
                }
                break;
        }
        return false;
    }

    @Override
    public void onProgressRate(final float progressRate) {
        if (mCutProgressView != null) {
            mCutProgressView.post(new Runnable() {
                @Override
                public void run() {
                    mCutProgressView.setProgress(progressRate);
                    if (progressRate >= mCutProgressView.endProgress()) {
                        mVideoView.seekTo((int) (mCutProgressView.startProgress() * mVideoDuration));
                    }
                }
            });
        }
    }

    @Override
    public void onChangePreviewProgress(float previewProgress) {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }

        mVideoView.seekTo((int) (previewProgress * mVideoDuration));
    }

    @Override
    public void onCompleteProgress() {
        mVideoView.seekTo((int) (mCutProgressView.startProgress() * mVideoDuration));
        mVideoView.start();
    }

    @Override
    public void onChangeLeftProgress() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    public void onChangeRightProgress() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    private class ReadVideoInfoTask extends AsyncTask<Void, Uri, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            hasCreateThumnailCount = 0;
            String framesh = (int) mCoverSelectView.getThumnailHeight() + "x" + (int) mCoverSelectView.getThumnailHeight();
            for (int i = 0; i < THUMNAIL_COUNT; i++) {
                String thumbnailPath = FileUtils.INSTANCE.captureThumnailPath(mPath, String.valueOf(i));
                File file = new File(getActivity().getCacheDir(), thumbnailPath);
                try {
                    if (!file.exists()) {
                        if (FFmpegUtils.INSTANCE.captureThumbnails(mPath, file.getCanonicalPath(),
                                framesh, formatTime((long) (i * mIntervalDuration)))) {
                            publishProgress(Uri.fromFile(file));
                        }
                    } else {
                        publishProgress(Uri.fromFile(file));
                    }
                } catch (Exception e) {

                }

            }
            return null;
        }

        @Override
        protected synchronized void onProgressUpdate(Uri... values) {
            super.onProgressUpdate(values);
            if (hasCreateThumnailCount % mCutIntervalThumnailCount == 0) {
                mCutProgressView.addSliceImage(values[0]);
            }
            mCoverSelectView.addSliceImage(values[0]);
            hasCreateThumnailCount++;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private View findViewById(int resId) {
        return rootView.findViewById(resId);
    }

    private void resetTab(int selectedTabID) {
        for (int i = 0; i < tabIdList.size(); i++) {
            TextView selectedTab = (TextView) findViewById(tabIdList.get(i));
            LinearLayout selectOperateLayout = (LinearLayout) findViewById(tabOperateList.get(i));
            if (selectedTabID == tabIdList.get(i)) {
                selectedTab.setSelected(true);
                selectOperateLayout.setVisibility(View.VISIBLE);
                selectedTab.setTextColor(getResources().getColor(R.color.media_tab_selected_textcolor));
            } else {
                selectedTab.setSelected(false);
                selectOperateLayout.setVisibility(View.GONE);
                selectedTab.setTextColor(getResources().getColor(R.color.media_tab_unselected_textcolor));
            }

        }

        if (selectedTabID == R.id.cut_tab) {
            if (!mVideoView.isPlaying()) {
                mVideoView.seekTo((int) (mCutProgressView.startProgress() * mVideoDuration));
                mVideoView.start();
                mPreviewCover.setVisibility(View.GONE);
            }
        } else if (selectedTabID == R.id.cover_tab) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
//            mVideoView.seekTo((int) (mCurrentThumnailIndex * mIntervalDuration));
            mPreviewCover.setImageURI(mCurrentUri);
            mPlayerStatus.setVisibility(View.GONE);
            mPreviewCover.setVisibility(View.VISIBLE);
        } else {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }
//            mVideoView.seekTo((int) (mCurrentThumnailIndex * mIntervalDuration));
            mPreviewCover.setImageURI(mCurrentUri);
            mPlayerStatus.setVisibility(View.VISIBLE);
            mPreviewCover.setVisibility(View.VISIBLE);
        }
    }

    private String formatTime(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("00:mm:ss.SSS");
        return sdf.format(milliseconds);
    }

    //callback method for process video
    public String startCutTime(){
        return formatTime((long) (mCutProgressView.startProgress() * mVideoDuration));
    }

    public String totalCutTime(){
        return formatTime((long) ((mCutProgressView.endProgress() - mCutProgressView.startProgress()) * mVideoDuration));
    }

    public String coverSelectTime(){
        return formatTime((long) (mCurrentThumnailIndex * mIntervalDuration));
    }

    public boolean isNeedCut(){
        return mCutProgressView.endProgress() != 1.0f || mCutProgressView.startProgress() != 0.0f;
    }
}