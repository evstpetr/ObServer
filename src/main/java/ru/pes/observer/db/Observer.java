package ru.pes.observer.db;

import java.util.HashMap;

/**
 * Created by Admin on 14.06.2016.
 */
public class Observer {
    private String address;
    HashMap<String, SensorDB> sensors;

    public Observer(String address, HashMap<String, SensorDB> sensors) {
        this.address = address;
        this.sensors = sensors;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public HashMap<String, SensorDB> getSensors() {
        return sensors;
    }

    public void setSensors(HashMap<String, SensorDB> sensors) {
        this.sensors = sensors;
    }
}
