package org.thighfill.ambi.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.thighfill.ambi.AmbiContext;
import org.thighfill.ambi.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class SongPack extends ZipStorable<SongPack.Bean> {

    private static final String MAIN_FILE = "songpack.json";

    private String _name, _author;
    private List<Song> _songs;
    private List<Clip> _clips;
    private String _filename = null;

    protected SongPack(AmbiContext context, ZipFile zip, Bean bean) throws IOException {
        super(context);
        _name = bean.name;
        _author = bean.author;
        List<Song> songs;
        songs = new ArrayList<>(bean.songs.size());
        for(Song.Bean b : bean.songs) {
            songs.add(new Song(context, zip, b));
        }
        _songs = songs;
        _clips = bean.clips.stream().map(b -> new Clip(context, _songs, b)).collect(Collectors.toList());
    }

    public SongPack(AmbiContext context, ZipFile zip) throws IOException {
        this(context, zip, fromZip(zip));
        zip.close();
    }

    public SongPack(AmbiContext context, String path) throws IOException {
        this(context, new ZipFile(path));
        _filename = path;
    }

    public SongPack(AmbiContext context) {
        super(context);
        _name = "Untitled Songpack";
        _author = "Unknown";
        _songs = new LinkedList<>();
        _clips = new LinkedList<>();
    }

    public Clip getAppropriateClip(List<Theme> themes) {
        List<Clip> clips = new ArrayList<>(_clips);
        for(Theme t : themes) {
            clips = clips.stream().filter(clip -> clip.getThemes().contains(t)).collect(Collectors.toList());
            if(clips.isEmpty()) {
                break;
            }
        }
        return null; //TODO
    }

    private static Bean fromZip(ZipFile zip) throws IOException {
        return new ObjectMapper().readValue(zip.getInputStream(Util.getEntry(zip, MAIN_FILE)), Bean.class);
    }

    @Override
    protected Bean toBean() {
        Bean res = new Bean();
        res.name = _name;
        res.author = _author;
        res.songs = _songs.stream().map(Song::toBean).collect(Collectors.toList());
        res.clips = _clips.stream().map(Clip::toBean).collect(Collectors.toList());
        return res;
    }

    @Override
    public String toString() {
        return _name;
    }

    public String getFilename() {
        return _filename;
    }

    public String getName() {
        return _name;
    }

    public String getAuthor() {
        return _author;
    }

    public List<Song> getSongs() {
        return _songs;
    }

    public List<Clip> getClips() {
        return _clips;
    }

    protected static class Bean {
        public String name, author;
        public List<Song.Bean> songs = new ArrayList<>();
        public List<Clip.Bean> clips = new ArrayList<>();
    }
}
