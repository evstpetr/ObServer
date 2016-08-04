package ru.pes.observer;

import com.google.gson.Gson;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import ru.pes.observer.db.*;
import ru.pes.observer.db.Observer;
import ru.pes.observer.db.utils.DBService;
import ru.pes.observer.object.Decoder;
import ru.pes.observer.object.Sensor;

import java.io.*;
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
                String mess = readMessage(line); // Пытаемся прочитать входящее сообщение
                if (mess != ERR) {
                    run = false; //Если удалось прочитать сообщение останавливаем поток
                    break;
                } else {
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
            sensor = gson.fromJson(strJson, Sensor.class);// Получаем объект Sensora из JSON объекта
            hash = sensor.getAddress().hashCode() + sensor.getData().hashCode() + sensor.getDate().hashCode();// Проверяем что полученны корректные данные
            if (hash == sensor.getHash()) {
                out.println(OK); // Если хеш суммы совпадают отправляем на клиент ответ об удачной доставке
                for (String s : sensor.getData()) {
                    decodeMsg(s, sensor.getDate()); // Расшифровываем входящее сообщение
                }
                return OK;
            } else {
                System.out.println(hash);
                System.out.println(sensor.getHash());
                System.out.println(ERR);
                out.println(ERR); // Если хеш не совпадает запрашиваем у клиента еще 1 отправку
                return ERR;
            }
        } catch (Exception e) {
            logger.error("Can't read: " + strJson, e);
            out.println(ERR);
            return ERR;
        }
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
        // Время первого срабатывания
        for (int i = 18; i < 22; i++) {
            ftime = ftime + string[i];
        }
        // SensorDB - объект для записи в БД
        SensorDB sensorDB = new SensorDB(Integer.parseInt(type, 16), Integer.parseInt(state, 16), Integer.parseInt(count, 16), getCorrectDate(ftime, date), getCorrectDate(ltime, date));
        sensors.put(id, sensorDB);
        System.out.println("id: " + id + "; state: " + Integer.parseInt(state, 16) + "; count: " + Integer.parseInt(count, 16) + "; last time: " + getCorrectDate(ltime, date) + "; first time: " + getCorrectDate(ftime, date));
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
