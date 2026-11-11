import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { Proveedores } from './proveedores';
import { AdminService } from '../../../service/admin.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { Proveedor } from '../../../models/admin';

describe('Proveedores', () => {
  let component: Proveedores;
  let adminService: AdminService;
  let toast: ToastService;

  const proveedorBase: Proveedor = {
    id: 7,
    nombre: 'Distribuidora Lima',
    ruc: '20123456789',
    telefono: '987654321',
    direccion: 'Av. Siempre Viva 123',
    contactoNombre: 'Ana Torres',
    estado: 'ACTIVO',
    tipoContrato: 'FIJO',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Proveedores],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    adminService = TestBed.inject(AdminService);
    toast = TestBed.inject(ToastService);
    spyOn(adminService, 'listarProveedores').and.returnValue(of([proveedorBase]));
    spyOn(toast, 'error');
    spyOn(toast, 'success');
  });

  function crearComponente(): void {
    const fixture = TestBed.createComponent(Proveedores);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('no guarda si el RUC no tiene 11 dígitos', () => {
    crearComponente();
    const crear = spyOn(adminService, 'crearProveedor');
    component.abrirFormularioNuevo();
    component.formulario.setValue({
      ruc: '123',
      nombre: 'Proveedor válido',
      contactoNombre: '',
      telefono: '',
    });

    component.guardar();

    expect(crear).not.toHaveBeenCalled();
    expect(component.ruc.hasError('pattern')).toBeTrue();
    expect(toast.error).toHaveBeenCalled();
  });

  it('no guarda con el formulario vacío y marca los campos', () => {
    crearComponente();
    const crear = spyOn(adminService, 'crearProveedor');
    component.abrirFormularioNuevo();

    component.guardar();

    expect(crear).not.toHaveBeenCalled();
    expect(component.formulario.touched).toBeTrue();
    expect(component.ruc.invalid).toBeTrue();
    expect(component.nombre.invalid).toBeTrue();
  });

  it('crea el proveedor con los datos normalizados', () => {
    crearComponente();
    const crear = spyOn(adminService, 'crearProveedor').and.returnValue(of(proveedorBase));
    component.abrirFormularioNuevo();
    component.formulario.setValue({
      ruc: ' 20123456789 ',
      nombre: '  Distribuidora Lima  ',
      contactoNombre: '',
      telefono: '',
    });

    component.guardar();

    expect(crear).toHaveBeenCalledWith({
      ruc: '20123456789',
      nombre: 'Distribuidora Lima',
      contactoNombre: null,
      telefono: null,
    } as never);
  });

  it('al editar rellena el formulario y conserva estado, dirección y contrato', () => {
    crearComponente();
    const actualizar = spyOn(adminService, 'actualizarProveedor').and.returnValue(of(proveedorBase));

    component.abrirFormularioEdicion(proveedorBase);
    expect(component.formulario.value.ruc).toBe('20123456789');
    expect(component.formulario.value.nombre).toBe('Distribuidora Lima');

    component.formulario.patchValue({ telefono: '999888777' });
    component.guardar();

    expect(actualizar).toHaveBeenCalledWith(
      7,
      jasmine.objectContaining({
        telefono: '999888777',
        estado: 'ACTIVO',
        direccion: 'Av. Siempre Viva 123',
        tipoContrato: 'FIJO',
      })
    );
  });

  it('acepta un teléfono vacío pero rechaza uno que no tiene 9 dígitos', () => {
    crearComponente();
    component.abrirFormularioNuevo();
    component.formulario.patchValue({ telefono: '' });
    expect(component.telefono.valid).toBeTrue();

    component.formulario.patchValue({ telefono: '123' });
    expect(component.telefono.hasError('pattern')).toBeTrue();
  });
});
