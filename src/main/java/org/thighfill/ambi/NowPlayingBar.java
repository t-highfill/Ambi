package org.thighfill.ambi;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.data.AmbiDocument;
import org.thighfill.ambi.data.Clip;
import org.thighfill.ambi.data.Page;
import org.thighfill.ambi.data.SongPack;
import org.thighfill.ambi.util.Util;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import java.util.stream.Stream;

public class NowPlayingBar extends JToolBar {

    private static Logger LOGGER = Util.getLogger(NowPlayingBar.class);
    private static int MAX_VOLUME = 100;

    private AmbiContext _context;

    private JComboBox<SongPack> _songPackSelector = new JComboBox<>();
    private JLabel _songName = new JLabel(), _artist = new JLabel();
    private JPanel _metaPanel = new JPanel();
    private JButton _playPause = new JButton("Play/Pause");
    private final JSlider _volumeSlider = new JSlider(0, MAX_VOLUME, MAX_VOLUME);
    private final JButton _muteBtn = new JButton("Mute");

    private AmbiDocument _currDoc = null;
    private SongPack _songPack = null;
    private int _page = 0;
    private MusicPlayer _player;

    public NowPlayingBar(AmbiContext context) {
        super("Now Playing");
        _context = context;
        _player = new MusicPlayer(context);

        Stream.of(_songPackSelector, _metaPanel, _playPause, _volumeSlider, _muteBtn).forEach(c -> {
            c.setVisible(true);
            add(c);
        });

        Stream.of(_songName, _artist).forEach(c -> {
            c.setVisible(true);
            _metaPanel.add(c);
        });

        SongPack internal = new InternalSongPack();
        setSongPack(internal);
        _songPackSelector.addItem(internal);
        _context.getKnownSongPacks().forEach(_songPackSelector::addItem);

        _songPackSelector.addActionListener(e -> setSongPack((SongPack) _songPackSelector.getSelectedItem()));

        _playPause.addActionListener(e -> {
            if (_player.isPaused()) {
                _player.play();
            }
            else {
                _player.pause();
            }
        });

        _volumeSlider.addChangeListener(e -> {
            double vol = _volumeSlider.getValue();
            _player.setMasterVolume(vol / MAX_VOLUME);
            _player.setMute(false);
        });
        _muteBtn.addActionListener(e -> _player.setMute(!_player.isMute()));
    }

    private void setSongPack(SongPack songPack) {
        _songPack = songPack;
    }

    public void setDocument(AmbiDocument doc) {
        _currDoc = doc;
        _player.stop();
    }

    public void setPage(int page) {
        _page = page;
        updatePlayer();
    }

    private void updatePlayer() {
        if (_currDoc == null) {
            _player.stop();
            return;
        }
        Page page = _currDoc.getPages().get(_page);
        SongPack pack = _songPack instanceof InternalSongPack ? _currDoc.getSongPack() : _songPack;
        Clip clip = page.getClip();
        if (clip == null) {
            if (pack == null) {
                LOGGER.error("Null songpack");
                return;
            }
            clip = pack.getAppropriateClip(page.getThemes());
            if (clip == null) {
                LOGGER.warn("No clip found for page {} in pack {}", _page, pack);
                _player.stop();
                return;
            }
        }
        _songName.setText(clip.getName());
        _artist.setText(clip.getArtist());
        _player.setClip(clip);
        _player.play();
    }

    private class InternalSongPack extends SongPack {
        private InternalSongPack() {
            super(_context);
        }

        @Override
        public String toString() {
            return "[Internal]";
        }
    }
}
