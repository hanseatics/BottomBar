package com.example.bottombar.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.roughike.bottombar.BottomBarLayout;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectedListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomBarLayout bottomBarLayout = (BottomBarLayout) findViewById(R.id.bottomBar);
        bottomBarLayout.setItems(
                new BottomBarTab(R.drawable.ic_recents, "Recents"),
                new BottomBarTab(R.drawable.ic_favorites, "Favorites"),
                new BottomBarTab(R.drawable.ic_nearby, "Nearby")
        );

        final TextView sampleText = (TextView) findViewById(R.id.sampleText);

        bottomBarLayout.setOnItemSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onItemSelected(final int position) {
                sampleText.animate()
                        .setDuration(150)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                sampleText.clearAnimation();
                                sampleText.setText("This would be screen number " + (position + 1));
                                sampleText.animate()
                                        .setDuration(150)
                                        .alpha(1)
                                        .start();
                            }
                        }).start();
            }
        });
    }
}
