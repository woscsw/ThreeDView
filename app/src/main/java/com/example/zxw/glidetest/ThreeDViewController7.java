package com.example.zxw.glidetest;

import android.util.Log;

/**
 * Created by chenzhifei on 2017/6/30.
 * control the ThreeDView
 */

public class ThreeDViewController7 {

    private ThreeDView7 threeDView;


    public ThreeDViewController7(ThreeDView7 threeDView) {
        this.threeDView = threeDView;
        this.threeDView.setTwoFingersGestureListener(new ThreeDView7.TwoFingersGestureListener() {
            @Override
            public void onDown(float downX, float downY, long downTime) {
//                ThreeDViewController7.this.threeDView.stopScroll();
            }

            @Override
            public void onMoved(float deltaMovedX, float deltaMovedY, long deltaMilliseconds) {
//                if (deltaMovedY < -100) {
//                    deltaMovedY = -100;
//                } else if(deltaMovedY>30){
//                    deltaMovedY=30;
//                }
            }

            @Override
            public void onRotated(float deltaRotatedDeg, long deltaMilliseconds) {
                //双指旋转
//                ThreeDViewController7.this.threeDView.updateRotateDeg(deltaRotatedDeg);
            }

            @Override
            public void onScaled(float deltaScaledX, float deltaScaledY, float deltaScaledDistance, long deltaMilliseconds) {
                //双指缩放
//                ThreeDViewController7.this.threeDView.updateCameraZtranslate(deltaScaledDistance);
            }

            @Override
            public void onUp(float upX, float upY, long upTime, float xVelocity, float yVelocity) {
                if (xVelocity == 0 && yVelocity == 0) {
                    ThreeDViewController7.this.threeDView.reLayout();
                    ThreeDViewController7.this.threeDView.reLayoutY();
                    return;//过滤掉点击
                }
                ThreeDViewController7.this.threeDView.startScroll(xVelocity, 0);//限制了y轴的滑动
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
