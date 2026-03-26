package com.dorin.inchirierimasini;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dorin.inchirierimasini.db.DatabaseHelper;
import com.dorin.inchirierimasini.model.Car;

public class CarDetailActivity extends AppCompatActivity {

    private TextView tvBrand, tvModel, tvYear, tvPrice, tvDescription,
            tvAvailability, tvRating, tvStars, tvRentalCount;
    private ImageView ivCarImage;
    private Button btnRentCar, btnEditCar;

    private Car car;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> editCarLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Car updatedCar = result.getData().getParcelableExtra("car");
                    if (updatedCar != null) {
                        car = updatedCar;
                        populateUI();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> rentCarLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Reload car from DB to get updated availability
                    car = dbHelper.getCarById(car.getId());
                    if (car != null) {
                        populateUI();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);

        dbHelper = new DatabaseHelper(this);
        car = getIntent().getParcelableExtra("car");

        if (car == null) {
            Toast.makeText(this, getString(R.string.error_no_car), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(car.getBrand() + " " + car.getModel());
        }

        tvBrand = findViewById(R.id.tvBrand);
        tvModel = findViewById(R.id.tvModel);
        tvYear = findViewById(R.id.tvYear);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvAvailability = findViewById(R.id.tvAvailability);
        tvRating = findViewById(R.id.tvRating);
        tvStars = findViewById(R.id.tvStars);
        tvRentalCount = findViewById(R.id.tvRentalCount);
        ivCarImage = findViewById(R.id.ivCarImage);
        btnRentCar = findViewById(R.id.btnRentCar);
        btnEditCar = findViewById(R.id.btnEditCar);

        populateUI();

        // Animate image
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        ivCarImage.startAnimation(scaleUp);

        // Animate content
        View contentLayout = findViewById(R.id.contentLayout);
        if (contentLayout != null) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            contentLayout.startAnimation(slideIn);
        }

        btnRentCar.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();

            if (!car.isAvailable()) {
                Toast.makeText(this, getString(R.string.car_not_available), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, RentCarActivity.class);
            intent.putExtra("car", car);
            rentCarLauncher.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
        });

        btnEditCar.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();

            Intent intent = new Intent(this, AddEditCarActivity.class);
            intent.putExtra("car", car);
            editCarLauncher.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
        });
    }

    private void populateUI() {
        tvBrand.setText(car.getBrand());
        tvModel.setText(car.getModel());
        tvYear.setText(getString(R.string.year_label) + " " + car.getYear());
        tvPrice.setText(String.format("%.0f lei/zi", car.getPricePerDay()));
        tvDescription.setText(car.getDescription() != null && !car.getDescription().isEmpty()
                ? car.getDescription() : getString(R.string.no_description));

        if (car.isAvailable()) {
            tvAvailability.setText(getString(R.string.available));
            tvAvailability.setTextColor(getResources().getColor(R.color.available_color, null));
            btnRentCar.setEnabled(true);
        } else {
            tvAvailability.setText(getString(R.string.not_available));
            tvAvailability.setTextColor(getResources().getColor(R.color.not_available_color, null));
            btnRentCar.setEnabled(false);
        }

        tvRating.setText(String.format("%.1f / 5.0", car.getRating()));

        StringBuilder stars = new StringBuilder();
        int fullStars = (int) car.getRating();
        for (int i = 0; i < fullStars; i++) stars.append("★");
        for (int i = fullStars; i < 5; i++) stars.append("☆");
        tvStars.setText(stars.toString());

        int rentalCount = dbHelper.getRentalsByCarId(car.getId()).size();
        tvRentalCount.setText(getString(R.string.rental_count_label) + " " + rentalCount);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
