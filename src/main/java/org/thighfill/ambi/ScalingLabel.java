package org.thighfill.ambi;

import org.thighfill.ambi.util.Util;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

public class ScalingLabel extends JLabel {

    private BufferedImage _master;

    public ScalingLabel() {
        this(null);
    }

    public ScalingLabel(BufferedImage img) {
        this(img, null);
    }

    public ScalingLabel(BufferedImage img, Dimension minSize){
        _master = img;
        setMinimumSize(minSize);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                rescale();
            }
        });
        rescale();
    }

    private void rescale() {
        int width = Math.max(getWidth(), getMinWidth()), height = Math.max(getHeight(), getMinHeight());
        if(_master != null) {
            setIcon(new ImageIcon(Util.resizeToFit(_master, width, height)));
        }else{
            setIcon(null);
        }
    }

    private int getMinWidth(){
        return (int) getMinimumSize().getWidth();
    }

    private int getMinHeight(){
        return (int) getMinimumSize().getHeight();
    }

    public BufferedImage getMaster() {
        return _master;
    }

    public void setMaster(BufferedImage master) {
        this._master = master;
        rescale();
    }
}
