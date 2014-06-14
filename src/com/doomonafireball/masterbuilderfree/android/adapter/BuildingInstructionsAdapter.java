package com.doomonafireball.masterbuilderfree.android.adapter;

import com.doomonafireball.masterbuilderfree.android.R;
import com.doomonafireball.masterbuilderfree.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilderfree.android.util.MasterBuilderUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 * User: derek Date: 1/21/13 Time: 4:08 PM
 */
public class BuildingInstructionsAdapter extends BaseAdapter {

    public static final int SORT_ALPHABETICAL = 0;
    public static final int SORT_STEPS_ASCENDING = 1;
    public static final int SORT_STEPS_DESCENDING = 2;

    private ArrayList<BuildingInstructions> mBuildingInstructions;
    private Set<String> mCompletedSets;
    private LayoutInflater mInflater;
    private ViewHolder holder;
    private int mSortingAlgorithm = SORT_ALPHABETICAL;
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private String nSteps;

    public BuildingInstructionsAdapter(Context context, ArrayList<BuildingInstructions> buildingInstructions,
            Set<String> completedSets, int sortingAlgorithm) {
        mInflater = LayoutInflater.from(context);
        mBuildingInstructions = buildingInstructions;
        mCompletedSets = completedSets;
        mSortingAlgorithm = sortingAlgorithm;
        nSteps = context.getString(R.string.total_steps);
    }

    private class ViewHolder {

        public ImageView picture;
        public ImageView checkmark;
        public TextView name;
        public TextView description;
        public TextView stepsCount;
    }

    @Override
    public void notifyDataSetChanged() {
        if (mBuildingInstructions != null) {
            sortData();
        }
        super.notifyDataSetChanged();
    }

    public void setSortingAlgorithm(int sortingAlgorithm) {
        mSortingAlgorithm = sortingAlgorithm;
    }

    private void sortData() {
        switch (mSortingAlgorithm) {
            case SORT_ALPHABETICAL:
                Collections.sort(mBuildingInstructions, new Comparator<BuildingInstructions>() {
                    @Override
                    public int compare(BuildingInstructions lhs, BuildingInstructions rhs) {
                        return lhs.name.toLowerCase().compareTo(rhs.name.toLowerCase());
                    }
                });
                break;
            case SORT_STEPS_ASCENDING:
                Collections.sort(mBuildingInstructions, new Comparator<BuildingInstructions>() {
                    @Override
                    public int compare(BuildingInstructions lhs, BuildingInstructions rhs) {
                        return Double.compare(lhs.stepsCount, rhs.stepsCount);
                    }
                });
                break;
            case SORT_STEPS_DESCENDING:
                Collections.sort(mBuildingInstructions, new Comparator<BuildingInstructions>() {
                    @Override
                    public int compare(BuildingInstructions lhs, BuildingInstructions rhs) {
                        return Double.compare(rhs.stepsCount, lhs.stepsCount);
                    }
                });
                break;
        }
    }

    public void setData(ArrayList<BuildingInstructions> buildingInstructions) {
        mBuildingInstructions = buildingInstructions;
    }

    public void setCompletedSets(Set<String> completedSets) {
        mCompletedSets = completedSets;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public BuildingInstructions getItem(int position) {
        return mBuildingInstructions.get(position);
    }

    @Override
    public int getCount() {
        return (mBuildingInstructions == null ? 0 : mBuildingInstructions.size());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.building_instructions_grid_item, null);
            holder = new ViewHolder();
            holder.picture = (ImageView) view.findViewById(R.id.picture);
            holder.checkmark = (ImageView) view.findViewById(R.id.checkmark);
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.description = (TextView) view.findViewById(R.id.description);
            holder.stepsCount = (TextView) view.findViewById(R.id.steps_count);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        BuildingInstructions bi = getItem(position);
        holder.name.setText(bi.description);
        holder.description.setText(bi.name);
        if (bi.stepsCount == null || bi.stepsCount == -1) {
            holder.stepsCount.setVisibility(View.GONE);
        } else {
            holder.stepsCount.setText(String.format(nSteps, Integer.toString(bi.stepsCount)));
            holder.stepsCount.setVisibility(View.VISIBLE);
        }
        if (mCompletedSets.contains(bi.idInstruction)) {
            holder.checkmark.setVisibility(View.VISIBLE);
        } else {
            holder.checkmark.setVisibility(View.GONE);
        }
        mImageLoader.displayImage(bi.shortcutPicture, holder.picture, MasterBuilderUtils.DEFAULT_LIST_IMAGE_OPTIONS);

        return view;
    }
}
