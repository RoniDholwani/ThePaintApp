package com.example.rdthepaintapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class PaintView extends View {
    private Path currentPath;
    private Paint currentPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private int paintColor = Color.parseColor("#212121");
    private int strokeWidth = 10;
    private boolean isEraserMode = false;
    private int canvasBackgroundColor = Color.parseColor("#FAFAFA");
    private int eraserColor = Color.parseColor("#FAFAFA");

    private final List<Path> pathList = new ArrayList<>();
    private final List<Paint> paintList = new ArrayList<>();
    private final List<Path> undonePaths = new ArrayList<>();
    private final List<Paint> undonePaints = new ArrayList<>();

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    public PaintView(Context context) {
        super(context);
        init();
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        currentPath = new Path();
        currentPaint = new Paint();
        currentPaint.setColor(paintColor);
        currentPaint.setAntiAlias(true);
        currentPaint.setStrokeWidth(strokeWidth);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setDither(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            if (canvasBitmap == null || canvasBitmap.getWidth() != w || canvasBitmap.getHeight() != h) {
                Bitmap oldBitmap = canvasBitmap;
                canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                drawCanvas = new Canvas(canvasBitmap);
                drawCanvas.drawColor(canvasBackgroundColor);

                // If there was an old bitmap, draw it onto the new canvas
                if (oldBitmap != null && !pathList.isEmpty()) {
                    drawCanvas.drawBitmap(oldBitmap, 0, 0, null);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        // Draw canvas background
        canvas.drawColor(canvasBackgroundColor);

        // Draw the bitmap containing all saved paths
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, null);
        }

        // Draw current path on top
        canvas.drawPath(currentPath, currentPaint);

        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    private void touchStart(float x, float y) {
        undonePaths.clear();
        undonePaints.clear();

        currentPath.reset();
        currentPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        currentPath.lineTo(mX, mY);

        // Commit the path to the bitmap
        if (drawCanvas != null) {
            drawCanvas.drawPath(currentPath, currentPaint);
        }

        // Save the path for undo/redo
        pathList.add(new Path(currentPath));
        paintList.add(new Paint(currentPaint));

        // Create new path for next drawing
        currentPath = new Path();
    }

    public void setCanvasBackground(int color) {
        this.canvasBackgroundColor = color;

        // Update canvas bitmap background
        if (drawCanvas != null && canvasBitmap != null) {
            drawCanvas.drawColor(color);
            // Redraw all existing paths
            for (int i = 0; i < pathList.size(); i++) {
                drawCanvas.drawPath(pathList.get(i), paintList.get(i));
            }
        }

        invalidate();
    }

    public void updateEraserColor(int color) {
        this.eraserColor = color;
        if (isEraserMode) {
            currentPaint.setColor(color);
        }
    }

    public void setPaintColor(int color) {
        isEraserMode = false;
        paintColor = color;
        currentPaint.setColor(color);
        currentPaint.setStrokeWidth(strokeWidth);
    }

    public void setBrushSize(int size) {
        strokeWidth = size;
        if (!isEraserMode) {
            currentPaint.setStrokeWidth(size);
        }
    }

    public void setEraserMode() {
        isEraserMode = true;
        currentPaint.setColor(eraserColor);
        currentPaint.setStrokeWidth(strokeWidth * 2);
    }

    public boolean isEraserMode() {
        return isEraserMode;
    }

    public void resetCanvas() {
        pathList.clear();
        paintList.clear();
        undonePaths.clear();
        undonePaints.clear();

        currentPath.reset();

        if (drawCanvas != null && canvasBitmap != null) {
            drawCanvas.drawColor(canvasBackgroundColor);
        }

        invalidate();
    }

    public void undo() {
        if (!pathList.isEmpty()) {
            undonePaths.add(pathList.remove(pathList.size() - 1));
            undonePaints.add(paintList.remove(paintList.size() - 1));

            // Redraw the entire bitmap
            if (drawCanvas != null && canvasBitmap != null) {
                drawCanvas.drawColor(canvasBackgroundColor);
                for (int i = 0; i < pathList.size(); i++) {
                    drawCanvas.drawPath(pathList.get(i), paintList.get(i));
                }
            }
            invalidate();
        }
    }

    public void redo() {
        if (!undonePaths.isEmpty()) {
            Path path = undonePaths.remove(undonePaths.size() - 1);
            Paint paint = undonePaints.remove(undonePaints.size() - 1);
            pathList.add(path);
            paintList.add(paint);

            // Redraw to bitmap
            if (drawCanvas != null) {
                drawCanvas.drawPath(path, paint);
            }
            invalidate();
        }
    }

    public boolean hasDrawings() {
        return !pathList.isEmpty() || !currentPath.isEmpty();
    }

    public Bitmap getBitmapForSaving() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return null;
        }

        Bitmap returnBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnBitmap);
        canvas.drawColor(canvasBackgroundColor);

        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, null);
        }

        // Draw current path if not empty
        if (!currentPath.isEmpty()) {
            canvas.drawPath(currentPath, currentPaint);
        }

        return returnBitmap;
    }

    public Bitmap getBitmap() {
        return getBitmapForSaving();
    }

    public void restoreFromBitmap(Bitmap bitmap) {
        if (bitmap != null && getWidth() > 0 && getHeight() > 0) {
            // Clear existing data
            pathList.clear();
            paintList.clear();
            undonePaths.clear();
            undonePaints.clear();

            // Scale bitmap to fit current view size
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);

            // Create new canvas bitmap
            canvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            drawCanvas.drawColor(canvasBackgroundColor);
            drawCanvas.drawBitmap(scaledBitmap, 0, 0, null);

            invalidate();
        }
    }
}