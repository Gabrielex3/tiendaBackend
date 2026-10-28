package com.exe.productservice.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * Feign Client para comunicarse con usuarios-service a través de Eureka.
 * Permite a product-service consultar información del usuario sin conocer la URL directa.
 */
@FeignClient(name = "usuarios-service")
public interface UserServiceFeignClient {

    @GetMapping("/api/usuarios/me")
    Map<String, Object> getUsuarioPerfil(@RequestHeader("Authorization") String authorization);
}
