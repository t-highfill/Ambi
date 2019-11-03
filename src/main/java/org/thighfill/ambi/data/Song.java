package org.thighfill.ambi.data;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.AmbiContext;
import org.thighfill.ambi.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Song extends ZipStorable<Song.Bean> {

    private static final Logger LOGGER = Util.getLogger(Song.class);

    private String _filename, _id, _name, _artist;
    private ZipFile _zip;
    private File _tmpFile;

    protected Song(AmbiContext context, ZipTree zipTree, Bean bean) throws IOException {
        super(context);
        _filename = bean.filename;
        _id = bean.id;
        ZipTree.ZRegFile entry = zipTree.getRoot().followPath(bean.filename).asRegFile();
        _name = bean.name;
        _artist = bean.artist;
        String[] f = _filename.split("\\.");
        String format = f[f.length - 1];
        _tmpFile = Util.createTempFile(context, "ambisong", '.' + format);
        try (InputStream in = entry.getInputStream();
                OutputStream out = new FileOutputStream(_tmpFile)) {
            LOGGER.debug("Copying song to temp file {}", _tmpFile);
            IOUtils.copy(in, out);
        }
    }

    @Override
    protected Bean toBean() {
        Bean res = new Bean();
        res.filename = _filename;
        res.id = _id;
        return res;
    }

    public String getFilename() {
        return _filename;
    }

    public void setFilename(String filename) {
        _filename = filename;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public ZipFile getZip() {
        return _zip;
    }

    public void setZip(ZipFile zip) {
        _zip = zip;
    }

    public File getTmpFile() {
        return _tmpFile;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getArtist() {
        return _artist;
    }

    public void setArtist(String artist) {
        _artist = artist;
    }

    protected static class Bean {
        public String filename, id, name, artist;
    }
}
