package com.example.dailycompass.statistics;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class StatisticsActivity extends AppCompatActivity {

    private Spinner spinnerHabits;
    private TextView tvTotalDays;
    private TextView tvCompletedDays;
    private TextView tvPercent;
    private ProgressBar progressPercent;
    private RecyclerView rvDailyStats;
    private RecyclerView rvWeeklyStats;
    private RecyclerView rvMonthlyStats;

    private AppDatabase database;
    private HabitDao habitDao;
    private CompletionMarkDao completionMarkDao;
    private ExecutorService executorService;

    private List<Habit> habitList = new ArrayList<>();
    private List<CompletionMark> currentMarks = new ArrayList<>();
    private StatAdapter dailyAdapter;
    private StatAdapter weeklyAdapter;
    private StatAdapter monthlyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

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
        loadHabits();
    }

    private void initViews() {
        spinnerHabits = findViewById(R.id.spinnerHabits);
        tvTotalDays = findViewById(R.id.tvTotalDays);
        tvCompletedDays = findViewById(R.id.tvCompletedDays);
        tvPercent = findViewById(R.id.tvPercent);
        progressPercent = findViewById(R.id.progressPercent);
        rvDailyStats = findViewById(R.id.rvDailyStats);
        rvWeeklyStats = findViewById(R.id.rvWeeklyStats);
        rvMonthlyStats = findViewById(R.id.rvMonthlyStats);

        rvDailyStats.setLayoutManager(new LinearLayoutManager(this));
        rvWeeklyStats.setLayoutManager(new LinearLayoutManager(this));
        rvMonthlyStats.setLayoutManager(new LinearLayoutManager(this));

        dailyAdapter = new StatAdapter(new ArrayList<>());
        weeklyAdapter = new StatAdapter(new ArrayList<>());
        monthlyAdapter = new StatAdapter(new ArrayList<>());

        rvDailyStats.setAdapter(dailyAdapter);
        rvWeeklyStats.setAdapter(weeklyAdapter);
        rvMonthlyStats.setAdapter(monthlyAdapter);
    }

    private void loadHabits() {
        executorService.execute(() -> {
            final List<Habit> habits = habitDao.getActiveHabits();
            final List<Habit> archived = habitDao.getArchivedHabits();
            habitList.clear();
            habitList.addAll(habits);
            habitList.addAll(archived);

            runOnUiThread(() -> setupSpinner());
        });
    }

    private void setupSpinner() {
        List<String> habitNames = new ArrayList<>();
        for (Habit habit : habitList) {
            habitNames.add(habit.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, habitNames) {
            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(android.graphics.Color.BLACK);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHabits.setAdapter(adapter);

        spinnerHabits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < habitList.size()) {
                    loadStatisticsForHabit(habitList.get(position).getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadStatisticsForHabit(final long habitId) {
        executorService.execute(() -> {
            currentMarks = completionMarkDao.getMarksForHabit(habitId);
            calculateAndDisplayStats();
        });
    }

    private void calculateAndDisplayStats() {
        if (currentMarks == null || currentMarks.isEmpty()) {
            runOnUiThread(() -> {
                tvTotalDays.setText("0");
                tvCompletedDays.setText("0");
                tvPercent.setText("0%");
                progressPercent.setProgress(0);
                dailyAdapter.updateList(new ArrayList<>());
                weeklyAdapter.updateList(new ArrayList<>());
                monthlyAdapter.updateList(new ArrayList<>());
            });
            return;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        long oldestDate = today;
        for (CompletionMark mark : currentMarks) {
            if (mark.getDate() < oldestDate) {
                oldestDate = mark.getDate();
            }
        }

        long daysDiff = (today - oldestDate) / (24 * 60 * 60 * 1000);
        int totalDays = (int) daysDiff + 1;

        Map<Long, Boolean> completedMap = new HashMap<>();
        for (CompletionMark mark : currentMarks) {
            completedMap.put(mark.getDate(), true);
        }

        int completedCount = completedMap.size();
        int percentCompleted = totalDays > 0 ? (completedCount * 100 / totalDays) : 0;

        List<StatAdapter.StatItem> dailyStats = calculateDailyStats(completedMap, oldestDate, today);
        List<StatAdapter.StatItem> weeklyStats = calculateWeeklyStats(completedMap);
        List<StatAdapter.StatItem> monthlyStats = calculateMonthlyStats(completedMap);

        final int finalPercent = percentCompleted;
        final int finalTotalDays = totalDays;
        final int finalCompletedCount = completedCount;

        runOnUiThread(() -> {
            tvTotalDays.setText(String.valueOf(finalTotalDays));
            tvCompletedDays.setText(String.valueOf(finalCompletedCount));
            tvPercent.setText(finalPercent + "%");
            progressPercent.setProgress(finalPercent);
            dailyAdapter.updateList(dailyStats);
            weeklyAdapter.updateList(weeklyStats);
            monthlyAdapter.updateList(monthlyStats);
        });
    }

    private List<StatAdapter.StatItem> calculateDailyStats(Map<Long, Boolean> completedMap,
                                                           long oldestDate, long today) {
        List<StatAdapter.StatItem> dailyStats = new ArrayList<>();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(today);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM", new Locale("ru"));

        for (int i = 0; i < 30; i++) {
            long date = calendar.getTimeInMillis();
            boolean completed = completedMap.containsKey(date);
            String dateStr = sdf.format(calendar.getTime());

            int percent = completed ? 100 : 0;
            dailyStats.add(new StatAdapter.StatItem(dateStr, completed ? 1 : 0, 1, percent));

            calendar.add(Calendar.DAY_OF_MONTH, -1);
            if (date < oldestDate) break;
        }

        return dailyStats;
    }

    private List<StatAdapter.StatItem> calculateWeeklyStats(Map<Long, Boolean> completedMap) {
        List<StatAdapter.StatItem> weeklyStats = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("'Неделя' w, yyyy", new Locale("ru"));

        for (int i = 0; i < 12; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            long weekStart = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_WEEK, 6);
            long weekEnd = calendar.getTimeInMillis();

            int completed = 0;
            int total = 0;

            Calendar dayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            dayCal.setTimeInMillis(weekStart);
            for (int d = 0; d < 7; d++) {
                long date = dayCal.getTimeInMillis();
                if (completedMap.containsKey(date)) {
                    completed++;
                }
                total++;
                dayCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            int percent = total > 0 ? (completed * 100 / total) : 0;
            String periodName = sdf.format(calendar.getTime());
            weeklyStats.add(new StatAdapter.StatItem(periodName, completed, total, percent));

            calendar.add(Calendar.DAY_OF_MONTH, -7);
        }

        return weeklyStats;
    }

    private List<StatAdapter.StatItem> calculateMonthlyStats(Map<Long, Boolean> completedMap) {
        List<StatAdapter.StatItem> monthlyStats = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("ru"));

        for (int i = 0; i < 12; i++) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MONTH, -i);

            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long monthStart = calendar.getTimeInMillis();

            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            int completed = 0;

            Calendar dayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            dayCal.setTimeInMillis(monthStart);
            for (int d = 0; d < daysInMonth; d++) {
                long date = dayCal.getTimeInMillis();
                if (completedMap.containsKey(date)) {
                    completed++;
                }
                dayCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            int percent = daysInMonth > 0 ? (completed * 100 / daysInMonth) : 0;
            String periodName = sdf.format(calendar.getTime());
            monthlyStats.add(new StatAdapter.StatItem(periodName, completed, daysInMonth, percent));
        }

        return monthlyStats;
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