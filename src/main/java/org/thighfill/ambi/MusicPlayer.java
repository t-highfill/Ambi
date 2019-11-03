package org.thighfill.ambi;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.data.Clip;
import org.thighfill.ambi.util.Util;

import javax.swing.SwingUtilities;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class MusicPlayer {

    private static final Logger LOGGER = Util.getLogger(MusicPlayer.class);
    private static boolean JAVAFX_INIT = false;

    private static void initJavaFX() {
        if (JAVAFX_INIT) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel();
            latch.countDown();
        });
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            LOGGER.warn("JavaFX may not have been initialized");
        }
    }

    private final AmbiContext _context;

    private MediaPlayer _player;
    private double _masterVolume = 1.0;
    private boolean _mute = false;
    private Clip _clip;

    public MusicPlayer(AmbiContext context) {
        _context = context;
        initJavaFX();
    }

    public void setClip(Clip clip) {
        if(clip == _clip){
            return;
        }
        stop();
        _clip = clip;
        if(clip == null){
            _player = null;
            return;
        }
        File tmpFile = clip.getSong().getTmpFile();
        Media media = new Media(tmpFile.toURI().toString());
        _player = new MediaPlayer(media);
        _player.setCycleCount(MediaPlayer.INDEFINITE);
        _player.setStartTime(new Duration(clip.getStart()));
        Double end = clip.getEnd();
        if (end != null) {
            _player.setStopTime(new Duration(end));
        }
        updateVolume();
    }

    public double getMasterVolume() {
        return _masterVolume;
    }

    public void setMasterVolume(double masterVolume) {
        _masterVolume = masterVolume;
        updateVolume();
    }

    public boolean isMute() {
        return _mute;
    }

    public void setMute(boolean mute) {
        _mute = mute;
        updateVolume();
    }

    private void updateVolume() {
        if (_player != null) {
            if (_mute) {
                _player.setVolume(0);
            }
            else {
                double clipVol = _clip == null ? 1.0 : _clip.getVolume();
                _player.setVolume(clipVol * _masterVolume);
            }
        }
    }

    public void play() {
        if (_player != null) {
            _player.play();
        }
    }

    public boolean isPaused() {
        return _player != null && _player.getStatus() == MediaPlayer.Status.PAUSED;
    }

    public void pause() {
        if (_player != null) {
            _player.pause();
        }
    }

    public void stop() {
        if (_player != null) {
            _player.stop();
        }
    }
}
