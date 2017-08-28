package com.example.zxw.glidetest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class OpenActivity extends Activity {

    private TextView xValue;
    private TextView yValue;
    private TextView rotateValue;
    private TextView cameraZvalue;
    ThreeDView12 threeDView;
    private ThreeDViewController12 threeDViewController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        xValue = (TextView) findViewById(R.id.tv_x_value);
        yValue = (TextView) findViewById(R.id.tv_y_value);
        rotateValue = (TextView) findViewById(R.id.tv_rotate_value);
        cameraZvalue = (TextView) findViewById(R.id.tv_cameraZ_value);
        threeDView = (ThreeDView12)findViewById(R.id.three_d_view);
        threeDView.setOnClick(new ThreeDView12.OnMyClick() {
            @Override
            public void onMyClick(View view, int position) {
                Toast.makeText(OpenActivity.this, "position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
        threeDView.setStateValueListener(new ThreeDView12.StateValueListener() {
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
            }

            @Override
            public void startA() {
//                startActivity(new Intent(OpenActivity.this,OpenActivity.class));
//                Toast.makeText(MainActivity.this, "assdfas", Toast.LENGTH_SHORT).show();
            }
        });

        threeDViewController = new ThreeDViewController12(threeDView);
//        mIndicatorView = (CircleIndicatorView) findViewById(R.id.indicator_view);


//        mIndicatorView.setUpWithThreeDView(threeDView);
//        mIndicatorView.setEnableClickSwitch(true);
//        mIndicatorView.setCount(7);
//        mIndicatorView.setOnIndicatorClickListener(new CircleIndicatorView.OnIndicatorClickListener() {
//            @Override
//            public void onSelected(int position) {
//                Log.i("44444", "132412");
//                threeDView.setCurrentItem(position+1);
//            }
//        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        threeDView.resetStart();
    }
}