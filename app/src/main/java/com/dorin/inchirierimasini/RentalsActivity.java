package com.dorin.inchirierimasini;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dorin.inchirierimasini.adapter.RentalAdapter;
import com.dorin.inchirierimasini.db.DatabaseHelper;
import com.dorin.inchirierimasini.dialogs.ConfirmDeleteDialog;
import com.dorin.inchirierimasini.model.Rental;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class RentalsActivity extends AppCompatActivity
        implements ConfirmDeleteDialog.OnDeleteConfirmedListener {

    private RecyclerView recyclerViewRentals;
    private RentalAdapter rentalAdapter;
    private List<Rental> rentalList;
    private DatabaseHelper dbHelper;
    private EditText etSearchRentals;
    private TextView tvEmptyState;
    private View coordinatorLayout;
    private long pendingDeleteId = -1;
    private int pendingDeletePosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rentals);

        overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.rentals);
        }

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        recyclerViewRentals = findViewById(R.id.recyclerViewRentals);
        etSearchRentals = findViewById(R.id.etSearchRentals);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rentalList = new ArrayList<>();
        rentalAdapter = new RentalAdapter(this, rentalList);
        recyclerViewRentals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRentals.setAdapter(rentalAdapter);

        rentalAdapter.setOnRentalClickListener((rental, position) -> {
            showRentalDetailsDialog(rental);
        });

        rentalAdapter.setOnRentalLongClickListener((rental, position) -> {
            pendingDeleteId = rental.getId();
            pendingDeletePosition = position;
            ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(
                    getString(R.string.confirm_delete),
                    getString(R.string.delete_rental_message, rental.getClientName()),
                    rental.getId()
            );
            dialog.show(getSupportFragmentManager(), "confirmDeleteRental");
        });

        etSearchRentals.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rentalAdapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Animate on enter
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        recyclerViewRentals.startAnimation(fadeIn);

        loadRentalsFromDB();
    }

    private void loadRentalsFromDB() {
        rentalList = dbHelper.getAllRentals();
        rentalAdapter.updateList(rentalList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (rentalAdapter.getItemCount() == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewRentals.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewRentals.setVisibility(View.VISIBLE);
        }
    }

    private void showRentalDetailsDialog(Rental rental) {
        StringBuilder details = new StringBuilder();
        details.append(getString(R.string.client_name_label)).append(": ").append(rental.getClientName()).append("\n");
        details.append(getString(R.string.client_phone_label)).append(": ").append(rental.getClientPhone()).append("\n");
        details.append(getString(R.string.start_date_label)).append(": ").append(rental.getStartDate()).append("\n");
        details.append(getString(R.string.end_date_label)).append(": ").append(rental.getEndDate()).append("\n");
        details.append(getString(R.string.total_price_label)).append(": ").append(String.format("%.2f lei", rental.getTotalPrice())).append("\n");
        details.append(getString(R.string.insurance_label)).append(": ").append(rental.isWithInsurance() ? getString(R.string.yes) : getString(R.string.no)).append("\n");
        details.append(getString(R.string.child_seat_label)).append(": ").append(rental.isHasChildSeat() ? getString(R.string.yes) : getString(R.string.no)).append("\n");
        details.append(getString(R.string.gps_label)).append(": ").append(rental.isHasGPS() ? getString(R.string.yes) : getString(R.string.no)).append("\n");
        details.append(getString(R.string.status_label)).append(": ").append(rental.getStatus());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.rental_details_title) + " #" + rental.getId())
                .setMessage(details.toString())
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton(R.string.mark_completed, (dialog, which) -> {
                    rental.setStatus("completed");
                    dbHelper.updateRental(rental);
                    rentalAdapter.updateList(dbHelper.getAllRentals());
                    Toast.makeText(this, R.string.rental_completed, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rental_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_clear_completed) {
            showClearCompletedDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearCompletedDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.clear_completed_title)
                .setMessage(R.string.clear_completed_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    List<Rental> allRentals = dbHelper.getAllRentals();
                    int deletedCount = 0;
                    for (Rental rental : allRentals) {
                        if ("completed".equalsIgnoreCase(rental.getStatus())) {
                            dbHelper.deleteRental(rental.getId());
                            deletedCount++;
                        }
                    }
                    loadRentalsFromDB();
                    Snackbar.make(coordinatorLayout,
                            getString(R.string.cleared_rentals, deletedCount),
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onDeleteConfirmed(long itemId) {
        int rows = dbHelper.deleteRental(itemId);
        if (rows > 0 && pendingDeletePosition >= 0) {
            rentalAdapter.removeRental(pendingDeletePosition);
            updateEmptyState();
            Snackbar.make(coordinatorLayout,
                    getString(R.string.rental_deleted_success),
                    Snackbar.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
        }
        pendingDeleteId = -1;
        pendingDeletePosition = -1;
    }

    @Override
    public void onDeleteCancelled() {
        pendingDeleteId = -1;
        pendingDeletePosition = -1;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
