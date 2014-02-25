package com.doomonafireball.masterbuilder.android.activity;

import com.google.gson.Gson;
import com.google.gson.internal.Pair;
import com.google.inject.Inject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.doomonafireball.masterbuilder.android.Datastore;
import com.doomonafireball.masterbuilder.android.R;
import com.doomonafireball.masterbuilder.android.adapter.StepsPagerAdapter;
import com.doomonafireball.masterbuilder.android.adapter.ThumbPhotoAdapter;
import com.doomonafireball.masterbuilder.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilder.android.api.model.Step;
import com.doomonafireball.masterbuilder.android.widget.FullScreenZoomSwankyGallery;
import com.doomonafireball.masterbuilder.android.widget.ZoomImageView;
import com.nineoldandroids.animation.ObjectAnimator;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * User: derek Date: 2/21/14 Time: 10:46 PM
 */
public class StepsActivity extends BaseGameActivity implements View.OnClickListener {

    private static final String BUILDING_INSTRUCTIONS_JSON = "StepsActivity_BuildingInstructionsJson";
    private static final String CURRENT_PHOTO_INDEX = "PhotosFragment_CurrentPhotoIndex";

    @InjectView(R.id.full_gallery) private FullScreenZoomSwankyGallery fullGallery;
    @InjectView(R.id.thumb_gallery) private Gallery thumbGallery;
    @InjectView(R.id.content) private RelativeLayout content;
    @InjectView(R.id.description) private TextView description;
    @InjectView(R.id.current_step) private TextView currentStep;
    @InjectView(R.id.view_sections) private ImageView viewSections;

    @InjectResource(R.string.achievement_baby_steps) private String achSet1;
    @InjectResource(R.string.achievement_come_with_me_if_you_want_to_not_die_) private String achSet5;
    @InjectResource(R.string.achievement_moc_me_do_you) private String achSet10;
    @InjectResource(R.string.achievement_to_the_invisible_jet) private String achSet25;
    @InjectResource(R.string.achievement_i_only_work_in_black__and_sometimes_very_very_dark_gray_) private String
            achSet50;
    @InjectResource(
            R.string.achievement_introducing_the_doubledecker_couch_so_everyone_could_watch_tv_together_and_be_buddies)
    private String achSet100;
    @InjectResource(R.string.achievement_suggested_age__thats_just_a_guideline) private String achSet150;
    @InjectResource(R.string.achievement_you_are_the_special) private String achSet200;
    @InjectResource(R.string.achievement_play_well) private String achSteps10;
    @InjectResource(R.string.achievement_highly_swooshable) private String achSteps25;
    @InjectResource(R.string.achievement_superior_greebling_abilities) private String achSteps50;
    @InjectResource(R.string.achievement_the_orb_of_teetleest) private String achSteps75;
    @InjectResource(R.string.achievement_centurion) private String achSteps100;
    @InjectResource(R.string.achievement_thats_an_afol_lot_of_steps_) private String achSteps200;
    @InjectResource(R.string.achievement_spaceship_spaceship_spaceship) private String achSteps400;
    @InjectResource(R.string.achievement_significantly_huge_investment_in_parts) private String achSteps600;
    @InjectResource(R.string.step_n_of_k) private String stepNOfK;

    @Inject Datastore mDatastore;

    private StepsPagerAdapter mStepsPagerAdapter;
    private ThumbPhotoAdapter thumbPhotoAdapter;

    private BuildingInstructions mBuildingInstructions;
    private boolean shouldUpdateThumbPosition = true;
    private boolean hiddenFromTap = false;
    private int currentPosition = 0;
    private ArrayList<Pair<String, String>> mImageUrls;

    private MenuItem mDoneMenuItem;

