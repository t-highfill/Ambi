package org.thighfill.ambi;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.data.AmbiDocument;
import org.thighfill.ambi.data.Page;
import org.thighfill.ambi.util.Util;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.text.Style;
import javax.swing.text.html.StyleSheet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;

/**
 * The main class. Extends JFrame and encapsulates the entire application
 *
 * @author Tobias Highfill
 */
public class Ambi extends JFrame {

    private static final Logger LOGGER = Util.getLogger(Ambi.class);

    public static final String DATA_DIR = "/data";
    public static final String IMGS_DIR = DATA_DIR + "/imgs";
    public static final String SETTINGS_DIR = DATA_DIR + "/settings";

    private final AmbiContext context;
    private AmbiMenuBar menuBar;
    private NowPlayingBar nowPlaying;
    private AmbiToolBar toolBar;
    private JPanel mainPanel = new JPanel();
    private PreviewPanel previewPanel;
    private JPanel readerPanel = new JPanel();

    private JPanel docViewPanel = new JPanel();
    private ScalingLabel leftPage = new ScalingLabel();
    private ScalingLabel rightPage = new ScalingLabel();

    private JScrollPane previewScrollPane;

    private JSplitPane _splitPane;

    private PageViewer _viewer = new PageViewer(leftPage, rightPage);
    private AmbiDocument _currentDoc = null;

