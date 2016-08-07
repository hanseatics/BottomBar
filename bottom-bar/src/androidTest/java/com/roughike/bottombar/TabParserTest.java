package com.roughike.bottombar;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.test.InstrumentationTestCase;

import java.util.List;

public class TabParserTest extends InstrumentationTestCase {
    private Context context;
    private List<BottomBarTab> tabs;

    @Override
    public void setUp() throws Exception {
        context = getInstrumentation().getContext();
        tabs = new TabParser(context, new TabParser.Config.Builder().build(), com.roughike.bottombar.test.R.xml.dummy_tab_xml)
                .getTabs();
    }

    public void testCorrectAmountOfTabs() {
        assertEquals(5, tabs.size());
    }

    public void testIdsNotEmpty() {
        assertNotSame(0, tabs.get(0).getId());
        assertNotSame(0, tabs.get(1).getId());
        assertNotSame(0, tabs.get(2).getId());
        assertNotSame(0, tabs.get(3).getId());
        assertNotSame(0, tabs.get(4).getId());
    }

    public void testCorrectTabTitles() {
        assertEquals("Recents", tabs.get(0).getTitle());
        assertEquals("Favorites", tabs.get(1).getTitle());
        assertEquals("Nearby", tabs.get(2).getTitle());
        assertEquals("Friends", tabs.get(3).getTitle());
        assertEquals("Food", tabs.get(4).getTitle());
    }

    public void testCorrectActiveColors() {
        assertEquals(Color.parseColor("#FF0000"), tabs.get(0).getActiveColor());

        assertEquals(
                ContextCompat.getColor(context, com.roughike.bottombar.test.R.color.test_random_color),
                tabs.get(1).getActiveColor()
        );

        assertEquals(Color.parseColor("#0000FF"), tabs.get(2).getActiveColor());
        assertEquals(Color.parseColor("#DAD666"), tabs.get(3).getActiveColor());
        assertEquals(Color.parseColor("#F00F00"), tabs.get(4).getActiveColor());
    }

    public void testIconResourcesExist() {
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(0).getIconResId()));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(1).getIconResId()));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(2).getIconResId()));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(3).getIconResId()));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(4).getIconResId()));
    }
}