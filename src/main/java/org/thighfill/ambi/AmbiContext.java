package org.thighfill.ambi;

import org.thighfill.ambi.data.SongPack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AmbiContext {
    Ambi getAmbi();

    String getProgramName();

    String getVersion();

    String getAuthor();

    String getContactEmail();

    void exit();

    void openDocument(File file) throws IOException;

    Configuration getConfiguration();

    File getLocalDirectory();

    File getTempDirectory();

    File getLocalConfigFile();

    void saveConfiguration() throws IOException;

    List<SongPack> getKnownSongPacks();
}
