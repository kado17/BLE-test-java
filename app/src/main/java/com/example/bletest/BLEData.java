package com.example.bletest;
import android.os.ParcelUuid;
import java.time.LocalTime;
public class BLEData {
    String address;
    String name;
    ParcelUuid[] uuids;
    int rssi;
    LocalTime localTime;

    public BLEData( String address, String name, ParcelUuid[] uuids, int rssi, LocalTime localTime){
        this.address = address;
        this.name = name;
        this.uuids = uuids;
        this.rssi = rssi;
        this.localTime = localTime;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public ParcelUuid[] getUuids() {
        return uuids;
    }

    public int getRssi() {
        return rssi;
    }
    public LocalTime getLocalTime() {
        return localTime;
    }
}
