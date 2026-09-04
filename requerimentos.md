# Documento de Requisitos - Munaq Fit Manager

---

## 1. Introducción

### 1.1 Contexto del Negocio

**Munaq Fit** es una tienda especializada en la venta de bebidas saludables ubicada en Av. Independencia 198, Ate. El negocio ofrece una variedad de batidos funcionales como Atomic, Detox, Relax, Pink Protein, Hydrate y Golden Glow, todos con un precio unitario de S/ 16.00. Adicionalmente, ofrecen productos de comida como Classic, Cheese, Pizza y opciones con base de proteína (Manjar, Queso, Chocolate).

Actualmente, el negocio maneja todas sus operaciones de forma manual:

- **Toma de pedidos:** En notas de papel, anotando el producto y número de mesa.
- **Control de inventario:** Mediante anotaciones en cuaderno y estadísticas de Easy Pay.
- **Capacitación de empleados:** Verbal y con fichas técnicas en papel.
- **Fidelización de clientes:** Sistema manual de "décima visita gratis".
- **Control de proveedores:** Seguimiento visual y pesaje manual.

### 1.2 Problemas Identificados

| Problema | Impacto |
|:---|:---|
| **Pedidos en papel** | Pérdida de notas, errores en la toma de pedidos, lentitud en horas pico |
| **Inventario sin control** | Desabastecimiento de insumos críticos, pérdida por vencimiento |
| **Capacitación verbal** | Inconsistencia en la preparación de batidos |
| **Fidelización manual** | Dificultad para hacer seguimiento de clientes frecuentes |
| **Estadísticas limitadas** | Dependencia de Easy Pay, sin visión unificada |

### 1.3 Objetivo del Sistema

Desarrollar un sistema web integral que permita a Munaq Fit:

1. **Digitalizar la toma de pedidos** para agilizar la atención en local.
2. **Automatizar el control de inventario** con alertas de stock crítico.
3. **Estandarizar las recetas** mediante fichas técnicas digitales.
4. **Gestionar la fidelización** de clientes con sistema de visitas.
5. **Generar reportes** de ventas, ranking de productos y valorización.
6. **Separar roles** de Administrador y Empleado para control de acceso.

### 1.4 Alcance del Sistema

**Incluye:**

- Login y autenticación con DNI y contraseña
- Dashboard diferenciado para Admin y Empleado
- Módulo de Ventas (toma de pedidos)
- Módulo de Órdenes (historial)
- Módulo de Menú con recetas
- Módulo de Inventario (solo Admin)
- Módulo de Usuarios (solo Admin)
- Módulo de Pagos (efectivo, Yape, Plin, transferencia)
- Módulo de Fidelización (visitas y promociones)
- Módulo de Reportes

**No incluye:**

- Pasarela de pagos real (simulación)
- Delivery o integración con apps externas
- Aplicación móvil nativa
- Integración con Easy Pay

---

## 2. Glosario

| Término | Definición |
|:---|:---|
| **Administrador** | Usuario con acceso total al sistema. Puede crear empleados, gestionar inventario y ver todos los reportes. Generalmente es el dueño del negocio (Elvis). |
| **Bebida** | Producto final que vende Munaq Fit. Ejemplos: Atomic, Detox, Relax, Pink Protein, Hydrate, Golden Glow. |
| **Caja** | Registro de apertura y cierre de turno, con control de ingresos del día. |
| **Cliente Frecuente** | Cliente registrado en el sistema de fidelización que acumula visitas para obtener beneficios. |
| **Detalle de Venta** | Línea individual de una venta que especifica qué bebida se vendió, en qué cantidad y a qué precio. |
| **Empleado** | Usuario con acceso limitado al sistema. Puede tomar pedidos, ver órdenes y consultar recetas. |
| **Ficha Técnica** | Documento digital que contiene la receta de una bebida: ingredientes, cantidades, pasos de preparación e información nutricional. |
| **Insumo** | Producto o ingrediente utilizado en la preparación de bebidas. Ejemplos: leche, proteína, frutas, miel. |
| **Kardex** | Registro histórico de todos los movimientos de inventario (entradas y salidas) de cada insumo. |
| **Mesa** | Número asignado a una mesa del local donde se sienta el cliente. |
| **Movimiento de Inventario** | Registro de entrada o salida de un insumo, con fecha, cantidad, stock anterior y nuevo. |
| **Orden de Compra** | Pedido realizado a un proveedor para reabastecer insumos. |
| **Pedido** | Solicitud de un cliente que puede contener una o más bebidas. |
| **Proveedor** | Empresa o persona que suministra insumos a Munaq Fit. |
| **Receta** | Conjunto de insumos y cantidades necesarias para preparar una bebida específica. |
| **Stock Crítico** | Nivel de stock por debajo del cual se considera que el insumo está en peligro de agotarse. |
| **Stock Mínimo** | Nivel de stock que activa una alerta de reabastecimiento. |
| **Venta** | Transacción comercial donde un cliente compra una o más bebidas. |
| **Visita** | Registro de una compra realizada por un cliente frecuente, que acumula para la promoción de "décima visita". |

---

## 3. Requisitos Funcionales

---

### 3.1 Módulo de Autenticación y Seguridad

---

#### REQ-001: Login con DNI

**Historia de Usuario:**
Como empleado o administrador de Munaq Fit, quiero iniciar sesión con mi número de DNI y contraseña, para acceder al sistema de forma segura y personalizada.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar una pantalla de login como primera vista al cargar la aplicación
2. EL formulario de login DEBE tener los campos:
   - DNI (texto, obligatorio)
   - Contraseña (password, obligatorio)
   - Botón "Iniciar Sesión"
3. EL DNI DEBE ser el identificador único del usuario (no se permiten nombres de usuario)
4. CUANDO un usuario ingresa DNI y contraseña válidos, EL sistema DEBE autenticar al usuario y redirigir al dashboard correspondiente (Admin o Empleado)
5. CUANDO un usuario ingresa DNI o contraseña inválidos, EL sistema DEBE mostrar el mensaje: "DNI o contraseña incorrectos"
6. EL sistema DEBE validar que el DNI tenga entre 8 y 12 caracteres (DNI o Carné de Extranjería)

**Prioridad:** ALTA
**Módulo:** Autenticación
**Rol:** PÚBLICO

---

#### REQ-002: Cifrado de Contraseñas

**Historia de Usuario:**
Como administrador del sistema, quiero que todas las contraseñas estén cifradas en la base de datos, para proteger la información de los empleados en caso de filtraciones.

**Criterios de Aceptación:**

1. EL sistema DEBE cifrar todas las contraseñas utilizando BCrypt
2. EL sistema DEBE almacenar en la base de datos únicamente el hash de la contraseña
3. EL sistema NUNCA DEBE almacenar contraseñas en texto plano
4. CUANDO un usuario inicia sesión, EL sistema DEBE comparar la contraseña ingresada con el hash almacenado usando BCrypt
5. EL sistema DEBE usar un salt aleatorio para cada contraseña

**Prioridad:** ALTA
**Módulo:** Autenticación
**Rol:** ADMIN

---

#### REQ-003: Límite de Intentos Fallidos

