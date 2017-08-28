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
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * Created by chenzhifei on 2017/6/25.
 * 使用Graphics.Camera来实现3D效果。
 */

public class ThreeDView8 extends View {

    private int THREE_D_VIEW_WIDTH;
    private int THREE_D_VIEW_HEIGHT;
    private static final int BIT_MAP_WIDTH = 180;
    private static final int BIT_MAP_HEIGHT = 200;
    private static final float CENTER_CIRCLE_R = 60f;
    private static final float CENTER_CIRCLE_SHADOW_R = 200f;

    private Camera camera = new Camera(); //default location: (0f, 0f, -8.0f), in pixels: -8.0f * 72 = -576f

    private Matrix matrixFront = new Matrix();
    private Matrix matrixFront2 = new Matrix();
    private Matrix matrixBack = new Matrix();
    private Matrix matrixBack2 = new Matrix();
    private Matrix matrixLeft = new Matrix();
    private Matrix matrixLeft2 = new Matrix();
    private Matrix matrixRight = new Matrix();
    private Matrix matrixRight2 = new Matrix();
    private Matrix matrixTop = new Matrix();
    private Matrix matrixBottom = new Matrix();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private Bitmap bitmap3;
    private Bitmap bitmap4;
    private Bitmap bitmap5;
    private Bitmap bitmap6;
    private Bitmap bitmap7;
    private Bitmap bitmap8;

    private float distanceX = 0f;
    private float distanceY = 40f;//倾斜角度
    private float rotateDeg = 0f;
    private float cameraZtranslate = 200; // 3D rotate radius

    private float distanceToDegree; // cameraZtranslate --> 90度

    private boolean isInfinity = false;
    private float distanceVelocityDecrease = 1f; //decrease 1 pixels/second when a message is handled in the loop
                    //loop frequency is 60hz or 120hz when handleMessage(msg) includes UI update code

    private float xVelocity = 0f;
    private float yVelocity = 0f;

    private Handler animHandler;
    private Handler touchHandler;

    public ThreeDView8(Context context) {
        this(context, null);
    }

