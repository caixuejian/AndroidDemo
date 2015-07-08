package com.example.dinus.androiddemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class RulerView extends View {
    private final float DEFAULT_TEXT_SIZE_SP                = 14;
    private final float DEFALUT_SCALE_INTERVAL_HEIGHT_DP    = 10;
    private final float DEFAULT_SCALE_INTERVAL_WIDHT_DP     = 2;
    private final float DEFAULT_RULER_UNIT                  = 1;
    private final float DEFAULT_RULER_MAX_VALUE             = 15;

    private final float BIG_UNIT_VALUE                      = 5;
    private final String DEFAULT_RULDER_UNIT_COLOR          = "#FF000000";

    private float textSize;
    private float scaleIntervalHeight;
    private float scaleIntervalWidht;
    private int rulerUnit;
    private int rulerMaxValue;
    private float height;
    private float width;
    private float paddingLeft;
    private float paddingRight;
    private float paddingTop;
    private float paddingBottom;
    private float textWidth;
    private float textInstrincHeight;

    private Paint smallUnitPaint;
    private Paint bigUnitPaint;
    private Paint linePaint;
    private Paint textPaint;

    public RulerView(Context context) {
        this(context, null);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP, metrics);
        scaleIntervalHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFALUT_SCALE_INTERVAL_HEIGHT_DP, metrics);
        scaleIntervalWidht = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SCALE_INTERVAL_WIDHT_DP, metrics);
        rulerUnit = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_RULER_UNIT, metrics);
        rulerMaxValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_RULER_MAX_VALUE, metrics);

        if (rulerMaxValue % 5 != 0) {
            throw new IllegalArgumentException("rulerMaxValue must be in multiples of five ");
        }
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();

            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
            textSize = ta.getDimensionPixelSize(R.styleable.RulerView_text_size,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP, metrics));
            scaleIntervalHeight = ta.getFloat(R.styleable.RulerView_scale_interval_height,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFALUT_SCALE_INTERVAL_HEIGHT_DP, metrics));
            scaleIntervalWidht = ta.getFloat(R.styleable.RulerView_scale_interval_width,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SCALE_INTERVAL_WIDHT_DP, metrics));
            rulerUnit = ta.getInt(R.styleable.RulerView_ruler_unit,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_RULER_UNIT, metrics));
            rulerMaxValue = ta.getInt(R.styleable.RulerView_ruler_max_value,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEFAULT_RULER_MAX_VALUE, metrics));

            ta.recycle();

            if (rulerMaxValue % 5 != 0) {
                throw new IllegalArgumentException("rulerMaxValue must be in multiples of five ");
            }
        }

        setBackgroundColor(0xff00ff00);
        init();
    }

    private void init() {
        smallUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallUnitPaint.setColor(Color.parseColor(DEFAULT_RULDER_UNIT_COLOR));
        smallUnitPaint.setStrokeWidth(scaleIntervalWidht / 2);

        bigUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigUnitPaint.setColor(Color.parseColor(DEFAULT_RULDER_UNIT_COLOR));
        bigUnitPaint.setStrokeWidth(scaleIntervalWidht);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(scaleIntervalWidht / 2);
        bigUnitPaint.setColor(Color.parseColor(DEFAULT_RULDER_UNIT_COLOR));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.parseColor(DEFAULT_RULDER_UNIT_COLOR));

        textWidth = textPaint.measureText(String.valueOf(rulerMaxValue));
        textInstrincHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = measure(widthMeasureSpec, true);
        height = measure(heightMeasureSpec, false);
        paddingBottom = getPaddingBottom();
        paddingRight = getPaddingRight();
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();

        setMeasuredDimension((int) width, (int) height);
    }

    private float measure(int measureSpec, boolean isWidth) {
        float result;
        float mode = MeasureSpec.getMode(measureSpec);
        float size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            return size;
        }
        float padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        result = isWidth ? getSuggestMinimumWidth() : getSuggestMinimumHeight();
        result -= padding;
        if (mode == MeasureSpec.AT_MOST) {
            if (isWidth) {
                result = Math.max(result, size);
            } else {
                result = Math.min(result, size);
            }
        }

        return result;
    }

    private float getSuggestMinimumWidth() {
        return scaleIntervalWidht * rulerMaxValue;
    }

    private float getSuggestMinimumHeight() {
        return textSize + scaleIntervalHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRulerLine(canvas);

        float unitCount = rulerMaxValue / rulerUnit;
        float margin = (width - paddingLeft - paddingRight - textWidth) / unitCount;
        for (int i = 0; i <= unitCount; i++) {
            drawUnit(canvas, margin * i, i % BIG_UNIT_VALUE != 0);
            if (i % BIG_UNIT_VALUE == 0) {
                drawText(canvas, formatUnit(i), margin * i, textSize, textPaint);
            }
        }
    }

    private void drawText(Canvas canvas, String content, float x, float textSize, Paint textPaint) {
        canvas.drawText(content, x + paddingLeft, textSize + paddingTop - textInstrincHeight, textPaint);
    }

    private void drawUnit(Canvas canvas, float x, boolean isSmallUnit) {
        canvas.drawLine(paddingLeft + x + textWidth / 2,
                isSmallUnit ? height - scaleIntervalHeight / 2 - paddingBottom : height - scaleIntervalHeight - paddingBottom,
                paddingLeft + x + textWidth / 2, height - paddingBottom, isSmallUnit ? smallUnitPaint : bigUnitPaint);
    }

    private void drawRulerLine(Canvas canvas) {
        canvas.drawLine(paddingLeft + textWidth / 2, height - paddingBottom, width - paddingRight - textWidth / 2, height - paddingBottom, linePaint);
    }

    private String formatUnit(int scaleInterval) {
        int maxUnitlen = String.valueOf(rulerMaxValue).length();
        int currentUnitLen = String.valueOf(scaleInterval).length();
        String result = "";
        for (int i = currentUnitLen; i < maxUnitlen; i++) {
            result += "0";
        }

        return result.concat(String.valueOf(scaleInterval));
    }
}