**Historia de Usuario:**
Como administrador del sistema, quiero limitar los intentos de inicio de sesión, para prevenir ataques de fuerza bruta y proteger las cuentas de los empleados.

**Criterios de Aceptación:**

1. EL sistema DEBE permitir un máximo de 3 intentos de inicio de sesión fallidos
2. CUANDO un usuario supera los 3 intentos fallidos, EL sistema DEBE bloquear el acceso por 5 minutos
3. CUANDO la cuenta está bloqueada, EL sistema DEBE mostrar el mensaje: "Demasiados intentos fallidos. Espere 5 minutos para intentar nuevamente"
4. CUANDO un usuario inicia sesión correctamente, EL sistema DEBE reiniciar el contador de intentos fallidos
5. EL sistema DEBE registrar en logs cada intento fallido de inicio de sesión

**Prioridad:** ALTA
**Módulo:** Autenticación
**Rol:** PÚBLICO

---

#### REQ-004: Recuperación de Contraseña

**Historia de Usuario:**
Como empleado, quiero poder recuperar mi contraseña si la olvido, para poder acceder nuevamente al sistema sin necesidad de contactar al administrador.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar un enlace "¿Olvidaste tu contraseña?" en la pantalla de login
2. CUANDO un usuario hace clic en el enlace, EL sistema DEBE mostrar un formulario para ingresar su correo electrónico registrado
3. SI el correo existe en la base de datos, EL sistema DEBE:
   - Generar un token único y seguro con expiración de 1 hora
   - Almacenar el token en la base de datos asociado al usuario
   - Mostrar en pantalla un mensaje con el enlace de recuperación (simulación, en producción se enviaría por email)
4. SI el correo NO existe en la base de datos, EL sistema DEBE mostrar un mensaje genérico: "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña"
5. EL enlace de recuperación DEBE llevar al usuario a una página donde pueda ingresar una nueva contraseña
6. CUANDO el usuario guarda la nueva contraseña, EL sistema DEBE:
   - Actualizar la contraseña en la base de datos con BCrypt
   - Invalidar el token de recuperación
   - Mostrar un mensaje de confirmación y redirigir al login
7. SI el token ha expirado o es inválido, EL sistema DEBE mostrar: "El enlace ha expirado o es inválido. Solicita un nuevo enlace de recuperación"

**Prioridad:** MEDIA
**Módulo:** Autenticación
**Rol:** PÚBLICO (Empleados/Admin)

---

#### REQ-005: Cierre de Sesión

**Historia de Usuario:**
Como empleado, quiero poder cerrar sesión al finalizar mi turno, para asegurar que nadie más use mi cuenta.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar un botón de "Cerrar Sesión" visible en todas las pantallas del dashboard
2. CUANDO un usuario hace clic en "Cerrar Sesión", EL sistema DEBE:
   - Invalidar el token JWT actual
   - Limpiar la sesión del navegador (localStorage/sessionStorage)
   - Redirigir a la pantalla de login
3. EL sistema DEBE mostrar un mensaje de confirmación: "Sesión cerrada exitosamente"
4. CUANDO un usuario intenta acceder a una ruta protegida sin sesión activa, EL sistema DEBE redirigir al login

**Prioridad:** ALTA
**Módulo:** Autenticación
**Rol:** EMPLEADO / ADMIN

---

### 3.2 Módulo de Gestión de Usuarios (Admin)

---

#### REQ-006: Crear Empleados

**Historia de Usuario:**
Como administrador, quiero registrar nuevos empleados en el sistema, para que puedan acceder al sistema y realizar sus funciones.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar una sección "Usuarios" accesible solo para Administradores
2. EL sistema DEBE mostrar un formulario para crear nuevos empleados con los campos:
   - Nombre completo (obligatorio)
   - DNI (obligatorio, único)
   - Correo electrónico (obligatorio, para recuperación de contraseña)
   - Contraseña (obligatorio, mínimo 8 caracteres)
   - Rol (fijo: EMPLEADO)
3. CUANDO el Administrador completa el formulario, EL sistema DEBE:
   - Validar que el DNI no exista ya en el sistema
   - Validar que el correo no exista ya en el sistema
   - Cifrar la contraseña con BCrypt
   - Guardar el nuevo empleado en la base de datos
4. CUANDO un empleado es creado exitosamente, EL sistema DEBE mostrar un mensaje: "Empleado registrado exitosamente"
5. SI el DNI o correo ya existen, EL sistema DEBE mostrar un mensaje de error específico

**Prioridad:** ALTA
**Módulo:** Usuarios
**Rol:** ADMIN

---

#### REQ-007: Listar Empleados

**Historia de Usuario:**
Como administrador, quiero ver una lista de todos los empleados registrados, para tener control del personal del negocio.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar una tabla con todos los empleados registrados
2. PARA CADA empleado, EL sistema DEBE mostrar:
   - DNI
   - Nombre completo
   - Correo electrónico
   - Estado (ACTIVO / INACTIVO)
   - Último inicio de sesión
3. EL sistema DEBE mostrar el estado actual de sesión del empleado (conectado / desconectado)
4. EL sistema DEBE permitir filtrar empleados por estado (ACTIVO / INACTIVO)
5. EL sistema DEBE permitir buscar empleados por DNI o nombre

**Prioridad:** ALTA
**Módulo:** Usuarios
**Rol:** ADMIN

---

#### REQ-008: Eliminar Empleados

**Historia de Usuario:**
Como administrador, quiero eliminar empleados del sistema en caso de despidos o renuncias, para mantener el control de acceso al sistema.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar un botón "Eliminar" para cada empleado en la lista
2. CUANDO el Administrador hace clic en "Eliminar", EL sistema DEBE mostrar una confirmación: "¿Está seguro de eliminar a [nombre]?"
3. SI el Administrador confirma, EL sistema DEBE:
   - Cambiar el estado del empleado a INACTIVO (soft delete)
   - Mantener el historial del empleado en el sistema
4. EL sistema DEBE mostrar un mensaje: "Empleado eliminado exitosamente"
5. EL sistema DEBE impedir que un empleado eliminado pueda iniciar sesión
6. EL sistema DEBE impedir que un Administrador se elimine a sí mismo

**Prioridad:** MEDIA
**Módulo:** Usuarios
**Rol:** ADMIN

---

#### REQ-009: Cambio de Contraseña (Auto-gestionado)

**Historia de Usuario:**
Como empleado, quiero poder cambiar mi contraseña sin necesidad del administrador, para mantener la seguridad de mi cuenta.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar una opción "Cambiar Contraseña" en el dashboard del empleado
2. EL formulario DEBE tener los campos:
   - Contraseña actual
   - Nueva contraseña (mínimo 8 caracteres)
   - Confirmar nueva contraseña
3. CUANDO el empleado ingresa la contraseña actual incorrecta, EL sistema DEBE mostrar: "Contraseña actual incorrecta"
4. CUANDO la nueva contraseña y la confirmación no coinciden, EL sistema DEBE mostrar: "Las contraseñas no coinciden"
5. CUANDO el empleado cambia exitosamente su contraseña, EL sistema DEBE:
   - Actualizar la contraseña en la base de datos con BCrypt
   - Invalidar cualquier sesión activa (forzar nuevo login)
   - Mostrar mensaje: "Contraseña actualizada exitosamente"

