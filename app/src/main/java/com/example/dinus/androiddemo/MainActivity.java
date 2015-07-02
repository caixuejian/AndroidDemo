package com.example.dinus.androiddemo;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity {

    private Fragment firstFragemnt;
    private ImageView imageView;
    private CoverSelectView coverView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstFragemnt = new UserFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, firstFragemnt).commit();

        imageView = (ImageView) findViewById(R.id.image);
        TextView textView = (TextView) findViewById(R.id.textView);
        ValueAnimator animator = ObjectAnimator.ofInt(textView, "backgroundColor", Color.RED, Color.BLUE);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(3000).setInterpolator(new LinearInterpolator());
        animator.start();
        imageView.setImageResource(R.mipmap.ic_launcher);
        coverView = (CoverSelectView) findViewById(R.id.coverSelectView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InitActivity.class);
                intent.putExtra("package", getPackageName());
                intent.putExtra("className", getLocalClassName());
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "test.mp4");
                intent.setData(Uri.fromFile(file));
                startActivity(intent);
            }
        });
        try {
            String appver = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_GIDS).versionName;
            Toast.makeText(MainActivity.this, appver, Toast.LENGTH_LONG).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++){
            coverView.addSliceImage(R.mipmap.ic_launcher);
        }

        Toast.makeText(this, formatTime(30123), Toast.LENGTH_LONG).show();
    }

    private String formatTime(long milliSeconds){
        SimpleDateFormat sdf = new SimpleDateFormat("00:mm:ss.SSS");
        return sdf.format(new Date(milliSeconds));
    }

}
