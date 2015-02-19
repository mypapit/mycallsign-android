package net.mypapit.android.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;

/*
 *  Taken from http://stackoverflow.com/questions/20834320/getting-square-images-like-gmail-app
 *  answere by Amulya Khare (http://www.amulyakhare.com/)
 */

public class CharacterDrawable extends ColorDrawable {

	private final char character;
	private final Paint textPaint;
	private final Paint borderPaint;
	private static final int STROKE_WIDTH = 10;
	private static final float SHADE_FACTOR = 0.9f;
	private int mwidth, mheight;

	public CharacterDrawable(char character, int color, int width, int height) {
		super(color);
		this.character = character;
		this.textPaint = new Paint();
		this.borderPaint = new Paint();
		this.mwidth = width;
		this.mheight = height;

		// text paint settings
		textPaint.setColor(Color.WHITE);
		textPaint.setAntiAlias(true);
		textPaint.setFakeBoldText(true);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTextAlign(Paint.Align.CENTER);

		// border paint settings
		borderPaint.setColor(getDarkerShade(color));
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(STROKE_WIDTH);
	}

	private int getDarkerShade(int color) {
		return Color.rgb((int) (SHADE_FACTOR * Color.red(color)), (int) (SHADE_FACTOR * Color.green(color)),
				(int) (SHADE_FACTOR * Color.blue(color)));
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);

		// draw border
		canvas.drawRect(getBounds(), borderPaint);

		// draw text
		int width = this.mwidth;
		int height = this.mheight;
		textPaint.setTextSize(height / 2);
		canvas.drawText(String.valueOf(character), width / 2, height / 2
				- ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);
	}

	public void setAlpha(int alpha) {
		textPaint.setAlpha(alpha);
	}

	public void setColorFilter(ColorFilter cf) {
		textPaint.setColorFilter(cf);
	}

	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}