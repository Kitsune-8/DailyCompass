package com.example.dailycompass.calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dailycompass.R;
import com.example.dailycompass.data.AppDatabase;
import com.example.dailycompass.data.CompletionMarkDao;
import com.example.dailycompass.data.HabitDao;
import com.example.dailycompass.models.CompletionMark;
import com.example.dailycompass.models.Habit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvMonthYear;
    private TableLayout tableCalendar;
    private TextView tvPrevMonth;
    private TextView tvNextMonth;

    private AppDatabase database;
    private HabitDao habitDao;
    private CompletionMarkDao completionMarkDao;
    private ExecutorService executorService;

    private Map<Long, Boolean> completionMap = new HashMap<>();
    private Calendar currentCalendar;

    // Цвет для выполненных дней: #4CAF50 с прозрачностью 10% (90% прозрачности)
    private static final String COMPLETED_DAY_COLOR = "#4CAF50";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        database = AppDatabase.getInstance(this);
        habitDao = database.habitDao();
        completionMarkDao = database.completionMarkDao();
        executorService = Executors.newSingleThreadExecutor();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initViews();
        currentCalendar = Calendar.getInstance(TimeZone.getDefault());
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);

        loadAllCompletionData();
    }

    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tableCalendar = findViewById(R.id.tableCalendar);
        tvPrevMonth = findViewById(R.id.tvPrevMonth);
        tvNextMonth = findViewById(R.id.tvNextMonth);

        tvPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarDisplay();
        });

        tvNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarDisplay();
        });
    }

    private void loadAllCompletionData() {
        executorService.execute(() -> {
            List<Habit> activeHabits = habitDao.getActiveHabits();

            completionMap.clear();
            for (Habit habit : activeHabits) {
                List<CompletionMark> marks = completionMarkDao.getMarksForHabit(habit.getId());
                for (CompletionMark mark : marks) {
                    long localDate = convertToLocalDate(mark.getDate());
                    completionMap.put(localDate, true);
                }
            }

            runOnUiThread(() -> updateCalendarDisplay());
        });
    }

    private long convertToLocalDate(long utcTimestamp) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTimeInMillis(utcTimestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void updateCalendarDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy", new Locale("ru"));
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));

        tableCalendar.removeAllViews();

        List<CalendarDay> days = getMonthDays(currentCalendar);

        int rowCount = 6;
        int dayIndex = 0;

        for (int row = 0; row < rowCount; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 7; col++) {
                if (dayIndex < days.size()) {
                    CalendarDay day = days.get(dayIndex);
                    TextView dayView = createDayView(day);
                    tableRow.addView(dayView);
                    dayIndex++;
                } else {
                    TextView emptyView = new TextView(this);
                    emptyView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    emptyView.setPadding(8, 12, 8, 12);
                    tableRow.addView(emptyView);
                }
            }
            tableCalendar.addView(tableRow);
        }
    }

    private TextView createDayView(CalendarDay day) {
        TextView dayView = new TextView(this);
        dayView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        dayView.setGravity(android.view.Gravity.CENTER);
        dayView.setPadding(8, 12, 8, 12);

        if (day.isCurrentMonth() && day.getDayNumber() > 0) {
            dayView.setText(String.valueOf(day.getDayNumber()));
            dayView.setTextSize(14);

            // Получаем дату дня в локальной временной зоне
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.set(Calendar.DAY_OF_MONTH, day.getDayNumber());
            cal.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH));
            cal.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long date = cal.getTimeInMillis();

            // Выполнен ли день
            if (completionMap.containsKey(date)) {
                dayView.setBackgroundColor(Color.parseColor(COMPLETED_DAY_COLOR));
            } else {
                dayView.setBackgroundColor(Color.TRANSPARENT);
            }

            // Сегодняшний день
            Calendar todayCal = Calendar.getInstance(TimeZone.getDefault());
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            long today = todayCal.getTimeInMillis();

            if (date == today) {
                dayView.setTextColor(Color.parseColor("#2196F3"));
                dayView.setTypeface(dayView.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                dayView.setTextColor(Color.parseColor("#333333"));
            }
        } else {
            dayView.setText("");
            dayView.setBackgroundColor(Color.TRANSPARENT);
        }

        return dayView;
    }

    private List<CalendarDay> getMonthDays(Calendar calendar) {
        List<CalendarDay> days = new ArrayList<>();

        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < offset; i++) {
            days.add(new CalendarDay(0, false, false));
        }

        Calendar todayCal = Calendar.getInstance(TimeZone.getDefault());
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long today = todayCal.getTimeInMillis();

        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            boolean isToday = (cal.getTimeInMillis() == today);
            days.add(new CalendarDay(i, true, isToday));
        }

        return days;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllCompletionData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}