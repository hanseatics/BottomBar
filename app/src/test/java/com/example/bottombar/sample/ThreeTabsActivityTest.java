package com.example.bottombar.sample;

import android.support.annotation.IdRes;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

/**
 * NOTE: I'm a testing noob. If you're facepalming when looking at these, please, feel free to
 * send me PR's with less horrible code!
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ThreeTabsActivityTest {
    private ThreeTabsActivity activity;
    private int currentMenuId;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(ThreeTabsActivity.class)
                .create()
                .resume()
                .get();
    }

    @Test
    public void defaulTabPositionTest() {
        BottomBar bottombar = BottomBar.attach(activity, null);
        bottombar.setDefaultTabPosition(1);
        setItems(bottombar);
        setListener(bottombar);

        assertEquals(bottombar.getCurrentTabPosition(), 1);
        assertEquals(currentMenuId, R.id.bb_menu_nearby);

        BottomBar secondBottombar = getBar();
        secondBottombar.setDefaultTabPosition(1);

        assertEquals(secondBottombar.getCurrentTabPosition(), 1);
        assertEquals(currentMenuId, R.id.bb_menu_nearby);
    }

    @Test
    public void menuItemSelectionTest() {
        BottomBar bottombar = getBar();

        bottombar.selectTabAtPosition(0, false);
        assertEquals(bottombar.getCurrentTabPosition(), 0);
        assertEquals(currentMenuId, R.id.bb_menu_favorites);

        bottombar.selectTabAtPosition(2, false);
        assertEquals(bottombar.getCurrentTabPosition(), 2);
        assertEquals(currentMenuId, R.id.bb_menu_friends);

        bottombar.selectTabAtPosition(1, false);
        assertEquals(bottombar.getCurrentTabPosition(), 1);
        assertEquals(currentMenuId, R.id.bb_menu_nearby);
    }

    private BottomBar getBar() {
        BottomBar bottombar = BottomBar.attach(activity, null);

        setItems(bottombar);
        setListener(bottombar);

        return bottombar;
    }

    private void setItems(BottomBar bottombar) {
        bottombar.setItems(R.menu.bottombar_menu_three_items);
    }

    private void setListener(BottomBar bottombar) {
        bottombar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                currentMenuId = menuItemId;
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

            }
        });
    }
}