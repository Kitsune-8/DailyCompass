package com.example.dailycompass.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.dailycompass.R;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CalendarAdapter extends BaseAdapter {

    private Context context;
    private List<CalendarDay> days;
    private Map<Long, Boolean> completionMap;

    public CalendarAdapter(Context context, List<CalendarDay> days, Map<Long, Boolean> completionMap) {
        this.context = context;
        this.days = days;
        this.completionMap = completionMap;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView tvDay = convertView.findViewById(R.id.tvDay);
        View indicator = convertView.findViewById(R.id.viewIndicator);
        View dayContainer = convertView.findViewById(R.id.dayContainer);

        CalendarDay day = days.get(position);

        if (day.isCurrentMonth() && day.getDayNumber() > 0) {
            tvDay.setText(String.valueOf(day.getDayNumber()));
            tvDay.setVisibility(View.VISIBLE);

            // Получаем timestamp для текущего дня
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.set(Calendar.DAY_OF_MONTH, day.getDayNumber());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long date = cal.getTimeInMillis();

            // Если в этот день есть хотя бы одна отметка выполнения - зелёный фон
            if (completionMap.containsKey(date)) {
                dayContainer.setBackgroundColor(Color.parseColor("#1A4CAF50"));
                indicator.setVisibility(View.VISIBLE);
            } else {
                dayContainer.setBackgroundColor(Color.TRANSPARENT);
                indicator.setVisibility(View.GONE);
            }

            if (day.isToday()) {
                tvDay.setTextColor(Color.parseColor("#2196F3"));
                tvDay.setTypeface(tvDay.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                tvDay.setTextColor(Color.parseColor("#333333"));
                tvDay.setTypeface(tvDay.getTypeface(), android.graphics.Typeface.NORMAL);
            }
        } else {
            tvDay.setVisibility(View.INVISIBLE);
            indicator.setVisibility(View.GONE);
            dayContainer.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }
}