    public ThreeDView8(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreeDView8(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image1);
        bitmap = Bitmap.createScaledBitmap(bitmap, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image2);
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);
        bitmap3 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image3);
        bitmap3 = Bitmap.createScaledBitmap(bitmap3, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);
        bitmap4 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image4);
        bitmap4 = Bitmap.createScaledBitmap(bitmap4, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);
        bitmap5 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image5);
        bitmap5 = Bitmap.createScaledBitmap(bitmap5, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);
        bitmap6 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image6);
        bitmap6 = Bitmap.createScaledBitmap(bitmap6, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);
        bitmap7 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image7);
        bitmap7 = Bitmap.createScaledBitmap(bitmap7, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);
        bitmap8 = BitmapFactory.decodeResource(context.getResources(), R.mipmap.image8);
        bitmap8 = Bitmap.createScaledBitmap(bitmap8, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);

        animHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                distanceX += (xVelocity * 0.0016);//数值越小惯性越小
                distanceY += (yVelocity * 0.0016);
                ThreeDView8.this.invalidate();
                if (ThreeDView8.this.stateValueListener != null) {
                    ThreeDView8.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                }


                if (xVelocity == 0f && yVelocity == 0f) { // anim will stop
                    return true;
                }
                if (ThreeDView8.this.isInfinity) {
                    ThreeDView8.this.sendMsgForAnim();
                } else {
                    // decrease the velocities.
                    // 'Math.abs(xVelocity) <= distanceVelocityDecrease' make sure the xVelocity will be 0 finally.
                    xVelocity =  Math.abs(xVelocity) <= distanceVelocityDecrease ? 0f :
                            (xVelocity > 0 ? xVelocity - distanceVelocityDecrease : xVelocity + distanceVelocityDecrease);
                    yVelocity = Math.abs(yVelocity) <= distanceVelocityDecrease ? 0f :
                            (yVelocity > 0 ? yVelocity - distanceVelocityDecrease : yVelocity + distanceVelocityDecrease);
                    ThreeDView8.this.sendMsgForAnim();
                }
                return true;
            }
        });

        touchHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (ThreeDView8.this.stateValueListener != null) {
                    ThreeDView8.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                }
                return true;
            }
        });
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
        if (distanceX>800) {
            distanceX -= 800;
        } else if (distanceX < -800) {
            distanceX +=800;
        }
        invalidate();
        touchHandler.sendEmptyMessage(0);
    }

    public void updateRotateDeg(float deltaRotateDeg) {
        this.rotateDeg += deltaRotateDeg;
        invalidate();
        touchHandler.sendEmptyMessage(0);
    }

    public void updateCameraZtranslate(float cameraZtranslate) {
        this.cameraZtranslate += cameraZtranslate;
        invalidate();
        touchHandler.sendEmptyMessage(0);
    }

    private void sendMsgForAnim() {
        animHandler.sendEmptyMessage(0);
    }

    public void stopAnim() {
        animHandler.removeCallbacksAndMessages(null);
    }

    public void startAnim(float xVelocity, float yVelocity) {
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;

//        sendMsgForAnim();
        reLayout();
    }
    //放手时复位
    public void reLayout() {
        if (getItemPosition() == 1) {
            distanceX=0;
            ThreeDView8.this.invalidate();
        }else if (getItemPosition() == 2) {
            distanceX=-100;
            ThreeDView8.this.invalidate();
        }else if (getItemPosition() == 3) {
            distanceX=-600;
            ThreeDView8.this.invalidate();
        }else if (getItemPosition() == 4) {
            distanceX=-700;
            ThreeDView8.this.invalidate();
        }else if (getItemPosition() == 5) {
            distanceX=-200;
            ThreeDView8.this.invalidate();
        }else if (getItemPosition() == 6) {
            distanceX=-300;
            ThreeDView8.this.invalidate();
        }else if (getItemPosition() == 7) {
            distanceX=-400;
            ThreeDView8.this.invalidate();
        }else if (getItemPosition() == 8) {
            distanceX=-500;
            ThreeDView8.this.invalidate();
        }
    }
    private OnMyClick onMyClick;
    public void setOnClick(final OnMyClick onClick) {
        this.onMyClick = onClick;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onMyClick(v,getItemPosition());
            }
        });
    }

    public int getItemPosition() {
        int position = 0;
        if ((distanceX > -50 && distanceX <= 50)||(distanceX>=-800&&distanceX<=-750)||(distanceX>750&&distanceX<=800)) {
            position = 1;
        }else  if (distanceX > -150 && distanceX <= -50||distanceX <=750 && distanceX  >650) {
            position = 2;
        }else  if (distanceX > -250 && distanceX <= -150||distanceX <=650 && distanceX >550) {
            position = 5;
        }else  if (distanceX > -350 && distanceX <= -250||distanceX <=550 && distanceX >450) {
            position = 6;
        }else  if (distanceX > -450 && distanceX <= -350||distanceX<=450 && distanceX > 350) {
            position = 7;
        }else  if (distanceX > -550 && distanceX <= -450||distanceX <=350 && distanceX >250) {
            position = 8;
        }else  if (distanceX > -650 && distanceX <= -550||distanceX <=250 && distanceX >150) {
            position = 3;
        }else  if (distanceX > -750 && distanceX <= -650||distanceX <=150 && distanceX >50) {
            position = 4;
        }
        return position;
    }
    public interface OnMyClick {
        void onMyClick(View v,int position);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        THREE_D_VIEW_WIDTH = w; //params value is in pixels not dp
        THREE_D_VIEW_HEIGHT = h;
//        cameraZtranslate = Math.min(w, h) / 2;
        distanceToDegree = 90f / cameraZtranslate;//NOT changed when cameraZtranslate changed in the future
    }
    @Override
    protected void onDraw(Canvas canvas) {
        // convert distances in pixels into degrees
        float xDeg = -distanceY * distanceToDegree;
        float yDeg = distanceX * distanceToDegree;
        paint.setAntiAlias(true);

//        setMatrix_test(xDeg, yDeg);
        setMatrixFront(xDeg, yDeg);
        setMatrixBack(xDeg, yDeg);
        setMatrixFront2(xDeg, yDeg);
        setMatrixBack2(xDeg, yDeg);
        setMatrixLeft(xDeg, yDeg);
        setMatrixLeft2(xDeg, yDeg);
        setMatrixRight(xDeg, yDeg);
        setMatrixRight2(xDeg, yDeg);

//        setMatrixTop(xDeg, yDeg);
//        setMatrixBottom(xDeg, yDeg);

        // translate canvas to locate the bitmap in center of the ThreeDViwe
        canvas.translate((THREE_D_VIEW_WIDTH - BIT_MAP_WIDTH) / 2f, (THREE_D_VIEW_HEIGHT - BIT_MAP_HEIGHT) / 2f);

        drawCanvas(canvas, xDeg, yDeg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
//        drawCanvas_test(canvas, xDeg, yDeg);
    }

    private void setMatrix_test(float xDeg, float yDeg) {
        matrixFront.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixFront);
        camera.restore();

        matrixFront.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixFront.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }


    private void setMatrixFront(float xDeg, float yDeg) {
        matrixFront.reset();

        camera.save(); // save the original state(no any transformation) so you can restore it after any changes
        camera.rotateX(xDeg); // it will lead to rotate Y and Z axis
        camera.rotateY(yDeg); // it will just lead to rotate Z axis, NOT X axis. BUT rotateZ(deg) will lead to nothing
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixFront);
        camera.restore(); // restore to the original state after uses for next use

        // translate coordinate origin the camera's transformation depends on to center of the bitmap
        matrixFront.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixFront.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }
    private void setMatrixLeft(float xDeg, float yDeg) {
        matrixLeft.reset();
        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg - 90f);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixLeft);
        camera.restore();
        matrixLeft.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixLeft.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }
    private void setMatrixFront2(float xDeg, float yDeg) {
        matrixFront2.reset();
        camera.save(); // save the original state(no any transformation) so you can restore it after any changes
        camera.rotateX(xDeg); // it will lead to rotate Y and Z axis
        camera.rotateY(yDeg+45f); // it will just lead to rotate Z axis, NOT X axis. BUT rotateZ(deg) will lead to nothing
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixFront2);
        camera.restore(); // restore to the original state after uses for next use

        // translate coordinate origin the camera's transformation depends on to center of the bitmap
        matrixFront2.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixFront2.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrixBack(float xDeg, float yDeg) {
        matrixBack.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg+180);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixBack);
        camera.restore();

        matrixBack.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixBack.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }
    private void setMatrixBack2(float xDeg, float yDeg) {
        matrixBack2.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg+225f);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixBack2);
        camera.restore();

        matrixBack2.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixBack2.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }


    private void setMatrixLeft2(float xDeg, float yDeg) {
        matrixLeft2.reset();
        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg - 45f);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixLeft2);
        camera.restore();
        matrixLeft2.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixLeft2.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrixRight(float xDeg, float yDeg) {
        matrixRight.reset();
        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg + 90f);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixRight);
        camera.restore();