    /**
     * Builds an Ambi application window
     */
    public Ambi() {
        super("Ambi - The Musical Book Reader");
        // The order of initialization here is VERY IMPORTANT. A lot of inter-related dependencies
        context = new SimpleAmbiContext(this);
        previewPanel = new PreviewPanel(this);
        toolBar = new AmbiToolBar(context);
        menuBar = new AmbiMenuBar(context);
        nowPlaying = new NowPlayingBar(context);

        Configuration conf = context.getConfiguration();
        Configuration.AmbiFrameConfig ambiFrameConfig = conf.getAmbiFrame();

        previewScrollPane = new JScrollPane(previewPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        previewScrollPane.setPreferredSize(new Dimension(conf.getPreviewPanel().getWidth(), 100));

        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewScrollPane, readerPanel);

        this.setPreferredSize(ambiFrameConfig.getDimensions());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                exit();
            }
        });
        // Update the config's settings constantly to reflect user changes
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                ambiFrameConfig.setWidth(getWidth());
                ambiFrameConfig.setHeight(getHeight());
            }
        });

        // Make everything visible
        Stream.of(menuBar, toolBar, mainPanel, previewPanel, readerPanel, nowPlaying, docViewPanel, previewScrollPane,
                leftPage, rightPage, _splitPane).forEach(c -> c.setVisible(true));
        this.add(mainPanel);
        this.setJMenuBar(menuBar);
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(_splitPane, BorderLayout.CENTER);

        readerPanel.setLayout(new BorderLayout());
        readerPanel.add(nowPlaying, BorderLayout.SOUTH);
        readerPanel.add(docViewPanel, BorderLayout.CENTER);

        docViewPanel.setLayout(new GridLayout(1, 2));
        docViewPanel.add(leftPage);
        leftPage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goLeft();
            }
        });
        docViewPanel.add(rightPage);
        rightPage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goRight();
            }
        });
        docViewPanel.setMinimumSize(new Dimension(0, 0));

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (_currentDoc == null) {
                    return;
                }
                switch (keyEvent.getKeyCode()) {
                case KeyEvent.VK_KP_RIGHT:
                case KeyEvent.VK_RIGHT:
                    goRight();
                    break;
                case KeyEvent.VK_KP_LEFT:
                case KeyEvent.VK_LEFT:
                    goLeft();
                    break;
                case KeyEvent.VK_KP_DOWN:
                case KeyEvent.VK_DOWN:
                    nextPage();
                    break;
                case KeyEvent.VK_KP_UP:
                case KeyEvent.VK_UP:
                    prevPage();
                    break;
                }
            }
        });

        setDocument(null);
    }

    public void setPage(int idx) {
        _viewer.setPage(idx);
        previewPanel.setSelected(idx);
        nowPlaying.setPage(idx);
    }

    public void setDocument(AmbiDocument doc) {
        _currentDoc = doc;
        _viewer.setDocument(doc);
        nowPlaying.setDocument(doc);
        previewPanel.setDocument(null);
        if (doc != null) {
            new Thread(() -> {
                try {
                    doc.waitForLoad();
                    LOGGER.debug("Doc loaded, setting preview...");
                    previewPanel.setDocument(doc);
                    LOGGER.debug("Preview set, setting page...");
                    setPage(0);
                }
                catch (InterruptedException e) {
                    Util.handleError(context, "Loading pages", e);
                    close();
                }
            }).start();
        }
    }

    public void close() {
        setDocument(null);
    }

    public void goLeft() {
        if (_currentDoc == null) {
            return;
        }
        if (_currentDoc.isRightToLeft()) {
            nextPage();
        }
        else {
            prevPage();
        }
    }

    public void goRight() {
        if (_currentDoc == null) {
            return;
        }
        if (_currentDoc.isRightToLeft()) {
            prevPage();
        }
        else {
            nextPage();
        }
    }

    public void nextPage() {
        setPage(_viewer.nextPage());
    }

    public void prevPage() {
        setPage(_viewer.prevPage());
    }

    public void open() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        switch (res) {
        case JFileChooser.APPROVE_OPTION:
            File f = fc.getSelectedFile();
            open(f);
            break;
        case JFileChooser.CANCEL_OPTION:
            LOGGER.info("User cancelled open");
            break;
        default:
            LOGGER.error("Unknown return value from file chooser: {}", res);
        }
    }

    public void open(File f) {
        context.getConfiguration().addFileToHistory(f);
        LOGGER.info("Opening doc: {}", f);
        Util.busyCursorWhile(context, () -> {
            try {
                context.openDocument(f);
            }
            catch (IOException e) {
                Util.handleError(context, "Opening doc", e);
            }
        });
    }

    public void exit() {
        LOGGER.info("Exiting normally");
        Util.busyCursorWhile(context, () -> {
            try {
                context.saveConfiguration();
            }
            catch (IOException e) {
                Util.handleError(context, "Saving config", e);
            }
        });
        System.exit(0);
    }

    public JScrollPane getPreviewScrollPane() {
        return previewScrollPane;
    }

    public AmbiContext getContext() {
        return context;
    }

    public AmbiMenuBar getAmbiMenuBar() {
        return menuBar;
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JPanel getPreviewPanel() {
        return previewPanel;
    }

    public JPanel getReaderPanel() {
        return readerPanel;
    }

    public NowPlayingBar getNowPlaying() {
        return nowPlaying;
    }

    public AmbiDocument getCurrentDoc() {
        return _currentDoc;
    }

    private static StyleSheet loadStyles(String resource) {
        URL url = Page.class.getResource(resource);
        LOGGER.debug("Loading CSS from: {}", url);
        StyleSheet res = new StyleSheet();
        res.importStyleSheet(url);
        return res;
    }

    private static void printStyle(Style s) {
        Enumeration<?> names = s.getAttributeNames();
        System.out.println("Style: " + s.getName());
        while (names.hasMoreElements()) {
            Object obj = names.nextElement();
            System.out.println('\t' + obj.toString() + ": " + s.getAttribute(obj));
        }
    }

    private static void printStyleSheet(StyleSheet sheet) {
        Enumeration<?> names = sheet.getStyleNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            printStyle(sheet.getStyle(name.toString()));
        }
        System.out.println("===END===");
        StyleSheet[] sheets = sheet.getStyleSheets();
        if (sheets != null) {
            Stream.of(sheets).forEach(Ambi::printStyleSheet);
        }
    }

    public static void main(String[] args)
            throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException,
            IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Ambi ambi = new Ambi();
        ambi.pack();
        ambi.setVisible(true);
        //Attempt to maximize
        ambi.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}
