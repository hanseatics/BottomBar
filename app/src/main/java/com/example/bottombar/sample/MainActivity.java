package com.example.bottombar.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarFragment;

public class MainActivity extends AppCompatActivity {
    private BottomBar mBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notice how you don't use the setContentView method here! Just
        // pass your layout to bottom bar, it will be taken care of.
        // Everything will be just like you're used to.
        mBottomBar = BottomBar.bind(this, R.layout.activity_main,
                savedInstanceState);

        mBottomBar.setFragmentItems(
                getSupportFragmentManager(),
                R.id.fragmentContainer,
                new BottomBarFragment(SampleFragment.newInstance("Content for recents."), R.drawable.ic_recents, "Recents"),
                new BottomBarFragment(SampleFragment.newInstance("Content for favorites."), R.drawable.ic_favorites, "Favorites"),
                new BottomBarFragment(SampleFragment.newInstance("Content for nearby stuff."), R.drawable.ic_nearby, "Nearby"),
                new BottomBarFragment(SampleFragment.newInstance("Content for friends."), R.drawable.ic_friends, "Friends"),
                new BottomBarFragment(SampleFragment.newInstance("Content for food."), R.drawable.ic_restaurants, "Food")
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mBottomBar.onSaveInstanceState(outState);
    }
}
