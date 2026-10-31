package com.exe.productservice.Service;

import com.exe.productservice.Dto.ActualizarProductoRequest;
import com.exe.productservice.Dto.CrearProductoRequest;
import com.exe.productservice.Model.Producto;
import com.exe.productservice.Repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoService {
    private final ProductoRepository productoRepository;

    public Page<Producto> listar(Pageable pageable) {
        return productoRepository.findByActivoTrue(pageable);
    }

    public Page<Producto> listarPorCategoria(String categoria, Pageable pageable) {
        return productoRepository.findByCategoriaAndActivoTrue(categoria, pageable);
    }

    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre, pageable);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional
    public Producto crear(CrearProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio(request.precio())
                .stock(request.stock() != null ? request.stock() : 0)
                .imagenUrl(request.imagenUrl())
                .categoria(request.categoria())
                .activo(true)
                .destacado(request.destacado() != null ? request.destacado() : false)
                .build();
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, ActualizarProductoRequest request) {
        Producto producto = obtenerPorId(id);
        if (request.nombre() != null) producto.setNombre(request.nombre());
        if (request.descripcion() != null) producto.setDescripcion(request.descripcion());
        if (request.precio() != null) producto.setPrecio(request.precio());
        if (request.stock() != null) producto.setStock(request.stock());
        if (request.imagenUrl() != null) producto.setImagenUrl(request.imagenUrl());
        if (request.categoria() != null) producto.setCategoria(request.categoria());
        if (request.activo() != null) producto.setActivo(request.activo());
        if (request.destacado() != null) producto.setDestacado(request.destacado());
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto decrementarStock(Long id, Integer cantidad) {
        Producto producto = obtenerPorId(id);
        int actual = producto.getStock() == null ? 0 : producto.getStock();
        int nuevo = actual - (cantidad == null ? 0 : cantidad);
        if (nuevo < 0) nuevo = 0;
        producto.setStock(nuevo);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }
}
