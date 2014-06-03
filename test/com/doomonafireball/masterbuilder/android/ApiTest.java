package com.doomonafireball.masterbuilder.android;

import com.google.inject.Injector;

import com.doomonafireball.masterbuilder.android.api.CubiculusApi;
import com.doomonafireball.masterbuilder.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilder.android.api.response.BuildingInstructionResponse;
import com.doomonafireball.masterbuilder.android.api.response.BuildingInstructionsResponse;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import roboguice.RoboGuice;

/**
 * User: derek Date: 11/22/13 Time: 2:25 PM
 */
@RunWith(RobolectricTestRunner.class)
public class ApiTest {

    CubiculusApi cubiculusApi;

    @Before
    public void setup() {
        Injector i = RoboGuice.getBaseApplicationInjector(Robolectric.application);
        cubiculusApi = i.getInstance(CubiculusApi.class);
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
    }

    @Test
    public void getBuildingInstructions() {
        try {
            BuildingInstructionsResponse response = cubiculusApi
                    .getBuildingInstructions(Robolectric.application.getString(R.string.api_key), "Unnamed Set",
                            "Group %1$s");
            Assert.assertNotNull(response.buildingInstructions);

            if (response.buildingInstructions.size() > 0) {
                BuildingInstructions bi = response.buildingInstructions.get(0);
                BuildingInstructionResponse individualResponse = cubiculusApi.getBuildingInstructions(
                        Robolectric.application.getString(R.string.api_key), bi.idInstruction, "Unnamed Set",
                        "Group %1$s");
                Assert.assertNotNull(individualResponse.buildingInstructions);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }
}
