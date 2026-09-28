import { Component, computed, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Navbar } from '../../layouts/navbar/navbar';
import { VentaService } from '../../service/venta.service';
import { ToastService } from '../../shared/toast/toast.service';
import { Bebida } from '../../models/producto';
import { PagoRequest, PagoRespuesta, Venta, VentaRequest } from '../../models/venta';

/** Linea del carrito: la bebida y cuantas unidades lleva el pedido. */
interface CarritoItem {
  bebida: Bebida;
  cantidad: number;
}

type TipoVenta = 'LOCAL' | 'DELIVERY';
type TipoPago = 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TRANSFERENCIA' | 'QR';
type EstadoCobro = 'espera' | 'procesando' | 'exito' | 'error';

@Component({
  selector: 'app-ventas',
  imports: [CommonModule, FormsModule, Navbar],
  templateUrl: './ventas.html',
  styleUrl: './ventas.css',
})
export class Ventas implements OnInit {
  /** La tasa la fija el parametro IGV en la BD; aqui solo se usa para la
   *  vista previa del carrito. El total definitivo lo calcula el backend. */
  private readonly TASA_IGV = 0.18;

  cargando = true;
  errorCarga = '';
  bebidas: Bebida[] = [];

  // ---- Carrito (estado reactivo con signals) ----
  private readonly _items = signal<CarritoItem[]>([]);
  readonly carrito = this._items.asReadonly();
  readonly cantidadTotal = computed(() => this.carrito().reduce((s, i) => s + i.cantidad, 0));
  readonly subtotal = computed(() =>
    this.carrito().reduce((suma, item) => suma + item.bebida.precio * item.cantidad, 0)
  );
  readonly igv = computed(() => this.subtotal() * this.TASA_IGV);
  readonly total = computed(() => this.subtotal() + this.igv());

  // ---- Datos del pedido ----
  mesa: number | null = null;
  tipoVenta: TipoVenta = 'LOCAL';
  notas = '';

  // ---- Cobro (modal) ----
  cobroAbierto = false;
  estadoCobro: EstadoCobro = 'espera';
  tipoPago: TipoPago = 'EFECTIVO';
  montoRecibido: number | null = null;
  mensajeError = '';
  ventaCobrada: Venta | null = null;
  pagoCobrado: PagoRespuesta | null = null;
  readonly metodosDePago: TipoPago[] = ['EFECTIVO', 'YAPE', 'PLIN', 'QR', 'TRANSFERENCIA'];

  constructor(
    private ventaService: VentaService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarMenu();
  }

  cargarMenu(): void {
    this.cargando = true;
    this.ventaService.menu().subscribe({
      next: (menu) => {
        this.bebidas = menu.filter((b) => b.activo);
        this.cargando = false;
      },
      error: () => {
        this.errorCarga = 'No se pudo cargar el menu de bebidas';
        this.cargando = false;
        this.toast.error(this.errorCarga);
      },
    });
  }

  // ===================== Carrito =====================

  agregar(bebida: Bebida): void {
    this._items.update((items) => {
      const existente = items.find((i) => i.bebida.id === bebida.id);
      if (existente) {
        return items.map((i) =>
          i.bebida.id === bebida.id ? { ...i, cantidad: i.cantidad + 1 } : i
        );
      }
      return [...items, { bebida, cantidad: 1 }];
    });
  }

  aumentar(id: number): void {
    this._items.update((items) =>
      items.map((i) => (i.bebida.id === id ? { ...i, cantidad: i.cantidad + 1 } : i))
    );
  }

  disminuir(id: number): void {
    this._items.update((items) =>
      items
        .map((i) => (i.bebida.id === id ? { ...i, cantidad: i.cantidad - 1 } : i))
        .filter((i) => i.cantidad > 0)
    );
  }

  quitarLinea(id: number): void {
    this._items.update((items) => items.filter((i) => i.bebida.id !== id));
  }

  vaciar(): void {
    if (!this.carrito().length) return;
    this._items.set([]);
    this.toast.info('El pedido se descarto', 'Carrito');
  }

  // ===================== Cobro (modal) =====================

  elegirMetodo(metodo: TipoPago): void {
    this.tipoPago = metodo;
    // Los pagos digitales son por el monto exacto; solo el efectivo pide vuelto.
    if (metodo !== 'EFECTIVO') {
      this.montoRecibido = this.redondear(this.total());
    }
  }