**Prioridad:** MEDIA
**Módulo:** Usuarios
**Rol:** EMPLEADO / ADMIN

---

### 3.3 Módulo de Ventas (Empleado)

---

#### REQ-010: Tomar Pedido

**Historia de Usuario:**
Como empleado de Munaq Fit, quiero tomar pedidos de clientes de manera rápida y digital, para evitar errores y agilizar la atención en horas pico.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar una pantalla de "Ventas" dividida en dos secciones:
   - **Izquierda:** Catálogo de bebidas con categorías (Energizante, Detox, Relajante, Proteico, Hidratante, Antiinflamatorio)
   - **Derecha:** Pedido actual (carrito de venta)
2. EL sistema DEBE mostrar cada bebida con su nombre, precio (S/ 16.00) e imagen
3. EL sistema DEBE incluir una barra de búsqueda para filtrar bebidas por nombre
4. CUANDO el empleado hace clic en una bebida, EL sistema DEBE agregarla al pedido (carrito)
5. CUANDO el empleado hace clic en el botón "+" o "-" en el carrito, EL sistema DEBE incrementar o decrementar la cantidad
6. CUANDO el empleado hace clic en "Eliminar" en el carrito, EL sistema DEBE eliminar la bebida del pedido
7. EL sistema DEBE mostrar el subtotal y total del pedido en tiempo real
8. EL sistema DEBE permitir agregar notas al pedido (ej. "sin azúcar", "con hielo")
9. EL sistema DEBE tener un campo para asignar número de mesa (opcional)
10. CUANDO el empleado confirma el pedido, EL sistema DEBE guardar la venta en la base de datos

**Prioridad:** ALTA
**Módulo:** Ventas
**Rol:** EMPLEADO / ADMIN

---

#### REQ-011: Ver Menú de Bebidas

**Historia de Usuario:**
Como empleado, quiero consultar el menú de bebidas con sus precios, para informar a los clientes sobre las opciones disponibles.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar el catálogo completo de bebidas en la sección de Ventas
2. EL sistema DEBE mostrar las bebidas organizadas por categorías
3. PARA CADA bebida, EL sistema DEBE mostrar:
   - Nombre
   - Precio (S/ 16.00)
   - Descripción breve (ej. "Saborear a naranja, shot de energía")
   - Imagen (opcional)
   - Opciones adicionales: "+ S/ 2.00 Conviértelo en Bubble Tea"
4. CUANDO el empleado pasa el mouse sobre una bebida, EL sistema DEBE mostrar un tooltip con información adicional
5. EL sistema DEBE permitir buscar bebidas por nombre

**Prioridad:** ALTA
**Módulo:** Ventas
**Rol:** EMPLEADO / ADMIN

---

#### REQ-012: Registrar Pago

**Historia de Usuario:**
Como empleado, quiero registrar el pago de un pedido, para completar la transacción con el cliente.

**Criterios de Aceptación:**

1. CUANDO el empleado confirma un pedido, EL sistema DEBE mostrar la ventana de pago
2. EL sistema DEBE mostrar el total a pagar
3. EL sistema DEBE permitir seleccionar el método de pago:
   - Efectivo
   - Yape
   - Plin
   - Transferencia bancaria
   - QR
4. PARA pago en EFECTIVO:
   - EL sistema DEBE mostrar un campo "Monto recibido"
   - EL sistema DEBE calcular y mostrar el vuelto automáticamente
   - EL sistema DEBE mostrar el vuelto en formato: "Vuelto: S/ X.XX"
5. PARA YAPE, PLIN o TRANSFERENCIA:
   - EL sistema DEBE mostrar un campo "Número de operación" (obligatorio)
   - EL sistema DEBE mostrar el campo "Banco" (para transferencias)
6. CUANDO el empleado confirma el pago, EL sistema DEBE:
   - Guardar la transacción en la tabla PAGO
   - Descontar el stock de insumos automáticamente
   - Registrar la visita del cliente (si aplica a fidelización)
   - Cambiar el estado de la venta a "PAGADO"
   - Mostrar mensaje: "Pago registrado exitosamente"
7. EL sistema DEBE generar un número de pedido único (formato: PED-{fecha}-{secuencia})

**Prioridad:** ALTA
**Módulo:** Ventas
**Rol:** EMPLEADO / ADMIN

---

#### REQ-013: Visualización de Productos en Ventas

**Historia de Usuario:**
Como empleado, quiero ver todos los productos disponibles (bebidas) de forma organizada, para poder tomar pedidos de manera eficiente.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar los productos organizados por secciones/categorías:
   - Bebidas Energizantes (Atomic, etc.)
   - Bebidas Detox
   - Bebidas Proteicas (Pink Protein)
   - Bebidas Relajantes (Relax)
   - Bebidas Hidratantes (Hydrate)
   - Bebidas Antiinflamatorias (Golden Glow)
   - Clásicos (Classic, Cheese, Pizza)
   - Bases (Base Manjar, Base Queso, Base Chocolate)
2. PARA CADA producto, EL sistema DEBE mostrar:
   - Nombre
   - Imagen (opcional)
   - Precio
   - Descripción corta
   - Indicador de disponibilidad
3. El sistema DEBE incluir una barra de búsqueda para encontrar productos rápidamente
4. CUANDO un producto no tiene stock, EL sistema DEBE mostrarlo visualmente (ej. "Agotado" o deshabilitado)

**Prioridad:** ALTA
**Módulo:** Ventas
**Rol:** EMPLEADO / ADMIN

---

### 3.4 Módulo de Órdenes

---

#### REQ-014: Ver Órdenes del Día (Empleado)

**Historia de Usuario:**
Como empleado, quiero ver todas las órdenes que he tomado durante mi turno, para hacer seguimiento de los pedidos y su estado.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar una sección "Órdenes" en el dashboard del empleado
2. EL sistema DEBE mostrar la lista de pedidos realizados por el empleado en el día actual
3. LA lista DEBE estar organizada cronológicamente (más reciente primero)
4. PARA CADA pedido, EL sistema DEBE mostrar:
   - Número de pedido
   - Hora del pedido
   - Mesa (si aplica)
   - Total
   - Estado (PENDIENTE / PAGADO / CANCELADO)
5. EL sistema DEBE mostrar el detalle del pedido al hacer clic en él (lado derecho)
6. EL detalle del pedido DEBE mostrar:
   - Todos los productos con sus cantidades y precios
   - Subtotal, IGV y total
   - Método de pago
   - Notas adicionales

**Prioridad:** ALTA
**Módulo:** Órdenes
**Rol:** EMPLEADO

---

#### REQ-015: Ver Todas las Órdenes (Admin)

**Historia de Usuario:**
Como administrador, quiero ver todas las órdenes de todos los empleados, para tener una visión completa de las ventas del negocio.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar una sección "Órdenes" en el dashboard del administrador
2. EL sistema DEBE mostrar la lista de TODOS los pedidos de TODOS los empleados
3. EL sistema DEBE mostrar pedidos de todos los días (no solo el día actual)
4. EL sistema DEBE permitir filtrar órdenes por:
   - Fecha (rango)
   - Empleado
   - Estado
