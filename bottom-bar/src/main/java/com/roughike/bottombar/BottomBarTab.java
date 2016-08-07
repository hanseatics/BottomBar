package com.roughike.bottombar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
public class BottomBarTab extends LinearLayout {
    private static final long ANIMATION_DURATION = 150;
    private static final float ACTIVE_TITLE_SCALE = 1;
    private static final float INACTIVE_FIXED_TITLE_SCALE = 0.86f;

    private final int sixDps;
    private final int eightDps;
    private final int sixteenDps;

    private Type type = Type.FIXED;
    private int iconResId;
    private String title;

    private float inActiveAlpha;
    private float activeAlpha;
    private int inActiveColor;
    private int activeColor;
    private int barColorWhenSelected;
    private int badgeBackgroundColor;

    private AppCompatImageView iconView;
    private TextView titleView;
    private boolean isActive;

    private BottomBarBadge badge;
    private int indexInContainer;

    public View getOuterView() {
        return (View) getParent();
    }

    enum Type {
        FIXED, SHIFTING, TABLET
    }

    BottomBarTab(Context context) {
        super(context);

        sixDps = MiscUtils.dpToPixel(context, 6);
        eightDps = MiscUtils.dpToPixel(context, 8);
        sixteenDps = MiscUtils.dpToPixel(context, 16);
    }

    Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }

    int getIconResId() {
        return iconResId;
    }

    void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    float getInActiveAlpha() {
        return inActiveAlpha;
    }

    void setInActiveAlpha(float inActiveAlpha) {
        this.inActiveAlpha = inActiveAlpha;
    }

    float getActiveAlpha() {
        return activeAlpha;
    }

    void setActiveAlpha(float activeAlpha) {
        this.activeAlpha = activeAlpha;
    }

    int getInActiveColor() {
        return inActiveColor;
    }

    void setInActiveColor(int inActiveColor) {
        this.inActiveColor = inActiveColor;
    }

    int getActiveColor() {
        return activeColor;
    }

    void setActiveColor(int activeIconColor) {
        this.activeColor = activeIconColor;
    }

    int getBarColorWhenSelected() {
        return barColorWhenSelected;
    }

    void setBarColorWhenSelected(int barColorWhenSelected) {
        this.barColorWhenSelected = barColorWhenSelected;
    }

    int getBadgeBackgroundColor() {
        return badgeBackgroundColor;
    }

    void setBadgeBackgroundColor(int badgeBackgroundColor) {
        this.badgeBackgroundColor = badgeBackgroundColor;
    }

    public void setBadgeCount(int count) {
        if (badge == null) {
            badge = new BottomBarBadge(getContext());
            badge.attachToTab(this, badgeBackgroundColor);
        }

        badge.setCount(count);
    }

    boolean isActive() {
        return isActive;
    }

    boolean hasActiveBadge() {
        return badge != null;
    }

    int getIndexInContainer() {
        return indexInContainer;
    }

    void setIndexInContainer(int indexInContainer) {
        this.indexInContainer = indexInContainer;
    }

    void prepareLayout() {
        int layoutResource;

        switch (type) {
            case FIXED:
                layoutResource = R.layout.bb_bottom_bar_item_fixed;
                break;
            case SHIFTING:
                layoutResource = R.layout.bb_bottom_bar_item_shifting;
                break;
            case TABLET:
                layoutResource = R.layout.bb_bottom_bar_item_fixed_tablet;
                break;
            default:
                // should never happen
                throw new RuntimeException("Unknown BottomBarTab type.");
        }

        inflate(getContext(), layoutResource, this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        iconView = (AppCompatImageView) findViewById(R.id.bb_bottom_bar_icon);
        titleView = (TextView) findViewById(R.id.bb_bottom_bar_title);

        iconView.setImageResource(iconResId);
        titleView.setText(title);
    }

    void setIconTint(int tint) {
        iconView.setColorFilter(tint);
    }

    @SuppressWarnings("deprecation")
    void setTitleTextAppearance(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleView.setTextAppearance(resId);
        } else {
            titleView.setTextAppearance(getContext(), resId);
        }
    }

    void setTitleTypeface(Typeface typeface) {
        titleView.setTypeface(typeface);
    }

    void select(boolean animate) {
        isActive = true;

        boolean isShifting = type == Type.SHIFTING;

        if (!isShifting) {
            setColors(activeColor);
        }

        if (animate) {
            setTopPaddingAnimated(iconView.getPaddingTop(), sixDps);
            animateIcon(activeAlpha);
            animateTitle(ACTIVE_TITLE_SCALE, activeAlpha);
        } else {
            setTitleScale(ACTIVE_TITLE_SCALE);
            setTopPadding(sixDps);

            if (isShifting) {
                ViewCompat.setAlpha(iconView, activeAlpha);
                ViewCompat.setAlpha(titleView, activeAlpha);
            }
        }

        if (badge != null) {
            badge.hide();
        }
    }

    void deselect(boolean animate) {
        isActive = false;

        boolean isShifting = type == Type.SHIFTING;

        if (!isShifting) {
            setColors(inActiveColor);
        }

        float scale = isShifting ? 0 : INACTIVE_FIXED_TITLE_SCALE;
        int iconPaddingTop = isShifting ? sixteenDps : eightDps;

        if (animate) {
            setTopPaddingAnimated(iconView.getPaddingTop(), iconPaddingTop);
            animateTitle(scale, 0);

            if (isShifting) {
                animateIcon(inActiveAlpha);
            }
        } else {
            setTitleScale(scale);
            setTopPadding(iconPaddingTop);

            if (isShifting) {
                ViewCompat.setAlpha(iconView, inActiveAlpha);
                ViewCompat.setAlpha(titleView, 0);
            }
        }

        if (badge != null) {
            badge.show();
        }
    }

    private void setColors(int color) {
        iconView.setColorFilter(color);

        if (titleView != null) {
            titleView.setTextColor(color);
        }
    }

    void updateWidthAnimated(float endWidth) {
        float start = getWidth();

        ValueAnimator animator = ValueAnimator.ofFloat(start, endWidth);
        animator.setDuration(150);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ViewGroup.LayoutParams params = getLayoutParams();
                if (params == null) return;

                params.width = Math.round((float) animator.getAnimatedValue());
                setLayoutParams(params);
            }
        });
        animator.start();
    }

    private void setTopPaddingAnimated(int start, int end) {
        ValueAnimator paddingAnimator = ValueAnimator.ofInt(start, end);
        paddingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iconView.setPadding(
                        iconView.getPaddingLeft(),
                        (Integer) animation.getAnimatedValue(),
                        iconView.getPaddingRight(),
                        iconView.getPaddingBottom()
                );
            }
        });

        paddingAnimator.setDuration(ANIMATION_DURATION);
        paddingAnimator.start();
    }

    private void animateTitle(float finalScale, float finalAlpha) {
        ViewPropertyAnimatorCompat titleAnimator = ViewCompat.animate(titleView)
                .setDuration(ANIMATION_DURATION)
                .scaleX(finalScale)
                .scaleY(finalScale);

        if (type == Type.SHIFTING) {
            titleAnimator.alpha(finalAlpha);
        }

        titleAnimator.start();
    }

    private void animateIcon(float finalAlpha) {
        ViewCompat.animate(iconView)
                .setDuration(ANIMATION_DURATION)
                .alpha(finalAlpha)
                .start();
    }

    private void setTopPadding(int topPadding) {
        iconView.setPadding(
                iconView.getPaddingLeft(),
                topPadding,
                iconView.getPaddingRight(),
                iconView.getPaddingBottom()
        );
    }

    private void setTitleScale(float scale) {
        ViewCompat.setScaleX(titleView, scale);
        ViewCompat.setScaleY(titleView, scale);
    }
}
