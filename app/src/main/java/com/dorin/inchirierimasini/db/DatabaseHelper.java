package com.dorin.inchirierimasini.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dorin.inchirierimasini.model.Car;
import com.dorin.inchirierimasini.model.Rental;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inchirieri_masini.db";
    private static final int DATABASE_VERSION = 1;

    // Cars table
    public static final String TABLE_CARS = "cars";
    public static final String COL_CAR_ID = "id";
    public static final String COL_BRAND = "brand";
    public static final String COL_MODEL = "model";
    public static final String COL_YEAR = "year";
    public static final String COL_PRICE_PER_DAY = "price_per_day";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IS_AVAILABLE = "is_available";
    public static final String COL_RATING = "rating";

    // Rentals table
    public static final String TABLE_RENTALS = "rentals";
    public static final String COL_RENTAL_ID = "id";
    public static final String COL_RENTAL_CAR_ID = "car_id";
    public static final String COL_CLIENT_NAME = "client_name";
    public static final String COL_CLIENT_PHONE = "client_phone";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";
    public static final String COL_TOTAL_PRICE = "total_price";
    public static final String COL_WITH_INSURANCE = "with_insurance";
    public static final String COL_HAS_CHILD_SEAT = "has_child_seat";
    public static final String COL_HAS_GPS = "has_gps";
    public static final String COL_STATUS = "status";

    private static final String CREATE_TABLE_CARS =
            "CREATE TABLE " + TABLE_CARS + " (" +
                    COL_CAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_BRAND + " TEXT NOT NULL, " +
                    COL_MODEL + " TEXT NOT NULL, " +
                    COL_YEAR + " INTEGER NOT NULL, " +
                    COL_PRICE_PER_DAY + " REAL NOT NULL, " +
                    COL_DESCRIPTION + " TEXT, " +
                    COL_IS_AVAILABLE + " INTEGER DEFAULT 1, " +
                    COL_RATING + " REAL DEFAULT 3.0" +
                    ")";

    private static final String CREATE_TABLE_RENTALS =
            "CREATE TABLE " + TABLE_RENTALS + " (" +
                    COL_RENTAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RENTAL_CAR_ID + " INTEGER NOT NULL, " +
                    COL_CLIENT_NAME + " TEXT NOT NULL, " +
                    COL_CLIENT_PHONE + " TEXT NOT NULL, " +
                    COL_START_DATE + " TEXT NOT NULL, " +
                    COL_END_DATE + " TEXT NOT NULL, " +
                    COL_TOTAL_PRICE + " REAL NOT NULL, " +
                    COL_WITH_INSURANCE + " INTEGER DEFAULT 0, " +
                    COL_HAS_CHILD_SEAT + " INTEGER DEFAULT 0, " +
                    COL_HAS_GPS + " INTEGER DEFAULT 0, " +
                    COL_STATUS + " TEXT DEFAULT 'active', " +
                    "FOREIGN KEY (" + COL_RENTAL_CAR_ID + ") REFERENCES " + TABLE_CARS + "(" + COL_CAR_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CARS);
        db.execSQL(CREATE_TABLE_RENTALS);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RENTALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARS);
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        insertCar(db, "Toyota", "Corolla", 2022, 45.0, "Masina economica, consum redus de carburant", true, 4.2f);
        insertCar(db, "BMW", "X5", 2023, 120.0, "SUV premium cu toate dotarile", true, 4.8f);
        insertCar(db, "Dacia", "Logan", 2021, 30.0, "Masina accesibila, ideala pentru oras", true, 3.9f);
        insertCar(db, "Mercedes", "C-Class", 2022, 150.0, "Sedan de lux cu interior elegant", true, 4.9f);
        insertCar(db, "Volkswagen", "Golf", 2023, 55.0, "Hatchback fiabil si spatios", true, 4.3f);
    }

    private void insertCar(SQLiteDatabase db, String brand, String model, int year,
                           double price, String description, boolean available, float rating) {
        ContentValues values = new ContentValues();
        values.put(COL_BRAND, brand);
        values.put(COL_MODEL, model);
        values.put(COL_YEAR, year);
        values.put(COL_PRICE_PER_DAY, price);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IS_AVAILABLE, available ? 1 : 0);
        values.put(COL_RATING, rating);
        db.insert(TABLE_CARS, null, values);
    }

    // ==================== CAR CRUD ====================

    public long addCar(Car car) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BRAND, car.getBrand());
        values.put(COL_MODEL, car.getModel());
        values.put(COL_YEAR, car.getYear());
        values.put(COL_PRICE_PER_DAY, car.getPricePerDay());
        values.put(COL_DESCRIPTION, car.getDescription());
        values.put(COL_IS_AVAILABLE, car.isAvailable() ? 1 : 0);
        values.put(COL_RATING, car.getRating());
        long id = db.insert(TABLE_CARS, null, values);
        db.close();
        return id;
    }

    public List<Car> getAllCars() {
        List<Car> carList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CARS, null, null, null, null, null,
                COL_BRAND + " ASC");
        if (cursor.moveToFirst()) {
            do {
                carList.add(cursorToCar(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return carList;
    }

    public Car getCarById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CARS, null,
                COL_CAR_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Car car = null;
        if (cursor.moveToFirst()) {
            car = cursorToCar(cursor);
        }
        cursor.close();
        db.close();
        return car;
    }

    public int updateCar(Car car) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BRAND, car.getBrand());
        values.put(COL_MODEL, car.getModel());
        values.put(COL_YEAR, car.getYear());
        values.put(COL_PRICE_PER_DAY, car.getPricePerDay());
        values.put(COL_DESCRIPTION, car.getDescription());
        values.put(COL_IS_AVAILABLE, car.isAvailable() ? 1 : 0);
        values.put(COL_RATING, car.getRating());
        int rows = db.update(TABLE_CARS, values,
                COL_CAR_ID + "=?", new String[]{String.valueOf(car.getId())});
        db.close();
        return rows;
    }

    public int deleteCar(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete associated rentals first
        db.delete(TABLE_RENTALS, COL_RENTAL_CAR_ID + "=?", new String[]{String.valueOf(id)});
        int rows = db.delete(TABLE_CARS, COL_CAR_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Car> searchCars(String query) {
        List<Car> carList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_BRAND + " LIKE ? OR " + COL_MODEL + " LIKE ? OR " +
                COL_DESCRIPTION + " LIKE ?";
        String[] selectionArgs = new String[]{
                "%" + query + "%", "%" + query + "%", "%" + query + "%"
        };
        Cursor cursor = db.query(TABLE_CARS, null, selection, selectionArgs,
                null, null, COL_BRAND + " ASC");
        if (cursor.moveToFirst()) {
            do {
                carList.add(cursorToCar(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return carList;
    }

    public List<Car> getAvailableCars() {
        List<Car> carList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CARS, null,
                COL_IS_AVAILABLE + "=1", null, null, null, COL_BRAND + " ASC");
        if (cursor.moveToFirst()) {
            do {
                carList.add(cursorToCar(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return carList;
    }

    private Car cursorToCar(Cursor cursor) {
        Car car = new Car();
        car.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CAR_ID)));
        car.setBrand(cursor.getString(cursor.getColumnIndexOrThrow(COL_BRAND)));
        car.setModel(cursor.getString(cursor.getColumnIndexOrThrow(COL_MODEL)));
        car.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(COL_YEAR)));
        car.setPricePerDay(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE_PER_DAY)));
        car.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        car.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_AVAILABLE)) == 1);
        car.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_RATING)));
        return car;
    }

    // ==================== RENTAL CRUD ====================

    public long addRental(Rental rental) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RENTAL_CAR_ID, rental.getCarId());
        values.put(COL_CLIENT_NAME, rental.getClientName());
        values.put(COL_CLIENT_PHONE, rental.getClientPhone());
        values.put(COL_START_DATE, rental.getStartDate());
        values.put(COL_END_DATE, rental.getEndDate());
        values.put(COL_TOTAL_PRICE, rental.getTotalPrice());
        values.put(COL_WITH_INSURANCE, rental.isWithInsurance() ? 1 : 0);
        values.put(COL_HAS_CHILD_SEAT, rental.isHasChildSeat() ? 1 : 0);
        values.put(COL_HAS_GPS, rental.isHasGPS() ? 1 : 0);
        values.put(COL_STATUS, rental.getStatus());
        long id = db.insert(TABLE_RENTALS, null, values);
        db.close();
        return id;
    }

    public List<Rental> getAllRentals() {
        List<Rental> rentalList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RENTALS, null, null, null, null, null,
                COL_RENTAL_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                rentalList.add(cursorToRental(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return rentalList;
    }

    public Rental getRentalById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RENTALS, null,
                COL_RENTAL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Rental rental = null;
        if (cursor.moveToFirst()) {
            rental = cursorToRental(cursor);
        }
        cursor.close();
        db.close();
        return rental;
    }

    public List<Rental> getRentalsByCarId(long carId) {
        List<Rental> rentalList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RENTALS, null,
                COL_RENTAL_CAR_ID + "=?", new String[]{String.valueOf(carId)},
                null, null, COL_RENTAL_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                rentalList.add(cursorToRental(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return rentalList;
    }

    public int updateRental(Rental rental) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CLIENT_NAME, rental.getClientName());
        values.put(COL_CLIENT_PHONE, rental.getClientPhone());
        values.put(COL_START_DATE, rental.getStartDate());
        values.put(COL_END_DATE, rental.getEndDate());
        values.put(COL_TOTAL_PRICE, rental.getTotalPrice());
        values.put(COL_WITH_INSURANCE, rental.isWithInsurance() ? 1 : 0);
        values.put(COL_HAS_CHILD_SEAT, rental.isHasChildSeat() ? 1 : 0);
        values.put(COL_HAS_GPS, rental.isHasGPS() ? 1 : 0);
        values.put(COL_STATUS, rental.getStatus());
        int rows = db.update(TABLE_RENTALS, values,
                COL_RENTAL_ID + "=?", new String[]{String.valueOf(rental.getId())});
        db.close();
        return rows;
    }

    public int deleteRental(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_RENTALS, COL_RENTAL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Rental> searchRentals(String query) {
        List<Rental> rentalList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_CLIENT_NAME + " LIKE ? OR " + COL_CLIENT_PHONE + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};
        Cursor cursor = db.query(TABLE_RENTALS, null, selection, selectionArgs,
                null, null, COL_RENTAL_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                rentalList.add(cursorToRental(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return rentalList;
    }

    private Rental cursorToRental(Cursor cursor) {
        Rental rental = new Rental();
        rental.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RENTAL_ID)));
        rental.setCarId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RENTAL_CAR_ID)));
        rental.setClientName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CLIENT_NAME)));
        rental.setClientPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_CLIENT_PHONE)));
        rental.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_START_DATE)));
        rental.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_END_DATE)));
        rental.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL_PRICE)));
        rental.setWithInsurance(cursor.getInt(cursor.getColumnIndexOrThrow(COL_WITH_INSURANCE)) == 1);
        rental.setHasChildSeat(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HAS_CHILD_SEAT)) == 1);
        rental.setHasGPS(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HAS_GPS)) == 1);
        rental.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        return rental;
    }

    public int getCarCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CARS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getRentalCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RENTALS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}
