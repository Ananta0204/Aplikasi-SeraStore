package com.serastore.views;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SplashScreenView extends JWindow {
    private int progressValue = 0;
    private String statusText = "Initializing...";

    public SplashScreenView() {
        setSize(800, 450);
        setLocationRelativeTo(null);
        
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));

        JPanel content = new JPanel(null) { 
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(0, 0, new Color(100, 0, 0), 0, getHeight(), new Color(30, 0, 0));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2d.setPaint(new RadialGradientPaint(getWidth()/2, getHeight()/2, 400, 
                    new float[]{0f, 1f}, new Color[]{new Color(150, 0, 0, 50), new Color(0, 0, 0, 0)}));
                g2d.fillOval(getWidth()/2-300, getHeight()/2-200, 600, 400);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 90));
                String title = "SERA STORE";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(title)) / 2;
                int y = (getHeight() / 2) + 30;

                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(title, x + 4, y + 4);
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(title, x, y);

                int pbWidth = 500;
                int pbHeight = 4;
                int pbX = (getWidth() - pbWidth) / 2;
                int pbY = getHeight() - 80;

                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillRoundRect(pbX, pbY, pbWidth, pbHeight, 2, 2);

                g2d.setColor(new Color(255, 215, 0)); 
                int currentWidth = (int) (pbWidth * (progressValue / 100.0));
                g2d.fillRoundRect(pbX, pbY, currentWidth, pbHeight, 2, 2);

                g2d.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                g2d.setColor(new Color(255, 215, 0, 180));
                g2d.drawString(statusText, pbX, pbY - 10);
            }
        };

        setContentPane(content);
    }

    public void runSplash() {
        setVisible(true);
        String[] steps = {
            "Booting Sera System...", 
            "Optimizing Graphics...", 
            "Verifying Database...", 
            "Almost there..."
        };

        try {
            for (int i = 0; i <= 100; i++) {
                progressValue = i;
                
                if (i == 10) statusText = steps[0];
                if (i == 40) statusText = steps[1];
                if (i == 70) statusText = steps[2];
                if (i == 90) statusText = steps[3];

                repaint(); 
                Thread.sleep(30); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        dispose();
        EventQueue.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}