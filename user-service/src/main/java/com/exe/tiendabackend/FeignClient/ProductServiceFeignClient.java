package com.exe.tiendabackend.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign Client para comunicarse con product-service a través de Eureka.
 * Permite a user-service consultar productos sin conocer la URL directa.
 */
@FeignClient(name = "product-service")
public interface ProductServiceFeignClient {

    @GetMapping("/api/productos")
    List<Map<String, Object>> getProductos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String nombre
    );

    @GetMapping("/api/productos/{id}")
    Map<String, Object> getProductoById(@PathVariable("id") Long id);
}
