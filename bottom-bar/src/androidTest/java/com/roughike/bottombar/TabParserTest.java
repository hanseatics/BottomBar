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
        tabs = new TabParser(context, com.roughike.bottombar.test.R.xml.dummy_tab_xml)
                .getTabs();
    }

    public void testCorrectAmountOfTabs() {
        assertEquals(5, tabs.size());
    }

    public void testIdsNotEmpty() {
        assertNotSame(0, tabs.get(0).id);
        assertNotSame(0, tabs.get(1).id);
        assertNotSame(0, tabs.get(2).id);
        assertNotSame(0, tabs.get(3).id);
        assertNotSame(0, tabs.get(4).id);
    }

    public void testCorrectTabTitles() {
        assertEquals("Recents", tabs.get(0).title);
        assertEquals("Favorites", tabs.get(1).title);
        assertEquals("Nearby", tabs.get(2).title);
        assertEquals("Friends", tabs.get(3).title);
        assertEquals("Food", tabs.get(4).title);
    }

    public void testCorrectColors() {
        assertEquals(Color.parseColor("#FF0000"), tabs.get(0).color);

        assertEquals(
                ContextCompat.getColor(context, com.roughike.bottombar.test.R.color.test_random_color),
                tabs.get(1).color
        );

        assertEquals(Color.parseColor("#0000FF"), tabs.get(2).color);
        assertEquals(Color.parseColor("#DAD666"), tabs.get(3).color);
        assertEquals(Color.parseColor("#F00F00"), tabs.get(4).color);
    }

    public void testIconResourcesExist() {
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(0).iconResId));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(1).iconResId));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(2).iconResId));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(3).iconResId));
        assertNotNull(ContextCompat.getDrawable(context, tabs.get(4).iconResId));
    }
}