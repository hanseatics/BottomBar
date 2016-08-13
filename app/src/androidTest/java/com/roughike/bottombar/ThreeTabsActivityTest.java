package com.roughike.bottombar;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.bottombar.sample.ThreeTabsActivity;
import com.example.bottombar.sample.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

/**
 * Created by iiro on 13.8.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ThreeTabsActivityTest {
    @Rule
    public ActivityTestRule<ThreeTabsActivity> threeTabsActivityRule =
            new ActivityTestRule<>(ThreeTabsActivity.class);

    private OnTabSelectListener selectListener;

    private OnTabReselectListener reselectListener;

    private BottomBar bottomBar;

    @Before
    public void setUp() {
        selectListener = Mockito.mock(OnTabSelectListener.class);
        reselectListener = Mockito.mock(OnTabReselectListener.class);

        bottomBar = (BottomBar) threeTabsActivityRule.getActivity().findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(selectListener);
        bottomBar.setOnTabReselectListener(reselectListener);
    }

    @Test
    @UiThreadTest
    public void whenTabIsSelected_SelectionListenerIsFired() {
        bottomBar.selectTabWithId(R.id.tab_friends);
        bottomBar.selectTabWithId(R.id.tab_nearby);
        bottomBar.selectTabWithId(R.id.tab_favorites);

        InOrder inOrder = inOrder(selectListener);
        inOrder.verify(selectListener).onTabSelected(R.id.tab_friends);
        inOrder.verify(selectListener).onTabSelected(R.id.tab_nearby);
        inOrder.verify(selectListener).onTabSelected(R.id.tab_favorites);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @UiThreadTest
    public void whenTabIsReselected_ReselectionListenerIsFired() {
        int firstTabId = R.id.tab_favorites;
        bottomBar.selectTabWithId(firstTabId);
        verify(reselectListener).onTabReSelected(firstTabId);

        int secondTabId = R.id.tab_nearby;
        bottomBar.selectTabWithId(secondTabId);
        bottomBar.selectTabWithId(secondTabId);
        verify(reselectListener).onTabReSelected(secondTabId);

        int thirdTabId = R.id.tab_friends;
        bottomBar.selectTabWithId(thirdTabId);
        bottomBar.selectTabWithId(thirdTabId);
        verify(reselectListener).onTabReSelected(thirdTabId);
    }

    @Test
    @UiThreadTest
    public void whenDefaultTabIsSet_ItsSelectedAtFirst() {
        int defaultTabId = R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        verify(selectListener).onTabSelected(defaultTabId);
    }

    @Test
    @UiThreadTest
    public void afterConfigurationChanged_UserSelectedTabPersistsWhenResettingDefaultTab() {
        int defaultTabId = R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        bottomBar.selectTabWithId(R.id.tab_nearby);

        Bundle savedState = bottomBar.saveState();
        bottomBar.restoreState(savedState);
        bottomBar.setDefaultTab(defaultTabId);

        assertNotSame(defaultTabId, bottomBar.getCurrentTabId());
        assertEquals(R.id.tab_nearby, bottomBar.getCurrentTabId());
    }
}
