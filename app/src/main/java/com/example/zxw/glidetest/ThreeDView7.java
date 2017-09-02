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

import java.util.ArrayList;
import java.util.List;

public class ThreeDView7 extends View {
    private static final String TAG = ThreeDView7.class.getSimpleName();
    private int THREE_D_VIEW_WIDTH;
    private int THREE_D_VIEW_HEIGHT;
    private static final int BIT_MAP_WIDTH = 300;
    private static final int BIT_MAP_HEIGHT = 300;
    private float cameraZtranslate = 320; // 3D rotate radius
    private GestureDetector mGestureDetector;
    private Camera camera = new Camera(); //default location: (0f, 0f, -8.0f), in pixels: -8.0f * 72 = -576f
    private PaintFlagsDrawFilter paintFilter;
    private int[] imgs = {R.mipmap.z1, R.mipmap.z2, R.mipmap.z3, R.mipmap.z4, R.mipmap.z5, R.mipmap.z6, R.mipmap.z7};
    private Paint paint;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<Matrix> matrices = new ArrayList<>();
    private float distanceX = 0f;
    private float distanceY = 60f;//倾斜角度
    private float rotateDeg = 0f;
    private final int VIEW_COUNT = 7;
    private float angItem = 51.4f;
    private float angItem2 = 51.6f;
    private float restFloat = 160;//计算每个item的float,每个item距离distanceX=160
    private float distanceToDegree;
    private float X_VELOCITY_DECREASE = 0.003f;//数值越小惯性越小
    private float X_MOVE_DECREASE = 0.7f;//手指移动的阻力
    private float Y_MOVE_DECREASE = 0.7f;//手指移动的阻力
    private boolean isInfinity = false;
    private float distanceVelocityDecrease = 10f; //decrease 1 pixels/second when a message is handled in the loop
    //loop frequency is 60hz or 120hz when handleMessage(msg) includes UI update code
    private float distanceXDecrease = 10f;
    private float distanceYDecrease = 0f;
    private float xVelocity = 0f;
    private float yVelocity = 0f;
    private float jumpItemX = 0f;//记录要跳转的distanceX
    private final float jumpItemY = 60f;
    private float jumpItemMiddlX = 0f;//记录distanceX与jumpItemX的中间值

    private Handler scrollHandler;//滑动的动画
    private Handler clickHandler;//点击item,跳转页面
    private Handler jumpHandler;//跳转到某item
    private Handler jumpHandlerII;//跳转到某item,升级版....带有减速效果
    private Handler reYHandler;//复位绕X轴的旋转
    private Handler scrollStopHandler;//滑动后复位的动画
    private boolean isScroll = false;//用于限制滑动时点击item
    private boolean isTouch = true;//用于点击中间item后，限制触摸view滑动

    public ThreeDView7(Context context) {
        this(context, null);
    }

