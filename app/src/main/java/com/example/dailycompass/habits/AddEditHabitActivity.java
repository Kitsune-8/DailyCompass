package com.example.dailycompass.habits;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditHabitActivity extends AppCompatActivity {

    // UI элементы
    private TextInputEditText etName;
    private AutoCompleteTextView actFrequency;
    private TextInputEditText etReminderTime;
    private TextInputEditText etTriggerCondition;
    private LinearLayout colorContainer;
    private MaterialButtonToggleGroup toggleTargetDays;
    private Button btnSave;
    private Button btnCancel;

    // Данные
    private Habit editHabit; // Если null - добавление, если не null - редактирование
    private String selectedColor = "#4CAF50"; // цвет по умолчанию
    private int selectedTargetDays = 7; // цель по умолчанию
    private String selectedFrequency = "daily";

    // БД
    private AppDatabase database;
    private HabitDao habitDao;
    private ExecutorService executorService;

    // Предустановленные цвета
    private final String[] colors = {
            "#4CAF50", "#2196F3", "#FF9800", "#F44336",
            "#9C27B0", "#00BCD4", "#795548", "#607D8B"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);

        // Инициализация БД
        database = AppDatabase.getInstance(this);
        habitDao = database.habitDao();
        executorService = Executors.newSingleThreadExecutor();

        // Проверяем, редактирование или добавление
        long editId = getIntent().getLongExtra("habit_id", -1);
        if (editId != -1) {
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
        toggleTargetDays = findViewById(R.id.toggleTargetDays);
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

        if (editHabit != null) {
            toolbar.setTitle("Редактирование привычки");
        } else {
            toolbar.setTitle("Добавление привычки");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHabit();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Выбор целевой периодичности
        toggleTargetDays.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.btn7Days) {
                        selectedTargetDays = 7;
                    } else if (checkedId == R.id.btn21Days) {
                        selectedTargetDays = 21;
                    } else if (checkedId == R.id.btn100Days) {
                        selectedTargetDays = 100;
                    }
                }
            }
        });
    }

    private void setupFrequencyDropdown() {
        String[] frequencies = {"Ежедневно", "Еженедельно", "Пользовательская"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, frequencies);
        actFrequency.setAdapter(adapter);
        actFrequency.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    selectedFrequency = "daily";
                    break;
                case 1:
                    selectedFrequency = "weekly";
                    break;
                case 2:
                    selectedFrequency = "custom";
                    break;
            }
        });
    }

    private void setupTimePicker() {
        etReminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(AddEditHabitActivity.this,
                        (view, hourOfDay, minuteOfHour) -> {
                            String time = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                            etReminderTime.setText(time);
                        }, hour, minute, true);
                timePicker.show();
            }
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

            colorCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedColor = colorHex;
                    highlightSelectedColor(colorCard);
                }
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
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                editHabit = habitDao.getHabitById(habitId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (editHabit != null) {
                            fillFieldsWithHabitData();
                        }
                    }
                });
            }
        });
    }

    private void fillFieldsWithHabitData() {
        etName.setText(editHabit.getName());
        selectedColor = editHabit.getColor();
        selectedTargetDays = editHabit.getTargetDays();
        selectedFrequency = editHabit.getFrequency();

        // Частота
        switch (selectedFrequency) {
            case "daily":
                actFrequency.setText("Ежедневно", false);
                break;
            case "weekly":
                actFrequency.setText("Еженедельно", false);
                break;
            case "custom":
                actFrequency.setText("Пользовательская", false);
                break;
        }

        // Время напоминания
        if (editHabit.getReminderTime() != null && !editHabit.getReminderTime().isEmpty()) {
            etReminderTime.setText(editHabit.getReminderTime());
        }

        // Триггер-условие
        if (editHabit.getTriggerCondition() != null && !editHabit.getTriggerCondition().isEmpty()) {
            etTriggerCondition.setText(editHabit.getTriggerCondition());
        }

        // Целевая периодичность
        if (selectedTargetDays == 7) {
            toggleTargetDays.check(R.id.btn7Days);
        } else if (selectedTargetDays == 21) {
            toggleTargetDays.check(R.id.btn21Days);
        } else if (selectedTargetDays == 100) {
            toggleTargetDays.check(R.id.btn100Days);
        }

        // Подсветка выбранного цвета
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

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (editHabit != null) {
                    // Редактирование
                    editHabit.setName(name);
                    editHabit.setFrequency(selectedFrequency);
                    editHabit.setReminderTime(finalReminderTime);
                    editHabit.setColor(selectedColor);
                    editHabit.setTriggerCondition(finalTriggerCondition);
                    editHabit.setTargetDays(selectedTargetDays);
                    habitDao.update(editHabit);
                } else {
                    // Добавление новой привычки
                    Habit newHabit = new Habit(
                            name, selectedFrequency, finalReminderTime,
                            "ic_default", selectedColor, finalTriggerCondition,
                            selectedTargetDays
                    );
                    habitDao.insert(newHabit);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddEditHabitActivity.this,
                                editHabit != null ? "Привычка обновлена" : "Привычка добавлена",
                                Toast.LENGTH_SHORT).show();
                        finish();
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
        executorService.shutdown();
    }
}