5. PARA CADA pedido, EL sistema DEBE mostrar:
   - Número de pedido
   - Fecha y hora
   - Empleado que atendió
   - Mesa
   - Total
   - Estado
6. EL sistema DEBE mostrar el detalle del pedido al hacer clic en él (lado derecho)
7. EL sistema DEBE permitir cambiar el estado de un pedido (PENDIENTE → PAGADO → CANCELADO)

**Prioridad:** ALTA
**Módulo:** Órdenes
**Rol:** ADMIN

---

### 3.5 Módulo de Menú y Recetas

---

#### REQ-016: Visualizar Catálogo de Bebidas

**Historia de Usuario:**
Como empleado, quiero visualizar el catálogo completo de bebidas con sus precios, para poder ofrecer información precisa a los clientes.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar una sección "Menú" en el dashboard
2. EL sistema DEBE mostrar todas las bebidas disponibles con sus precios
3. EL sistema DEBE organizar las bebidas por categorías
4. PARA CADA bebida, EL sistema DEBE mostrar:
   - Nombre
   - Precio (S/ 16.00)
   - Descripción
   - Imagen (opcional)
   - Opciones adicionales (ej. "Conviértelo en Bubble Tea + S/ 2.00")
5. EL sistema DEBE incluir una barra de búsqueda para filtrar bebidas
6. EL sistema DEBE mostrar un indicador si una bebida no está disponible

**Prioridad:** ALTA
**Módulo:** Menú
**Rol:** EMPLEADO / ADMIN

---

#### REQ-017: Ver Receta de Bebida

**Historia de Usuario:**
Como empleado, quiero consultar la receta de cada bebida, para prepararla de manera estandarizada y consistente.

**Criterios de Aceptación:**

1. CUANDO el empleado hace clic en una bebida del menú, EL sistema DEBE mostrar los detalles de la receta
2. LA receta DEBE mostrar:
   - Lista de ingredientes con cantidades exactas
   - Pasos de preparación (instrucciones)
   - Tiempo de preparación estimado
   - Información nutricional (si está disponible)
   - Imagen de referencia del producto final
3. EL sistema DEBE mostrar un mensaje si la receta no tiene imagen o información nutricional
4. EL sistema DEBE permitir imprimir o ver la receta en formato de ficha técnica

**Prioridad:** ALTA
**Módulo:** Menú
**Rol:** EMPLEADO / ADMIN

---

#### REQ-018: Información Nutricional

**Historia de Usuario:**
Como empleado, quiero consultar la información nutricional de las bebidas, para informar a los clientes sobre el contenido de los productos.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar información nutricional en la vista de receta
2. PARA CADA bebida, EL sistema DEBE mostrar (si está disponible):
   - Calorías
   - Proteínas
   - Carbohidratos
   - Grasas
   - Fibra
   - Azúcares
   - Otros nutrientes relevantes
3. EL sistema DEBE mostrar la información nutricional en un formato claro y visual
4. EL sistema DEBE mostrar un mensaje: "Información nutricional no disponible" si no hay datos

**Prioridad:** MEDIA
**Módulo:** Menú
**Rol:** EMPLEADO / ADMIN

---

### 3.6 Módulo de Inventario (Admin)

---

#### REQ-019: Ver Stock Actual

**Historia de Usuario:**
Como administrador, quiero ver el stock actual de todos los insumos, para conocer el estado del inventario y planificar compras.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar una sección "Inventario" en el dashboard del administrador
2. EL sistema DEBE mostrar una tabla con todos los insumos y su stock actual
3. PARA CADA insumo, EL sistema DEBE mostrar:
   - Nombre del insumo
   - Categoría
   - Stock actual
   - Stock mínimo
   - Stock crítico
   - Unidad de medida (KG, G, L, ML, UNIDAD)
   - Última fecha de reabastecimiento
   - Fecha de caducidad (si aplica)
   - Proveedor
4. EL sistema DEBE resaltar visualmente los insumos con stock bajo (color amarillo)
5. EL sistema DEBE resaltar visualmente los insumos con stock crítico (color rojo)
6. EL sistema DEBE permitir buscar insumos por nombre o categoría

**Prioridad:** ALTA
**Módulo:** Inventario
**Rol:** ADMIN

---

#### REQ-020: Agregar Stock (Reabastecimiento)

**Historia de Usuario:**
Como administrador, quiero agregar stock a los insumos cuando llegan nuevas compras, para mantener el inventario actualizado.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar un formulario para agregar stock a un insumo
2. EL formulario DEBE tener los campos:
   - Insumo (seleccionable de la lista)
   - Cantidad a agregar
   - Fecha de caducidad (opcional)
   - Número de comprobante (opcional)
   - Proveedor (opcional)
3. CUANDO el administrador guarda el reabastecimiento, EL sistema DEBE:
   - Actualizar el stock actual del insumo
   - Registrar el movimiento en el Kardex (tipo: INGRESO)
   - Actualizar la última fecha de reabastecimiento
   - Mostrar mensaje: "Stock actualizado exitosamente"
4. EL sistema DEBE validar que la cantidad sea un número positivo
5. EL sistema DEBE registrar qué administrador realizó el reabastecimiento

**Prioridad:** ALTA
**Módulo:** Inventario
**Rol:** ADMIN

---

#### REQ-021: Alertas de Stock Crítico

**Historia de Usuario:**
Como administrador, quiero recibir alertas cuando el stock de un insumo está críticamente bajo, para evitar desabastecimiento.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar automáticamente una alerta cuando un insumo alcanza el stock crítico
2. LAS alertas DEBEN ser visibles en el dashboard del administrador (ej. en la parte superior)
3. EL sistema DEBE mostrar el número de insumos con stock crítico en un contador
4. CUANDO el administrador hace clic en la alerta, EL sistema DEBE filtrar el inventario para mostrar solo los insumos críticos
5. EL sistema DEBE mostrar una alerta cuando un insumo supera su fecha de caducidad

**Prioridad:** ALTA
**Módulo:** Inventario
**Rol:** ADMIN

---

#### REQ-022: Registro de Movimientos (Kardex)

**Historia de Usuario:**
Como administrador, quiero ver el historial de movimientos de inventario (Kardex), para auditar los cambios de stock y detectar inconsistencias.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar un registro histórico de todos los movimientos de inventario
2. PARA CADA movimiento, EL sistema DEBE mostrar:
   - Fecha y hora
   - Insumo
   - Tipo de movimiento (INGRESO / SALIDA)
   - Cantidad
   - Stock anterior
   - Stock nuevo
   - Motivo (ej. Venta, Reabastecimiento)
   - Usuario que realizó el movimiento
3. EL sistema DEBE permitir filtrar movimientos por:
   - Fecha (rango)
   - Insumo
   - Tipo de movimiento
   - Usuario
4. EL sistema DEBE permitir exportar el Kardex a CSV o Excel

**Prioridad:** MEDIA
**Módulo:** Inventario
**Rol:** ADMIN

---

### 3.7 Módulo de Fidelización

---

#### REQ-023: Registrar Clientes Frecuentes

