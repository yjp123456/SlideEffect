package com.example.myapplication.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.List;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * 滑动拼接切割图片控件
 */
public class CutImageView extends ImageView {
    private Paint bitmapLeftPaint = new Paint(ANTI_ALIAS_FLAG);
    private Paint bitmapRightPaint = new Paint(ANTI_ALIAS_FLAG);
    private Bitmap bitmap = null;
    private Bitmap bitmapLeft = null;
    private Bitmap bitmapRight = null;
    private BitmapShader bitmapLeftShader;
    private BitmapShader bitmapRightShader;


    private Matrix shaderMatrix = new Matrix();
    private Matrix leftAreaMatrix = new Matrix();
    private Matrix rightAreaMatrix = new Matrix();

    /**
     * 由于滑动拼接动效需要沿切割线消失，所以需要移动图片纹理,固定顶点坐标
     */
    private float leftAreaTranslateX;
    private float leftAreaTranslateY;
    private float rightAreaTranslateX;
    private float rightAreaTranslateY;

    //滑动拼接左边切割区域点
    private List<ClipPoint> leftAreaPoints;
    //滑动拼接右边切割区域点
    private List<ClipPoint> rightAreaPoints;
    private float width, height;

    Path pathLeft = new Path();
    Path pathRight = new Path();


    private boolean mIsShowSrc = true;

    public CutImageView(Context context) {
        super(context);
    }


    public CutImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 设置切割区域边界点
     */
    public void setClipArea(List<ClipPoint> leftPoints, List<ClipPoint> rightPoints,boolean isNeedInit) {
        leftAreaPoints = leftPoints;
        rightAreaPoints = rightPoints;
        if (bitmap != null && isNeedInit) {
            createAreaBitmap();
        }
        postInvalidate();
    }

