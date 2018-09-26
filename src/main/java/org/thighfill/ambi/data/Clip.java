package org.thighfill.ambi.data;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.AmbiContext;
import org.thighfill.ambi.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Clip extends ZipStorable<Clip.Bean> {

    private static final Logger LOGGER = Util.getLogger(Clip.class);

    private String _name;
    private List<Theme> _themes;
    private Integer _start, _end;
    private double _volume;
    private Song _song;

    public Clip(AmbiContext context, List<Song> songs, Bean bean) {
        super(context);
        _name = bean.name;
        _themes = bean.themes.stream().map(Theme::fromJSONName).collect(Collectors.toList());
        _start = bean.start;
        _end = bean.end;
        _volume = bean.volume;
        _song = songs.stream().filter(s -> s.getId().equals(bean.songID)).findFirst().orElse(null);
    }

    @Override
    protected Bean toBean() {
        Bean res = new Bean();
        res.name = _name;
        res.songID = _song.getId();
        res.themes = _themes.stream().map(Theme::getJsonName).collect(Collectors.toList());
        res.start = _start;
        res.end = _end;
        res.volume = _volume;
        return res;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getArtist() {
        return _song.getArtist();
    }

    public List<Theme> getThemes() {
        return _themes;
    }

    public void setThemes(List<Theme> themes) {
        _themes = themes;
    }

    public int getStart() {
        return _start;
    }

    public void setStart(int start) {
        _start = start;
    }

    public int getEnd() {
        return _end;
    }

    public void setEnd(int end) {
        _end = end;
    }

    public double getVolume() {
        return _volume;
    }

    public void setVolume(double volume) {
        _volume = volume;
    }

    public Song getSong() {
        return _song;
    }

    public void setSong(Song song) {
        _song = song;
    }

    protected static class Bean {
        public String name = "", songID = "";
        public List<String> themes = new ArrayList<>();
        public Integer start, end;
        public double volume = 1;
    }
}
