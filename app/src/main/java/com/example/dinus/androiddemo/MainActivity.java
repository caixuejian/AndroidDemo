package com.example.dinus.androiddemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends Activity {

    private Fragment firstFragemnt;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstFragemnt = new UserFragment();
        getFragmentManager().beginTransaction().replace(R.id.container, firstFragemnt).commit();

        imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageResource(R.mipmap.ic_launcher);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("entity", new ParcelEntity("name", 1, 1.2f));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        try {
            String appver = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_GIDS).versionName;
            Toast.makeText(MainActivity.this, appver, Toast.LENGTH_LONG).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        List<PackageInfo> infoList = getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
    }

}
