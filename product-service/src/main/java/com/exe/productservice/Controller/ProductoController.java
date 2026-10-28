package com.exe.productservice.Controller;

import com.exe.productservice.Model.Producto;
import com.exe.productservice.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    @GetMapping
    public Page<Producto> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 5, direction = Sort.Direction.DESC, sort = "id") Pageable pageable) {

        if (categoria != null && !categoria.isBlank()) {
            return productoService.listarPorCategoria(categoria, pageable);
        }
        if (nombre != null && !nombre.isBlank()) {
            return productoService.buscarPorNombre(nombre, pageable);
        }
        return productoService.listar(pageable);
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }
}
