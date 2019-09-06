package com.flyaudio.entities;

public class ButtonEntity {
	
	private String tag="";
	private String background="";
	private String m_Text="";
	private String isMenu="";
	private String marginLeft="";
	private String packageName="";
	private String className="";
	private String shortcutIcon="";
	private String screen="";
	private String x="";
	private String y="";
	
	public String getScreen() {
		return screen;
	}
	public void setScreen(String screen) {
		this.screen = screen;
	}
	public String getX() {
		return x;
	}
	public void setX(String x) {
		this.x = x;
	}
	public String getY() {
		return y;
	}
	public void setY(String y) {
		this.y = y;
	}
	public String getPackageName() {
		return "com.android.launcher3";
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getShortcutIcon() {
		return shortcutIcon;
	}
	public void setShortcutIcon(String shortcutIcon) {
		this.shortcutIcon = shortcutIcon;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getBackground() {
		return background;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	public String getM_Text() {
		return m_Text;
	}
	public void setM_Text(String m_Text) {
		this.m_Text = m_Text;
	}
	public String getIsMenu() {
		return isMenu;
	}
	public void setIsMenu(String isMenu) {
		this.isMenu = isMenu;
	}
	

	public String getMarginLeft() {
		return marginLeft;
	}
	public void setMarginLeft(String marginLeft) {
		this.marginLeft = marginLeft;
	}
	
	public boolean isCreateShortcut() {
		if(packageName==null||className==null||shortcutIcon==null
				||screen==null||x==null||y==null)
			return false;
		else
			return true;
	}
	
	
}
