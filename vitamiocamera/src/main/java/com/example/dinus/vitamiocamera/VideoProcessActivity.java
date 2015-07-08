package com.example.dinus.vitamiocamera;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.yixia.weibo.sdk.FFMpegUtils;
import com.yixia.weibo.sdk.util.DeviceUtils;
import com.yixia.weibo.sdk.util.FileUtils;

import java.io.File;


public class VideoProcessActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mProcessBack;
    private TextView mProcessComplete;

    private String mVideoPath;
    private String mOutputVideoPath;
    private String mOutputCoverPath;
    private int mVideoDuration;
    private VideoProcessFragment mProcessFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_process);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVideoPath = getIntent().getStringExtra(VideoRecordActivity.INPUT_VIDEO_PATH);
        mVideoDuration = getIntent().getIntExtra(VideoRecordActivity.INPUT_VIDEO_DURATION, 0);
        mOutputVideoPath = getIntent().getStringExtra(VideoRecordActivity.OUTPUT_VIDEO_PATH);
        mOutputCoverPath = getIntent().getStringExtra(VideoRecordActivity.OUTPUT_COVER_PATH);

        mProcessBack = (ImageView) findViewById(R.id.process_back);
        mProcessComplete = (TextView) findViewById(R.id.process_complete);
        mProcessBack.setOnClickListener(this);
        mProcessComplete.setOnClickListener(this);
        mProcessFragment = VideoProcessFragment.newInstance(mVideoPath, mVideoDuration);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, mProcessFragment)
                .commit();

    }

    private void doProcessVideo() {
        final String startTime = mProcessFragment.startCutTime();
        final String totalCutTime = mProcessFragment.totalCutTime();
        final String coverSelectTime = mProcessFragment.coverSelectTime();

        showProgress("", getString(R.string.record_camera_progress_message));
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                FileUtils.deleteFile(mOutputVideoPath);
                if (mProcessFragment.isNeedCut()) {
//                    String cmd = String.format("ffmpeg -ss %s -t %s -i \"%s\" -vcodec libx264 -acodec libfacc \"%s\"", new Object[]{startTime, totalCutTime, mVideoPath, mOutputVideoPath});
                    String cmd = String.format("ffmpeg -ss %s -t %s -i \"%s\" -vcodec copy -acodec copy \"%s\"", new Object[]{startTime, totalCutTime, mVideoPath, mOutputVideoPath});

                    if (UtilityAdapter.FFmpegRun("", cmd) == 0) {
                        int screenWidth = DeviceUtils.getScreenWidth(VideoProcessActivity.this);
                        return FFMpegUtils.captureThumbnails(mVideoPath, mOutputCoverPath, screenWidth + "x" + screenWidth, coverSelectTime);
                    }
                } else {
                    new File(mVideoPath).renameTo(new File(mOutputVideoPath));
                    int screenWidth = DeviceUtils.getScreenWidth(VideoProcessActivity.this);
                    return FFMpegUtils.captureThumbnails(mOutputVideoPath, mOutputCoverPath, screenWidth + "x" + screenWidth, coverSelectTime);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                hideProgress();
                if (!aBoolean){
                    Toast.makeText(VideoProcessActivity.this, getString(R.string.record_process_failture), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("videoPath", Uri.fromFile(new File(mOutputVideoPath)));
                    intent.putExtra("coverPath", Uri.fromFile(new File(mOutputCoverPath)));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.process_back:
                onBackPressed();
                break;
            case R.id.process_complete:
                doProcessVideo();
                break;
        }
    }
}
