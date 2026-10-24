package com.finantel.conciliacion_bancaria.aplicacion;

import com.finantel.registrar_accion.aplicacion.AuditoriaService;
import com.finantel.conciliacion_bancaria.dominio.entity.MovimientoBancario;
import com.finantel.conciliacion_bancaria.dominio.repository.ConciliacionBancariaRepository;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ConciliacionBancariaService {

    private final ConciliacionBancariaRepository repoMovimientos;
    private final AsientoContableRepository repoAsientos;
    private final AuditoriaService auditoriaService;

    public ConciliacionBancariaService(ConciliacionBancariaRepository repoMovimientos,
                                        AsientoContableRepository repoAsientos,
                                        AuditoriaService auditoriaService) {
        this.repoMovimientos = repoMovimientos;
        this.repoAsientos = repoAsientos;
        this.auditoriaService = auditoriaService;
    }

    public Map<String, Object> conciliar(String nombreArchivo) {
        List<AsientoContable> asientos = repoAsientos.findAll();
        List<MovimientoBancario> movimientos = repoMovimientos.findAll();
        double totalAsientos = asientos.stream()
            .mapToDouble(a -> a.getDebe() != null ? a.getDebe() : 0).sum();
        double totalBanco = movimientos.stream()
            .mapToDouble(m -> m.getMonto() != null ? m.getMonto() : 0).sum();
        String estado = Math.abs(totalBanco - totalAsientos) < 0.01 ? "Conciliado" : "Con diferencias";
        auditoriaService.registrar(
            "sistema", "Conciliación bancaria", "MovimientosBancarios",
            null, estado
        );

        String archivoFinal = (nombreArchivo != null && !nombreArchivo.trim().isEmpty()) ? nombreArchivo : "archivo_por_defecto.xlsx";

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("archivo", archivoFinal);
        resultado.put("totalAsientos", totalAsientos);
        resultado.put("totalBanco", totalBanco);
        resultado.put("diferencia", totalBanco - totalAsientos);
        resultado.put("estado", estado);
        return resultado;
    }

    public List<MovimientoBancario> listarMovimientos() {
        return repoMovimientos.findAll();
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void conciliarAutomatico() {
        conciliar("automatico");
    }
}