//        matrixRight.setRotate(0,0,180f);
//        matrixRight.preRotate(0, 0, 0);
//        matrixRight.postRotate(180f, 0, 0);
//        matrixRight.setRotate(180f);
        matrixRight.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixRight.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }
    private void setMatrixRight2(float xDeg, float yDeg) {
        matrixRight2.reset();
        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg +135f);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixRight2);
        camera.restore();

        matrixRight2.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixRight2.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrixTop(float xDeg, float yDeg) {
        matrixTop.reset();

        camera.save();
        camera.rotateX(xDeg - 90f);
        camera.rotateY(yDeg);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrixTop);
        camera.restore();

        matrixTop.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixTop.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrixBottom(float xDeg, float yDeg) {
        matrixBottom.reset();

        camera.save();
        camera.rotateX(xDeg - 90f);
        camera.rotateY(yDeg);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, cameraZtranslate);
        camera.getMatrix(matrixBottom);
        camera.restore();

        matrixBottom.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrixBottom.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }


    private void drawCanvas(Canvas canvas, float xDeg, float yDeg) {

        if (Math.cos(Math.toRadians(xDeg)) <= 0 || Math.cos(Math.toRadians(yDeg)) <= 0) {//钝角
            canvas.drawBitmap(bitmap, matrixFront, paint);

            if (Math.cos(Math.toRadians(xDeg)) <= 0 || Math.cos(Math.toRadians(yDeg - 90f)) <= 0) {
                canvas.drawBitmap(bitmap4, matrixLeft2, paint);
                canvas.drawBitmap(bitmap2, matrixFront2, paint);
                canvas.drawBitmap(bitmap3, matrixLeft, paint);
                canvas.drawBitmap(bitmap8, matrixBack2, paint);
                canvas.drawBitmap(bitmap7, matrixBack, paint);
                canvas.drawBitmap(bitmap5, matrixRight, paint);
                canvas.drawBitmap(bitmap6, matrixRight2, paint);
                Log.i("drawCanvas", "11");//左上
            } else {
                canvas.drawBitmap(bitmap5, matrixRight, paint);
                canvas.drawBitmap(bitmap2, matrixFront2, paint);
                canvas.drawBitmap(bitmap6, matrixRight2, paint);
                canvas.drawBitmap(bitmap4, matrixLeft2, paint);
                canvas.drawBitmap(bitmap7, matrixBack, paint);
                canvas.drawBitmap(bitmap3, matrixLeft, paint);
                canvas.drawBitmap(bitmap8, matrixBack2, paint);
                Log.i("drawCanvas", "22");//右上
            }

        }  else {
            canvas.drawBitmap(bitmap7, matrixBack, paint);

            if (Math.cos(Math.toRadians(xDeg)) <= 0 || Math.cos(Math.toRadians(yDeg - 90f)) <= 0) {
                canvas.drawBitmap(bitmap8, matrixBack2, paint);
                canvas.drawBitmap(bitmap3, matrixLeft, paint);
                canvas.drawBitmap(bitmap4, matrixLeft2, paint);
                canvas.drawBitmap(bitmap6, matrixRight2, paint);
                canvas.drawBitmap(bitmap5, matrixRight, paint);
                canvas.drawBitmap(bitmap, matrixFront, paint);
                canvas.drawBitmap(bitmap2, matrixFront2, paint);
                Log.i("drawCanvas", "33");//左下
            } else {
                canvas.drawBitmap(bitmap6, matrixRight2, paint);
                canvas.drawBitmap(bitmap8, matrixBack2, paint);
                canvas.drawBitmap(bitmap5, matrixRight, paint);
                canvas.drawBitmap(bitmap2, matrixFront2, paint);
                canvas.drawBitmap(bitmap, matrixFront, paint);
                canvas.drawBitmap(bitmap3, matrixLeft, paint);
                canvas.drawBitmap(bitmap4, matrixLeft2, paint);
                Log.i("drawCanvas", "44");//右下
            }


        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animHandler != null) {
            animHandler.removeCallbacksAndMessages(null);
        }
    }


    public interface StateValueListener {
        void stateValue(float distanceX, float distanceY, float rotateDegree, float cameraZtranslate);
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
                    Log.i("nima", "oldX="+oldX+"---oldY="+oldY+"---oldTimestamp="+oldTimestamp+"--xVelocity="+xVelocity+"---yVelocity="+yVelocity);
                }
                if (xVelocity == 0 && yVelocity == 0) {

                } else {
                    return false;
                }
                break;
        }
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

    private TwoFingersGestureListener twoFingersGestureListener;

    public void setTwoFingersGestureListener(TwoFingersGestureListener l) {
        this.twoFingersGestureListener = l;
    }
}
