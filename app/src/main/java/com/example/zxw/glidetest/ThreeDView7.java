package com.example.zxw.glidetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * Created by chenzhifei on 2017/6/25.
 * 使用Graphics.Camera来实现3D效果。
 */

public class ThreeDView7 extends View {

    private int THREE_D_VIEW_WIDTH;
    private int THREE_D_VIEW_HEIGHT;
    private static final int BIT_MAP_WIDTH = 300;
    private static final int BIT_MAP_HEIGHT = 400;
    private float cameraZtranslate = 320; // 3D rotate radius
    private float RADIUS = 280;
    private static final float CENTER_CIRCLE_R = 60f;
    private static final float CENTER_CIRCLE_SHADOW_R = 200f;
    private GestureDetector mGestureDetector;
    private Camera camera = new Camera(); //default location: (0f, 0f, -8.0f), in pixels: -8.0f * 72 = -576f

    private Matrix matrix1 = new Matrix();
    private Matrix matrix2 = new Matrix();
    private Matrix matrix3 = new Matrix();
    private Matrix matrix4 = new Matrix();
    private Matrix matrix5 = new Matrix();
    private Matrix matrix6 = new Matrix();
    private Matrix matrix7 = new Matrix();
    private Paint paint;
    //    private PaintFlagsDrawFilter mSetfil;
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private Bitmap bitmap3;
    private Bitmap bitmap4;
    private Bitmap bitmap5;
    private Bitmap bitmap6;
    private Bitmap bitmap7;

    private float distanceX = 0f;
    private float distanceY = 60f;//倾斜角度
    private float rotateDeg = 0f;

    private float angItem = 51.4f;
    private float angItem2 = 51.6f;
    private float restFloat = 160;//计算每个item的float,每个item距离distanceX=160
    private float distanceToDegree; // cameraZtranslate --> 90度

    private boolean isInfinity = false;
    private float distanceVelocityDecrease = 30f; //decrease 1 pixels/second when a message is handled in the loop
    //loop frequency is 60hz or 120hz when handleMessage(msg) includes UI update code

    private float xVelocity = 0f;
    private float yVelocity = 0f;
    private float jumpItemX = 0f;//记录要跳转的distanceX
    private float jumpItemY = 60f;

    private Handler scrollHandler;//滑动的动画
    private Handler clickHandler;//点击item,跳转页面
    private Handler jumpHandler;//点击某item，跳转到某item
    private Handler reYHandler;//复位Y

    private boolean isScroll = false;

    public ThreeDView7(Context context) {
        this(context, null);
    }

    public ThreeDView7(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreeDView7(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, new SimpleGestureListener());
//        mSetfil = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        camera.setLocation(0f, 0f, -cameraZtranslate);//设置camera的位置
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.w1);
        bitmap = Bitmap.createScaledBitmap(bitmap, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.w2);
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
        bitmap3 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.w3);
        bitmap3 = Bitmap.createScaledBitmap(bitmap3, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
        bitmap4 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.w4);
        bitmap4 = Bitmap.createScaledBitmap(bitmap4, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
        bitmap5 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.w5);
        bitmap5 = Bitmap.createScaledBitmap(bitmap5, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
        bitmap6 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.w6);
        bitmap6 = Bitmap.createScaledBitmap(bitmap6, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
        bitmap7 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.w7);
        bitmap7 = Bitmap.createScaledBitmap(bitmap7, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
        reYHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("jumpHandler", "jumpItemY=" + jumpItemY + "----distanceY=" + distanceY);
                isScroll = true;
                if ((distanceY - jumpItemY >= -10 && distanceY - jumpItemY <= 10) || (jumpItemY - distanceY >= -10 && jumpItemY - distanceY <= 10)) {
                    distanceY = jumpItemY;
                }
                if (distanceY > jumpItemY) {
                    distanceY = distanceY - 5;
                } else if (distanceY < jumpItemY){
                    distanceY = distanceY + 5;
                }
                if (distanceY == jumpItemY) {
                    reYHandler.removeMessages(0);
                    invalidate();
                    stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                    isScroll = false;
                    return false;
                }
                invalidate();
                reYHandler.sendEmptyMessage(0);
                return false;

            }
        });
        jumpHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("jumpHandler", "jumpItemX=" + jumpItemX + "----xx=" + distanceX);
                isScroll = true;
                if ((distanceX - jumpItemX >= -10 && distanceX - jumpItemX <= 10) || (jumpItemX - distanceX >= -10 && jumpItemX - distanceX <= 10)) {
                    distanceX = jumpItemX;
                }
                if (distanceX > jumpItemX) {
                    distanceX = distanceX - (distanceX - jumpItemX) / 5;
                } else if (distanceX < jumpItemX){
                    distanceX = distanceX + (jumpItemX - distanceX) / 5;
                }
                if (distanceX == jumpItemX ) {
                    jumpHandler.removeMessages(0);
                    invalidate();
                    stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                    isScroll = false;
                    return false;
                }
                invalidate();
                jumpHandler.sendEmptyMessage(0);
                return false;

            }
        });
        //顺时针jump
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                return false;
            }
        });
        scrollHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("scrollHandler", "xVelocity=" + xVelocity + "--yVelocity=" + yVelocity);
                isScroll = true;
                distanceX += (xVelocity * 0.004);//数值越小惯性越小
