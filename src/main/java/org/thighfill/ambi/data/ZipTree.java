package org.thighfill.ambi.data;

import org.apache.logging.log4j.Logger;
import org.thighfill.ambi.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipTree implements AutoCloseable {

    private static final Logger LOGGER = Util.getLogger(ZipTree.class);

    private final ZipFile _zipFile;
    private final ZDirectory _root;

    public ZipTree(ZipFile zipFile) {
        _zipFile = zipFile;
        _root = new ZDirectory(null, "/");
        zipFile.stream().forEach(e -> {
            LinkedList<String> path = new LinkedList<>(Arrays.asList(e.getName().split("/")));
            LOGGER.debug("Adding Entry {}", path);
            _root.add(e, path);
        });
    }

    private ZipTree(ZipFile zipFile, ZDirectory root){
        _zipFile = zipFile;
        _root = root;
    }

    public ZipTree chroot(ZDirectory newRoot){
        return new ZipTree(_zipFile, newRoot);
    }

    public ZDirectory getRoot() {
        return _root;
    }

    private static <E> LinkedList<E> popCopy(LinkedList<E> list) {
        LinkedList<E> res = new LinkedList<>(list);
        res.removeFirst();
        return res;
    }

    @Override
    public void close() throws IOException {
        _zipFile.close();
    }

    public abstract class ZFile {
        private final String _name;
        private final ZDirectory _parent;

        protected ZFile(ZDirectory parent, String name) {
            _parent = parent;
            _name = name;
        }

        public abstract boolean isDirectory();

        public String getName() {
            return _name;
        }

        public ZDirectory getParent() {
            return _parent;
        }

        public abstract ZRegFile asRegFile();

        @Override
        public String toString() {
            String pre = _parent == null ? "" : (_parent.toString() + "/");
            return pre + _name;
        }
    }

    public class ZDirectory extends ZFile {
        private final Map<String, ZFile> _listing;

        public ZDirectory(ZDirectory parent, String name) {
            super(parent, name);
            _listing = new HashMap<>();
        }

        private void add(ZipEntry entry, LinkedList<String> path) {
            if (path.isEmpty()) {
                return;
            }
            String fname = path.get(0);
            if (path.size() == 1 && !entry.isDirectory()) {
                LOGGER.debug("Adding File \"{}\" to \"{}\"", fname, this.toString());
                _listing.put(fname, new ZRegFile(this, fname, entry));
            }
            else {
                ZDirectory sub = (ZDirectory) _listing.get(fname);
                if (sub == null) {
                    LOGGER.debug("Adding Directory \"{}\" to \"{}\"", fname, this.toString());
                    sub = new ZDirectory(this, fname);
                    _listing.put(fname, sub);
                }
                sub.add(entry, popCopy(path));
            }
        }

        public ZFile get(String name) {
            return _listing.get(name);
        }

        public ZFile find(String name) {
            ZFile res = get(name);
            if (res != null) {
                return res;
            }
            for (ZFile file : _listing.values()) {
                if (file instanceof ZDirectory) {
                    res = ((ZDirectory) file).find(name);
                    if (res != null) {
                        return res;
                    }
                }
            }
            return null;
        }

        public ZFile followPath(String path) throws FileNotFoundException {
            return followPath(path.split("/"), 0);
        }

        private ZFile followPath(String[] path, int start) throws FileNotFoundException {
            ZFile next = get(path[start]);
            if (next == null) {
                throw new FileNotFoundException(
                        String.format("Could not find file %s in directory %s", path[start], this));
            }
            if (start == path.length - 1) {
                return next;
            }
            if (next.isDirectory()) {
                return ((ZDirectory) next).followPath(path, start + 1);
            }
            throw new FileNotFoundException();
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public ZRegFile asRegFile() {
            throw new RuntimeException("This is a directory!");
        }
    }

    public class ZRegFile extends ZFile {

        private final ZipEntry _zipEntry;

        protected ZRegFile(ZDirectory parent, String name, ZipEntry zipEntry) {
            super(parent, name);
            _zipEntry = zipEntry;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public ZRegFile asRegFile() {
            return this;
        }

        public ZipEntry getZipEntry() {
            return _zipEntry;
        }

        public InputStream getInputStream() throws IOException {
            return _zipFile.getInputStream(_zipEntry);
        }
    }
}
