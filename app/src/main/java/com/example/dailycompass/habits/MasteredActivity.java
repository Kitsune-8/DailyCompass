package com.example.dailycompass.habits;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import com.example.dailycompass.utils.StreakCalculator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MasteredActivity extends AppCompatActivity {

    private RecyclerView rvMasteredHabits;
    private TextView tvEmpty;
    private MasteredHabitAdapter adapter;
    private List<Habit> habitList = new ArrayList<>();
    private AppDatabase database;
    private HabitDao habitDao;
    private CompletionMarkDao completionMarkDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mastered);

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

        rvMasteredHabits = findViewById(R.id.rvMasteredHabits);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvMasteredHabits.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MasteredHabitAdapter(habitList,
                new MasteredHabitAdapter.OnMasteredHabitClickListener() {
                    @Override
                    public void onMasteredClick(Habit habit) {
                        Intent intent = new Intent(MasteredActivity.this, HabitFullInfoActivity.class);
                        intent.putExtra("habit_id", habit.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onMasteredLongClick(Habit habit) {
                        showDeleteConfirmation(habit);
                    }
                });

        rvMasteredHabits.setAdapter(adapter);
        loadMasteredHabits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMasteredHabits();
    }

    private void loadMasteredHabits() {
        executorService.execute(() -> {
            final List<Habit> masteredHabits = habitDao.getArchivedHabits();

            for (final Habit habit : masteredHabits) {
                List<CompletionMark> marks = completionMarkDao.getMarksForHabit(habit.getId());
                final int streak = StreakCalculator.calculateCurrentStreak(marks);
                habit.setCurrentStreak(streak);
            }

            runOnUiThread(() -> {
                habitList.clear();
                habitList.addAll(masteredHabits);
                adapter.updateList(habitList);

                if (habitList.isEmpty()) {
                    rvMasteredHabits.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvMasteredHabits.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                }
            });
        });
    }

    private void showDeleteConfirmation(final Habit habit) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление из архива")
                .setMessage("Удалить \"" + habit.getName() + "\" из архива?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    executorService.execute(() -> {
                        habitDao.delete(habit);
                        runOnUiThread(() -> {
                            loadMasteredHabits();
                            Toast.makeText(MasteredActivity.this, "Привычка удалена", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Отмена", null)
                .show();
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