package ru.pes.observer;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by Admin on 07.06.2016.
 */
public class App {
    public static void main(String[] args) {
        new Thread(new Server(8081)).start();
        JFrame frame = new JFrame();
        frame.setTitle("Server");
        frame.setSize(300, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
