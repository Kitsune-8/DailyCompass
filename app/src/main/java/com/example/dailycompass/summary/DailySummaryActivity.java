package com.example.dailycompass.summary;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailySummaryActivity extends AppCompatActivity {

    private TextView tvDate;
    private TextView tvTotalHabits;
    private TextView tvCompletedCount;
    private TextView tvPercent;
    private RecyclerView rvCompletedHabits;
    private RecyclerView rvPendingHabits;

    private AppDatabase database;
    private HabitDao habitDao;
    private CompletionMarkDao completionMarkDao;
    private ExecutorService executorService;

    private SummaryAdapter completedAdapter;
    private SummaryAdapter pendingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_summary);

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
        loadDailySummary();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvTotalHabits = findViewById(R.id.tvTotalHabits);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvPercent = findViewById(R.id.tvPercent);
        rvCompletedHabits = findViewById(R.id.rvCompletedHabits);
        rvPendingHabits = findViewById(R.id.rvPendingHabits);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, EEEE", new Locale("ru"));
        tvDate.setText(sdf.format(Calendar.getInstance().getTime()));

        rvCompletedHabits.setLayoutManager(new LinearLayoutManager(this));
        rvPendingHabits.setLayoutManager(new LinearLayoutManager(this));

        completedAdapter = new SummaryAdapter(new ArrayList<>());
        pendingAdapter = new SummaryAdapter(new ArrayList<>());

        rvCompletedHabits.setAdapter(completedAdapter);
        rvPendingHabits.setAdapter(pendingAdapter);
    }

    private void loadDailySummary() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                final long today = calendar.getTimeInMillis();

                final List<Habit> activeHabits = habitDao.getActiveHabits();
                final List<SummaryItem> completedItems = new ArrayList<>();
                final List<SummaryItem> pendingItems = new ArrayList<>();

                for (Habit habit : activeHabits) {
                    CompletionMark mark = completionMarkDao.getMarkByDate(habit.getId(), today);
                    if (mark != null) {
                        completedItems.add(new SummaryItem(habit.getName(), mark.getNote(), habit.getColor()));
                    } else {
                        pendingItems.add(new SummaryItem(habit.getName(), null, habit.getColor()));
                    }
                }

                final int totalHabits = activeHabits.size();
                final int completedCount = completedItems.size();
                final int percent = totalHabits > 0 ? (completedCount * 100 / totalHabits) : 0;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTotalHabits.setText(String.valueOf(totalHabits));
                        tvCompletedCount.setText(String.valueOf(completedCount));
                        tvPercent.setText(percent + "%");
                        completedAdapter.updateList(completedItems);
                        pendingAdapter.updateList(pendingItems);

                        if (completedCount == totalHabits && totalHabits > 0) {
                            Toast.makeText(DailySummaryActivity.this,
                                    "🎉 Отлично! Все привычки выполнены!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
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

    public static class SummaryItem {
        public String habitName;
        public String note;
        public String color;

        public SummaryItem(String habitName, String note, String color) {
            this.habitName = habitName;
            this.note = note;
            this.color = color;
        }
    }
}