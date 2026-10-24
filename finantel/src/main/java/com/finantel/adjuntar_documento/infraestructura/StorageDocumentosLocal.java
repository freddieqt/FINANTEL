package com.finantel.adjuntar_documento.infraestructura;

import com.finantel.adjuntar_documento.aplicacion.StorageDocumentos;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class StorageDocumentosLocal implements StorageDocumentos {

    private final Path base = Paths.get(System.getProperty("user.home"), "finantel-docs");

    @Override
    public String guardar(byte[] contenido, String extension) {
        try {
            Files.createDirectories(base);
            String nombre = UUID.randomUUID() + "." + extension;
            Files.write(base.resolve(nombre), contenido);
            return nombre;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el documento", e);
        }
    }

    @Override
    public byte[] leer(String ruta) {
        try {
            return Files.readAllBytes(base.resolve(ruta));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el documento", e);
        }
    }
}
