package com.roughike.bottombar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Iiro Krankka (http://github.com/roughike)
 */
public class BottomBarLayout extends RelativeLayout implements View.OnClickListener {
    private static final long ANIMATION_DURATION = 150;

    private static final String TAG_BOTTOM_BAR_VIEW_INACTIVE = "BOTTOM_BAR_VIEW_INACTIVE";
    private static final String TAG_BOTTOM_BAR_VIEW_ACTIVE = "BOTTOM_BAR_VIEW_ACTIVE";

    private Context mContext;

    private LinearLayout mItemContainer;

    private int mPrimaryColor;
    private int mInActiveColor;

    private int mTwoDp;
    private int mMinItemWidth;
    private int mMaxItemWidth;

    private OnBarItemSelectedListener mListener;

    public BottomBarLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BottomBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BottomBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomBarLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        mPrimaryColor = MiscUtils.getColor(mContext, R.attr.colorPrimary);
        mInActiveColor = ContextCompat.getColor(mContext, R.color.bb_inActiveBottomBarItemColor);

        mTwoDp = MiscUtils.dpToPixel(mContext, 2);
        mMinItemWidth = MiscUtils.dpToPixel(mContext, 104);
        mMaxItemWidth = MiscUtils.dpToPixel(mContext, 168);

        initializeViews();
    }

    private void initializeViews() {
        ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        setLayoutParams(params);

        RelativeLayout.LayoutParams containerParams = new RelativeLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);

        FrameLayout itemContainerRoot = (FrameLayout) View.inflate(mContext,
                R.layout.bb_bottom_bar_item_container, null);
        mItemContainer = (LinearLayout) itemContainerRoot.findViewById(R.id.bb_bottom_bar_item_container);
        addView(itemContainerRoot, containerParams);

        RelativeLayout.LayoutParams childParams = (LayoutParams) getChildAt(0).getLayoutParams();
        childParams.addRule(ABOVE, R.id.bb_bottom_bar_item_container);
    }

    public void setItems(BottomBarItem... bottomBarItems) {
        clearItems();

        int index = 0;
        int biggestWidth = 0;

        View[] viewsToAdd = new View[bottomBarItems.length];

        for (BottomBarItem bottomBarItem : bottomBarItems) {
            ViewGroup bottomBarView = (ViewGroup) View.inflate(mContext, R.layout.bb_bottom_bar_item, null);

            ImageView icon = (ImageView) bottomBarView.findViewById(R.id.bottom_bar_icon);
            TextView title = (TextView) bottomBarView.findViewById(R.id.bottom_bar_title);

            icon.setImageDrawable(bottomBarItem.getIcon(mContext));
            title.setText(bottomBarItem.getTitle(mContext));
            MiscUtils.setTextAppearance(title, R.style.BB_BottomBarItem_Fixed_Title);

            if (index == 0) {
                activateView(bottomBarView, false);
            } else {
                inActivateView(bottomBarView, false);
            }

            if (bottomBarView.getWidth() > biggestWidth) {
                biggestWidth = bottomBarView.getWidth();
            }

            bottomBarView.setOnClickListener(this);
            viewsToAdd[index] = bottomBarView;
            index++;
        }

        int screenWidth = MiscUtils.getScreenWidth(mContext);
        int proposedItemWidth = Math.min(
                MiscUtils.dpToPixel(mContext, screenWidth / bottomBarItems.length),
                mMaxItemWidth
        );

        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(proposedItemWidth, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (View bottomBarView : viewsToAdd) {
            bottomBarView.setLayoutParams(params);
            mItemContainer.addView(bottomBarView);
        }
    }

    public void setOnItemSelectedListener(OnBarItemSelectedListener listener) {
        mListener = listener;
    }

    private void activateView(ViewGroup bottomBarView, boolean animate) {
        bottomBarView.setTag(TAG_BOTTOM_BAR_VIEW_ACTIVE);

        ((ImageView) bottomBarView.findViewById(R.id.bottom_bar_icon)).setColorFilter(mPrimaryColor);
        TextView title = (TextView) bottomBarView.findViewById(R.id.bottom_bar_title);
        title.setTextColor(mPrimaryColor);

        if (animate) {
            title.animate()
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(1)
                    .scaleY(1)
                    .start();
            bottomBarView.animate()
                    .setDuration(ANIMATION_DURATION)
                    .translationY(-mTwoDp)
                    .start();
        } else {
            title.setScaleX(1);
            title.setScaleY(1);
            bottomBarView.setTranslationY(-mTwoDp);
        }
    }

    private void inActivateView(ViewGroup bottomBarView, boolean animate) {
        bottomBarView.setTag(TAG_BOTTOM_BAR_VIEW_INACTIVE);

        ((ImageView) bottomBarView.findViewById(R.id.bottom_bar_icon)).setColorFilter(mInActiveColor);
        TextView title = (TextView) bottomBarView.findViewById(R.id.bottom_bar_title);
        title.setTextColor(mInActiveColor);

        if (animate) {
            title.animate()
                    .setDuration(ANIMATION_DURATION)
                    .scaleX(0.86f)
                    .scaleY(0.86f)
                    .start();
            bottomBarView.animate()
                    .setDuration(ANIMATION_DURATION)
                    .translationY(0)
                    .start();
        } else {
            title.setScaleX(0.86f);
            title.setScaleY(0.86f);
            bottomBarView.setTranslationY(0);
        }
    }

    private void clearItems() {
        View bottomBarView = findViewWithTag(TAG_BOTTOM_BAR_VIEW_INACTIVE);

        while (bottomBarView != null) {
            removeView(bottomBarView);
            bottomBarView = findViewWithTag(TAG_BOTTOM_BAR_VIEW_INACTIVE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag().equals(TAG_BOTTOM_BAR_VIEW_INACTIVE)) {
            inActivateView((ViewGroup) findViewWithTag(TAG_BOTTOM_BAR_VIEW_ACTIVE), true);
            activateView((ViewGroup) v, true);

            if (mListener != null) {
                int position = 0;

                for (int i = 0; i < mItemContainer.getChildCount(); i++) {
                    View candidate = mItemContainer.getChildAt(i);

                    if (candidate.getTag().equals(TAG_BOTTOM_BAR_VIEW_ACTIVE)) {
                        position = i;
                        break;
                    }
                }

                mListener.onItemSelected(position);
            }
        }
    }
}
