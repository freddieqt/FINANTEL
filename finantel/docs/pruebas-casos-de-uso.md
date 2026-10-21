# Matriz de pruebas de los casos de uso

## Alcance y ejecución

La matriz define seis niveles por caso de uso: componente/unitaria, equivalencia,
valor límite, integración, sistema y aceptación. Los resultados esperados se basan
en el comportamiento implementado actualmente y deben contrastarse con los
requisitos funcionales aprobados.

Las pruebas automatizadas se ejecutan con:

```powershell
.\gradlew.bat test
```

Las pruebas parametrizadas de equivalencia y valores límite recorren CU01-CU22.
La suite HTTP de `CasosDeUsoSistemaAceptacionIT` usa `FINANTEL_TEST` y ejecuta
pruebas de integración, sistema y aceptación para los 22 casos mediante MockMvc.
CU16/CU17 sustituyen el repositorio porque `FINANTEL_TEST` no contiene la tabla
`RespaldosBD`; CU19 simula Tesseract y CU22 simula el cliente de tipo de cambio.
Por ello, esas pruebas verifican los flujos locales, pero no la disponibilidad
de esos servicios o infraestructura externos.

## Casos de prueba

| CU | Componente / unitaria | Equivalencia | Valor límite | Integración | Sistema | Aceptación |
|---|---|---|---|---|---|---|
| CU01 Registrar asiento | Cuenta activa, tipo y moneda válidos: guardar y correlativo. | Tipo, moneda y descripción válidos frente a clases inválidas. | Debe/haber `0`, `0.01` y diferencia `0.01` frente a `0.02`. | POST `/asientos` persiste asiento y auditoría; rechazos no persisten. | GET `/asientos` devuelve JSON en la app levantada. | Registrar asiento balanceado y comprobarlo en respuesta/repositorio; casos `CU01RegistrarAsientoIT`. |
| CU02 Autenticar usuario | Credencial presente y estado activo/inactivo. | Usuario/password correctos frente a incorrectos o vacíos. | Email/password nulos o vacíos se rechazan sin token. | POST `/auth/login` contrasta usuario, encoder, JWT y auditoría. | Login inválido devuelve 401 y cuerpo de error. | Usuario válido recibe token con email y rol; inválido no inicia sesión. |
| CU03 Gestionar roles y permisos | Matriz rol-acción: administrador, contador, auditor y personal autorizado. | Acciones permitidas frente a acciones no permitidas. | Rol/acción nulos o desconocidos deniegan permiso. | Controlador de administración y `GestorPermisos`. | GET `/admin` carga la pantalla de administración. | Cada rol solo puede realizar sus operaciones autorizadas. |
| CU04 Registrar acción de usuario | Guarda usuario, acción, objeto, valores y fecha. | Valores anterior/nuevo presentes u opcionales. | Valores vacíos/nulos conservan el registro sin alterar el resto. | Servicio de auditoría y repositorio `AuditoriaAcciones`. | GET `/auditoria` devuelve listado JSON. | Ejecutar una operación y verificar que aparece atribuida al usuario correcto. |
| CU05 Inactivar asiento | Cambia estado y registra auditoría. | Asiento existente frente a inexistente; abierto frente a cerrado. | Periodo cerrado no permite inactivar. | PATCH `/inactivar-asiento/{id}` actualiza entidad persistida. | Endpoint devuelve éxito con asiento válido. | Inactivar y verificar estado `Inactivo`; asiento cerrado permanece sin cambios. |
| CU06 Duplicar asiento | Copia datos y asigna usuario/correlativo nuevo. | Asiento existente frente a ID inexistente. | Valores opcionales nulos y descripción larga no deben romper la copia. | GET `/web/asientos/duplicar/{id}` guarda la copia y audita. | Endpoint de duplicación opera sobre asiento persistido. | Copia aparece como nuevo asiento y mantiene periodo, cuenta, moneda y montos. |
| CU07 Clasificar asientos | Filtra por tipo y devuelve catálogo disponible. | Tipos existentes, inexistentes y variación de mayúsculas. | Tipo vacío o nulo produce una lista vacía, no un error inesperado. | Servicio consulta repositorio y controlador serializa resultados. | GET `/clasificar-asiento/tipos` devuelve el catálogo. | Elegir un tipo y ver únicamente asientos de esa clasificación. |
| CU08 Buscar asientos | Filtra por tipo, usuario o estado. | Coincidencia frente a búsqueda sin resultados. | Cadena vacía, espacios y caracteres acentuados se manejan explícitamente. | Servicio y métodos derivados del repositorio. | GET `/buscar-asiento/estado/Activo` devuelve JSON. | Buscar por filtro y comprobar que todos los resultados lo cumplen. |
| CU09 Detectar inconsistencias | Detecta asiento cuadrado/descuadrado y faltante. | Diferencia mayor/menor que tolerancia contable. | Diferencias `0.01` y `0.02`; montos nulos deben tratarse o rechazarse explícitamente. | Servicio consulta y transforma los asientos del repositorio. | GET `/inconsistencias` devuelve hallazgos. | Un asiento desbalanceado aparece con ID y detalle; uno cuadrado no aparece. |
| CU10 Validar uso de cuentas | Cuenta activa, inactiva y no encontrada. | Estados aceptados frente a no aceptados. | Código vacío, nulo, no numérico y de longitud extrema. | GET/POST `/validar-cuentas` y persistencia de cuenta. | Listado de cuentas activas y consulta por código responden. | Solo se permite registrar un asiento con una cuenta activa existente. |
| CU11 Generar estados financieros | Calcula categorías y totales de resultados/balance. | Filas de activo, pasivo, patrimonio, ingresos y gastos. | Resultado vacío, saldo cero, negativo o nulo no produce división inválida. | Consulta nativa SQL y servicio de estados. | GET `/estados-financieros/balance` devuelve las claves del reporte. | Totales del reporte coinciden con saldos contables del periodo de prueba. |
| CU12 Exportar reportes | Genera resultado para PDF/Excel según registros. | Reporte PDF y Excel; sin registros y con registros. | Cero registros devuelve conteo cero y formato desconocido se rechaza. | Servicio, factory y repositorio. | GET `/exportar-reportes/pdf` y `/excel` responden. | Usuario descarga/recibe el reporte solicitado con datos del libro. |
| CU13 Visualizar reportes gráficos | Suma debe/haber y agrupa por tipo. | Tipo definido frente a tipo ausente. | Sin asientos y saldos nulos producen cero, no error. | Servicio consulta repositorio y construye datos del gráfico. | GET `/reportes-graficos/resumen` entrega agregados. | Gráfico presenta conteos y totales que coinciden con los asientos fuente. |
| CU14 Importar Excel | Lee CSV/XLS/XLSX, guarda filas y registra auditoría. | Extensiones admitidas frente a no admitidas; archivo válido/inválido. | Archivo vacío, solo cabecera, fila incompleta y montos no numéricos. | Multipart, parser POI/CSV, repositorio y auditoría. | POST `/web/importar-excel` devuelve resultado estructurado. | Importar archivo y comprobar el total, preview y asientos guardados. |
| CU15 Registrar historial | Persiste campo, autor, valores y fecha. | Cambio con valores anteriores/nuevos y campos opcionales. | Cadenas vacías/nulas y texto largo. | POST/GET `/historial-cambios` y repositorio. | Listado HTTP devuelve los registros creados. | Cambio realizado aparece en historial con usuario y valores correctos. |
| CU16 Copia de seguridad | Genera metadatos y audita el respaldo. | Respaldo manual con usuario válido. | Usuario vacío o respuesta de persistencia fallida debe dar error explícito. | Servicio y tabla `RespaldosBD` (pendiente de tabla en FINANTEL_TEST). | GET `/copia-seguridad`; persistencia simulada en suite HTTP actual. | Crear respaldo y verlo disponible para restauración. |
| CU17 Restaurar base de datos | Resuelve punto existente o devuelve no encontrado. | Punto de restauración presente/ausente. | ID inválido, cero o negativo no restaura. | Servicio y tabla `RespaldosBD` (pendiente de tabla en FINANTEL_TEST). | GET `/restaurar-bd`; repositorio sustituido en suite HTTP actual. | Restaurar punto elegido y verificar que la aplicación confirma el punto. |
| CU18 Registrar usuario | Valida rol, password, email, DNI y teléfono; codifica password. | Email nuevo/duplicado; roles permitidos/no permitidos. | Password mínimo, DNI de 7/8/9 dígitos y teléfono de 8/9/10. | POST `/usuarios`, encoder, repositorio y auditoría. | GET `/usuarios` con rol administrador. | Registrar usuario y comprobar perfil activo con contraseña no reversible. |
| CU19 OCR documentos | Convierte imagen y devuelve texto reconocido. | Imagen compatible frente a archivo que no es imagen. | Archivo vacío, nombre vacío y tamaño máximo configurado. | Multipart, conversión de imagen y motor Tesseract. | POST `/ocr/leer` devuelve texto o error controlado. | Subir documento legible y comprobar que el texto reconocido es utilizable. |
| CU20 Conciliación bancaria | Suma movimientos/asientos y registra estado. | Totales conciliados frente a totales con diferencias. | Diferencia `0.009` y `0.01` alrededor de tolerancia. | POST `/conciliacion/comparar` consulta ambas fuentes y registra auditoría. | GET `/conciliacion` lista movimientos. | Conciliar extracto y verificar el resultado frente a los saldos del banco. |
| CU21 Generar libro contable | Devuelve asientos del libro. | Libro con asientos frente a libro vacío. | Primer/último día del periodo y fechas fuera del rango. | GET `/libro-contable` y repositorio contable. | Respuesta JSON del libro desde la aplicación. | Libro presenta los movimientos del periodo y conserva debe/haber. |
| CU22 Tipo de cambio SBS | Convierte respuesta externa a compra/venta y guarda fuente. | Respuesta válida frente a campos ausentes/no numéricos/HTTP fallido. | Valores cero, negativos, precisión decimal y respuesta vacía. | Cliente HTTP, almacenamiento y lectura; actualizar usa actualmente una URL SUNAT y fuente SBS. | GET `/web/tipocambio`; no se llama al servicio externo en suite. | Actualizar desde SBS, guardar fecha/compra/venta y mostrar el dato reciente. |

## Estado de la ejecución automatizada

- **Unitarias/componentes:** `CasosDeUsoUnitariosTest`, las pruebas específicas
  de OCR y tipo de cambio y `CasosDeUsoEquivalenciaLimitesTest` cubren la lógica
  de componentes de los CU implementados.
- **Equivalencia y valores límite:** `CasosDeUsoEquivalenciaLimitesTest` contiene
  un escenario de cada categoría para cada CU (44 ejecuciones parametrizadas).
- **Sistema y aceptación HTTP:** 22 casos de sistema y 22 de aceptación en
  `CasosDeUsoSistemaAceptacionIT`.
- **Integración:** 22 escenarios ejercitan rutas, servicios y persistencia o
  dependencias colaboradoras. CU01 valida SQL Server de pruebas; CU16/CU17 usan
  repositorios simulados por la tabla ausente y CU22 simula la llamada externa.
- **Dependencias externas:** OCR usa un motor simulado; el refresco de tipo de
  cambio no se ejecuta para evitar tráfico externo. Estos resultados no
  certifican la instalación local de Tesseract ni disponibilidad de SBS/SUNAT.
