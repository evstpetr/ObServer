package ru.pes.observer.db;

import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Created by Admin on 14.06.2016.
 */
public class DBConnector {
    private static final Logger logger = Logger.getLogger(DBConnector.class);
    private static Connection conn;
    private static PreparedStatement pstmt;
    private static final String WRITE_OBSERVER_SQL = "INSERT INTO observer (ADDRESS) VALUES (?)";
    private static final String WRITE_SENSOR_SQL = "INSERT INTO sensor (SENSOR_TYPE_ID, SENSOR_STATE_ID, SENSOR_NAME_ID, SENSOR_VALUE, SENSOR_F_TIME, SENSOR_L_TIME, OBSERVER_ID) VALUES (?,?,?,?,?,?,?)";

    public static void InsertObserver(Observer observer) {
        try {
            if (getDBObjectId(observer.getAddress(), "observer") == -1) { // True - адреса в базе еще нет
                conn = getConn();
                pstmt = conn.prepareStatement(WRITE_OBSERVER_SQL);
                pstmt.setString(1, observer.getAddress());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("DB error...", e);
        } finally {
            closeConnection();
        }
    }

    public static void InsertSensor(String sensorId, SensorDB sensorDB, int observerId) {
        try {
            conn = getConn();
            pstmt = conn.prepareStatement(WRITE_SENSOR_SQL);
            pstmt.setInt(1, sensorDB.getSensor_type());
            pstmt.setInt(2, sensorDB.getSensor_state());
            pstmt.setString(3, sensorId);
            pstmt.setInt(4, sensorDB.getSensor_count());
            pstmt.setString(5, sensorDB.getSensor_f_time());
            pstmt.setString(6, sensorDB.getSensor_l_time());
            pstmt.setInt(7, observerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("DB error...", e);
        } finally {
            closeConnection();
        }

    }

    public static void InsertSensorProperty(int sensorProperty, String propertyName) {
        try {
            if (getSensorPropertyId(sensorProperty, propertyName) == -1) {
                conn = getConn();
                if (propertyName.equalsIgnoreCase("type")) {
                    pstmt = conn.prepareStatement("INSERT INTO sensor_type (TYPE_ID) VALUES (?)");
                } else {
                    pstmt = conn.prepareStatement("INSERT INTO sensor_state (STATE_ID) VALUES (?)");
                }
                pstmt.setInt(1, sensorProperty);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            logger.error("DB error...", e);
        } finally {
            closeConnection();
        }
    }

    public static void InsertName(String sensorId) {
        try {
            if (getDBObjectId(sensorId, "sensor") == -1) {
                conn = getConn();
                pstmt = conn.prepareStatement("INSERT INTO sensor_name (NAME_ID) VALUES (?)");
                pstmt.setString(1, sensorId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("DB error...", e);
        } finally {
            closeConnection();
        }
    }

    public static int getSensorPropertyId(int propertyId, String propertyName) {
        int res = -1;
        try {
            conn = getConn();
            if (propertyName.equalsIgnoreCase("type")) {
                pstmt = conn.prepareStatement("SELECT id FROM sensor_type where TYPE_ID=?");
            } else {
                pstmt = conn.prepareStatement("SELECT id FROM sensor_state where STATE_ID=?");
            }
            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                res = rs.getInt("id");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("DB error...", e);
        } finally {
            closeConnection();
        }
        return res;
    }

    public static int getDBObjectId(String objectProperty, String objectName) {
        int res = -1;
        try {
            conn = getConn();
            if (objectName.equalsIgnoreCase("sensor")) {
                pstmt = conn.prepareStatement("SELECT id FROM sensor_name where NAME_ID=?");
            } else {
                pstmt = conn.prepareStatement("SELECT id FROM observer where ADDRESS=?");
            }
            pstmt.setString(1, objectProperty);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                res = rs.getInt("id");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error("DB error...", e);
        } finally {
            closeConnection();
        }
        return res;
    }

    private static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException se) {
                logger.error("Неудалось закрыть соединение с БД ", se);
            }
        }
    }

    private static Connection getConn() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/observer";
        String username = "root";
        String password = "";
        Connection connection = DriverManager.getConnection(url, username, password);
        return connection;
    }

}
