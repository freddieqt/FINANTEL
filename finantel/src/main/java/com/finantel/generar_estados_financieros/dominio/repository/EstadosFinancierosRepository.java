package com.finantel.generar_estados_financieros.dominio.repository;

import com.finantel.registrar_asiento_contable.dominio.entity.AsientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EstadosFinancierosRepository extends JpaRepository<AsientoContable, Integer> {

	@Query(value = """
		    SELECT 
		        cc.codigo,
		        cc.descripcion,
		        cc.clasificacion,
		        CASE 
		            WHEN cc.tipo = 'Debito'  THEN ISNULL(SUM(a.debe), 0) - ISNULL(SUM(a.haber), 0)
		            WHEN cc.tipo = 'Credito' THEN ISNULL(SUM(a.haber), 0) - ISNULL(SUM(a.debe), 0)
		            ELSE 0
		        END AS saldo
		    FROM AsientosContables a
		    JOIN CuentasContables cc ON a.cuenta_contable = cc.codigo
		    WHERE a.estado = 'Activo'
		      AND (cc.clasificacion LIKE 'Activo%' 
		           OR cc.clasificacion LIKE 'Pasivo%' 
		           OR cc.clasificacion = 'Patrimonio')
		    GROUP BY cc.codigo, cc.descripcion, cc.clasificacion, cc.tipo
		    ORDER BY cc.codigo
		""", nativeQuery = true)
		List<Object[]> findBalanceGeneral();

	@Query(value = """
		    SELECT 
		        cc.codigo,
		        cc.descripcion,
		        cc.clasificacion,
		        CASE 
		            WHEN cc.tipo = 'Debito'  THEN ISNULL(SUM(a.debe), 0) - ISNULL(SUM(a.haber), 0)
		            WHEN cc.tipo = 'Credito' THEN ISNULL(SUM(a.haber), 0) - ISNULL(SUM(a.debe), 0)
		            ELSE 0
		        END AS saldo
		    FROM AsientosContables a
		    JOIN CuentasContables cc ON a.cuenta_contable = cc.codigo
		    WHERE a.estado = 'Activo'
		    GROUP BY cc.codigo, cc.descripcion, cc.clasificacion, cc.tipo
		    ORDER BY cc.codigo
		""", nativeQuery = true)
	List<Object[]> findTodosSaldos();
}