**Historia de Usuario:**
Como empleado, quiero registrar a los clientes frecuentes en el sistema, para que puedan acumular visitas y obtener beneficios.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar un formulario para registrar clientes frecuentes
2. EL formulario DEBE tener los campos:
   - Nombre (obligatorio)
   - Teléfono (opcional)
   - Correo (opcional)
3. CUANDO el empleado registra un cliente, EL sistema DEBE:
   - Guardar el cliente en la tabla CLIENTE_FIDELIDAD
   - Inicializar el contador de visitas en 0
   - Establecer el umbral de premio en 10 visitas (configurable)
4. EL sistema DEBE mostrar el historial de visitas del cliente cuando se busca por nombre o teléfono

**Prioridad:** MEDIA
**Módulo:** Fidelización
**Rol:** EMPLEADO / ADMIN

---

#### REQ-024: Registrar Visitas

**Historia de Usuario:**
Como empleado, quiero registrar las visitas de los clientes frecuentes, para que acumulen puntos y puedan obtener beneficios.

**Criterios de Aceptación:**

1. CUANDO un cliente frecuente realiza una compra, EL sistema DEBE registrar una visita automáticamente
2. EL sistema DEBE incrementar el contador de visitas del cliente en 1
3. EL sistema DEBE registrar la visita en la tabla VISITA_CLIENTE (con fecha y venta asociada)
4. EL sistema DEBE permitir al empleado buscar un cliente por nombre o teléfono para registrarlo en la venta

**Prioridad:** MEDIA
**Módulo:** Fidelización
**Rol:** EMPLEADO

---

#### REQ-025: Décima Visita Gratis

**Historia de Usuario:**
Como cliente frecuente, quiero recibir un producto gratis al completar 10 visitas, para sentirme valorado y seguir comprando en Munaq Fit.

**Criterios de Aceptación:**

1. CUANDO un cliente alcanza 10 visitas (o el umbral configurado), EL sistema DEBE mostrar una notificación al empleado
2. EL sistema DEBE permitir al empleado aplicar un producto de cortesía en la siguiente compra del cliente
3. EL sistema DEBE resetear el contador de visitas a 0 después de aplicar el beneficio
4. EL sistema DEBE registrar en el historial del cliente cuándo recibió el beneficio
5. EL sistema DEBE permitir al administrador configurar el número de visitas para el premio (default: 10)

**Prioridad:** MEDIA
**Módulo:** Fidelización
**Rol:** EMPLEADO / ADMIN

---

### 3.8 Módulo de Proveedores

---

#### REQ-026: Registrar Proveedores

**Historia de Usuario:**
Como administrador, quiero registrar a los proveedores del negocio, para tener un control de quiénes suministran los insumos.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar un formulario para registrar proveedores
2. EL formulario DEBE tener los campos:
   - Nombre del proveedor (obligatorio)
   - RUC (opcional)
   - Teléfono (opcional)
   - Dirección (opcional)
   - Contacto (nombre del representante)
   - Estado del contrato (ACTIVO / INACTIVO)
   - Tipo de contrato (FIJO / VARIABLE)
3. CUANDO el administrador guarda un proveedor, EL sistema DEBE almacenarlo en la base de datos
4. EL sistema DEBE mostrar una lista de todos los proveedores registrados
5. EL sistema DEBE permitir buscar proveedores por nombre o RUC

**Prioridad:** MEDIA
**Módulo:** Proveedores
**Rol:** ADMIN

---

#### REQ-027: Registrar Pedidos a Proveedores

**Historia de Usuario:**
Como administrador, quiero registrar los pedidos realizados a proveedores, para llevar un control de las compras y el tiempo de entrega.

**Criterios de Aceptación:**

1. EL sistema DEBE proporcionar un formulario para registrar pedidos a proveedores
2. EL formulario DEBE tener los campos:
   - Proveedor (seleccionable)
   - Producto/Insumo
   - Cantidad
   - Precio
   - Fecha de pedido (fecha_llamada)
   - Fecha de llegada estimada (fecha_llegada)
   - Tiempo de demora estimado (días)
3. EL sistema DEBE mostrar una lista de todos los pedidos a proveedores
4. EL sistema DEBE permitir actualizar el estado del pedido (PENDIENTE / RECIBIDO / CANCELADO)

**Prioridad:** MEDIA
**Módulo:** Proveedores
**Rol:** ADMIN

---

#### REQ-028: Control de Entregas (Peso Neto/Bruto)

**Historia de Usuario:**
Como administrador, quiero verificar que los proveedores entreguen la cantidad exacta de insumos, para evitar pérdidas por diferencias de peso.

**Criterios de Aceptación:**

1. CUANDO llega un pedido de proveedor, EL sistema DEBE permitir registrar:
   - Peso bruto (con empaque)
   - Peso neto (sin empaque)
   - Cantidad recibida vs cantidad pedida
2. EL sistema DEBE mostrar una alerta si hay diferencia entre lo pedido y lo recibido
3. EL sistema DEBE registrar la fecha de recepción
4. EL sistema DEBE actualizar automáticamente el stock al registrar la recepción

**Prioridad:** MEDIA
**Módulo:** Proveedores
**Rol:** ADMIN

---

### 3.9 Módulo de Reportes (Admin)

---

#### REQ-029: Ranking de Bebidas Más Vendidas

**Historia de Usuario:**
Como administrador, quiero saber cuáles son las bebidas más vendidas, para tomar decisiones sobre el menú y promociones.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar un reporte con el ranking de bebidas más vendidas
2. EL reporte DEBE mostrar:
   - Posición
   - Nombre de la bebida
   - Cantidad vendida
   - Ingresos totales generados
   - Porcentaje del total de ventas
3. EL sistema DEBE permitir filtrar por rango de fechas (día, semana, mes, personalizado)
4. EL sistema DEBE mostrar un gráfico de barras con el ranking

**Prioridad:** MEDIA
**Módulo:** Reportes
**Rol:** ADMIN

---

#### REQ-030: Reporte de Ventas Diarias/Semanales/Mensuales

**Historia de Usuario:**
Como administrador, quiero ver reportes de ventas consolidados por período, para evaluar el desempeño del negocio.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar un reporte de ventas con:
   - Total de ventas
   - Total de ingresos
   - Promedio por día
   - Número de transacciones
   - Producto más vendido
2. EL sistema DEBE permitir seleccionar el período: DÍA, SEMANA, MES, PERSONALIZADO
3. EL sistema DEBE mostrar un gráfico de líneas con la evolución de ventas
4. EL sistema DEBE mostrar los ingresos desglosados por método de pago
5. EL sistema DEBE permitir exportar el reporte a PDF o Excel

**Prioridad:** ALTA
**Módulo:** Reportes
**Rol:** ADMIN

---

#### REQ-031: Alertas de Stock Bajo

**Historia de Usuario:**
Como administrador, quiero ver un reporte de insumos con stock bajo, para planificar compras y evitar desabastecimiento.

**Criterios de Aceptación:**

1. EL sistema DEBE mostrar un reporte con todos los insumos que están por debajo del stock mínimo
2. PARA CADA insumo, EL sistema DEBE mostrar:
   - Nombre
   - Stock actual
   - Stock mínimo
   - Días estimados de consumo (basado en ventas promedio)
   - Cantidad recomendada para reabastecer
