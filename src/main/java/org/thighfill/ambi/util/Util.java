package org.thighfill.ambi.util;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.AmbiContext;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class Util {

    private static final Logger LOGGER = getLogger(Util.class);

    private Util() {
    }

    public static Logger getLogger(Class<?> cls) {
        return LogManager.getLogger(cls);
    }

    public static String readAll(InputStream in, String charset) throws IOException {
        return IOUtils.toString(in, charset);
    }

    public static void busyCursorWhile(AmbiContext context, Runnable runner){
        context.getAmbi().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        runner.run();
        context.getAmbi().setCursor(Cursor.getDefaultCursor());
    }

    public static BufferedImage resizeToFit(BufferedImage image, int width, int height){
        int newWidth, newHeight;
        if(width < height){
            newWidth = width;
            newHeight = image.getHeight()*newWidth/image.getWidth();
        }else{
            newHeight = height;
            newWidth = image.getWidth()*newHeight/image.getHeight();
        }
        return resize(image, newWidth, newHeight);
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = bi.createGraphics();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return bi;
    }

    public static ZipEntry getEntry(ZipFile zip, String filename) throws IOException {
        LOGGER.debug("Searching {} for {}", zip, filename);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        int idx = 0;
        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            idx++;
            //System.out.println("Entry " + idx + ": \"" + entry.getName() + '"');
            if(entry.getName().endsWith(filename)) {
                return entry;
            }
        }
        throw new IllegalArgumentException("Could not find file " + filename + " in zip");
    }

    public static File createTempFile(AmbiContext context, String prefix, String postfix) throws IOException {
        File tmp = File.createTempFile(prefix, postfix);
        tmp.deleteOnExit();
        return tmp;
    }

    public static void handleError(AmbiContext context, String message, Exception e) {
        LOGGER.error(message, e);
    }

    public static void handleError(AmbiContext context, Exception e) {
        handleError(context, "Unhandled exception", e);
    }
}
