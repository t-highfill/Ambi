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

    public File getLocalDirectory();

    public File getLocalConfigFile();

    public void saveConfiguration() throws IOException;

    public List<SongPack> getKnownSongPacks();
}
