package com.example.bottombar.sample;

/**
 * Created by iiro on 7.6.2016.
 */
public class TabMessage {
    public static String get(int menuItemId, boolean isReselection) {
        String message = "Content for ";

        switch (menuItemId) {
            case R.id.bb_menu_recents:
                message += "recents";
                break;
            case R.id.bb_menu_favorites:
                message += "favorites";
                break;
            case R.id.bb_menu_nearby:
                message += "nearby";
                break;
            case R.id.bb_menu_friends:
                message += "friends";
                break;
            case R.id.bb_menu_food:
                message += "food";
                break;
        }

        if (isReselection) {
            message += " WAS RESELECTED! YAY!";
        }

        return message;
    }
}
