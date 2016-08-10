package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final float DEFAULT_INACTIVE_TAB_ALPHA = 0.6f;
    private static final float DEFAULT_ACTIVE_TAB_ALPHA = 1;

    // Behaviors
    private static final int BEHAVIOR_NONE = 0;
    private static final int BEHAVIOR_SHIFTING = 1;
    private static final int BEHAVIOR_SHY = 2;

    private boolean isComingFromRestoredState;
    private boolean ignoreTabReselectionListener;
    private boolean shyHeightAlreadyCalculated;
    private boolean useExtraOffset;

    private ViewGroup tabContainer;

    private View backgroundOverlay;
    private View shadowView;
    private View tabletRightBorder;

    private int primaryColor;
    private int darkBackgroundColor;

    private int screenWidth;
    private int tenDp;
    private int maxFixedItemWidth;
    private int inActiveShiftingItemWidth;
    private int activeShiftingItemWidth;

    private OnTabSelectListener onTabSelectListener;
    private OnTabReselectListener onTabReselectListener;

    private int currentTabPosition;

    private int currentBackgroundColor;
    private int defaultBackgroundColor = Color.WHITE;

    private ViewGroup outerContainer;

    // XML Attributes
    private int tabXmlResource;
    private int behaviors;
    private float inActiveTabAlpha;
    private float activeTabAlpha;
    private int inActiveTabColor;
    private int activeTabColor;
    private int titleTextAppearance;
    private String titleTypeFace;
    private boolean isTabletMode;

    /**
     * ------------------------------------------- //
     */
    public BottomBar(Context context) {
        super(context);
        init(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Set items for this BottomBar from an XML menu resource file.
     * <p/>
     * When setting more than 3 items, only the icons will show by
     * default, but the selected item will have the text visible.
     *
     * @param xmlRes the menu resource to inflate items from.
     */
    void setItems(@XmlRes int xmlRes) {
        if (xmlRes == 0) {
            throw new RuntimeException("No items specified for the BottomBar!");
        }

        TabParser.Config config = new TabParser.Config.Builder()
                .inActiveTabAlpha(inActiveTabAlpha)
                .activeTabAlpha(activeTabAlpha)
                .inActiveTabColor(inActiveTabColor)
                .activeTabColor(activeTabColor)
                .barColorWhenSelected(defaultBackgroundColor)
                .badgeBackgroundColor(Color.RED)
                .titleTextAppearance(titleTextAppearance)
                .titleTypeFace(getContext(), titleTypeFace)
                .build();

        TabParser parser = new TabParser(getContext(), config, xmlRes);
        updateItems(parser.getTabs());
    }

    /**
     * Set a listener that gets fired when the selected tab changes.
     * <p/>
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
     * Get the currently selected tab position.
     */
    public int getCurrentTabPosition() {
        return currentTabPosition;
    }

    /**
     * Get the currently selected tab.
     */
    public BottomBarTab getCurrentTab() {
        return getTabAtPosition(getCurrentTabPosition());
    }

    /**
     * Get the resource id for the currently selected tab.
     * @return
     */
    @IdRes
    public int getCurrentTabId() {
        return getCurrentTab().getId();
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
            isComingFromRestoredState = true;
            ignoreTabReselectionListener = true;

            int restoredPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, currentTabPosition);
            selectTabAtPosition(restoredPosition, false);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        ViewCompat.setElevation(this, MiscUtils.dpToPixel(getContext(), 8));

        populateAttributes(context, attrs);
        initializeViews();
        determineInitialBackgroundColor();
        setItems(tabXmlResource);
    }

    private void populateAttributes(Context context, AttributeSet attrs) {
        darkBackgroundColor = ContextCompat.getColor(getContext(), R.color.bb_darkBackgroundColor);
        primaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);

        screenWidth = MiscUtils.getScreenWidth(getContext());
        tenDp = MiscUtils.dpToPixel(getContext(), 10);
        maxFixedItemWidth = MiscUtils.dpToPixel(getContext(), 168);

        TypedArray ta = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.BottomBar, 0, 0);

        try {
            tabXmlResource = ta.getResourceId(R.styleable.BottomBar_bb_tabXmlResource, 0);
            behaviors = ta.getInteger(R.styleable.BottomBar_bb_behavior, BEHAVIOR_NONE);
            inActiveTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_inActiveTabAlpha, DEFAULT_INACTIVE_TAB_ALPHA);
            activeTabAlpha = ta.getFloat(R.styleable.BottomBar_bb_activeTabAlpha, DEFAULT_ACTIVE_TAB_ALPHA);

            @ColorInt
            int defaultInActiveColor = isShiftingMode()?
                    Color.WHITE : ContextCompat.getColor(context, R.color.bb_inActiveBottomBarItemColor);
            int defaultActiveColor = isShiftingMode()? Color.WHITE : primaryColor;

            inActiveTabColor = ta.getColor(R.styleable.BottomBar_bb_inActiveTabColor, defaultInActiveColor);
            activeTabColor = ta.getColor(R.styleable.BottomBar_bb_activeTabColor, defaultActiveColor);
            titleTextAppearance = ta.getResourceId(R.styleable.BottomBar_bb_titleTextAppearance, 0);
            titleTypeFace = ta.getString(R.styleable.BottomBar_bb_titleTypeFace);
        } finally {
            ta.recycle();
        }
    }

    private void initializeViews() {
        View rootView = inflate(getContext(), R.layout.bb_bottom_bar_item_container, this);

        tabletRightBorder = rootView.findViewById(R.id.bb_tablet_right_border);
        shadowView = rootView.findViewById(R.id.bb_bottom_bar_shadow);
        backgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay);
        outerContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_outer_container);
        tabContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_item_container);
    }

    private void determineInitialBackgroundColor() {
        if (isShiftingMode()) {
            defaultBackgroundColor = primaryColor;
        }

        Drawable userDefinedBackground = getBackground();

        boolean userHasDefinedBackgroundColor = userDefinedBackground != null
                && userDefinedBackground instanceof ColorDrawable;

        if (userHasDefinedBackgroundColor) {
            defaultBackgroundColor = ((ColorDrawable) userDefinedBackground).getColor();
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private boolean isShiftingMode() {
        return hasBehavior(BEHAVIOR_SHIFTING);
    }

    private boolean isShy() {
        return hasBehavior(BEHAVIOR_SHY);
    }

    private boolean hasBehavior(int behavior) {
        return (behaviors | behavior) == behaviors;
    }

    private void initializeShyBehavior() {
        ViewParent parent = getParent();

        boolean hasAbusiveParent = parent != null
                && parent instanceof CoordinatorLayout;

        if (!hasAbusiveParent) {
            throw new RuntimeException("In order to have shy behavior, the " +
                    "BottomBar must be directly contained by a CoordinatorLayout.");
        }

        if (!shyHeightAlreadyCalculated) {
            int height = getHeight();

            if (height != 0) {
                ((CoordinatorLayout.LayoutParams) getLayoutParams())
                        .setBehavior(new BottomNavigationBehavior(height, 0, false));
                shyHeightAlreadyCalculated = true;
            }
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
        if (isShiftingMode()) {
            oldTab.updateWidth(inActiveShiftingItemWidth, animate);
            newTab.updateWidth(activeShiftingItemWidth, animate);
        }
    }

    private void updateSelectedTab(int newPosition) {
        int newTabId = getTabAtPosition(newPosition).getId();

        if (newPosition != currentTabPosition && onTabSelectListener != null) {
            currentTabPosition = newPosition;
            onTabSelectListener.onTabSelected(newTabId);
        } else if (onTabReselectListener != null && !ignoreTabReselectionListener) {
            onTabReselectListener.onTabReSelected(newTabId);
        }

        if (ignoreTabReselectionListener) {
            ignoreTabReselectionListener = false;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return handleLongClick(v);
    }

    private boolean handleLongClick(View v) {
        if (isShiftingMode() && !((BottomBarTab) v).isActive()) {
            Toast.makeText(getContext(), getTabAtPosition(findItemPosition(v)).getTitle(), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private BottomBarTab getSelectedTab() {
        return getTabAtPosition(currentTabPosition);
    }

    private BottomBarTab getTabAtPosition(int position) {
        View child = tabContainer.getChildAt(position);

        if (child instanceof FrameLayout) {
            return findTabInLayout((FrameLayout) child);
        }

        return (BottomBarTab) child;
    }

    private BottomBarTab findTabInLayout(ViewGroup child) {
        for (int i = 0; i < child.getChildCount(); i++) {
            View candidate = child.getChildAt(i);

            if (candidate instanceof BottomBarTab) {
                return (BottomBarTab) candidate;
            }
        }

        return null;
    }

    private int getTabCount() {
        return tabContainer.getChildCount();
    }

    private void updateItems(final List<BottomBarTab> bottomBarItems) {
        int index = 0;
        int biggestWidth = 0;

        if (isShiftingMode()) {
            defaultBackgroundColor = currentBackgroundColor = primaryColor;
        }

        BottomBarTab[] viewsToAdd = new BottomBarTab[bottomBarItems.size()];

        for (BottomBarTab bottomBarTab : bottomBarItems) {
            BottomBarTab.Type type;

            if (isShiftingMode()) {
                type = BottomBarTab.Type.SHIFTING;
            } else if (isTabletMode) {
                type = BottomBarTab.Type.TABLET;
            } else {
                type = BottomBarTab.Type.FIXED;
            }

            bottomBarTab.setType(type);
            bottomBarTab.prepareLayout();

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

                if (isShiftingMode()) {
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
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            updateTitleBottomPadding();

            if (isShy()) {
                initializeShyBehavior();
            }
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
            int height = title.getHeight();
            int paddingInsideTitle = height - baseline;
            int missingPadding = tenDp - paddingInsideTitle;

            if (missingPadding > 0) {
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

        View clickedView = tab;

        if (tab.hasActiveBadge()) {
            clickedView = tab.getOuterView();
        }

        animateBGColorChange(clickedView, newColor);

        currentBackgroundColor = newColor;
    }

    private void animateBGColorChange(View clickedView, final int newColor) {
        prepareForBackgroundColorAnimation(newColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!outerContainer.isAttachedToWindow()) {
                return;
            }

            backgroundCircularRevealAnimation(clickedView, newColor);
        } else {
            backgroundCrossfadeAnimation(newColor);
        }
    }

    private void prepareForBackgroundColorAnimation(int newColor) {
        outerContainer.clearAnimation();
        backgroundOverlay.clearAnimation();

        backgroundOverlay.setBackgroundColor(newColor);
        backgroundOverlay.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void backgroundCircularRevealAnimation(View clickedView, final int newColor) {
        int centerX = (int) (ViewCompat.getX(clickedView) + (clickedView.getMeasuredWidth() / 2));
        int centerY = clickedView.getMeasuredHeight() / 2;
        int startRadius = 0;
        int finalRadius = outerContainer.getWidth();

        Animator animator = ViewAnimationUtils.createCircularReveal(
                backgroundOverlay,
                centerX,
                centerY,
                startRadius,
                finalRadius
        );

        animator.addListener(new AnimatorListenerAdapter() {
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
                backgroundOverlay.setVisibility(View.INVISIBLE);
                ViewCompat.setAlpha(backgroundOverlay, 1);
            }
        });

        animator.start();
    }

    private void backgroundCrossfadeAnimation(final int newColor) {
        ViewCompat.setAlpha(backgroundOverlay, 0);
        ViewCompat.animate(backgroundOverlay)
                .alpha(1)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
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
                        backgroundOverlay.setVisibility(View.INVISIBLE);
                        ViewCompat.setAlpha(backgroundOverlay, 1);
                    }
                }).start();
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

    public BottomBarTab getTabWithId(@IdRes int tabId) {
        return (BottomBarTab) tabContainer.findViewById(tabId);
    }
}