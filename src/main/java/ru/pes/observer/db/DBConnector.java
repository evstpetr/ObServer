package ru.pes.observer.db;

import org.apache.log4j.Logger;
import ru.pes.observer.db.utils.DBService;

import java.sql.*;


public class DBConnector {
    private static final Logger logger = Logger.getLogger(DBConnector.class);
    private static Connection conn;
    private static PreparedStatement pstmt;
    private static final String WRITE_OBSERVER_SQL = "INSERT INTO hubs (ADDRESS) VALUES (?)";
    private static final String WRITE_SENSOR_SQL = "INSERT INTO sensors (SENSOR_TYPE_ID, SENSOR_STATE_ID, SENSOR_DESCRIPTION_ID, SENSOR_VALUE, SENSOR_L_TIME, HUB_ID) VALUES (?,?,?,?,?,?)";
    private static final String WRITE_USER_SQL = "INSERT INTO users (USERNAME, PASSWORD) VALUES (?, ?)";
    private static final String ID_STRING = "id";
    private static final String PASSWORD_STRING = "password";
    private static final String DB_ERROR_MESSAGE = "DB error...";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/observer";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    public static void InsertObserver(Observer observer) {
        try {
            if (getDBObjectId(observer.getAddress(), DBService.OBSERVER_STRING) == -1) { // True - адреса в базе еще нет
                conn = getConn();
                pstmt = conn.prepareStatement(WRITE_OBSERVER_SQL);
                pstmt.setString(1, observer.getAddress());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
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
            pstmt.setString(5, sensorDB.getSensor_l_time());
            pstmt.setInt(6, observerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
        } finally {
            closeConnection();
        }

    }

    public static int InsertUser(String name, int password) {
        int res = 0;
        try {
            conn = getConn();
            pstmt = conn.prepareStatement(WRITE_USER_SQL);
            pstmt.setString(1, name);
            pstmt.setInt(2, password);
            res = pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
        } finally {
            closeConnection();
        }

        return res;
    }

    public static void InsertSensorProperty(int sensorProperty, String propertyName) {
        try {
            if (getSensorPropertyId(sensorProperty, propertyName) == -1) {
                conn = getConn();
                if (propertyName.equalsIgnoreCase(DBService.TYPE_STRING)) {
                    pstmt = conn.prepareStatement("INSERT INTO sensor_type (TYPE_ID) VALUES (?)");
                } else if (propertyName.equalsIgnoreCase(DBService.STATE_STRING)){
                    pstmt = conn.prepareStatement("INSERT INTO sensor_state (STATE_ID) VALUES (?)");
                }
                pstmt.setInt(1, sensorProperty);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
        } finally {
            closeConnection();
        }
    }

    public static void InsertName(String sensorId) {
        try {
            if (getDBObjectId(sensorId, DBService.SENSOR_STRING) == -1) {
                conn = getConn();
                pstmt = conn.prepareStatement("INSERT INTO sensor_description (DESCRIPTION_ID) VALUES (?)");
                pstmt.setString(1, sensorId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
        } finally {
            closeConnection();
        }
    }

    public static int getSensorPropertyId(int propertyId, String propertyName) {
        int res = -1;
        try {
            conn = getConn();
            if (propertyName.equalsIgnoreCase(DBService.TYPE_STRING)) {
                pstmt = conn.prepareStatement("SELECT id FROM sensor_type where TYPE_ID=?");
            } else if (propertyName.equalsIgnoreCase(DBService.STATE_STRING)){
                pstmt = conn.prepareStatement("SELECT id FROM sensor_state where STATE_ID=?");
            }
            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                res = rs.getInt(ID_STRING);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
        } finally {
            closeConnection();
        }
        return res;
    }

    public static int getDBObjectId(String objectProperty, String objectName) {
        int res = -1;
        try {
            conn = getConn();
            if (objectName.equalsIgnoreCase(DBService.SENSOR_STRING)) {
                pstmt = conn.prepareStatement("SELECT id FROM sensor_description where DESCRIPTION_ID=?");
            } else if (objectName.equalsIgnoreCase(DBService.OBSERVER_STRING)){
                pstmt = conn.prepareStatement("SELECT id FROM hubs where ADDRESS=?");
            } else if (objectName.equalsIgnoreCase(DBService.USER_STRING)) {
                pstmt = conn.prepareStatement("SELECT id FROM users where USERNAME=?");
            }
            pstmt.setString(1, objectProperty);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                res = rs.getInt(ID_STRING);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
        } finally {
            closeConnection();
        }
        return res;
    }

    public static int getPassword(int id) {
        int result = 0;
        try {
            conn = getConn();
            pstmt = conn.prepareStatement("SELECT password FROM users where id=?");
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getInt(PASSWORD_STRING);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            logger.error(DB_ERROR_MESSAGE, e);
        } finally {
            closeConnection();
        }
        return result;
    }

    private static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException se) {
                logger.error(DB_ERROR_MESSAGE, se);
            }
        }
    }

    private static Connection getConn() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        return connection;
    }

}
