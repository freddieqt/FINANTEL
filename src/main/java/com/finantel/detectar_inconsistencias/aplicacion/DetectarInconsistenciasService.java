package com.finantel.detectar_inconsistencias.aplicacion;

import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.detectar_inconsistencias.dominio.repository.InconsistenciaRepository;
import com.finantel.detectar_inconsistencias.dominio.entity.Inconsistencia;
import org.springframework.stereotype.Service;
import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Service
public class DetectarInconsistenciasService {

    private final InconsistenciaRepository repo;

    public DetectarInconsistenciasService(InconsistenciaRepository repo) {
        this.repo = repo;
    }

    public String detectarErrores(@NonNull Integer id) {
        AsientoContable a = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
        if (estaDescuadrado(a)) {
            return "Error: El asiento no está balanceado. Debe: " + a.getDebe() + " Haber: " + a.getHaber();
        }
        return "Sin inconsistencias detectadas";
    }

    public List<Inconsistencia> detectarTodos() {
        List<AsientoContable> asientos = repo.findAll();
        List<Inconsistencia> errores = new ArrayList<>();
        for (AsientoContable a : asientos) {
            if (estaDescuadrado(a)) {
                errores.add(new Inconsistencia(
                    a.getId(),
                    "Desbalance contable",
                    "Debe: " + a.getDebe() + " - Haber: " + a.getHaber()
                ));
            }
        }
        return errores;
    }

    private boolean estaDescuadrado(AsientoContable asiento) {
        return BigDecimal.valueOf(asiento.getDebe())
            .subtract(BigDecimal.valueOf(asiento.getHaber()))
            .abs()
            .compareTo(new BigDecimal("0.01")) > 0;
    }
}