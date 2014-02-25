package com.doomonafireball.masterbuilder.android.activity;

import com.actionbarsherlock.view.MenuItem;
import com.doomonafireball.masterbuilder.android.R;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import roboguice.inject.InjectView;

/**
 * User: derek Date: 2/21/14 Time: 8:41 PM
 */
public class AboutActivity extends RoboSherlockFragmentActivity {

    @InjectView(R.id.parent) View parent;
    @InjectView(R.id.about_text) TextView aboutText;
    @InjectView(R.id.attribution) TextView attribution;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setTitle(R.string.about_master_builder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int defaultPadding = getResources().getDimensionPixelSize(R.dimen.default_padding);
        SystemBarTintManager.SystemBarConfig config = new SystemBarTintManager(this).getConfig();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            parent.setPadding(defaultPadding, defaultPadding + config.getPixelInsetTop(true),
                    defaultPadding + config.getPixelInsetRight(), defaultPadding + config.getPixelInsetBottom());
        } else {
            parent.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
        }
        attribution.setText(Html.fromHtml(getString(R.string.icon_attribution)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
