package org.thighfill.ambi.data;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.ScalingLabel;
import org.thighfill.ambi.util.Util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public abstract class Page extends ZipStorable<Page.Bean> {

    private static final Logger LOGGER = Util.getLogger(Page.class);

    private Type _type;
    private Clip _clip;
    private List<Theme> _themes;
    private AmbiDocument _doc;

    protected Page(AmbiDocument doc, Bean bean) {
        super(doc.getContext());
        _doc = doc;
        _type = valueOfIgnoreCase(bean.type);
        SongPack pack = doc.getSongPack();
        if (pack != null) {
            _clip = pack.getClips().stream().filter(clip -> clip.getName().equals(bean.clipName)).findFirst().orElse(
                    null);
        }
        _themes = bean.themes.stream().map(Theme::fromJSONName).collect(Collectors.toList());
    }

    public abstract void addToLabel(ScalingLabel label);

    @Override
    protected Bean toBean() {
        Bean res = new Bean();
        res.type = _type.name();
        res.clipName = _clip == null ? null : _clip.getName();
        return res;
    }

    public abstract BufferedImage getIcon();

    public Type getType() {
        return _type;
    }

    public void setType(Type type) {
        _type = type;
    }

    private static Type valueOfIgnoreCase(String type) {
        return Type.valueOf(type.toUpperCase());
    }

    protected static Page fromBean(AmbiDocument document, ZipFile zip, Bean bean) {
        Type t = valueOfIgnoreCase(bean.type);
        switch (t) {
        case IMAGE:
            return new ImagePage(document, zip, bean);
        case TEXT:
            return new TextPage(document, bean);
        default:
            throw new IllegalArgumentException("Unknown Type: " + t);
        }
    }

    public Clip getClip() {
        return _clip;
    }

    public void setClip(Clip clip) {
        _clip = clip;
    }

    public List<Theme> getThemes() {
        return _themes;
    }

    public void setThemes(List<Theme> themes) {
        _themes = themes;
    }

    public AmbiDocument getDoc() {
        return _doc;
    }

    protected static class Bean {
        public String type, imgFile, text, clipName;
        public List<String> themes = new ArrayList<>();
    }

    public enum Type {
        IMAGE, TEXT
    }
}
