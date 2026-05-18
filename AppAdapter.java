package com.example.freeze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.MyViewHolder> {

    private final Context context;
    private List<AppModel> appModelList;


    public AppAdapter(Context context, List<AppModel> appModelList){
        this.context = context;
        this.appModelList = appModelList;
    }

    public void setFilteredList(List<AppModel> filteredList) {
        this.appModelList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.app_display, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AppModel currentApp = appModelList.get(position);


        holder.checkBox.setText(currentApp.getAppName());
        holder.icon.setImageDrawable(currentApp.getAppIcon());

        // Remove listener before setting state to avoid recursion bugs
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(currentApp.is_Selected());

        // Updates selection when checkbox is clicked
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentApp.setIs_Selected(isChecked);
        });

        // Updates selection when the whole row (icon/area) is clicked
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !currentApp.is_Selected();
            currentApp.setSelected(newState);
            holder.checkBox.setChecked(newState);
        });
    }

    @Override
    public int getItemCount() {
        return (appModelList != null) ? appModelList.size() : 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.img_app_icon);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

}