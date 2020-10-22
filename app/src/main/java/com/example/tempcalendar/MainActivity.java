package com.example.tempcalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.tempcalendar.calender.CalendarView;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
      //  calendarView.setBackgroundColor(getResources().getColor(R.color.spinner_background));

        calendarView.setHeaderVisibility(View.GONE);
    }
}