//                distanceY += (yVelocity * 0.004);
                if (distanceY < -30) {
                    distanceY = -30;
                } else if (distanceY > 100) {
                    distanceY = 100;
                }
                if (distanceX > restFloat * 7) {
                    distanceX -= restFloat * 7;
                } else if (distanceX < -restFloat * 7) {
                    distanceX += restFloat * 7;
                }
                ThreeDView7.this.invalidate();
                if (ThreeDView7.this.stateValueListener != null) {
                    ThreeDView7.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                }

                if (xVelocity == 0f ) { // anim will stop
                    reLayout();
                    return true;
                }
                if (ThreeDView7.this.isInfinity) {
                    scrollHandler.sendEmptyMessage(0);
                } else {
                    // decrease the velocities.
                    // 'Math.abs(xVelocity) <= distanceVelocityDecrease' make sure the xVelocity will be 0 finally.
//                    if (xVelocity>=-distanceVelocityDecrease/2&&xVelocity <= distanceVelocityDecrease/2) {
//                        xVelocity=0f;
//                    }
//                    if (xVelocity > 0) {
//                        xVelocity -= distanceVelocityDecrease;
//                    } else if (xVelocity<0){
//                        xVelocity += distanceVelocityDecrease;
//                    }
//                    if (yVelocity>-distanceVelocityDecrease&&yVelocity < distanceVelocityDecrease) {
//                        yVelocity=0;
//                    }
//                    if (yVelocity > 0) {
//                        yVelocity -= distanceVelocityDecrease;
//                    } else if (yVelocity<0){
//                        yVelocity += distanceVelocityDecrease;
//                    }
                    xVelocity = Math.abs(xVelocity) <= distanceVelocityDecrease ? 0f :
                            (xVelocity > 0 ? xVelocity - distanceVelocityDecrease : xVelocity + distanceVelocityDecrease);
                    yVelocity = Math.abs(yVelocity) <= distanceVelocityDecrease ? 0f :
                            (yVelocity > 0 ? yVelocity - distanceVelocityDecrease : yVelocity + distanceVelocityDecrease);
                    scrollHandler.sendEmptyMessage(0);
                }
                return true;
            }
        });
        clickHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                isScroll = true;
                Log.i("clickHandler", "clickHandler");
                if (msg.what == 1) {
                    if ((distanceY > 0 && distanceY < 1) || (distanceY < 0 && distanceY > -1)) {
                        distanceY = 0;
                        clickHandler.removeMessages(1);

                    }
                    if (distanceY > 0) {
                        distanceY--;
                        distanceY *= 0.8f;
                    } else if (distanceY < 0) {
                        distanceY++;
                    } else {
                        clickHandler.removeMessages(1);
                        clickHandler.sendEmptyMessage(0);
                        return false;
                    }
                    Log.i("yidong", distanceY + "");
                    ThreeDView7.this.invalidate();
                    clickHandler.sendEmptyMessage(1);

                } else if (msg.what==0){
                    if (count == 10) {
                        if (ThreeDView7.this.stateValueListener != null) {
                            ThreeDView7.this.stateValueListener.startA();
                        }
                        count =0;
                        isScroll = false;
                        clickHandler.removeCallbacksAndMessages(null);
                    } else {
                        camera.translate(0,0,-180*5);
                        invalidate();
                        count++;
                        clickHandler.sendEmptyMessage(0);
                    }
                } else if (msg.what == 2) {
                    if (count == 10) {
                        reLayoutY();
                        count =0;
                        isScroll = false;
                        clickHandler.removeCallbacksAndMessages(null);
                    } else {
                        camera.translate(0, 0, 180*5);
                        invalidate();
                        count++;
                        clickHandler.sendEmptyMessage(2);
                    }

                }
                return true;
            }
        });
    }
    private int count = 0;//用来计数动画进度
    public void resetStart() {
        clickHandler.sendEmptyMessageDelayed(2,500);
    }

    //设置速度下降率
    public void setDistanceVelocityDecrease(float distanceVelocityDecrease) {
        if (distanceVelocityDecrease <= 0f) {
            this.isInfinity = true;
            this.distanceVelocityDecrease = 0f;
        } else {
            this.isInfinity = false;
            this.distanceVelocityDecrease = distanceVelocityDecrease;
        }
    }

    public void updateXY(float movedX, float movedY) {
        this.distanceX += movedX;
        this.distanceY += movedY;
        if (distanceY > 100) {
            distanceY = 100;
        } else if (distanceY < -30) {
            distanceY = -30;
        }
        if (distanceX > restFloat * 7) {
            distanceX -= restFloat * 7;
        } else if (distanceX < -restFloat * 7) {
            distanceX += restFloat * 7;
        }
        invalidate();
        if (ThreeDView7.this.stateValueListener != null) {
            ThreeDView7.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
        }
    }

    public void updateRotateDeg(float deltaRotateDeg) {
        this.rotateDeg += deltaRotateDeg;
        invalidate();
        if (ThreeDView7.this.stateValueListener != null) {
            ThreeDView7.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
        }
    }

    public void updateCameraZtranslate(float cameraZtranslate) {
        this.cameraZtranslate += cameraZtranslate;
        invalidate();
        if (ThreeDView7.this.stateValueListener != null) {
            ThreeDView7.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
        }
    }


    public void stopScoll() {
        scrollHandler.removeCallbacksAndMessages(null);
        reYHandler.removeMessages(0);
    }
    public void startAnim(float xVelocity, float yVelocity) {
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        reLayoutY();
        scrollHandler.sendEmptyMessage(0);
    }
    public void reLayoutY() {
        if (yVelocity == 0f&& distanceY != jumpItemY) {
            reYHandler.sendEmptyMessage(0);
        }
    }
    //放手时复位
    public void reLayout() {
        Log.i("reLayout", "reLayout");
        if (getItemPosition() == 1) {
            jumpItemX = 0;
        } else if (getItemPosition() == 2) {
            jumpItemX = restFloat * 6;
        } else if (getItemPosition() == 3) {
            jumpItemX = restFloat * 5;
        } else if (getItemPosition() == 4) {
            jumpItemX = restFloat * 4;
        } else if (getItemPosition() == 5) {
            jumpItemX = restFloat * 3;
        } else if (getItemPosition() == 6) {
            jumpItemX = restFloat * 2;
        } else if (getItemPosition() == 7) {
            jumpItemX = restFloat;
        } else if (getItemPosition() == 11) {
            jumpItemX = restFloat * 7;
        } else if (getItemPosition() == -1) {
            jumpItemX = -restFloat * 7;
        } else if (getItemPosition() == -2) {
            jumpItemX = -restFloat;
        } else if (getItemPosition() == -3) {
            jumpItemX = -restFloat * 2;
        } else if (getItemPosition() == -4) {
            jumpItemX = -restFloat * 3;
        } else if (getItemPosition() == -5) {
            jumpItemX = -restFloat * 4;
        } else if (getItemPosition() == -6) {
            jumpItemX = -restFloat * 5;
        } else if (getItemPosition() == -7) {
            jumpItemX = -restFloat * 6 + restFloat * (angItem - angItem2) / angItem;
        }
        jumpHandler.removeMessages(0);
        jumpHandler.sendEmptyMessage(0);

    }

    private OnMyClick onMyClick;

    public void setOnClick(final OnMyClick onClick) {
        this.onMyClick = onClick;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void setCurrentItem(int position) {
        if (position == 1) {
            jumpItemX = 0;
        } else if (position == 2) {
            jumpItemX = restFloat * 6;
        } else if (position == 3) {
            jumpItemX = restFloat * 5;
        } else if (position == 4) {
            jumpItemX = restFloat * 4;
        } else if (position == 5) {
            jumpItemX = restFloat * 3;
        } else if (position == 6) {
            jumpItemX = restFloat * 2;
        } else if (position == 7) {
            jumpItemX = restFloat;
        }
//        jumpHandler.removeMessages(0);
        jumpHandler.sendEmptyMessage(0);
    }

    public int getItemPosition() {
        int position = 1;
        if (distanceX > -restFloat / 2 && distanceX <= restFloat / 2) {
            position = 1;
        } else if (distanceX > restFloat * 13 / 2 && distanceX <= restFloat * 7) {
            position = 11;
        } else if ((distanceX >= -restFloat * 7 && distanceX <= -restFloat * 13 / 2)) {
            position = -1;
        } else if (distanceX > -restFloat * 3 / 2 && distanceX <= -restFloat / 2) {
            position = -2;
        } else if (distanceX <= restFloat * 13 / 2 && distanceX > restFloat * 11 / 2) {
            position = 2;
        } else if (distanceX <= restFloat * 11 / 2 && distanceX > restFloat * 9 / 2) {
            position = 3;
        } else if (distanceX > -restFloat * 5 / 2 && distanceX <= -restFloat * 3 / 2) {
            position = -3;
        } else if (distanceX <= restFloat * 9 / 2 && distanceX > restFloat * 7 / 2) {
            position = 4;
        } else if (distanceX > -restFloat * 7 / 2 && distanceX <= -restFloat * 5 / 2) {
            position = -4;
        } else if (distanceX > -restFloat * 9 / 2 && distanceX <= -restFloat * 7 / 2) {
            position = -5;
        } else if (distanceX <= restFloat * 7 / 2 && distanceX > restFloat * 5 / 2) {
            position = 5;
        } else if (distanceX > -restFloat * 11 / 2 && distanceX <= -restFloat * 9 / 2) {
            position = -6;
        } else if (distanceX <= restFloat * 5 / 2 && distanceX > restFloat * 3 / 2) {
            position = 6;//distanceX为正数
        } else if (distanceX > -restFloat * 13 / 2 && distanceX <= -restFloat * 11 / 2) {
            position = -7;
        } else if (distanceX <= restFloat * 3 / 2 && distanceX > restFloat / 2) {
            position = 7;//distanceX为正数
        }
//        Log.i("getItemPosition","getItemPosition()=" + position);
        return position;
    }

    public interface OnMyClick {
        void onMyClick(View v, int position);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        THREE_D_VIEW_WIDTH = w; //params value is in pixels not dp
        THREE_D_VIEW_HEIGHT = h;
//        cameraZtranslate = Math.min(w, h) / 2;
        distanceToDegree = 90f / RADIUS;//NOT changed when cameraZtranslate changed in the future
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // convert distances in pixels into degrees
        float xDeg = -distanceY * distanceToDegree;
        float yDeg = distanceX * distanceToDegree;
        Log.i("xydeg", "xdeg=" + xDeg + "---ydeg=" + yDeg);
        paint.setAntiAlias(true);
        paint.setDither(true);
//        canvas.setDrawFilter(mSetfil);

        setMatrix1(xDeg, yDeg);
        setMatrix2(xDeg, yDeg);
        setMatrix3(xDeg, yDeg);
        setMatrix4(xDeg, yDeg);
        setMatrix5(xDeg, yDeg);
        setMatrix6(xDeg, yDeg);
        setMatrix7(xDeg, yDeg);
        // translate canvas to locate the bitmap in center of the ThreeDViwe
        canvas.translate((THREE_D_VIEW_WIDTH - BIT_MAP_WIDTH) / 2f, (THREE_D_VIEW_HEIGHT - BIT_MAP_HEIGHT) / 2f);

        drawCanvas(canvas, xDeg, yDeg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

    }

    //51.4  51.6
    private void setMatrix1(float xDeg, float yDeg) {
        matrix1.reset();
        camera.save(); // save the original state(no any transformation) so you can restore it after any changes
        camera.rotateX(xDeg); // it will lead to rotate Y and Z axis
        camera.rotateY(yDeg); // it will just lead to rotate Z axis, NOT X axis. BUT rotateZ(deg) will lead to nothing
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix1);
        camera.restore(); // restore to the original state after uses for next use

        // translate coordinate origin the camera's transformation depends on to center of the bitmap
        matrix1.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix1.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix2(float xDeg, float yDeg) {
        matrix2.reset();
        camera.save(); // save the original state(no any transformation) so you can restore it after any changes
        camera.rotateX(xDeg); // it will lead to rotate Y and Z axis
        camera.rotateY(yDeg + angItem); // it will just lead to rotate Z axis, NOT X axis. BUT rotateZ(deg) will lead to nothing
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix2);
        camera.restore(); // restore to the original state after uses for next use

        // translate coordinate origin the camera's transformation depends on to center of the bitmap
        matrix2.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix2.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix3(float xDeg, float yDeg) {
        matrix3.reset();
        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg + angItem * 2);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix3);
        camera.restore();
        matrix3.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix3.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }


    private void setMatrix4(float xDeg, float yDeg) {
        matrix4.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg + angItem * 3);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix4);
        camera.restore();

        matrix4.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix4.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix5(float xDeg, float yDeg) {
        matrix5.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg + angItem * 4);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix5);
        camera.restore();

        matrix5.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix5.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }


    private void setMatrix6(float xDeg, float yDeg) {
        matrix6.reset();
        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg + angItem * 5);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix6);
        camera.restore();
        matrix6.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix6.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix7(float xDeg, float yDeg) {
        matrix7.reset();
        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg + angItem * 5 + angItem2);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix7);
        camera.restore();