    private SystemBarTintManager mTintManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setStatusBarAlpha(1.0f);
        mTintManager.setStatusBarTintColor(Color.parseColor("#222222"));
        SystemBarTintManager.SystemBarConfig config = mTintManager.getConfig();
        thumbGallery.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(),
                config.getPixelInsetBottom());
        int defaultPadding = getResources().getDimensionPixelSize(R.dimen.default_padding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            content.setPadding(defaultPadding, defaultPadding, config.getPixelInsetRight() + defaultPadding,
                    config.getPixelInsetBottom() + defaultPadding);
        } else {
            content.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
        }

        Intent myIntent = getIntent();
        mBuildingInstructions = new Gson()
                .fromJson(myIntent.getStringExtra(BUILDING_INSTRUCTIONS_JSON), BuildingInstructions.class);

        getSupportActionBar().setTitle(mBuildingInstructions.description);
        getSupportActionBar().setSubtitle(R.string.building_instructions);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_PHOTO_INDEX)) {
            currentPosition = savedInstanceState.getInt(CURRENT_PHOTO_INDEX, 0);
        } else {
            currentPosition = mDatastore.getCurrentStepForSetId(mBuildingInstructions.idInstruction);
        }

        mImageUrls = new ArrayList<Pair<String, String>>();
        for (Step step : mBuildingInstructions.steps) {
            for (String filename : step.fileNames) {
                mImageUrls.add(new Pair<String, String>(filename, step.name));
            }
        }

        description.setText(mBuildingInstructions.description);

        viewSections.setOnClickListener(this);

        populatePhotos();

        setStepCountText();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_sections:
                // TODO Show ListView dialog with sections
                final String[] groups = new String[mBuildingInstructions.steps.size()];
                final int[] positions = new int[mBuildingInstructions.steps.size()];
                int currentPosition = 0;
                for (int i = 0; i < mBuildingInstructions.steps.size(); i++) {
                    Step s = mBuildingInstructions.steps.get(i);
                    groups[i] = s.name;
                    positions[i] = currentPosition;
                    currentPosition += s.fileNames.size();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.jump_to_group);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setItems(groups, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        fullGallery.setCurrentItem(positions[position], true);
                    }
                });
                builder.show();
                break;
        }
    }

    public static Intent getIntent(Context context, Gson gson, BuildingInstructions buildingInstructions) {
        Intent intent = new Intent(context, StepsActivity.class);
        intent.putExtra(BUILDING_INSTRUCTIONS_JSON, gson.toJson(buildingInstructions));
        return intent;
    }

    private void setStepCountText() {
        int currentItem = fullGallery.getPager().getCurrentItem();
        currentStep.setText(
                String.format(stepNOfK, mImageUrls.get(currentItem).second, getStepCountForGroup(currentItem),
                        getGroupStepCount(currentItem)));
    }

    private int getStepCountForGroup(int totalStepCount) {
        int stepCount = 0;
        for (Step step : mBuildingInstructions.steps) {
            if (totalStepCount < step.fileNames.size()) {
                stepCount = totalStepCount;
                break;
            }
            totalStepCount -= step.fileNames.size();
        }

        return stepCount + 1;
    }

    private int getGroupStepCount(int currentStepCountOfTotal) {
        int stepCount = 0;
        for (Step step : mBuildingInstructions.steps) {
            if (currentStepCountOfTotal < step.fileNames.size()) {
                stepCount = step.fileNames.size();
                break;
            }
            currentStepCountOfTotal -= step.fileNames.size();
            stepCount += step.fileNames.size();
        }

        return stepCount;
    }

    private void populatePhotos() {
        setupFullGallery(mImageUrls);

        setupThumbsGallery(mImageUrls);

        fullGallery.getPager().setCurrentItem(currentPosition);
        thumbPhotoAdapter.setViewFullIndex(currentPosition);
        for (int idx = 0; idx < thumbGallery.getChildCount(); idx++) {
            int vis = (idx + thumbGallery.getFirstVisiblePosition()) == currentPosition
                    ? View.VISIBLE : View.INVISIBLE;
            thumbGallery.getChildAt(idx).findViewById(R.id.thumb_border).setVisibility(vis);
        }
        thumbGallery.setSelection(currentPosition, true);
    }

    private void setupFullGallery(ArrayList<Pair<String, String>> slides) {
        SystemBarTintManager.SystemBarConfig config = mTintManager.getConfig();
        mStepsPagerAdapter = new StepsPagerAdapter(this, slides, config.getPixelInsetTop(false),
                config.getPixelInsetRight(), config.getPixelInsetBottom());
        fullGallery.setAdapter(mStepsPagerAdapter);

        fullGallery.getPager().setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int position) {
                View prev = fullGallery.getPager().findViewWithTag(position);
                if (prev != null) {
                    if (((ZoomImageView) prev).getCurrentScale() > 1) {
                        ((ZoomImageView) prev).resetScale();
                    }
                }
                thumbPhotoAdapter.setViewFullIndex(position);
                for (int idx = 0; idx < thumbGallery.getChildCount(); idx++) {
                    int vis = (idx + thumbGallery.getFirstVisiblePosition()) == position
                            ? View.VISIBLE : View.INVISIBLE;
                    thumbGallery.getChildAt(idx).findViewById(R.id.thumb_border).setVisibility(vis);
                }
                if (shouldUpdateThumbPosition) {
                    thumbGallery.setSelection(position, true);
                } else {
                    shouldUpdateThumbPosition = true;
                }

                setStepCountText();

                if (mDoneMenuItem != null) {
                    if (position >= mStepsPagerAdapter.getCount() - 1) {
                        // Show menu
                        mDoneMenuItem.setVisible(true);
                    } else {
                        // Hide menu
                        mDoneMenuItem.setVisible(false);
                    }
                    supportInvalidateOptionsMenu();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        fullGallery.setOnSingleTapListener(new FullScreenZoomSwankyGallery.OnSingleTapListener() {

            @Override
            public void onSingleTap() {
                if (getSupportActionBar().isShowing()) {
                    getSupportActionBar().hide();
                    hiddenFromTap = true;
                    zoomInHandler();
                } else {
                    getSupportActionBar().show();
                    hiddenFromTap = false;
                    zoomOutHandler();
                }
            }
        });

        fullGallery.setOnZoomChangedListener(new FullScreenZoomSwankyGallery.OnZoomChangedListener() {
            @Override
            public void onZoomIn() {
                if (getSupportActionBar().isShowing()) {
                    getSupportActionBar().hide();
                }
                zoomInHandler();
            }

            @Override
            public void onZoomOut() {
                if (!hiddenFromTap) {
                    if (!getSupportActionBar().isShowing()) {
                        getSupportActionBar().show();
                    }
                    thumbGallery.setVisibility(View.VISIBLE);
                    zoomOutHandler();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.steps_completed_menu, menu);
        mDoneMenuItem = menu.findItem(R.id.done);
        mDoneMenuItem.setVisible(fullGallery.getPager().getCurrentItem() >= (mStepsPagerAdapter.getCount() - 1));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.done:
                mDatastore.persistCompletedSet(mBuildingInstructions.idInstruction);
                if (!getGamesClient().isConnected()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.congratulations_excl);
                    builder.setMessage(R.string.congratulations_with_sign_in_to_gplus);
                    builder.setNegativeButton(R.string.go_me, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                    return true;
                }
                // Post to G+
                if (mBuildingInstructions.stepsCount >= 600) {
                    getGamesClient().unlockAchievement(achSteps600);
                } else if (mBuildingInstructions.stepsCount >= 400) {
                    getGamesClient().unlockAchievement(achSteps400);
                } else if (mBuildingInstructions.stepsCount >= 200) {
                    getGamesClient().unlockAchievement(achSteps200);
                } else if (mBuildingInstructions.stepsCount >= 100) {
                    getGamesClient().unlockAchievement(achSteps100);
                } else if (mBuildingInstructions.stepsCount >= 75) {
                    getGamesClient().unlockAchievement(achSteps75);
                } else if (mBuildingInstructions.stepsCount >= 50) {
                    getGamesClient().unlockAchievement(achSteps50);
                } else if (mBuildingInstructions.stepsCount >= 25) {
                    getGamesClient().unlockAchievement(achSteps25);
                } else {
                    getGamesClient().unlockAchievement(achSteps10);
                }
                getGamesClient().unlockAchievement(achSet1);
                getGamesClient().incrementAchievement(achSet5, 1);
                getGamesClient().incrementAchievement(achSet10, 1);
                getGamesClient().incrementAchievement(achSet25, 1);
                getGamesClient().incrementAchievement(achSet50, 1);
                getGamesClient().incrementAchievement(achSet100, 1);
                getGamesClient().incrementAchievement(achSet150, 1);
                getGamesClient().incrementAchievement(achSet200, 1);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.congratulations_excl);
                builder.setMessage(R.string.congratulations_text);
                builder.setNegativeButton(R.string.go_me, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDatastore.persistCurrentStepForSetId(mBuildingInstructions.idInstruction,
                fullGallery.getPager().getCurrentItem());
    }

    private void zoomInHandler() {
        if (!thumbGallery.isSelected()) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(thumbGallery, "translationY", -thumbGallery.getHeight());
            animator.setDuration(250);
            animator.start();
            thumbGallery.setClickable(false);
            thumbGallery.setSelected(true);
            ObjectAnimator contentAnimator = ObjectAnimator
                    .ofFloat(content, "translationY", content.getHeight());
            contentAnimator.setDuration(250);
            contentAnimator.start();
        }
    }

    private void zoomOutHandler() {
        if (thumbGallery.isSelected()) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(thumbGallery, "translationY", 0.0f);
            animator.setDuration(250);
            animator.start();
            thumbGallery.setClickable(true);
            thumbGallery.setSelected(false);
            ObjectAnimator contentAnimator = ObjectAnimator.ofFloat(content, "translationY", 0.0f);
            contentAnimator.setDuration(250);
            contentAnimator.start();
        }
    }

    private void setupThumbsGallery(ArrayList<Pair<String, String>> slides) {
        thumbPhotoAdapter = new ThumbPhotoAdapter(this, slides);
        thumbGallery.setAdapter(thumbPhotoAdapter);
        thumbPhotoAdapter.setViewFullIndex(0);
        thumbGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                shouldUpdateThumbPosition = false;
                fullGallery.setCurrentItem(position, true);
                thumbPhotoAdapter.setViewFullIndex(position);
                for (int idx = 0; idx < thumbGallery.getChildCount(); idx++) {
                    int vis = (idx + thumbGallery.getFirstVisiblePosition()) == position
                            ? View.VISIBLE : View.INVISIBLE;
                    thumbGallery.getChildAt(idx).findViewById(R.id.thumb_border).setVisibility(vis);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(CURRENT_PHOTO_INDEX, fullGallery.getPager().getCurrentItem());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }
}
