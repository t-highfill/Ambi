package org.thighfill.ambi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.thighfill.ambi.event.Event;
import org.thighfill.ambi.event.EventDriver;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Configuration {

    private static final int HISTORY_LIMIT = 10;

    public static Configuration fromJSON(InputStream in) throws IOException {
        return new ObjectMapper().readValue(in, Configuration.class);
    }

    private List<String> songPackPaths = new LinkedList<>();
    private List<String> fileHistory = new LinkedList<>();

    public static class HistoryEvent{
        private final List<String> _newHist;

        private HistoryEvent(List<String> newHist) {
            _newHist = newHist;
        }

        public List<String> getNewHist() {
            return _newHist;
        }
    }
    @JsonIgnore
    private final EventDriver<HistoryEvent> _historyDriver = new EventDriver<>();
    @JsonIgnore
    public final Event<HistoryEvent> historyUpdated = _historyDriver.getMyEvent();

    private AmbiFrameConfig ambiFrame;
    private PreviewPanelConfig previewPanel;
    private ToolbarConfig toolbar;

    public List<String> getFileHistory() {
        return new LinkedList<>(fileHistory);
    }

    public void addFileToHistory(File file){
        String fpath = file.getAbsolutePath();
        fileHistory.remove(fpath);
        fileHistory.add(0, fpath);
        while(fileHistory.size() > HISTORY_LIMIT){
            fileHistory.remove(fileHistory.size()-1);
        }
        _historyDriver.fire(new HistoryEvent(fileHistory));
    }

    public void clearHistory(){
        fileHistory = new LinkedList<>();
    }

    public AmbiFrameConfig getAmbiFrame() {
        return ambiFrame;
    }

    public PreviewPanelConfig getPreviewPanel() {
        return previewPanel;
    }

    public ToolbarConfig getToolbar() {
        return toolbar;
    }

    public List<String> getSongPackPaths() {
        return songPackPaths;
    }

    public void setSongPackPaths(List<String> songPackPaths) {
        this.songPackPaths = songPackPaths;
    }

    public static class AmbiFrameConfig {
        private int width, height;

        public Dimension getDimensions() {
            return new Dimension(width, height);
        }

        public void setDimensions(Dimension dim) {
            width = (int) dim.getWidth();
            height = (int) dim.getHeight();
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public static class PreviewPanelConfig {
        private int width, iconWidth;
        private boolean visible;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getIconWidth() {
            return iconWidth;
        }

        public void setIconWidth(int iconWidth) {
            this.iconWidth = iconWidth;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }

    public static class ToolbarConfig {
        private boolean visible;

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
}
