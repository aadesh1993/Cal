package com.example.tempcalendar.calender.listeners;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.annimon.stream.Stream;
import com.example.tempcalendar.R;
import com.example.tempcalendar.calender.CalendarUtils;
import com.example.tempcalendar.calender.CalendarView;
import com.example.tempcalendar.calender.EventDay;
import com.example.tempcalendar.calender.adapter.CalendarPageAdapter;
import com.example.tempcalendar.calender.utils.CalendarProperties;
import com.example.tempcalendar.calender.utils.DateUtils;
import com.example.tempcalendar.calender.utils.DayColorsUtils;
import com.example.tempcalendar.calender.utils.ImageUtils;
import com.example.tempcalendar.calender.utils.SelectedDay;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * This class is responsible for handle click events
 * <p>
 * Created by Mateusz Kornakiewicz on 24.05.2017.
 */

public class DayRowClickListener implements AdapterView.OnItemClickListener {

    private CalendarPageAdapter mCalendarPageAdapter;

    private CalendarProperties mCalendarProperties;
    private int mPageMonth;
    private View prevView;
    private int prevDate = 0;

    public DayRowClickListener(CalendarPageAdapter calendarPageAdapter, CalendarProperties calendarProperties, int pageMonth) {
        mCalendarPageAdapter = calendarPageAdapter;
        mCalendarProperties = calendarProperties;
        mPageMonth = pageMonth < 0 ? 11 : pageMonth;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Calendar day = new GregorianCalendar();
        day.setTime((Date) adapterView.getItemAtPosition(position));

        if (mCalendarProperties.getOnDayClickListener() != null) {
            onClick(day);
        }

        switch (mCalendarProperties.getCalendarType()) {
            case CalendarView.ONE_DAY_PICKER:
                selectOneDay(view, day);
                break;

            case CalendarView.MANY_DAYS_PICKER:
                selectManyDays(view, day);
                break;

            case CalendarView.RANGE_PICKER:
                selectRange(view, day);
                break;

            case CalendarView.CLASSIC:
                if (prevView == null) {
                    prevView = view;
                    prevDate = day.get(Calendar.DAY_OF_MONTH);
                }
                else {

                    Calendar calendar = Calendar.getInstance();
                    Log.e("prev "+prevDate,"selected "+calendar.get(Calendar.DAY_OF_MONTH));
                    if (prevDate != calendar.get(Calendar.DAY_OF_MONTH)) {
                        prevView.findViewById(R.id.child_layout).setBackground(null);
                        ImageView image = prevView.findViewById(R.id.dayIcon);
                        ImageUtils.loadImage(image, ContextCompat.getDrawable(prevView.getContext(), R.drawable.dark_circle));
                    }
                    prevView = view;
                    prevDate = day.get(Calendar.DAY_OF_MONTH);
                }
                if (!istoday(day)) {
                    view.findViewById(R.id.child_layout).setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.gray_circle));
                    ImageView image = prevView.findViewById(R.id.dayIcon);
                    ImageUtils.loadImage(image, ContextCompat.getDrawable(view.getContext(), R.drawable.light_circle));
                }

