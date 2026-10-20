package com.finantel.importar_datos_excel.aplicacion;

import com.finantel.importar_datos_excel.dominio.repository.ImportarDatosExcelRepository;
import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportarDatosExcelService {

    private final ImportarDatosExcelRepository repository;
    private final AuditoriaService auditoriaService;

    public ImportarDatosExcelService(ImportarDatosExcelRepository repository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Map<String, Object> importar(MultipartFile archivo, String usuario) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (archivo == null || archivo.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Error: archivo vacío");
            return response;
        }

        String nombre = archivo.getOriginalFilename();
        if (nombre == null || (!nombre.endsWith(".xlsx") && !nombre.endsWith(".xls") && !nombre.endsWith(".csv"))) {
            response.put("exito", false);
            response.put("mensaje", "Error: formato inválido");
            return response;
        }

        try {
            if (nombre.endsWith(".csv")) {
                return importarCSV(archivo, usuario);
            } else {
                return importarExcel(archivo, usuario);
            }
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al procesar el archivo: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> importarCSV(MultipartFile archivo, String usuario) throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();
        List<AsientoContable> asientos = new ArrayList<>();
        List<Map<String, Object>> preview = new ArrayList<>();

        String contenido = new String(archivo.getBytes());
        String[] lineas = contenido.split("\n");
        int count = 0;

        for (int i = 1; i < lineas.length; i++) {
            if (lineas[i].trim().isEmpty()) continue;

            String[] campos = lineas[i].split(",");
            if (campos.length < 4) continue;

            String tipo = campos[0].trim();
            String descripcion = campos[1].trim();
            double debe = parseDouble(campos[2].trim());
            double haber = parseDouble(campos[3].trim());
            String cuenta = campos.length > 4 ? campos[4].trim() : "1011";
            String centro = campos.length > 5 ? campos[5].trim() : "General";
            String moneda = campos.length > 6 ? campos[6].trim() : "PEN";

            if (tipo.isEmpty()) continue;

            AsientoContable a = new AsientoContable();
            a.setTipo(tipo);
            a.setDescripcion(descripcion);
            a.setDebe(debe);
            a.setHaber(haber);
            a.setEstado("Activo");
            a.setUsuario(usuario);
            a.setCuentaContable(cuenta.isEmpty() ? "1011" : cuenta);
            a.setCentroCosto(centro.isEmpty() ? "General" : centro);
            a.setMoneda(moneda.isEmpty() ? "PEN" : moneda);
            a.setTipoCambio(1.0);
            a.setFechaRegistro(LocalDateTime.now());
            a.setPeriodoId(3);
            a.setNumeroCorrelativo("IMP-" + System.currentTimeMillis() + "-" + count);
            asientos.add(a);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipo", tipo);
            item.put("descripcion", descripcion);
            item.put("cuenta", a.getCuentaContable());
            item.put("debe", debe);
            item.put("haber", haber);
            item.put("centro", centro);
            preview.add(item);

            count++;
        }

        repository.saveAll(asientos);
        auditoriaService.registrar(usuario, "Importación CSV", "AsientosContables", null, archivo.getOriginalFilename() + " — " + count + " registros");

        response.put("exito", true);
        response.put("mensaje", count + " asientos importados correctamente");
        response.put("asientos", preview);
        response.put("total", count);

        return response;
    }

    private Map<String, Object> importarExcel(MultipartFile archivo, String usuario) throws Exception {
        Map<String, Object> response = new LinkedHashMap<>();

        try (InputStream is = archivo.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<AsientoContable> asientos = new ArrayList<>();
            List<Map<String, Object>> preview = new ArrayList<>();
            int count = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String tipo = getCellValue(row, 0);
                String descripcion = getCellValue(row, 1);
                String debeStr = getCellValue(row, 2);
                String haberStr = getCellValue(row, 3);
                String cuenta = getCellValue(row, 4);
                String centro = getCellValue(row, 5);
                String moneda = getCellValue(row, 6);

                if (tipo == null || tipo.isBlank()) continue;

                AsientoContable a = new AsientoContable();
                a.setTipo(tipo);
                a.setDescripcion(descripcion);
                a.setDebe(parseDouble(debeStr));
                a.setHaber(parseDouble(haberStr));
                a.setEstado("Activo");
                a.setUsuario(usuario);
                a.setCuentaContable(cuenta.isEmpty() ? "1011" : cuenta);
                a.setCentroCosto(centro.isEmpty() ? "General" : centro);
                a.setMoneda(moneda.isEmpty() ? "PEN" : moneda);
                a.setTipoCambio(1.0);
                a.setFechaRegistro(LocalDateTime.now());
                a.setPeriodoId(3);
                a.setNumeroCorrelativo("IMP-" + System.currentTimeMillis() + "-" + count);
                asientos.add(a);

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("tipo", tipo);
                item.put("descripcion", descripcion);
                item.put("cuenta", a.getCuentaContable());
                item.put("debe", a.getDebe());
                item.put("haber", a.getHaber());
                item.put("centro", a.getCentroCosto());
                preview.add(item);

                count++;
            }

            repository.saveAll(asientos);
            auditoriaService.registrar(usuario, "Importación Excel", "AsientosContables", null, archivo.getOriginalFilename() + " — " + count + " registros");

            response.put("exito", true);
            response.put("mensaje", count + " asientos importados correctamente");
            response.put("asientos", preview);
            response.put("total", count);

            return response;
        }
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    private double parseDouble(String val) {
        try { return Double.parseDouble(val); } catch (Exception e) { return 0.0; }
    }
}