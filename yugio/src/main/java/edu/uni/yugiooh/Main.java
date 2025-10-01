package edu.uni.yugiooh;

import javax.swing.SwingUtilities;

import edu.uni.yugiooh.ui.DuelFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DuelFrame frame = new DuelFrame();
            frame.setVisible(true);
        });
    }
}