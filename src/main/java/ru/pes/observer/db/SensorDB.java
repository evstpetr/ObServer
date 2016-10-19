package ru.pes.observer.db;

/**
 * Created by Admin on 14.06.2016.
 */
public class SensorDB {
    private int sensor_type;
    private int sensor_state;
    private int sensor_count;
    private String sensor_l_time;

    public SensorDB(int sensor_type, int sensor_state, int sensor_count, String sensor_l_time) {
        this.sensor_type = sensor_type;
        this.sensor_state = sensor_state;
        this.sensor_count = sensor_count;
        this.sensor_l_time = sensor_l_time;
    }

    public int getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(int sensor_type) {
        this.sensor_type = sensor_type;
    }

    public int getSensor_state() {
        return sensor_state;
    }

    public void setSensor_state(int sensor_state) {
        this.sensor_state = sensor_state;
    }

    public int getSensor_count() {
        return sensor_count;
    }

    public void setSensor_count(int sensor_count) {
        this.sensor_count = sensor_count;
    }

    public String getSensor_l_time() {
        return sensor_l_time;
    }

    public void setSensor_l_time(String sensor_l_time) {
        this.sensor_l_time = sensor_l_time;
    }
}