    private void createAreaBitmap() {
        if (bitmap == null) {
            return;
        }

        Paint bitmapPaint = new Paint(ANTI_ALIAS_FLAG);
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapPaint.setShader(bitmapShader);
        bitmapShader.setLocalMatrix(shaderMatrix);

        //生成左边裁剪图片
        Bitmap temp = bitmapLeft == null ? Bitmap.createBitmap(getWidth() + 2, getHeight() + 2, bitmap.getConfig()) : bitmapLeft;
        temp.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(temp);
        pathLeft.reset();
        //生成裁剪图片记得外层留出一个透明像素，以防止移动时拉伸现象
        for (int i = 0; i < leftAreaPoints.size(); i++) {
            float tempX = leftAreaPoints.get(i).x;
            float tempY = leftAreaPoints.get(i).y;
            float x = tempX == 0 ? 1 : tempX * getWidth();
            float y = tempY == 0 ? 1 : tempY * getHeight();
            if (i == 0) {
                pathLeft.moveTo(x, y);
            } else {
                pathLeft.lineTo(x, y);
            }
        }
        canvas.drawPath(pathLeft, bitmapPaint);
        //bitmapShader的bitmap不能回收，不然在小米机器上会崩溃
        bitmapLeftShader = new BitmapShader(temp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapLeftPaint.setShader(bitmapLeftShader);

        //生成右边裁剪图片
        temp = bitmapRight == null ? Bitmap.createBitmap(getWidth() + 2, getHeight() + 2, bitmap.getConfig()) : bitmapRight;
        temp.eraseColor(Color.TRANSPARENT);
        canvas = new Canvas(temp);
        pathRight.reset();
        for (int i = 0; i < rightAreaPoints.size(); i++) {
            float tempX = rightAreaPoints.get(i).x;
            float tempY = rightAreaPoints.get(i).y;
            float x = tempX == 0 ? 1 : tempX * getWidth();
            float y = tempY == 0 ? 1 : tempY * getHeight();
            if (i == 0) {
                pathRight.moveTo(x, y);
            } else {
                pathRight.lineTo(x, y);
            }
        }
        canvas.drawPath(pathRight, bitmapPaint);
        bitmapRightShader = new BitmapShader(temp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapRightPaint.setShader(bitmapRightShader);
    }

    /**
     * 设置左边切割区域纹理移动距离
     */
    public void setLeftAreaTranslate(float translateX, float translateY) {
        leftAreaTranslateX = translateX;
        leftAreaTranslateY = translateY;
    }


    /**
     * 设置右边切割区域纹理移动距离
     */
    public void setRightAreaTranslate(float translateX, float translateY) {
        rightAreaTranslateX = translateX;
        rightAreaTranslateY = translateY;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        releaseBitmap();
        setupBitmap(this, width, height);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseBitmap();
    }

    private void releaseBitmap() {
        if (bitmapRight != null) {
            bitmapRight.recycle();
            bitmapRight = null;
        }
        if (bitmapLeft != null) {
            bitmapLeft.recycle();
            bitmapLeft = null;
        }
    }

    public void setIsShowSrc(boolean isShowSrc) {
        mIsShowSrc = isShowSrc;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsShowSrc) {
            super.onDraw(canvas);
        } else {
            if (leftAreaPoints != null) {
                leftAreaMatrix.reset();
                leftAreaMatrix.postTranslate(leftAreaTranslateX, leftAreaTranslateY);
                bitmapLeftShader.setLocalMatrix(leftAreaMatrix);
                if (bitmap != null) {
                    canvas.drawPath(pathLeft, bitmapLeftPaint);
                }
            }

            if (rightAreaPoints != null) {
                rightAreaMatrix.reset();
                rightAreaMatrix.postTranslate(rightAreaTranslateX, rightAreaTranslateY);
                bitmapRightShader.setLocalMatrix(rightAreaMatrix);
                if (bitmap != null) {
                    canvas.drawPath(pathRight, bitmapRightPaint);
                }
            }
        }
    }


    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setupBitmap(this, width, height);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setupBitmap(this, width, height);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        setupBitmap(this, width, height);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setupBitmap(this, width, height);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == ScaleType.CENTER_CROP || scaleType == ScaleType.FIT_XY)
            super.setScaleType(scaleType);
        else
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    public void setupBitmap(ImageView imageView, float width, float height) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return;
        }
        try {

            bitmap = (drawable instanceof BitmapDrawable) ?
                    ((BitmapDrawable) drawable).getBitmap()
                    :
                    Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bitmap == null) {
            imageView.invalidate();
            return;
        }

        if (imageView.getScaleType() != ScaleType.CENTER_CROP && imageView.getScaleType() != ScaleType.FIT_XY)
            imageView.setScaleType(ScaleType.CENTER_CROP);
        setUpScaleType(imageView, width, height);
        imageView.invalidate();

        if (leftAreaPoints != null && rightAreaPoints != null) {
            createAreaBitmap();
        }
    }


    private void setUpScaleType(ImageView iv, float width, float height) {
        float scaleX = 1, scaleY, dx, dy;
        if (bitmap == null || shaderMatrix == null)
            return;
        shaderMatrix.set(null);
        if (iv.getScaleType() == ScaleType.CENTER_CROP) {
            if (width != bitmap.getWidth()) {
                scaleX = width / bitmap.getWidth();
            }
            if (scaleX * bitmap.getHeight() < height) {
                scaleX = height / bitmap.getHeight();
            }
            dy = (height - bitmap.getHeight() * scaleX) * 0.5f;
            dx = (width - bitmap.getWidth() * scaleX) * 0.5f;
            shaderMatrix.setScale(scaleX, scaleX);
        } else {
            scaleX = width / bitmap.getWidth();
            scaleY = height / bitmap.getHeight();
            dy = (height - bitmap.getHeight() * scaleY) * 0.5f;
            dx = (width - bitmap.getWidth() * scaleX) * 0.5f;
            shaderMatrix.setScale(scaleX, scaleY);
        }
        shaderMatrix.postTranslate(dx + 0.5f, dy + 0.5f);
    }

    public void clearArea() {
        leftAreaPoints = null;
        rightAreaPoints = null;
    }


    public static class ClipPoint {
        public float x;
        public float y;

        public ClipPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public ClipPoint(ClipPoint other) {
            this.x = other.x;
            this.y = other.y;
        }

        /**
         * 交换数据
         */
        public void swap(ClipPoint second) {
            float temp = this.x;
            this.x = second.x;
            second.x = temp;
            temp = this.y;
            this.y = second.y;
            second.y = temp;
        }
    }

}