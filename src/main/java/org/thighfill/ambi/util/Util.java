package org.thighfill.ambi.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.AmbiContext;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Static utility class for various needs
 *
 * @author Tobias Highfill
 */
public final class Util {

    private static final Logger LOGGER = getLogger(Util.class);

    private Util() {
    }

    /**
     * Gets the logger for the given class
     *
     * @param cls The class to log
     * @return The logger for that class
     */
    public static Logger getLogger(Class<?> cls) {
        return LogManager.getLogger(cls);
    }

    /**
     * Sets the user's cursor to "Busy" while the Runnable is running and then returns to default
     *
     * @param context The current Ambi context
     * @param runner The runner to run
     */
    public static void busyCursorWhile(AmbiContext context, Runnable runner) {
        context.getAmbi().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        runner.run();
        context.getAmbi().setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Resize an image to fill the given bounds without exceeding them
     *
     * @param image The image to scale
     * @param width The maximum width
     * @param height The maximum height
     * @return The resized image
     */
    public static BufferedImage resizeToFit(BufferedImage image, int width, int height) {
        int newWidth, newHeight;
        // Try scaling by width
        newWidth = width;
        newHeight = image.getHeight() * newWidth / image.getWidth();
        // Check whether we've exceeded our bounds
        if (newHeight > height) {
            // Scale by height instead
            newHeight = height;
            newWidth = image.getWidth() * newHeight / image.getHeight();
        }
        return resize(image, newWidth, newHeight);
    }

    /**
     * Resize an image to an arbitrary size. Will distort if aspect ratio is not maintained
     *
     * @param image The image to resize
     * @param width The new width
     * @param height The new height
     * @return The resized image
     */
    public static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = bi.createGraphics();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return bi;
    }

    /**
     * Creates a new temporary file and marks it for deletion on exit. Files are created within a special folder
     * maintained by the context
     *
     * @param context The current Ambi context
     * @param prefix The beginning of the filename
     * @param postfix The end of the filename
     * @return The temporary file
     * @throws IOException
     */
    public static File createTempFile(AmbiContext context, String prefix, String postfix) throws IOException {
        File tmp = File.createTempFile(prefix, postfix, context.getTempDirectory());
        tmp.deleteOnExit();
        return tmp;
    }

    /**
     * Logs error messages. Can be updated later to do more with the error
     * @param context The current Ambi context
     * @param message An error specific message
     * @param e The exception that occurred
     */
    public static void handleError(AmbiContext context, String message, Exception e) {
        LOGGER.error(message, e);
    }

    /**
     * Gets a filename's file extension
     * @param fileName The filename to break up
     * @return The filename's extension
     */
    public static String fileExt(String fileName) {
        String[] arr = fileName.split("\\.");
        return arr[arr.length - 1];
    }
}
