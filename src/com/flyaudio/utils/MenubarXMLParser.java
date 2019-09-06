package com.flyaudio.utils;

import com.flyaudio.entities.ButtonEntity;
import com.flyaudio.entities.MenuBarConstent;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MenubarXMLParser extends DefaultHandler
{
	
	private ArrayList<com.flyaudio.entities.ButtonEntity> dataList;
	private ButtonEntity ButtonEntity;
	//private StringBuffer buffer=new StringBuffer();
	
	public ArrayList<ButtonEntity> getData()
	{
		return dataList;
	}
	
	public void startDocument() throws SAXException 
	{
		// TODO Auto-generated method stub
		dataList = new ArrayList<ButtonEntity>();
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException 
			{
		// TODO Auto-generated method stub
		if(qName.equals(MenuBarConstent.SHORTCUTS))
		{
			ButtonEntity = new ButtonEntity();
			
			ButtonEntity.setTag(attributes.getValue(MenuBarConstent.MENUBAR_TAG));
			ButtonEntity.setBackground(attributes.getValue(MenuBarConstent.MENUBAR_BACKGROUND));
			ButtonEntity.setIsMenu(attributes.getValue(MenuBarConstent.MENUBAR_ISMENU));
			ButtonEntity.setM_Text(attributes.getValue(MenuBarConstent.MENUBAR_M_TEXT));
			ButtonEntity.setMarginLeft(attributes.getValue(MenuBarConstent.MENUBAR_MARGIN_LEFT));
			
//			ButtonEntity.setShortcutIcon(attributes.getValue(MenuBarConstent.MENUBAR_SHORTCUTICON));
//			ButtonEntity.setPackageName(attributes.getValue(MenuBarConstent.MENUBAR_PACKAGENAME));
//			ButtonEntity.setClassName(attributes.getValue(MenuBarConstent.MENUBAR_CLASSNAME));
//			
//			ButtonEntity.setScreen(attributes.getValue(MenuBarConstent.MENUBAR_SCREEN));
//			ButtonEntity.setX(attributes.getValue(MenuBarConstent.MENUBAR_X));
//			ButtonEntity.setY(attributes.getValue(MenuBarConstent.MENUBAR_Y));
	
			dataList.add(ButtonEntity);
			//Log.i("msg", boxButtonEntity.getM_Text()+"---"+boxButtonEntity.getLayout_marginBottom());
		}
		super.startElement(uri, localName, qName, attributes);
	}
}
