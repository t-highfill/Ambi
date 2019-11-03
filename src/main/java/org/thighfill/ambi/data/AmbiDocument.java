package org.thighfill.ambi.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.thighfill.ambi.AmbiContext;
import org.thighfill.ambi.data.cache.Cache;
import org.thighfill.ambi.data.cache.RecencyCache;
import org.thighfill.ambi.util.Util;

import javax.imageio.ImageIO;
import javax.swing.ProgressMonitor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class AmbiDocument extends ZipStorable<AmbiDocument.Bean> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MAIN_FILE = "doc.json";

    private String _name, _author;
    private SongPack _songPack;
    private List<Page> pages;
    private boolean _twoPageMode, _rightToLeft, _firstPageAlone;
    private Thread _loadThread;
    private ProgressMonitor _monitor;
    private final Cache<File, BufferedImage> _imageCache;

    public AmbiDocument(AmbiContext context, ZipFile zip) throws IOException {
        this(context, zip, processZip(zip));
        new Thread(() -> {
            try {
                waitForLoad();
            } catch(InterruptedException e) {
                Util.handleError(getContext(), "Waiting for load", e);
            }
            try {
                zip.close();
            } catch(IOException e) {
                Util.handleError(getContext(), "Closing zip", e);
            }
        }).start();
    }

    protected AmbiDocument(AmbiContext context, ZipFile zip, Bean bean) throws IOException {
        super(context);
        _imageCache = new RecencyCache<>(imgFile -> {
            try (FileInputStream fis = new FileInputStream(imgFile)) {
                return ImageIO.read(fis);
            }
            catch (IOException e) {
                Util.handleError(context, "Loading image", e);
                return null;
            }
        }, 10);
        _name = bean.name;
        _author = bean.author;
        _songPack = bean.songPack == null ? null : new SongPack(context, zip, bean.songPack);
        _monitor = new ProgressMonitor(context.getAmbi(), "Loading pages", "", 0, bean.pages.size());
        pages = new ArrayList<>(bean.pages.size());
        _loadThread = new Thread(() -> {
            int idx = 0;
            _monitor.setProgress(idx);
            for(Page.Bean b : bean.pages){
                if(_monitor.isCanceled()){
                    break;
                }
                pages.add(Page.fromBean(this, zip, b));
                idx++;
                _monitor.setProgress(idx);
            }
        });
        _loadThread.start();
        _twoPageMode = bean.twoPageMode;
        _rightToLeft = bean.rightToLeft;
        _firstPageAlone = bean.firstPageAlone;
    }

    public boolean isLoaded(){
        return _loadThread.isAlive();
    }

    public boolean waitForLoad() throws InterruptedException {
        _loadThread.join();
        return ! _monitor.isCanceled();
    }

    @Override
    protected Bean toBean() {
        Bean res = new Bean();
        res.name = _name;
        res.author = _author;
        res.songPack = _songPack.toBean();
        res.pages = pages.stream().map(Page::toBean).collect(Collectors.toList());
        res.twoPageMode = _twoPageMode;
        return res;
    }

    private static Bean processZip(ZipFile zip) throws IOException {
        return MAPPER.readValue(zip.getInputStream(Util.getEntry(zip, MAIN_FILE)), Bean.class);
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getAuthor() {
        return _author;
    }

    public void setAuthor(String author) {
        this._author = author;
    }

    public SongPack getSongPack() {
        return _songPack;
    }

    public void setSongPack(SongPack songPack) {
        this._songPack = songPack;
    }

    public boolean isTwoPageMode() {
        return _twoPageMode;
    }

    public void setTwoPageMode(boolean twoPageMode) {
        this._twoPageMode = twoPageMode;
    }

    public boolean isRightToLeft() {
        return _rightToLeft;
    }

    public void setRightToLeft(boolean rightToLeft) {
        this._rightToLeft = rightToLeft;
    }

    public boolean isFirstPageAlone() {
        return _firstPageAlone;
    }

    public void setFirstPageAlone(boolean firstPageAlone) {
        this._firstPageAlone = firstPageAlone;
    }

    public Cache<File, BufferedImage> getImageCache() {
        return _imageCache;
    }

    protected static class Bean {
        public String name, author;
        public SongPack.Bean songPack;
        public List<Page.Bean> pages;
        public boolean twoPageMode, rightToLeft, firstPageAlone;
    }
}
