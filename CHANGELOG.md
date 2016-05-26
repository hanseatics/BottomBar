## Changelog

(or: why are you updating this all the time?)

### Newest version: 1.3.4

* Now the BottomBar is 56dp tall, as it should be! Make sure your icons are 24dp and **untrimmed**, meaning that the whole png size musn't be more than 24dp.
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
