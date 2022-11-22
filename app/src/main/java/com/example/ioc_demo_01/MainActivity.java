package com.example.ioc_demo_01;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//标识出需要哪个布局文件
@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {


    public Button mBtn2;
    @ViewInject(R.id.btn1)
    Button btn1;

    @ViewInject(R.id.btn2)
    Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btn1.setText("按钮1");
        btn2.setText("按钮2");
    }

    @Deprecated
    @OnClick({R.id.btn1, R.id.btn2})
    public void click(View view) {
        Toast.makeText(this, "短按下了", Toast.LENGTH_SHORT).show();
    }

    @OnLongClick({R.id.btn2})
    public boolean longClick(View view) {
        Toast.makeText(this, "长按下了", Toast.LENGTH_SHORT).show();
        return false;
    }


}
