package com.example.dinus.androiddemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShrinkView extends RelativeLayout implements View.OnClickListener {

    private final static int DEFAULT_SUMMARY_SIZE = 100;
    private final static int MARGIN_BOTTOM_DP = 10;
    private final static int MIN_TEXT_LINE = 6;

    private final static String EXPAND_MORE = "更多";
    private final static String SHRINK_MORE = "收起";

    private TextView mTextContent;
    private TextView mTextMore;
    private String mContent;
    private int mSummarySize;
    private float mMarginBottom;

    private boolean isShowMore = false;
    private OnShrinkChangeLinstener mListener;

    public ShrinkView(Context context) {
        this(context, null);
    }

    public ShrinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public interface OnShrinkChangeLinstener {
         void onShrinkChange(boolean isShrink);
    }

    public void setOnShrinkChangeListener(OnShrinkChangeLinstener listener){
        mListener = listener;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_shink_layout, this, true);
        setBackgroundColor(0xfff3f3f4);

        mTextContent = (TextView) findViewById(R.id.text_content);
        mTextMore = (TextView) findViewById(R.id.text_more);

        mTextMore.setOnClickListener(this);

        mSummarySize = DEFAULT_SUMMARY_SIZE;
        mMarginBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_BOTTOM_DP, getResources().getDisplayMetrics());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_more:
                isShowMore = !isShowMore;
                if (mListener != null){
                    mListener.onShrinkChange(isShowMore);
                }

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                if (isShowMore) {
                    layoutParams.addRule(RelativeLayout.BELOW, R.id.text_content);
                    mTextContent.setMaxLines(Integer.MAX_VALUE);
                    layoutParams.bottomMargin = (int) mMarginBottom;
                    mTextMore.setText(SHRINK_MORE);
                } else {
                    layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.text_content);
                    mTextContent.setMaxLines(MIN_TEXT_LINE);
                    mTextMore.setText(EXPAND_MORE);
                }
                mTextMore.setLayoutParams(layoutParams);

                break;
            default:
                break;
        }
    }


    public void setText(String textContent) {
        mContent = textContent;
        mTextContent.setText(mContent);
        mTextContent.setMaxLines(MIN_TEXT_LINE);
    }
}
