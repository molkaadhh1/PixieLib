package com.app.mydashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class AdminReservationAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Map<String, Object>> reservations;
    private DatabaseHelper db;
    private OnReservationActionListener actionListener;

    public interface OnReservationActionListener {
        void onConfirm(int reservationId, int position);
        void onCancel(int reservationId, int position);
    }

    public AdminReservationAdapter(Context context, ArrayList<Map<String, Object>> reservations, OnReservationActionListener actionListener) {
        this.context = context;
        this.reservations = reservations;
        this.actionListener = actionListener;
        this.db = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return reservations.size();
    }

    @Override
    public Object getItem(int position) {
        return reservations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_res_item, parent, false);
        }

        Map<String, Object> res = reservations.get(position);
        int resId = (int) res.get("id");
        String username = (String) res.get("username");
        String bookTitle = (String) res.get("bookTitle");
        String status = (String) res.get("status");
        String confDate = (String) res.get("confirmationDate");

        TextView tvUser = convertView.findViewById(R.id.admin_res_username);
        TextView tvBook = convertView.findViewById(R.id.admin_res_book_title);
        TextView tvBadge = convertView.findViewById(R.id.admin_res_status_badge);
        TextView tvDate = convertView.findViewById(R.id.admin_res_date);
        Button btnConfirm = convertView.findViewById(R.id.admin_res_btn_confirm);
        Button btnCancel = convertView.findViewById(R.id.admin_res_btn_cancel);

        tvUser.setText("Reader: " + username);
        tvBook.setText("Book: " + bookTitle);

        if ("Confirmed".equalsIgnoreCase(status)) {
            tvBadge.setText("Confirmed");
            tvBadge.setBackgroundResource(R.drawable.chip_background_selected); // Green
            tvBadge.setTextColor(context.getResources().getColor(R.color.white));
            tvDate.setVisibility(View.VISIBLE);
            tvDate.setText("Confirmed Date: " + (confDate != null ? confDate : "-"));
            btnConfirm.setVisibility(View.GONE);
        } else {
            tvBadge.setText("Pending");
            tvBadge.setBackgroundResource(R.drawable.chip_background_unselected); // Gray
            tvBadge.setTextColor(context.getResources().getColor(R.color.text_secondary));
            tvDate.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.VISIBLE);
        }

        btnConfirm.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onConfirm(resId, position);
            }
        });

        btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Cancel Reservation")
                    .setMessage("Are you sure you want to cancel the reservation for '" + bookTitle + "' by " + username + "?")
                    .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                        if (db.deleteReservationById(resId)) {
                            Toast.makeText(context, "Reservation cancelled.", Toast.LENGTH_SHORT).show();
                            if (actionListener != null) {
                                actionListener.onCancel(resId, position);
                            }
                        } else {
                            Toast.makeText(context, "Failed to cancel reservation.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }
}
