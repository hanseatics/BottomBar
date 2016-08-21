package com.roughike.bottombar;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by iiro on 13.8.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ThreeFixedTabsTest {
    private OnTabSelectListener selectListener;
    private OnTabReselectListener reselectListener;

    private BottomBar bottomBar;

    @Before
    public void setUp() {
        selectListener = Mockito.mock(OnTabSelectListener.class);
        reselectListener = Mockito.mock(OnTabReselectListener.class);

        bottomBar = new BottomBar(InstrumentationRegistry.getContext());
        bottomBar.setItems(com.roughike.bottombar.test.R.xml.dummy_tabs_three);
        bottomBar.setOnTabSelectListener(selectListener);
        bottomBar.setOnTabReselectListener(reselectListener);
    }

    @Test
    @UiThreadTest
    public void tabCount_IsCorrect() {
        assertEquals(3, bottomBar.getTabCount());
    }

    @Test
    @UiThreadTest
    public void findingPositionForTabs_ReturnsCorrectPositions() {
        assertEquals(0, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_favorites));
        assertEquals(1, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_nearby));
        assertEquals(2, bottomBar.findPositionForTabWithId(com.roughike.bottombar.test.R.id.tab_friends));
    }

    @Test
    @UiThreadTest
    public void whenTabIsSelected_SelectionListenerIsFired() {
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        InOrder inOrder = inOrder(selectListener);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_friends);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_nearby);
        inOrder.verify(selectListener, times(1)).onTabSelected(com.roughike.bottombar.test.R.id.tab_favorites);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @UiThreadTest
    public void afterConfigurationChanged_SavedStateRestored_AndSelectedTabPersists() {
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        Bundle savedState = bottomBar.saveState();
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.restoreState(savedState);

        assertEquals(com.roughike.bottombar.test.R.id.tab_favorites, bottomBar.getCurrentTabId());
    }

    @Test
    @UiThreadTest
    public void whenTabIsReselected_ReselectionListenerIsFired() {
        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        bottomBar.selectTabWithId(firstTabId);
        verify(reselectListener, times(1)).onTabReSelected(firstTabId);

        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        bottomBar.selectTabWithId(secondTabId);
        bottomBar.selectTabWithId(secondTabId);
        verify(reselectListener, times(1)).onTabReSelected(secondTabId);

        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;
        bottomBar.selectTabWithId(thirdTabId);
        bottomBar.selectTabWithId(thirdTabId);
        verify(reselectListener, times(1)).onTabReSelected(thirdTabId);
    }

    @Test
    @UiThreadTest
    public void whenDefaultTabIsSet_ItsSelectedAtFirst() {
        int defaultTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        verify(selectListener).onTabSelected(defaultTabId);
    }

    @Test
    @UiThreadTest
    public void afterConfigurationChanged_UserSelectedTabPersistsWhenResettingDefaultTab() {
        int defaultTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.setDefaultTab(defaultTabId);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);

        Bundle savedState = bottomBar.saveState();
        bottomBar.restoreState(savedState);
        bottomBar.setDefaultTab(defaultTabId);

        assertNotSame(defaultTabId, bottomBar.getCurrentTabId());
        assertEquals(com.roughike.bottombar.test.R.id.tab_nearby, bottomBar.getCurrentTabId());
    }

    @Test
    @UiThreadTest
    public void whenGettingCurrentTab_ReturnsCorrectOne() {
        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        bottomBar.selectTabWithId(firstTabId);

        assertEquals(firstTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(firstTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(firstTabId), bottomBar.getCurrentTab());

        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        bottomBar.selectTabWithId(secondTabId);

        assertEquals(secondTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(secondTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(secondTabId), bottomBar.getCurrentTab());

        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;
        bottomBar.selectTabWithId(thirdTabId);

        assertEquals(thirdTabId, bottomBar.getCurrentTabId());
        assertEquals(bottomBar.findPositionForTabWithId(thirdTabId), bottomBar.getCurrentTabPosition());
        assertEquals(bottomBar.getTabWithId(thirdTabId), bottomBar.getCurrentTab());
    }

    @Test
    @UiThreadTest
    public void whenSelectionChanges_AndHasNoListeners_onlyOneTabIsSelectedAtATime() {
        bottomBar.setOnTabSelectListener(null);
        bottomBar.setOnTabReselectListener(null);

        int firstTabId = com.roughike.bottombar.test.R.id.tab_favorites;
        int secondTabId = com.roughike.bottombar.test.R.id.tab_nearby;
        int thirdTabId = com.roughike.bottombar.test.R.id.tab_friends;

        bottomBar.selectTabWithId(secondTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(secondTabId);

        bottomBar.selectTabWithId(thirdTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(thirdTabId);

        bottomBar.selectTabWithId(firstTabId);
        assertOnlyHasOnlyOneSelectedTabWithId(firstTabId);
    }

    private void assertOnlyHasOnlyOneSelectedTabWithId(int tabId) {
        for (int i = 0; i < bottomBar.getTabCount(); i++) {
            BottomBarTab tab = bottomBar.getTabAtPosition(i);

            if (tab.getId() == tabId) {
                assertTrue(tab.isActive());
            } else {
                assertFalse(tab.isActive());
            }
        }
    }

    @Test
    @UiThreadTest
    public void whenTabIsSelectedOnce_AndNoSelectionListenerSet_ReselectionListenerIsNotFired() {
        bottomBar.setOnTabSelectListener(null);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_friends);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_nearby);
        bottomBar.selectTabWithId(com.roughike.bottombar.test.R.id.tab_favorites);

        verifyZeroInteractions(reselectListener);
    }
}
