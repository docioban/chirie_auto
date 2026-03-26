package com.dorin.inchirierimasini.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Car implements Parcelable {

    private long id;
    private String brand;
    private String model;
    private int year;
    private double pricePerDay;
    private String description;
    private boolean isAvailable;
    private float rating;

    public Car() {
    }

    public Car(long id, String brand, String model, int year, double pricePerDay,
               String description, boolean isAvailable, float rating) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.description = description;
        this.isAvailable = isAvailable;
        this.rating = rating;
    }

    protected Car(Parcel in) {
        id = in.readLong();
        brand = in.readString();
        model = in.readString();
        year = in.readInt();
        pricePerDay = in.readDouble();
        description = in.readString();
        isAvailable = in.readByte() != 0;
        rating = in.readFloat();
    }

    public static final Creator<Car> CREATOR = new Creator<Car>() {
        @Override
        public Car createFromParcel(Parcel in) {
            return new Car(in);
        }

        @Override
        public Car[] newArray(int size) {
            return new Car[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(brand);
        dest.writeString(model);
        dest.writeInt(year);
        dest.writeDouble(pricePerDay);
        dest.writeString(description);
        dest.writeByte((byte) (isAvailable ? 1 : 0));
        dest.writeFloat(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    @Override
    public String toString() {
        return brand + " " + model + " (" + year + ")";
    }
}
