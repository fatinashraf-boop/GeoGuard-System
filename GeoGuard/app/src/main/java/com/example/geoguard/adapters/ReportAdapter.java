package com.example.geoguard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geoguard.R;
import com.example.geoguard.models.IncidentReport;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(IncidentReport report);
    }

    private final List<IncidentReport> reports;
    private final OnItemClickListener listener;

    public ReportAdapter(List<IncidentReport> reports, OnItemClickListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        IncidentReport report = reports.get(position);
        holder.tvTitle.setText(report.title);
        holder.tvType.setText(report.incident_type);
        holder.tvStatus.setText(report.status != null ? report.status.toUpperCase() : "SUBMITTED");
        holder.tvDate.setText(report.created_at != null ? report.created_at : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(report);
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvType, tvStatus, tvDate;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportTitle);
            tvType = itemView.findViewById(R.id.tvReportType);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
            tvDate = itemView.findViewById(R.id.tvReportDate);
        }
    }
}