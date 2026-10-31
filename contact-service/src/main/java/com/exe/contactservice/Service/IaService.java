package com.exe.contactservice.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class IaService {
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public IaService(@Value("${GOOGLE_AI_API_KEY:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String responder(String mensaje, String historial) {
        // Intentar Gemini si hay key y créditos
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String system = "Eres el asistente virtual de SneakerX, una tienda de zapatillas. Responde en español, de forma breve y amable. Ayuda con productos, tallas, envíos, pagos y pedidos. Si no sabes la respuesta o el usuario pide soporte humano/administrador, responde exactamente: [ESCALAR].";
                String prompt = (historial == null ? "" : historial + "\n") + "Usuario: " + mensaje;
                Map<String, Object> body = Map.of(
                    "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
                );
                String json = mapper.writeValueAsString(body);
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    Map<?,?> parsed = mapper.readValue(resp.body(), Map.class);
                    List<?> candidates = (List<?>) parsed.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<?,?> content = (Map<?,?>) ((Map<?,?>) candidates.get(0)).get("content");
                        List<?> parts = (List<?>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return String.valueOf(((Map<?,?>) parts.get(0)).get("text"));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        // Fallback gratuito basado en FAQ
        return faq(mensaje);
    }

    private String faq(String mensaje) {
        String m = mensaje == null ? "" : mensaje.toLowerCase();
        if (m.contains("hola") || m.contains("buenas")) return "¡Hola! Soy el asistente de SneakerX. Pregúntame por productos, tallas, envíos o pagos.";
        if (m.contains("producto") || m.contains("zapatilla") || m.contains("catalogo") || m.contains("catálogo") || m.contains("marca")) return "Tenemos zapatillas de marcas como Nike, Jordan, Adidas y más. Puedes ver el catálogo completo en la sección Productos.";
        if (m.contains("precio") || m.contains("cuanto") || m.contains("cuánto") || m.contains("valor")) return "Los precios varían según el modelo. Puedes ver el precio de cada producto en su ficha. Aceptamos débito y crédito.";
        if (m.contains("metodo") || m.contains("método") || m.contains("pago") || m.contains("pagar") || m.contains("tarjeta") || m.contains("debito") || m.contains("débito") || m.contains("credito") || m.contains("crédito")) return "Aceptamos pago con débito y crédito. El pago se procesa de forma segura al finalizar la compra.";
        if (m.contains("talla") || m.contains("size")) return "Trabajamos tallas desde 35 a 46. Si no estás seguro, te recomendamos medir tu pie y comparar con nuestra guía de tallas.";
        if (m.contains("envio") || m.contains("envío") || m.contains("despacho")) return "Realizamos envíos a todo Chile. El tiempo estimado es de 2 a 5 días hábiles según la región.";
        if (m.contains("stock") || m.contains("disponib")) return "Puedes ver el stock disponible en la página de cada producto. Si no hay stock, puedes contactar a un agente.";
        if (m.contains("devolucion") || m.contains("devolución") || m.contains("cambio")) return "Aceptamos devoluciones dentro de 30 días si el producto está sin uso y con etiquetas.";
        if (m.contains("agente") || m.contains("administrador") || m.contains("soporte") || m.contains("humano")) return "Te conecto con un agente humano. [ESCALAR]";
        return "No tengo esa información exacta, pero un agente humano puede ayudarte. [ESCALAR]";
    }
}