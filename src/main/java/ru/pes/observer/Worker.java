package ru.pes.observer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import ru.pes.observer.db.*;
import ru.pes.observer.db.Observer;
import ru.pes.observer.db.utils.DBService;
import ru.pes.observer.object.Decoder;
import ru.pes.observer.object.Message;
import ru.pes.observer.object.Sensor;
import ru.pes.observer.object.User;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Admin on 08.06.2016.
 */
public class Worker implements Runnable {
    private final Socket CLIENT;
    private boolean run = true;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private Gson gson;
    private ArrayList<Object> aList;
    private final String OK = "ok";
    private final String ERR = "err";
    private HashMap<String, SensorDB> sensors = new HashMap<>();
    private Observer observer;
    private static final Logger logger = Logger.getLogger(Worker.class);
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public Worker(Socket client) {
        CLIENT = client;
    }

    @Override
    public void run() {
        String line = "";
        gson = new Gson();
        try {
            in = new BufferedReader(new InputStreamReader(CLIENT.getInputStream()));
            out = new PrintWriter(CLIENT.getOutputStream(), true);
        } catch (IOException e) {
            logger.error("Can't create I/O stream...", e);
        }

        while (run) {
            try {
                line = in.readLine();
                System.out.println(line);
                String mess = readMessage(line); // Пытаемся прочитать входящее сообщение
                if (!mess.equalsIgnoreCase(ERR)) {
                    run = false; //Если удалось прочитать сообщение останавливаем поток
                    out.println("OK");
                    break;
                } else {
                    out.println("ERR");
                    break;
                }

            } catch (IOException e) {
                logger.error(line);
                logger.error("Can't read message", e);
            }

        }

        out.close();
        try {
            if (in != null) {
                in.close();
            }
            if (CLIENT != null) {
                CLIENT.close();
            }
        } catch (IOException e) {
            logger.error("Can't close I/O...", e);
        }
    }

    /**
     *
     * @param strJson
     * @return прочитана строка или нет
     */
    private String readMessage(String strJson) {
        System.out.println(strJson.getBytes().length);
        int hash;
        Sensor sensor;
        try {
            Message message = gson.fromJson(strJson, Message.class);// Получаем объект Sensora из JSON объекта
            if (message.getType().toString().equalsIgnoreCase("SEN")) {
                sensor = gson.fromJson(message.getData(), Sensor.class);
                System.out.println(sensor);
                hash = sensor.getAddress().hashCode() + sensor.getData().hashCode() + sensor.getDate().hashCode();// Проверяем что полученны корректные данные
                if (hash == sensor.getHash()) {
                    for (String s : sensor.getData()) {
                        if (s != null) {
                            decodeMsg(s, sensor.getDate()); // Расшифровываем входящее сообщение
                        }
                    }
                    observer = new Observer(sensor.getAddress(), sensors);
                    DBService.SaveToDB(observer);
                    System.out.println(observer);
                    return OK;
                } else {
                    System.out.println(hash);
                    System.out.println(sensor.getHash());
                    System.out.println(ERR);
                    return ERR;
                }
            } else if (message.getType().toString().equalsIgnoreCase("SUP")) {
                User user = gson.fromJson(message.getData(), User.class);
                return DBService.recordUser(user.getName(), user.getPassword()) > 0 ? OK : ERR;
            } else if (message.getType().toString().equalsIgnoreCase("SIN")) {
                User user = gson.fromJson(message.getData(), User.class);
                return DBService.checkUser(user.getName(), user.getPassword()) > 0 ? OK : ERR;
            }
        } catch (Exception e) {
            logger.error("Can't read: " + strJson, e);
            return ERR;
        }
        return ERR;
    }

    /**
     * @param str  - Зашифрованная Hex строка
     * @param date
     * @return расшифрованный массив байт
     * @throws DecoderException
     * @throws ParseException
     */
    private byte[] decodeMsg(String str, String date) throws DecoderException, ParseException {

        byte[] all, part = new byte[16];
        all = Hex.decodeHex(str.toCharArray());
        // Расшифровка
        System.arraycopy(all, 0, part, 0, 16);
        System.arraycopy(Decoder.decrypt(part, 1), 0, all, 0, 16);
        System.arraycopy(all, 10, part, 0, 16);
        System.arraycopy(Decoder.decrypt(part, 2), 0, all, 10, 16);
        System.arraycopy(all, 6, part, 0, 16);
        System.arraycopy(Decoder.decrypt(part, 2), 0, all, 6, 16);
        // Чтение
        char[] string;
        String type = "", state = "", id = "", count = "", ltime = "", ftime = "";
        string = bytesToHex(all).toCharArray();
        // Тип
        for (int i = 0; i < 2; i++) {
            type = type + string[i];
        }
        // Состояние
        for (int i = 2; i < 4; i++) {
            state = state + string[i];
        }
        // Идентификатор
        for (int i = 4; i < 12; i++) {
            id = id + string[i];
        }
        // Счетчик/Показатель
        for (int i = 12; i < 14; i++) {
            count = count + string[i];
        }
        // Время последнего срабатывания
        for (int i = 14; i < 18; i++) {
            ltime = ltime + string[i];
        }
        // SensorDB - объект для записи в БД
        SensorDB sensorDB = new SensorDB(Integer.parseInt(type, 16), Integer.parseInt(state, 16), Integer.parseInt(count, 16), getCorrectDate(ltime, date));
        sensors.put(id, sensorDB);
        System.out.println("id: " + id + "; state: " + Integer.parseInt(state, 16) + "; count: " + Integer.parseInt(count, 16) + "; last time: " + getCorrectDate(ltime, date));
        return all;
    }

    /**
     * @param strDelta - Колличество секунд
     * @param strDate  - Дата
     * @return
     * @throws ParseException
     */
    private String getCorrectDate(String strDelta, String strDate) throws ParseException {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(strDate);
        calendar.setTime(date);
        int delta = Integer.parseInt(strDelta, 16);
        calendar.add(calendar.SECOND, -delta);
        return sdf.format(calendar.getTime());
    }

    /**
     * Функция преобразования массива байт в хекс строку
     *
     * @param bytes
     * @return HexString
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
