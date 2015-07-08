package com.example.dinus.androiddemo.shader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.example.dinus.androiddemo.R;

public class ShaderView extends View{

    private Paint paint;
    public ShaderView(Context context) {
        this(context, null);
    }

    public ShaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.GRAY);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shader_test);
        int x = 200;
        int y = 200;
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();

        canvas.drawBitmap(bitmap, x, y, paint);
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1);
        Bitmap mirrorBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);

        canvas.drawBitmap(mirrorBitmap, x, y + bitmapHeight, paint);
    }
}
