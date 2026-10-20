package com.munaqfit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Todo lo que el frontend necesita para montar el formulario embebido de
 * Izipay: el token de sesion (authorization), la llave RSA publica del
 * comercio, el merchantCode y la URL del SDK a cargar.
 *
 * No se expone ninguna credencial secreta: el token de sesion es de un solo
 * uso y la llave RSA es publica por diseno del proveedor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IzipayTokenResponse {

    /** tokenGUID devuelto por Izipay: se pasa a LoadForm como "authorization". */
    private String authorization;

    /** Llave RSA publica del comercio (keyRSA de LoadForm). */
    private String keyRSA;

    /** Codigo del comercio (merchantCode del config del SDK). */
    private String merchantCode;

    /** URL del script del checkout embebido (sandbox o produccion). */
    private String scriptUrl;

    /** Moneda del cobro: siempre PEN. */
    private String currency;

    /** Monto en formato texto con 2 decimales (ej. "25.90"). */
    private String amount;

    /** Numero de pedido, usado como numero de orden. */
    private String orderNumber;
}
