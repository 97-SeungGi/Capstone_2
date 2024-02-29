package com.allinwon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.allinwon.ui.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CenterActivity extends Activity {


    private final String packageName = "com.sec.android.app.clockpackage";
    private Intent intent1;
    private ConstraintLayout constraintLayout;


    Button btncal, btnwer, btnbus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center);

        constraintLayout = findViewById(R.id.activity_center);
        setBackgroundByTime();

        btncal = findViewById(R.id.calendar_btn);

        //캘린더
        btncal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivity(intent);
            }
        });


        btnwer = findViewById(R.id.Weather_btn);

        
        //날씨
        btnwer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        //버스
        btnbus = findViewById(R.id.Bus_btn);

        btnbus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BusActivity.class);
                startActivity(intent);
            }
        });



        //알람
        intent1 = this.getPackageManager().getLaunchIntentForPackage(packageName);
        Button Buttonss = (Button) findViewById(R.id.Alram_btn);
        Buttonss.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CenterActivity.this.startActivity(intent1);
            }
        });



    }
    // 배경색 설정
    private void setBackgroundByTime() {
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String hourText = sdfHour.format(date);

        String nowText = sdfNow.format(date);

        int time = Integer.parseInt(hourText);
        if (time >= 0 && time < 6) {
            constraintLayout.setBackgroundResource(R.drawable.sunny_night_background);
        } else if (time >= 6 && time < 15) {
            constraintLayout.setBackgroundResource(R.drawable.sunny_afternoon_background);
        } else if (time >= 15 && time < 20) {
            constraintLayout.setBackgroundResource(R.drawable.sunny_sunset_background);
        } else if (time >= 20 && time < 24) {
            constraintLayout.setBackgroundResource(R.drawable.sunny_night_background);
        }
    }
}