3. EL sistema DEBE permitir exportar el reporte a PDF o Excel

**Prioridad:** ALTA
**Módulo:** Reportes
**Rol:** ADMIN

---

#### REQ-032: Valorización de Inventario

**Historia de Usuario:**
Como administrador, quiero conocer el valor total del inventario, para tener una visión financiera del negocio.

**Criterios de Aceptación:**

1. EL sistema DEBE calcular la valorización total del inventario (suma de stock_actual * costo_unitario)
2. EL sistema DEBE mostrar un desglose por categoría de insumo
3. EL sistema DEBE mostrar el valor del inventario por proveedor
4. EL sistema DEBE permitir ver la valorización en diferentes monedas (Soles)

**Prioridad:** BAJA
**Módulo:** Reportes
**Rol:** ADMIN

---

## 4. Requisitos No Funcionales

---

### 4.1 Seguridad

#### RNF-001: Cifrado de Contraseñas
- Todas las contraseñas DEBEN ser cifradas usando BCrypt con factor de costo 10
- NO se DEBEN almacenar contraseñas en texto plano en la base de datos

#### RNF-002: Autenticación con JWT
- El sistema DEBE usar JSON Web Tokens (JWT) para la autenticación
- Los tokens DEBEN tener una expiración de 24 horas
- Los tokens DEBEN incluir el rol del usuario (ADMIN / EMPLEADO)

#### RNF-003: Protección de Rutas
- Todas las rutas del dashboard DEBEN estar protegidas por AuthGuard
- Las rutas de administración DEBEN estar protegidas por RoleGuard (solo ADMIN)

#### RNF-004: CORS
- El backend DEBE configurar CORS para permitir peticiones desde el frontend (Angular en puerto 4200)

#### RNF-005: Logs de Seguridad
- El sistema DEBE registrar en logs todos los intentos de inicio de sesión (exitosos y fallidos)
- El sistema DEBE registrar en logs las acciones críticas (creación de usuarios, eliminación, cambios de stock)

---

### 4.2 Rendimiento

#### RNF-006: Tiempo de Respuesta
- EL sistema DEBE responder a las peticiones en menos de 2 segundos en condiciones normales
- LAS consultas a la base de datos DEBEN estar optimizadas con índices

#### RNF-007: Carga de Catálogo
- EL catálogo de bebidas DEBE cargar en menos de 1 segundo

#### RNF-008: Concurrencia
- EL sistema DEBE soportar al menos 5 empleados utilizando el sistema simultáneamente

---

### 4.3 Tecnológicos

#### RNF-009: Frontend
- Angular 17+ como framework
- TypeScript como lenguaje
- Bootstrap 5+ para estilos y responsividad
- RxJS para programación reactiva

#### RNF-010: Backend
- Java 17+ como lenguaje
- Spring Boot 3.2+ como framework
- Spring Security para autenticación y autorización
- JJWT para manejo de JWT

#### RNF-011: Base de Datos
- MySQL 8.0+ como motor de base de datos
- JPA/Hibernate para ORM

#### RNF-012: Control de Versiones
- Git para control de versiones
- Repositorio en GitHub

---

### 4.4 Usabilidad

#### RNF-013: Diseño Responsive
- EL sistema DEBE funcionar correctamente en:
  - Escritorio (1920x1080)
  - Laptop (1366x768)
  - Tablet (768x1024)
  - Móvil (375x667)
- Los formularios DEBEN adaptarse a pantallas táctiles

#### RNF-014: Interfaz Intuitiva
- LA interfaz DEBE ser intuitiva para empleados con conocimiento básico de tecnología
- Los botones DEBEN tener etiquetas claras y descriptivas
- Los mensajes de error DEBEN ser claros y accionables

#### RNF-015: Feedback al Usuario
- TODAS las acciones del usuario DEBEN tener feedback visual (loading, éxito, error)
- Los mensajes DEBEN mostrarse mediante notificaciones toast/alert

---

### 4.5 Disponibilidad

#### RNF-016: Tiempo de Actividad
- EL sistema DEBE estar disponible durante el horario de atención del negocio (9:00 AM - 10:00 PM)

#### RNF-017: Backup
- LA base de datos DEBE tener respaldos automáticos diarios

---

## 5. Modelo de Datos

### 5.1 Diagrama Entidad-Relación (DER)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              MUNAQ FIT MANAGER                             │
│                             MODELO DE DATOS                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐          ┌─────────────────┐          ┌─────────────────┐
│    USUARIO      │          │    BEBIDA       │          │   CATEGORIA     │
├─────────────────┤          ├─────────────────┤          ├─────────────────┤
│ id              │          │ id              │          │ id              │
│ dni             │          │ nombre          │          │ nombre          │
│ nombre_completo │          │ descripcion     │          │ descripcion     │
│ email           │          │ precio          │          └─────────────────┘
│ password        │          │ categoria       │                  ▲
│ rol             │◄─────────│ imagen_url      │                  │
│ estado          │          │ tiempo_preparaci│                  │
│ ultimo_login    │          │ activo          │                  │
│ fecha_creacion  │          └─────────────────┘                  │
└─────────────────┘                  ▲                            │
        ▲                            │                            │
        │                            │                            │
        │                     ┌──────┴──────┐                     │
        │                     │   RECETA    │                     │
        │                     ├─────────────┤                     │
        │                     │ id          │                     │
        │                     │ bebida_id   │─────────────────────│
        │                     │ producto_id │                     │
        │                     │ cantidad    │                     │
        │                     │ unidad      │                     │
        │                     │ instruccion │                     │
        │                     └──────┬──────┘                     │
        │                            │                            │
        │                            ▼                            │
        │                     ┌─────────────────┐          ┌──────┴──────┐
        │                     │   PRODUCTO      │          │  PROVEEDOR  │
        │                     ├─────────────────┤          ├─────────────┤
        │                     │ id              │          │ id          │
        │                     │ nombre          │          │ nombre      │
        │                     │ categoria_id    │─────────▶│ ruc         │
        │                     │ proveedor_id    │─────────▶│ telefono    │
        │                     │ stock_actual    │          │ direccion   │
        │                     │ stock_minimo    │          │ contacto    │
        │                     │ stock_critico   │          │ estado      │
        │                     │ unidad_medida   │          └─────────────┘
        │                     │ costo_unitario  │
        │                     │ precio_venta    │
        │                     │ fecha_caducidad │
        │                     └─────────────────┘
        │                            ▲
        │                            │
┌───────┴───────┐            ┌───────┴───────┐
│    VENTA      │            │MOV_INVENTARIO │
├───────────────┤            ├───────────────┤
│ id            │            │ id            │
│ usuario_id    │────────────│ producto_id   │
│ fecha_hora    │            │ tipo_movim    │
│ mesa          │            │ cantidad      │
│ tipo_venta    │            │ stock_anterior│
│ subtotal      │            │ stock_nuevo   │
│ igv           │            │ motivo        │
│ total         │            │ referencia_id │
│ estado        │            │ fecha_movim   │
└───────┬───────┘            │ usuario_id    │────────────►
        │                    └───────────────┘
        │
        ▼
