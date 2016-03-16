# BottomBar
<img src="https://raw.githubusercontent.com/roughike/BottomBar/master/demo1.gif" width="278" height="492" /> <img src="https://raw.githubusercontent.com/roughike/BottomBar/master/demo2.gif" width="278" height="492" />

## What?

A custom view component that mimicks the new [Material Design Bottom Navigation pattern](https://www.google.com/design/spec/components/bottom-navigation.html#bottom-navigation-specs).

**(currently under development, expect to see changes during this week)**

## minSDK version

The current minSDK version is API level 14.

## Gimme that Gradle sweetness, pls?

It's pending. I'll update right away when it's available.

## How?

BottomBar likes Fragments very much, but you can also handle your tab changes by yourself.

**Handling tab changes yourself:**

```java
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
    
        mBottomBar.setItems(
                new BottomBarTab(R.drawable.ic_recents, "Recents"),
                new BottomBarTab(R.drawable.ic_favorites, "Favorites"),
                new BottomBarTab(R.drawable.ic_nearby, "Nearby")
        );
    
        mBottomBar.setOnItemSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onItemSelected(final int position) {
                // the user selected a new tab
            }
        });
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mBottomBar.onSaveInstanceState(outState);
    }
}
```

**Working with Fragments**

Just call ```setFragmentItems()``` instead of ```setItems()```:

```java
mBottomBar.setFragmentItems(
    getSupportFragmentManager(),
    R.id.fragmentContainer,
    new BottomBarFragment(SampleFragment.newInstance("Content for recents."), R.drawable.ic_recents, "Recents"),
    new BottomBarFragment(SampleFragment.newInstance("Content for favorites."), R.drawable.ic_favorites, "Favorites"),
    new BottomBarFragment(SampleFragment.newInstance("Content for nearby stuff."), R.drawable.ic_nearby, "Nearby")
);
```

For a working example, refer to [the sample app](https://github.com/roughike/BottomBar/tree/master/app/src/main).

## What about the (insert thing that looks different than the specs here)?

I'll implement the Material Design spec as well as I can, including all the animations. Just give me some time and **all your dreams will come true**.

## Apps using BottomBar

Send me a pull request with modified README.md or contact me at iiro.krankka@gmail.com to get a shoutout!

## Contributions

Feel free to create issues / pull requests.

## License

```
BottomBar library for Android
Copyright (c) 2016 Iiro Krankka (http://github.com/roughike).

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
