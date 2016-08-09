package com.roughike.bottombar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by iiro on 9.8.2016.
 */
class BackgroundColorAnimator {
    static void animateBGColorChange(View clickedView, final View backgroundView, final View bgOverlay, final int newColor) {
        prepareForAnimation(backgroundView, bgOverlay, newColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!bgOverlay.isAttachedToWindow()) {
                return;
            }

            showCircularRevealAnimation(
                    clickedView,
                    backgroundView,
                    bgOverlay,
                    newColor
            );
        } else {
            showCrossfadeAnimation(
                    backgroundView,
                    bgOverlay,
                    newColor
            );
        }
    }

    private static void prepareForAnimation(View backgroundView, View bgOverlay, int newColor) {
        backgroundView.clearAnimation();
        bgOverlay.clearAnimation();

        bgOverlay.setBackgroundColor(newColor);
        bgOverlay.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void showCircularRevealAnimation(View clickedView, final View backgroundView, final View bgOverlay, final int newColor) {
        int centerX = (int) (ViewCompat.getX(clickedView) + (clickedView.getMeasuredWidth() / 2));
        int centerY = clickedView.getMeasuredHeight() / 2;
        int startRadius = 0;
        int finalRadius = backgroundView.getWidth();

        Animator animator = ViewAnimationUtils.createCircularReveal(
                bgOverlay,
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
                backgroundView.setBackgroundColor(newColor);
                bgOverlay.setVisibility(View.INVISIBLE);
                ViewCompat.setAlpha(bgOverlay, 1);
            }
        });

        animator.start();
    }

    private static void showCrossfadeAnimation(final View backgroundView, final View bgOverlay, final int newColor) {
        ViewCompat.setAlpha(bgOverlay, 0);
        ViewCompat.animate(bgOverlay)
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
                        backgroundView.setBackgroundColor(newColor);
                        bgOverlay.setVisibility(View.INVISIBLE);
                        ViewCompat.setAlpha(bgOverlay, 1);
                    }
                }).start();
    }
}
