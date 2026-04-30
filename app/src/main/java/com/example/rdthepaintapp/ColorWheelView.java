package com.example.rdthepaintapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorWheelView extends View {
    private Paint wheelPaint;
    private Paint selectorPaint;
    private Paint centerPaint;
    private Bitmap wheelBitmap;
    private Canvas wheelCanvas;
    private float selectorAngle = 0;
    private float selectorRadius = 0.7f;
    private int selectedColor = Color.RED;
    private OnColorChangeListener listener;
    private int[] colors = new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED};

    public interface OnColorChangeListener {
        void onColorChanged(int color);
    }

    public ColorWheelView(Context context) {
        super(context);
        init();
    }

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(4);
        selectorPaint.setColor(Color.WHITE);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            createWheelBitmap(w, h);
        }
    }

    private void createWheelBitmap(int width, int height) {
        wheelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        wheelCanvas = new Canvas(wheelBitmap);

        int centerX = width / 2;
        int centerY = height / 2;
        float radius = Math.min(centerX, centerY) - 20;

        // Draw color wheel using sweep gradient
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        SweepGradient sweepGradient = new SweepGradient(centerX, centerY, colors, null);
        paint.setShader(sweepGradient);
        wheelCanvas.drawCircle(centerX, centerY, radius, paint);

        // Add white center for saturation/brightness control
        RadialGradient radialGradient = new RadialGradient(centerX, centerY, radius,
                Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        paint.setShader(radialGradient);
        wheelCanvas.drawCircle(centerX, centerY, radius, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (wheelBitmap != null) {
            canvas.drawBitmap(wheelBitmap, 0, 0, null);
        }

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        float radius = Math.min(centerX, centerY) - 20;

        // Draw selector
        float selectorX = centerX + (float) (radius * 0.7f * Math.cos(Math.toRadians(selectorAngle)));
        float selectorY = centerY + (float) (radius * 0.7f * Math.sin(Math.toRadians(selectorAngle)));

        canvas.drawCircle(selectorX, selectorY, 15, selectorPaint);

        // Draw center dot showing selected color
        centerPaint.setColor(selectedColor);
        canvas.drawCircle(selectorX, selectorY, 10, centerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        float radius = Math.min(centerX, centerY) - 20;

        float touchX = event.getX();
        float touchY = event.getY();

        float dx = touchX - centerX;
        float dy = touchY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= radius) {
            selectorAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (selectorAngle < 0) {
                selectorAngle += 360;
            }

            // Calculate color at this position
            float hue = selectorAngle;
            float saturation = distance / radius;
            selectedColor = Color.HSVToColor(new float[]{hue, saturation, 1.0f});

            if (listener != null) {
                listener.onColorChanged(selectedColor);
            }

            invalidate();
            return true;
        }

        return false;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.listener = listener;
    }
}