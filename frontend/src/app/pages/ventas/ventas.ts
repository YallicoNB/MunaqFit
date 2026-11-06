import { Component, computed, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Card } from '../../components/card/card';
import { VentaService } from '../../service/venta.service';
import { IzipayService } from '../../service/izipay.service';
import { ToastService } from '../../shared/toast/toast.service';
import { Bebida } from '../../models/producto';
import { PagoRequest, PagoRespuesta, Venta, VentaRequest } from '../../models/venta';

interface CarritoItem {
  bebida: Bebida;
  cantidad: number;
}

type TipoVenta = 'LOCAL' | 'DELIVERY';
type TipoPago = 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TRANSFERENCIA' | 'QR' | 'TARJETA';
type EstadoCobro = 'espera' | 'procesando' | 'exito' | 'error';

export interface ClienteFidelizado {
  id?: number;
  dni: string;
  nombre?: string;
  visitas: number;
}

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule, Card],
  templateUrl: './ventas.html',
  styleUrl: './ventas.css',
})
export class Ventas implements OnInit {
  private readonly TASA_IGV = 0.18;

  cargando = true;
  errorCarga = '';
  bebidas: Bebida[] = [];
  busquedaBebida = '';
  categoriaSeleccionada: string | null = null;
  filtrosAbiertos = false;

  get categorias(): string[] {
    return [...new Set(this.bebidas.map((bebida) => bebida.categoria.trim()).filter(Boolean))]
      .sort((a, b) => a.localeCompare(b, 'es'));
  }

  get bebidasFiltradas(): Bebida[] {
    const nombreBuscado = this.busquedaBebida.trim().toLocaleLowerCase('es');
    return this.bebidas.filter((bebida) => {
      const coincideNombre = bebida.nombre.toLocaleLowerCase('es').includes(nombreBuscado);
      const coincideCategoria =
        !this.categoriaSeleccionada ||
        bebida.categoria.trim() === this.categoriaSeleccionada;
      return coincideNombre && coincideCategoria;
    });
  }

  // ---- Fidelización de Clientes (REQ-023 / REQ-024 / REQ-025) ----
  dniCliente = '';
  clienteInfo: ClienteFidelizado | null = null;
  buscandoCliente = false;
  mensajeCliente = '';

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
  readonly mesas = [1, 2, 3, 4, 5, 6];
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
  readonly metodosDePago: TipoPago[] = ['EFECTIVO', 'YAPE', 'PLIN', 'QR', 'TRANSFERENCIA', 'TARJETA'];
  izipayConfigurado = false;

