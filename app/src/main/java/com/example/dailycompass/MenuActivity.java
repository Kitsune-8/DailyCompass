package com.example.dailycompass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.dailycompass.calendar.CalendarActivity;
import com.example.dailycompass.habits.HabitsActivity;
import com.example.dailycompass.habits.MasteredActivity;
import com.example.dailycompass.statistics.StatisticsActivity;
import com.example.dailycompass.summary.DailySummaryActivity;

public class MenuActivity extends AppCompatActivity {

    private CardView cardHabits;
    private CardView cardMastered;
    private CardView cardStatistics;
    private CardView cardCalendar;
    private CardView cardExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardHabits = findViewById(R.id.cardHabits);
        cardMastered = findViewById(R.id.cardMastered);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardCalendar = findViewById(R.id.cardCalendar);
        cardExit = findViewById(R.id.cardExit);

        cardHabits.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, HabitsActivity.class);
            startActivity(intent);
        });

        cardMastered.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MasteredActivity.class);
            startActivity(intent);
        });

        cardStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        cardCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        cardExit.setOnClickListener(v -> showExitDialog());
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выход из приложения");
        builder.setMessage("Вы уверены, что хотите выйти?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Да", (dialog, which) -> finishAffinity());
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}