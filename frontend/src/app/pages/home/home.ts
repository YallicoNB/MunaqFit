import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { CardHome, Promocion } from '../../components/card-home/card-home';
import { CardHomeMenu } from '../../components/card-home-menu/card-home-menu';
import { Bebida } from '../../models/producto';
import { VentaService } from '../../service/venta.service';

@Component({
  selector: 'app-home',
  imports: [CommonModule, CardHome, CardHomeMenu],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit, OnDestroy {
  readonly promociones: Promocion[] = [
    {
      titulo: 'Ven con tu grupo de 4',
      descripcion: 'Recibe 10% de descuento en toda tu compra.',
      imagen: 'assets/promocion_multicolor.jpg',
      textoAlternativo: 'Grupo de amigos brindando con bebidas de colores',
    },
    {
      titulo: 'Compra 2 y llévate 1 de regalo',
      descripcion: 'Elige tus bebidas favoritas y disfruta una más por nuestra cuenta.',
      imagen: 'assets/promocion_2x1.jpg',
      textoAlternativo: 'Promoción de bebidas 2 por 1',
    },
    {
      titulo: '¡El nuevo sabor ha vuelto!',
      descripcion: 'No te lo pierdas: vuelve a disfrutar ese sabor que tanto te gusta.',
      imagen: 'assets/promocion_nuevoSabor.jpg',
      textoAlternativo: 'Bebida fría de frutos rojos con limón y hielo',
    },
    {
      titulo: 'Comparte tu momento',
      descripcion: 'Tómate una foto, etiquétanos en Instagram como @MunaqFit y recibe 5% de descuento en tu próxima bebida.',
      imagen: 'assets/Promocion_fotoEtiqueanos.jpg',
      textoAlternativo: 'Persona compartiendo una bebida fría',
    },
  ];
  readonly frases = [
    'Elige la bebida que más te representa y disfruta tu día.',
    'Refresca tus momentos con el sabor que más te gusta.',
    'Un sorbo frío, una pausa y energía para seguir.',
  ];
  fraseActual = 0;
  promocionActual = 0;
  fraseVisible = true;
  bebidasMenu: Bebida[] = [];
  productoActual = 0;
  cargandoMenu = true;
  errorMenu = '';
  private suscripcionMenu?: Subscription;
  private temporizadorTransicion?: ReturnType<typeof setTimeout>;
  private intervaloMenu?: ReturnType<typeof setInterval>;
  private readonly intervaloCarrusel = setInterval(
    () => this.seleccionarFrase((this.fraseActual + 1) % this.frases.length),
    5000,
  );
  private readonly intervaloPromociones = setInterval(() => this.mostrarSiguientePromocion(), 6000);

  constructor(private ventaService: VentaService) {}

  ngOnInit(): void {
    this.suscripcionMenu = this.ventaService.menu().subscribe({
      next: (bebidas) => {
        this.bebidasMenu = bebidas;
        this.cargandoMenu = false;
        if (this.bebidasMenu.length > 1) {
          this.intervaloMenu = setInterval(() => this.mostrarSiguienteProducto(), 4500);
        }
      },
      error: () => {
        this.errorMenu = 'No se pudo cargar el menú de bebidas.';
        this.cargandoMenu = false;
      },
    });
  }

  get promocionesVisibles(): { promocion: Promocion; posicion: string }[] {
    return [-1, 0, 1].map((desplazamiento) => {
      const indice = (this.promocionActual + desplazamiento + this.promociones.length) % this.promociones.length;
      const posicion = desplazamiento === 0
        ? 'promocion-centro'
        : desplazamiento < 0
          ? 'promocion-anterior'
          : 'promocion-siguiente';

      return { promocion: this.promociones[indice], posicion };
    });
  }

  get productosVisibles(): { bebida: Bebida; posicion: string }[] {
    const cantidad = Math.min(5, this.bebidasMenu.length);
    if (cantidad === 0) {
      return [];
    }

    const posicionesPorCantidad: Record<number, string[]> = {
      1: ['producto-central'],
      2: ['producto-central', 'producto-siguiente'],
      3: ['producto-anterior', 'producto-central', 'producto-siguiente'],
      4: [
        'producto-anterior',
        'producto-central',
        'producto-siguiente',
        'producto-lejano-siguiente',
      ],
      5: [
        'producto-lejano-anterior',
        'producto-anterior',
        'producto-central',
        'producto-siguiente',
        'producto-lejano-siguiente',
      ],
    };
    const posiciones = posicionesPorCantidad[cantidad] ?? [];
    const inicio =
      (this.productoActual - Math.floor((cantidad - 1) / 2) + this.bebidasMenu.length) %
      this.bebidasMenu.length;

    return posiciones.map((posicion, indice) => ({
      bebida: this.bebidasMenu[(inicio + indice) % this.bebidasMenu.length],
      posicion,
    }));
  }

  rastrearPromocion(
    _indice: number,
    elemento: { promocion: Promocion; posicion: string },
  ): string {
    return elemento.promocion.titulo;
  }

  ngOnDestroy(): void {
    this.suscripcionMenu?.unsubscribe();
    clearInterval(this.intervaloCarrusel);
    clearInterval(this.intervaloPromociones);
    if (this.intervaloMenu) {
      clearInterval(this.intervaloMenu);
    }
    if (this.temporizadorTransicion) {
      clearTimeout(this.temporizadorTransicion);
    }
  }

  mostrarSiguienteProducto(): void {
    if (this.bebidasMenu.length > 1) {
      this.productoActual = (this.productoActual + 1) % this.bebidasMenu.length;
    }
  }

  mostrarProductoAnterior(): void {
    if (this.bebidasMenu.length > 1) {
      this.productoActual = (this.productoActual - 1 + this.bebidasMenu.length) % this.bebidasMenu.length;
    }
  }

  rastrearBebida(_indice: number, elemento: { bebida: Bebida; posicion: string }): number {
    return elemento.bebida.id;
  }

  mostrarSiguientePromocion(): void {
    this.promocionActual = (this.promocionActual + 1) % this.promociones.length;
  }

  mostrarPromocionAnterior(): void {
    this.promocionActual = (this.promocionActual - 1 + this.promociones.length) % this.promociones.length;
  }

  seleccionarPromocion(indice: number): void {
    this.promocionActual = indice;
  }

  seleccionarFrase(indice: number): void {
    if (indice === this.fraseActual) {
      return;
    }

    if (this.temporizadorTransicion) {
      clearTimeout(this.temporizadorTransicion);
    }
    this.fraseVisible = false;
    this.temporizadorTransicion = setTimeout(() => {
      this.fraseActual = indice;
      this.fraseVisible = true;
      this.temporizadorTransicion = undefined;
    }, 220);
  }
}