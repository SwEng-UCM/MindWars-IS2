package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingOptionCard extends JPanel {

    private final String title;
    private final String description;
    private boolean selected;
    private final JCheckBox toggle;

     public SettingOptionCard(String title, String description, boolean initialState) {
        this.title = title;
        this.description = description;
        this.selected = initialState;
    }
    
}