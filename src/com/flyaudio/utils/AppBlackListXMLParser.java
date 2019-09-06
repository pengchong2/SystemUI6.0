package com.flyaudio.utils;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AppBlackListXMLParser {

    public static List<String> getBlackList(InputStream in) {

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(in, "UTF-8");
            int eventType = parser.getEventType();
            List<String> blacklistList = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        blacklistList = new ArrayList<String>();
                        break;
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if (name.equalsIgnoreCase("item")) {  
                            blacklistList.add(parser.getAttributeValue(0));
                        }
                        break;
                    case XmlPullParser.END_TAG:  
                        break;  
                    }  
                    eventType = parser.next();
                }
            in.close();
            return blacklistList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}