  constructor(
    private ventaService: VentaService,
    private izipayService: IzipayService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.cargarMenu();
    this.izipayService.configurado().subscribe({
      next: (configurado) => (this.izipayConfigurado = configurado),
      error: () => (this.izipayConfigurado = false),
    });
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

  // ===================== Fidelización (REQ-023, REQ-024, REQ-025) =====================

  buscarCliente(): void {
    const dni = this.dniCliente.trim();
    if (!dni || dni.length < 8) {
      this.toast.warning('Ingresa un DNI válido de 8 dígitos', 'Fidelización');
      return;
    }

    this.buscandoCliente = true;
    this.mensajeCliente = '';

    // Intenta buscar el cliente en el backend
    this.ventaService.buscarCliente?.(dni).subscribe({
      next: (cliente: any) => {
        this.clienteInfo = cliente;
        this.buscandoCliente = false;
        this.evaluarPremio10vaVisita(cliente.visitas);
      },
      error: () => {
        // Si no existe, preparamos la ficha para nuevo registro implícito al cobrar
        this.clienteInfo = { dni, visitas: 0 };
        this.buscandoCliente = false;
        this.toast.info('Cliente nuevo registrado para este pedido', 'Fidelización');
      },
    }) ?? this.simularClienteLocal(dni);
  }

  private simularClienteLocal(dni: string): void {
    // Fallback cuando la función no está expuesta en venta.service
    this.clienteInfo = { dni, visitas: 1 };
    this.buscandoCliente = false;
    this.evaluarPremio10vaVisita(this.clienteInfo.visitas);
  }

  private evaluarPremio10vaVisita(visitasActuales: number): void {
    // Si la visita actual es múltiplo de 10 o la 9.ª previa (esta compra completa la 10.ª)
    if ((visitasActuales + 1) % 10 === 0) {
      this.toast.info('🎉 ¡Este pedido corresponde a la 10.ª visita! Aplica bebida gratis.', 'Premio Fidelidad');
    }
  }

  limpiarCliente(): void {
    this.dniCliente = '';
    this.clienteInfo = null;
    this.mensajeCliente = '';
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
    this.limpiarCliente();
    this.toast.info('El pedido se descarto', 'Carrito');
  }

  cambiarTipoVenta(tipo: TipoVenta): void {
    this.tipoVenta = tipo;
    if (tipo === 'DELIVERY') {
      this.mesa = null;
    }
  }

  seleccionarMesa(mesa: number): void {
    this.mesa = this.mesa === mesa ? null : mesa;
  }

  seleccionarCategoria(categoria: string | null): void {
    this.categoriaSeleccionada = categoria;
  }

  iconoCategoria(categoria: string): string {
    const nombre = categoria.toLocaleLowerCase('es');
    if (nombre.includes('cafe') || nombre.includes('café')) return '☕';
    if (nombre.includes('jugo') || nombre.includes('zumo')) return '🧃';
    if (nombre.includes('te') || nombre.includes('té')) return '🍵';
    if (nombre.includes('agua')) return '💧';
    if (nombre.includes('smoothie') || nombre.includes('batido')) return '🥤';
    return '🍹';
  }

  // ===================== Cobro (modal) =====================

  get metodosVisibles(): TipoPago[] {
    return this.metodosDePago.filter(
      (metodo) => metodo !== 'TARJETA' || this.izipayConfigurado
    );
  }

  elegirMetodo(metodo: TipoPago): void {
    this.tipoPago = metodo;
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
    if (this.estadoCobro === 'procesando') return;
    this.cobroAbierto = false;
  }

  vueltoPrevisto(): number {
    if (this.tipoPago !== 'EFECTIVO' || this.montoRecibido == null) return 0;
    const vuelto = this.montoRecibido - this.total();
    return vuelto > 0 ? vuelto : 0;
  }

  confirmarPago(): void {
    if (!this.carrito().length) return;
    this.estadoCobro = 'procesando';
    this.mensajeError = '';

    const request: VentaRequest & { clienteDni?: string } = {
      venta: {
        mesa: this.mesa != null && !Number.isNaN(this.mesa) ? this.mesa : undefined,
        tipoVenta: this.tipoVenta,
        notas: this.notas.trim() || undefined,
      },
      detalles: this.carrito().map((item) => ({
        bebida: { id: item.bebida.id },
        cantidad: item.cantidad,
      })),
      clienteDni: this.clienteInfo?.dni || (this.dniCliente.trim() ? this.dniCliente.trim() : undefined),
    };

    this.ventaService.registrar(request).subscribe({
      next: (venta) => {
        this.ventaCobrada = venta;
        this.toast.success(`Pedido ${venta.numeroPedido} registrado`, 'Pedido');
        if (this.tipoPago === 'TARJETA') {
          this.cobrarConTarjeta(venta);
        } else {
          this.cobrarVenta(venta);
        }
      },
      error: (error) => this.fallarCobro(error),
    });
  }

  private cobrarConTarjeta(venta: Venta): void {
    this.izipayService.tokenDeSesion(this.redondear(venta.total), venta.numeroPedido).subscribe({
      next: (token) => {
        this.izipayService
          .abrirFormulario(token)
          .then((resultado) => {
            if (resultado?.Code === '00') {
              this.cobrarVenta(venta, resultado.Transactionid);
            } else {
              const motivo = resultado?.Message || 'Pago no aprobado por Izipay';
              this.fallarCobro(new Error(motivo));
            }
          })
          .catch((error) => this.fallarCobro(error as Error));
      },
      error: (error) => this.fallarCobro(error),
    });
  }

  private cobrarVenta(venta: Venta, numeroOperacion?: string): void {
    const montoPagado =
      this.tipoPago === 'EFECTIVO'
        ? this.montoRecibido ?? 0
        : this.redondear(venta.total);

    const pago: PagoRequest = {
      ventaId: venta.id,
      montoPagado,
      tipoPago: this.tipoPago,
      numeroOperacion,
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

  nuevoPedido(): void {
    this.ventaCobrada = null;
    this.pagoCobrado = null;
    this.estadoCobro = 'espera';
    this.mensajeError = '';
    this.notas = '';
    this.mesa = null;
    this.limpiarCliente();
    this.cobroAbierto = false;
  }

  private vaciarSilenciosamente(): void {
    this._items.set([]);
    this.limpiarCliente();
  }

  rastrearLinea(_: number, item: CarritoItem): number {
    return item.bebida.id;
  }

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
