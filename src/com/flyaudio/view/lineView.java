package com.flyaudio.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class lineView extends View {


	private Paint p;
	private int mColor;

	public lineView(Context context) {
		this(context, null);
	}

	public lineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setColor(Color.RED);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		p.setColor(mColor);
		Path path = new Path();
		path.moveTo(7.5f, 0f);
		path.lineTo(0f, 7.5f);
		path.lineTo(15f, 7.5f);
		path.close();
		canvas.drawPath(path, p);
	}
	
	public synchronized void setColor(int color) {  
        mColor = color;  
        invalidate();  
    } 
}
