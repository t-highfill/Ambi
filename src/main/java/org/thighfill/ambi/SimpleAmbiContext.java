package org.thighfill.ambi;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.data.AmbiDocument;
import org.thighfill.ambi.data.SongPack;
import org.thighfill.ambi.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class SimpleAmbiContext implements AmbiContext {

    private static final Logger LOGGER = Util.getLogger(SimpleAmbiContext.class);

    private final Ambi _parent;
    private final Configuration _config;
    private final File _localDir, _localConfig, _tempDir;
    private List<SongPack> _songPacks = null;

    public SimpleAmbiContext(Ambi parent) {
        _parent = parent;
        _localDir = new File(System.getProperty("user.home"), ".ambi");
        _tempDir = new File(_localDir, "tmp");
        try {
            Files.createDirectories(_localDir.toPath());
            checkTempDir();
        }
        catch (IOException e) {
            Util.handleError(this, "Creating local directories", e);
            System.exit(-1);
        }
        assert _localDir.exists();
        LOGGER.info("Local dir set to {}", _localDir);
        _localConfig = new File(_localDir, "settings.json");
        Configuration config = null;
        InputStream in = null;
        if (_localConfig.exists()) {
            LOGGER.info("Loading local config file {}", _localConfig);
            try {
                in = new FileInputStream(_localConfig);
            }
            catch (FileNotFoundException e) {
                Util.handleError(this, "Reading nonexistant config file", e);
            }
        }
        if (in == null) {
            LOGGER.info("Loading default config file");
            in = SimpleAmbiContext.class.getResourceAsStream(Ambi.SETTINGS_DIR + "/ambi-defaults.json");
        }
        try {
            config = Configuration.fromJSON(in);
        }
        catch (IOException e) {
            Util.handleError(this, "Loading config", e);
            System.exit(-1);
        }
        _config = config;
    }

    private void checkTempDir() throws IOException {
        Path tmp = _tempDir.toPath();
        // Create it if it doesn't exist
        Files.createDirectories(tmp);
        // Clear out any old files we might have missed
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tmp)) {
            for (Path path : stream) {
                Files.delete(path);
            }
        }
    }

    @Override
    public void saveConfiguration() throws IOException {
        LOGGER.info("Saving local config file at {}", _localConfig);
        if (!_localConfig.exists()) {
            LOGGER.info("Creating local file");
            Files.createFile(_localConfig.toPath());
        }
        if (_songPacks != null) {
            _config.setSongPackPaths(_songPacks.stream().map(SongPack::getFilename).filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        try (OutputStream out = new FileOutputStream(_localConfig)) {
            new ObjectMapper().writeValue(out, _config);
        }
        catch (FileNotFoundException e) {
            // This should never happen barring some obscene race condition
            Util.handleError(this, "Config file DNE", e);
        }
    }

    @Override
    public List<SongPack> getKnownSongPacks() {
        if (_songPacks == null) {
            _songPacks = _config.getSongPackPaths().stream().filter(Objects::nonNull).map(p -> {
                try {
                    return new SongPack(this, p);
                }
                catch (IOException e) {
                    Util.handleError(this, "Loading song pack", e);
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return _songPacks;
    }

    @Override
    public File getLocalDirectory() {
        return _localDir;
    }

    @Override
    public File getTempDirectory() {
        return _tempDir;
    }

    @Override
    public Ambi getAmbi() {
        return _parent;
    }

    @Override
    public String getProgramName() {
        return "Ambi";
    }

    @Override
    public String getVersion() {
        return "0.1.0.0";
    }

    @Override
    public String getAuthor() {
        return "Tobias Highfill";
    }

    @Override
    public String getContactEmail() {
        return "trh52@drexel.edu";
    }

    @Override
    public void exit() {
        _parent.exit();
    }

    @Override
    public void openDocument(File file) throws IOException {
        _parent.setDocument(new AmbiDocument(this, new ZipFile(file)));
    }

    @Override
    public Configuration getConfiguration() {
        return _config;
    }

    @Override
    public File getLocalConfigFile() {
        return _localConfig;
    }
}
