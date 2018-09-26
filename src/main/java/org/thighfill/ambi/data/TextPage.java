package org.thighfill.ambi.data;

import org.thighfill.ambi.Ambi;
import org.thighfill.ambi.ScalingLabel;
import org.thighfill.ambi.util.Util;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class TextPage extends Page {

    private static final BufferedImage DOC_ICON = getDocIcon();

    private String _text;

    protected TextPage(AmbiDocument document, Bean bean) {
        super(document, bean);
        _text = bean.text;
    }

    @Override
    public void addToLabel(ScalingLabel label) {
        label.setText(_text);
        label.setMaster(null);
    }

    @Override
    public BufferedImage getIcon() {
        return DOC_ICON;
    }

    @Override
    protected Bean toBean() {
        Bean res = super.toBean();
        res.text = _text;
        return res;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    private static BufferedImage getDocIcon(){
        try {
            return ImageIO.read(Page.class.getResource(Ambi.IMGS_DIR+"/doc-icon.png"));
        } catch(IOException e) {
            Util.handleError(null, "Loading doc icon", e);
            return null;
        }
    }
}
