package org.thighfill.ambi.data;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.ScalingLabel;
import org.thighfill.ambi.util.Util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ImagePage extends Page {

    private static final Logger LOGGER = Util.getLogger(ImagePage.class);

    private String _imgFile;
    private File _tmpFile;

    protected ImagePage(AmbiDocument doc, ZipFile zip, Bean bean) {
        super(doc, bean);
        _imgFile = bean.imgFile;
        if (_imgFile == null) {
            throw new IllegalArgumentException("Image file cannot be null");
        }
        try {
            _tmpFile = Util.createTempFile(getContext(), "ambiImgPg", '.' + Util.fileExt(_imgFile));
            ZipEntry ent = Util.getEntry(zip, _imgFile);
            try (FileOutputStream out = new FileOutputStream(_tmpFile)) {
                IOUtils.copy(zip.getInputStream(ent), out);
            }
        }
        catch (IOException e) {
            Util.handleError(getContext(), "Loading Image", e);
        }
    }

    @Override
    public BufferedImage getIcon() {
        return getDoc().getImageCache().get(_tmpFile);
    }

    @Override
    public void addToLabel(ScalingLabel label) {
        label.setText("");
        label.setMaster(getIcon());
    }

    @Override
    protected Bean toBean() {
        Bean res = super.toBean();
        res.imgFile = _imgFile;
        return res;
    }
}
