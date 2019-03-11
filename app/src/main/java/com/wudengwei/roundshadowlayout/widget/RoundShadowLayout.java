package com.wudengwei.roundshadowlayout.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.wudengwei.roundshadowlayout.R;

/**
 * Created by wudengwei
 * on 2019/3/7
 */
public class RoundShadowLayout extends FrameLayout {
    //two radius values [X, Y]. The corners are ordered top-left, top-right, bottom-right, bottom-left
    private float[] radiusArray = new float[8];

    private Paint shadowPaint;//阴影画笔
    private RectF shadowRect;//阴影矩形的layer区域
    private Path shadowPath;//阴影矩形的路径
    private float shadowRadius = 0;//阴影半径
    private int shadowColor;//阴影颜色
    private float shadow_x = 0;//阴影偏移
    private float shadow_y = 0;//阴影偏移

    private Paint roundPaint;//裁剪圆角的画笔
    private RectF roundRect;//包含圆角矩形的路径的layer区域
    private Path roundPath;//圆角矩形的路径

    public RoundShadowLayout(@NonNull Context context) {
        this(context,null);
    }

    public RoundShadowLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundShadowLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //setLayerType(LAYER_TYPE_SOFTWARE, null);
        //四个边角的圆角半径
        float topLeftRadius = 0;
        float topRightRadius = 0;
        float bottomLeftRadius = 0;
        float bottomRightRadius = 0;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundShadowLayout);
            float radius = ta.getDimension(R.styleable.RoundShadowLayout_radius, 0);
            topLeftRadius = ta.getDimension(R.styleable.RoundShadowLayout_topLeftRadius, radius);
            topRightRadius = ta.getDimension(R.styleable.RoundShadowLayout_topRightRadius, radius);
            bottomLeftRadius = ta.getDimension(R.styleable.RoundShadowLayout_bottomLeftRadius, radius);
            bottomRightRadius = ta.getDimension(R.styleable.RoundShadowLayout_bottomRightRadius, radius);
            shadowRadius = ta.getDimension(R.styleable.RoundShadowLayout_shadowRadius, 0);
            shadowColor = ta.getColor(R.styleable.RoundShadowLayout_shadowColor, 0x88757575);
            shadow_x = ta.getDimension(R.styleable.RoundShadowLayout_shadow_x, 0);
            shadow_y = ta.getDimension(R.styleable.RoundShadowLayout_shadow_y, 0);
            //统一shadowRadius
            if (shadowRadius > 0) {
                int temp = (int) (shadowRadius + 0.5);
                shadowRadius = temp;
            }
            ta.recycle();
        }
        radiusArray[0] = topLeftRadius;
        radiusArray[1] = topLeftRadius;

        radiusArray[2] = topRightRadius;
        radiusArray[3] = topRightRadius;

        radiusArray[4] = bottomRightRadius;
        radiusArray[5] = bottomRightRadius;

        radiusArray[6] = bottomLeftRadius;
        radiusArray[7] = bottomLeftRadius;
        //裁剪圆角的画笔
        roundPaint = new Paint();
        //圆角矩形的路径
        roundPath = new Path();
        //包含圆角矩形的路径的layer区域
        roundRect = new RectF();

        //阴影矩形的layer区域
        shadowRect = new RectF();
        //阴影矩形的路径
        shadowPath = new Path();
        //阴影画笔
        shadowPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 测量子view的最大宽高
        int width = 0;
        int height = 0;
        for (int i=0;i<getChildCount();i++) {
            View child = getChildAt(i);
            //测量子View的宽高,measureChild最终调用child.measure(w,h)
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec-(int) (shadowRadius)*2,
                    getPaddingLeft() + getPaddingRight(), lp.width);
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec-(int) (shadowRadius)*2,
                    getPaddingTop() + getPaddingBottom(), lp.height);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            MarginLayoutParams mlp = (MarginLayoutParams) child
                    .getLayoutParams();
            int childWidth = child.getMeasuredWidth() + mlp.leftMargin
                    + mlp.rightMargin;
            int childHeight = child.getMeasuredHeight() + mlp.topMargin
                    + mlp.bottomMargin;
            width = Math.max(width, childWidth);
            height = Math.max(height, childHeight);
        }
        //如果使用阴影，则宽高加上阴影
        setMeasuredDimension(
                width + getPaddingLeft() + getPaddingRight() + (int) (shadowRadius)*2,
                height + getPaddingTop() + getPaddingBottom() + (int) (shadowRadius)*2
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w > 0 && h > 0 && shadowRadius > 0) {
            //设置阴影背景
            setBackgroundCompat(w, h);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i=0;i<getChildCount();i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            int lc = (int) (shadowRadius) + lp.leftMargin + getPaddingLeft();
            int tc = (int) (shadowRadius) + lp.topMargin + getPaddingTop();
            int rc = lc + child.getMeasuredWidth();
            int bc = tc + child.getMeasuredHeight();
            child.layout(lc, tc, rc, bc);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //使用canvas.saveLayer()配合roundPaint.setXfermode裁剪圆角区域
        roundRect.set(shadowRadius,shadowRadius,getWidth()- shadowRadius,getHeight()- shadowRadius);
        canvas.saveLayer(roundRect, null, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        //裁剪圆角区域
        clipRound(canvas);
        canvas.restore();
    }

    //裁剪圆角区域
    private void clipRound(Canvas canvas) {
        roundPath.reset();
        roundPath.addRoundRect(roundRect, radiusArray, Path.Direction.CW);
        //画笔设置
        roundPaint.setColor(Color.WHITE);
        roundPaint.setAntiAlias(true);
        roundPaint.setStyle(Paint.Style.FILL);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawPath(roundPath, roundPaint);
        } else {
            roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            final Path path = new Path();
            path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                path.op(roundPath, Path.Op.DIFFERENCE);
            }
            canvas.drawPath(path, roundPaint);
        }
    }

    //设置阴影背景
    private void setBackgroundCompat(int w, int h) {
        Bitmap bitmap = createShadowBitmap(w, h, shadowRadius, shadow_x, shadow_y, shadowColor);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        setBackground(drawable);
    }

    //添加阴影bitmap
    private Bitmap createShadowBitmap(int shadowWidth, int shadowHeight, float shadowRadius,
                                      float dx, float dy, int shadowColor) {
        Bitmap output = Bitmap.createBitmap(shadowWidth, shadowHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        shadowRect.set(shadowRadius,shadowRadius,shadowWidth - shadowRadius,shadowHeight - shadowRadius);

        if (dy > 0) {
            shadowRect.top += dy;
            shadowRect.bottom -= dy;
        } else if (dy < 0) {
            shadowRect.top += Math.abs(dy);
            shadowRect.bottom -= Math.abs(dy);
        }

        if (dx > 0) {
            shadowRect.left += dx;
            shadowRect.right -= dx;
        } else if (dx < 0) {
            shadowRect.left += Math.abs(dx);
            shadowRect.right -= Math.abs(dx);
        }

        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(shadowColor);
        if (!isInEditMode()) {
            shadowPaint.setShadowLayer(shadowRadius, dx, dy, shadowColor);
        }
        shadowPath.reset();
        shadowPath.addRoundRect(shadowRect, radiusArray, Path.Direction.CW);
        canvas.drawPath(shadowPath, shadowPaint);

        return output;
    }
}