    public ThreeDView7(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreeDView7(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, new SimpleGestureListener());
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paintFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG);
//        camera.setLocation(0f, 0f, -cameraZtranslate);//设置camera的位置,这个有问题...形状都变了,应该用下面的函数
        camera.translate(0,0,250);//cameraZtranslate越大,view越远
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        for (int i = 0; i < VIEW_COUNT; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgs[i]);
            bitmap = Bitmap.createScaledBitmap(bitmap, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, true);
            bitmaps.add(bitmap);
            matrices.add(new Matrix());
        }
        reYHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("reYHandler", "jumpItemY=" + jumpItemY + "----distanceY=" + distanceY);
                isScroll = true;
                if (Math.abs(distanceY - jumpItemY )<= distanceYDecrease) {
                    distanceY = jumpItemY;
                }
                if (distanceY > jumpItemY) {
                    distanceY = distanceY - distanceYDecrease;
                } else if (distanceY < jumpItemY) {
                    distanceY = distanceY + distanceYDecrease;
                }
                if (distanceY == jumpItemY) {
                    reYHandler.removeMessages(0);
                    invalidate();
                    isScroll = false;
                    return false;
                }
                invalidate();
                stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                reYHandler.sendEmptyMessage(0);
                return false;

            }
        });
        jumpHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("jumpHandler", "jumpItemX=" + jumpItemX + "----xx=" + distanceX);
                isScroll = true;
                if (Math.abs(distanceX - jumpItemX) <= distanceXDecrease ) {
                    distanceX = jumpItemX;
                }
                if (distanceX > jumpItemX) {
                    distanceX = distanceX - distanceXDecrease;
                } else if (distanceX < jumpItemX) {
                    distanceX = distanceX + distanceXDecrease;
                }
                if (distanceX > restFloat * 7) {
                    distanceX -= restFloat * 7;
                } else if (distanceX < -restFloat * 7) {
                    distanceX += restFloat * 7;
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
        jumpHandlerII = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                isScroll = true;
                if (Math.abs(distanceX - jumpItemX) <= distanceXDecrease ) {
                    distanceX = jumpItemX;
                }

                if (distanceX > jumpItemX) {
                    if (distanceX > jumpItemMiddlX) {//跳转前部分
                        distanceX = distanceX - distanceXDecrease;
                    } else {//跳转后部分
                        if(distanceXDecrease>1.5f) {
                            distanceXDecrease*=0.90;
                        }
                        distanceX = distanceX - distanceXDecrease;
                    }
                } else if (distanceX < jumpItemX) {
                    if (distanceX < jumpItemMiddlX) {//跳转前部分
                        distanceX = distanceX + distanceXDecrease;
                    } else {//跳转后部分
                        if(distanceXDecrease>1.5f) {
                            distanceXDecrease*=0.90;
                        }
                        distanceX = distanceX + distanceXDecrease;
                    }
                }
                Log.i(TAG,"jumpHandlerII   jumpItemX=" + jumpItemX + "  ----distanceX=" + distanceX+"  ---jumpItemMiddlX="+jumpItemMiddlX+"   --distanceXDecrease="+distanceXDecrease);
                if (distanceX > restFloat * 7) {
                    distanceX -= restFloat * 7;
                } else if (distanceX < -restFloat * 7) {
                    distanceX += restFloat * 7;
                }
                if (distanceX == jumpItemX) {
                    jumpHandlerII.removeMessages(0);
                    invalidate();
                    stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                    isScroll = false;
                    return false;
                }
                invalidate();
                jumpHandlerII.sendEmptyMessage(0);
                return false;

            }
        });
        scrollHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("scrollHandler", "xVelocity=" + xVelocity + "--yVelocity=" + yVelocity + "---distanceX=" + distanceX);
                isScroll = true;
                distanceX += (xVelocity * X_VELOCITY_DECREASE);
//                distanceY += (yVelocity * X_VELOCITY_DECREASE);
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
                if (Math.abs(xVelocity) < 350f) {//这么慢，不让他惯性滑动了
                    reLayout();
                    return true;
                }
                if (Math.abs(xVelocity) < 600f) {
                    int position = getItemPosition();//当前大概位置
                    float itemFloat = getItemFloat();//当前位置
                    if (xVelocity > 0) { //逆时针
                        if (Math.abs(position) == 11 || Math.abs(position) == 1) {
                            if (distanceX - itemFloat > 0) {
                                //此时位置在getItemPosition()的item的左边，所以跳转到getItemPosition()的位置就好了
                                setJumpItemX(7);
                            } else if (distanceX - itemFloat < 0) {
                                setJumpItemX(1);
                            } else {
                                //刚好在位置上，可能吗..
                                return true;
                            }
                        } else {
                            if (distanceX - itemFloat > 0) {
                                //此时位置在getItemPosition()的item的左边，所以跳转到getItemPosition()的位置就好了
                                setJumpItemX(Math.abs(position) - 1);
                            } else if (distanceX - itemFloat < 0) {
                                setJumpItemX(Math.abs(position));
                            } else {
                                //刚好在位置上，可能吗..
                                return true;
                            }
                        }
                    } else if (xVelocity < 0) {//顺时针
                        if (Math.abs(position) == 11) {
                            setJumpItemX(2);
                        } else if (Math.abs(position) == 7) {
                            setJumpItemX(1);
                        } else {
                            setJumpItemX(Math.abs(position) + 1);
                        }

                    }
                    scrollStopHandler.sendEmptyMessage(0);
                    return true;
                }
                if (xVelocity == 0f) { // anim will stop
                    /**
                     * 滑动后，减速到一定程度，偏匀速转动到指定位置
                     */
                    reLayout();
                    return true;
                }
                if (ThreeDView7.this.isInfinity) {
                    scrollHandler.sendEmptyMessage(0);
                } else {
                    // decrease the velocities.
                    xVelocity = Math.abs(xVelocity) <= distanceVelocityDecrease ? 0f :
                            (xVelocity > 0 ? xVelocity - distanceVelocityDecrease : xVelocity + distanceVelocityDecrease);
                    yVelocity = Math.abs(yVelocity) <= distanceVelocityDecrease ? 0f :
                            (yVelocity > 0 ? yVelocity - distanceVelocityDecrease : yVelocity + distanceVelocityDecrease);
                    scrollHandler.sendEmptyMessage(0);
                }
                return true;
            }
        });
        scrollStopHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("scrollStopHandler", "xVelocity=" + xVelocity + "--yVelocity=" + yVelocity + "--distanceX=" + distanceX + "---jumpX=" + jumpItemX);
                isScroll = true;
                if (Math.abs(distanceX - jumpItemX) <= Math.abs(xVelocity * X_VELOCITY_DECREASE)) {
                    distanceX = jumpItemX;
                    ThreeDView7.this.invalidate();
                    return true;
                }
                if (Math.abs(distanceX) <= Math.abs(xVelocity * X_VELOCITY_DECREASE)  && (jumpItemX == restFloat * 7 || jumpItemX == -restFloat * 7)) {
                    //特殊情况...因为当distanceX>restFloat*7||distanceX<-restFloat*7时会distanceX -= restFloat * 7;导致会至少多转一圈
                    distanceX = jumpItemX;
                    ThreeDView7.this.invalidate();
                    return true;
                }
                distanceX += (xVelocity * X_VELOCITY_DECREASE);//数值越小惯性越小
