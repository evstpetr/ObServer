package ru.pes.observer.object;

import java.util.List;

/**
 * Created by Admin on 08.06.2016.
 */
public class Sensor{
    private String address;
    private List<String> data;
    private String date;
    private int hash;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
