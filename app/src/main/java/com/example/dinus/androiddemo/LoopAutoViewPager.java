package com.example.dinus.androiddemo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * Created by dinus on 15/6/14.
 */
public class LoopAutoViewPager extends ViewPager {

    public LoopAutoViewPager(Context context) {
        this(context, null);

    }

    public LoopAutoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private static final int SCROLL_WHAT = 1;
    private static final int SCROLL_DELAY = 3 * 1000;

    private Handler mHandler;
    private CustomDurationScroller mScroller;
    private int mScrollDelay = SCROLL_DELAY;
    private static float mAutoScrollFactor = 1.0f;
    private static final boolean DEFAULT_BOUNDARY_CASHING = true;

    private OnPageChangeListener mOuterPageChangeListener;
    private LoopPagerAdapterWrapper mAdapter;
    private boolean mBoundaryCaching = DEFAULT_BOUNDARY_CASHING;


    private static class MyHandler extends Handler {
        private WeakReference<LoopAutoViewPager> viewPagerReference;

        public MyHandler(LoopAutoViewPager viewPager) {
            viewPagerReference = new WeakReference<>(viewPager);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCROLL_WHAT:
                    LoopAutoViewPager viewPager = viewPagerReference.get();
                    Log.d("debug", "is null" + (viewPager == null));
                    if (viewPager != null) {
                        viewPager.mScroller.setScrollDurationFactor(mAutoScrollFactor);
                        viewPager.scrollToNext();
                        viewPager.sendAutoScrollMsg();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void init() {
        mHandler = new MyHandler(this);
        initViewPagerScroller();
        super.setOnPageChangeListener(onPageChangeListener);
    }

    public void initViewPagerScroller() {
        try {
            Field scrollerFiled = ViewPager.class.getDeclaredField("mScroller");
            scrollerFiled.setAccessible(true);
            Field interPortedFiled = ViewPager.class.getDeclaredField("sInterpolator");
            interPortedFiled.setAccessible(true);

            mScroller = new CustomDurationScroller(getContext(), (Interpolator) interPortedFiled.get(null));
            scrollerFiled.set(this, mScroller);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void scrollToNext() {
        PagerAdapter adapter = getAdapter();
        int totalItemCount = 0;
        int currentItem = getCurrentItem();


        if (adapter == null || (totalItemCount = adapter.getCount()) <= 1) {
            return;
        }

        Log.d("debug", totalItemCount + "toatalItemCount" + currentItem);


        int nextItem = ++currentItem;
        setCurrentItem(nextItem);
    }

    public void sendAutoScrollMsg() {
        mHandler.removeMessages(SCROLL_WHAT);
        mHandler.sendEmptyMessageDelayed(SCROLL_WHAT, (int) (mScrollDelay + mScroller.getDuration() * mAutoScrollFactor));
    }

    public void startAutoScroll() {
        sendAutoScrollMsg();
    }

    public void stopAutoScroll() {
        mHandler.removeMessages(SCROLL_WHAT);
    }

    public void setmScrollDelay(int mScrollDelay) {
        this.mScrollDelay = mScrollDelay;
    }

    public void setAutoScrollFactor(float mAutoScrollFactor) {
        this.mAutoScrollFactor = mAutoScrollFactor;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopAutoScroll();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startAutoScroll();

                break;
        }

        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * helper function which may be used when implementing FragmentPagerAdapter
     *
     * @param position
     * @param count
     * @return (position-1)%count
     */
    public static int toRealPosition(int position, int count) {
        position = position - 1;
        if (position < 0) {
            position += count;
        } else {
            position = position % count;
        }
        return position;
    }

    /**
     * If set to true, the boundary views (i.e. first and last) will never be destroyed
     * This may help to prevent "blinking" of some views
     *
     * @param flag
     */
    public void setBoundaryCaching(boolean flag) {
        mBoundaryCaching = flag;
        if (mAdapter != null) {
            mAdapter.setBoundaryCaching(flag);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        mAdapter = new LoopPagerAdapterWrapper(adapter);
        mAdapter.setBoundaryCaching(mBoundaryCaching);
        super.setAdapter(mAdapter);
        setCurrentItem(0, false);
    }

    @Override
    public PagerAdapter getAdapter() {
        return mAdapter != null ? mAdapter.getRealAdapter() : mAdapter;
    }

    @Override
    public int getCurrentItem() {
        return mAdapter != null ? mAdapter.toRealPosition(super.getCurrentItem()) : 0;
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        int realItem = mAdapter.toInnerPosition(item);
        super.setCurrentItem(realItem, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        if (getCurrentItem() != item) {
            setCurrentItem(item, true);
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOuterPageChangeListener = listener;
    }


    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        private float mPreviousOffset = -1;
        private float mPreviousPosition = -1;

        @Override
        public void onPageSelected(int position) {

            int realPosition = mAdapter.toRealPosition(position);
            if (mPreviousPosition != realPosition) {
                mPreviousPosition = realPosition;
                if (mOuterPageChangeListener != null) {
                    mOuterPageChangeListener.onPageSelected(realPosition);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            int realPosition = position;
            if (mAdapter != null) {
                realPosition = mAdapter.toRealPosition(position);

                if (positionOffset == 0
                        && mPreviousOffset == 0
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }

            mPreviousOffset = positionOffset;
            if (mOuterPageChangeListener != null) {
                if (realPosition != mAdapter.getRealCount() - 1) {
                    mOuterPageChangeListener.onPageScrolled(realPosition,
                            positionOffset, positionOffsetPixels);
                } else {
                    if (positionOffset > .5) {
                        mOuterPageChangeListener.onPageScrolled(0, 0, 0);
                    } else {
                        mOuterPageChangeListener.onPageScrolled(realPosition,
                                0, 0);
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mAdapter != null) {
                int position = LoopAutoViewPager.super.getCurrentItem();
                int realPosition = mAdapter.toRealPosition(position);
                if (state == ViewPager.SCROLL_STATE_IDLE
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }
            if (mOuterPageChangeListener != null) {
                mOuterPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    };

}
