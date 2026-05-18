package com.example.freeze;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;

public class ActiveAdapter extends RecyclerView.Adapter<ActiveAdapter.MyViewHolder> {
    private ArrayList<AppModel> apps;

    // Constructor updated: We no longer pass H and M because we get the absolute time from DB
    public ActiveAdapter(ArrayList<AppModel> apps) {
        this.apps = apps;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frozen, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AppModel app = apps.get(position);
        holder.name.setText(app.getAppName());
        holder.icon.setImageDrawable(app.getAppIcon());

        // FIX: Get the exact target moment from the database
       // DatabaseHelper db = new DatabaseHelper(holder.itemView.getContext());
        long targetMillis = app.getTargetTime();
        long diff = targetMillis - System.currentTimeMillis();

        if (holder.countDown != null) holder.countDown.cancel();

        // If the time hasn't reached the target yet, start the countdown
        if (diff > 0) {
            holder.countDown = new CountDownTimer(diff, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Manual math to ensure Cameroon local time stays accurate
                    long totalSecs = millisUntilFinished / 1000;
                    long h = totalSecs / 3600;
                    long m = (totalSecs % 3600) / 60;
                    long s = totalSecs % 60;

                    holder.timerText.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
                }

                @Override
                public void onFinish() {
                    holder.timerText.setText("Unlock");
                }
            }.start();
        } else {
            // If time is already up, show Unlock immediately
            holder.timerText.setText("Unlock");
        }

        // KEEPING YOUR UNLOCK LISTENER LOGIC
        holder.timerText.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && holder.timerText.getText().toString().equals("Unlock")) {
                // Remove from DB and refresh list
                new DatabaseHelper(v.getContext()).unfreezeSingleApp(apps.get(currentPos).getPackageName());
                apps.remove(currentPos);
                notifyItemRemoved(currentPos);

                // If no apps are left frozen, close the activity
                if (apps.isEmpty() && v.getContext() instanceof Activity) {
                    ((Activity) v.getContext()).finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, timerText;
        ImageView icon;
        CountDownTimer countDown;

        public MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.txt_app_name);
            timerText = v.findViewById(R.id.txt_countdown);
            icon = v.findViewById(R.id.img_app_icon);
        }
    }
}