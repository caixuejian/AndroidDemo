package com.example.dinus.androiddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.dinus.androiddemo.shader.ShaderView;

public class MainActivity extends AppCompatActivity{

    private Button hideButton;
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideButton = (Button) findViewById(R.id.hide_fragment);
        container = (FrameLayout) findViewById(R.id.container);

        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (container.getVisibility() == View.VISIBLE){
                    container.setVisibility(View.GONE);
                } else{
                    container.setVisibility(View.VISIBLE);
                }
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.container, new UserFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
     }
}
