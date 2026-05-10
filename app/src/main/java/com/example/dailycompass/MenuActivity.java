package com.example.dailycompass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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

        cardHabits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, HabitsActivity.class);
                startActivity(intent);
            }
        });

        cardMastered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MenuActivity.this, "Освоенные привычки", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MenuActivity.this, MasteredActivity.class));
            }
        });

        cardStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MenuActivity.this, "Статистика", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MenuActivity.this, StatisticsActivity.class));
            }
        });

        cardCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MenuActivity.this, "Календарь", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(MenuActivity.this, CalendarActivity.class));
            }
        });

        cardExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_theme_light) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Светлая тема", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_theme_dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Тёмная тема", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выход из приложения");
        builder.setMessage("Вы уверены, что хотите выйти?");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}