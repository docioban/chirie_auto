package com.dorin.inchirierimasini.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.dorin.inchirierimasini.R;
import com.dorin.inchirierimasini.model.Car;

import java.util.ArrayList;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;
    private List<Car> carListFull;
    private OnCarClickListener onCarClickListener;
    private OnCarLongClickListener onCarLongClickListener;
    private int lastPosition = -1;

    public interface OnCarClickListener {
        void onCarClick(Car car, int position);
    }

    public interface OnCarLongClickListener {
        void onCarLongClick(Car car, int position);
    }

    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
        this.carListFull = new ArrayList<>(carList);
    }

    public void setOnCarClickListener(OnCarClickListener listener) {
        this.onCarClickListener = listener;
    }

    public void setOnCarLongClickListener(OnCarLongClickListener listener) {
        this.onCarLongClickListener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        holder.tvBrand.setText(car.getBrand());
        holder.tvModel.setText(car.getModel());
        holder.tvYear.setText(String.valueOf(car.getYear()));
        holder.tvPrice.setText(String.format("%.0f lei/zi", car.getPricePerDay()));
        holder.tvRating.setText(String.format("%.1f", car.getRating()));

        if (car.isAvailable()) {
            holder.tvAvailability.setText(context.getString(R.string.available));
            holder.tvAvailability.setTextColor(context.getResources().getColor(R.color.available_color, null));
        } else {
            holder.tvAvailability.setText(context.getString(R.string.not_available));
            holder.tvAvailability.setTextColor(context.getResources().getColor(R.color.not_available_color, null));
        }

        // Build star rating display
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) car.getRating();
        for (int i = 0; i < fullStars; i++) stars.append("★");
        for (int i = fullStars; i < 5; i++) stars.append("☆");
        holder.tvStars.setText(stars.toString());

        // Add animation for new items
        setAnimation(holder.cardView, position);

        holder.itemView.setOnClickListener(v -> {
            if (onCarClickListener != null) {
                onCarClickListener.onCarClick(car, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onCarLongClickListener != null) {
                onCarLongClickListener.onCarLongClick(car, holder.getAdapterPosition());
            }
            return true;
        });
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
        return carList.size();
    }

    public void updateList(List<Car> newList) {
        carList.clear();
        carList.addAll(newList);
        carListFull.clear();
        carListFull.addAll(newList);
        lastPosition = -1;
        notifyDataSetChanged();
    }

    public void filter(String query) {
        carList.clear();
        if (query.isEmpty()) {
            carList.addAll(carListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Car car : carListFull) {
                if (car.getBrand().toLowerCase().contains(lowerQuery) ||
                        car.getModel().toLowerCase().contains(lowerQuery) ||
                        String.valueOf(car.getYear()).contains(lowerQuery)) {
                    carList.add(car);
                }
            }
        }
        lastPosition = -1;
        notifyDataSetChanged();
    }

    public void addCar(Car car) {
        carList.add(0, car);
        carListFull.add(0, car);
        notifyItemInserted(0);
    }

    public void removeCar(int position) {
        Car car = carList.get(position);
        carListFull.remove(car);
        carList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateCar(Car car) {
        for (int i = 0; i < carList.size(); i++) {
            if (carList.get(i).getId() == car.getId()) {
                carList.set(i, car);
                notifyItemChanged(i);
                break;
            }
        }
        for (int i = 0; i < carListFull.size(); i++) {
            if (carListFull.get(i).getId() == car.getId()) {
                carListFull.set(i, car);
                break;
            }
        }
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivCarIcon;
        TextView tvBrand;
        TextView tvModel;
        TextView tvYear;
        TextView tvPrice;
        TextView tvAvailability;
        TextView tvRating;
        TextView tvStars;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivCarIcon = itemView.findViewById(R.id.ivCarIcon);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvModel = itemView.findViewById(R.id.tvModel);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStars = itemView.findViewById(R.id.tvStars);
        }
    }
}
