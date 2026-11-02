package com.finantel.adjuntar_documento.aplicacion;

public interface StorageDocumentos {
    String guardar(byte[] contenido, String extension);
    byte[] leer(String ruta);
}
