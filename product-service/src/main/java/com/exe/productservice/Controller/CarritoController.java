package com.exe.productservice.Controller;

import com.exe.productservice.Dto.AgregarAlCarritoRequest;
import com.exe.productservice.Model.Carrito;
import com.exe.productservice.Service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {
    private final CarritoService carritoService;

    // ==========================================
    // Carrito para usuario autenticado (X-User-Id como Long)
    // ==========================================

    @GetMapping
    public Carrito obtenerCarrito(@RequestHeader("X-User-Id") Long usuarioId) {
        return carritoService.obtenerCarrito(usuarioId);
    }

    @PostMapping("/items")
    public Carrito agregarItem(@RequestHeader("X-User-Id") Long usuarioId,
                               @Valid @RequestBody AgregarAlCarritoRequest request) {
        return carritoService.agregarItem(usuarioId, request);
    }

    @PutMapping("/items/{itemId}")
    public Carrito actualizarCantidad(@RequestHeader("X-User-Id") Long usuarioId,
                                      @PathVariable Long itemId,
                                      @RequestParam Integer cantidad) {
        return carritoService.actualizarCantidad(usuarioId, itemId, cantidad);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarItem(@RequestHeader("X-User-Id") Long usuarioId,
                             @PathVariable Long itemId) {
        carritoService.eliminarItem(usuarioId, itemId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vaciarCarrito(@RequestHeader("X-User-Id") Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
    }

    // ==========================================
    // Carrito temporal para visitantes (X-Session-Id)
    // ==========================================

    @GetMapping("/sesion")
    public Carrito obtenerCarritoPorSesion(@RequestHeader("X-Session-Id") String sessionId) {
        return carritoService.obtenerCarritoPorSesion(sessionId);
    }

    @PostMapping("/sesion/items")
    public Carrito agregarItemPorSesion(@RequestHeader("X-Session-Id") String sessionId,
                                        @Valid @RequestBody AgregarAlCarritoRequest request) {
        return carritoService.agregarItemPorSesion(sessionId, request);
    }

    @PutMapping("/sesion/items/{itemId}")
    public Carrito actualizarCantidadPorSesion(@RequestHeader("X-Session-Id") String sessionId,
                                               @PathVariable Long itemId,
                                               @RequestParam Integer cantidad) {
        return carritoService.actualizarCantidadPorSesion(sessionId, itemId, cantidad);
    }

    @DeleteMapping("/sesion/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarItemPorSesion(@RequestHeader("X-Session-Id") String sessionId,
                                      @PathVariable Long itemId) {
        carritoService.eliminarItemPorSesion(sessionId, itemId);
    }

    @DeleteMapping("/sesion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vaciarCarritoPorSesion(@RequestHeader("X-Session-Id") String sessionId) {
        carritoService.vaciarCarritoPorSesion(sessionId);
    }

    // ==========================================
    // Transferir carrito temporal a usuario autenticado
    // ==========================================

    @PostMapping("/transferir")
    public Carrito transferirCarrito(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestHeader("X-User-Id") Long usuarioId) {
        return carritoService.transferirCarritoASesion(sessionId, usuarioId);
    }
}
