package com.github.alexthe666.iceandfire.client.model.util;

import dev.vladeezyy.rampart.client.model.TabulaModelHandler;
import dev.vladeezyy.rampart.client.model.container.TabulaModelContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TabulaModelHandlerHelper {

    public static TabulaModelContainer loadTabulaModel(String path) throws IOException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (!path.endsWith(".tbl")) {
            path = path + ".tbl";
        }

        // Class#getResourceAsStream: the path is absolute ("/assets/..."), a class loader would want it without the slash
        // (Fabric's loader returns null then); this class is in the mod's own jar
        InputStream stream = TabulaModelHandlerHelper.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("No Tabula model " + path);
        }
        return TabulaModelHandler.INSTANCE.loadTabulaModel(getModelJsonStream(path, stream));
    }

    private static InputStream getModelJsonStream(String name, InputStream file) throws IOException {
        ZipInputStream zip = new ZipInputStream(file);

        ZipEntry entry;
        do {
            if ((entry = zip.getNextEntry()) == null) {
                throw new RuntimeException("No model.json present in " + name);
            }
        } while (!entry.getName().equals("model.json"));

        return zip;
    }
}
