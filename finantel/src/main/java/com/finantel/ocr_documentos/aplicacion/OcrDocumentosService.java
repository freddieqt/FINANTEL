package com.finantel.ocr_documentos.aplicacion;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OcrDocumentosService {

    @Value("${ocr.tessdata-path:}")
    private String tessdataPath;

    public String extraerTexto(MultipartFile archivo) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio");
        }

        Path tempDir = Files.createTempDirectory("ocr");
        Path originalFile = tempDir.resolve("documento");
        Path pngFile = tempDir.resolve("imagen.png");
        try {
            archivo.transferTo(originalFile);

            BufferedImage image = ImageIO.read(originalFile.toFile());
            if (image == null) {
                throw new IllegalArgumentException("El archivo no contiene una imagen compatible");
            }
            if (!ImageIO.write(image, "png", pngFile.toFile())) {
                throw new IllegalArgumentException("No se pudo convertir la imagen a PNG");
            }

            Tesseract tesseract = new Tesseract();
            if (tessdataPath != null && !tessdataPath.isBlank()) {
                tesseract.setDatapath(tessdataPath);
            }
            tesseract.setLanguage("eng+spa");
            return tesseract.doOCR(pngFile.toFile());
        } finally {
            Files.deleteIfExists(originalFile);
            Files.deleteIfExists(pngFile);
            Files.deleteIfExists(tempDir);
        }
    }
}