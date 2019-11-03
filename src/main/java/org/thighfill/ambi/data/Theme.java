package org.thighfill.ambi.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Theme {
    GOOD("good", "Good"), BAD("bad", "Bad"), AMBIENT("ambient", "Ambient", "ambience"), SILENT("silent", "silence");

    private final String _jsonName, _friendlyName;
    private final Set<String> _synonyms;
    private final Theme _parent;

    Theme(String jsonName, String friendlyName, String... synonyms) {
        this(null, jsonName, friendlyName, synonyms);
    }

    Theme(Theme parent, String jsonName, String friendlyName, String... synonyms) {
        _jsonName = jsonName;
        _friendlyName = friendlyName;
        _synonyms = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(synonyms)));
        _parent = parent;
    }

    public String getFullName() {
        return (_parent == null ? "" : (_parent.getFullName() + ".")) + getFriendlyName();
    }

    public String getJsonName() {
        return _jsonName;
    }

    public String getFriendlyName() {
        return _friendlyName;
    }

    public Set<String> getSynonyms() {
        return _synonyms;
    }

    public Theme getParent() {
        return _parent;
    }

    private static Map<String, Theme> JSON_MAP = initJSONMap();

    private static Map<String, Theme> initJSONMap() {
        Map<String, Theme> res = new HashMap<>();
        for (Theme t : Theme.values()) {
            res.put(t.getJsonName(), t);
        }
        return res;
    }

    public static Theme fromJSONName(String jsonName) {
        return JSON_MAP.get(jsonName);
    }
}
