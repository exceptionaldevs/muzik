package com.exceptionaldevs.muzyka.ui.widget.fortunewheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.exceptionaldevs.muzyka.R;

import java.util.ArrayList;

/**
 * Created by sebnap on 28.01.16.
 * <p/>
 * {@link PieView}, make us rich!
 */
public class PieView extends View {
    private static float FULL_SWEEP = (float) (2 * Math.PI);
    private float mSweepSlice;
    private Rect mDrawableRect;
    private int mSize;
    BindDrawablesCallback callback;
    ArrayList<PiePieceDrawable> pieSlices;

    public PieView(Context context) {
        this(context, null);
    }

    public PieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getSize() {
        return mSize;
    }

    /**
     * pie slice count
     *
     * @param size gets constrained to [2,10]
     */
    public void setSize(int size) {
        mSize = Utils.clamp(size, 2, 10);
        createPie();
    }

    public void createPie() {
        if (callback == null) {
            Log.w("PieView", "createPie: callback not set, aborting");
            return;
        }

        pieSlices = new ArrayList<>(mSize);

        mSweepSlice = FULL_SWEEP / mSize;

        float start = (float) (1.5f * Math.PI - mSweepSlice / 2f);
        float end = (float) (1.5f * Math.PI + mSweepSlice / 2f);
        int chordLength = (int) Math.ceil(2 * getRadius() * Math.sin(mSweepSlice / 2f));
        mDrawableRect =
                new Rect((int) Math.ceil(-chordLength / 2),
                        -getRadius(),
                        (int) Math.ceil(chordLength / 2),
                        0);
        Path p = makeSlice((float) Math.toDegrees(start), (float) Math.toDegrees(end),
                getInnerRadius(), getRadius(), 0, 0);

        for (int i = 0; i < mSize; i++) {
            final PiePieceDrawable drawable = new PiePieceDrawable();
            drawable.setPath(new Path(p));
            drawable.setBounds(mDrawableRect);

            callback.onBindDrawable(this, drawable, mDrawableRect.width(), mDrawableRect.height(), i);

            pieSlices.add(drawable);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createPie();
    }

    private int getRadiusInc() {
        // cxy   | radiusInc |
        // o ----| --------- |
        return (int) Math.floor(Math.min(getMeasuredWidth() / 2, getMeasuredHeight() / 2) * 0.6f);
    }

    private int getInnerRadius() {
        return getResources().getDimensionPixelOffset(R.dimen.fabSize56dp) / 2;
    }

    public int getRadius() {
        return getInnerRadius() + getRadiusInc();
    }

    private int getCenterX() {
        return getWidth() / 2;
    }

    private int getCenterY() {
        return getHeight() / 2;
    }

    /**
     * @param start start angle in degrees
     * @param end   end angle in degrees
     * @param inner inner radius
     * @param outer outer radius
     * @param cx    center x
     * @param cy    center y
     * @return
     */
    private Path makeSlice(float start, float end, int inner, int outer, int cx, int cy) {
        RectF bb =
                new RectF(cx - outer, cy - outer, cx + outer,
                        cy + outer);
        RectF bbi =
                new RectF(cx - inner, cy - inner, cx + inner,
                        cy + inner);

        Path path = new Path();
        path.arcTo(bb, start, end - start, false);
        path.arcTo(bbi, end, start - end);
        path.close();

        return path;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int ntargets = pieSlices.size();
        canvas.translate(getCenterX(), getCenterY());

        for (int i = 0; i < ntargets; i++) {
            PiePieceDrawable target = pieSlices.get(i);
            target.draw(canvas);
            canvas.rotate((float) Math.toDegrees(mSweepSlice));
        }
    }

    public BindDrawablesCallback getCallback() {
        return callback;
    }

    public void setCallback(BindDrawablesCallback callback) {
        this.callback = callback;
    }

    public interface BindDrawablesCallback {
        void onBindDrawable(PieView pieView, PiePieceDrawable drawable, int width, int height, int position);
    }
}
