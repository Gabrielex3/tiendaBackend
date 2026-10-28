package com.exe.productservice.Controller;

import com.exe.productservice.Dto.ActualizarProductoRequest;
import com.exe.productservice.Dto.CrearProductoRequest;
import com.exe.productservice.Model.Producto;
import com.exe.productservice.Service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
public class AdminProductoController {
    private final ProductoService productoService;

    @PostMapping
    public Producto crear(@Valid @RequestBody CrearProductoRequest request) {
        return productoService.crear(request);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
