package com.dorin.inchirierimasini;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dorin.inchirierimasini.db.DatabaseHelper;
import com.dorin.inchirierimasini.model.Car;

public class AddEditCarActivity extends AppCompatActivity {

    private EditText etBrand, etModel, etYear, etPrice, etDescription;
    private ToggleButton toggleAvailable;
    private SeekBar seekBarRating;
    private TextView tvRatingValue;
    private Button btnSave;
    private ImageView ivCarImage;

    private DatabaseHelper dbHelper;
    private Car editCar = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_car);

        // Enter animation
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etYear = findViewById(R.id.etYear);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        toggleAvailable = findViewById(R.id.toggleAvailable);
        seekBarRating = findViewById(R.id.seekBarRating);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        btnSave = findViewById(R.id.btnSave);
        ivCarImage = findViewById(R.id.ivCarImage);

        // Animate image view on enter
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        ivCarImage.startAnimation(scaleUp);

        // SeekBar for rating (0-4 -> maps to 1-5 stars)
        seekBarRating.setMax(4);
        seekBarRating.setProgress(2); // Default rating 3
        updateRatingText(3);

        seekBarRating.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rating = progress + 1;
                updateRatingText(rating);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Check if editing existing car
        if (getIntent().hasExtra("car")) {
            editCar = getIntent().getParcelableExtra("car");
            isEditMode = true;
            populateFields(editCar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.edit_car);
            }
            btnSave.setText(R.string.update_car);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.add_car);
            }
        }

        btnSave.setOnClickListener(v -> {
            // Button press animation
            ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.9f, 1f);
            ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.9f, 1f);
            scaleXAnim.setDuration(200);
            scaleYAnim.setDuration(200);
            scaleXAnim.start();
            scaleYAnim.start();

            saveCar();
        });

        // Animate whole form on enter
        View formLayout = findViewById(R.id.scrollView);
        if (formLayout != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            formLayout.startAnimation(fadeIn);
        }
    }

    private void updateRatingText(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) stars.append("★");
        for (int i = rating; i < 5; i++) stars.append("☆");
        tvRatingValue.setText(rating + "/5  " + stars.toString());
    }

    private void populateFields(Car car) {
        etBrand.setText(car.getBrand());
        etModel.setText(car.getModel());
        etYear.setText(String.valueOf(car.getYear()));
        etPrice.setText(String.valueOf(car.getPricePerDay()));
        etDescription.setText(car.getDescription());
        toggleAvailable.setChecked(car.isAvailable());
        int ratingProgress = (int) car.getRating() - 1;
        if (ratingProgress < 0) ratingProgress = 0;
        if (ratingProgress > 4) ratingProgress = 4;
        seekBarRating.setProgress(ratingProgress);
        updateRatingText((int) car.getRating());
    }

    private void saveCar() {
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (brand.isEmpty()) {
            etBrand.setError(getString(R.string.field_required));
            etBrand.requestFocus();
            return;
        }
        if (model.isEmpty()) {
            etModel.setError(getString(R.string.field_required));
            etModel.requestFocus();
            return;
        }
        if (yearStr.isEmpty()) {
            etYear.setError(getString(R.string.field_required));
            etYear.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError(getString(R.string.field_required));
            etPrice.requestFocus();
            return;
        }

        int year;
        double price;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            etYear.setError(getString(R.string.invalid_year));
            etYear.requestFocus();
            return;
        }
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError(getString(R.string.invalid_price));
            etPrice.requestFocus();
            return;
        }

        if (year < 1900 || year > 2100) {
            etYear.setError(getString(R.string.invalid_year));
            etYear.requestFocus();
            return;
        }
        if (price <= 0) {
            etPrice.setError(getString(R.string.invalid_price));
            etPrice.requestFocus();
            return;
        }

        boolean available = toggleAvailable.isChecked();
        float rating = seekBarRating.getProgress() + 1f;

        if (isEditMode && editCar != null) {
            editCar.setBrand(brand);
            editCar.setModel(model);
            editCar.setYear(year);
            editCar.setPricePerDay(price);
            editCar.setDescription(description);
            editCar.setAvailable(available);
            editCar.setRating(rating);

            int rows = dbHelper.updateCar(editCar);
            if (rows > 0) {
                Toast.makeText(this, getString(R.string.car_updated_success), Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "edit");
                resultIntent.putExtra("car", editCar);
                setResult(RESULT_OK, resultIntent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            } else {
                Toast.makeText(this, getString(R.string.update_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Car newCar = new Car();
            newCar.setBrand(brand);
            newCar.setModel(model);
            newCar.setYear(year);
            newCar.setPricePerDay(price);
            newCar.setDescription(description);
            newCar.setAvailable(available);
            newCar.setRating(rating);

            long id = dbHelper.addCar(newCar);
            if (id > 0) {
                newCar.setId(id);
                Toast.makeText(this, getString(R.string.car_added_success), Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "add");
                resultIntent.putExtra("car", newCar);
                setResult(RESULT_OK, resultIntent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            } else {
                Toast.makeText(this, getString(R.string.add_error), Toast.LENGTH_SHORT).show();
            }
        }
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
