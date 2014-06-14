package com.doomonafireball.masterbuilderfree.android.adapter;

import com.google.gson.internal.Pair;

import com.doomonafireball.masterbuilderfree.android.R;
import com.doomonafireball.masterbuilderfree.android.util.MasterBuilderUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 *
 */
public class ThumbPhotoAdapter extends BaseAdapter {

    private class ViewHolder {

        public ImageView thumbImage;
    }

    private ArrayList<Pair<String, String>> slides;
    private LayoutInflater inflater;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private int viewFullIndex = 0;
    private ViewHolder holder;

    public ThumbPhotoAdapter(Context context, ArrayList<Pair<String, String>> slides) {
        inflater = LayoutInflater.from(context);
        setSlides(slides);
    }

    public void setSlides(ArrayList<Pair<String, String>> slides) {
        this.slides = slides;
        if (this.slides == null) {
            this.slides = new ArrayList<Pair<String, String>>();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = inflater.inflate(R.layout.gallery_thumb_item, parent, false);
            holder = new ViewHolder();
            holder.thumbImage = (ImageView) v.findViewById(R.id.thumb_image);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        imageLoader
                .displayImage(getItem(position), holder.thumbImage, MasterBuilderUtils.DEFAULT_LIST_IMAGE_OPTIONS);

        if (position == viewFullIndex) {
            v.findViewById(R.id.thumb_border).setVisibility(View.VISIBLE);
        }

        return v;
    }


    @Override
    public int getCount() {
        return slides.size();
    }

    @Override
    public String getItem(int position) {
        return slides.get(position).first;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setViewFullIndex(int position) {
        viewFullIndex = position;
    }
}