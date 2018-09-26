package org.thighfill.ambi.data;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.ScalingLabel;
import org.thighfill.ambi.util.Util;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ImagePage extends Page {

    private static final Logger LOGGER = Util.getLogger(ImagePage.class);

    private String _imgFile;
    private BufferedImage _img = null;

    protected ImagePage(AmbiDocument doc, ZipFile zip, Bean bean) {
        super(doc, bean);
        _imgFile = bean.imgFile;
        if(_imgFile == null) {
            throw new IllegalArgumentException("Image file cannot be null");
        }
        try {
            ZipEntry ent = Util.getEntry(zip, _imgFile);
            _img = ent == null ? null : ImageIO.read(zip.getInputStream(ent));
        } catch(IOException e) {
            Util.handleError(getContext(), "Loading Image", e);
            _img = null;
        }
    }

    @Override
    public BufferedImage getIcon() {
        return _img;
    }

    @Override
    public void addToLabel(ScalingLabel label) {
        label.setText("");
        label.setMaster(_img);
    }

    @Override
    protected Bean toBean() {
        Bean res = super.toBean();
        res.imgFile = _imgFile;
        return res;
    }
}
