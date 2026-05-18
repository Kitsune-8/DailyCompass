package com.example.dailycompass.habits;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.dailycompass.R;
import com.example.dailycompass.data.AppDatabase;
import com.example.dailycompass.data.HabitDao;
import com.example.dailycompass.models.Habit;
import com.example.dailycompass.utils.ReminderAlarmManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditHabitActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private AutoCompleteTextView actFrequency;
    private TextInputEditText etReminderTime;
    private TextInputEditText etTriggerCondition;
    private LinearLayout colorContainer;
    private RadioGroup rgTargetDays;
    private RadioButton rb7Days;
    private RadioButton rb21Days;
    private RadioButton rb100Days;
    private Button btnSave;
    private Button btnCancel;

    private Habit editHabit;
    private String selectedColor = "#4CAF50";
    private int selectedTargetDays = 7;
    private String selectedFrequency = "daily";
    private boolean isEditMode = false; // Флаг для определения режима

    private AppDatabase database;
    private HabitDao habitDao;
    private ExecutorService executorService;

    private final String[] colors = {
            "#4CAF50", "#2196F3", "#FF9800", "#F44336",
            "#9C27B0", "#00BCD4", "#795548", "#607D8B"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);

        database = AppDatabase.getInstance(this);
        habitDao = database.habitDao();
        executorService = Executors.newSingleThreadExecutor();

        // Получаем ID привычки для редактирования
        long editId = getIntent().getLongExtra("habit_id", -1);
        isEditMode = (editId != -1);

        if (isEditMode) {
            loadHabitForEdit(editId);
        }

        initViews();
        setupToolbar();
        setupListeners();
        setupColorPicker();
        setupFrequencyDropdown();
        setupTimePicker();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        actFrequency = findViewById(R.id.actFrequency);
        etReminderTime = findViewById(R.id.etReminderTime);
        etTriggerCondition = findViewById(R.id.etTriggerCondition);
        colorContainer = findViewById(R.id.colorContainer);
        rgTargetDays = findViewById(R.id.rgTargetDays);
        rb7Days = findViewById(R.id.rb7Days);
        rb21Days = findViewById(R.id.rb21Days);
        rb100Days = findViewById(R.id.rb100Days);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (isEditMode) {
            toolbar.setTitle("Редактирование привычки");
        } else {
            toolbar.setTitle("Добавление привычки");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveHabit());
        btnCancel.setOnClickListener(v -> finish());

        rgTargetDays.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb7Days) {
                selectedTargetDays = 7;
            } else if (checkedId == R.id.rb21Days) {
                selectedTargetDays = 21;
            } else if (checkedId == R.id.rb100Days) {
                selectedTargetDays = 100;
            }
        });
    }

    private void setupFrequencyDropdown() {
        String[] frequencies = {"Ежедневно", "Еженедельно"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, frequencies);
        actFrequency.setAdapter(adapter);

        actFrequency.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedFrequency = "daily";
            } else {
                selectedFrequency = "weekly";
            }
        });
    }

    private void setupTimePicker() {
        etReminderTime.setInputType(android.text.InputType.TYPE_NULL);
        etReminderTime.setFocusable(true);
        etReminderTime.setFocusableInTouchMode(true);
        etReminderTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(AddEditHabitActivity.this,
                    (view, hourOfDay, minuteOfHour) -> {
                        String time = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                        etReminderTime.setText(time);
                    }, hour, minute, true);
            timePicker.show();
        });
    }

    private void setupColorPicker() {
        for (String colorHex : colors) {
            CardView colorCard = new CardView(this);
            CardView.LayoutParams params = new CardView.LayoutParams(80, 80);
            params.setMargins(8, 8, 8, 8);
            colorCard.setLayoutParams(params);
            colorCard.setCardElevation(4);
            colorCard.setRadius(40);
            colorCard.setCardBackgroundColor(Color.parseColor(colorHex));
            colorCard.setClickable(true);
            colorCard.setFocusable(true);

            colorCard.setOnClickListener(v -> {
                selectedColor = colorHex;
                highlightSelectedColor(colorCard);
            });

            colorContainer.addView(colorCard);
        }
    }

    private void highlightSelectedColor(CardView selectedCard) {
        for (int i = 0; i < colorContainer.getChildCount(); i++) {
            View child = colorContainer.getChildAt(i);
            if (child instanceof CardView) {
                child.setElevation(4);
                child.setScaleX(1.0f);
                child.setScaleY(1.0f);
            }
        }
        selectedCard.setElevation(12);
        selectedCard.setScaleX(1.1f);
        selectedCard.setScaleY(1.1f);
    }

    private void loadHabitForEdit(long habitId) {
        executorService.execute(() -> {
            editHabit = habitDao.getHabitById(habitId);
            runOnUiThread(() -> {
                if (editHabit != null) {
                    fillFieldsWithHabitData();
                }
            });
        });
    }

    private void fillFieldsWithHabitData() {
        etName.setText(editHabit.getName());
        selectedColor = editHabit.getColor();
        selectedTargetDays = editHabit.getTargetDays();
        selectedFrequency = editHabit.getFrequency();

        if (selectedFrequency.equals("daily")) {
            actFrequency.setText("Ежедневно");
        } else {
            actFrequency.setText("Еженедельно");
        }

        if (editHabit.getReminderTime() != null && !editHabit.getReminderTime().isEmpty()) {
            etReminderTime.setText(editHabit.getReminderTime());
        }

        if (editHabit.getTriggerCondition() != null && !editHabit.getTriggerCondition().isEmpty()) {
            etTriggerCondition.setText(editHabit.getTriggerCondition());
        }

        if (selectedTargetDays == 7) {
            rb7Days.setChecked(true);
        } else if (selectedTargetDays == 21) {
            rb21Days.setChecked(true);
        } else if (selectedTargetDays == 100) {
            rb100Days.setChecked(true);
        }

        for (int i = 0; i < colorContainer.getChildCount() && i < colors.length; i++) {
            if (colors[i].equals(selectedColor)) {
                highlightSelectedColor((CardView) colorContainer.getChildAt(i));
                break;
            }
        }
    }

    private void saveHabit() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Введите название привычки");
            return;
        }

        String reminderTime = etReminderTime.getText().toString().trim();
        if (reminderTime.isEmpty()) {
            reminderTime = null;
        }

        String triggerCondition = etTriggerCondition.getText().toString().trim();
        if (triggerCondition.isEmpty()) {
            triggerCondition = null;
        }

        final String finalReminderTime = reminderTime;
        final String finalTriggerCondition = triggerCondition;
        final boolean fromFullInfo = getIntent().getBooleanExtra("from_full_info", false);
        final long habitIdForReturn = getIntent().getLongExtra("habit_id", -1);

        executorService.execute(() -> {
            long savedId = -1;
            if (editHabit != null) {
                // Редактирование существующей привычки
                editHabit.setName(name);
                editHabit.setFrequency(selectedFrequency);
                editHabit.setReminderTime(finalReminderTime);
                editHabit.setColor(selectedColor);
                editHabit.setTriggerCondition(finalTriggerCondition);
                editHabit.setTargetDays(selectedTargetDays);
                habitDao.update(editHabit);
                savedId = editHabit.getId();

                if (finalReminderTime != null && !finalReminderTime.isEmpty()) {
                    ReminderAlarmManager.rescheduleReminder(
                            AddEditHabitActivity.this,
                            savedId,
                            name,
                            finalReminderTime
                    );
                } else {
                    ReminderAlarmManager.cancelReminder(AddEditHabitActivity.this, savedId);
                }
            } else {
                // Создание новой привычки
                Habit newHabit = new Habit(
                        name, selectedFrequency, finalReminderTime,
                        "ic_default", selectedColor, finalTriggerCondition,
                        selectedTargetDays
                );
                savedId = habitDao.insert(newHabit);

                if (finalReminderTime != null && !finalReminderTime.isEmpty()) {
                    ReminderAlarmManager.scheduleReminder(
                            AddEditHabitActivity.this,
                            savedId,
                            name,
                            finalReminderTime
                    );
                }
            }

            final long finalSavedId = savedId;
            runOnUiThread(() -> {
                Toast.makeText(AddEditHabitActivity.this,
                        editHabit != null ? "Привычка обновлена" : "Привычка добавлена",
                        Toast.LENGTH_SHORT).show();

                if (fromFullInfo && finalSavedId != -1) {
                    Intent intent = new Intent(AddEditHabitActivity.this, HabitFullInfoActivity.class);
                    intent.putExtra("habit_id", finalSavedId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                finish();
            });
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