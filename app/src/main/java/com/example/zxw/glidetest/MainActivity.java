package com.example.zxw.glidetest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private TextView xValue;
    private TextView yValue;
    private TextView rotateValue;
    private TextView cameraZvalue;

    private ThreeDViewController threeDViewController;
    private CircleIndicatorView mIndicatorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xValue = (TextView) findViewById(R.id.tv_x_value);
        yValue = (TextView) findViewById(R.id.tv_y_value);
        rotateValue = (TextView) findViewById(R.id.tv_rotate_value);
        cameraZvalue = (TextView) findViewById(R.id.tv_cameraZ_value);
        final ThreeDView7 threeDView = (ThreeDView7)findViewById(R.id.three_d_view);
        threeDView.setOnClick(new ThreeDView7.OnMyClick() {
            @Override
            public void onMyClick(View view, int position) {
                Toast.makeText(MainActivity.this, "position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
        threeDView.setStateValueListener(new ThreeDView7.StateValueListener() {
            @Override
            public void stateValue(float distanceX, float distanceY, float rotateDeg, float cameraZtranslate) {
                String xvalue = "" + distanceX, yvalue = "" + distanceY, rotateDegStr = "" + rotateDeg,
                        cameraZtranslateStr = "" + cameraZtranslate;
                xValue.setText(xvalue);
                yValue.setText(yvalue);
                rotateValue.setText(rotateDegStr);
                //test
                cameraZvalue.setText("第"+ threeDView.getItemPosition()+"号");
                int num = threeDView.getItemPosition();
                if (num == 11) {
                    num=1;
                }
                mIndicatorView.setSelectPosition(Math.abs(num)-1);
            }

            @Override
            public void startA() {
                startActivity(new Intent(MainActivity.this,OpenActivity.class));
            }
        });

        threeDViewController = new ThreeDViewController(threeDView);
        mIndicatorView = (CircleIndicatorView) findViewById(R.id.indicator_view);


//        indicatorView1.setUpWithViewPager(mViewPager);

//        indicatorView2.setUpWithViewPager(mViewPager);
        // 在代码中设置相关属性
        // 设置半径
//        indicatorView.setRadius(DisplayUtils.dpToPx(15));
//        // 设置Border
//        indicatorView.setBorderWidth(DisplayUtils.dpToPx(2));
//
//        // 设置文字颜色
//        indicatorView.setTextColor(Color.WHITE);
//        // 设置选中颜色
//        indicatorView.setSelectColor(Color.parseColor("#FEBB50"));
//        //
//        indicatorView.setDotNormalColor(Color.parseColor("#E38A7C"));
//        // 设置指示器间距
//        indicatorView.setSpace(DisplayUtils.dpToPx(10));
//        // 设置模式
//        indicatorView.setFillMode(CircleIndicatorView.FillMode.LETTER);
        mIndicatorView.setUpWithThreeDView(threeDView);
        mIndicatorView.setEnableClickSwitch(true);
        mIndicatorView.setCount(7);
        mIndicatorView.setOnIndicatorClickListener(new CircleIndicatorView.OnIndicatorClickListener() {
            @Override
            public void onSelected(int position) {
                Log.i("44444", "132412");
                threeDView.setCurrentItem(position+1);
            }
        });
    }

}