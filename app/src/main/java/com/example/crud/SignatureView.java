package com.example.crud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {

    private Paint paint = new Paint();
    private Path path = new Path();
    private boolean isTouched = false;
    private Bitmap signatureBitmap;

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(6f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (signatureBitmap != null) {
            canvas.drawBitmap(signatureBitmap, 0, 0, null);
        } else {
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                isTouched = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setSignatureBitmap(Bitmap bitmap) {
        path.reset();
        this.signatureBitmap = bitmap;
        isTouched = true;
        invalidate();
    }

    public boolean hasSignature() {
        return isTouched || signatureBitmap != null;
    }

    public void clear() {
        path.reset();
        signatureBitmap = null;
        isTouched = false;
        invalidate();
    }

    public Bitmap getSignatureBitmap() {
        if (signatureBitmap != null) {
            return signatureBitmap;
        } else {
            Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            draw(canvas);
            return bitmap;
        }
    }
}