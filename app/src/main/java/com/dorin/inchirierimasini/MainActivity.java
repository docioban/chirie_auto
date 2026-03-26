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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dorin.inchirierimasini.adapter.CarAdapter;
import com.dorin.inchirierimasini.db.DatabaseHelper;
import com.dorin.inchirierimasini.dialogs.ConfirmDeleteDialog;
import com.dorin.inchirierimasini.model.Car;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ConfirmDeleteDialog.OnDeleteConfirmedListener {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAddCar;
    private EditText etSearch;
    private TextView tvEmptyState;
    private View coordinatorLayout;
    private long pendingDeleteId = -1;
    private int pendingDeletePosition = -1;

    private final ActivityResultLauncher<Intent> addEditCarLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String action = result.getData().getStringExtra("action");
                    Car car = result.getData().getParcelableExtra("car");
                    if (car != null) {
                        if ("add".equals(action)) {
                            carAdapter.addCar(car);
                            recyclerView.smoothScrollToPosition(0);
                            updateEmptyState();
                            Snackbar.make(coordinatorLayout,
                                    getString(R.string.car_added_success), Snackbar.LENGTH_SHORT).show();
                        } else if ("edit".equals(action)) {
                            carAdapter.updateCar(car);
                            Toast.makeText(this, getString(R.string.car_updated_success), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> rentalsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Reload cars in case availability changed
                loadCarsFromDB();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply enter animation
        overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);

        dbHelper = new DatabaseHelper(this);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewCars);
        fabAddCar = findViewById(R.id.fabAddCar);
        etSearch = findViewById(R.id.etSearch);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        carList = new ArrayList<>();
        carAdapter = new CarAdapter(this, carList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carAdapter);

        carAdapter.setOnCarClickListener((car, position) -> {
            Intent intent = new Intent(MainActivity.this, CarDetailActivity.class);
            intent.putExtra("car", car);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
        });

        carAdapter.setOnCarLongClickListener((car, position) -> {
            pendingDeleteId = car.getId();
            pendingDeletePosition = position;
            ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(
                    getString(R.string.confirm_delete),
                    getString(R.string.delete_car_message, car.getBrand(), car.getModel()),
                    car.getId()
            );
            dialog.show(getSupportFragmentManager(), "confirmDeleteCar");
        });

        fabAddCar.setOnClickListener(v -> {
            // Button animation
            v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();

            Intent intent = new Intent(MainActivity.this, AddEditCarActivity.class);
            addEditCarLauncher.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
        });

        // Animate FAB on start
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        fabAddCar.startAnimation(bounce);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                carAdapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadCarsFromDB();

        // Animate whole layout on enter
        View mainContent = findViewById(R.id.mainContent);
        if (mainContent != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            mainContent.startAnimation(fadeIn);
        }
    }

    private void loadCarsFromDB() {
        carList = dbHelper.getAllCars();
        carAdapter.updateList(carList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (carAdapter.getItemCount() == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_view_rentals) {
            Intent intent = new Intent(this, RentalsActivity.class);
            rentalsLauncher.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
            return true;
        } else if (id == R.id.menu_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    public void onDeleteConfirmed(long itemId) {
        int rows = dbHelper.deleteCar(itemId);
        if (rows > 0 && pendingDeletePosition >= 0) {
            carAdapter.removeCar(pendingDeletePosition);
            updateEmptyState();
            Snackbar.make(coordinatorLayout, getString(R.string.car_deleted_success), Snackbar.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        loadCarsFromDB();
    }
}
