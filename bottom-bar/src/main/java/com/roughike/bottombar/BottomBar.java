package com.roughike.bottombar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
public class BottomBar extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    private static final long ANIMATION_DURATION = 150;
    private static final int MAX_FIXED_TAB_COUNT = 3;

    private static final String STATE_CURRENT_SELECTED_TAB = "STATE_CURRENT_SELECTED_TAB";
    private static final String TAG_BOTTOM_BAR_VIEW_INACTIVE = "BOTTOM_BAR_VIEW_INACTIVE";
    private static final String TAG_BOTTOM_BAR_VIEW_ACTIVE = "BOTTOM_BAR_VIEW_ACTIVE";

    private Context mContext;
    private boolean mIsTabletMode;

    private FrameLayout mUserContentContainer;
    private LinearLayout mItemContainer;

    private int mPrimaryColor;
    private int mInActiveColor;
    private int mWhiteColor;

    private int mScreenWidth;
    private int mTwoDp;
    private int mTenDp;
    private int mMaxFixedItemWidth;

    private OnTabSelectedListener mListener;
    private OnMenuTabSelectedListener mMenuListener;

    private int mCurrentTabPosition;
    private boolean mIsShiftingMode;

    private FragmentManager mFragmentManager;
    private int mFragmentContainer;
    private BottomBarItemBase[] mItems;

    public BottomBar(Context context) {
        this(context, null, 0, 0);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;

        mPrimaryColor = MiscUtils.getColor(getContext(), R.attr.colorPrimary);
        mInActiveColor = ContextCompat.getColor(getContext(), R.color.bb_inActiveBottomBarItemColor);
        mWhiteColor = ContextCompat.getColor(getContext(), R.color.white);

        mScreenWidth = MiscUtils.getScreenWidth(mContext);
        mTwoDp = MiscUtils.dpToPixel(mContext, 2);
        mTenDp = MiscUtils.dpToPixel(mContext, 10);
        mMaxFixedItemWidth = MiscUtils.dpToPixel(mContext, 168);

        initializeViews();
    }

    private void initializeViews() {
        ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        View rootView = View.inflate(mContext,
                R.layout.bb_bottom_bar_item_container, null);

        mIsTabletMode = rootView.findViewById(R.id.bb_tablet_right_border) != null;
        mUserContentContainer = (FrameLayout) rootView.findViewById(R.id.bb_user_content_container);
        mItemContainer = (LinearLayout) rootView.findViewById(R.id.bb_bottom_bar_item_container);

        addView(rootView, params);
    }

    protected FrameLayout getUserContainer() {
        return mUserContentContainer;
    }

    /**
     * Bind the BottomBar to your Activity, and inflate your layout here.
     * <p/>
     * Remember to also call {@link #onRestoreInstanceState(Bundle)} inside
     * of your {@link Activity#onSaveInstanceState(Bundle)} to restore the state.
     *
     * @param activity           an Activity to attach to.
     * @param savedInstanceState a Bundle for restoring the state on configuration change.
     * @return a BottomBar at the bottom of the screen.
     */
    public static BottomBar attach(Activity activity, Bundle savedInstanceState) {
        BottomBar bottomBar = new BottomBar(activity);
        bottomBar.onRestoreInstanceState(savedInstanceState);

        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View oldLayout = contentView.getChildAt(0);
        contentView.removeView(oldLayout);

        bottomBar.getUserContainer()
                .addView(oldLayout, oldLayout.getLayoutParams());
        contentView.addView(bottomBar, 0);

        return bottomBar;
    }

    /**
     * Bind the BottomBar to the specified View's parent, and inflate
     * your layout there. Useful when the BottomBar overlaps some content
     * that shouldn't be overlapped.
     * <p/>
     * Remember to also call {@link #onRestoreInstanceState(Bundle)} inside
     * of your {@link Activity#onSaveInstanceState(Bundle)} to restore the state.
     *
     * @param view               a View, which parent we're going to attach to.
     * @param savedInstanceState a Bundle for restoring the state on configuration change.
     * @return a BottomBar at the bottom of the screen.
     */
    public static BottomBar attach(View view, Bundle savedInstanceState) {
        BottomBar bottomBar = new BottomBar(view.getContext());
        bottomBar.onRestoreInstanceState(savedInstanceState);

        ViewGroup contentView = (ViewGroup) view.getParent();

        if (contentView != null) {
            View oldLayout = contentView.getChildAt(0);
            contentView.removeView(oldLayout);

            bottomBar.getUserContainer()
                    .addView(oldLayout, oldLayout.getLayoutParams());
            contentView.addView(bottomBar, 0);
        } else {
            bottomBar.getUserContainer()
                    .addView(view, view.getLayoutParams());
        }

        return bottomBar;
    }

    /**
     * Set tabs and fragments for this BottomBar. When setting more than 3 items,
     * only the icons will show by default, but the selected item
     * will have the text visible.
     *
     * @param fragmentManager   a FragmentManager for managing the Fragments.
     * @param containerResource id for the layout to inflate Fragments to.
     * @param fragmentItems     an array of {@link BottomBarFragment} objects.
     */
    public void setFragmentItems(FragmentManager fragmentManager, @IdRes int containerResource,
                                 BottomBarFragment... fragmentItems) {
        clearItems();
        mFragmentManager = fragmentManager;
        mFragmentContainer = containerResource;
        mItems = fragmentItems;
        updateItems(mItems);
    }

    /**
     * Set tabs for this BottomBar. When setting more than 3 items,
     * only the icons will show by default, but the selected item
     * will have the text visible.
     *
     * @param bottomBarTabs an array of {@link BottomBarTab} objects.
     */
    public void setItems(BottomBarTab... bottomBarTabs) {
        clearItems();
        mItems = bottomBarTabs;
        updateItems(mItems);
    }

    /**
     * Set items from an XML menu resource file.
     *
     * @param menuRes  the menu resource to inflate items from.
     * @param listener listener for tab change events.
     */
    public void setItemsFromMenu(@MenuRes int menuRes, OnMenuTabSelectedListener listener) {
        clearItems();
        mItems = MiscUtils.inflateMenuFromResource((Activity) getContext(), menuRes);
        mMenuListener = listener;
        updateItems(mItems);
    }

    /**
     * Set a listener that gets fired when the selected tab changes.
     *
     * @param listener a listener for monitoring changes in tab selection.
     */
    public void setOnItemSelectedListener(OnTabSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Select a tab at the specified position.
     *
     * @param position the position to select.
     */
    public void selectTabAtPosition(int position, boolean animate) {
        unselectTab((ViewGroup) mItemContainer.findViewWithTag(TAG_BOTTOM_BAR_VIEW_ACTIVE), animate);
        selectTab((ViewGroup) mItemContainer.getChildAt(position), animate);

        if (mListener != null) {
            mListener.onItemSelected(position);
        }
    }

    /**
     * Call this method in your Activity's onSaveInstanceState
     * to keep the BottomBar's state on configuration change.
     *
     * @param outState the Bundle to save data to.
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_SELECTED_TAB, mCurrentTabPosition);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag().equals(TAG_BOTTOM_BAR_VIEW_INACTIVE)) {
            unselectTab(findViewWithTag(TAG_BOTTOM_BAR_VIEW_ACTIVE), true);
            selectTab(v, true);
            updateSelectedTab(findItemPosition(v));
        }
    }

    private void updateSelectedTab(int newPosition) {
        if (newPosition != mCurrentTabPosition) {
            mCurrentTabPosition = newPosition;

            if (mListener != null) {
                mListener.onItemSelected(mCurrentTabPosition);
            }

            if (mMenuListener != null && mItems instanceof BottomBarTab[]) {
                mMenuListener.onMenuItemSelected(((BottomBarTab) mItems[mCurrentTabPosition]).id);
            }

            updateCurrentFragment();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if ((mIsShiftingMode || mIsTabletMode) && v.getTag().equals(TAG_BOTTOM_BAR_VIEW_INACTIVE)) {
            Toast.makeText(mContext, mItems[findItemPosition(v)].getTitle(mContext), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void updateItems(BottomBarItemBase[] bottomBarItems) {
        int index = 0;
        int biggestWidth = 0;
        mIsShiftingMode = MAX_FIXED_TAB_COUNT < bottomBarItems.length;

        if (!mIsTabletMode && mIsShiftingMode) {
            mItemContainer.setBackgroundColor(mPrimaryColor);
        }

        View[] viewsToAdd = new View[bottomBarItems.length];

        for (BottomBarItemBase bottomBarItemBase : bottomBarItems) {
            int layoutResource;

            if (mIsShiftingMode && !mIsTabletMode) {
                layoutResource = R.layout.bb_bottom_bar_item_shifting;
            } else {
                layoutResource = R.layout.bb_bottom_bar_item_fixed;
            }

            View bottomBarView = View.inflate(mContext, layoutResource, null);
            ImageView icon = (ImageView) bottomBarView.findViewById(R.id.bb_bottom_bar_icon);

            icon.setImageDrawable(bottomBarItemBase.getIcon(mContext));

            if (!mIsTabletMode) {
                TextView title = (TextView) bottomBarView.findViewById(R.id.bb_bottom_bar_title);
                title.setText(bottomBarItemBase.getTitle(mContext));
            }

            if (!mIsTabletMode && mIsShiftingMode) {
                icon.setColorFilter(mWhiteColor);
            }

            if (bottomBarItemBase instanceof BottomBarTab) {
                bottomBarView.setId(((BottomBarTab) bottomBarItemBase).id);
            }

            if (index == mCurrentTabPosition) {
                selectTab(bottomBarView, false);
            } else {
                unselectTab(bottomBarView, false);
            }

            if (!mIsTabletMode) {
                if (bottomBarView.getWidth() > biggestWidth) {
                    biggestWidth = bottomBarView.getWidth();
                }

                viewsToAdd[index] = bottomBarView;
            } else {
                mItemContainer.addView(bottomBarView);
            }

            bottomBarView.setOnClickListener(this);
            bottomBarView.setOnLongClickListener(this);
            index++;
        }

        if (!mIsTabletMode) {
            int proposedItemWidth = Math.min(
                    MiscUtils.dpToPixel(mContext, mScreenWidth / bottomBarItems.length),
                    mMaxFixedItemWidth
            );

            LinearLayout.LayoutParams params = new LinearLayout
                    .LayoutParams(proposedItemWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

            for (View bottomBarView : viewsToAdd) {
                bottomBarView.setLayoutParams(params);
                mItemContainer.addView(bottomBarView);
            }
        }

        updateCurrentFragment();
    }

    private void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentTabPosition = savedInstanceState.getInt(STATE_CURRENT_SELECTED_TAB, -1);

            if (mCurrentTabPosition == -1) {
                mCurrentTabPosition = 0;
                Log.e("BottomBar", "You must override the Activity's onSave" +
                        "InstanceState(Bundle outState) and call BottomBar.onSaveInstanc" +
                        "eState(outState) there to restore the state properly.");
            }
        }
    }

    private void selectTab(View bottomBarView, boolean animate) {
        bottomBarView.setTag(TAG_BOTTOM_BAR_VIEW_ACTIVE);
        ImageView icon = (ImageView) bottomBarView.findViewById(R.id.bb_bottom_bar_icon);
        TextView title = (TextView) bottomBarView.findViewById(R.id.bb_bottom_bar_title);

        if (!mIsShiftingMode || mIsTabletMode) {
            icon.setColorFilter(mPrimaryColor);

            if (title != null) {
                title.setTextColor(mPrimaryColor);
            }
        }

        if (title == null) {
            return;
        }

        int translationY = mIsShiftingMode ? mTenDp : mTwoDp;

        if (animate) {
            title.animate()
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(1)
                    .scaleY(1)
                    .start();
            bottomBarView.animate()
                    .setDuration(ANIMATION_DURATION)
                    .translationY(-translationY)
                    .start();

            if (mIsShiftingMode) {
                icon.animate()
                        .setDuration(ANIMATION_DURATION)
                        .alpha(1.0f)
                        .start();
            }
        } else {
            title.setScaleX(1);
            title.setScaleY(1);
            bottomBarView.setTranslationY(-translationY);

            if (mIsShiftingMode) {
                icon.setAlpha(1.0f);
            }
        }
    }

    private void unselectTab(View bottomBarView, boolean animate) {
        bottomBarView.setTag(TAG_BOTTOM_BAR_VIEW_INACTIVE);

        ImageView icon = (ImageView) bottomBarView.findViewById(R.id.bb_bottom_bar_icon);
        TextView title = (TextView) bottomBarView.findViewById(R.id.bb_bottom_bar_title);

        if (!mIsShiftingMode || mIsTabletMode) {
            icon.setColorFilter(mInActiveColor);

            if (title != null) {
                title.setTextColor(mInActiveColor);
            }
        }

        if (title == null) {
            return;
        }

        float scale = mIsShiftingMode ? 0 : 0.86f;

        if (animate) {
            title.animate()
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(scale)
                    .scaleY(scale)
                    .start();
            bottomBarView.animate()
                    .setDuration(ANIMATION_DURATION)
                    .translationY(0)
                    .start();

            if (mIsShiftingMode && !mIsTabletMode) {
                icon.animate()
                        .setDuration(ANIMATION_DURATION)
                        .alpha(0.6f)
                        .start();
            }
        } else {
            title.setScaleX(scale);
            title.setScaleY(scale);
            bottomBarView.setTranslationY(0);

            if (mIsShiftingMode && !mIsTabletMode) {
                icon.setAlpha(0.6f);
            }
        }
    }

    private int findItemPosition(View viewToFind) {
        int position = 0;

        for (int i = 0; i < mItemContainer.getChildCount(); i++) {
            View candidate = mItemContainer.getChildAt(i);

            if (candidate.equals(viewToFind)) {
                position = i;
                break;
            }
        }

        return position;
    }

    private void updateCurrentFragment() {
        if (mFragmentManager != null
                && mFragmentContainer != 0
                && mItems != null
                && mItems instanceof BottomBarFragment[]) {
            mFragmentManager.beginTransaction()
                    .replace(mFragmentContainer, ((BottomBarFragment) mItems[mCurrentTabPosition]).getFragment())
                    .commit();
        }
    }

    private void clearItems() {
        int childCount = mItemContainer.getChildCount();

        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                mItemContainer.removeView(mItemContainer.getChildAt(i));
            }
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
