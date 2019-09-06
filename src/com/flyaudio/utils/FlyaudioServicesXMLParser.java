package com.flyaudio.utils;


import android.util.Log;
import android.util.Xml;

import com.flyaudio.entities.FlyaudioServices;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaoyuqing
 * on 17-6-13.
 */

public class FlyaudioServicesXMLParser {
    private static String TAG="qwer";

        public static List<FlyaudioServices> getFlyaudioServices(InputStream xml) throws Exception {
            List<FlyaudioServices> serviceslist = null;
            FlyaudioServices service = null;
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setInput(xml, "UTF-8"); //为Pull解释器设置要解析的XML数据
            int event = pullParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {

                switch (event) {

                    case XmlPullParser.START_DOCUMENT:
                        serviceslist = new ArrayList<FlyaudioServices>();
                        break;
                    case XmlPullParser.START_TAG:

                        if ("service".equals(pullParser.getName())) {
                            service = new FlyaudioServices();
                        }
                        if ("packagename".equals(pullParser.getName())) {
                            String name = pullParser.nextText();

                            service.setPackage_name(name);
                        }
                        if ("classname".equals(pullParser.getName())) {
                            String name = pullParser.nextText();

                            service.setClass_name(name);
                        }
                        if ("actionname".equals(pullParser.getName())) {
                            String name = pullParser.nextText();

                            service.setAciton_name(name);
                        }
                        if ("accstart".equals(pullParser.getName())) {
                            String name = pullParser.nextText();
                            if (name.equals("true")) {
                                service.setAcc_start(true);
                            } else if (name.equals("false")) {
                                service.setAcc_start(false);
                            } else {
                                service.setAcc_start(false);
                            }

                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("service".equals(pullParser.getName())) {

                            serviceslist.add(service);
                            service = null;
                        }
                        break;
                }

                event = pullParser.next();
            }
            return serviceslist;
        }
    }