┌───────────────┐
│ DETALLE_VENTA │
├───────────────┤
│ id            │
│ venta_id      │
│ bebida_id     │
│ cantidad      │
│ precio_unit   │
│ subtotal      │
└───────┬───────┘
        │
        ▼
┌───────────────┐          ┌─────────────────┐
│    PAGO       │          │ CLIENTE_FIDELIDAD│
├───────────────┤          ├─────────────────┤
│ id            │          │ id              │
│ venta_id      │          │ nombre          │
│ monto_total   │          │ telefono        │
│ monto_pagado  │          │ email           │
│ monto_cambio  │          │ visitas         │
│ tipo_pago     │          │ umbral_premio   │
│ num_operacion │          │ ultima_visita   │
│ fecha_pago    │          │ fecha_registro  │
│ estado        │          └─────────────────┘
└───────────────┘                  ▲
                                   │
                            ┌──────┴──────┐
                            │VISITA_CLIENTE│
                            ├──────────────┤
                            │ id           │
                            │ cliente_id   │
                            │ venta_id     │
                            │ fecha_visita │
                            └──────────────┘
```

### 5.2 Descripción de Tablas

#### USUARIO
Almacena todos los usuarios del sistema (Administradores y Empleados).

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| dni | VARCHAR(20) | DNI o Carné de Extranjería (único) |
| nombre_completo | VARCHAR(100) | Nombre completo del usuario |
| email | VARCHAR(100) | Correo electrónico (para recuperación) |
| password | VARCHAR(255) | Hash de la contraseña (BCrypt) |
| rol | ENUM | ADMIN / EMPLEADO |
| estado | ENUM | ACTIVO / INACTIVO |
| ultimo_login | TIMESTAMP | Fecha del último inicio de sesión |
| fecha_creacion | TIMESTAMP | Fecha de creación del usuario |

#### BEBIDA
Almacena los productos finales que vende Munaq Fit.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| nombre | VARCHAR(100) | Nombre de la bebida (ej. "Atomic") |
| descripcion | TEXT | Descripción breve (sabor, beneficios) |
| precio | DECIMAL(10,2) | Precio de venta (S/ 16.00) |
| categoria | VARCHAR(50) | Categoría de la bebida |
| imagen_url | VARCHAR(255) | URL de la imagen (opcional) |
| tiempo_preparacion | INT | Tiempo en minutos |
| activo | BOOLEAN | Disponible para la venta |

#### RECETA
Relaciona una bebida con los insumos necesarios para prepararla.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| bebida_id | BIGINT | Llave foránea a BEBIDA |
| producto_id | BIGINT | Llave foránea a PRODUCTO (insumo) |
| cantidad | DECIMAL(10,2) | Cantidad del insumo en la receta |
| unidad | VARCHAR(20) | Unidad de medida (g, ml, unidad) |
| paso_instrucciones | TEXT | Instrucciones de preparación |

#### PRODUCTO (Insumo)
Almacena los insumos/ingredientes utilizados en la preparación de bebidas.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| nombre | VARCHAR(100) | Nombre del insumo |
| categoria_id | BIGINT | Llave foránea a CATEGORIA |
| proveedor_id | BIGINT | Llave foránea a PROVEEDOR |
| stock_actual | DECIMAL(10,2) | Cantidad actual en inventario |
| stock_minimo | DECIMAL(10,2) | Nivel mínimo para alerta |
| stock_critico | DECIMAL(10,2) | Nivel crítico (alerta urgente) |
| unidad_medida | ENUM | KG / G / L / ML / UNIDAD |
| costo_unitario | DECIMAL(10,2) | Costo de compra por unidad |
| precio_venta | DECIMAL(10,2) | Precio de venta (opcional) |
| fecha_caducidad | DATE | Fecha de vencimiento (opcional) |

#### CATEGORIA
Clasifica los insumos.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| nombre | VARCHAR(50) | Nombre de la categoría |
| descripcion | TEXT | Descripción de la categoría |

#### PROVEEDOR
Almacena información de los proveedores de insumos.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| nombre | VARCHAR(100) | Nombre del proveedor |
| ruc | VARCHAR(20) | RUC del proveedor (opcional) |
| telefono | VARCHAR(20) | Teléfono de contacto |
| direccion | VARCHAR(200) | Dirección del proveedor |
| contacto_nombre | VARCHAR(100) | Nombre del contacto |
| estado | ENUM | ACTIVO / INACTIVO |

#### VENTA (Cabecera)
Registro principal de cada transacción de venta.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| usuario_id | BIGINT | Llave foránea a USUARIO (empleado) |
| fecha_hora | TIMESTAMP | Fecha y hora de la venta |
| mesa | INT | Número de mesa (opcional) |
| tipo_venta | ENUM | LOCAL / DELIVERY |
| subtotal | DECIMAL(10,2) | Subtotal sin IGV |
| igv | DECIMAL(10,2) | IGV (18%) |
| total | DECIMAL(10,2) | Total a pagar |
| estado | ENUM | PENDIENTE / PAGADO / CANCELADO |

#### DETALLE_VENTA (Líneas)
Detalle de cada producto vendido en una venta.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| venta_id | BIGINT | Llave foránea a VENTA |
| bebida_id | BIGINT | Llave foránea a BEBIDA |
| cantidad | INT | Cantidad vendida |
| precio_unitario | DECIMAL(10,2) | Precio unitario al momento de la venta |
| subtotal | DECIMAL(10,2) | Subtotal de la línea |

#### PAGO
Registro de los pagos asociados a una venta.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| venta_id | BIGINT | Llave foránea a VENTA |
| monto_total | DECIMAL(10,2) | Monto total de la venta |
| monto_pagado | DECIMAL(10,2) | Monto recibido (efectivo) |
| monto_cambio | DECIMAL(10,2) | Vuelto (efectivo) |
| tipo_pago | ENUM | EFECTIVO / YAPE / PLIN / TRANSFERENCIA / QR |
| numero_operacion | VARCHAR(50) | Número de operación (Yape/Plin/Transferencia) |
| fecha_pago | TIMESTAMP | Fecha del pago |
| estado | ENUM | PENDIENTE / COMPLETADO |

#### MOVIMIENTO_INVENTARIO (Kardex)
Registro histórico de todos los movimientos de inventario.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| producto_id | BIGINT | Llave foránea a PRODUCTO |
| tipo_movimiento | ENUM | INGRESO / SALIDA |
| cantidad | DECIMAL(10,2) | Cantidad del movimiento |
| stock_anterior | DECIMAL(10,2) | Stock antes del movimiento |
| stock_nuevo | DECIMAL(10,2) | Stock después del movimiento |
| motivo | VARCHAR(100) | Motivo del movimiento |
| referencia_id | BIGINT | ID de venta o compra relacionada |
| fecha_movimiento | TIMESTAMP | Fecha del movimiento |
| usuario_id | BIGINT | Llave foránea a USUARIO |

#### CLIENTE_FIDELIDAD
Almacena la información de los clientes frecuentes.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| nombre | VARCHAR(100) | Nombre del cliente |
| telefono | VARCHAR(20) | Teléfono (opcional) |
| email | VARCHAR(100) | Correo (opcional) |
| visitas | INT | Número de visitas acumuladas |
| umbral_premio | INT | Visitas necesarias para premio (default: 10) |
| ultima_visita | TIMESTAMP | Fecha de la última visita |
| fecha_registro | TIMESTAMP | Fecha de registro |

#### VISITA_CLIENTE
Registro de cada visita de un cliente frecuente.

| Campo | Tipo | Descripción |
|:---|:---|:---|
| id | BIGINT | Llave primaria, autoincrementable |
| cliente_fidelidad_id | BIGINT | Llave foránea a CLIENTE_FIDELIDAD |
| venta_id | BIGINT | Llave foránea a VENTA |
| fecha_visita | TIMESTAMP | Fecha de la visita |

---

## 6. Casos de Uso

### 6.1 Diagrama de Casos de Uso

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          MUNAQ FIT MANAGER                                 │
│                         CASOS DE USO                                       │
└─────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────────┐
                                    │  Iniciar Sesión │
                                    │   (REQ-001)     │
                                    └────────┬────────┘
                                             │
                                    ┌─────────▼─────────┐
                                    │   Sistema MUNAQ   │
                                    │    FIT MANAGER    │
                                    └─────────┬─────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────────┐
                    │                         │                             │
                    ▼                         ▼                             ▼
          ┌──────────────────┐     ┌──────────────────┐          ┌──────────────────┐
          │  EMPLEADO        │     │  ADMINISTRADOR   │          │  SISTEMA         │
          └──────────────────┘     └──────────────────┘          └──────────────────┘
                    │                         │                             │
    ┌───────────────┼───────────────┐         │                             │
    │               │               │         │                             │
    ▼               ▼               ▼         ▼                             ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐          ┌──────────────────┐
│Tomar     │ │Ver       │ │Consultar │ │Gestionar     │          │Registrar         │
│Pedido    │ │Órdenes   │ │Menú      │ │Inventario    │          │Movimiento de     │
│(REQ-010) │ │(REQ-014) │ │(REQ-016) │ │(REQ-019)     │          │Inventario (Kardex)│
└──────────┘ └──────────┘ └──────────┘ └──────────────┘          │(REQ-022)         │
    │               │               │         │                  └──────────────────┘
    │               │               │         │
    ▼               ▼               ▼         ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐          ┌──────────────────┐
│Registrar │ │Ver       │ │Ver       │ │Gestionar     │          │Generar           │
│Pago      │ │Detalle   │ │Receta    │ │Proveedores   │          │Reportes          │
│(REQ-012) │ │Orden     │ │(REQ-017) │ │(REQ-026)     │          │(REQ-029-032)     │
└──────────┘ └──────────┘ └──────────┘ └──────────────┘          └──────────────────┘
    │                                                                │
    ▼                                                                ▼
┌──────────┐          ┌──────────────────┐          ┌──────────────────┐
│Calcular  │          │Gestionar         │          │Registrar         │
│Vuelto    │          │Usuarios          │          │Clientes          │
│(REQ-012) │          │(REQ-006-008)     │          │Frecuentes        │
└──────────┘          └──────────────────┘          │(REQ-023)         │
                                                    └──────────────────┘
                                                             │
                                                             ▼
                                                    ┌──────────────────┐
                                                    │Registrar        │
                                                    │Visitas          │
                                                    │(REQ-024)        │
                                                    └──────────────────┘
                                                             │
                                                             ▼
                                                    ┌──────────────────┐
                                                    │Aplicar          │
                                                    │Décima Visita    │
                                                    │(REQ-025)        │
                                                    └──────────────────┘
```

