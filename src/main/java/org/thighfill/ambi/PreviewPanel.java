package org.thighfill.ambi;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.data.AmbiDocument;
import org.thighfill.ambi.data.Page;
import org.thighfill.ambi.data.cache.Cache;
import org.thighfill.ambi.data.cache.RecencyCache;
import org.thighfill.ambi.util.Util;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class PreviewPanel extends JPanel {

    private static final Logger LOGGER = Util.getLogger(PreviewPanel.class);

    private static final Color SELECTION_COLOR = Color.ORANGE;

    private List<PagePreview> pages = new LinkedList<>();

    private AmbiDocument currDoc = null;
    private PagePreview currPage = null;
    private final Ambi _ambi;
    private final Configuration.PreviewPanelConfig config;
    private final Cache<Integer, ImageIcon> imgCache;

    public PreviewPanel(Ambi ambi) {
        this._ambi = ambi;
        config = ambi.getContext().getConfiguration().getPreviewPanel();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        imgCache = new RecencyCache<>(i-> new ImageIcon(scaleWidth(currDoc.getPages().get(i).getIcon(), config.getIconWidth())), 20);
    }

    public void setDocument(AmbiDocument doc) {
        if(currDoc == doc) {
            return;
        }
        LOGGER.debug("Setting document {}", doc);
        JScrollPane parent = _ambi.getPreviewScrollPane();
        boolean thisVis = isVisible(), pVis = parent.isVisible();
        currDoc = doc;
        setVisible(false);
        setSelected(null);
        setVisible(false);
        parent.setVisible(false);
        pages.forEach(this::remove);
        pages = new LinkedList<>();
        if(doc != null) {
            int idx = 1;
            for(Page p : doc.getPages()){
                PagePreview pp = new PagePreview(idx++, p);
                add(pp);
                pages.add(pp);
            }
        }
        setVisible(thisVis);
        parent.setVisible(pVis);
        parent.revalidate();
    }

    public void setSelected(int pageNum) {
        setSelected(pages.get(pageNum));
    }

    private void setSelected(PagePreview page) {
        if(currPage != null) {
            currPage.setBackground(currPage.normalBGColor);
        }
        currPage = page;
        if(page != null) {
            page.setBackground(SELECTION_COLOR);
        }
    }

    private static Image scaleWidth(BufferedImage img, int newWidth) {
        return Util.resize(img, newWidth, (img.getHeight() * newWidth) / img.getWidth());
    }

    private class PagePreview extends JPanel {

        final Color normalBGColor;
        final JLabel imgLbl;
        final JLabel idxLbl;

        private PagePreview(int pageNum, Page page) {
            LOGGER.debug("Loading preview for page {}", pageNum);
            normalBGColor = getBackground();
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setMaximumSize(null);
            setMinimumSize(new Dimension(100, 100));

            imgLbl = new JLabel();
            imgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(imgLbl);

            idxLbl = new JLabel(String.valueOf(pageNum));
            idxLbl.setHorizontalTextPosition(JLabel.CENTER);
            idxLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(idxLbl);

            MouseListener listener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    _ambi.setPage(pageNum - 1);
                }
            };
            Stream.of(this, idxLbl, imgLbl).forEach(c -> {
                c.setVisible(true);
                c.addMouseListener(listener);
            });
        }

    }

}
