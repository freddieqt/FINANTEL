package com.finantel.ocr_documentos.aplicacion;

import net.sourceforge.tess4j.Tesseract;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class OcrDocumentosServiceTest {

    @Test
    void extraerTextoProcesaImagenYDevuelveResultadoOcr() throws Exception {
        ByteArrayOutputStream contenido = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", contenido);
        MockMultipartFile imagen = new MockMultipartFile(
            "archivo", "documento.png", "image/png", contenido.toByteArray());

        try (MockedConstruction<Tesseract> tesseract = mockConstruction(
                Tesseract.class,
                (mock, context) -> when(mock.doOCR(any(java.io.File.class))).thenReturn("Texto reconocido"))) {
            String resultado = new OcrDocumentosService().extraerTexto(imagen);

            assertEquals("Texto reconocido", resultado);
            assertEquals(1, tesseract.constructed().size());
        }
    }

    @Test
    void extraerTextoRechazaArchivoVacioOSinImagen() {
        OcrDocumentosService service = new OcrDocumentosService();
        MockMultipartFile vacio = new MockMultipartFile("archivo", "vacio.png", "image/png", new byte[0]);
        MockMultipartFile noImagen = new MockMultipartFile(
            "archivo", "texto.txt", "text/plain", "no es imagen".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.extraerTexto(vacio));
        assertThrows(IllegalArgumentException.class, () -> service.extraerTexto(noImagen));
    }
}
