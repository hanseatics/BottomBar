package com.example.bottombar.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBarLayout;
import com.roughike.bottombar.BottomBarItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomBarLayout bottomBarLayout = (BottomBarLayout) findViewById(R.id.bottomBar);
        bottomBarLayout.setItems(
                new BottomBarItem(R.drawable.ic_recents, "Recents"),
                new BottomBarItem(R.drawable.ic_favorites, "Favorites"),
                new BottomBarItem(R.drawable.ic_nearby, "Nearby")
        );
    }
}
