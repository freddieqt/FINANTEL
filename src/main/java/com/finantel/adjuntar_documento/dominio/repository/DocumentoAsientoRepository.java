package com.finantel.adjuntar_documento.dominio.repository;

import com.finantel.adjuntar_documento.dominio.entity.DocumentoAsiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoAsientoRepository extends JpaRepository<DocumentoAsiento, Integer> {
}
