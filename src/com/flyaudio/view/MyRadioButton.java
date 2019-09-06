package com.flyaudio.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;
import com.android.systemui.R;
public class MyRadioButton extends RadioButton {
    private static String TAG = "MyRadioButton";
    public MyRadioButton(Context context) {
        super(context);
    }


    public MyRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint mPaint = new Paint();
        mPaint.setStrokeWidth(2);
       // mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(this.getResources().getDimensionPixelSize(R.dimen.flyaudio_temp_icon_x), this.getResources().getDimensionPixelSize(R.dimen.flyaudio_temp_icon_y), 2, mPaint);

    }
}
