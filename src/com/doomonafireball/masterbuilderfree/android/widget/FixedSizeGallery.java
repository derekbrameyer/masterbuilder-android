package com.doomonafireball.masterbuilderfree.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Gallery;

public class FixedSizeGallery extends Gallery {

    private boolean initializedOnScreen = false;
    private float mCurrX = 0.0f;
    private float mCurrY = 0.0f;
    private int mTouchSlop;

    public FixedSizeGallery(Context context) {
        super(context);
        initialize();
    }

    public FixedSizeGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FixedSizeGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        initializedOnScreen = false;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = (viewConfiguration.getScaledTouchSlop());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        if (!initializedOnScreen) {
        super.onLayout(changed, l, t, r, b);
        initializedOnScreen = true;
//        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_DOWN:
                mCurrX = event.getX();
                mCurrY = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            if (getSelectedItemPosition() == 0 && ((event.getX() - mCurrX) > mTouchSlop)) {
                // User moved finger to the right and is on the leftmost ViewGroup
                getParent().requestDisallowInterceptTouchEvent(false);
            } else if (getSelectedItemPosition() == (getAdapter().getCount() - 1) && ((mCurrX - event.getX())
                    > mTouchSlop)) {
                // User moved finger to the left and is on the rightmost ViewGroup
                getParent().requestDisallowInterceptTouchEvent(false);
            } else if (Math.abs(event.getY() - mCurrY) > Math.abs(event.getX() - mCurrX)) {
                // User scrolled vertically
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return super.onTouchEvent(event);
    }
}