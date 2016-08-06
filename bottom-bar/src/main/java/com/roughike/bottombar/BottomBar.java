package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    private static final long ANIMATION_DURATION = 150;

    private static final String STATE_CURRENT_SELECTED_TAB = "STATE_CURRENT_SELECTED_TAB";
    private static final String STATE_BADGE_STATES_BUNDLE = "STATE_BADGE_STATES_BUNDLE";
    private static final String TAG_BADGE = "BOTTOMBAR_BADGE_";

    private Context mContext;
    private boolean mIsComingFromRestoredState;
    private boolean mIgnoreTabReselectionListener;
    private boolean mIgnoreTabletLayout;
    private boolean mIsTabletMode;
    private boolean mIsShy;
    private boolean mShyHeightAlreadyCalculated;
    private boolean mUseExtraOffset;

    private ViewGroup mUserContentContainer;
    private ViewGroup mOuterContainer;
    private ViewGroup mTabContainer;

    private View mBackgroundOverlay;
    private View mShadowView;
    private View mTabletRightBorder;

    private Integer mPrimaryColor;
    private Integer mInActiveColor;
    private Integer mDarkBackgroundColor;
    private Integer mWhiteColor;
    private float mTabAlpha = 0.6f;

    private int mScreenWidth;
    private int mTenDp;
    private int mSixDp;
    private int mSixteenDp;
    private int mEightDp;
    private int mMaxFixedItemWidth;
    private int mMaxInActiveShiftingItemWidth;
    private int mInActiveShiftingItemWidth;
    private int mActiveShiftingItemWidth;

    private OnTabSelectListener mListener;
    private OnTabReselectListener mReselectionListener;

    private int mCurrentTabPosition;
    private boolean mIsShiftingMode;

    private Object mFragmentManager;
    private int mFragmentContainer;

    private List<BottomBarTab> mItems;
    private HashMap<Integer, Object> mBadgeMap;
    private HashMap<Integer, Boolean> mBadgeStateMap;

    private int mCurrentBackgroundColor;
    private int mDefaultBackgroundColor;

    private boolean mIsDarkTheme;
    private boolean mIgnoreNightMode;
    private boolean mIgnoreShiftingResize;
    private boolean mIgnoreScalingResize;

    private int mCustomActiveTabColor;

    private boolean mDrawBehindNavBar = true;
    private boolean mUseTopOffset = true;
    private boolean mUseOnlyStatusBarOffset;

    private int mPendingTextAppearance = -1;
    private Typeface mPendingTypeface;

    private int mMaxFixedTabCount = 3;

    /**
     * Set items for this BottomBar from an XML menu resource file.
     *
     * When setting more than 3 items, only the icons will show by
     * default, but the selected item will have the text visible.
     *
     * @param xmlRes  the menu resource to inflate items from.
     */
    public void setItems(@XmlRes int xmlRes) {
        clearItems();
        mItems = MiscUtils.inflateFromXMLResource(getContext(), xmlRes);
        newUpdateItems(mItems);
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
        mListener = listener;

        if (mListener != null && mItems != null && mItems.size() > 0) {
            listener.onTabSelected(mItems.get(mCurrentTabPosition).getId());
        }
    }

    /**
     * Set a listener that gets fired when a currently selected tab is clicked.
     *
     * @param listener a listener for handling tab reselections.
     */
    public void setOnTabReselectListener(@Nullable OnTabReselectListener listener) {
        mReselectionListener = listener;
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     */
    public void selectTabAtPosition(int position, boolean animate) {
        if (mItems == null || mItems.size() == 0) {
            throw new UnsupportedOperationException("Can't select tab at " +
                    "position " + position + ". This BottomBar has no items set yet.");
        } else if (position > mItems.size() - 1 || position < 0) {
            throw new IndexOutOfBoundsException("Can't select tab at position " +
                    position + ". This BottomBar has no items at that position.");
        }

        BottomBarTab oldTab = (BottomBarTab) mTabContainer.findViewWithTag(BottomBarTab.TAG_ACTIVE);
        BottomBarTab newTab = (BottomBarTab) mTabContainer.getChildAt(position);

        oldTab.deselect(animate);
        newTab.select(animate);

        updateSelectedTab(position);
        shiftingMagic(oldTab, newTab, animate);
        handleBackgroundColorChange(newTab);
    }

    /**
     * Sets the default tab for this BottomBar that is shown until the user changes
     * the selection.
     *
     * @param defaultTabPosition the default tab position.
     */
    public void setDefaultTabPosition(int defaultTabPosition) {
        if (mIsComingFromRestoredState) return;

        if (mItems == null) {
            mCurrentTabPosition = defaultTabPosition;
            return;
        } else if (mItems.size() == 0 || defaultTabPosition > mItems.size() - 1
                || defaultTabPosition < 0) {
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
        return mCurrentTabPosition;
    }

    /**
     * Hide the BottomBar.
     */
    public void hide() {
        setBarVisibility(GONE);
    }

    /**
     * Show the BottomBar.
     */
    public void show() {
        setBarVisibility(VISIBLE);
    }

    /**
     * Set the maximum number of tabs, after which the tabs should be shifting
     * ones with a background activeIconColor.
     * 
     * NOTE: You must call this method before setting any items.
     *
     * @param count maximum number of fixed tabs.
     */
    public void setMaxFixedTabs(int count) {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call the setMaxFixedTabs() method before specifying any items.");
        }

        mMaxFixedTabCount = count;
    }

    /**
     * Always show the titles and icons also on inactive tabs, even if there's more
     * than three of them.
     */
    public void useFixedMode() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call the useFixedMode() method before specifying any items.");
        }

        mMaxFixedTabCount = -1;
    }

    /**
     * Deprecated. Use {@link #useDarkTheme()} instead.
     */
    @Deprecated
    public void useDarkTheme(boolean darkThemeEnabled) {
        mIsDarkTheme = darkThemeEnabled;
        useDarkTheme();
    }

    /**
     * Use dark theme instead of the light one.
     * 
     * NOTE: You might want to change your active tab activeIconColor to something else
     * using {@link #setActiveTabColor(int)}, as the default primary activeIconColor might
     * not have enough contrast for the dark background.
     */
    public void useDarkTheme() {
        if (!mIsDarkTheme && mItems != null && mItems.size() > 0) {
            darkThemeMagic();

            for (int i = 0; i < mTabContainer.getChildCount(); i++) {
                BottomBarTab bottomBarTab = (BottomBarTab) mTabContainer.getChildAt(i);
                ((AppCompatImageView) bottomBarTab.findViewById(R.id.bb_bottom_bar_icon))
                        .setColorFilter(mWhiteColor);

                if (i == mCurrentTabPosition) {
                    bottomBarTab.select(false);
                } else {
                    bottomBarTab.deselect(false);
                }
            }
        }

        mIsDarkTheme = true;
    }

    /**
     * Ignore the automatic Night Mode detection and use a light theme by default,
     * even if the Night Mode is on.
     */
    public void ignoreNightMode() {
        if (mItems != null && mItems.size() > 0) {
            throw new UnsupportedOperationException("This BottomBar " +
                    "already has items! You must call ignoreNightMode() " +
                    "before setting any items.");
        }

        mIgnoreNightMode = true;
    }

    /**
     * Set a custom activeIconColor for an active tab when there's three
     * or less items.
     * 
     * NOTE: This value is ignored on mobile devices if you have more than
     * three items.
     *
     * @param activeTabColor a hex activeIconColor used for active tabs, such as "#00FF000".
     */
    public void setActiveTabColor(String activeTabColor) {
        setActiveTabColor(Color.parseColor(activeTabColor));
    }

    /**
     * Set a custom activeIconColor for an active tab when there's three
     * or less items.
     * 
     * NOTE: This value is ignored if you have more than three items.
     *
     * @param activeTabColor a hex activeIconColor used for active tabs, such as 0xFF00FF00.
     */
    public void setActiveTabColor(int activeTabColor) {
        mCustomActiveTabColor = activeTabColor;

        if (mItems != null && mItems.size() > 0) {
            selectTabAtPosition(mCurrentTabPosition, false);
        }
    }

    /**
     * Set a custom activeIconColor for inactive icons in fixed mode.
     * 
     * NOTE: This value is ignored if not in fixed mode.
     *
     * @param iconColor a hex activeIconColor used for icons, such as 0xFF00FF00.
     */
    public void setFixedInactiveIconColor(int iconColor) {
        mInActiveColor = iconColor;

        if (mItems != null && mItems.size() > 0) {
            throw new UnsupportedOperationException("This BottomBar " +
                    "already has items! You must call setFixedInactiveIconColor() " +
                    "before setting any items.");
        }
    }

    /**
     * Set a custom activeIconColor for icons in shifting mode.
     * 
     * NOTE: This value is ignored in fixed mode.
     *
     * @param iconColor a hex activeIconColor used for icons, such as 0xFF00FF00.
     */
    public void setShiftingIconColor(int iconColor) {
        mWhiteColor = iconColor;

        if (mItems != null && mItems.size() > 0) {
            throw new UnsupportedOperationException("This BottomBar " +
                    "already has items! You must call setShiftingIconColor() " +
                    "before setting any items.");
        }
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
        if (mItems == null || mItems.size() == 0) {
            throw new UnsupportedOperationException("You have no BottomBar Tabs set yet. " +
                    "Please set them first before calling the makeBadgeForTabAt() method.");
        } else if (tabPosition > mItems.size() - 1 || tabPosition < 0) {
            throw new IndexOutOfBoundsException("Cant make a Badge for Tab " +
                    "index " + tabPosition + ". You have no BottomBar Tabs at that position.");
        }

        final View tab = mTabContainer.getChildAt(tabPosition);

        BottomBarBadge badge = new BottomBarBadge(mContext, tabPosition,
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

        if (mBadgeMap == null) {
            mBadgeMap = new HashMap<>();
        }

        mBadgeMap.put(tabPosition, badge.getTag());

        boolean canShow = true;

        if (mIsComingFromRestoredState && mBadgeStateMap != null
                && mBadgeStateMap.containsKey(tabPosition)) {
            canShow = mBadgeStateMap.get(tabPosition);
        }

        if (canShow && mCurrentTabPosition != tabPosition
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
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),
                typeFacePath);

        if (mTabContainer != null && mTabContainer.getChildCount() > 0) {
            for (int i = 0; i < mTabContainer.getChildCount(); i++) {
                View bottomBarTab = mTabContainer.getChildAt(i);
                TextView title = (TextView) bottomBarTab.findViewById(R.id.bb_bottom_bar_title);
                title.setTypeface(typeface);
            }
        } else {
            mPendingTypeface = typeface;
        }
    }

    /**
     * Set a custom text appearance for the tab title.
     *
     * @param resId path to the custom text appearance.
     */
    public void setTextAppearance(@StyleRes int resId) {
        if (mTabContainer != null && mTabContainer.getChildCount() > 0) {
            for (int i = 0; i < mTabContainer.getChildCount(); i++) {
                View bottomBarTab = mTabContainer.getChildAt(i);
                TextView title = (TextView) bottomBarTab.findViewById(R.id.bb_bottom_bar_title);
                MiscUtils.setTextAppearance(title, resId);
            }
        } else {
            mPendingTextAppearance = resId;
        }
    }

    /**
     * Hide the shadow that's normally above the BottomBar.
     */
    public void hideShadow() {
        if (mShadowView != null) {
            mShadowView.setVisibility(GONE);
        }
    }

    /**
     * Prevent the BottomBar drawing behind the Navigation Bar and making
     * it transparent. Must be called before setting items.
     */
    public void noNavBarGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noNavBarGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mDrawBehindNavBar = false;
    }

    /**
     * Force the BottomBar to behave exactly same on tablets and phones,
     * instead of showing a left menu on tablets.
     */
    public void noTabletGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noTabletGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mIgnoreTabletLayout = true;
    }

    /**
     * Don't resize the tabs when selecting a new one, so every tab is the same if you have more than three
     * tabs. The text still displays the scale animation and the icon moves up, but the badass width animation
     * is ignored.
     */
    public void noResizeGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noResizeGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mIgnoreShiftingResize = true;
    }

    /**
     * Don't animate the scaling of the text when selecting a new tab. The text still displays the badass width animation,
     * but the scale animation is ignored.
     */
    public void noScalingGoodness() {
        if (mItems != null) {
            throw new UnsupportedOperationException("This BottomBar already has items! " +
                    "You must call noScalingGoodness() before setting the items, preferably " +
                    "right after attaching it to your layout.");
        }

        mIgnoreScalingResize = true;
    }

    /**
     * Get the actual BottomBar that has the tabs inside it for whatever what you may want
     * to do with it.
     *
     * @return the BottomBar.
     */
    public View getBar() {
        return mOuterContainer;
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
        mUseTopOffset = false;
    }

    /**
     * If your ActionBar gets inside the status bar for some reason,
     * this fixes it.
     */
    public void useOnlyStatusBarTopOffset() {
        mUseOnlyStatusBarOffset = true;
    }

    /**
     * ------------------------------------------- //
     */
    public BottomBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BottomBar(Context context, int backgroundColor, int activeColor, float alpha) {
        super(context);
        mTabAlpha = alpha;
        mWhiteColor = activeColor;
        mPrimaryColor = backgroundColor;
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
        outState.putInt(STATE_CURRENT_SELECTED_TAB, mCurrentTabPosition);

        if (mBadgeMap != null && mBadgeMap.size() > 0) {
            if (mBadgeStateMap == null) {
                mBadgeStateMap = new HashMap<>();
            }

            for (Integer key : mBadgeMap.keySet()) {
                BottomBarBadge badgeCandidate = (BottomBarBadge) mOuterContainer
                        .findViewWithTag(mBadgeMap.get(key));

                if (badgeCandidate != null) {
                    mBadgeStateMap.put(key, badgeCandidate.isVisible());
                }
            }

            outState.putSerializable(STATE_BADGE_STATES_BUNDLE, mBadgeStateMap);
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
                    mBadgeStateMap = (HashMap<Integer, Boolean>) restoredBadgeStateMap;
                } catch (ClassCastException ignored) {}
            }

            mIsComingFromRestoredState = true;
            mIgnoreTabReselectionListener = true;

            selectTabAtPosition(savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, mCurrentTabPosition), false);
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setOrientation(VERTICAL);

        mContext = context;

        mDarkBackgroundColor = ContextCompat.getColor(getContext(), R.color.bb_darkBackgroundColor);


        if (mWhiteColor == null) {
            mWhiteColor = ContextCompat.getColor(getContext(), R.color.white);
            mPrimaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
            mInActiveColor = ContextCompat.getColor(getContext(), R.color.bb_inActiveBottomBarItemColor);
        }

        //mWhiteColor = ContextCompat.getColor(getContext(), R.activeIconColor.white);
        //mPrimaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
        //mInActiveColor = ContextCompat.getColor(getContext(), R.activeIconColor.bb_inActiveBottomBarItemColor);


        //mWhiteColor = Color.parseColor("#000000");
        //mPrimaryColor = Color.parseColor("#555555");
        //mInActiveColor = Color.parseColor("#ffffff");

        mScreenWidth = MiscUtils.getScreenWidth(mContext);
        mTenDp = MiscUtils.dpToPixel(mContext, 10);
        mSixteenDp = MiscUtils.dpToPixel(mContext, 16);
        mSixDp = MiscUtils.dpToPixel(mContext, 6);
        mEightDp = MiscUtils.dpToPixel(mContext, 8);
        mMaxFixedItemWidth = MiscUtils.dpToPixel(mContext, 168);
        mMaxInActiveShiftingItemWidth = MiscUtils.dpToPixel(mContext, 96);
    }

    private void initializeViews() {
        mIsTabletMode = !mIgnoreTabletLayout &&
                mContext.getResources().getBoolean(R.bool.bb_bottom_bar_is_tablet_mode);
        ViewCompat.setElevation(this, MiscUtils.dpToPixel(mContext, 8));
        View rootView = inflate(mContext, mIsTabletMode ?
                        R.layout.bb_bottom_bar_item_container_tablet : R.layout.bb_bottom_bar_item_container,
                this);
        mTabletRightBorder = rootView.findViewById(R.id.bb_tablet_right_border);

        mUserContentContainer = (ViewGroup) rootView.findViewById(R.id.bb_user_content_container);

        mShadowView = rootView.findViewById(R.id.bb_bottom_bar_shadow);

        mOuterContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_outer_container);
        mTabContainer = (ViewGroup) rootView.findViewById(R.id.bb_bottom_bar_item_container);

        mBackgroundOverlay = rootView.findViewById(R.id.bb_bottom_bar_background_overlay);

        if (mIsShy && !mIsTabletMode) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (!mShyHeightAlreadyCalculated) {
                        ((CoordinatorLayout.LayoutParams) getLayoutParams())
                                .setBehavior(new BottomNavigationBehavior(getOuterContainer().getHeight(), 0, isShy(), mIsTabletMode));
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
        mIsShy = true;
        mUseExtraOffset = useExtraOffset;
    }

    protected boolean isShy() {
        return mIsShy;
    }

    protected void shyHeightAlreadyCalculated() {
        mShyHeightAlreadyCalculated = true;
    }

    protected boolean useExtraOffset() {
        return mUseExtraOffset;
    }

    protected ViewGroup getUserContainer() {
        return mUserContentContainer;
    }

    protected View getOuterContainer() {
        return mOuterContainer;
    }

    protected boolean drawBehindNavBar() {
        return mDrawBehindNavBar;
    }

    protected boolean useTopOffset() {
        return mUseTopOffset;
    }

    protected boolean useOnlyStatusbarOffset() {
        return mUseOnlyStatusBarOffset;
    }

    protected void setBarVisibility(int visibility) {
        if (mIsShy) {
            toggleShyVisibility(visibility == VISIBLE);
            return;
        }

        if (mOuterContainer != null) {
            mOuterContainer.setVisibility(visibility);
        }

        if (mBackgroundOverlay != null) {
            mBackgroundOverlay.setVisibility(visibility);
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
        if (v.getTag().equals(BottomBarTab.TAG_INACTIVE)) {
            BottomBarTab oldTab = (BottomBarTab) findViewWithTag(BottomBarTab.TAG_ACTIVE);

            oldTab.deselect(!mIgnoreScalingResize);
            ((BottomBarTab) v).select(!mIgnoreScalingResize);

            shiftingMagic(oldTab, v, true);
            handleBackgroundColorChange((BottomBarTab) v);
        }
        updateSelectedTab(findItemPosition(v));
    }

    private void shiftingMagic(View oldTab, View newTab, boolean animate) {
        if (!mIsTabletMode && mIsShiftingMode && !mIgnoreShiftingResize) {
            if (oldTab instanceof FrameLayout) {
                // It's a badge, goddammit!
                oldTab = ((FrameLayout) oldTab).getChildAt(0);
            }

            if (newTab instanceof FrameLayout) {
                // It's a badge, goddammit!
                newTab = ((FrameLayout) newTab).getChildAt(0);
            }

            if (animate) {
                MiscUtils.resizeTab(oldTab, oldTab.getWidth(), mInActiveShiftingItemWidth);
                MiscUtils.resizeTab(newTab, newTab.getWidth(), mActiveShiftingItemWidth);
            } else {
                oldTab.getLayoutParams().width = mInActiveShiftingItemWidth;
                newTab.getLayoutParams().width = mActiveShiftingItemWidth;
            }
        }
    }

    private void updateSelectedTab(int newPosition) {
        int newTabId = mItems.get(newPosition).getId();

        if (newPosition != mCurrentTabPosition && mListener != null) {
            handleBadgeVisibility(mCurrentTabPosition, newPosition);
            mCurrentTabPosition = newPosition;
            mListener.onTabSelected(newTabId);
        } else if (mReselectionListener != null && !mIgnoreTabReselectionListener) {
            mReselectionListener.onTabReSelected(newTabId);
        }

        if (mIgnoreTabReselectionListener) {
            mIgnoreTabReselectionListener = false;
        }
    }

    private void handleBadgeVisibility(int oldPosition, int newPosition) {
        if (mBadgeMap == null) {
            return;
        }

        if (mBadgeMap.containsKey(oldPosition)) {
            BottomBarBadge oldBadge = (BottomBarBadge) mOuterContainer
                    .findViewWithTag(mBadgeMap.get(oldPosition));

            if (oldBadge.getAutoShowAfterUnSelection()) {
                oldBadge.show();
            } else {
                oldBadge.hide();
            }
        }

        if (mBadgeMap.containsKey(newPosition)) {
            BottomBarBadge newBadge = (BottomBarBadge) mOuterContainer
                    .findViewWithTag(mBadgeMap.get(newPosition));

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
        if ((mIsShiftingMode || mIsTabletMode) && v.getTag().equals(BottomBarTab.TAG_INACTIVE)) {
            Toast.makeText(mContext, mItems.get(findItemPosition(v)).getTitle(), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void newUpdateItems(final List<BottomBarTab> bottomBarItems) {
        if (mTabContainer == null) {
            initializeViews();
        }

        int index = 0;
        int biggestWidth = 0;

        mIsShiftingMode = mMaxFixedTabCount >= 0 && mMaxFixedTabCount < bottomBarItems.size();

        if (!mIsDarkTheme && !mIgnoreNightMode
                && MiscUtils.isNightMode(mContext)) {
            mIsDarkTheme = true;
        }

        if (mIsDarkTheme) {
            darkThemeMagic();
        } else if (!mIsTabletMode && mIsShiftingMode) {
            mDefaultBackgroundColor = mCurrentBackgroundColor = mPrimaryColor;

            if (mContext instanceof Activity) {
                // navBarMagic((Activity) mContext, this);
            }
        }

        View[] viewsToAdd = new View[bottomBarItems.size()];

        for (BottomBarTab bottomBarTab : bottomBarItems) {
            BottomBarTab.Type type;

            if (mIsShiftingMode && !mIsTabletMode) {
                type = BottomBarTab.Type.SHIFTING;
            } else if (mIsTabletMode) {
                type = BottomBarTab.Type.TABLET;
            } else {
                type = BottomBarTab.Type.FIXED;
            }

            bottomBarTab.setType(type);
            bottomBarTab.prepareLayout();

            if (!mIsTabletMode) {
                if (mPendingTextAppearance != -1) {
                    bottomBarTab.setTitleTextAppearance(mPendingTextAppearance);
                }

                if (mPendingTypeface != null) {
                    bottomBarTab.setTitleTypeface(mPendingTypeface);
                }
            }

            if (mIsDarkTheme || (!mIsTabletMode && mIsShiftingMode)) {
                bottomBarTab.setIconTint(mWhiteColor);
            }

            if (index == mCurrentTabPosition) {
                bottomBarTab.select(false);
            } else {
                bottomBarTab.deselect(false);
            }

            if (!mIsTabletMode) {
                if (bottomBarTab.getWidth() > biggestWidth) {
                    biggestWidth = bottomBarTab.getWidth();
                }

                viewsToAdd[index] = bottomBarTab;
            } else {
                mTabContainer.addView(bottomBarTab);
            }

            bottomBarTab.setOnClickListener(this);
            bottomBarTab.setOnLongClickListener(this);
            index++;
        }

        if (!mIsTabletMode) {
            int proposedItemWidth = Math.min(
                    MiscUtils.dpToPixel(mContext, mScreenWidth / bottomBarItems.size()),
                    mMaxFixedItemWidth
            );

            mInActiveShiftingItemWidth = (int) (proposedItemWidth * 0.9);
            mActiveShiftingItemWidth = (int) (proposedItemWidth + (proposedItemWidth * (bottomBarItems.size() * 0.1)));

            int height = Math.round(mContext.getResources().getDimension(R.dimen.bb_height));
            for (View bottomBarView : viewsToAdd) {
                LinearLayout.LayoutParams params;

                if (mIsShiftingMode && !mIgnoreShiftingResize) {
                    if (BottomBarTab.TAG_ACTIVE.equals(bottomBarView.getTag())) {
                        params = new LinearLayout.LayoutParams(mActiveShiftingItemWidth, height);
                    } else {
                        params = new LinearLayout.LayoutParams(mInActiveShiftingItemWidth, height);
                    }
                } else {
                    params = new LinearLayout.LayoutParams(proposedItemWidth, height);
                }

                bottomBarView.setLayoutParams(params);
                mTabContainer.addView(bottomBarView);
            }
        }

        if (mPendingTextAppearance != -1) {
            mPendingTextAppearance = -1;
        }

        if (mPendingTypeface != null) {
            mPendingTypeface = null;
        }
    }

    private void darkThemeMagic() {
        if (!mIsTabletMode) {
            mTabContainer.setBackgroundColor(mDarkBackgroundColor);
        } else {
            mTabContainer.setBackgroundColor(mDarkBackgroundColor);
            mTabletRightBorder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.bb_tabletRightBorderDark));
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
        if (mTabContainer == null) {
            return;
        }

        int childCount = mTabContainer.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View tab = mTabContainer.getChildAt(i);
            TextView title = (TextView) tab.findViewById(R.id.bb_bottom_bar_title);
            if (title == null) {
                continue;
            }
            int baseline = title.getBaseline();
            // Height already includes any possible top/bottom padding
            int height = title.getHeight();
            int paddingInsideTitle = height - baseline;
            int missingPadding = mTenDp - paddingInsideTitle;
            if (missingPadding > 0) {
                // Only update the padding if really needed
                title.setPadding(title.getPaddingLeft(), title.getPaddingTop(),
                    title.getPaddingRight(), missingPadding + title.getPaddingBottom());
            }
        }
    }

    private void handleBackgroundColorChange(BottomBarTab tab) {
        Integer newColor = tab.getBarColorWhenSelected();

        if (newColor == null) {
            return;
        }

        animateBGColorChange(tab,
                mOuterContainer,
                mBackgroundOverlay,
                newColor);
        mCurrentBackgroundColor = newColor;
    }

    private void animateBGColorChange(View clickedView, final View backgroundView,
                                final View bgOverlay, final int newColor) {
        int centerX = (int) (ViewCompat.getX(clickedView) + (clickedView.getMeasuredWidth() / 2));
        int centerY = clickedView.getMeasuredHeight() / 2;
        int finalRadius = backgroundView.getWidth();

        backgroundView.clearAnimation();
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
                    backgroundView.setBackgroundColor(newColor);
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
                    backgroundView.setBackgroundColor(newColor);
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

        for (int i = 0; i < mTabContainer.getChildCount(); i++) {
            View candidate = mTabContainer.getChildAt(i);

            if (candidate.equals(viewToFind)) {
                position = i;
                break;
            }
        }

        return position;
    }

    private void clearItems() {
        if (mTabContainer != null) {
            mTabContainer.removeAllViews();
        }

        if (mFragmentManager != null) {
            mFragmentManager = null;
        }

        if (mFragmentContainer != 0) {
            mFragmentContainer = 0;
        }

        if (mItems != null) {
            mItems = null;
        }
    }
}
