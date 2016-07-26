package com.roughike.bottombar;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.support.annotation.XmlRes;
import android.support.v4.content.ContextCompat;

import com.roughike.bottombar.view.BottomBarTab;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iiro on 21.7.2016.
 */
public class TabParser {
    private final Context context;
    private final XmlResourceParser parser;

    private ArrayList<BottomBarTab> tabs;
    private BottomBarTab workingTab;

    public TabParser(Context context, @XmlRes int tabsXmlResId) {
        this.context = context;
        parser = context.getResources().getXml(tabsXmlResId);
        tabs = new ArrayList<>();

        parse();
    }

    private void parse() {
        try {
            parser.next();
            int eventType = parser.getEventType();

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if(eventType == XmlResourceParser.START_TAG) {
                    parseNewTab(parser);
                } else if(eventType == XmlResourceParser.END_TAG) {
                    if (parser.getName().equals("tab")) {
                        if (workingTab != null) {
                            tabs.add(workingTab);
                            workingTab = null;
                        }
                    }
                }

                eventType = parser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private void parseNewTab(XmlResourceParser parser) {
        if (workingTab == null) {
            workingTab = new BottomBarTab(context);
        }

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attrName = parser.getAttributeName(i);

            switch (attrName) {
                case "id":
                    workingTab.setId(parser.getIdAttributeResourceValue(i));
                    break;
                case "color":
                    workingTab.setActiveIconColor(getColorValue(i, parser));
                    break;
                case "title":
                    workingTab.setTitle(getTitleValue(i, parser));
                    break;
                case "icon":
                    workingTab.setIconResId(parser.getAttributeResourceValue(i, 0));
                    break;
            }
        }
    }

    private String getTitleValue(int attrIndex, XmlResourceParser parser) {
        int titleResource = parser.getAttributeResourceValue(attrIndex, 0);

        if (titleResource != 0) {
            return context.getString(titleResource);
        }

        return parser.getAttributeValue(attrIndex);
    }

    private int getColorValue(int attrIndex, XmlResourceParser parser) {
        int colorResource = parser.getAttributeResourceValue(attrIndex, 0);

        if (colorResource != 0) {
            return ContextCompat.getColor(context, colorResource);
        }

        return Color.parseColor(parser.getAttributeValue(attrIndex));
    }

    public List<BottomBarTab> getTabs() {
        return tabs;
    }
}
