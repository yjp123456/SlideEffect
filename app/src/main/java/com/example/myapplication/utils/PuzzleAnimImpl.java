package com.example.myapplication.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.example.myapplication.view.CutImageView;

import java.util.ArrayList;
import java.util.List;

public class PuzzleAnimImpl {
    private ValueAnimator animator = ValueAnimator.ofFloat(1);
    List<CutImageView.ClipPoint> leftPoints = new ArrayList<>();
    List<CutImageView.ClipPoint> rightPoints = new ArrayList<>();

    public static final int MODE_RIGHT_TO_LEFT = 0;
    public static final int MODE_LEFT_TO_RIGHT = 1;
    public static final int MODE_BOTTOM_TO_TOP = 2;
    public static final int MODE_TOP_TO_BOTTOM = 3;
    public static final int MODE_OPPOSITE = 4;

    public void stopAnim() {
        if (animator != null) {
            animator.cancel();
        }
    }

    public void startAnim() {
        if (animator != null) {
            animator.start();
        }
    }

    /**
     * @param cutView       切割图片展示控件
     * @param first         第一个切割点 x:[0,1],y:[0,-1]
     * @param second        第二个切割点 x:[0,1],y:[0,-1]
     * @param modeLeftArea  左边区域动效模式
     * @param modeRightArea 右边区域动效模式
     * @param animTime      动效展示时长
     */
    public void initAnimByMode(final CutImageView cutView, CutImageView.ClipPoint first, CutImageView.ClipPoint second, int modeLeftArea, int modeRightArea, long animTime, final AnimCallback callback) {
        leftPoints.clear();
        rightPoints.clear();
        final Point leftAnimStartPoint = new Point();
        final Point leftAnimEndPoint = new Point();
        final Point rightAnimStartPoint = new Point();
        final Point rightAnimEndPoint = new Point();
        float k;
        if (first.x == second.x) {
            first.y = Math.abs(first.y);
            second.y = Math.abs(second.y);
            if (first.y > second.y) {
                first.swap(second);
            }
            leftPoints.add(new CutImageView.ClipPoint(0, 0));
            leftPoints.add(first);
            leftPoints.add(second);
            leftPoints.add(new CutImageView.ClipPoint(0, 1f));
            leftPoints.add(new CutImageView.ClipPoint(0, 0));

            rightPoints.add(first);
            rightPoints.add(new CutImageView.ClipPoint(1f, 0));
            rightPoints.add(new CutImageView.ClipPoint(1f, 1f));
            rightPoints.add(second);
            rightPoints.add(first);
            if (modeLeftArea == MODE_OPPOSITE && modeRightArea == MODE_OPPOSITE) {
                modeLeftArea = MODE_RIGHT_TO_LEFT;
                modeRightArea = MODE_LEFT_TO_RIGHT;
            }

        } else if (first.y == second.y) {
            first.y = Math.abs(first.y);
            second.y = Math.abs(second.y);
            if (first.x > second.x) {
                first.swap(second);
            }
            leftPoints.add(new CutImageView.ClipPoint(0, 0));
            leftPoints.add(new CutImageView.ClipPoint(1f, 0));
            leftPoints.add(second);
            leftPoints.add(first);
            leftPoints.add(new CutImageView.ClipPoint(0, 0));

            rightPoints.add(first);
            rightPoints.add(second);
            rightPoints.add(new CutImageView.ClipPoint(1f, 1f));
            rightPoints.add(new CutImageView.ClipPoint(0, 1f));
            rightPoints.add(first);

            if (modeLeftArea == MODE_OPPOSITE && modeRightArea == MODE_OPPOSITE) {
                modeLeftArea = MODE_BOTTOM_TO_TOP;
                modeRightArea = MODE_TOP_TO_BOTTOM;
            }
        } else {
            k = (second.y - first.y) / (second.x - first.x);
            float b = ((second.y + first.y) - k * (second.x + first.x)) / 2f;
            first.y = Math.abs(first.y);
            second.y = Math.abs(second.y);

            //左上角开始顺时针判断
            if (k > 0) {
                if (first.x < second.x) {
                    first.swap(second);
                }
                //切割点坐标，x: [0,1],y:[0,-1]
                leftPoints.add(new CutImageView.ClipPoint(0, 0));
                rightPoints.add(new CutImageView.ClipPoint(first));
                CutImageView.ClipPoint rightTop = new CutImageView.ClipPoint(1f, 0);
                if (rightTop.y > k * rightTop.x + b) {
                    leftPoints.add(rightTop);
                } else {
                    rightPoints.add(rightTop);
                }
                leftPoints.add(first);
                rightPoints.add(new CutImageView.ClipPoint(1f, 1f));
                leftPoints.add(second);
                CutImageView.ClipPoint leftBottom = new CutImageView.ClipPoint(0f, 1f);
                //判断时要用切割点坐标判断，添加时用图片坐标
                if (-leftBottom.y > k * leftBottom.x + b) {
                    leftPoints.add(leftBottom);
                } else {
                    rightPoints.add(leftBottom);
                }
                rightPoints.add(second);

                //闭合区域
                leftPoints.add(new CutImageView.ClipPoint(leftPoints.get(0)));
                rightPoints.add(new CutImageView.ClipPoint(rightPoints.get(0)));

                if (modeLeftArea == MODE_OPPOSITE && modeRightArea == MODE_OPPOSITE) {
                    // 动效坐标，x:[0,width],y:[0,height],
                    // 方案是把切割线与边界最后一个交点作为终止点，求过改点并与切割线垂直的直线与切割线的交点作为起始点
                    //左半区域切割线斜率为正时，最后一个交点是左上角，斜率为负时最后一个交点是左下角
                    float verticalK = -1 / k;
                    float tempX = b / (verticalK - k);
                    leftAnimStartPoint.x = (int) (tempX * cutView.getWidth());
                    leftAnimStartPoint.y = -(int) ((k * tempX + b) * cutView.getHeight());
                    leftAnimEndPoint.x = 0;
                    leftAnimEndPoint.y = 0;

                    //右半区域切割线斜率为正时，最后一个交点是右下角，斜率为负时最后一个交点是右上角
                    tempX = (b + verticalK + 1) / (verticalK - k);
                    rightAnimStartPoint.x = (int) (tempX * cutView.getWidth());
                    rightAnimStartPoint.y = -(int) ((k * tempX + b) * cutView.getHeight());
                    rightAnimEndPoint.x = cutView.getWidth();
                    rightAnimEndPoint.y = cutView.getHeight();
                }
            } else {
                if (first.x > second.x) {
                    first.swap(second);
                }

                //切割点坐标，x: [0,1],y:[0,-1]
                rightPoints.add(first);
                CutImageView.ClipPoint leftTop = new CutImageView.ClipPoint(0, 0);
                if (leftTop.y < k * leftTop.x + b) {
                    leftPoints.add(leftTop);
                } else {
                    rightPoints.add(leftTop);
                }
                leftPoints.add(first);
                leftPoints.add(second);
                rightPoints.add(new CutImageView.ClipPoint(1f, 0f));
                CutImageView.ClipPoint rightftBottom = new CutImageView.ClipPoint(1f, 1f);
                if (-rightftBottom.y < k * rightftBottom.x + b) {
                    leftPoints.add(rightftBottom);
                } else {
                    rightPoints.add(rightftBottom);
                }
                leftPoints.add(new CutImageView.ClipPoint(0, 1f));
                rightPoints.add(second);

                //闭合区域
                leftPoints.add(new CutImageView.ClipPoint(leftPoints.get(0)));
                rightPoints.add(new CutImageView.ClipPoint(rightPoints.get(0)));

                if (modeLeftArea == MODE_OPPOSITE && modeRightArea == MODE_OPPOSITE) {
                    // 动效坐标，x:[0,width],y:[0,height],
                    float verticalK = -1 / k;
                    float tempX = (b + 1) / (verticalK - k);
                    leftAnimStartPoint.x = (int) (tempX * cutView.getWidth());
                    leftAnimStartPoint.y = -(int) ((k * tempX + b) * cutView.getHeight());
                    leftAnimEndPoint.x = 0;
                    leftAnimEndPoint.y = cutView.getHeight();

                    tempX = (b + verticalK) / (verticalK - k);
                    rightAnimStartPoint.x = (int) (tempX * cutView.getWidth());
                    rightAnimStartPoint.y = -(int) ((k * tempX + b) * cutView.getHeight());
                    rightAnimEndPoint.x = cutView.getWidth();
                    rightAnimEndPoint.y = 0;
                }
            }
        }


        switch (modeLeftArea) {
            case MODE_RIGHT_TO_LEFT:
                float maxX = Math.max(first.x, second.x);
                leftAnimStartPoint.x = (int) (maxX * cutView.getWidth());
                leftAnimStartPoint.y = 0;
                leftAnimEndPoint.x = 0;
                leftAnimEndPoint.y = 0;
                break;
            case MODE_LEFT_TO_RIGHT:
                maxX = Math.max(first.x, second.x);
                leftAnimStartPoint.x = 0;
                leftAnimStartPoint.y = 0;
                leftAnimEndPoint.x = (int) (maxX * cutView.getWidth());
                leftAnimEndPoint.y = 0;
                break;
            case MODE_TOP_TO_BOTTOM:
                float minY = Math.min(first.y, second.y);
                leftAnimStartPoint.y = (int) (minY * cutView.getHeight());
                leftAnimStartPoint.x = 0;
                leftAnimEndPoint.y = cutView.getHeight();
                leftAnimEndPoint.x = 0;
                break;
            case MODE_BOTTOM_TO_TOP:
                float maxY = Math.max(first.y, second.y);
                leftAnimStartPoint.y = (int) (maxY * cutView.getHeight());
                leftAnimStartPoint.x = 0;
                leftAnimEndPoint.y = 0;
                leftAnimEndPoint.x = 0;
                break;

        }

        switch (modeRightArea) {
            case MODE_RIGHT_TO_LEFT:
                float minX = Math.min(first.x, second.x);
                rightAnimStartPoint.x = cutView.getWidth();
                rightAnimStartPoint.y = 0;
                rightAnimEndPoint.x = (int) (minX * cutView.getWidth());
                rightAnimEndPoint.y = 0;
                break;
            case MODE_LEFT_TO_RIGHT:
                minX = Math.min(first.x, second.x);
                rightAnimStartPoint.x = (int) (minX * cutView.getWidth());
                rightAnimStartPoint.y = 0;
                rightAnimEndPoint.x = cutView.getWidth();
                rightAnimEndPoint.y = 0;
                break;
            case MODE_TOP_TO_BOTTOM:
                float minY = Math.min(first.y, second.y);
                rightAnimStartPoint.y = (int) (minY * cutView.getHeight());
                rightAnimStartPoint.x = 0;
                rightAnimEndPoint.y = cutView.getHeight();
                rightAnimEndPoint.x = 0;
                break;
            case MODE_BOTTOM_TO_TOP:
                float maxY = Math.max(first.y, second.y);
                rightAnimStartPoint.y = (int) (maxY * cutView.getHeight());
                rightAnimStartPoint.x = 0;
                rightAnimEndPoint.y = 0;
                rightAnimEndPoint.x = 0;
                break;

        }
        cutView.setClipArea(leftPoints, rightPoints, true);

        animator.setDuration(animTime);
        animator.setInterpolator(new AccelerateInterpolator());
        final float leftDistanceX = leftAnimEndPoint.x - leftAnimStartPoint.x;
        final float leftDistanceY = leftAnimEndPoint.y - leftAnimStartPoint.y;
        final float rightDistanceX = rightAnimEndPoint.x - rightAnimStartPoint.x;
        final float rightDistanceY = rightAnimEndPoint.y - rightAnimStartPoint.y;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                float leftAreaTranX = leftDistanceX * value;
                float leftAreaTranY = leftDistanceY * value;
                float rightAreaTranX = rightDistanceX * value;
                float rightAreaTranY = rightDistanceY * value;

                cutView.setLeftAreaTranslate(leftAreaTranX, leftAreaTranY);
                cutView.setRightAreaTranslate(rightAreaTranX, rightAreaTranY);
                cutView.postInvalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                super.onAnimationEnd(animation);
                cutView.setVisibility(View.INVISIBLE);
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                cutView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void initClipArea(CutImageView cutImageView, boolean isNeedInit) {
        cutImageView.setClipArea(leftPoints, rightPoints, isNeedInit);
    }

    public interface AnimCallback {
        void onAnimationEnd();
    }
}
