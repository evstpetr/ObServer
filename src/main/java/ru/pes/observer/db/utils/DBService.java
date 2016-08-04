package ru.pes.observer.db.utils;

import org.apache.log4j.Logger;
import ru.pes.observer.db.DBConnector;
import ru.pes.observer.db.Observer;
import ru.pes.observer.db.SensorDB;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 14.06.2016.
 */
public class DBService {
    private static final Logger logger = Logger.getLogger(DBService.class);

    public static void SaveToDB(Observer observer) {
        DBConnector.InsertObserver(observer);
        int observer_id = DBConnector.getDBObjectId(observer.getAddress(), "observer");
        for (Map.Entry<String, SensorDB> map : observer.getSensors().entrySet()) {
            DBConnector.InsertSensor(map.getKey(), map.getValue(), observer_id);
            DBConnector.InsertSensorProperty(map.getValue().getSensor_type(), "type");
            DBConnector.InsertSensorProperty(map.getValue().getSensor_state(), "state");
            DBConnector.InsertName(map.getKey());
        }
    }
}