                mCalendarPageAdapter.setSelectedDay(new SelectedDay(view, day));
        }
    }

    private void selectOneDay(View view, Calendar day) {
        SelectedDay previousSelectedDay = mCalendarPageAdapter.getSelectedDay();

        TextView dayLabel = (TextView) view.findViewById(R.id.dayLabel);


        if (isAnotherDaySelected(previousSelectedDay, day)) {
            selectDay(dayLabel, day);
            reverseUnselectedColor(previousSelectedDay);
        }
    }

    private void selectManyDays(View view, Calendar day) {
        TextView dayLabel = (TextView) view.findViewById(R.id.dayLabel);

        if (isCurrentMonthDay(day) && isActiveDay(day)) {
            SelectedDay selectedDay = new SelectedDay(dayLabel, day);

            if (!mCalendarPageAdapter.getSelectedDays().contains(selectedDay)) {
                DayColorsUtils.setSelectedDayColors(dayLabel, mCalendarProperties);
            } else {
                reverseUnselectedColor(selectedDay);
            }

            mCalendarPageAdapter.addSelectedDay(selectedDay);
        }
    }

    private void selectRange(View view, Calendar day) {
        TextView dayLabel = (TextView) view.findViewById(R.id.dayLabel);

        if (!isCurrentMonthDay(day) || !isActiveDay(day)) {
            return;
        }

        List<SelectedDay> selectedDays = mCalendarPageAdapter.getSelectedDays();

        if (selectedDays.size() > 1) {
            clearAndSelectOne(dayLabel, day);
        }

        if (selectedDays.size() == 1) {
            selectOneAndRange(dayLabel, day);
        }

        if (selectedDays.isEmpty()) {
            selectDay(dayLabel, day);
        }
    }

    private void clearAndSelectOne(TextView dayLabel, Calendar day) {
        Stream.of(mCalendarPageAdapter.getSelectedDays()).forEach(this::reverseUnselectedColor);
        selectDay(dayLabel, day);
    }

    private void selectOneAndRange(TextView dayLabel, Calendar day) {
        SelectedDay previousSelectedDay = mCalendarPageAdapter.getSelectedDay();

        Stream.of(CalendarUtils.getDatesRange(previousSelectedDay.getCalendar(), day))
                .filter(calendar -> !mCalendarProperties.getDisabledDays().contains(calendar))
                .forEach(calendar -> mCalendarPageAdapter.addSelectedDay(new SelectedDay(calendar)));

        if (isOutOfMaxRange(previousSelectedDay.getCalendar(), day)) {
            return;
        }

        DayColorsUtils.setSelectedDayColors(dayLabel, mCalendarProperties);

        mCalendarPageAdapter.addSelectedDay(new SelectedDay(dayLabel, day));
        mCalendarPageAdapter.notifyDataSetChanged();
    }

    private void selectDay(TextView dayLabel, Calendar day) {
        DayColorsUtils.setSelectedDayColors(dayLabel, mCalendarProperties);
        mCalendarPageAdapter.setSelectedDay(new SelectedDay(dayLabel, day));
    }

    private boolean istoday(Calendar day) {

        Calendar calendar = Calendar.getInstance();
        int currentday = calendar.get(Calendar.DAY_OF_MONTH);
        int selected = day.get(Calendar.DAY_OF_MONTH);

        Log.e("Selected " + selected, "today " + currentday);
        if (currentday == selected) {
            return true;
        } else
            return false;


    }

    private void reverseUnselectedColor(SelectedDay selectedDay) {
        DayColorsUtils.setCurrentMonthDayColors(selectedDay.getCalendar(),
                DateUtils.getCalendar(), (TextView) selectedDay.getView(), mCalendarProperties, null, null);
    }

    private boolean isCurrentMonthDay(Calendar day) {
        return day.get(Calendar.MONTH) == mPageMonth && isBetweenMinAndMax(day);
    }

    private boolean isActiveDay(Calendar day) {
        return !mCalendarProperties.getDisabledDays().contains(day);
    }

    private boolean isBetweenMinAndMax(Calendar day) {
        return !((mCalendarProperties.getMinimumDate() != null && day.before(mCalendarProperties.getMinimumDate()))
                || (mCalendarProperties.getMaximumDate() != null && day.after(mCalendarProperties.getMaximumDate())));
    }

    private boolean isOutOfMaxRange(Calendar firstDay, Calendar lastDay) {
        // Number of selected days plus one last day
        int numberOfSelectedDays = CalendarUtils.getDatesRange(firstDay, lastDay).size() + 1;
        int daysMaxRange = mCalendarProperties.getMaximumDaysRange();

        return daysMaxRange != 0 && numberOfSelectedDays >= daysMaxRange;
    }

    private boolean isAnotherDaySelected(SelectedDay selectedDay, Calendar day) {
        return selectedDay != null && !day.equals(selectedDay.getCalendar())
                && isCurrentMonthDay(day) && isActiveDay(day);
    }

    private void onClick(Calendar day) {
        if (mCalendarProperties.getEventDays() == null) {
            createEmptyEventDay(day);
            return;
        }

        Stream.of(mCalendarProperties.getEventDays())
                .filter(eventDate -> eventDate.getCalendar().equals(day))
                .findFirst()
                .ifPresentOrElse(this::callOnClickListener, () -> createEmptyEventDay(day));
    }

    private void createEmptyEventDay(Calendar day) {
        EventDay eventDay = new EventDay(day);
        callOnClickListener(eventDay);
    }

    private void callOnClickListener(EventDay eventDay) {
        boolean enabledDay = mCalendarProperties.getDisabledDays().contains(eventDay.getCalendar())
                || !isBetweenMinAndMax(eventDay.getCalendar());

        eventDay.setEnabled(enabledDay);
        mCalendarProperties.getOnDayClickListener().onDayClick(eventDay);
    }
}
