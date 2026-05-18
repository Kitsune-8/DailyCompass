package com.example.dailycompass.habits;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailycompass.R;
import com.example.dailycompass.models.CompletionMark;

import java.util.List;

public class HabitDetailAdapter extends RecyclerView.Adapter<HabitDetailAdapter.ViewHolder> {

    private List<MarkItem> marks;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onSaveNote(CompletionMark mark, String newText);
    }

    public static class MarkItem {
        public CompletionMark mark;
        public String formattedDate;
        public String displayNote;

        public MarkItem(CompletionMark mark, String formattedDate, String displayNote) {
            this.mark = mark;
            this.formattedDate = formattedDate;
            this.displayNote = displayNote;
        }
    }

    public HabitDetailAdapter(List<MarkItem> marks, OnNoteClickListener listener) {
        this.marks = marks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarkItem item = marks.get(position);
        CompletionMark mark = item.mark;

        holder.tvDate.setText(item.formattedDate);

        // Устанавливаем текст в EditText
        String currentText = item.displayNote;
        if (currentText.equals("Не заполнено")) {
            holder.etNote.setText("");
            holder.etNote.setHint("Нет заметки. Напишите что-нибудь...");
            holder.etNote.setHintTextColor(Color.parseColor("#888888"));
        } else {
            holder.etNote.setText(currentText);
            holder.etNote.setHint("");
        }
        holder.etNote.setTextColor(Color.parseColor("#333333"));

        // Сохраняем текущий текст для проверки изменений
        final String[] savedText = {item.displayNote};

        // Кнопка Сохранить
        holder.btnSave.setOnClickListener(v -> {
            String newText = holder.etNote.getText().toString().trim();
            if (newText.isEmpty()) {
                newText = "Не заполнено";
            }
            // Сохраняем только если текст изменился
            if (!newText.equals(savedText[0])) {
                savedText[0] = newText;
                if (listener != null) {
                    listener.onSaveNote(mark, newText);
                }
                // Обновляем отображение
                if (newText.equals("Не заполнено")) {
                    holder.etNote.setText("");
                    holder.etNote.setHint("Нет заметки. Напишите что-нибудь...");
                } else {
                    holder.etNote.setText(newText);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return marks == null ? 0 : marks.size();
    }

    public void updateList(List<MarkItem> newMarks) {
        this.marks = newMarks;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDate;
        EditText etNote;
        Button btnSave;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvDate = itemView.findViewById(R.id.tvDate);
            etNote = itemView.findViewById(R.id.etNote);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}