//                distanceY += (yVelocity X_VELOCITY_DECREASE);
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

                if (distanceX == jumpItemX) {
                    isScroll = false;
                    return true;
                }
                if (ThreeDView7.this.isInfinity) {
                    scrollStopHandler.sendEmptyMessage(0);
                } else {
                    // decrease the velocities.
//                    xVelocity = Math.abs(xVelocity) <= 2 ? 0f :
//                            (xVelocity > 0 ? xVelocity - 2 : xVelocity + 2);
                    scrollStopHandler.sendEmptyMessage(0);//不用减速了
                }
                return true;
            }
        });
        clickHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                isTouch = false;
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
                    ThreeDView7.this.invalidate();
                    clickHandler.sendEmptyMessage(1);

                } else if (msg.what == 0) {//放大变形，跳转
                    if (count == 3) {
                        if (ThreeDView7.this.stateValueListener != null) {
                            ThreeDView7.this.stateValueListener.startA();
                        }
                        count = 0;
                        isScroll = false;
                        isTouch = true;
                        clickHandler.removeCallbacksAndMessages(null);
                    } else {
                        camera.translate(0, 0, -90);
                        invalidate();
                        count++;
                        clickHandler.sendEmptyMessage(0);
                    }
                } else if (msg.what == 2) {//缩小变形
                    if (count == 3) {
                        reLayoutY();
                        count = 0;
                        isScroll = false;
                        isTouch = true;
                        clickHandler.removeCallbacksAndMessages(null);
                    } else {
                        camera.translate(0, 0, 90);
                        invalidate();
                        count++;
                        clickHandler.sendEmptyMessage(2);
                    }

                }
                return true;
            }
        });
    }

    private int count = 0;//用来计数动画进度clickHandler

    public void resetStart() {
        clickHandler.sendEmptyMessageDelayed(2, 500);
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
        Log.i(TAG, "updateXY: movedX= "+movedX+"  ---movedY= "+movedY);
        this.distanceX += movedX*X_MOVE_DECREASE;
        this.distanceY += movedY*Y_MOVE_DECREASE;
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

    //停止滑动
    public void stopScroll() {
        scrollHandler.removeCallbacksAndMessages(null);
        scrollStopHandler.removeCallbacksAndMessages(null);
        reYHandler.removeCallbacksAndMessages(null);
        jumpHandler.removeCallbacksAndMessages(null);
    }

    //滑动动画
    public void startAnim(float xVelocity, float yVelocity) {
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        reLayoutY();
        jumpHandler.removeCallbacksAndMessages(null);
        scrollHandler.sendEmptyMessage(0);
    }

    //Y复位
    public void reLayoutY() {
        if (yVelocity == 0f && distanceY != jumpItemY) {
            distanceYDecrease = Math.abs(distanceY - jumpItemY) / 10f;
            reYHandler.sendEmptyMessage(0);
        }
    }

    //放手时复位
    public void reLayout() {
        Log.i("reLayout", "reLayout");
        int position = getItemPosition();
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
        } else if (position == 11) {
            jumpItemX = restFloat * 7;
        } else if (position == -1) {
            jumpItemX = -restFloat * 7;
        } else if (position == -2) {
            jumpItemX = -restFloat;
        } else if (position == -3) {
            jumpItemX = -restFloat * 2;
        } else if (position == -4) {
            jumpItemX = -restFloat * 3;
        } else if (position == -5) {
            jumpItemX = -restFloat * 4;
        } else if (position == -6) {
            jumpItemX = -restFloat * 5;
        } else if (position == -7) {
            jumpItemX = -restFloat * 6 + restFloat * (angItem - angItem2) / angItem;
        }
        distanceXDecrease = Math.abs(jumpItemX - distanceX) / 10f;
        jumpHandler.removeMessages(0);
        jumpHandler.sendEmptyMessage(0);

    }

    public void jumpNext() {
        int position = getItemPosition();
        if (Math.abs(position) == 11) {
            setCurrentItem(2);
        } else if (Math.abs(position) == 7) {
            setCurrentItem(1);
        } else {
            setCurrentItem(Math.abs(position) + 1);
        }
    }

    public void jumpPre() {
        int position = getItemPosition();
        if (Math.abs(position) == 11 || Math.abs(position) == 1) {
            setCurrentItem(7);
        } else {
            setCurrentItem(Math.abs(position) - 1);
        }
    }

    //下方导航选择跳转  从1开始
    public void setCurrentItem(int position) {
        int itemPosition = getItemPosition();
        if (position == 1) {

            if (itemPosition == 1) {
                jumpItemX = 0;
                return;
            } else if (itemPosition == -1) {
                jumpItemX = -restFloat * 7;
                return;
            } else if (itemPosition == 11) {
                jumpItemX = restFloat * 7;
                return;
            } else if (itemPosition == 2 || itemPosition == 3 || itemPosition == 4) {
                jumpItemX = restFloat * 7;
            } else if (itemPosition == -5 || itemPosition == -6 || itemPosition == -7) {
                jumpItemX = -restFloat * 7;
            } else {
                jumpItemX = 0;
            }
        } else if (position == 2) {
            if (itemPosition == 2) {
                jumpItemX = restFloat * 6;
                return;
            } else if (itemPosition == -2) {
                jumpItemX = -restFloat;
                return;
            } else if (itemPosition == 1 || itemPosition == 6 || itemPosition == 7
                    || itemPosition == -3 || itemPosition == -4 || itemPosition == -5) {
                jumpItemX = -restFloat;
            } else if (itemPosition == -6) {
                distanceX = restFloat * 2;
                jumpItemX = -restFloat;
            } else if (itemPosition == -7) {
                distanceX = restFloat;
                jumpItemX = -restFloat;
            } else if (itemPosition == -1) {
                distanceX = 0;
                jumpItemX = -restFloat;
            } else {
                jumpItemX = restFloat * 6;
            }
        } else if (position == 3) {
            if (itemPosition == 3) {
                jumpItemX = restFloat * 5;
                return;
            } else if (itemPosition == -3) {
                jumpItemX = -restFloat * 2;
                return;
            } else if (itemPosition == 1 || itemPosition == -2 || itemPosition == 7
                    || itemPosition == -6 || itemPosition == -4 || itemPosition == -5) {
                jumpItemX = -restFloat * 2;
            } else if (itemPosition == -7) {
                distanceX = restFloat;
                jumpItemX = -restFloat * 2;
            } else if (itemPosition == -1) {
                distanceX = 0;
                jumpItemX = -restFloat * 2;
            } else {
                jumpItemX = restFloat * 5;
            }
        } else if (position == 4) {
            if (itemPosition == 4) {
                jumpItemX = restFloat * 4;
                return;
            } else if (itemPosition == -4) {
                jumpItemX = -restFloat * 3;
                return;
            } else if (itemPosition == -1) {
                distanceX = 0;
                jumpItemX = -restFloat * 3;
            } else if (itemPosition < 0 || itemPosition == 1) {
                jumpItemX = -restFloat * 3;
            } else {
                jumpItemX = restFloat * 4;
            }
        } else if (position == 5) {
            if (itemPosition == 5) {
                jumpItemX = restFloat * 3;
                return;
            } else if (itemPosition == -5) {
                jumpItemX = -restFloat * 4;
                return;
            } else if (itemPosition < 0) {
                jumpItemX = -restFloat * 4;
            } else if (itemPosition == 11) {
                distanceX = 0;
                jumpItemX = restFloat * 3;
            } else {
                jumpItemX = restFloat * 3;
            }
        } else if (position == 6) {
            if (itemPosition == 6) {
                jumpItemX = restFloat * 2;
                return;
            } else if (itemPosition == -6) {
                jumpItemX = -restFloat * 5;
                return;
            } else if (itemPosition == -2) {
                jumpItemX = restFloat * 2;
            } else if (itemPosition < 0) {
                jumpItemX = -restFloat * 5;
            } else if (itemPosition == 2) {
                distanceX = -restFloat;
                jumpItemX = restFloat * 2;
            } else if (itemPosition == 11) {
                distanceX = 0;
                jumpItemX = restFloat * 2;
            } else {
                jumpItemX = restFloat * 2;
            }
        } else if (position == 7) {
            if (itemPosition == 7) {
                jumpItemX = restFloat;
                return;
            } else if (itemPosition == -7) {
                jumpItemX = -restFloat * 6;
                return;
            } else if (itemPosition == -4 || itemPosition == -5 || itemPosition == -6) {
                jumpItemX = -restFloat * 6;
            } else if (itemPosition == -2) {
                distanceX = -restFloat;
                jumpItemX = restFloat;
            } else if (itemPosition == -3) {
                distanceX = -restFloat * 2;
                jumpItemX = restFloat;
            } else if (itemPosition == 11 || itemPosition == -1||itemPosition == 1) {
                distanceX = 0;
                jumpItemX = restFloat;
            } else {
                jumpItemX = restFloat;
            }
        }
        distanceXDecrease = Math.abs(jumpItemX - distanceX) / 20f;
        if (jumpItemX != distanceX) {
            jumpItemMiddlX = (jumpItemX+distanceX)/2;
        }
        jumpHandler.removeMessages(0);
        jumpHandlerII.removeMessages(0);
        scrollStopHandler.removeCallbacksAndMessages(null);
        scrollHandler.removeCallbacksAndMessages(null);
        jumpHandlerII.sendEmptyMessage(0);
    }

    //根据position设置jumpItemX--需要跳转的item的distanceX
    public boolean setJumpItemX(int position) {
        int itemPosition = getItemPosition();
        if (position == 1) {
            if (itemPosition == 1) {
                jumpItemX = 0;
                return false;
            } else if (itemPosition == -1) {
                jumpItemX = -restFloat * 7;
                return false;
            } else if (itemPosition == 11) {
                jumpItemX = restFloat * 7;
                return false;
            } else if (itemPosition == 2 || itemPosition == 3 || itemPosition == 4) {
                jumpItemX = restFloat * 7;
            } else if (itemPosition == -5 || itemPosition == -6 || itemPosition == -7) {
                jumpItemX = -restFloat * 7;
            } else {
                jumpItemX = 0;
            }
        } else if (position == 2) {
            if (itemPosition == 2) {
                jumpItemX = restFloat * 6;
                return false;
            } else if (itemPosition == -2) {
                jumpItemX = -restFloat;
                return false;
            } else if (itemPosition == 1 || itemPosition == 6 || itemPosition == 7
                    || itemPosition == -3 || itemPosition == -4 || itemPosition == -5) {
                jumpItemX = -restFloat;
            } else if (itemPosition == -6) {
//                distanceX = restFloat * 2;
                jumpItemX = -restFloat;
            } else if (itemPosition == -7) {
//                distanceX = restFloat;
                jumpItemX = -restFloat;
            } else if (itemPosition == -1) {
//                distanceX = 0;
                jumpItemX = -restFloat;
            } else {
                jumpItemX = restFloat * 6;
            }
        } else if (position == 3) {
            if (itemPosition == 3) {
                jumpItemX = restFloat * 5;
                return false;
            } else if (itemPosition == -3) {
                jumpItemX = -restFloat * 2;
                return false;
            } else if (itemPosition == 1 || itemPosition == -2 || itemPosition == 7
                    || itemPosition == -6 || itemPosition == -4 || itemPosition == -5) {
                jumpItemX = -restFloat * 2;
            } else if (itemPosition == -7) {
//                distanceX = restFloat;
                jumpItemX = -restFloat * 2;
            } else if (itemPosition == -1) {
//                distanceX = 0;
                jumpItemX = -restFloat * 2;
            } else {
                jumpItemX = restFloat * 5;
            }
        } else if (position == 4) {
            if (itemPosition == 4) {
                jumpItemX = restFloat * 4;
                return false;
            } else if (itemPosition == -4) {
                jumpItemX = -restFloat * 3;
                return false;
            } else if (itemPosition == -1) {
//                distanceX = 0;
                jumpItemX = -restFloat * 3;
            } else if (itemPosition < 0 || itemPosition == 1) {
                jumpItemX = -restFloat * 3;
            } else {
                jumpItemX = restFloat * 4;
            }
        } else if (position == 5) {
            if (itemPosition == 5) {
                jumpItemX = restFloat * 3;
                return false;
            } else if (itemPosition == -5) {
                jumpItemX = -restFloat * 4;
                return false;
            } else if (itemPosition < 0) {
                jumpItemX = -restFloat * 4;
            } else if (itemPosition == 11) {
//                distanceX = 0;
                jumpItemX = restFloat * 3;
            } else {
                jumpItemX = restFloat * 3;
            }
        } else if (position == 6) {
            if (itemPosition == 6) {
                jumpItemX = restFloat * 2;
                return false;
            } else if (itemPosition == -6) {
                jumpItemX = -restFloat * 5;
                return false;
            } else if (itemPosition == -2) {
                jumpItemX = restFloat * 2;
            } else if (itemPosition < 0) {
                jumpItemX = -restFloat * 5;
            } else if (itemPosition == 2) {
//                distanceX = -restFloat;
                jumpItemX = restFloat * 2;
            } else if (itemPosition == 11) {
//                distanceX = 0;
                jumpItemX = restFloat * 2;
            } else {
                jumpItemX = restFloat * 2;
            }
        } else if (position == 7) {
            if (itemPosition == 7) {
                jumpItemX = restFloat;
                return false;
            } else if (itemPosition == -7) {
                jumpItemX = -restFloat * 6;
                return false;
            } else if (itemPosition == -4 || itemPosition == -5 || itemPosition == -6) {
                jumpItemX = -restFloat * 6;
            } else if (itemPosition == -2) {
//                distanceX = -restFloat;
                jumpItemX = restFloat;
            } else if (itemPosition == -3) {
//                distanceX = -restFloat * 2;
                jumpItemX = restFloat;
            } else if (itemPosition == 11 || itemPosition == -1) {
//                distanceX = 0;
                jumpItemX = restFloat;
            } else {
                jumpItemX = restFloat;
            }
        }
        return true;
    }
    //得到item的标准位置
    public float getItemFloat() {
        float f = 0f;
        switch (getItemPosition()) {
            case 1:
                f = 0f;
                break;
            case 11:
                f = restFloat * 7;
                break;
            case -1:
                f = -restFloat * 7;
                break;
            case 2:
                f = restFloat * 6;
                break;
            case -2:
                f = -restFloat;
                break;
            case 3:
                f = restFloat * 5;
                break;
            case -3:
                f = -restFloat * 2;
                break;
            case 4:
                f = restFloat * 4;
                break;
            case -4:
                f = -restFloat * 3;
                break;
            case 5:
                f = restFloat * 3;
                break;
            case -5:
                f = -restFloat * 4;
                break;
            case 6:
                f = restFloat * 2;
                break;
            case -6:
                f = -restFloat * 5;
                break;
            case 7:
                f = restFloat;
                break;
            case -7:
                f = -restFloat * 6;
                break;
        }
        return f;
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
        distanceToDegree = 90f / 280;//NOT changed when cameraZtranslate changed in the future
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // convert distances in pixels into degrees
        float xDeg = -distanceY * distanceToDegree;
        float yDeg = distanceX * distanceToDegree;
        Log.i(TAG,"onDraw  xdeg=" + xDeg + "---ydeg=" + yDeg);
        canvas.setDrawFilter(paintFilter);
        setMatrix(xDeg, yDeg);
        // translate canvas to locate the bitmap in center of the ThreeDViwe
        canvas.translate((THREE_D_VIEW_WIDTH - BIT_MAP_WIDTH) / 2f, (THREE_D_VIEW_HEIGHT - BIT_MAP_HEIGHT) / 2f);
        canvas.clipRect(-150, -90, 450, 600);
        drawCanvas(canvas);
    }
    private void setMatrix(float xDeg, float yDeg) {
        for (int i = 0; i < VIEW_COUNT; i++) {
            Matrix matrix = matrices.get(i);
            matrix.reset();
            camera.save();
            camera.rotateX(xDeg); // it will lead to rotate Y and Z axis
            float rotate = 0;
            if (i == 6) {
                rotate = yDeg + angItem * 5 + angItem2;
            } else {
                rotate = yDeg + angItem * i;
            }
//            Log.i("setMatrix", "i=" + i + "  --rotate = " + rotate);
            camera.rotateY(rotate);// it will just lead to rotate Z axis, NOT X axis. BUT rotateZ(deg) will lead to nothing
            camera.rotateZ(-rotateDeg);
            camera.translate(0f, 0f, -cameraZtranslate);
            camera.getMatrix(matrix);
            camera.restore(); // restore to the original state after uses for next use
            // translate coordinate origin the camera's transformation depends on to center of the bitmap
            matrix.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
            matrix.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
        }
    }

    //绘制顺序
    private void drawBitmap(Canvas canvas, int[] num) {
        for (int i :num) {
            canvas.drawBitmap(bitmaps.get(i), matrices.get(i), paint);
        }
    }
    int[] i1 = {3, 4, 5, 6, 2, 1, 0};
    int[] i2 = {4, 5, 3, 6, 2, 0, 1};
    int[] i3 = {5, 4, 6, 0, 3, 1, 2};
    int[] i4 = {6, 5, 0, 1, 4, 2, 3};
    int[] i5 = {0, 6, 1, 2, 5, 3, 4};
    int[] i6 = {1, 2, 3, 0, 4, 6, 5};
    int[] i7 = {2, 1, 3, 4, 5, 0, 6};
    private void drawCanvas(Canvas canvas) {
        int position = getItemPosition();
        if (position == 1 || position == -1 || position == 11) {
            drawBitmap(canvas,i1);
        } else if (position == 2 || position == -2) {
            drawBitmap(canvas, i2);
        } else if (position == 3 || position == -3) {
            drawBitmap(canvas, i3);
        } else if (position == 4 || position == -4) {
            drawBitmap(canvas, i4);
        } else if (position == 5 || position == -5) {
            drawBitmap(canvas, i5);
        } else if (position == 6 || position == -6) {
            drawBitmap(canvas, i6);
        } else if (position == 7 || position == -7) {
            drawBitmap(canvas, i7);
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
        if (jumpHandlerII != null) {
            jumpHandlerII.removeCallbacksAndMessages(null);
        }
        if (reYHandler != null) {
            reYHandler.removeCallbacksAndMessages(null);
        }
        if (clickHandler != null) {
            clickHandler.removeCallbacksAndMessages(null);
        }
        if (scrollStopHandler != null) {
            scrollStopHandler.removeCallbacksAndMessages(null);
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
//        if (event.getPointerCount() > 2) {//三指时会取消各种handler
//            moreThan2Fingers = true;
//            if (twoFingersGestureListener != null) {
//                twoFingersGestureListener.onCancel();
//            }
//        }
        if (!isTouch) {
            return true;
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
                updateXY(currDeltaMovedX, currDeltaMovedY);
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
            if (moreThan2Fingers) {
                return false;
            }
            stopScroll();
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
            //在屏幕上拖动事件。无论是用手拖动view，或者是以抛的动作滚动，都会多次触发,这个方法---好像没卵用
//            Log.i("Gesture-onScroll", "e1.getAction()=" + e1.getAction() + "---e2.getac()=" + e2.getAction() + "--distanceX=" + distanceX + "--distanceY=" + distanceY);
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

    /**
     *  根据坐标判断点击哪个item,多边形...判断几个矩形算了
     */
    public void checkClickItem(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        distanceXDecrease = 10f;
        int position = getItemPosition();
        jumpHandler.removeMessages(0);
        jumpHandlerII.removeMessages(0);
        scrollStopHandler.removeCallbacksAndMessages(null);
        scrollHandler.removeCallbacksAndMessages(null);
        //上1    ok
        if (x >= 45 && x < 105) {
            if (y > 120 && y < 240) {
                if (position == 11) {
                    distanceX = 0;
                    jumpItemX = restFloat * 2;
                } else if (position == 2) {
                    distanceX = -restFloat;
                    jumpItemX = distanceX + restFloat * 2;
                } else if (position > 0) {
                    jumpItemX = distanceX + restFloat * 2;
                } else {
                    jumpItemX = distanceX + restFloat * 2;
                }
                distanceXDecrease = Math.abs(jumpItemX - distanceX) / 20f;
                if (jumpItemX != distanceX) {
                    jumpItemMiddlX = (jumpItemX+distanceX)/2;
                }
                jumpHandlerII.sendEmptyMessage(0);
            }
        }
        //上2    ok
        if (x >= 107 && x < 370) {
            if (y > 60 && y < 290) {
                if (position == 3) {
                    distanceX = -restFloat * 2;
                    jumpItemX = distanceX + restFloat * 3;
                } else if (position == 2) {
                    distanceX = -restFloat;
                    jumpItemX = distanceX + restFloat * 3;
                } else if (position == 11) {
                    distanceX = 0;
                    jumpItemX = restFloat * 3;
                } else {
                    jumpItemX = distanceX + restFloat * 3;
                }
                distanceXDecrease = Math.abs(jumpItemX - distanceX) / 20f;
                if (jumpItemX != distanceX) {
                    jumpItemMiddlX = (jumpItemX+distanceX)/2;
                }
                jumpHandlerII.sendEmptyMessage(0);
            }
        }
        //上3    ok
        if (x >= 390 && x < 650) {
            if (y > 60 && y < 230) {
                if (position == 1) {
                    jumpItemX = distanceX - restFloat * 3;
                } else if (position > 0) {
                    jumpItemX = distanceX - restFloat * 3;
                } else if (position == -6) {
                    distanceX = restFloat * 2;
                    jumpItemX = distanceX - restFloat * 3;
                } else if (position == -7) {
                    distanceX = restFloat;
                    jumpItemX = distanceX - restFloat * 3;
                } else if (position == -1) {//-1 -3
                    distanceX = 0;
                    jumpItemX = distanceX - restFloat * 3;
                } else {
                    jumpItemX = distanceX - restFloat * 3;
                }
                distanceXDecrease = Math.abs(jumpItemX - distanceX) / 20f;
                if (jumpItemX != distanceX) {
                    jumpItemMiddlX = (jumpItemX+distanceX)/2;
                }
                jumpHandlerII.sendEmptyMessage(0);
            }
        }

        //上4 ok
        if (x >= 650 && x < 720) {
            if (y > 120 && y < 240) {
                if (position > 0) {
                    jumpItemX = distanceX - restFloat * 2;
                } else if (position == -7) {
                    distanceX = restFloat;
                    jumpItemX = distanceX - restFloat * 2;
                } else if (position == -1) {//-1 -3
                    distanceX = 0;
                    jumpItemX = distanceX - restFloat * 2;
                } else {
                    jumpItemX = distanceX - restFloat * 2;
                }
                distanceXDecrease = Math.abs(jumpItemX - distanceX) / 20f;
                if (jumpItemX != distanceX) {
                    jumpItemMiddlX = (jumpItemX+distanceX)/2;
                }
                jumpHandlerII.sendEmptyMessage(0);
            }
        }
        //下左    ok
        if (x >= 45 && x < 230) {
            if (y > 210 && y < 660) {
                if (position == 1) {
                    jumpItemX = restFloat;
                } else if (position == 11) {
                    distanceX -= restFloat * 7;
                    jumpItemX = restFloat;
                } else {
                    jumpItemX = distanceX + restFloat;
                }
                distanceXDecrease = Math.abs(jumpItemX - distanceX) / 20f;
                if (jumpItemX != distanceX) {
                    jumpItemMiddlX = (jumpItemX+distanceX)/2;
                }
                jumpHandlerII.sendEmptyMessage(0);
            }
        }
        //中
        if (x >= 230 && x <= 533) {
            if (y >= 230 && y <= 650) {
                clickHandler.sendEmptyMessage(1);
            }
        }
        //下右    ok
        if (x >= 533 && x < 720) {
            if (y > 210 && y < 660) {
                if (position == 11) {
                    distanceX = 0;
                    jumpItemX = -restFloat;
                } else if (position == -1) {
                    distanceX = 0;
                    jumpItemX = -restFloat;
                } else {
                    jumpItemX = distanceX - restFloat;
                }
                distanceXDecrease = Math.abs(jumpItemX - distanceX) / 20f;
                if (jumpItemX != distanceX) {
                    jumpItemMiddlX = (jumpItemX+distanceX)/2;
                }
                jumpHandlerII.sendEmptyMessage(0);
            }
        }
    }

    private TwoFingersGestureListener twoFingersGestureListener;

    public void setTwoFingersGestureListener(TwoFingersGestureListener l) {
        this.twoFingersGestureListener = l;
    }
}
