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

package com.example.bottombar.sample;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarFragment;
import com.roughike.bottombar.BottomBarTab;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentWithBottomBars extends Fragment {

    BottomBar mBottomBar;


    public FragmentWithBottomBars() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_with_bottom_bars, container, false);
        // initialize your views here

        mBottomBar = BottomBar.attach(view, savedInstanceState);
        mBottomBar.setFragmentItems(getChildFragmentManager(), R.id.container,
                new BottomBarFragment(SampleFragment.newInstance("Content for recents."), R.drawable.ic_recents, "Recents"),
                new BottomBarFragment(SampleFragment.newInstance("Content for favorites."), R.drawable.ic_favorites, "Favorites"),
                new BottomBarFragment(SampleFragment.newInstance("Content for nearby stuff."), R.drawable.ic_nearby, "Nearby"),
                new BottomBarFragment(SampleFragment.newInstance("Content for friends."), R.drawable.ic_friends, "Friends"),
                new BottomBarFragment(SampleFragment.newInstance("Content for food."), R.drawable.ic_restaurants, "Food"));

        // Important! Don't return the view here. Instead, return the bottomBar, as it already contains your view.
        return mBottomBar;

//        bottomBar.setItems(
//                new BottomBarTab(R.drawable.ic_recents, "Recents"),
//                new BottomBarTab(R.drawable.ic_favorites, "Favorites"),
//                new BottomBarTab(R.drawable.ic_nearby, "Nearby")
//        );
//
//        // Important! Don't return the view here. Instead, return the bottomBar, as it already contains your view.
//        return bottomBar;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
