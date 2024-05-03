package com.example.bletest;
import android.os.ParcelUuid;

public class BLEData {
    String address;
    String name;
    ParcelUuid[] uuids;
    int rssi;

    public BLEData( String address, String name, ParcelUuid[] uuids, int rssi){
        this.address = address;
        this.name = name;
        this.uuids = uuids;
        this.rssi = rssi;
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
}
