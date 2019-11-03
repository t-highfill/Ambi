package org.thighfill.ambi.data;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipTree {

    private final ZipFile _zipFile;
    private final ZDirectory _root;

    public ZipTree(ZipFile zipFile) {
        _zipFile = zipFile;
        Map<ZipEntry, LinkedList<String>> entriesMap = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            entriesMap.put(entry, new LinkedList<>(Arrays.asList(entry.getName().split("/"))));
        }
        _root = new ZDirectory(null, "", entriesMap);
    }

    public ZDirectory getRoot() {
        return _root;
    }

    public static abstract class ZFile {
        private final String _name;
        private final ZFile _parent;

        protected ZFile(ZFile parent, String name) {
            _parent = parent;
            _name = name;
        }

        public abstract boolean isDirectory();

        public String getName() {
            return _name;
        }

        public ZFile getParent() {
            return _parent;
        }
    }

    private static <E> LinkedList<E> popCopy(LinkedList<E> list) {
        LinkedList<E> res = new LinkedList<>(list);
        res.removeFirst();
        return res;
    }

    public static class ZDirectory extends ZFile {
        private final Map<String, ZFile> _listing;

        public ZDirectory(ZFile parent, String name, Map<ZipEntry, LinkedList<String>> entries) {
            super(parent, name);
            _listing = entries.entrySet().stream().collect(Collectors.toMap(e -> e.getValue().get(0), e -> {
                LinkedList<String> path = e.getValue();
                String fname = path.get(0);
                if (path.size() == 1) {
                    return new ZRegFile(this, fname, e.getKey());
                }
                Map<ZipEntry, LinkedList<String>> nextLvl = entries.entrySet().stream().filter(
                        e2 -> e2.getValue().size() > 1).filter(e2 -> e2.getValue().get(0).equals(fname)).collect(
                        Collectors.toMap(Map.Entry::getKey, e2 -> popCopy(e2.getValue())));
                return new ZDirectory(this, fname, nextLvl);
            }));
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

        @Override
        public boolean isDirectory() {
            return true;
        }
    }

    public static class ZRegFile extends ZFile {

        private final ZipEntry _zipEntry;

        protected ZRegFile(ZFile parent, String name, ZipEntry zipEntry) {
            super(parent, name);
            _zipEntry = zipEntry;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        public ZipEntry getZipEntry() {
            return _zipEntry;
        }
    }
}
