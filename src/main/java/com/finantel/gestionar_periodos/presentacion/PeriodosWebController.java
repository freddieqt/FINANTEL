package com.finantel.gestionar_periodos.presentacion;

import com.finantel.gestionar_periodos.dominio.entity.PeriodoContable;
import com.finantel.gestionar_periodos.dominio.repository.PeriodoContableRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class PeriodosWebController {

    private final PeriodoContableRepository repo;

    public PeriodosWebController(PeriodoContableRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/web/periodos/lista")
    public List<Map<String, Object>> lista() {
        return repo.findAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("periodo", p.getPeriodo());
            m.put("estado", p.getEstado());
            return m;
        }).toList();
    }
}