  abrirCobro(): void {
    if (!this.carrito().length) {
      this.toast.warning('Agrega al menos una bebida al pedido');
      return;
    }
    this.cobroAbierto = true;
    this.estadoCobro = 'espera';
    this.mensajeError = '';
    this.montoRecibido = this.redondear(this.total());
    this.ventaCobrada = null;
    this.pagoCobrado = null;
  }

  cerrarCobro(): void {
    // Un cobro en proceso no se interrumpe con un clic accidental.
    if (this.estadoCobro === 'procesando') return;
    this.cobroAbierto = false;
  }

  /** Vista previa del vuelto mientras el empleado tipea el efectivo. */
  vueltoPrevisto(): number {
    if (this.tipoPago !== 'EFECTIVO' || this.montoRecibido == null) return 0;
    const vuelto = this.montoRecibido - this.total();
    return vuelto > 0 ? vuelto : 0;
  }

  confirmarPago(): void {
    if (!this.carrito().length) return;
    this.estadoCobro = 'procesando';
    this.mensajeError = '';

    const request: VentaRequest = {
      venta: {
        mesa: this.mesa != null && !Number.isNaN(this.mesa) ? this.mesa : undefined,
        tipoVenta: this.tipoVenta,
        notas: this.notas.trim() || undefined,
      },
      detalles: this.carrito().map((item) => ({
        bebida: { id: item.bebida.id },
        cantidad: item.cantidad,
      })),
    };

    // 1) El backend registra el pedido y descuenta el stock.
    this.ventaService.registrar(request).subscribe({
      next: (venta) => {
        this.ventaCobrada = venta;
        this.toast.success(`Pedido ${venta.numeroPedido} registrado`, 'Pedido');
        this.cobrarVenta(venta);
      },
      error: (error) => this.fallarCobro(error),
    });
  }

  /** 2) Con la venta registrada se cobra (la pasa a PAGADO). */
  private cobrarVenta(venta: Venta): void {
    const montoPagado =
      this.tipoPago === 'EFECTIVO'
        ? this.montoRecibido ?? 0
        : this.redondear(venta.total);

    const pago: PagoRequest = {
      ventaId: venta.id,
      montoPagado,
      tipoPago: this.tipoPago,
    };

    this.ventaService.registrarPago(pago).subscribe({
      next: (resultado) => {
        this.pagoCobrado = resultado;
        this.estadoCobro = 'exito';
        this.vaciarSilenciosamente();
        this.toast.success(`Pago cobrado: S/ ${resultado.montoTotal.toFixed(2)}`, 'Cobrado');
        if (resultado.vuelto > 0) {
          this.toast.info(`Vuelto para el cliente: S/ ${resultado.vuelto.toFixed(2)}`, 'Vuelto');
        }
      },
      error: (error) => this.fallarCobro(error),
    });
  }

  private fallarCobro(error: Error | HttpErrorResponse): void {
    this.estadoCobro = 'error';
    this.mensajeError = this.mensajeDe(error);
    if (error instanceof HttpErrorResponse && error.status === 409) {
      this.toast.warning('La operacion choco con otra simultanea. Reintenta.', 'Reintenta');
    } else {
      this.toast.error(this.mensajeError, 'Cobro');
    }
  }

  /** Reinicia el modal y deja la caja lista para otro cliente. */
  nuevoPedido(): void {
    this.ventaCobrada = null;
    this.pagoCobrado = null;
    this.estadoCobro = 'espera';
    this.mensajeError = '';
    this.notas = '';
    this.mesa = null;
    this.cobroAbierto = false;
  }

  private vaciarSilenciosamente(): void {
    this._items.set([]);
  }

  // ===================== Utilidades =====================

  /** trackBy del carrito: Angular solo redibuja la linea que cambio. */
  rastrearLinea(_: number, item: CarritoItem): number {
    return item.bebida.id;
  }

  /** Extrae el mensaje de un error de API (JSON del handler o texto plano). */
  private mensajeDe(error: Error | HttpErrorResponse): string {
    if (error instanceof HttpErrorResponse) {
      const cuerpo = error.error;
      if (cuerpo && typeof cuerpo === 'object' && 'message' in cuerpo) {
        return String((cuerpo as { message: unknown }).message);
      }
      return String(cuerpo ?? error.message);
    }
    return error.message;
  }

  private redondear(monto: number): number {
    return Math.round(monto * 100) / 100;
  }
}