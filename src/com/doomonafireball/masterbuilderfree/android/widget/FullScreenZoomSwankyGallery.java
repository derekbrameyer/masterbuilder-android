package com.doomonafireball.masterbuilderfree.android.widget;

import com.doomonafireball.masterbuilderfree.android.adapter.StepsPagerAdapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import oak.widget.SwankyImageView;

public class FullScreenZoomSwankyGallery extends FrameLayout {

    private Context context;
    private FullScreenZoomSwankyViewPager viewPager;
    private StepsPagerAdapter adapter;
    private GestureDetector doubleTapDetector;

    private float xPosPrev;
    private float maxZoom = 3.5f;

    public OnZoomChangedListener onZoomChangedListener;
    public OnSingleTapListener onSingleTapListener;

    private boolean hasBeenZooming = false;

    public FullScreenZoomSwankyGallery(Context context) {
        super(context);
        init(context, null);
    }

    public FullScreenZoomSwankyGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FullScreenZoomSwankyGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        viewPager = new FullScreenZoomSwankyViewPager(context, attrs);
        viewPager.setId(0);
        addView(viewPager);
        viewPager.setSaveEnabled(true);
        doubleTapDetector = new GestureDetector(context, new DoubleTapListener());
    }

    public void setAdapter(StepsPagerAdapter adapter) {
        viewPager.setAdapter(this.adapter = adapter);
    }

    public FullScreenZoomSwankyViewPager getPager() {
        return viewPager;
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        viewPager.setCurrentItem(item, smoothScroll);
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    public void setOnZoomChangedListener(OnZoomChangedListener listener) {
        onZoomChangedListener = listener;
    }

    public void setOnSingleTapListener(OnSingleTapListener listener) {
        onSingleTapListener = listener;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        super.onRestoreInstanceState(parcelable);
    }


    @Override
    public final boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        ZoomImageView currentView = viewPager.getCurrentView();
        if (doubleTapDetector.onTouchEvent(event)) {
            if (onZoomChangedListener != null) {
                if (currentView.getCurrentScale() == 1) {
                    onZoomChangedListener.onZoomIn();
                } else {
                    onZoomChangedListener.onZoomOut();
                }
            }
            currentView.toggleZoom(event.getX(), event.getY(), maxZoom);
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                hasBeenZooming = false;
                viewPager.onInterceptTouchEvent(event);
                viewPager.onTouchEvent(event);
                currentView.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                currentView.onTouchEvent(event);
                hasBeenZooming = true;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                currentView.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    float prevScale = currentView.getCurrentScale();
                    currentView.onTouchEvent(event);
                    if (onZoomChangedListener != null) {
                        float newScale = currentView.getCurrentScale();
                        if (prevScale == 1 && newScale > prevScale) {
                            onZoomChangedListener.onZoomIn();
                        } else if (newScale == 1 && prevScale > newScale) {
                            onZoomChangedListener.onZoomOut();
                        }
                    }
                } else if (currentView.getCurrentScale() == 1 && !hasBeenZooming) {
                    try {
                        viewPager.onTouchEvent(event);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    currentView.onTouchEvent(event);
                    if (!hasBeenZooming) {
                        if ((xPosPrev < event.getX() && !viewPager.getCurrentView().canScrollLeft()) ||
                                (xPosPrev > event.getX() && !viewPager.getCurrentView().canScrollRight())) {
                            try {
                                viewPager.onTouchEvent(event);
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            default:
                if (!hasBeenZooming) {
                    viewPager.onTouchEvent(event);
                }
                try {
                    currentView.onTouchEvent(event);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
        }
        xPosPrev = event.getX();
        return true;
    }

    /**
     *
     */
    public class FullScreenZoomSwankyViewPager extends ViewPager {

        private int currentItem = 0;

        public FullScreenZoomSwankyViewPager(Context context) {
            super(context);
            setOnPageChangeListener(pageChangeListener);
        }

        public FullScreenZoomSwankyViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
            setOnPageChangeListener(pageChangeListener);
        }

        private final SimpleOnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                View prev = findViewWithTag(currentItem);
                if (prev != null) {
                    if (((SwankyImageView) prev).getCurrentScale() > 1) {
                        ((SwankyImageView) prev).resetScale();
                        if (onZoomChangedListener != null) {
                            onZoomChangedListener.onZoomOut();
                        }
                    }
                }
                currentItem = i;
            }
        };

        public final ZoomImageView getCurrentView() {
            return (ZoomImageView) findViewWithTag(getCurrentItem());
        }
    }

    public interface OnZoomChangedListener {

        public void onZoomIn();

        public void onZoomOut();
    }

    public interface OnSingleTapListener {

        public void onSingleTap();
    }

    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (onSingleTapListener != null) {
                onSingleTapListener.onSingleTap();
            }
            return false;
        }

    }

}
