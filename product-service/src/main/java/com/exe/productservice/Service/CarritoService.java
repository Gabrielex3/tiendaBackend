package com.exe.productservice.Service;

import com.exe.productservice.Dto.AgregarAlCarritoRequest;
import com.exe.productservice.Model.CartItem;
import com.exe.productservice.Model.Carrito;
import com.exe.productservice.Model.Producto;
import com.exe.productservice.Repository.CarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarritoService {
    private final CarritoRepository carritoRepository;
    private final ProductoService productoService;

    // --- Métodos para usuario autenticado ---

    private Carrito obtenerCarritoOCrear(Long usuarioId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Carrito c = Carrito.builder()
                    .usuarioId(usuarioId)
                    .build();
            return carritoRepository.save(c);
        });
        carrito.getItems().size();
        return carrito;
    }

    @Transactional(readOnly = false)
    public Carrito obtenerCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarritoOCrear(usuarioId);
        carrito.getItems().forEach(item -> item.getProducto().getNombre());
        return carrito;
    }

    // --- Métodos para visitante con sesión ---

    private Carrito obtenerCarritoSesionOCrear(String sessionId) {
        Carrito carrito = carritoRepository.findBySessionId(sessionId).orElseGet(() -> {
            Carrito c = Carrito.builder()
                    .sessionId(sessionId)
                    .build();
            return carritoRepository.save(c);
        });
        carrito.getItems().size();
        return carrito;
    }

    @Transactional(readOnly = false)
    public Carrito obtenerCarritoPorSesion(String sessionId) {
        Carrito carrito = obtenerCarritoSesionOCrear(sessionId);
        carrito.getItems().forEach(item -> item.getProducto().getNombre());
        return carrito;
    }

    @Transactional(readOnly = false)
    public Carrito agregarItem(Long usuarioId, AgregarAlCarritoRequest request) {
        Carrito carrito = obtenerCarritoOCrear(usuarioId);
        Producto producto = productoService.obtenerPorId(request.productoId());

        Optional<CartItem> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getNombre().equals(request.productoId()) && java.util.Objects.equals(item.getTalla(), request.talla()))
                .findFirst();

        if (itemExistente.isPresent()) {
            CartItem item = itemExistente.get();
            item.setCantidad(item.getCantidad() + request.cantidad());
        } else {
            CartItem newItem = CartItem.builder()
                    .carrito(carrito)
                    .producto(producto)
                    .talla(request.talla())
                    .cantidad(request.cantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();
            carrito.getItems().add(newItem);
        }

        return carritoRepository.save(carrito);
    }

    @Transactional(readOnly = false)
    public Carrito actualizarCantidad(Long usuarioId, Long itemId, Integer cantidad) {
        Carrito carrito = obtenerCarritoOCrear(usuarioId);
        CartItem item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item del carrito no encontrado"));

        if (cantidad <= 0) {
            carrito.getItems().remove(item);
        } else {
            item.setCantidad(cantidad);
        }

        return carritoRepository.save(carrito);
    }

    @Transactional(readOnly = false)
    public void eliminarItem(Long usuarioId, Long itemId) {
        Carrito carrito = obtenerCarritoOCrear(usuarioId);
        CartItem item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item del carrito no encontrado"));
        carrito.getItems().remove(item);
        carritoRepository.save(carrito);
    }

    @Transactional(readOnly = false)
    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarritoOCrear(usuarioId);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    // --- Métodos para sesión temporal (visitantes) ---

    @Transactional(readOnly = false)
    public Carrito agregarItemPorSesion(String sessionId, AgregarAlCarritoRequest request) {
        Carrito carrito = obtenerCarritoSesionOCrear(sessionId);
        Producto producto = productoService.obtenerPorId(request.productoId());

        Optional<CartItem> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getNombre().equals(request.productoId()) && java.util.Objects.equals(item.getTalla(), request.talla()))
                .findFirst();

        if (itemExistente.isPresent()) {
            CartItem item = itemExistente.get();
            item.setCantidad(item.getCantidad() + request.cantidad());
        } else {
            CartItem newItem = CartItem.builder()
                    .carrito(carrito)
                    .producto(producto)
                    .talla(request.talla())
                    .cantidad(request.cantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();
            carrito.getItems().add(newItem);
        }

        return carritoRepository.save(carrito);
    }

    @Transactional(readOnly = false)
    public Carrito actualizarCantidadPorSesion(String sessionId, Long itemId, Integer cantidad) {
        Carrito carrito = obtenerCarritoSesionOCrear(sessionId);
        CartItem item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item del carrito no encontrado"));

        if (cantidad <= 0) {
            carrito.getItems().remove(item);
        } else {
            item.setCantidad(cantidad);
        }

        return carritoRepository.save(carrito);
    }

    @Transactional(readOnly = false)
    public void eliminarItemPorSesion(String sessionId, Long itemId) {
        Carrito carrito = obtenerCarritoSesionOCrear(sessionId);
        CartItem item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item del carrito no encontrado"));
        carrito.getItems().remove(item);
        carritoRepository.save(carrito);
    }

    @Transactional(readOnly = false)
    public void vaciarCarritoPorSesion(String sessionId) {
        Carrito carrito = obtenerCarritoSesionOCrear(sessionId);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    // --- Transferir carrito temporal a usuario autenticado ---

    @Transactional(readOnly = false)
    public Carrito transferirCarritoASesion(String sessionId, Long usuarioId) {
        // Buscar el carrito temporal
        Carrito carritoTemporal = carritoRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito temporal no encontrado"));

        // Si el usuario ya tiene un carrito, fusionar los items
        Carrito carritoUsuario = carritoRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Carrito nuevo = Carrito.builder()
                    .usuarioId(usuarioId)
                    .build();
            return carritoRepository.save(nuevo);
        });

        // Mover los items del carrito temporal al carrito del usuario
        for (CartItem itemTemporal : carritoTemporal.getItems()) {
            Optional<CartItem> itemExistente = carritoUsuario.getItems().stream()
                .filter(i -> i.getProducto().getId().equals(itemTemporal.getProducto().getId()) && java.util.Objects.equals(i.getTalla(), itemTemporal.getTalla()))
                    .findFirst();

            if (itemExistente.isPresent()) {
                // Fusionar cantidades
                itemExistente.get().setCantidad(
                    itemExistente.get().getCantidad() + itemTemporal.getCantidad()
                );
            } else {
                // Crear nuevo item en el carrito del usuario
                CartItem newItem = CartItem.builder()
                        .carrito(carritoUsuario)
                        .producto(itemTemporal.getProducto())
                        .talla(itemTemporal.getTalla())
                        .cantidad(itemTemporal.getCantidad())
                        .precioUnitario(itemTemporal.getPrecioUnitario())
                        .build();
                carritoUsuario.getItems().add(newItem);
            }
        }

        // Eliminar el carrito temporal
        carritoRepository.delete(carritoTemporal);

        return carritoRepository.save(carritoUsuario);
    }
}
