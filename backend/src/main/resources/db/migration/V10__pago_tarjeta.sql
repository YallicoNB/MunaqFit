-- Etapa 3, P1 (Izipay): el cobro con tarjeta necesita su propio valor en el
-- ENUM de pago.tipo_pago. El baseline V1 solo preveia los metodos manuales.
ALTER TABLE pago
    MODIFY COLUMN tipo_pago ENUM('EFECTIVO','YAPE','PLIN','TRANSFERENCIA','QR','TARJETA') NOT NULL;
