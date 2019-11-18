package org.thighfill.ambi.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class AmbiDocument extends ZipStorable<AmbiDocument.Bean> implements AutoCloseable {

    private static final Logger LOGGER = Util.getLogger(AmbiDocument.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MAIN_FILE = "doc.json";
    private static final int CACHE_SIZE = 10;
    private static final float PDF_DPI = 72f;

    private String _name, _author, _pdfFile;
    private SongPack _songPack;
    private List<Page> pages;
    private boolean _twoPageMode, _rightToLeft, _firstPageAlone;
    private Thread _loadThread;
    private ProgressMonitor _monitor;

    private PDDocument _pdf;
    private PDFRenderer _renderer;

    private final Cache<File, BufferedImage> _imageCache = new RecencyCache<>(imgFile -> {
        try (FileInputStream fis = new FileInputStream(imgFile)) {
            return ImageIO.read(fis);
        }
        catch (IOException e) {
            Util.handleError(getContext(), "Loading image", e);
            return null;
        }
    }, CACHE_SIZE);
    private final Cache<Integer, BufferedImage> _pdfCache = new RecencyCache<>(idx -> {
        try {
            return _renderer.renderImageWithDPI(idx, PDF_DPI);
        }
        catch (IOException e) {
            Util.handleError(getContext(), "Loading page from pdf", e);
            return null;
        }
    }, 2);

    public AmbiDocument(AmbiContext context, ZipFile zip) throws IOException {
        this(context, new ZipTree(zip));
        new Thread(() -> {
            try {
                waitForLoad();
            }
            catch (InterruptedException e) {
                Util.handleError(getContext(), "Waiting for load", e);
            }
            try {
                zip.close();
            }
            catch (IOException e) {
                Util.handleError(getContext(), "Closing zip", e);
            }
        }).start();
    }

    protected AmbiDocument(AmbiContext context, ZipTree zipTree) throws IOException {
        super(context);
        ZipTree.ZRegFile mainFile = findMainFile(zipTree);
        Bean bean = processZip(mainFile);
        ZipTree newRoot = zipTree.chroot(mainFile.getParent());
        _name = bean.name;
        _author = bean.author;
        _pdfFile = bean.pdfFile;
        _monitor = new ProgressMonitor(context.getAmbi(), "Loading pages", "", 0, bean.pages.size());
        _songPack = bean.songPack == null ? null : new SongPack(context, newRoot, bean.songPack);
        pages = new ArrayList<>(bean.pages.size());
        _loadThread = new Thread(() -> {
            int idx = 0;
            _monitor.setProgress(idx);
            loadPDF(newRoot);
            for (Page.Bean b : bean.pages) {
                if (_monitor.isCanceled()) {
                    break;
                }
                pages.add(Page.fromBean(this, newRoot, b, idx));
                idx++;
                _monitor.setProgress(idx);
            }
        });
        _loadThread.start();
        _twoPageMode = bean.twoPageMode;
        _rightToLeft = bean.rightToLeft;
        _firstPageAlone = bean.firstPageAlone;
    }

    public boolean hasPDF() {
        return _pdfFile != null;
    }

    private void loadPDF(ZipTree zipTree) {
        if (_pdfFile == null) {
            return;
        }
        try (InputStream in = zipTree.getRoot().followPath(_pdfFile).asRegFile().getInputStream()) {
            LOGGER.info("Loading PDF...");

            MemoryUsageSetting memSettings = MemoryUsageSetting.setupTempFileOnly().setTempDir(
                    getContext().getTempDirectory());
            _pdf = PDDocument.load(in, memSettings);
            LOGGER.info("PDF loaded");
            _renderer = new PDFRenderer(_pdf);
        }
        catch (IOException e) {
            Util.handleError(getContext(), "Loading pdf", e);
        }
    }

    public boolean isLoaded() {
        return _loadThread.isAlive();
    }

    public boolean waitForLoad() throws InterruptedException {
        _loadThread.join();
        return !_monitor.isCanceled();
    }

    @Override
    protected Bean toBean() {
        Bean res = new Bean();
        res.name = _name;
        res.author = _author;
        res.pdfFile = _pdfFile;
        res.songPack = _songPack.toBean();
        res.pages = pages.stream().map(Page::toBean).collect(Collectors.toList());
        res.twoPageMode = _twoPageMode;
        return res;
    }

    private static ZipTree.ZRegFile findMainFile(ZipTree zipTree) {
        return zipTree.getRoot().find(MAIN_FILE).asRegFile();
    }

    private static Bean processZip(ZipTree.ZRegFile mainFile) throws IOException {
        return MAPPER.readValue(mainFile.getInputStream(), Bean.class);
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

    public Cache<Integer, BufferedImage> getPdfCache() {
        return _pdfCache;
    }

    @Override
    public void close() throws Exception {
        if (_pdf != null) {
            _pdf.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    protected static class Bean {
        public String name, author, pdfFile;
        public SongPack.Bean songPack;
        public List<Page.Bean> pages;
        public boolean twoPageMode, rightToLeft, firstPageAlone;
    }
}
