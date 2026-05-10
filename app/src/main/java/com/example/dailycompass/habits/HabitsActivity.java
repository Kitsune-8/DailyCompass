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

        // Инициализация базы данных
        database = AppDatabase.getInstance(this);
        habitDao = database.habitDao();
        completionMarkDao = database.completionMarkDao();
        milestoneDao = database.milestoneDao();
        executorService = Executors.newSingleThreadExecutor();

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Настройка RecyclerView
        rvHabits = findViewById(R.id.rvHabits);
        rvHabits.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HabitAdapter(habitList,
                new HabitAdapter.OnHabitClickListener() {
                    @Override
                    public void onHabitClick(Habit habit) {
                        // Редактирование привычки
                        Intent intent = new Intent(HabitsActivity.this, AddEditHabitActivity.class);
                        intent.putExtra("habit_id", habit.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onHabitLongClick(Habit habit) {
                        // Удаление привычки
                        showDeleteConfirmation(habit);
                    }
                },
                new HabitAdapter.OnCompleteClickListener() {
                    @Override
                    public void onCompleteClick(Habit habit) {
                        // Отметка выполнения
                        showCompletionDialog(habit);
                    }
                });

        rvHabits.setAdapter(adapter);

        // Кнопка добавления привычки
        FloatingActionButton fabAdd = findViewById(R.id.fabAddHabit);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HabitsActivity.this, AddEditHabitActivity.class);
                startActivity(intent);
            }
        });

        // Загрузка списка привычек
        loadHabits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }

    private void loadHabits() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final List<Habit> habits = habitDao.getActiveHabits();

                // Для каждой привычки загружаем отметки и рассчитываем серию
                for (final Habit habit : habits) {
                    List<CompletionMark> marks = completionMarkDao.getMarksForHabit(habit.getId());
                    final int streak = StreakCalculator.calculateCurrentStreak(marks);
                    habit.setCurrentStreak(streak); // Временно храним серию в объекте
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        habitList.clear();
                        habitList.addAll(habits);
                        adapter.updateList(habitList);

                        // Обновляем отображение серии в адаптере
                        for (int i = 0; i < habitList.size(); i++) {
                            Habit habit = habitList.get(i);
                            int streak = habit.getCurrentStreak();
                            adapter.updateStreak(i, streak);
                        }
                    }
                });
            }
        });
    }

    private void showDeleteConfirmation(final Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление привычки");
        builder.setMessage("Вы уверены, что хотите удалить привычку \"" + habit.getName() + "\"?\nВсе отметки выполнения также будут удалены.");
        builder.setPositiveButton("Удалить", (dialog, which) -> {
            deleteHabit(habit);
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void deleteHabit(final Habit habit) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                habitDao.delete(habit);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadHabits();
                        Toast.makeText(HabitsActivity.this, "Привычка удалена", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showCompletionDialog(final Habit habit) {
        // Получаем сегодняшнюю дату (начало дня)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final long today = calendar.getTimeInMillis();

        // Проверяем, не отмечена ли уже сегодня
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                CompletionMark existingMark = completionMarkDao.getMarkByDate(habit.getId(), today);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (existingMark != null) {
                            Toast.makeText(HabitsActivity.this,
                                    "Вы уже отметили эту привычку сегодня!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Диалог с полем для заметки
                        android.widget.EditText editText = new android.widget.EditText(HabitsActivity.this);
                        editText.setHint("Заметка о выполнении (необязательно)");

                        AlertDialog.Builder builder = new AlertDialog.Builder(HabitsActivity.this);
                        builder.setTitle("Отметка выполнения");
                        builder.setMessage("Выполнено: " + habit.getName());
                        builder.setView(editText);
                        builder.setPositiveButton("Сохранить", (dialog, which) -> {
                            String note = editText.getText().toString().trim();
                            saveCompletionMark(habit, today, note);
                        });
                        builder.setNegativeButton("Отмена", null);
                        builder.show();
                    }
                });
            }
        });
    }

    private void saveCompletionMark(final Habit habit, final long date, final String note) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Сохраняем отметку выполнения
                CompletionMark mark = new CompletionMark(habit.getId(), date, note);
                completionMarkDao.insert(mark);

                // Получаем все отметки для расчёта серии
                List<CompletionMark> allMarks = completionMarkDao.getMarksForHabit(habit.getId());
                int currentStreak = StreakCalculator.calculateCurrentStreak(allMarks);

                // Получаем уже достигнутые рубежи
                List<Milestone> existingMilestones = milestoneDao.getMilestonesForHabit(habit.getId());
                List<Integer> achievedValues = new ArrayList<>();
                for (Milestone m : existingMilestones) {
                    achievedValues.add(m.getTargetValue());
                }

                // Проверяем новый рубеж
                int newMilestone = StreakCalculator.checkNewMilestone(currentStreak, achievedValues);

                if (newMilestone > 0) {
                    // Сохраняем рубеж
                    Milestone milestone = new Milestone(habit.getId(), newMilestone, System.currentTimeMillis());
                    milestoneDao.insert(milestone);

                    // Если достигнут целевой рубеж - архивируем привычку
                    if (currentStreak >= habit.getTargetDays()) {
                        habitDao.archiveHabit(habit.getId(), System.currentTimeMillis(), newMilestone);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HabitsActivity.this,
                                        "🏆 Поздравляем! Привычка \"" + habit.getName() + "\" освоена!",
                                        Toast.LENGTH_LONG).show();
                                loadHabits();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HabitsActivity.this,
                                        "🎉 Отлично! Серия: " + currentStreak + " дней!",
                                        Toast.LENGTH_SHORT).show();
                                loadHabits();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HabitsActivity.this,
                                    "✓ Выполнение отмечено! Серия: " + currentStreak + " дней",
                                    Toast.LENGTH_SHORT).show();
                            loadHabits();
                        }
                    });
                }
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