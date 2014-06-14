package com.doomonafireball.masterbuilderfree.android.adapter;

import com.google.gson.internal.Pair;

import com.doomonafireball.masterbuilderfree.android.util.MasterBuilderUtils;
import com.doomonafireball.masterbuilderfree.android.widget.ZoomImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * User: derek Date: 2/21/14 Time: 10:48 PM
 */
public class StepsPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Pair<String, String>> slides;
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;

    public StepsPagerAdapter(Context context, ArrayList<Pair<String, String>> slides, int paddingTop, int paddingRight,
            int paddingBottom) {
        init(context, slides);

        mPaddingTop = paddingTop;
        mPaddingRight = paddingRight;
        mPaddingBottom = paddingBottom;
    }

    private void init(Context context, ArrayList<Pair<String, String>> slides) {
        this.context = context;
        setSlides(slides);
    }

    public void setSlides(ArrayList<Pair<String, String>> slides) {
        this.slides = slides;
        if (this.slides == null) {
            this.slides = new ArrayList<Pair<String, String>>();
        }
    }

    public String getSlide(int position) {
        if (slides == null || position < 0 || position >= slides.size()) {
            return null;
        }
        return slides.get(position).first;
    }

    @Override
    public ZoomImageView instantiateItem(ViewGroup container, int position) {

        String slide = getSlide(position);

        final ZoomImageView img = new ZoomImageView(context);
        container.addView(img);
        img.setTag(position);
        img.setId(position);
        img.setIsCenterInside(true);
        img.setPadding(0, mPaddingTop, mPaddingRight, mPaddingBottom);

        mImageLoader.displayImage(slide, img, MasterBuilderUtils.DEFAULT_LIST_IMAGE_OPTIONS);

        return img;
    }

    @Override
    public int getCount() {
        return slides.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
