# BottomBar
<img src="https://raw.githubusercontent.com/roughike/BottomBar/master/demo1.gif" width="278" height="492" /> <img src="https://raw.githubusercontent.com/roughike/BottomBar/master/demo2.gif" width="278" height="492" />

## What?

A custom view component that mimicks the new [Material Design Bottom Navigation pattern](https://www.google.com/design/spec/components/bottom-navigation.html).

**(currently under development, expect to see changes during this week)**

## minSDK version

The current minSDK version is API level 14.

## Gimme that Gradle sweetness, pls?

It's waiting approval on jCenter. I'll update right away when it's available.

Meanwhile you can get up and running by using Jitpack. **Remember to check here in a day to use the jCenter dependency.**

**Project-level build.gradle:**
```groovy
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

**App-level (in the app module) build.gralde:**
```groovy
compile 'com.github.roughike:BottomBar:-SNAPSHOT'
```

## How?

BottomBar likes Fragments very much, but you can also handle your tab changes by yourself. You can add items by specifying an array of items or **by xml menu resources**.

#### Adding items from menu resource

**res/menu/bottombar_menu.xml:**

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/bottomBarItemOne"
        android:icon="@drawable/ic_recents"
        android:title="Recents" />
        ...
</menu>
```

**MainActivity.java**

```java
public class MainActivity extends AppCompatActivity {
    private BottomBar mBottomBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItemsFromMenu(R.menu.bottombar_menu, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int resId) {
                if (resId == R.id.bottomBarItemOne) {
                    // the user selected item number one
                }
            }
        });
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }
}
```

#### Working with Fragments

Just call ```setFragmentItems()``` instead of ```setItemsFromMenu()```:

```java
mBottomBar.setFragmentItems(getSupportFragmentManager(), R.id.fragmentContainer,
    new BottomBarFragment(SampleFragment.newInstance("Content for recents."), R.drawable.ic_recents, "Recents"),
    new BottomBarFragment(SampleFragment.newInstance("Content for favorites."), R.drawable.ic_favorites, "Favorites"),
    new BottomBarFragment(SampleFragment.newInstance("Content for nearby stuff."), R.drawable.ic_nearby, "Nearby")
);
```

#### I hate Fragments and wanna do everything by myself!

That's alright, you can also handle items by yourself. 

```java
mBottomBar.setItems(
        new BottomBarTab(R.drawable.ic_recents, "Recents"),
        new BottomBarTab(R.drawable.ic_favorites, "Favorites"),
        new BottomBarTab(R.drawable.ic_nearby, "Nearby")
);

// Listen for tab changes
mBottomBar.setOnItemSelectedListener(new OnTabSelectedListener() {
    @Override
    public void onItemSelected(int position) {
        // user selected a different tab
    }
});
```

For a working example, refer to [the sample app](https://github.com/roughike/BottomBar/tree/master/app/src/main).

## What about the (insert thing that looks different than the specs here)?

I'll implement the Material Design spec as well as I can, including all the animations. Just give me some time and **all your dreams will come true**.

## Apps using BottomBar

Send me a pull request with modified README.md to get a shoutout!

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
