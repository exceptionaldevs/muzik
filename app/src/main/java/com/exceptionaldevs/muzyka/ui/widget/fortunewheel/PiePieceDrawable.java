package com.exceptionaldevs.muzyka.ui.widget.fortunewheel;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * Created by sebnap on 28.01.16.
 */
public class PiePieceDrawable extends Drawable {
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mColorPaint = new Paint();
    private final Paint mBitmapPaint = new Paint();
    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapHeight;
    private int mBitmapWidth;
    private RectF mDrawableRect = new RectF();
    Path mPath;


    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        if(mBitmap != null){
            canvas.drawPath(mPath, mBitmapPaint);
        }else{
            canvas.drawPath(mPath, mColorPaint);
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void setImageBitmap(Bitmap bm) {
        mBitmap = bm;
        setup();
    }

    public void setColor(int color){
        mColorPaint.setColor(color);
        mColorPaint.setAntiAlias(true);
    }

    public Path getPath() {
        return mPath;
    }

    public void setPath(Path path) {
        mPath = path;
    }

    private void setup() {
        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        updateShaderMatrix();
        invalidateSelf();
    }


    @Override
    protected void onBoundsChange(Rect bounds) {
        mDrawableRect = new RectF(bounds);
        setup();
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
}
