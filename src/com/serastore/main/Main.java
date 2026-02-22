package com.serastore.main;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.serastore.views.SplashScreenView;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SplashScreenView splash = new SplashScreenView();
        splash.runSplash();
    }
}