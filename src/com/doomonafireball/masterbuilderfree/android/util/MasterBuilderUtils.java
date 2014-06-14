package com.doomonafireball.masterbuilderfree.android.util;

import com.doomonafireball.masterbuilderfree.android.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * User: derek Date: 11/25/13 Time: 2:36 PM
 */
public class MasterBuilderUtils {

    public static final DisplayImageOptions DEFAULT_LIST_IMAGE_OPTIONS = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.yellow_two_four_small)
            .showImageForEmptyUri(R.drawable.yellow_two_four_small)
            .resetViewBeforeLoading()
            .cacheInMemory()
            .cacheOnDisc()
            .displayer(new FadeInBitmapDisplayer(250))
            .build();

}
