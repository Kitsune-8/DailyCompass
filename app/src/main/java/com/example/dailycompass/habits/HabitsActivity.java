package com.example.dailycompass.habits;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycompass.R;
import com.example.dailycompass.data.AppDatabase;
import com.example.dailycompass.data.CompletionMarkDao;
import com.example.dailycompass.data.HabitDao;
import com.example.dailycompass.data.MilestoneDao;
import com.example.dailycompass.models.CompletionMark;
import com.example.dailycompass.models.Habit;
import com.example.dailycompass.models.Milestone;
import com.example.dailycompass.utils.NotificationHelper;
import com.example.dailycompass.utils.StreakCalculator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitsActivity extends AppCompatActivity {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private List<Habit> habitList = new ArrayList<>();
    private AppDatabase database;
    private HabitDao habitDao;
    private CompletionMarkDao completionMarkDao;
    private MilestoneDao milestoneDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        database = AppDatabase.getInstance(this);
        habitDao = database.habitDao();
        completionMarkDao = database.completionMarkDao();
        milestoneDao = database.milestoneDao();
        executorService = Executors.newSingleThreadExecutor();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvHabits = findViewById(R.id.rvHabits);
        rvHabits.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HabitAdapter(habitList,
                new HabitAdapter.OnHabitClickListener() {
                    @Override
                    public void onHabitClick(Habit habit) {
                        Intent intent = new Intent(HabitsActivity.this, HabitFullInfoActivity.class);
                        intent.putExtra("habit_id", habit.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onHabitLongClick(Habit habit) {
                        showDeleteConfirmation(habit);
                    }
                },
                new HabitAdapter.OnCompleteClickListener() {
                    @Override
                    public void onCompleteClick(Habit habit) {
                        showCompletionDialog(habit);
                    }
                });

        rvHabits.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAddHabit);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HabitsActivity.this, AddEditHabitActivity.class);
            startActivity(intent);
        });

        loadHabits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }

    private void loadHabits() {
        executorService.execute(() -> {
            final List<Habit> habits = habitDao.getActiveHabits();

            for (final Habit habit : habits) {
                List<CompletionMark> marks = completionMarkDao.getMarksForHabit(habit.getId());
                final int streak = StreakCalculator.calculateCurrentStreak(marks);
                habit.setCurrentStreak(streak);
            }

            runOnUiThread(() -> {
                habitList.clear();
                habitList.addAll(habits);
                adapter.updateList(habitList);

                for (int i = 0; i < habitList.size(); i++) {
                    Habit habit = habitList.get(i);
                    int streak = habit.getCurrentStreak();
                    adapter.updateStreak(i, streak);
                }
            });
        });
    }

    private void showDeleteConfirmation(final Habit habit) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление привычки")
                .setMessage("Вы уверены, что хотите удалить привычку \"" + habit.getName() + "\"?\nВсе отметки выполнения также будут удалены.")
                .setPositiveButton("Удалить", (dialog, which) -> deleteHabit(habit))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteHabit(final Habit habit) {
        executorService.execute(() -> {
            habitDao.delete(habit);
            runOnUiThread(() -> {
                loadHabits();
                Toast.makeText(HabitsActivity.this, "Привычка удалена", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showCompletionDialog(final Habit habit) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final long today = calendar.getTimeInMillis();

        executorService.execute(() -> {
            CompletionMark existingMark = completionMarkDao.getMarkByDate(habit.getId(), today);

            runOnUiThread(() -> {
                if (existingMark != null) {
                    Toast.makeText(HabitsActivity.this,
                            "Вы уже отметили эту привычку сегодня!", Toast.LENGTH_SHORT).show();
                    return;
                }

                android.widget.EditText editText = new android.widget.EditText(HabitsActivity.this);
                editText.setHint("Заметка о выполнении (необязательно)");

                new AlertDialog.Builder(HabitsActivity.this)
                        .setTitle("Отметка выполнения")
                        .setMessage("Выполнено: " + habit.getName())
                        .setView(editText)
                        .setPositiveButton("Сохранить", (dialog, which) -> {
                            String note = editText.getText().toString().trim();
                            saveCompletionMark(habit, today, note);
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        });
    }

    private void saveCompletionMark(final Habit habit, final long date, final String note) {
        executorService.execute(() -> {
            CompletionMark mark = new CompletionMark(habit.getId(), date, note);
            completionMarkDao.insert(mark);

            List<CompletionMark> allMarks = completionMarkDao.getMarksForHabit(habit.getId());
            int currentStreak = StreakCalculator.calculateCurrentStreak(allMarks);

            List<Milestone> existingMilestones = milestoneDao.getMilestonesForHabit(habit.getId());
            List<Integer> achievedValues = new ArrayList<>();
            for (Milestone m : existingMilestones) {
                achievedValues.add(m.getTargetValue());
            }

            int newMilestone = StreakCalculator.checkNewMilestone(currentStreak, achievedValues);

            if (newMilestone > 0) {
                Milestone milestone = new Milestone(habit.getId(), newMilestone, System.currentTimeMillis());
                milestoneDao.insert(milestone);

                NotificationHelper.sendMilestoneNotification(
                        HabitsActivity.this,
                        habit.getName(),
                        currentStreak,
                        newMilestone
                );

                if (currentStreak >= habit.getTargetDays()) {
                    habitDao.archiveHabit(habit.getId(), System.currentTimeMillis(), newMilestone);

                    runOnUiThread(() -> {
                        NotificationHelper.sendMasteredNotification(
                                HabitsActivity.this,
                                habit.getName(),
                                currentStreak
                        );
                        Toast.makeText(HabitsActivity.this,
                                "🏆 Поздравляем! Привычка \"" + habit.getName() + "\" освоена!",
                                Toast.LENGTH_LONG).show();
                        loadHabits();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(HabitsActivity.this,
                                "🎉 Отлично! Серия: " + currentStreak + " дней!",
                                Toast.LENGTH_SHORT).show();
                        loadHabits();
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(HabitsActivity.this,
                            "✓ Выполнение отмечено! Серия: " + currentStreak + " дней",
                            Toast.LENGTH_SHORT).show();
                    loadHabits();
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
}