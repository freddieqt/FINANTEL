package com.finantel.generar_estados_financieros.aplicacion;

import com.finantel.generar_estados_financieros.dominio.repository.EstadosFinancierosRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GenerarEstadosFinancierosService {

    private final EstadosFinancierosRepository repo;

    public GenerarEstadosFinancierosService(EstadosFinancierosRepository repo) {
        this.repo = repo;
    }

    public Map<String, Object> getBalanceGeneral(){
        List<Object[]> rows = repo.findTodosSaldos();

        List<Map<String, Object>> ingresos = new ArrayList<>();
        List<Map<String, Object>> costos = new ArrayList<>();
        List<Map<String, Object>> gastosOp = new ArrayList<>();
        List<Map<String, Object>> gastosFin = new ArrayList<>();

        double totalIngresos = 0;
        double totalCostos = 0;
        double totalGastosOp = 0;
        double totalGastosFin = 0;

        for (Object[] row : rows) {
            String codigo = (String) row[0];
            String descripcion = (String) row[1];
            Double saldoObj = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            double saldo = Math.abs(saldoObj != null ? saldoObj : 0.0);

            if (codigo == null || codigo.isEmpty()) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("codigo", codigo);
            item.put("descripcion", descripcion);
            item.put("saldo", saldo);

            char primera = codigo.charAt(0);

            if (primera == '5') {
                ingresos.add(item);
                totalIngresos += saldo;
            } else if (codigo.startsWith("600")) {
                costos.add(item);
                totalCostos += saldo;
            } else if (codigo.equals("4501") || (codigo.startsWith("6") && !codigo.startsWith("600") && !codigo.startsWith("65"))) {
                gastosOp.add(item);
                totalGastosOp += saldo;
            } else if (codigo.startsWith("65")) {
                gastosFin.add(item);
                totalGastosFin += saldo;
            }
        }

        double utilidadBruta = totalIngresos - totalCostos;
        double resultadoOp = utilidadBruta - totalGastosOp;
        double utilidadNeta = resultadoOp - totalGastosFin;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ingresos", ingresos);
        result.put("totalIngresos", totalIngresos);
        result.put("costos", costos);
        result.put("totalCostos", totalCostos);
        result.put("utilidadBruta", utilidadBruta);
        result.put("gastosOperativos", gastosOp);
        result.put("totalGastosOperativos", totalGastosOp);
        result.put("resultadoOperativo", resultadoOp);
        result.put("gastosFinancieros", gastosFin);
        result.put("totalGastosFinancieros", totalGastosFin);
        result.put("utilidadNeta", utilidadNeta);

        return result;
    }
    private void calcularPorcentajes(List<Map<String, Object>> lista, double total) {
        if (total == 0) return;
        for (Map<String, Object> item : lista) {
            Object saldoObj = item.get("saldo");
            if (saldoObj == null) continue;
            double saldo = ((Number) saldoObj).doubleValue();
            double pct = Math.abs(saldo / total * 100);
            item.put("porcentaje", Math.round(pct * 10.0) / 10.0);
        }
    }
    public Map<String, Object> getBalanceGeneralReal() {
        List<Object[]> rows = repo.findTodosSaldos();

        List<Map<String, Object>> activos = new ArrayList<>();
        List<Map<String, Object>> pasivos = new ArrayList<>();
        List<Map<String, Object>> patrimonio = new ArrayList<>();

        double totalActivo = 0, totalPasivo = 0, totalPatrimonio = 0;

        for (Object[] row : rows) {
            String codigo = (String) row[0];
            String descripcion = (String) row[1];
            String clasificacion = row[2] != null ? (String) row[2] : "";
            double saldo = row[3] != null ? Math.abs(((Number) row[3]).doubleValue()) : 0.0;

            if (codigo == null || codigo.isEmpty()) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("codigo", codigo);
            item.put("descripcion", descripcion);
            item.put("saldo", saldo);
            item.put("porcentaje", 0.0);

            String cla = clasificacion.toLowerCase();
            if (cla.contains("activo")) {
                activos.add(item);
                totalActivo += saldo;
            } else if (cla.contains("pasivo")) {
                pasivos.add(item);
                totalPasivo += saldo;
            } else if (cla.contains("patrimonio")) {
                patrimonio.add(item);
                totalPatrimonio += saldo;
            }
        }

        calcularPorcentajes(activos, totalActivo);
        calcularPorcentajes(pasivos, totalPasivo);
        calcularPorcentajes(patrimonio, totalPatrimonio);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activos", activos);
        result.put("totalActivo", totalActivo);
        result.put("pasivos", pasivos);
        result.put("totalPasivo", totalPasivo);
        result.put("patrimonio", patrimonio);
        result.put("totalPatrimonio", totalPatrimonio);

        return result;
    }
}