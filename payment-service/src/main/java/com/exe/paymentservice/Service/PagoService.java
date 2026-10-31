package com.exe.paymentservice.Service;

import com.exe.paymentservice.Dto.PagoRequest;
import com.exe.paymentservice.Dto.PagoResponse;
import com.exe.paymentservice.Model.Pago;
import com.exe.paymentservice.Model.PagoItem;
import com.exe.paymentservice.Repository.PagoRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PagoService {
    private final PagoRepository pagoRepository;
    private final RestTemplate restTemplate;

    public PagoService(PagoRepository pagoRepository, RestTemplate restTemplate) {
        this.pagoRepository = pagoRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional(readOnly = true)
    public List<Pago> listarTodos() {
        return pagoRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Pago> listarPorUsuario(Long usuarioId) {
        if (usuarioId == null) return List.of();
        return pagoRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
    }

    @Transactional
    public PagoResponse procesar(PagoRequest request) {
        BigDecimal total = request.items().stream()
                .map(i -> i.precioUnitario().multiply(BigDecimal.valueOf(i.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pago pago = new Pago();
        pago.setUsuarioId(request.usuarioId());
        pago.setMetodoPago(request.metodoPago());
        pago.setObservacion(request.observacion());
        pago.setBuyerName(request.buyerName());
        pago.setBuyerEmail(request.buyerEmail());
        pago.setDeliveryName(request.deliveryName());
        pago.setDeliveryRut(request.deliveryRut());
        pago.setDeliveryPhone(request.deliveryPhone());
        pago.setDeliveryAddress(request.deliveryAddress());
        pago.setDeliveryNotes(request.deliveryNotes());
        pago.setTotal(total);
        pago.setReceiptNumber("SX-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());

        for (PagoRequest.ItemRequest it : request.items()) {
            PagoItem item = new PagoItem();
            item.setProductoId(it.productoId());
            item.setNombre(it.nombre());
            item.setImagenUrl(it.imagenUrl());
            item.setTalla(it.talla());
            item.setCantidad(it.cantidad());
            item.setPrecioUnitario(it.precioUnitario());
            pago.getItems().add(item);
        }
        Pago saved = pagoRepository.save(pago);

        // Simular envío de correo (en producción usar Spring Mail)
        System.out.println("[PAGO] Notificación enviada a " + saved.getBuyerEmail() + " por compra " + saved.getReceiptNumber() + " | Obs: " + saved.getObservacion());

        // Decrementar stock (best-effort)
        for (PagoRequest.ItemRequest it : request.items()) {
            try {
                restTemplate.exchange(
                        "http://product-service/api/productos/" + it.productoId() + "/stock?cantidad=" + it.cantidad(),
                        HttpMethod.PUT, new HttpEntity<>(new HttpHeaders()), Void.class);
            } catch (Exception e) {
                System.out.println("[PAGO] stock decrement error: " + e.getMessage());
            }
        }

        // Vaciar carrito (best-effort)
        try {
            HttpHeaders headers = new HttpHeaders();
            if (request.usuarioId() != null) {
                headers.set("X-User-Id", String.valueOf(request.usuarioId()));
                restTemplate.exchange("http://product-service/api/carrito",
                        HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            } else if (request.sessionId() != null) {
                headers.set("X-Session-Id", request.sessionId());
                restTemplate.exchange("http://product-service/api/carrito/sesion",
                        HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            }
        } catch (Exception e) {
            System.out.println("[PAGO] cart clear error: " + e.getMessage());
        }

        return new PagoResponse(saved.getId(), saved.getReceiptNumber(), saved.getTotal(), saved.getCreatedAt());
    }
}
