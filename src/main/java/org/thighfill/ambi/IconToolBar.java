package org.thighfill.ambi;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import java.net.URL;

public class IconToolBar extends JToolBar {

    public static final int ICON_SIZE = 24;
    public static final String GENERAL = "general";
    public static final String MEDIA = "media";
    public static final String NAVIGATION = "navigation";

    public IconToolBar() {
    }

    public IconToolBar(String name) {
        super(name);
    }

    protected static ImageIcon loadIcon(String directory, String name){
        String resource = String.format("/toolbarButtonGraphics/%s/%s%d.gif", directory, name, ICON_SIZE);
        URL imgUrl = AmbiToolBar.class.getResource(resource);
        return new ImageIcon(imgUrl);
    }
}