//        matrixRight.setRotate(0,0,180f);
//        matrixRight.preRotate(0, 0, 0);
//        matrixRight.postRotate(180f, 0, 0);
//        matrixRight.setRotate(180f);
        matrix7.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix7.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }


    private void drawCanvas(Canvas canvas, float xDeg, float yDeg) {
        if (getItemPosition() == 1 || getItemPosition() == -1 || getItemPosition() == 11) {
            canvas.drawBitmap(bitmap4, matrix4, paint);
            canvas.drawBitmap(bitmap5, matrix5, paint);
            canvas.drawBitmap(bitmap6, matrix6, paint);
            canvas.drawBitmap(bitmap7, matrix7, paint);
            canvas.drawBitmap(bitmap3, matrix3, paint);
            canvas.drawBitmap(bitmap2, matrix2, paint);
            canvas.drawBitmap(bitmap, matrix1, paint);
        } else if (getItemPosition() == 2 || getItemPosition() == -2) {
            canvas.drawBitmap(bitmap5, matrix5, paint);
            canvas.drawBitmap(bitmap6, matrix6, paint);
            canvas.drawBitmap(bitmap4, matrix4, paint);
            canvas.drawBitmap(bitmap7, matrix7, paint);
            canvas.drawBitmap(bitmap3, matrix3, paint);
            canvas.drawBitmap(bitmap, matrix1, paint);
            canvas.drawBitmap(bitmap2, matrix2, paint);
        } else if (getItemPosition() == 3 || getItemPosition() == -3) {
            canvas.drawBitmap(bitmap6, matrix6, paint);
            canvas.drawBitmap(bitmap5, matrix5, paint);
            canvas.drawBitmap(bitmap7, matrix7, paint);
            canvas.drawBitmap(bitmap, matrix1, paint);
            canvas.drawBitmap(bitmap4, matrix4, paint);
            canvas.drawBitmap(bitmap2, matrix2, paint);
            canvas.drawBitmap(bitmap3, matrix3, paint);
        } else if (getItemPosition() == 4 || getItemPosition() == -4) {
            canvas.drawBitmap(bitmap7, matrix7, paint);
            canvas.drawBitmap(bitmap6, matrix6, paint);
            canvas.drawBitmap(bitmap, matrix1, paint);
            canvas.drawBitmap(bitmap2, matrix2, paint);
            canvas.drawBitmap(bitmap5, matrix5, paint);
            canvas.drawBitmap(bitmap3, matrix3, paint);
            canvas.drawBitmap(bitmap4, matrix4, paint);
        } else if (getItemPosition() == 5 || getItemPosition() == -5) {
            canvas.drawBitmap(bitmap, matrix1, paint);
            canvas.drawBitmap(bitmap7, matrix7, paint);
            canvas.drawBitmap(bitmap2, matrix2, paint);
            canvas.drawBitmap(bitmap3, matrix3, paint);
            canvas.drawBitmap(bitmap6, matrix6, paint);
            canvas.drawBitmap(bitmap4, matrix4, paint);
            canvas.drawBitmap(bitmap5, matrix5, paint);
        } else if (getItemPosition() == 6 || getItemPosition() == -6) {
            canvas.drawBitmap(bitmap2, matrix2, paint);
            canvas.drawBitmap(bitmap3, matrix3, paint);
            canvas.drawBitmap(bitmap4, matrix4, paint);
            canvas.drawBitmap(bitmap, matrix1, paint);
            canvas.drawBitmap(bitmap5, matrix5, paint);
            canvas.drawBitmap(bitmap7, matrix7, paint);
            canvas.drawBitmap(bitmap6, matrix6, paint);
        } else if (getItemPosition() == 7 || getItemPosition() == -7) {
            canvas.drawBitmap(bitmap3, matrix3, paint);
            canvas.drawBitmap(bitmap2, matrix2, paint);
            canvas.drawBitmap(bitmap4, matrix4, paint);
            canvas.drawBitmap(bitmap5, matrix5, paint);
            canvas.drawBitmap(bitmap6, matrix6, paint);
            canvas.drawBitmap(bitmap, matrix1, paint);
            canvas.drawBitmap(bitmap7, matrix7, paint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (scrollHandler != null) {
            scrollHandler.removeCallbacksAndMessages(null);
        }
        if (jumpHandler != null) {
            jumpHandler.removeCallbacksAndMessages(null);
        }
        if (reYHandler != null) {
            reYHandler.removeCallbacksAndMessages(null);
        }
        if (clickHandler != null) {
            clickHandler.removeCallbacksAndMessages(null);
        }
    }


    public interface StateValueListener {
        void stateValue(float distanceX, float distanceY, float rotateDegree, float cameraZtranslate);

        void startA();
    }

    private StateValueListener stateValueListener;

    public void setStateValueListener(StateValueListener stateValueListener) {
        this.stateValueListener = stateValueListener;
    }

    //----------------------------------------------------------------------
    private boolean moreThan2Fingers = false;

    private float oldX = 0f;
    private float oldY = 0f;

    // 如果在连续两次MOVE事件中，转动的角度超过180度，这次转动效果或完全被忽略或小于实际角度。但这几乎是不可能的。
    private static final float MAX_DEGREES_IN_TWO_MOVE_EVENTS = 180f;
    private static final float REFERENCE_DEGREES = 360f - MAX_DEGREES_IN_TWO_MOVE_EVENTS;
    private static final float RADIAN_TO_DEGREE = (float) (180.0 / Math.PI);
    private float oldTanDeg = 0f;

    private float oldScaledX = 0f;
    private float oldScaledY = 0f;
    private float old2FingersDistance = 0f;

    private long oldTimestamp = 0;

    private VelocityTracker vt = VelocityTracker.obtain();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 2) {
            moreThan2Fingers = true;
            if (twoFingersGestureListener != null) {
                twoFingersGestureListener.onCancel();
            }
        }
        vt.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                oldX = event.getX(0);
                oldY = event.getY(0);
                oldTimestamp = event.getDownTime();
                if (twoFingersGestureListener != null) {
                    twoFingersGestureListener.onDown(oldX, oldY, oldTimestamp);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (moreThan2Fingers) {
                    return true;
                }

                // 第二个触点一出现就清空。当然上次up清理也行。
                oldTanDeg = 0f;
                oldScaledX = 0f;
                oldScaledY = 0f;
                old2FingersDistance = 0f;

                oldX = (event.getX(0) + event.getX(1)) / 2f;
                oldY = (event.getY(0) + event.getY(1)) / 2f;
                oldTimestamp = event.getEventTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (moreThan2Fingers) {
                    return true;
                }
                long newTimestamp = event.getEventTime();
                long currDeltaMilliseconds = newTimestamp - oldTimestamp;
                oldTimestamp = newTimestamp;

                float newX, newY;
                // handle 2 fingers touch
                if (event.getPointerCount() == 2) {
                    // handle rotate
                    float currDeltaRotatedDeg = getRotatedDegBetween2Events(event);
                    // handle scale
                    float deltaScaledX = getDeltaScaledXBetween2Events(event);
                    float deltaScaledY = getDeltaScaledYBetween2Events(event);
                    float currDeltaScaledDistance = getScaledDistanceBetween2Events(event);

                    if (this.twoFingersGestureListener != null) {
                        twoFingersGestureListener.onScaled(deltaScaledX, deltaScaledY, currDeltaScaledDistance, currDeltaMilliseconds);
                        twoFingersGestureListener.onRotated(currDeltaRotatedDeg, currDeltaMilliseconds);
                    }
                    // handle move
                    newX = (event.getX(0) + event.getX(1)) / 2f;
                    newY = (event.getY(0) + event.getY(1)) / 2f;
                } else {
                    newX = event.getX(0);
                    newY = event.getY(0);
                }

                float currDeltaMovedX = newX - oldX;
                float currDeltaMovedY = newY - oldY;
                oldX = newX;
                oldY = newY;

                if (this.twoFingersGestureListener != null) {
                    twoFingersGestureListener.onMoved(currDeltaMovedX, currDeltaMovedY, currDeltaMilliseconds);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (moreThan2Fingers) {
                    return true;
                }

                if (event.getActionIndex() == 0) {
                    oldX = event.getX(1);
                    oldY = event.getY(1);
                } else if (event.getActionIndex() == 1) {
                    oldX = event.getX(0);
                    oldY = event.getY(0);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (moreThan2Fingers) {
                    moreThan2Fingers = false;
                    return true;
                }
                vt.computeCurrentVelocity(1000);
                float yVelocity = vt.getYVelocity();
                float xVelocity = vt.getXVelocity();
                vt.clear();

                if (twoFingersGestureListener != null) {
                    twoFingersGestureListener.onUp(oldX, oldY, oldTimestamp, xVelocity, yVelocity);
                    Log.i("nima", "oldX=" + oldX + "---oldY=" + oldY + "---oldTimestamp=" + oldTimestamp + "--xVelocity=" + xVelocity + "---yVelocity=" + yVelocity);
                }
                if (xVelocity == 0 && yVelocity == 0) {

                } else {
                    return false;
                }
                break;
        }
        this.mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    private float getRotatedDegBetween2Events(MotionEvent event) {
        float spanX = event.getX(1) - event.getX(0);
        float spanY = event.getY(1) - event.getY(0);
        float tanDeg = (float) Math.atan2(spanY, spanX) * RADIAN_TO_DEGREE;
        if (oldTanDeg == 0f
                || (tanDeg - oldTanDeg > REFERENCE_DEGREES && tanDeg >= 0f && oldTanDeg <= 0f)
                || (oldTanDeg - tanDeg > REFERENCE_DEGREES && oldTanDeg >= 0f && tanDeg <= 0f)) {

            oldTanDeg = tanDeg;
            return 0f;
        } else {
            float deltaDeg = tanDeg - oldTanDeg;
            oldTanDeg = tanDeg;
            return deltaDeg;
        }
    }

    private float getDeltaScaledXBetween2Events(MotionEvent event) {
        float newScaledX = Math.abs(event.getX(1) - event.getX(0));
        if (oldScaledX == 0f) {
            oldScaledX = newScaledX;
            return 0f;
        } else {
            float deltaScaledX = newScaledX - oldScaledX;
            oldScaledX = newScaledX;
            return deltaScaledX;
        }
    }

    private float getDeltaScaledYBetween2Events(MotionEvent event) {
        float newScaledY = Math.abs(event.getY(1) - event.getY(0));
        if (oldScaledY == 0f) {
            oldScaledY = newScaledY;
            return 0f;
        } else {
            float deltaScaledY = newScaledY - oldScaledY;
            oldScaledY = newScaledY;
            return deltaScaledY;
        }
    }

    private float getScaledDistanceBetween2Events(MotionEvent event) {
        float newScaledX = event.getX(1) - event.getX(0), newScaledY = event.getY(1) - event.getY(0);
        float new2FingerDistance = (float) Math.sqrt((newScaledX * newScaledX) + (newScaledY * newScaledY));
        if (old2FingersDistance == 0f) {
            old2FingersDistance = new2FingerDistance;
            return 0f;
        } else {
            float deltaDistance = new2FingerDistance - old2FingersDistance;
            old2FingersDistance = new2FingerDistance;
            return deltaDistance;
        }
    }

    public interface TwoFingersGestureListener {
        void onDown(float downX, float downY, long downTime);

        void onMoved(float deltaMovedX, float deltaMovedY, long deltaMilliseconds);

        void onRotated(float deltaRotatedDeg, long deltaMilliseconds);

        void onScaled(float deltaScaledX, float deltaScaledY, float deltaScaledDistance, long deltaMilliseconds);

        // velocity: pixels/second   degrees/second
        void onUp(float upX, float upY, long upTime, float xVelocity, float yVelocity);

        /**
         * invoked when more than 2 findgers
         */
        void onCancel();
    }

    public class SimpleGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            //用户按下屏幕就会触发；
            Log.i("Gesture-onDown", "onDown");
            stopScoll();
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            //如果是按下的时间超过瞬间，而且在按下的时候没有松开或者是拖动的，那么onShowPress就会执行，具体这个瞬间是多久，我也不清楚呃
            Log.i("Gesture-onShowPress", "onShowPress");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //从名子也可以看出,一次单独的轻击抬起操作,也就是轻击一下屏幕，立刻抬起来，
            // 才会有这个触发，当然,如果除了Down以外还有其它操作,那就不再算是Single操作了,所以也就不会触发这个事件
            //增加点击坐标判断
            float x = e.getX();
            float y = e.getY();
            if (!isScroll) {//判断view不在滑动时
                checkClickItem(e);

            }

//            onMyClick.onMyClick(null,getItemPosition());
            Log.i("Gesture-onSingleTapUp", "x=" + x + "---y=" + y + "--jumpItemX=" + jumpItemX);
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //在屏幕上拖动事件。无论是用手拖动view，或者是以抛的动作滚动，都会多次触发,这个方法
            Log.i("Gesture-onScroll", "e1.getAction()=" + e1.getAction() + "---e2.getac()=" + e2.getAction()+"--distanceX="+distanceX+"--distanceY="+distanceY);
//            if (distanceY < -100) {
//                distanceY = -100;
//            } else if(distanceY>30){
//                distanceY=30;
//            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //长按触摸屏，超过一定时长，就会触发这个事件
            Log.i("Gesture-onLongPress", "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //滑屏，用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
            Log.i("Gesture-onFling", "onFling");
            return false;
        }
    }

    public void checkClickItem(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        //上1
        if (x >= 45 && x < 105) {
            if (y > 120 && y < 240) {
                int num = Math.abs(getItemPosition()) + 4;
                if (num > 6) {
                    num -= 7;
                }
                jumpItemX = -restFloat * num;
                jumpHandler.sendEmptyMessage(0);
            }
        }
        //上2
        if (x >= 107 && x < 370) {
            if (y > 60 && y < 290) {
                int num = Math.abs(getItemPosition()) + 3;
                if (num > 6) {
                    num -= 7;
                }
                jumpItemX = -restFloat * num;
                jumpHandler.sendEmptyMessage(0);
            }
        }
        //上3
        if (x >= 390 && x < 650) {
            if (y > 60 && y < 230) {
                int num = Math.abs(getItemPosition()) + 2;
                if (num > 6) {
                    num -= 7;
                }
                jumpItemX = -restFloat * num;
                jumpHandler.sendEmptyMessage(0);
            }
        }
        //上4
        if (x >= 650 && x < 720) {
            if (y > 120 && y < 240) {
                int num = Math.abs(getItemPosition()) + 1;
                if (num > 6) {
                    num -= 7;
                }
                jumpItemX = -restFloat * num;
                jumpHandler.sendEmptyMessage(0);
            }
        }
        //下左
        if (x >= 45 && x < 230) {
            if (y > 210 && y < 660) {
                int num = Math.abs(getItemPosition()) + 5;
                if (num > 6) {
                    num -= 7;
                }
                jumpItemX = -restFloat * num;
                jumpHandler.sendEmptyMessage(0);
            }
        }
        //中
        if (x >= 230 && x <= 533) {
            if (y >= 370 && y <= 750) {
                if (false) {//判断是否在滑动

                }
                clickHandler.sendEmptyMessage(1);
            }
        }
        //下右
        if (x >= 533 && x < 720) {
            if (y > 210 && y < 660) {
                int num = Math.abs(getItemPosition());
                if (num > 6) {
                    num -= 7;
                }
                jumpItemX = -restFloat * num;
                jumpHandler.sendEmptyMessage(0);
            }
        }
//        Matrix mInvertPhotoMatrix = new Matrix();
//        matrix1.invert(mInvertPhotoMatrix);//mPhotoMatrix为图片变化矩阵，计算得到逆矩阵mInvertPhotoMatrix
//        float[] invertPoint = new float[2];
//        mInvertPhotoMatrix.mapPoints(invertPoint, new float[]{x, y});//对点击点进行逆矩阵变换
////photoRectSrc为图片未变换前的Rect，如果包含点击点即可认为该在图片区域
//        if (photoRectSrc.contains(invertPoint[0], invertPoint[1])) {
//            actionMode = MODE_DRAG;
//        }
    }

    private TwoFingersGestureListener twoFingersGestureListener;

    public void setTwoFingersGestureListener(TwoFingersGestureListener l) {
        this.twoFingersGestureListener = l;
    }
}
