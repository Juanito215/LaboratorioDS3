package edu.uni.ygoduellite;

import edu.uni.ygoduellite.ui.DuelFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DuelFrame frame = new DuelFrame();
            frame.setVisible(true);
        });
    }
}