package com.flyaudio.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class ChooseStateTextView extends TextView {
	
	private String TAG = "ChooseStateTextView"; 
	private int textViewWidth;
	private OnLayoutListener listener;
	
	public ChooseStateTextView(Context context) {
		super(context);
	}

	public ChooseStateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ChooseStateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		textViewWidth = getWidth();
		if (listener != null) {
			listener.onLayoutWidth(getWidth());
		}
		Log.d(TAG, " onLayout textViewWidth = " + textViewWidth + "; changed = " + changed);
	}
	
	public int getTextWidth(){
		return textViewWidth;
	}

	public void setTextTextSize(int unit, float size){
		setTextSize(unit, size);
		Log.d(TAG, " setTextTextSize size =  " + size);
	}
	
	public void setTextTextColor(int color){
		setTextColor(color);
		Log.d(TAG, " setTextTextColor ");
	}
	
	public void setOnLayoutListener(OnLayoutListener listener){
		this.listener = listener;
	}
	
	public interface OnLayoutListener{
		void onLayoutWidth(int width);
	}
}
