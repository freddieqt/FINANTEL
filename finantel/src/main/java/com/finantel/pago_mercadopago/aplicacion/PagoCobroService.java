package com.finantel.pago_mercadopago.aplicacion;

import com.finantel.pago_mercadopago.dominio.entity.PagoMercadoPago;
import com.finantel.pago_mercadopago.dominio.repository.PagoMercadoPagoRepository;
import com.finantel.registrar_asiento_contable.aplicacion.AsientoContableService;
import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import com.finantel.registrar_asiento_contable.dominio.repository.AsientoContableRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PagoCobroService {

    private final PagoService pagoService;
    private final PagoMercadoPagoRepository pagoRepo;
    private final AsientoContableRepository asientoRepo;
    private final AsientoContableService asientoService;

    public PagoCobroService(PagoService pagoService,
                            PagoMercadoPagoRepository pagoRepo,
                            AsientoContableRepository asientoRepo,
                            AsientoContableService asientoService) {
        this.pagoService = pagoService;
        this.pagoRepo = pagoRepo;
        this.asientoRepo = asientoRepo;
        this.asientoService = asientoService;
    }

    public PagoMercadoPago generarEnlace(Integer asientoId) throws Exception {
        AsientoContable a = asientoRepo.findById(asientoId)
            .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));

        if (!"1201".equals(a.getCuentaContable()))
            throw new RuntimeException("Solo se pueden cobrar asientos de la cuenta 1201 (Cuentas por cobrar)");

        String ref = a.getNumeroCorrelativo();
        BigDecimal monto = BigDecimal.valueOf(a.getDebe());
        String url = pagoService.crearEnlace("Cobro " + ref + " - " + a.getDescripcion(), monto, ref);

        PagoMercadoPago p = new PagoMercadoPago();
        p.setAsientoId(asientoId);
        p.setNumeroCorrelativo(ref);
        p.setMonto(monto);
        p.setInitPoint(url);
        p.setEstado("PENDIENTE");
        p.setFechaCreacion(LocalDateTime.now());
        return pagoRepo.save(p);
    }

    public String verificarYRegistrar(Integer asientoId, String usuario) throws Exception {
        AsientoContable a = asientoRepo.findById(asientoId)
            .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));

        Optional<String> pagoAprobado = pagoService.buscarPagoAprobado(a.getNumeroCorrelativo());
        if (pagoAprobado.isEmpty())
            return "Todavía no hay un pago aprobado para " + a.getNumeroCorrelativo();

        String paymentId = pagoAprobado.get();
        if (pagoRepo.existsByPaymentId(paymentId))
            return "Este pago ya fue registrado";

        // Asiento de cobro: Debe 1011 Caja / Haber 1201 Cuentas por cobrar
        AsientoContable cobro = new AsientoContable();
        cobro.setTipo("Operación ordinaria");
        cobro.setDescripcion("Cobro " + a.getNumeroCorrelativo() + " vía Mercado Pago");
        cobro.setDebe(a.getDebe());
        cobro.setHaber(a.getDebe());
        cobro.setCuentaContable("1011");
        cobro.setMoneda(a.getMoneda());
        cobro.setUsuario(usuario);
        asientoService.crear(cobro);

        PagoMercadoPago p = pagoRepo.findFirstByAsientoIdOrderByIdDesc(asientoId)
            .orElseThrow(() -> new RuntimeException("No hay enlace generado para este asiento"));
        p.setPaymentId(paymentId);
        p.setEstado("APROBADO");
        pagoRepo.save(p);

        return "Cobro registrado. Pago " + paymentId;
    }
}