package com.example.dailycompass.habits;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitFullInfoActivity extends AppCompatActivity {

    private TextView tvHabitName;
    private TextView tvFrequency;
    private TextView tvReminderTime;
    private TextView tvTrigger;
    private TextView tvTargetDays;
    private TextView tvStatus;
    private Button btnEdit;
    private RecyclerView rvMarks;

    private AppDatabase database;
    private HabitDao habitDao;
    private CompletionMarkDao completionMarkDao;
    private ExecutorService executorService;

    private long habitId;
    private Habit currentHabit;
    private List<HabitDetailAdapter.MarkItem> markList = new ArrayList<>();
    private HabitDetailAdapter marksAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_full_info);

        habitId = getIntent().getLongExtra("habit_id", -1);
        if (habitId == -1) {
            finish();
            return;
        }

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
        loadHabitData();
    }

    private void initViews() {
        tvHabitName = findViewById(R.id.tvHabitName);
        tvFrequency = findViewById(R.id.tvFrequency);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        tvTrigger = findViewById(R.id.tvTrigger);
        tvTargetDays = findViewById(R.id.tvTargetDays);
        tvStatus = findViewById(R.id.tvStatus);
        btnEdit = findViewById(R.id.btnEdit);
        rvMarks = findViewById(R.id.rvMarks);

        rvMarks.setLayoutManager(new LinearLayoutManager(this));
        marksAdapter = new HabitDetailAdapter(markList,
                new HabitDetailAdapter.OnNoteClickListener() {
                    @Override
                    public void onSaveNote(CompletionMark mark, String newText) {
                        saveNoteToDatabase(mark, newText);
                    }
                });
        rvMarks.setAdapter(marksAdapter);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(HabitFullInfoActivity.this, AddEditHabitActivity.class);
            intent.putExtra("habit_id", habitId);
            intent.putExtra("from_full_info", true);
            startActivity(intent);
        });
    }

    private void saveNoteToDatabase(CompletionMark mark, String newText) {
        executorService.execute(() -> {
            mark.setNote(newText);
            completionMarkDao.update(mark);

            runOnUiThread(() -> {
                loadHabitData();
                Toast.makeText(HabitFullInfoActivity.this,
                        "Заметка сохранена", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadHabitData() {
        executorService.execute(() -> {
            currentHabit = habitDao.getHabitById(habitId);
            List<CompletionMark> marks = completionMarkDao.getMarksForHabit(habitId);

            markList.clear();
            for (CompletionMark mark : marks) {
                String note = mark.getNote();
                if (note == null || note.isEmpty()) {
                    note = "Не заполнено";
                }
                markList.add(new HabitDetailAdapter.MarkItem(mark, formatDate(mark.getDate()), note));
            }

            runOnUiThread(() -> {
                if (currentHabit != null) {
                    displayHabitInfo();
                }
                marksAdapter.updateList(markList);
            });
        });
    }

    private void displayHabitInfo() {
        tvHabitName.setText(currentHabit.getName());

        String frequencyText;
        if (currentHabit.getFrequency().startsWith("custom")) {
            if (currentHabit.getFrequency().contains("_")) {
                String days = currentHabit.getFrequency().split("_")[1];
                frequencyText = "Каждые " + days + " дней";
            } else {
                frequencyText = "Пользовательская";
            }
        } else {
            switch (currentHabit.getFrequency()) {
                case "daily":
                    frequencyText = "Ежедневно";
                    break;
                case "weekly":
                    frequencyText = "Еженедельно";
                    break;
                default:
                    frequencyText = "Ежедневно";
            }
        }
        tvFrequency.setText("Частота: " + frequencyText);

        if (currentHabit.getReminderTime() != null && !currentHabit.getReminderTime().isEmpty()) {
            tvReminderTime.setText("Напоминание: " + currentHabit.getReminderTime());
        } else {
            tvReminderTime.setText("Напоминание: не установлено");
        }

        if (currentHabit.getTriggerCondition() != null && !currentHabit.getTriggerCondition().isEmpty()) {
            tvTrigger.setText("Триггер: " + currentHabit.getTriggerCondition());
        } else {
            tvTrigger.setText("Триггер: не указан");
        }

        tvTargetDays.setText("Цель: " + currentHabit.getTargetDays() + " дней");

        if (currentHabit.isArchived()) {
            tvStatus.setText("Статус: Освоена 🏆");
            tvStatus.setTextColor(getColor(R.color.orange_primary));
        } else {
            tvStatus.setText("Статус: Активна ✅");
            tvStatus.setTextColor(getColor(R.color.green_primary));
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, EEEE", new Locale("ru"));
        return sdf.format(new Date(timestamp));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabitData();
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