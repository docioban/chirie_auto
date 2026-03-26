package com.dorin.inchirierimasini.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Rental implements Parcelable {

    private long id;
    private long carId;
    private String clientName;
    private String clientPhone;
    private String startDate;
    private String endDate;
    private double totalPrice;
    private boolean withInsurance;
    private boolean hasChildSeat;
    private boolean hasGPS;
    private String status;

    public Rental() {
    }

    public Rental(long id, long carId, String clientName, String clientPhone,
                  String startDate, String endDate, double totalPrice,
                  boolean withInsurance, boolean hasChildSeat, boolean hasGPS, String status) {
        this.id = id;
        this.carId = carId;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.withInsurance = withInsurance;
        this.hasChildSeat = hasChildSeat;
        this.hasGPS = hasGPS;
        this.status = status;
    }

    protected Rental(Parcel in) {
        id = in.readLong();
        carId = in.readLong();
        clientName = in.readString();
        clientPhone = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        totalPrice = in.readDouble();
        withInsurance = in.readByte() != 0;
        hasChildSeat = in.readByte() != 0;
        hasGPS = in.readByte() != 0;
        status = in.readString();
    }

    public static final Creator<Rental> CREATOR = new Creator<Rental>() {
        @Override
        public Rental createFromParcel(Parcel in) {
            return new Rental(in);
        }

        @Override
        public Rental[] newArray(int size) {
            return new Rental[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(carId);
        dest.writeString(clientName);
        dest.writeString(clientPhone);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeDouble(totalPrice);
        dest.writeByte((byte) (withInsurance ? 1 : 0));
        dest.writeByte((byte) (hasChildSeat ? 1 : 0));
        dest.writeByte((byte) (hasGPS ? 1 : 0));
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCarId() { return carId; }
    public void setCarId(long carId) { this.carId = carId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public boolean isWithInsurance() { return withInsurance; }
    public void setWithInsurance(boolean withInsurance) { this.withInsurance = withInsurance; }

    public boolean isHasChildSeat() { return hasChildSeat; }
    public void setHasChildSeat(boolean hasChildSeat) { this.hasChildSeat = hasChildSeat; }

    public boolean isHasGPS() { return hasGPS; }
    public void setHasGPS(boolean hasGPS) { this.hasGPS = hasGPS; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
