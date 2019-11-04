package org.thighfill.ambi;

import javax.swing.JButton;

import java.awt.event.ActionListener;

public class AmbiToolBar extends IconToolBar {

    public AmbiToolBar(AmbiContext context){
        Ambi ambi = context.getAmbi();
        addButton(GENERAL, "Open", e -> ambi.open());
        addButton(NAVIGATION, "Back", "Left", e -> ambi.goLeft());
        addButton(NAVIGATION, "Forward", "Right", e -> ambi.goRight());
    }

    private void addButton(String directory, String name, ActionListener listener){
        addButton(directory, name, name, listener);
    }

    private void addButton(String directory, String name, String tooltip, ActionListener listener){
        JButton button = new JButton(loadIcon(directory, name));
        button.setToolTipText(tooltip);
        button.setVisible(true);
        button.addActionListener(listener);
        button.setFocusable(false);
        add(button);
    }
}
