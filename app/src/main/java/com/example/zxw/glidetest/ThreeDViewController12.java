package com.example.zxw.glidetest;

import android.util.Log;

/**
 * Created by chenzhifei on 2017/6/30.
 * control the ThreeDView
 */

public class ThreeDViewController12 {

    private ThreeDView12 threeDView;


    public ThreeDViewController12(ThreeDView12 threeDView) {
        this.threeDView = threeDView;
        this.threeDView.setTwoFingersGestureListener(new ThreeDView12.TwoFingersGestureListener() {
            @Override
            public void onDown(float downX, float downY, long downTime) {
//                ThreeDViewController12.this.threeDView.stopScoll();
            }

            @Override
            public void onMoved(float deltaMovedX, float deltaMovedY, long deltaMilliseconds) {
                if (deltaMovedY < -100) {
                    deltaMovedY = -100;
                } else if(deltaMovedY>30){
                    deltaMovedY=30;
                }
                ThreeDViewController12.this.threeDView.updateXY(deltaMovedX, deltaMovedY);//限制了y轴的滑动
            }

            @Override
            public void onRotated(float deltaRotatedDeg, long deltaMilliseconds) {
                //双指旋转
//                ThreeDViewController12.this.threeDView.updateRotateDeg(deltaRotatedDeg);
            }

            @Override
            public void onScaled(float deltaScaledX, float deltaScaledY, float deltaScaledDistance, long deltaMilliseconds) {
                //双指缩放
                ThreeDViewController12.this.threeDView.updateCameraZtranslate(deltaScaledDistance);
            }

            @Override
            public void onUp(float upX, float upY, long upTime, float xVelocity, float yVelocity) {
                if (xVelocity == 0 && yVelocity == 0) {
                    ThreeDViewController12.this.threeDView.reLayout();
                    ThreeDViewController12.this.threeDView.reLayoutY();
                    return;//过滤掉点击
                }
                ThreeDViewController12.this.threeDView.startAnim(xVelocity, 0);//限制了y轴的滑动
                Log.i("onUp", "upX=" + upX+"--upY"+upY+"--upTime="+upTime+"--xVelocity="+xVelocity+"--yVelocity="+yVelocity);
            }

            @Override
            public void onCancel() {}
        });
//        threeDView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }


}
