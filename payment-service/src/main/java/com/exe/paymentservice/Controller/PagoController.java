package com.exe.paymentservice.Controller;

import com.exe.paymentservice.Dto.PagoRequest;
import com.exe.paymentservice.Dto.PagoResponse;
import com.exe.paymentservice.Model.Pago;
import com.exe.paymentservice.Service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {
    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public ResponseEntity<List<Pago>> todos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }
    @PostMapping
    public ResponseEntity<PagoResponse> pagar(@RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagoService.procesar(request));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pago>> historial(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pagoService.listarPorUsuario(usuarioId));
    }
}
