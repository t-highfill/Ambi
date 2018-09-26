package org.thighfill.ambi.data;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.thighfill.ambi.AmbiContext;

import java.io.File;
import java.io.IOException;

public abstract class ZipStorable<B> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AmbiContext _context;

    public ZipStorable(AmbiContext context) {
        _context = context;
    }

    protected abstract B toBean();

    public void writeTo(File file) throws IOException {
        MAPPER.writeValue(file, toBean());
    }

    public AmbiContext getContext() {
        return _context;
    }
}
