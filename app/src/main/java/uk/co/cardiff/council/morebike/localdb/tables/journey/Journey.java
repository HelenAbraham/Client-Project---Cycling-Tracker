package uk.co.cardiff.council.morebike.localdb.tables.journey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import uk.co.cardiff.council.morebike.Bike;
import com.google.gson.*;

@Entity
public class Journey {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    @ColumnInfo(name = "start_address")
    private String startAddress;

    @ColumnInfo(name = "end_address")
    private String endAddress;

    @ColumnInfo(name = "start_time")
    private Date startTime;

    @ColumnInfo(name = "end_time")
    private Date endTime;

    @ColumnInfo(name = "kilometers_travelled")
    private double kilometersTravelled;

    @ColumnInfo(name = "is_sent")
    private boolean sent;

    @Ignore
    private String startDate;

    @Ignore
    private String endDate;

    public Journey(String startAddress, String endAddress, Date startTime, Date endTime, double kilometersTravelled, boolean sent) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.startTime = startTime;
        setEndTime(endTime);
        this.kilometersTravelled = kilometersTravelled;
        this.sent = sent;
    }

    @Ignore
    public Journey() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        setStartDate(startTime);
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.startDate = formatter.format(startDate);
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.endDate = formatter.format(endDate);
    }

    public void setEndTime(Date endTime) {
        if (startTime == null)
            throw new IllegalStateException("Cannot set journey end time. Start time is not yet defined.");
        if(endTime.before(startTime))
            throw  new IllegalArgumentException("Journey end time must be after the journey start time.");
        if(endTime == null)
            throw new IllegalArgumentException("Journey end time must be defined.");
        setEndDate(endTime);
        this.endTime = endTime;
    }

    public double calculateEmissionsSaved() {
        return new Bike(kilometersTravelled).getEmissions();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.UK, "%d: %s - %s | Time: %s - %s | Kms: %.2f", getId(),
                getStartAddress(), getEndAddress(), getStartDate(), getEndDate(), getKilometersTravelled());
    }
    public double getKilometersTravelled() {
        return kilometersTravelled;
    }

    public void setKilometersTravelled(double kilometersTravelled) {
        this.kilometersTravelled = kilometersTravelled;
    }

    public String toJson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        return gson.toJson(this);
    }

    public static String toJSON(Journey journey) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        return gson.toJson(journey);
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
