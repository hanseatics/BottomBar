package com.example.bottombar.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarBadge;
import com.roughike.bottombar.OnTabClickListener;

/**
 * Created by iiro on 7.6.2016.
 */
public class BadgeActivity extends AppCompatActivity {
    private BottomBar mBottomBar;
    private TextView mMessageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inflated_in_xml);

        mMessageView = (TextView) findViewById(R.id.messageView);

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.setItems(R.xml.bottombar_tabs_three);
        mBottomBar.setOnTabClickListener(new OnTabClickListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                mMessageView.setText(TabMessage.get(tabId, false));
            }

            @Override
            public void onTabReSelected(@IdRes int tabId) {
                Toast.makeText(getApplicationContext(), TabMessage.get(tabId, true), Toast.LENGTH_SHORT).show();
            }
        });

        int redColor = Color.parseColor("#FF0000");

        // We want the nearbyBadge to be always shown, except when the Favorites tab is selected.
        BottomBarBadge nearbyBadge = mBottomBar.makeBadgeForTabAt(1, redColor, 5);
        nearbyBadge.setAutoShowAfterUnSelection(true);
    }
}
