package com.roughike.bottombar;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 * Created by iiro on 8.8.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BadgeTest {
    private BottomBar bottomBar;
    private BottomBarTab nearby;

    @Before
    public void setUp() {
        bottomBar = new BottomBar(InstrumentationRegistry.getContext());
        bottomBar.setItems(com.roughike.bottombar.test.R.xml.dummy_tabs_three);
        nearby = bottomBar.getTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        nearby.setBadgeCount(5);
    }

    @Test
    public void hasNoBadges_ExceptNearby() {
        assertNull(bottomBar.getTabWithId(com.roughike.bottombar.test.R.id.tab_favorites).badge);
        assertNull(bottomBar.getTabWithId(com.roughike.bottombar.test.R.id.tab_friends).badge);

        assertNotNull(nearby.badge);
    }

    @Test
    @UiThreadTest
    public void whenTabWithBadgeClicked_BadgeIsHidden() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
                assertFalse(nearby.badge.isVisible());
            }
        });
    }

    @Test
    @UiThreadTest
    public void whenBadgeCountIsZero_BadgeIsRemoved() {
        assertNotNull(nearby.badge);

        nearby.setBadgeCount(0);
        assertNull(nearby.badge);
    }

    @Test
    @UiThreadTest
    public void whenBadgeCountIsNegative_BadgeIsRemoved() {
        assertNotNull(nearby.badge);

        nearby.setBadgeCount(-1);
        assertNull(nearby.badge);
    }

    @Test
    @UiThreadTest
    public void whenBadgeStateRestored_CountPersists() {
        assertNotNull(nearby.badge);

        nearby.setBadgeCount(1);
        assertEquals(1, nearby.badge.getCount());


        int tabIndex = nearby.getIndexInTabContainer();
        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putInt(BottomBarBadge.STATE_COUNT + tabIndex, 2);
        nearby.badge.restoreState(savedInstanceState, tabIndex);

        assertEquals(2, nearby.badge.getCount());
    }
}
