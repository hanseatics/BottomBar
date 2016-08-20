## Changelog

(or: y u no add shiny new things?!)

### Newest version: 1.4.0.1

* Merged a [PR that adds support for vector drawables for API < 21](https://github.com/roughike/BottomBar/pull/332)
* Merged a [PR that fixes removing all tabs from BottomBar](https://github.com/roughike/BottomBar/pull/339)

### 1.4.0

* Started writing some tests. **Contributions more than welcome**, as I'm still a noob when it comes to testing.
* Merged a [PR that adds support for vector drawables](https://github.com/roughike/BottomBar/pull/280)
* Merged a [PR that adds support for disabling text scale animation](https://github.com/roughike/BottomBar/pull/298)
* Merged a [PR that adds support for custom background and tab icon colors, and also custom alpha](https://github.com/roughike/BottomBar/pull/302)
* Merged a [PR that fixes wrong method name for message shown by an exception](https://github.com/roughike/BottomBar/pull/320)

### 1.3.9

* Merged [another PR that should get rid of the infinite Badge loop for good](https://github.com/roughike/BottomBar/pull/289).

### 1.3.8

* Merged a [PR that fixes infinite loop caused by a layout listener when adding Badges.](https://github.com/roughike/BottomBar/pull/286)

### 1.3.7

* Merged a [PR that fixes elliptical Badges](https://github.com/roughike/BottomBar/pull/275).
* Fixed issues [#276](https://github.com/roughike/BottomBar/issues/276) and [#277](https://github.com/roughike/BottomBar/issues/277)

### 1.3.6

* Fixed a bug that would cause the navigation bar to not be transparent.
* Flattened View hierarchy.
* Throwing a nice little Exception if someone tries to call ```noResizeGoodness()``` improperly, instead of just failing silently.

### 1.3.5

* [Merged](https://github.com/roughike/BottomBar/pull/260) [some](https://github.com/roughike/BottomBar/pull/268) [pull](https://github.com/roughike/BottomBar/pull/269) [requests.](https://github.com/roughike/BottomBar/pull/271)
* Thanks to @henhal, now the unselection bug when using badges is fixed.
* Deprecated the ```setItemsFromMenu(@MenuRes int resId, OnMenuTabClickListener listener)``` method in favor of two separate methods: ```mBottomBar.setItems(@MenuRes int resId)``` and ```mBottomBar.setOnMenuTabClickListener(OnMenuTabClickListener listener)```. Not only because deprecating stuff is so fun (it is), but because this actually makes more sense than the old approach. The old approach still works.

### 1.3.4

* Now the BottomBar is 56dp tall, as it should be! Make sure your icons are 24dp and **trimmed**, meaning that the whole png size musn't be more than 24dp. So **don't use padding** around the icon.
* Fixed a minor bug when programmatically selecting a tab.
* Added a ```setAutoHideOnSelection(boolean autoHideOnSelection)``` method for the BottomBarBadge to control whether it is automatically hidden after the tab that contains it is selected or not. 
* Titles are now forced to be single line, make sure your title texts are short enough, or else they'll get truncated with a "..." !
* Updated some dependencies and Gradle.

### 1.3.3

* The show / hide methods now behave nicely with CoordinatorLayout.
* Added alpha animation for the tab titles when using the shifting tabs.

### 1.3.2

* Now it's possible to use fixed mode (show titles on inactive tabs) even when there's more than three tabs.

### 1.3.1

* Fixed a critical bug in OnLongClickListener behavior (why didn't I see that before?) when using badges.

### 1.3.0

* Fixed a critical bug in OnClickListener behavior when using badges.

### 1.2.9

* Fixed the issue when using badges and the tab resize animation is enabled. Now the badges automatically adjust their position when the tab's size (or position) updates.

### 1.2.8

* Fixed the [ugly layout bug](https://github.com/roughike/BottomBar/issues/126) that happened when calling the ```setDefaultTabPosition()``` or ```selectTabAtPosition()``` methods.

### Versions 0.0.1 - 1.2.7

* Sweating my ass off making this library and trying to compete with the other ones.
