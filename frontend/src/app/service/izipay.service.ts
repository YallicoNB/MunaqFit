import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api';

/** Datos que el backend devuelve para montar el formulario embebido. */
export interface TokenIzipay {
  authorization: string;
  keyRSA: string;
  merchantCode: string;
  scriptUrl: string;
  currency: string;
  amount: string;
  orderNumber: string;
}

/** Resultado del callback del SDK (Code "00" = aprobado). */
export interface ResultadoIzipay {
  Code?: string;
  Message?: string;
  Transactionid?: string;
}

/**
 * Integracion de pagos con Izipay (reparto 3 - Persona 1).
 *
 * El token de sesion lo genera el backend con las credenciales del
 * comercio; aqui solo lo pedimos, cargamos el SDK oficial y abrimos el
 * formulario embebido. El cobro se considera exitoso cuando el callback
 * trae Code "00".
 */
@Injectable({ providedIn: 'root' })
export class IzipayService {
  private apiUrl = `${API_URL}/izipay`;
  private cargandoScript: Promise<void> | null = null;

  constructor(private http: HttpClient) {}

  /** true si el comercio ya tiene credenciales Izipay configuradas. */
  configurado(): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/configuracion`);
  }

  /** Pide al backend el token de sesion de Izipay para este cobro. */
  tokenDeSesion(monto: number, pedido: string): Observable<TokenIzipay> {
    return this.http.post<TokenIzipay>(`${API_URL}/get-izipay-token`, {
      monto,
      pedido,
      descripcion: `Pedido ${pedido}`,
    });
  }

  /**
   * Abre el formulario seguro de Izipay (overlay del SDK). Resuelve con la
   * data del callback: Code "00" = pago aprobado.
   */
  async abrirFormulario(token: TokenIzipay): Promise<ResultadoIzipay> {
    await this.cargarScript(token.scriptUrl);

    const iziConfig = {
      transactionId: `${token.orderNumber}-${Date.now()}`,
      action: 'pay',
      merchantCode: token.merchantCode,
      order: {
        orderNumber: token.orderNumber,
        currency: token.currency,
        amount: token.amount,
        processType: 'AT',
        merchantBuyerId: '000',
        dateTimeTransaction: new Date().toISOString(),
      },
      billing: {
        firstName: 'Cliente',
        lastName: 'MunaqFit',
        email: 'cliente@munaqfit.pe',
        phoneNumber: '999999999',
        streetAddress: 'Av. Principal 123',
        city: 'Lima',
        country: 'PE',
      },
      shipping: {
        firstName: 'Cliente',
        lastName: 'MunaqFit',
        streetAddress: 'Av. Principal 123',
        city: 'Lima',
        country: 'PE',
        phoneNumber: '999999999',
        email: 'cliente@munaqfit.pe',
      },
    };

    return new Promise<ResultadoIzipay>((resolve, reject) => {
      try {
        const constructorIzipay = (window as unknown as { Izipay: new (opciones: unknown) => unknown }).Izipay;
        const checkout = new constructorIzipay({ config: iziConfig }) as {
          LoadForm: (opciones: unknown) => void;
        };
        checkout.LoadForm({
          authorization: token.authorization,
          keyRSA: token.keyRSA,
          callbackResponse: (data: ResultadoIzipay) => resolve(data),
        });
      } catch (error) {
        reject(error);
      }
    });
  }

  /** Carga el script del SDK una sola vez (si ya esta, reutiliza). */
  private cargarScript(url: string): Promise<void> {
    if ((window as unknown as { Izipay?: unknown }).Izipay) {
      return Promise.resolve();
    }
    if (this.cargandoScript) {
      return this.cargandoScript;
    }
    this.cargandoScript = new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => {
        this.cargandoScript = null;
        reject(new Error('No se pudo cargar el formulario de Izipay. Revisa tu conexion.'));
      };
      document.body.appendChild(script);
    });
    return this.cargandoScript;
  }
}
