import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { VentaService } from '../../service/venta.service';
import { Navbar } from '../../layouts/navbar/navbar';
import { DetalleVentaRespuesta, Venta } from '../../models/venta';

@Component({
  selector: 'app-ordenes',
  imports: [CommonModule, DecimalPipe, DatePipe, Navbar],
  templateUrl: './ordenes.html',
  styleUrl: './ordenes.css',
})
export class Ordenes implements OnInit {
  ordenes: Venta[] = [];
  cargando = true;
  error = '';

  ordenAbierta: Venta | null = null;
  detalle: DetalleVentaRespuesta[] = [];

  constructor(private ventaService: VentaService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.ventaService.getHistorialVentas().subscribe({
      next: (ordenes) => {
        this.ordenes = ordenes;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el historial de ordenes';
        this.cargando = false;
      },
    });
  }

  verDetalle(venta: Venta): void {
    if (this.ordenAbierta?.id === venta.id) {
      this.ordenAbierta = null;
      this.detalle = [];
      return;
    }

    this.ordenAbierta = venta;
    this.detalle = [];
    this.ventaService.getDetalleVentaByVentaId(venta.id).subscribe({
      next: (detalle) => (this.detalle = detalle),
      error: () => (this.detalle = []),
    });
  }
}
