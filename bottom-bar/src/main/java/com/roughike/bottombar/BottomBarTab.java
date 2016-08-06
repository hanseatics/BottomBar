package com.roughike.bottombar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
    public static final String TAG_ACTIVE = "TAB_ACTIVE";
    public static final String TAG_INACTIVE = "TAB_INACTIVE";

    private static final long ANIMATION_DURATION = 150;
    private static final float INACTIVE_ALPHA = 0.6f;

    private final int sixDps;
    private final int eightDps;
    private final int sixteenDps;
    private final int primaryColor;

    private Type type = Type.FIXED;
    private int iconResId;
    private String title;

    private int inActiveColor;
    private int activeColor;
    private Integer barColorWhenSelected;

    private AppCompatImageView iconView;
    private TextView titleView;
    private boolean isSelected;

    public enum Type {
        FIXED, SHIFTING, TABLET
    }

    public BottomBarTab(Context context) {
        super(context);

        sixDps = MiscUtils.dpToPixel(context, 6);
        eightDps = MiscUtils.dpToPixel(context, 8);
        sixteenDps = MiscUtils.dpToPixel(context, 16);
        primaryColor = MiscUtils.getColor(context, R.attr.colorPrimary);

        inActiveColor = ContextCompat.getColor(context, R.color.bb_inActiveBottomBarItemColor);
        activeColor = primaryColor;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getInActiveColor() {
        return inActiveColor;
    }

    public void setInActiveColor(int inActiveColor) {
        this.inActiveColor = inActiveColor;
    }

    public int getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(int activeIconColor) {
        this.activeColor = activeIconColor;
    }

    @Nullable
    public Integer getBarColorWhenSelected() {
        return barColorWhenSelected;
    }

    public void setBarColorWhenSelected(Integer barColorWhenSelected) {
        this.barColorWhenSelected = barColorWhenSelected;
    }

    public void prepareLayout() {
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

    public void setIconTint(int tint) {
        iconView.setColorFilter(tint);
    }

    @SuppressWarnings("deprecation")
    public void setTitleTextAppearance(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleView.setTextAppearance(resId);
        } else {
            titleView.setTextAppearance(getContext(), resId);
        }
    }

    public void setTitleTypeface(Typeface typeface) {
        titleView.setTypeface(typeface);
    }

    public void select(boolean animate) {
        setTag(TAG_ACTIVE);
        isSelected = true;

        boolean isShifting = type == Type.SHIFTING;

        if (!isShifting) {
            setColors(activeColor);
        }

        if (animate) {
            setTopPaddingAnimated(iconView.getPaddingTop(), sixDps);
            animateIcon(1);
            animateTitle(1, 1);
        } else {
            setTitleScale(1);
            setTopPadding(sixDps);

            if (isShifting) {
                ViewCompat.setAlpha(iconView, 1);
                ViewCompat.setAlpha(titleView, 1);
            }
        }
    }

    public void deselect(boolean animate) {
        isSelected = false;
        setTag(TAG_INACTIVE);

        boolean isShifting = type == Type.SHIFTING;

        if (!isShifting) {
            setColors(inActiveColor);
        }

        float scale = isShifting ? 0 : 0.86f;
        int iconPaddingTop = isShifting ? sixteenDps : eightDps;

        if (animate) {
            setTopPaddingAnimated(iconView.getPaddingTop(), iconPaddingTop);
            animateTitle(scale, 0);

            if (isShifting) {
                animateIcon(INACTIVE_ALPHA);
            }
        } else {
            setTitleScale(scale);
            setTopPadding(iconPaddingTop);

            if (isShifting) {
                ViewCompat.setAlpha(iconView, INACTIVE_ALPHA);
                ViewCompat.setAlpha(titleView, 0);
            }
        }
    }

    private void setColors(int color) {
        iconView.setColorFilter(color);

        if (titleView != null) {
            titleView.setTextColor(color);
        }
    }

    public void updateWidthAnimated(float endWidth) {
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
