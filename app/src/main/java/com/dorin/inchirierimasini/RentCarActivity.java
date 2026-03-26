package com.dorin.inchirierimasini;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dorin.inchirierimasini.db.DatabaseHelper;
import com.dorin.inchirierimasini.model.Car;
import com.dorin.inchirierimasini.model.Rental;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class RentCarActivity extends AppCompatActivity {

    private EditText etClientName, etClientPhone;
    private Button btnPickStartDate, btnPickEndDate, btnPickTime, btnRent;
    private RadioGroup radioGroupInsurance;
    private RadioButton rbWithInsurance, rbWithoutInsurance;
    private CheckBox cbChildSeat, cbGPS;
    private TextView tvCarInfo, tvTotalPrice, tvStartDate, tvEndDate, tvPickupTime;
    private View coordinatorLayout;

    private DatabaseHelper dbHelper;
    private Car car;
    private String startDate = "";
    private String endDate = "";
    private String pickupTime = "";
    private int startDay, startMonth, startYear;
    private int endDay, endMonth, endYear;

    private static final double INSURANCE_COST_PER_DAY = 15.0;
    private static final double CHILD_SEAT_COST = 10.0;
    private static final double GPS_COST = 5.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_car);

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
            getSupportActionBar().setTitle(R.string.rent_car);
        }

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        etClientName = findViewById(R.id.etClientName);
        etClientPhone = findViewById(R.id.etClientPhone);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnRent = findViewById(R.id.btnRent);
        radioGroupInsurance = findViewById(R.id.radioGroupInsurance);
        rbWithInsurance = findViewById(R.id.rbWithInsurance);
        rbWithoutInsurance = findViewById(R.id.rbWithoutInsurance);
        cbChildSeat = findViewById(R.id.cbChildSeat);
        cbGPS = findViewById(R.id.cbGPS);
        tvCarInfo = findViewById(R.id.tvCarInfo);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvPickupTime = findViewById(R.id.tvPickupTime);

        tvCarInfo.setText(car.getBrand() + " " + car.getModel() + " (" + car.getYear() + ") - " +
                String.format("%.0f lei/zi", car.getPricePerDay()));

        // Animate car info
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvCarInfo.startAnimation(fadeIn);

        // Set default dates
        Calendar cal = Calendar.getInstance();
        startDay = cal.get(Calendar.DAY_OF_MONTH);
        startMonth = cal.get(Calendar.MONTH);
        startYear = cal.get(Calendar.YEAR);
        endDay = startDay + 1;
        endMonth = startMonth;
        endYear = startYear;

        startDate = String.format("%02d/%02d/%04d", startDay, startMonth + 1, startYear);
        endDate = String.format("%02d/%02d/%04d", endDay, endMonth + 1, endYear);
        tvStartDate.setText(startDate);
        tvEndDate.setText(endDate);

        btnPickStartDate.setOnClickListener(v -> showStartDatePicker());
        btnPickEndDate.setOnClickListener(v -> showEndDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());

        radioGroupInsurance.setOnCheckedChangeListener((group, checkedId) -> calculateTotalPrice());
        cbChildSeat.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotalPrice());
        cbGPS.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotalPrice());

        calculateTotalPrice();

        btnRent.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();
            processRental();
        });
    }

    private void showStartDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    startYear = year;
                    startMonth = month;
                    startDay = dayOfMonth;
                    startDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    tvStartDate.setText(startDate);
                    calculateTotalPrice();
                }, startYear, startMonth, startDay);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    endYear = year;
                    endMonth = month;
                    endDay = dayOfMonth;
                    endDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    tvEndDate.setText(endDate);
                    calculateTotalPrice();
                }, endYear, endMonth, endDay);
        dialog.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    pickupTime = String.format("%02d:%02d", hourOfDay, minute);
                    tvPickupTime.setText(getString(R.string.pickup_time_label) + " " + pickupTime);
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private int getDaysBetween() {
        Calendar start = Calendar.getInstance();
        start.set(startYear, startMonth, startDay);
        Calendar end = Calendar.getInstance();
        end.set(endYear, endMonth, endDay);
        long diff = end.getTimeInMillis() - start.getTimeInMillis();
        int days = (int) (diff / (1000 * 60 * 60 * 24));
        return Math.max(1, days);
    }

    private void calculateTotalPrice() {
        int days = getDaysBetween();
        double total = car.getPricePerDay() * days;

        if (rbWithInsurance.isChecked()) {
            total += INSURANCE_COST_PER_DAY * days;
        }
        if (cbChildSeat.isChecked()) {
            total += CHILD_SEAT_COST;
        }
        if (cbGPS.isChecked()) {
            total += GPS_COST;
        }

        tvTotalPrice.setText(getString(R.string.total_price_label) + " " +
                String.format("%.2f lei", total) + " (" + days + " " +
                getString(R.string.days) + ")");
    }

    private void processRental() {
        String clientName = etClientName.getText().toString().trim();
        String clientPhone = etClientPhone.getText().toString().trim();

        if (clientName.isEmpty()) {
            etClientName.setError(getString(R.string.field_required));
            etClientName.requestFocus();
            return;
        }
        if (clientPhone.isEmpty()) {
            etClientPhone.setError(getString(R.string.field_required));
            etClientPhone.requestFocus();
            return;
        }
        if (startDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.select_start_date), Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.select_end_date), Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar start = Calendar.getInstance();
        start.set(startYear, startMonth, startDay);
        Calendar end = Calendar.getInstance();
        end.set(endYear, endMonth, endDay);

        if (!end.after(start)) {
            Toast.makeText(this, getString(R.string.invalid_date_range), Toast.LENGTH_SHORT).show();
            return;
        }

        int days = getDaysBetween();
        double total = car.getPricePerDay() * days;
        boolean withInsurance = rbWithInsurance.isChecked();
        boolean hasChildSeat = cbChildSeat.isChecked();
        boolean hasGPS = cbGPS.isChecked();

        if (withInsurance) total += INSURANCE_COST_PER_DAY * days;
        if (hasChildSeat) total += CHILD_SEAT_COST;
        if (hasGPS) total += GPS_COST;

        Rental rental = new Rental();
        rental.setCarId(car.getId());
        rental.setClientName(clientName);
        rental.setClientPhone(clientPhone);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setTotalPrice(total);
        rental.setWithInsurance(withInsurance);
        rental.setHasChildSeat(hasChildSeat);
        rental.setHasGPS(hasGPS);
        rental.setStatus("active");

        long rentalId = dbHelper.addRental(rental);
        if (rentalId > 0) {
            // Update car availability
            car.setAvailable(false);
            dbHelper.updateCar(car);

            Snackbar.make(coordinatorLayout,
                    getString(R.string.rental_success, clientName),
                    Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.ok), v -> {})
                    .show();

            btnRent.postDelayed(() -> {
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }, 1500);
        } else {
            Toast.makeText(this, getString(R.string.rental_error), Toast.LENGTH_SHORT).show();
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
