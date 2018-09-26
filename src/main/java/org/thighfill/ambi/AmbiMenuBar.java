package org.thighfill.ambi;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.util.Util;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class AmbiMenuBar extends JMenuBar {

    private static final Logger LOGGER = Util.getLogger(AmbiMenuBar.class);

    private static final Map<Character, Integer> KEY_MAP = initKeyMap();

    private final AmbiContext context;
    private JMenu file = new JMenu("File");
    private JMenu edit = new JMenu("Edit");
    private JMenu view = new JMenu("View");
    private JMenu help = new JMenu("Help");

    public AmbiMenuBar(AmbiContext context) {
        this.context = context;
        JMenu[] topLevel = {file, edit, view, help};
        autoMnemonic(topLevel);
        Stream.of(topLevel).forEach(m -> {
            m.setVisible(true);
            add(m);
        });

        addButton(file, "Open", KeyStroke.getKeyStroke('O', InputEvent.CTRL_MASK), e -> open());
        addButton(file, "Exit", e -> exit());
        autoMnemonic(file);

        visibilityToggle(context.getAmbi().getPreviewPanel(), view, "Previews",
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.ALT_MASK));
        visibilityToggle(context.getAmbi().getToolBar(), view, "Toolbar",
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.ALT_MASK));
        view.addSeparator();
        addButton(view, "Previous Page", e->prev());
        addButton(view, "Next Page", e->next());
        autoMnemonic(view);

        addButton(help, "About", e -> about());
        autoMnemonic(help);
    }

    private void prev() {
        context.getAmbi().prevPage();
    }

    private void next() {
        context.getAmbi().nextPage();
    }

    private void about() {
        AboutDialog about = new AboutDialog(context);
        about.pack();
        about.setVisible(true);
    }

    private void exit() {
        this.context.exit();
    }

    private void open() {
        context.getAmbi().open();
    }

    private static <E extends JMenuItem> E initItem(E item, JMenu parent, String text, KeyStroke keyStroke,
                                                    ActionListener listener) {
        item.setText(text);
        item.setVisible(true);
        if(keyStroke != null) {
            item.setAccelerator(keyStroke);
        }
        item.addActionListener(listener);
        item.addActionListener(e -> LOGGER.debug("Menu item \"" + item.getText() + "\" action triggered"));
        parent.add(item);
        return item;
    }

    private static JCheckBoxMenuItem visibilityToggle(JComponent target, JMenu parent, String text, KeyStroke
            keyStroke) {
        return initItem(new JCheckBoxMenuItem("", target.isVisible()), parent, text, keyStroke,
                e -> target.setVisible(((JCheckBoxMenuItem) e.getSource()).getState()));
    }

    private static JCheckBoxMenuItem addCheckBox(JMenu parent, String text, KeyStroke keyStroke, ActionListener
            listener) {
        return initItem(new JCheckBoxMenuItem(), parent, text, keyStroke, listener);
    }

    private static JMenuItem addButton(JMenu parent, String text, KeyStroke keyStroke, ActionListener listener) {
        return initItem(new JMenuItem(), parent, text, keyStroke, listener);
    }

    private static JMenuItem addButton(JMenu parent, String text, ActionListener listener) {
        return addButton(parent, text, null, listener);
    }

    private static void autoMnemonic(JMenu parent) {
        JMenuItem[] items = new JMenuItem[parent.getItemCount()];
        for(int i = 0; i < items.length; i++) {
            items[i] = parent.getItem(i);
        }
        autoMnemonic(items);
    }

    private static void autoMnemonic(JMenuItem... menus) {
        Set<Integer> used = new HashSet<>();
        for(JMenuItem item : menus) {
            if(item == null){
                continue;
            }
            for(char c : item.getText().toCharArray()) {
                Integer key = KEY_MAP.get(c);
                if(key != null && !used.contains(key)) {
                    item.setMnemonic(key);
                    used.add(key);
                    break;
                }
            }
        }
    }

    private static Map<Character, Integer> initKeyMap() {
        Map<Character, Integer> res = new HashMap<>();
        int[] keys = {KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E, KeyEvent.VK_F,
                KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M,
                KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R, KeyEvent.VK_S, KeyEvent.VK_T,
                KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W, KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_Z};
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        String ALPHABET = alphabet.toUpperCase();
        for(int i = 0; i < keys.length; i++) {
            res.put(alphabet.charAt(i), keys[i]);
            res.put(ALPHABET.charAt(i), keys[i]);
        }
        return Collections.unmodifiableMap(res);
    }
}
