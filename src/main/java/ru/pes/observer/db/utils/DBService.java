package ru.pes.observer.db.utils;

import org.apache.log4j.Logger;
import ru.pes.observer.db.DBConnector;
import ru.pes.observer.db.Observer;
import ru.pes.observer.db.SensorDB;

import java.util.HashMap;
import java.util.Map;


public class DBService {
    private static final Logger logger = Logger.getLogger(DBService.class);

    public static final String OBSERVER_STRING = "observer";
    public static final String SENSOR_STRING = "sensor";
    public static final String USER_STRING = "user";
    public static final String TYPE_STRING = "type";
    public static final String STATE_STRING = "state";
    public static final String NONE_STRING = "none";
    //errors
    public static final int ERR_MESS_EXIST_LOGIN = -1;

    public static void SaveToDB(Observer observer) {
        DBConnector.InsertObserver(observer);
        int observer_id = DBConnector.getDBObjectId(observer.getAddress(), OBSERVER_STRING);
        for (Map.Entry<String, SensorDB> map : observer.getSensors().entrySet()) {
            DBConnector.InsertSensor(map.getKey(), map.getValue(), observer_id);
            DBConnector.InsertSensorProperty(map.getValue().getSensor_type(), TYPE_STRING);
            DBConnector.InsertSensorProperty(map.getValue().getSensor_state(), STATE_STRING);
            DBConnector.InsertName(map.getKey());
        }
    }

    public static int recordUser(String name, int password) {
        int result = 0;
        if (DBConnector.getDBObjectId(name, USER_STRING) == -1) {
            result = DBConnector.InsertUser(name, password);
        } else {
            return ERR_MESS_EXIST_LOGIN;
        }
        return result;
    }

    public static int checkUser(String name, int password) {
        int id;
        id = DBConnector.getDBObjectId(name, USER_STRING);
        if (id > 0) {
            return DBConnector.getPassword(id) == password ? 1 : 0;
        }
        return -1;
    }
}
