package com.example.zxw.glidetest;

import android.graphics.Canvas;

/**
 * Created by ZXW23 on 2017/9/2.
 */

public interface ThreeDView {
    void activityResult();
    //设置速度下降率
    void setDistanceVelocityDecrease(float distanceVelocityDecrease);
    //手指移动
    void updateXY(float movedX, float movedY);
    //更新双指旋转角度
    void updateRotateDeg(float deltaRotateDeg);
    //更新View的半径
    void updateCameraZtranslate(float cameraZtranslate);
    //停止滑动
    void stopScroll();
    //惯性滑动动画
    void startScroll(float xVelocity, float yVelocity);
    //Y复位
    public void reLayoutY();
    //View复位
    public void reLayout();
    //跳转下一个
    public void jumpNext();
    //跳转上一个
    public void jumpPre();
    //跳转某一个
    public void setCurrentItem(int position);
    //根据position设置jumpItemX--需要跳转的item的distanceX
    public boolean setJumpItemX(int position);
    //得到item的标准位置
    public float getItemFloat();
    //获取item的编号
    public int getItemPosition();

    void setMatrix(float xDeg, float yDeg);
    //绘制顺序
    void drawBitmap(Canvas canvas, int[] num);
    void drawCanvas(Canvas canvas);
}
