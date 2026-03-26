package com.dorin.inchirierimasini.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.dorin.inchirierimasini.R;
import com.dorin.inchirierimasini.model.Rental;

import java.util.ArrayList;
import java.util.List;

public class RentalAdapter extends RecyclerView.Adapter<RentalAdapter.RentalViewHolder> {

    private Context context;
    private List<Rental> rentalList;
    private List<Rental> rentalListFull;
    private OnRentalClickListener onRentalClickListener;
    private OnRentalLongClickListener onRentalLongClickListener;
    private int lastPosition = -1;

    public interface OnRentalClickListener {
        void onRentalClick(Rental rental, int position);
    }

    public interface OnRentalLongClickListener {
        void onRentalLongClick(Rental rental, int position);
    }

    public RentalAdapter(Context context, List<Rental> rentalList) {
        this.context = context;
        this.rentalList = rentalList;
        this.rentalListFull = new ArrayList<>(rentalList);
    }

    public void setOnRentalClickListener(OnRentalClickListener listener) {
        this.onRentalClickListener = listener;
    }

    public void setOnRentalLongClickListener(OnRentalLongClickListener listener) {
        this.onRentalLongClickListener = listener;
    }

    @NonNull
    @Override
    public RentalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rental, parent, false);
        return new RentalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentalViewHolder holder, int position) {
        Rental rental = rentalList.get(position);

        holder.tvClientName.setText(rental.getClientName());
        holder.tvClientPhone.setText(rental.getClientPhone());
        holder.tvStartDate.setText(context.getString(R.string.start_date_label) + " " + rental.getStartDate());
        holder.tvEndDate.setText(context.getString(R.string.end_date_label) + " " + rental.getEndDate());
        holder.tvTotalPrice.setText(String.format("%.2f lei", rental.getTotalPrice()));
        holder.tvRentalId.setText("#" + rental.getId());

        String extras = buildExtras(rental);
        holder.tvExtras.setText(extras);

        String statusText = rental.getStatus() != null ? rental.getStatus().toUpperCase() : "ACTIV";
        holder.tvStatus.setText(statusText);
        if ("active".equalsIgnoreCase(rental.getStatus())) {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.available_color, null));
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.not_available_color, null));
        }

        setAnimation(holder.cardView, position);

        holder.itemView.setOnClickListener(v -> {
            if (onRentalClickListener != null) {
                onRentalClickListener.onRentalClick(rental, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onRentalLongClickListener != null) {
                onRentalLongClickListener.onRentalLongClick(rental, holder.getAdapterPosition());
            }
            return true;
        });
    }

    private String buildExtras(Rental rental) {
        List<String> extras = new ArrayList<>();
        if (rental.isWithInsurance()) extras.add("Asigurare");
        if (rental.isHasChildSeat()) extras.add("Scaun copil");
        if (rental.isHasGPS()) extras.add("GPS");
        if (extras.isEmpty()) return context.getString(R.string.no_extras);
        return android.text.TextUtils.join(", ", extras);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return rentalList.size();
    }

    public void updateList(List<Rental> newList) {
        rentalList.clear();
        rentalList.addAll(newList);
        rentalListFull.clear();
        rentalListFull.addAll(newList);
        lastPosition = -1;
        notifyDataSetChanged();
    }

    public void filter(String query) {
        rentalList.clear();
        if (query.isEmpty()) {
            rentalList.addAll(rentalListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Rental rental : rentalListFull) {
                if (rental.getClientName().toLowerCase().contains(lowerQuery) ||
                        rental.getClientPhone().toLowerCase().contains(lowerQuery)) {
                    rentalList.add(rental);
                }
            }
        }
        lastPosition = -1;
        notifyDataSetChanged();
    }

    public void removeRental(int position) {
        Rental rental = rentalList.get(position);
        rentalListFull.remove(rental);
        rentalList.remove(position);
        notifyItemRemoved(position);
    }

    public static class RentalViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvClientName;
        TextView tvClientPhone;
        TextView tvStartDate;
        TextView tvEndDate;
        TextView tvTotalPrice;
        TextView tvRentalId;
        TextView tvExtras;
        TextView tvStatus;

        public RentalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvClientPhone = itemView.findViewById(R.id.tvClientPhone);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvRentalId = itemView.findViewById(R.id.tvRentalId);
            tvExtras = itemView.findViewById(R.id.tvExtras);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
