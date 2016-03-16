# BottomBar
<img src="https://raw.githubusercontent.com/roughike/BottomBar/master/screenshot_two.png" width="278" height="492" /> <img src="https://raw.githubusercontent.com/roughike/BottomBar/master/screenshot_one.png" width="278" height="492" />

<a href="https://play.google.com/store/apps/details?id=com.iroughapps.swipeselectordemo&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-AC-global-none-all-co-pr-py-PartBadges-Oct1515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge-border.png" width="216" height="70"/></a>

## What?

A custom view component that mimicks the [Material Design "Bottom navigation" pattern](https://www.google.com/design/spec/components/bottom-navigation.html#bottom-navigation-specs).

## Installation

**Gradle:**

```groovy
compile 'com.roughike:bottom-bar:0.0.1'
```

**Maven:**
```xml
<dependency>
  <groupId>com.roughike</groupId>
  <artifactId>bottom-bar</artifactId>
  <version>0.0.1</version>
  <type>pom</type>
</dependency>
```

## How?

The usage is really simple.

**Here's a quick snippet to get started:**

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
                new BottomBarTab(R.drawable.ic_nearby, "Nearby"),
                new BottomBarTab(R.drawable.ic_friends, "Friends")
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
