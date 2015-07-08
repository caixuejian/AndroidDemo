package com.example.dinus.androiddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.dinus.androiddemo.shader.ShaderView;

public class MainActivity extends AppCompatActivity{

    private ShaderView shaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shaderView = (ShaderView) findViewById(R.id.shader_view);

    }

}
