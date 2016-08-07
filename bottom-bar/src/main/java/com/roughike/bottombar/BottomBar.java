package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.annotation.XmlRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.scrollsweetness.BottomNavigationBehavior;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/*
 * BottomBar library for Android
 * Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BottomBar extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {
    private static final String STATE_CURRENT_SELECTED_TAB = "STATE_CURRENT_SELECTED_TAB";
    private static final String STATE_BADGE_STATES_BUNDLE = "STATE_BADGE_STATES_BUNDLE";
    private static final String TAG_BADGE = "BOTTOMBAR_BADGE_";

    private static final float DEFAULT_INACTIVE_TAB_ALPHA = 0.6f;
    private static final float DEFAULT_ACTIVE_TAB_ALPHA = 1;

    private boolean isComingFromRestoredState;
    private boolean ignoreTabReselectionListener;
    private boolean isTabletMode;
    private boolean isShy;
    private boolean shyHeightAlreadyCalculated;
    private boolean useExtraOffset;

    private ViewGroup tabContainer;

    private View backgroundOverlay;
    private View shadowView;
    private View tabletRightBorder;

    private Integer primaryColor;
    private Integer darkBackgroundColor;

    private int screenWidth;
    private int tenDp;
    private int maxFixedItemWidth;
    private int inActiveShiftingItemWidth;
    private int activeShiftingItemWidth;

    private OnTabSelectListener onTabSelectListener;
    private OnTabReselectListener onTabReselectListener;

    private int currentTabPosition;
    private boolean isShiftingMode;

    private HashMap<Integer, Object> badgeMap;
    private HashMap<Integer, Boolean> badgeStateMap;

    private int currentBackgroundColor;
    private int defaultBackgroundColor;

    private boolean isDarkTheme;
    private boolean ignoreNightMode;

    private boolean drawBehindNavBar = true;
    private boolean useTopOffset = true;
    private boolean useOnlyStatusBarOffset;

    private int pendingTextAppearance = -1;
    private Typeface pendingTypeface;

    private int maxFixedTabCount = 3;
    private ViewGroup outerContainer;

    // XML Attributes
    private float inActiveTabAlpha;
    private float activeTabAlpha;
    private int inActiveTabColor;
    private int activeTabColor;

    /**
     * Set items for this BottomBar from an XML menu resource file.
     *
     * When setting more than 3 items, only the icons will show by
     * default, but the selected item will have the text visible.
     *
     * @param xmlRes  the menu resource to inflate items from.
     */
    public void setItems(@XmlRes int xmlRes) {
        TabParser.Config config = new TabParser.Config.Builder()
                .inActiveTabAlpha(inActiveTabAlpha)
                .activeTabAlpha(activeTabAlpha)
                .inActiveTabColor(inActiveTabColor)
                .activeTabColor(activeTabColor)
                .barColorWhenSelected(Color.WHITE)
                .build();

        TabParser parser = new TabParser(getContext(), config, xmlRes);
        updateItems(parser.getTabs());
    }

    /**
     * Set a listener that gets fired when the selected tab changes.
     *
     * Note: If listener is set after items are added to the BottomBar, onTabSelected
     * will be immediately called for the currently selected tab
     *
     * @param listener a listener for monitoring changes in tab selection.
     */
    public void setOnTabSelectListener(@Nullable OnTabSelectListener listener) {
        onTabSelectListener = listener;

        if (onTabSelectListener != null && getTabCount() > 0) {
            listener.onTabSelected(getSelectedTab().getId());
        }
    }

    /**
     * Set a listener that gets fired when a currently selected tab is clicked.
     *
     * @param listener a listener for handling tab reselections.
     */
    public void setOnTabReselectListener(@Nullable OnTabReselectListener listener) {
        onTabReselectListener = listener;
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     */
    public void selectTabAtPosition(int position, boolean animate) {
        if (position > getTabCount() - 1 || position < 0) {
            throw new IndexOutOfBoundsException("Can't select tab at position " +
                    position + ". This BottomBar has no items at that position.");
        }

        BottomBarTab oldTab = getSelectedTab();
        BottomBarTab newTab = getTabAtPosition(position);

        oldTab.deselect(animate);
        newTab.select(animate);

        updateSelectedTab(position);
        shiftingMagic(oldTab, newTab, animate);
        handleBackgroundColorChange(newTab, false);
    }

    /**
     * Sets the default tab for this BottomBar that is shown until the user changes
     * the selection.
     *
     * @param defaultTabPosition the default tab position.
     */
    public void setDefaultTabPosition(int defaultTabPosition) {
        if (isComingFromRestoredState) return;

        int tabCount = getTabCount();

        if (tabCount == 0 || defaultTabPosition > tabCount - 1 || defaultTabPosition < 0) {
            throw new IndexOutOfBoundsException("Can't set default tab at position " +
                    defaultTabPosition + ". This BottomBar has no items at that position.");
        }

        selectTabAtPosition(defaultTabPosition, false);
    }

    /**
     * Get the current selected tab position.
     *
     * @return the position of currently selected tab.
     */
    public int getCurrentTabPosition() {
        return currentTabPosition;
    }

    /**
     * Use dark theme instead of the light one.
     */
    public void useDarkTheme() {
        if (!isDarkTheme && getTabCount() > 0) {
            darkThemeMagic();

            for (int i = 0; i < getTabCount(); i++) {
                BottomBarTab bottomBarTab = (BottomBarTab) getTabAtPosition(i);
                ((AppCompatImageView) bottomBarTab.findViewById(R.id.bb_bottom_bar_icon))
                        .setColorFilter(Color.WHITE);

                if (i == currentTabPosition) {
                    bottomBarTab.select(false);
                } else {
                    bottomBarTab.deselect(false);
                }
            }
        }

        isDarkTheme = true;
    }

    /**
     * Ignore the automatic Night Mode detection and use a light theme by default,
     * even if the Night Mode is on.
     */
    public void ignoreNightMode() {
        if (getTabCount() > 0) {
            throw new UnsupportedOperationException("This BottomBar " +
                    "already has items! You must call ignoreNightMode() " +
                    "before setting any items.");
        }

        ignoreNightMode = true;
    }

    /**
     * Creates a new Badge (for example, an indicator for unread messages) for a Tab at
     * the specified position.
     *
     * @param tabPosition     zero-based index for the tab.
     * @param backgroundColor a activeIconColor for this badge, such as "#FF0000".
     * @param initialCount    text displayed initially for this Badge.
     * @return a {@link BottomBarBadge} object.
     */
    public BottomBarBadge makeBadgeForTabAt(int tabPosition, String backgroundColor, int initialCount) {
        return makeBadgeForTabAt(tabPosition, Color.parseColor(backgroundColor), initialCount);
    }

    /**
     * Creates a new Badge (for example, an indicator for unread messages) for a Tab at
     * the specified position.
     *
     * @param tabPosition     zero-based index for the tab.
     * @param backgroundColor a activeIconColor for this badge, such as 0xFFFF0000.
     * @param initialCount    text displayed initially for this Badge.
     * @return a {@link BottomBarBadge} object.
     */
    public BottomBarBadge makeBadgeForTabAt(int tabPosition, int backgroundColor, int initialCount) {
        if (tabPosition > getTabCount() - 1 || tabPosition < 0) {
            throw new IndexOutOfBoundsException("Cant make a Badge for Tab " +
                    "index " + tabPosition + ". You have no BottomBar Tabs at that position.");
        }

        final View tab = getTabAtPosition(tabPosition);

        BottomBarBadge badge = new BottomBarBadge(getContext(), tabPosition,
                tab, backgroundColor);
        badge.setTag(TAG_BADGE + tabPosition);
        badge.setCount(initialCount);

        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick((View) tab.getParent());
            }
        });

        tab.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return handleLongClick((View) tab.getParent());
            }
        });

        if (badgeMap == null) {
            badgeMap = new HashMap<>();
        }

        badgeMap.put(tabPosition, badge.getTag());

        boolean canShow = true;

        if (isComingFromRestoredState && badgeStateMap != null
                && badgeStateMap.containsKey(tabPosition)) {
            canShow = badgeStateMap.get(tabPosition);
        }

        if (canShow && currentTabPosition != tabPosition
                && initialCount != 0) {
            badge.show();
        } else {
            badge.hide();
        }

        return badge;
    }

    /**
     * Set a custom TypeFace for the tab titles.
     * The .ttf file should be located at "/src/main/assets".
     *
     * @param typeFacePath path for the custom typeface in the assets directory.
     */
    public void setTypeFace(String typeFacePath) {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(),
                typeFacePath);

        if (tabContainer != null && getTabCount() > 0) {
            for (int i = 0; i < getTabCount(); i++) {
                View bottomBarTab = getTabAtPosition(i);
                TextView title = (TextView) bottomBarTab.findViewById(R.id.bb_bottom_bar_title);
                title.setTypeface(typeface);
            }
        } else {
            pendingTypeface = typeface;
        }
    }

    /**
     * Set a custom text appearance for the tab title.
     *
     * @param resId path to the custom text appearance.
     */
    public void setTextAppearance(@StyleRes int resId) {
        if (tabContainer != null && getTabCount() > 0) {
            for (int i = 0; i < getTabCount(); i++) {
                View bottomBarTab = getTabAtPosition(i);
                TextView title = (TextView) bottomBarTab.findViewById(R.id.bb_bottom_bar_title);
                MiscUtils.setTextAppearance(title, resId);
            }
        } else {
            pendingTextAppearance = resId;
        }
    }

    /**
     * Hide the shadow that's normally above the BottomBar.
     */
    public void hideShadow() {
        if (shadowView != null) {
            shadowView.setVisibility(GONE);
        }
    }

    /**
     * Super ugly hacks
     * ----------------------------/
     */

    /**
     * If you get some unwanted extra padding in the top (such as
     * when using CoordinatorLayout), this fixes it.
     */
    public void noTopOffset() {
        useTopOffset = false;
    }

    /**
     * If your ActionBar gets inside the status bar for some reason,
     * this fixes it.
     */
    public void useOnlyStatusBarTopOffset() {
        useOnlyStatusBarOffset = true;
    }

    /**
     * ------------------------------------------- //
     */
    public BottomBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = saveState();
        bundle.putParcelable("superstate", super.onSaveInstanceState());
        return bundle;
    }

    private Bundle saveState() {
        Bundle outState = new Bundle();
        outState.putInt(STATE_CURRENT_SELECTED_TAB, currentTabPosition);

        if (badgeMap != null && badgeMap.size() > 0) {
            if (badgeStateMap == null) {
                badgeStateMap = new HashMap<>();
            }

            for (Integer key : badgeMap.keySet()) {
                BottomBarBadge badgeCandidate = (BottomBarBadge) findViewWithTag(badgeMap.get(key));

                if (badgeCandidate != null) {
                    badgeStateMap.put(key, badgeCandidate.isVisible());
                }
            }

            outState.putSerializable(STATE_BADGE_STATES_BUNDLE, badgeStateMap);
        }

        return outState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            restoreState(bundle);

            state = bundle.getParcelable("superstate");
        }
        super.onRestoreInstanceState(state);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Serializable restoredBadgeStateMap = savedInstanceState.getSerializable(STATE_BADGE_STATES_BUNDLE);

            if (restoredBadgeStateMap instanceof HashMap) {
                try {
                    //noinspection unchecked
                    badgeStateMap = (HashMap<Integer, Boolean>) restoredBadgeStateMap;
                } catch (ClassCastException ignored) {}
            }

            isComingFromRestoredState = true;
            ignoreTabReselectionListener = true;

            int restoredPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, currentTabPosition);
            selectTabAtPosition(restoredPosition, false);
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setOrientation(VERTICAL);

        darkBackgroundColor = ContextCompat.getColor(getContext(), R.color.bb_darkBackgroundColor);
        primaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);

        screenWidth = MiscUtils.getScreenWidth(getContext());
        tenDp = MiscUtils.dpToPixel(getContext(), 10);
        maxFixedItemWidth = MiscUtils.dpToPixel(getContext(), 168);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.BottomBar, defStyleAttr, defStyleRes);

        try {
            inActiveTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_inActiveTabAlpha, DEFAULT_INACTIVE_TAB_ALPHA);
            activeTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_activeTabAlpha, DEFAULT_ACTIVE_TAB_ALPHA);
            inActiveTabColor = ta.getColor(R.styleable.BottomBar_bb_inActiveTabColor,
                    ContextCompat.getColor(context, R.color.bb_inActiveBottomBarItemColor));
            activeTabColor = ta.getColor(R.styleable.BottomBar_bb_activeTabColor,
                    MiscUtils.getColor(context, R.attr.colorPrimary));
        } finally {
            ta.recycle();
        }
    }

    private void initializeViews() {
        isTabletMode = getContext().getResources().getBoolean(R.bool.bb_bottom_bar_is_tablet_mode);
        ViewCompat.setElevation(this, MiscUtils.dpToPixel(getContext(), 8));
        View rootView = inflate(getContext(), isTabletMode ?
                        R.layout.bb_bottom_bar_item_container_tablet : R.layout.bb_bottom_bar_item_container,
                this);
        tabletRightBorder = rootView.findViewById(R.id.bb_tablet_right_border);

        shadowView = rootView.findViewById(R.id.bb_bottom_bar_shadow);

        outerContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_outer_container);
        tabContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_item_container);

        backgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay);

        if (isShy && !isTabletMode) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (!shyHeightAlreadyCalculated) {
                        ((CoordinatorLayout.LayoutParams) getLayoutParams())
                                .setBehavior(new BottomNavigationBehavior(getHeight(), 0, isShy(), isTabletMode));
                    }

                    ViewTreeObserver obs = getViewTreeObserver();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        obs.removeOnGlobalLayoutListener(this);
                    } else {
                        obs.removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    /**
     * Makes this BottomBar "shy". In other words, it hides on scroll.
     */
    private void toughChildHood(boolean useExtraOffset) {
        isShy = true;
        this.useExtraOffset = useExtraOffset;
    }

    protected boolean isShy() {
        return isShy;
    }

    protected void shyHeightAlreadyCalculated() {
        shyHeightAlreadyCalculated = true;
    }

    protected boolean useExtraOffset() {
        return useExtraOffset;
    }

    protected boolean useTopOffset() {
        return useTopOffset;
    }

    protected boolean useOnlyStatusbarOffset() {
        return useOnlyStatusBarOffset;
    }

    protected void setBarVisibility(int visibility) {
        if (isShy) {
            toggleShyVisibility(visibility == VISIBLE);
            return;
        }

        if (backgroundOverlay != null) {
            backgroundOverlay.setVisibility(visibility);
        }
    }

    /**
     * Toggle translation of BottomBar to hidden and visible in a CoordinatorLayout.
     *
     * @param visible true resets translation to 0, false translates view to hidden
     */
    protected void toggleShyVisibility(boolean visible) {
        BottomNavigationBehavior<BottomBar> from = BottomNavigationBehavior.from(this);
        if (from != null) {
            from.setHidden(this, visible);
        }
    }

    @Override
    public void onClick(View v) {
        handleClick(v);
    }

    private void handleClick(View v) {
        BottomBarTab oldTab = getSelectedTab();
        BottomBarTab newTab = (BottomBarTab) v;

        oldTab.deselect(true);
        newTab.select(true);

        shiftingMagic(oldTab, newTab, true);
        handleBackgroundColorChange(newTab, true);
        updateSelectedTab(findItemPosition(v));
    }

    private void shiftingMagic(BottomBarTab oldTab, BottomBarTab newTab, boolean animate) {
        if (!isTabletMode && isShiftingMode) {
            if (animate) {
                oldTab.updateWidthAnimated(inActiveShiftingItemWidth);
                newTab.updateWidthAnimated(activeShiftingItemWidth);
            } else {
                oldTab.getLayoutParams().width = inActiveShiftingItemWidth;
                newTab.getLayoutParams().width = activeShiftingItemWidth;
            }
        }
    }

    private void updateSelectedTab(int newPosition) {
        int newTabId = getTabAtPosition(newPosition).getId();

        if (newPosition != currentTabPosition && onTabSelectListener != null) {
            handleBadgeVisibility(currentTabPosition, newPosition);
            currentTabPosition = newPosition;
            onTabSelectListener.onTabSelected(newTabId);
        } else if (onTabReselectListener != null && !ignoreTabReselectionListener) {
            onTabReselectListener.onTabReSelected(newTabId);
        }

        if (ignoreTabReselectionListener) {
            ignoreTabReselectionListener = false;
        }
    }

    private void handleBadgeVisibility(int oldPosition, int newPosition) {
        if (badgeMap == null) {
            return;
        }

        if (badgeMap.containsKey(oldPosition)) {
            BottomBarBadge oldBadge = (BottomBarBadge) findViewWithTag(badgeMap.get(oldPosition));

            if (oldBadge.getAutoShowAfterUnSelection()) {
                oldBadge.show();
            } else {
                oldBadge.hide();
            }
        }

        if (badgeMap.containsKey(newPosition)) {
            BottomBarBadge newBadge = (BottomBarBadge) findViewWithTag(badgeMap.get(newPosition));

            if (newBadge.getAutoHideOnSelection()) {
                newBadge.hide();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return handleLongClick(v);
    }

    private boolean handleLongClick(View v) {
        if ((isShiftingMode || isTabletMode) && !((BottomBarTab) v).isActive()) {
            Toast.makeText(getContext(), getTabAtPosition(findItemPosition(v)).getTitle(), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private BottomBarTab getSelectedTab() {
        return getTabAtPosition(currentTabPosition);
    }

    private BottomBarTab getTabAtPosition(int position) {
        return (BottomBarTab) tabContainer.getChildAt(position);
    }

    private int getTabCount() {
        return tabContainer.getChildCount();
    }

    private void updateItems(final List<BottomBarTab> bottomBarItems) {
        if (tabContainer == null) {
            initializeViews();
        }

        int index = 0;
        int biggestWidth = 0;

        isShiftingMode = maxFixedTabCount >= 0 && maxFixedTabCount < bottomBarItems.size();

        if (!isDarkTheme && !ignoreNightMode
                && MiscUtils.isNightMode(getContext())) {
            isDarkTheme = true;
        }

        if (isDarkTheme) {
            darkThemeMagic();
        } else if (!isTabletMode && isShiftingMode) {
            defaultBackgroundColor = currentBackgroundColor = primaryColor;
        }

        BottomBarTab[] viewsToAdd = new BottomBarTab[bottomBarItems.size()];

        for (BottomBarTab bottomBarTab : bottomBarItems) {
            BottomBarTab.Type type;

            if (isShiftingMode && !isTabletMode) {
                type = BottomBarTab.Type.SHIFTING;
            } else if (isTabletMode) {
                type = BottomBarTab.Type.TABLET;
            } else {
                type = BottomBarTab.Type.FIXED;
            }

            bottomBarTab.setType(type);
            bottomBarTab.prepareLayout();

            if (!isTabletMode) {
                if (pendingTextAppearance != -1) {
                    bottomBarTab.setTitleTextAppearance(pendingTextAppearance);
                }

                if (pendingTypeface != null) {
                    bottomBarTab.setTitleTypeface(pendingTypeface);
                }
            }

            if (isDarkTheme || (!isTabletMode && isShiftingMode)) {
                bottomBarTab.setIconTint(Color.WHITE);
            }

            if (index == currentTabPosition) {
                bottomBarTab.select(false);

                int barBackgroundColor = bottomBarTab.getBarColorWhenSelected();
                    outerContainer.setBackgroundColor(barBackgroundColor);
            } else {
                bottomBarTab.deselect(false);
            }

            if (!isTabletMode) {
                if (bottomBarTab.getWidth() > biggestWidth) {
                    biggestWidth = bottomBarTab.getWidth();
                }

                viewsToAdd[index] = bottomBarTab;
            } else {
                tabContainer.addView(bottomBarTab);
            }

            bottomBarTab.setOnClickListener(this);
            bottomBarTab.setOnLongClickListener(this);
            index++;
        }

        if (!isTabletMode) {
            int proposedItemWidth = Math.min(
                    MiscUtils.dpToPixel(getContext(), screenWidth / bottomBarItems.size()),
                    maxFixedItemWidth
            );

            inActiveShiftingItemWidth = (int) (proposedItemWidth * 0.9);
            activeShiftingItemWidth = (int) (proposedItemWidth + (proposedItemWidth * (bottomBarItems.size() * 0.1)));

            int height = Math.round(getContext().getResources().getDimension(R.dimen.bb_height));
            for (BottomBarTab bottomBarView : viewsToAdd) {
                LinearLayout.LayoutParams params;

                if (isShiftingMode) {
                    if (bottomBarView.isActive()) {
                        params = new LinearLayout.LayoutParams(activeShiftingItemWidth, height);
                    } else {
                        params = new LinearLayout.LayoutParams(inActiveShiftingItemWidth, height);
                    }
                } else {
                    params = new LinearLayout.LayoutParams(proposedItemWidth, height);
                }

                bottomBarView.setLayoutParams(params);
                tabContainer.addView(bottomBarView);
            }
        }

        if (pendingTextAppearance != -1) {
            pendingTextAppearance = -1;
        }

        if (pendingTypeface != null) {
            pendingTypeface = null;
        }
    }

    private void darkThemeMagic() {
        if (!isTabletMode) {
            tabContainer.setBackgroundColor(darkBackgroundColor);
        } else {
            tabContainer.setBackgroundColor(darkBackgroundColor);
            tabletRightBorder.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bb_tabletRightBorderDark));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateTitleBottomPadding();
        }
    }

    /**
     * Material Design specify that there should be a 10dp padding under the text, it seems that
     * it means 10dp starting from the text baseline.
     * This method takes care of calculating the amount of padding that needs to be added to the
     * Title TextView in order to comply with the Material Design specifications.
     */
    private void updateTitleBottomPadding() {
        if (tabContainer == null) {
            return;
        }

        int childCount = getTabCount();

        for (int i = 0; i < childCount; i++) {
            View tab = tabContainer.getChildAt(i);
            TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);
            if (title == null) {
                continue;
            }
            int baseline = title.getBaseline();
            // Height already includes any possible top/bottom padding
            int height = title.getHeight();
            int paddingInsideTitle = height - baseline;
            int missingPadding = tenDp - paddingInsideTitle;
            if (missingPadding > 0) {
                // Only update the padding if really needed
                title.setPadding(title.getPaddingLeft(), title.getPaddingTop(),
                    title.getPaddingRight(), missingPadding + title.getPaddingBottom());
            }
        }
    }

    private void handleBackgroundColorChange(BottomBarTab tab, boolean animate) {
        int newColor = tab.getBarColorWhenSelected();

        if (currentBackgroundColor == newColor) {
            return;
        }

        if (!animate) {
            outerContainer.setBackgroundColor(newColor);
            return;
        }

        animateBGColorChange(tab, backgroundOverlay, newColor);
        currentBackgroundColor = newColor;
    }

    private void animateBGColorChange(View clickedView, final View bgOverlay, final int newColor) {
        int centerX = (int) (ViewCompat.getX(clickedView) + (clickedView.getMeasuredWidth() / 2));
        int centerY = clickedView.getMeasuredHeight() / 2;
        int finalRadius = outerContainer.getWidth();

        outerContainer.clearAnimation();
        bgOverlay.clearAnimation();

        Object animator;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!bgOverlay.isAttachedToWindow()) {
                return;
            }

            animator = ViewAnimationUtils
                    .createCircularReveal(bgOverlay, centerX, centerY, 0, finalRadius);
        } else {
            ViewCompat.setAlpha(bgOverlay, 0);
            animator = ViewCompat.animate(bgOverlay).alpha(1);
        }

        if (animator instanceof ViewPropertyAnimatorCompat) {
            ((ViewPropertyAnimatorCompat) animator).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    onEnd();
                }

                @Override
                public void onAnimationCancel(View view) {
                    onEnd();
                }

                private void onEnd() {
                    outerContainer.setBackgroundColor(newColor);
                    bgOverlay.setVisibility(View.INVISIBLE);
                    ViewCompat.setAlpha(bgOverlay, 1);
                }
            }).start();
        } else if (animator != null) {
            ((Animator) animator).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onEnd();
                }

                private void onEnd() {
                    outerContainer.setBackgroundColor(newColor);
                    bgOverlay.setVisibility(View.INVISIBLE);
                    ViewCompat.setAlpha(bgOverlay, 1);
                }
            });

            ((Animator) animator).start();
        }

        bgOverlay.setBackgroundColor(newColor);
        bgOverlay.setVisibility(View.VISIBLE);
    }

    private int findItemPosition(View viewToFind) {
        int position = 0;

        for (int i = 0; i < getTabCount(); i++) {
            View candidate = getTabAtPosition(i);

            if (candidate.equals(viewToFind)) {
                position = i;
                break;
            }
        }

        return position;
    }

    private void clearItems() {
        if (tabContainer != null) {
            tabContainer.removeAllViews();
        }
    }
}