### 6.2 Actores

| Actor | Descripción |
|:---|:---|
| **Empleado** | Usuario del sistema que toma pedidos, registra pagos y consulta el menú. No tiene acceso a la gestión de inventario ni usuarios. |
| **Administrador** | Usuario con control total del sistema. Puede gestionar empleados, inventario, proveedores y ver todos los reportes. |
| **Sistema** | Actor automático que ejecuta procesos como descuento de stock, registro de movimientos (Kardex) y generación de reportes. |

### 6.3 Descripción de Casos de Uso

| ID | Caso de Uso | Actor Principal | Descripción |
|:---|:---|:---|:---|
| CU-01 | Iniciar Sesión | Empleado/Admin | El usuario ingresa su DNI y contraseña para acceder al sistema |
| CU-02 | Tomar Pedido | Empleado/Admin | El usuario selecciona bebidas del catálogo y arma un pedido |
| CU-03 | Registrar Pago | Empleado/Admin | El usuario registra el pago del cliente (efectivo, Yape, Plin) |
| CU-04 | Calcular Vuelto | Empleado/Admin | El sistema calcula automáticamente el vuelto para pagos en efectivo |
| CU-05 | Ver Órdenes | Empleado/Admin | El usuario visualiza el historial de pedidos |
| CU-06 | Consultar Menú | Empleado/Admin | El usuario visualiza el catálogo de bebidas y recetas |
| CU-07 | Gestionar Inventario | Admin | El usuario actualiza el stock de insumos y ve alertas |
| CU-08 | Registrar Movimiento | Admin/Sistema | El sistema registra automáticamente cada movimiento de stock en el Kardex |
| CU-09 | Gestionar Usuarios | Admin | El usuario crea, lista o elimina empleados |
| CU-10 | Gestionar Proveedores | Admin | El usuario registra y administra proveedores |
| CU-11 | Registrar Cliente Frecuente | Empleado/Admin | El usuario registra a un cliente en el programa de fidelización |
| CU-12 | Registrar Visita | Empleado/Admin | El usuario registra una compra de un cliente frecuente |
| CU-13 | Aplicar Décima Visita | Empleado/Admin | El sistema aplica el beneficio de la décima visita gratis |
| CU-14 | Generar Reportes | Admin | El usuario genera reportes de ventas, ranking y stock |
| CU-15 | Cerrar Sesión | Empleado/Admin | El usuario finaliza su sesión en el sistema |

---

## 7. Anexos

### 7.1 Links

- **Figma (Prototipo):** [https://www.figma.com/design/Vj8R3Xdtw6Q0121bC870zC/Sin-t%C3%ADtulo](https://www.figma.com/design/Vj8R3Xdtw6Q0121bC870zC/Sin-t%C3%ADtulo?node-id=0-1&t=I55rtICYNDuEEnRK-1)
- **Repositorio GitHub:** (pendiente)
- **Documentación Técnica:** (pendiente)

### 7.2 Tecnologías Propuestas

| Capa | Tecnología | Versión |
|:---|:---|:---|
| Frontend | Angular | 17+ |
| Frontend | TypeScript | 5+ |
| Frontend | Bootstrap | 5.3+ |
| Backend | Java | 17+ |
| Backend | Spring Boot | 3.2+ |
| Backend | Spring Security | 6.2+ |
| Backend | JJWT | 0.12+ |
| Backend | BCrypt | 0.10+ |
| Base de Datos | MySQL | 8.0+ |
| Control de Versiones | Git | - |

### 7.3 Versiones del Documento

| Versión | Fecha | Autor | Cambios |
|:---|:---|:---|:---|
| 1.0 | 2026-09-03 | Equipo Munaq Fit | Creación del documento |
