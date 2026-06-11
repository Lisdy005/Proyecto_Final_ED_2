package Main;

import Modelo.*;
import Arbol.*;
import Grafo.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    private static ABB arbol = new ABB();
    private static Grafo grafo = new Grafo(50);
    private static HttpServer server;

    public static void main(String[] args) throws Exception {
        cargarDatosIniciales();
        iniciarServidor();

        System.out.println("\n" + "=".repeat(55));
        System.out.println("🏰 SERVIDOR DEL PARQUE DE DIVERSIONES INICIADO");
        System.out.println("=".repeat(55));
        System.out.println("🌐 Abre tu navegador en: http://localhost:8081");
        System.out.println("=".repeat(55));

        while (true) {
            Thread.sleep(1000);
        }
    }

    private static void iniciarServidor() throws Exception {
        server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/", new ArchivoEstaticoHandler());
        server.createContext("/consultar", new ConsultarHandler());
        server.createContext("/administrar", new AdministrarHandler());
        server.createContext("/ruta", new RutaHandler());
        server.createContext("/estadisticas", new EstadisticasHandler());

        server.setExecutor(null);
        server.start();
    }

    // ========== MANEJADOR PARA ARCHIVOS ESTÁTICOS ==========
    static class ArchivoEstaticoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/menu.html";
            }
            String fileName = path.substring(1);
            File file = new File(fileName);
            if (!file.exists() || file.isDirectory()) {
                String response = "404 - Archivo no encontrado: " + fileName;
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            String contentType = "text/html";
            if (path.endsWith(".css")) contentType = "text/css";
            if (path.endsWith(".js")) contentType = "application/javascript";
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fs.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        }
    }

    // ========== MANEJADOR: CONSULTAR ATRACCIONES ==========
    static class ConsultarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String accion = "";
            String zonaSeleccionada = "";
            String nombreBuscado = "";

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2) {
                        if (kv[0].equals("accion")) accion = java.net.URLDecoder.decode(kv[1], "UTF-8");
                        if (kv[0].equals("zona")) zonaSeleccionada = java.net.URLDecoder.decode(kv[1], "UTF-8");
                        if (kv[0].equals("nombre")) nombreBuscado = java.net.URLDecoder.decode(kv[1], "UTF-8");
                    }
                }
            }

            String html = generarHtmlConsultar(accion, zonaSeleccionada, nombreBuscado);
            sendHtmlResponse(exchange, html);
        }
    }

    // ========== MANEJADOR: ADMINISTRAR ATRACCIONES ==========
    static class AdministrarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            String accion = "";
            String mensaje = "";

            // Obtener acción del GET para mostrar el formulario correcto
            if (method.equalsIgnoreCase("GET") && query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && kv[0].equals("accion")) {
                        accion = java.net.URLDecoder.decode(kv[1], "UTF-8");
                    }
                }
            }

            // Procesar POST (insertar, eliminar, modificar)
            if (method.equalsIgnoreCase("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String nombre = "";
                String zona = "";
                String nuevoNombre = "";

                String[] params = body.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2) {
                        String key = java.net.URLDecoder.decode(kv[0], "UTF-8");
                        String value = java.net.URLDecoder.decode(kv[1], "UTF-8");
                        if (key.equals("accion")) accion = value;
                        if (key.equals("nombre")) nombre = value;
                        if (key.equals("zona")) zona = value;
                        if (key.equals("nuevoNombre")) nuevoNombre = value;
                    }
                }

                switch (accion) {
                    case "insertar":
                        try {
                            Zona zonaObj = Zona.valueOf(zona);
                            Atraccion nueva = new Atraccion(nombre, zonaObj);
                            arbol.insertar(nueva);
                            grafo.agregarAtraccion(nueva);
                            // Conectar al anillo
                            int id = nueva.getId();
                            if (id <= 20) {
                                grafo.agregarCamino(id, id == 1 ? 20 : id - 1, 5);
                                grafo.agregarCamino(id, id == 20 ? 1 : id + 1, 5);
                            }
                            mensaje = "✅ Atracción agregada correctamente (ID: " + nueva.getId() + ")";
                        } catch (Exception e) {
                            mensaje = "❌ Error al insertar: " + e.getMessage();
                        }
                        break;
                    case "eliminar":
                        Atraccion a = arbol.buscar(nombre);
                        if (a != null) {
                            arbol.eliminar(nombre);
                            grafo.eliminarAtraccion(a.getId());
                            mensaje = "✅ Atracción eliminada correctamente";
                        } else {
                            mensaje = "❌ Atracción no encontrada";
                        }
                        break;
                    case "modificar":
                        if (arbol.modificarNombre(nombre, nuevoNombre)) {
                            mensaje = "✅ Nombre modificado correctamente";
                        } else {
                            mensaje = "❌ No se pudo modificar";
                        }
                        break;
                    default:
                        mensaje = "❌ Acción no reconocida";
                }
            }

            String html = generarHtmlAdministrar(accion, mensaje);
            sendHtmlResponse(exchange, html);
        }
    }

    // ========== MANEJADOR: RUTA ==========
    static class RutaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String origen = "";
            String destino = "";
            if (query != null) {
                if (query.contains("origen=")) {
                    origen = query.split("origen=")[1];
                    if (origen.contains("&")) origen = origen.split("&")[0];
                    origen = java.net.URLDecoder.decode(origen, "UTF-8");
                }
                if (query.contains("destino=")) {
                    destino = query.split("destino=")[1];
                    if (destino.contains("&")) destino = destino.split("&")[0];
                    destino = java.net.URLDecoder.decode(destino, "UTF-8");
                }
            }
            String html = generarHtmlRuta(origen, destino);
            sendHtmlResponse(exchange, html);
        }
    }

    // ========== MANEJADOR: ESTADÍSTICAS ==========
    static class EstadisticasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generarHtmlEstadisticas();
            sendHtmlResponse(exchange, html);
        }
    }

    // ========== MÉTODOS PARA GENERAR HTML ==========
    private static void sendHtmlResponse(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private static String generarHtmlConsultar(String accion, String zonaSeleccionada, String nombreBuscado) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <title>Consultar atracciones - Parque de Diversiones</title>\n");
        html.append("    <link rel='stylesheet' href='/estilo.css'>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class='container'>\n");
        html.append("    <div class='header'>\n");
        html.append("        <h1>🏰 PARQUE DE DIVERSIONES 🎡</h1>\n");
        html.append("        <p class='subtitulo-grande'>CONSULTAR ATRACCIONES</p>\n");
        html.append("    </div>\n");
        html.append("    <div class='resultado-card'>\n");

        // Selector de acción
        html.append("        <div class='selector-container'>\n");
        html.append("            <form method='get' action='/consultar'>\n");
        html.append("                <label>📌 Opción:</label>\n");
        html.append("                <select name='accion' onchange='this.form.submit()'>\n");
        html.append("                    <option value=''>-- Seleccionar --</option>\n");
        html.append("                    <option value='ver_todas' ").append("ver_todas".equals(accion) ? "selected" : "").append(">📋 Ver todas las atracciones</option>\n");
        html.append("                    <option value='ver_por_zona' ").append("ver_por_zona".equals(accion) ? "selected" : "").append(">🗺️ Ver por zona</option>\n");
        html.append("                    <option value='buscar' ").append("buscar".equals(accion) ? "selected" : "").append(">🔍 Buscar atracción</option>\n");
        html.append("                </select>\n");
        html.append("            </form>\n");
        html.append("        </div>\n");

        // Resultados según la acción
        if ("ver_todas".equals(accion)) {
            html.append("        <div class='resultado'>\n");
            html.append("            <h2>📋 Todas las atracciones</h2>\n");
            html.append("            <ul class='lista'>\n");
            for (Atraccion a : arbol.recorridoInorden()) {
                String zonaClass = "zona-" + a.getZona().name();
                String emoji = getEmojiPorZona(a.getZona());
                html.append("                <li class='").append(zonaClass).append("'>");
                html.append(emoji).append(" ").append(a.getNombre());
                html.append(" - ").append(a.getZona().name());
                if (!a.isOperativa()) html.append(" - ❌ Cerrada");
                html.append("</li>\n");
            }
            html.append("            </ul>\n");
            html.append("            <p><strong>Total:</strong> ").append(arbol.contarAtracciones()).append(" atracciones</p>\n");
            html.append("        </div>\n");
        } else if ("ver_por_zona".equals(accion)) {
            html.append("        <div class='selector-container'>\n");
            html.append("            <form method='get' action='/consultar'>\n");
            html.append("                <input type='hidden' name='accion' value='ver_por_zona'>\n");
            html.append("                <label>📍 Zona:</label>\n");
            html.append("                <select name='zona' onchange='this.form.submit()'>\n");
            html.append("                    <option value=''>-- Seleccionar zona --</option>\n");
            for (Zona zona : Zona.values()) {
                String emoji = getEmojiPorZona(zona);
                String selected = (zonaSeleccionada != null && zonaSeleccionada.equals(zona.name())) ? "selected" : "";
                html.append("                    <option value='").append(zona.name()).append("' ").append(selected).append(">");
                html.append(emoji).append(" ").append(zona.name());
                html.append("</option>\n");
            }
            html.append("                </select>\n");
            html.append("            </form>\n");
            html.append("        </div>\n");

            if (zonaSeleccionada != null && !zonaSeleccionada.isEmpty()) {
                try {
                    Zona zona = Zona.valueOf(zonaSeleccionada);
                    String emoji = getEmojiPorZona(zona);
                    html.append("            <h2>").append(emoji).append(" ").append(zona.name()).append("</h2>\n");
                    html.append("            <ul class='lista'>\n");
                    for (Atraccion a : arbol.listarPorZona(zona)) {
                        html.append("                <li>📍 ").append(a.getNombre()).append("</li>\n");
                    }
                    html.append("            </ul>\n");
                } catch (IllegalArgumentException e) {
                    html.append("            <p>❌ Zona no válida</p>\n");
                }
            }
        } else if ("buscar".equals(accion)) {
            html.append("        <div class='selector-container'>\n");
            html.append("            <form method='get' action='/consultar'>\n");
            html.append("                <input type='hidden' name='accion' value='buscar'>\n");
            html.append("                <label>🔍 Selecciona una atracción:</label>\n");
            html.append("                <select name='nombre'>\n");
            html.append("                    <option value=''>-- Seleccionar --</option>\n");
            for (Atraccion a : arbol.recorridoInorden()) {
                String emoji = getEmojiPorZona(a.getZona());
                String selected = (nombreBuscado != null && nombreBuscado.equals(a.getNombre())) ? "selected" : "";
                html.append("                    <option value='").append(a.getNombre()).append("' ").append(selected).append(">");
                html.append(emoji).append(" ").append(a.getNombre());
                html.append("</option>\n");
            }
            html.append("                </select>\n");
            html.append("                <button type='submit'>🔍 Buscar</button>\n");
            html.append("            </form>\n");
            html.append("        </div>\n");

            if (nombreBuscado != null && !nombreBuscado.isEmpty()) {
                Atraccion a = arbol.buscar(nombreBuscado);
                if (a != null) {
                    String emoji = getEmojiPorZona(a.getZona());
                    html.append("        <div class='resultado'>\n");
                    html.append("            <h3>✅ Resultado:</h3>\n");
                    html.append("            <div style='padding: 15px; background: #e8f4f8; border-radius: 10px;'>\n");
                    html.append("                <p><strong>").append(emoji).append(" ").append(a.getNombre()).append("</strong></p>\n");
                    html.append("                <p>📍 Zona: ").append(a.getZona().name()).append("</p>\n");
                    html.append("                <p>🆔 ID: ").append(a.getId()).append("</p>\n");
                    html.append("                <p>").append(a.isOperativa() ? "✅ Operativa" : "❌ Cerrada").append("</p>\n");
                    html.append("            </div>\n");
                    html.append("        </div>\n");
                } else {
                    html.append("        <div class='resultado'>\n");
                    html.append("            <h3>❌ No encontrada</h3>\n");
                    html.append("            <p>No se encontró la atracción \"").append(nombreBuscado).append("\"</p>\n");
                    html.append("        </div>\n");
                }
            }
        }

        html.append("        <button class='btn-volver' onclick='window.location.href=\"/\"'>← Volver al menú</button>\n");
        html.append("    </div>\n");
        html.append("    <div class='footer'>\n");
        html.append("        <p>🎢 ¡Diviértete y disfruta tu visita! 🎢</p>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private static String generarHtmlAdministrar(String accion, String mensaje) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <title>Administrar atracciones - Parque de Diversiones</title>\n");
        html.append("    <link rel='stylesheet' href='/estilo.css'>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class='container'>\n");
        html.append("    <div class='header'>\n");
        html.append("        <h1>🏰 PARQUE DE DIVERSIONES 🎡</h1>\n");
        html.append("        <p class='subtitulo-grande'>ADMINISTRAR ATRACCIONES</p>\n");
        html.append("    </div>\n");
        html.append("    <div class='resultado-card'>\n");

        if (mensaje != null && !mensaje.isEmpty()) {
            String clase = mensaje.startsWith("✅") ? "mensaje-exito" : "mensaje-error";
            html.append("        <div class='mensaje ").append(clase).append("'>").append(mensaje).append("</div>\n");
        }

        // Selector de acción
        html.append("        <div class='selector-container'>\n");
        html.append("            <form method='get' action='/administrar'>\n");
        html.append("                <label>📌 Acción:</label>\n");
        html.append("                <select name='accion' onchange='this.form.submit()'>\n");
        html.append("                    <option value=''>-- Seleccionar --</option>\n");
        html.append("                    <option value='insertar' ").append("insertar".equals(accion) ? "selected" : "").append(">➕ Insertar nueva atracción</option>\n");
        html.append("                    <option value='eliminar' ").append("eliminar".equals(accion) ? "selected" : "").append(">❌ Eliminar atracción</option>\n");
        html.append("                    <option value='modificar' ").append("modificar".equals(accion) ? "selected" : "").append(">✏️ Modificar nombre</option>\n");
        html.append("                </select>\n");
        html.append("            </form>\n");
        html.append("        </div>\n");

        // Mostrar formulario según la acción
        if ("insertar".equals(accion)) {
            html.append("        <div style='margin-top: 20px; padding: 20px; background: #f8f9fa; border-radius: 10px;'>\n");
            html.append("            <h3>➕ Insertar nueva atracción</h3>\n");
            html.append("            <form method='post' action='/administrar'>\n");
            html.append("                <input type='hidden' name='accion' value='insertar'>\n");
            html.append("                <label>Nombre:</label><br>\n");
            html.append("                <input type='text' name='nombre' required style='width: 100%; padding: 8px; margin: 8px 0; border-radius: 8px; border: 1px solid #ddd;'><br>\n");
            html.append("                <label>Zona:</label><br>\n");
            html.append("                <select name='zona' required style='width: 100%; padding: 8px; margin: 8px 0; border-radius: 8px; border: 1px solid #ddd;'>\n");
            for (Zona zona : Zona.values()) {
                String emoji = getEmojiPorZona(zona);
                html.append("                    <option value='").append(zona.name()).append("'>").append(emoji).append(" ").append(zona.name()).append("</option>\n");
            }
            html.append("                </select><br>\n");
            html.append("                <button type='submit' class='btn-volver' style='margin-top: 10px; background: #28a745;'>✅ Agregar atracción</button>\n");
            html.append("            </form>\n");
            html.append("        </div>\n");
        } else if ("eliminar".equals(accion)) {
            html.append("        <div style='margin-top: 20px; padding: 20px; background: #f8f9fa; border-radius: 10px;'>\n");
            html.append("            <h3>❌ Eliminar atracción</h3>\n");
            html.append("            <form method='post' action='/administrar'>\n");
            html.append("                <input type='hidden' name='accion' value='eliminar'>\n");
            html.append("                <label>Selecciona la atracción a eliminar:</label><br>\n");
            html.append("                <select name='nombre' required style='width: 100%; padding: 8px; margin: 8px 0; border-radius: 8px; border: 1px solid #ddd;'>\n");
            for (Atraccion a : arbol.recorridoInorden()) {
                String emoji = getEmojiPorZona(a.getZona());
                html.append("                    <option value='").append(a.getNombre()).append("'>").append(emoji).append(" ").append(a.getNombre()).append("</option>\n");
            }
            html.append("                </select><br>\n");
            html.append("                <button type='submit' class='btn-volver' style='margin-top: 10px; background: #dc3545;'>🗑️ Eliminar atracción</button>\n");
            html.append("            </form>\n");
            html.append("        </div>\n");
        } else if ("modificar".equals(accion)) {
            html.append("        <div style='margin-top: 20px; padding: 20px; background: #f8f9fa; border-radius: 10px;'>\n");
            html.append("            <h3>✏️ Modificar nombre de atracción</h3>\n");
            html.append("            <form method='post' action='/administrar'>\n");
            html.append("                <input type='hidden' name='accion' value='modificar'>\n");
            html.append("                <label>Selecciona la atracción:</label><br>\n");
            html.append("                <select name='nombre' required style='width: 100%; padding: 8px; margin: 8px 0; border-radius: 8px; border: 1px solid #ddd;'>\n");
            for (Atraccion a : arbol.recorridoInorden()) {
                String emoji = getEmojiPorZona(a.getZona());
                html.append("                    <option value='").append(a.getNombre()).append("'>").append(emoji).append(" ").append(a.getNombre()).append("</option>\n");
            }
            html.append("                </select><br>\n");
            html.append("                <label>Nuevo nombre:</label><br>\n");
            html.append("                <input type='text' name='nuevoNombre' required style='width: 100%; padding: 8px; margin: 8px 0; border-radius: 8px; border: 1px solid #ddd;'><br>\n");
            html.append("                <button type='submit' class='btn-volver' style='margin-top: 10px; background: #ffc107; color: #333;'>✏️ Modificar nombre</button>\n");
            html.append("            </form>\n");
            html.append("        </div>\n");
        }

        html.append("        <button class='btn-volver' onclick='window.location.href=\"/\"'>← Volver al menú</button>\n");
        html.append("    </div>\n");
        html.append("    <div class='footer'>\n");
        html.append("        <p>🎢 ¡Diviértete y disfruta tu visita! 🎢</p>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private static String generarHtmlRuta(String origenNombre, String destinoNombre) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <title>Calcular ruta - Parque de Diversiones</title>\n");
        html.append("    <link rel='stylesheet' href='/estilo.css'>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class='container'>\n");
        html.append("    <div class='header'>\n");
        html.append("        <h1>🏰 PARQUE DE DIVERSIONES 🎡</h1>\n");
        html.append("        <p class='subtitulo-grande'>CALCULAR RUTA</p>\n");
        html.append("    </div>\n");
        html.append("    <div class='resultado-card'>\n");

        html.append("        <form method='get' action='/ruta' class='formulario-ruta'>\n");
        html.append("            <label>📍 Origen:</label>\n");
        html.append("            <select name='origen' class='select-atraccion'>\n");
        for (Atraccion a : arbol.recorridoInorden()) {
            String emoji = getEmojiPorZona(a.getZona());
            String selected = (origenNombre != null && origenNombre.equals(a.getNombre())) ? "selected" : "";
            html.append("                <option value='").append(a.getNombre()).append("' ").append(selected).append(">");
            html.append(emoji).append(" ").append(a.getNombre());
            html.append("</option>\n");
        }
        html.append("            </select>\n");
        html.append("            <br><br>\n");
        html.append("            <label>📍 Destino:</label>\n");
        html.append("            <select name='destino' class='select-atraccion'>\n");
        for (Atraccion a : arbol.recorridoInorden()) {
            String emoji = getEmojiPorZona(a.getZona());
            String selected = (destinoNombre != null && destinoNombre.equals(a.getNombre())) ? "selected" : "";
            html.append("                <option value='").append(a.getNombre()).append("' ").append(selected).append(">");
            html.append(emoji).append(" ").append(a.getNombre());
            html.append("</option>\n");
        }
        html.append("            </select>\n");
        html.append("            <br><br>\n");
        html.append("            <button type='submit' class='btn-volver'>🚶 Calcular ruta</button>\n");
        html.append("        </form>\n");

        if (origenNombre != null && destinoNombre != null && !origenNombre.isEmpty() && !destinoNombre.isEmpty()) {
            Atraccion origen = arbol.buscar(origenNombre);
            Atraccion destino = arbol.buscar(destinoNombre);

            if (origen == null) {
                html.append("        <div class='error'><p>❌ Origen no encontrado</p></div>\n");
            } else if (destino == null) {
                html.append("        <div class='error'><p>❌ Destino no encontrado</p></div>\n");
            } else if (origen.getId() == destino.getId()) {
                html.append("        <div class='error'><p>❌ Origen y destino son la misma atracción</p></div>\n");
            } else {
                Grafo.ResultadoDijkstra res = grafo.dijkstra(origen.getId(), destino.getId());
                if (!res.hayCamino()) {
                    html.append("        <div class='error'><p>❌ No hay camino disponible entre ").append(origen.getNombre()).append(" y ").append(destino.getNombre()).append("</p></div>\n");
                } else {
                    html.append("        <div class='resultado-ruta'>\n");
                    html.append("            <h3>📍 Ruta óptima encontrada:</h3>\n");
                    double total = 0;
                    for (int i = 0; i < res.getCamino().size(); i++) {
                        Atraccion a = grafo.getAtraccionPorId(res.getCamino().get(i));
                        String emoji = getEmojiPorZona(a.getZona());
                        html.append("            <div class='ruta-paso'>");
                        html.append(emoji).append(" ").append(a.getNombre());
                        if (i < res.getCamino().size() - 1) {
                            int next = res.getCamino().get(i + 1);
                            double caminata = grafo.obtenerTiempoCamino(a.getId(), next);
                            html.append(" → 🚶 ").append((int)caminata).append(" min");
                            total += caminata;
                        }
                        html.append("</div>\n");
                    }
                    html.append("            <div class='total'>⏱️ TOTAL: ").append((int)total).append(" minutos de caminata</div>\n");
                    html.append("        </div>\n");
                }
            }
        }

        html.append("        <button class='btn-volver' onclick='window.location.href=\"/\"'>← Volver al menú</button>\n");
        html.append("    </div>\n");
        html.append("    <div class='footer'>\n");
        html.append("        <p>🎢 ¡Diviértete y disfruta tu visita! 🎢</p>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private static String generarHtmlEstadisticas() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <title>Estadísticas - Parque de Diversiones</title>\n");
        html.append("    <link rel='stylesheet' href='/estilo.css'>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class='container'>\n");
        html.append("    <div class='header'>\n");
        html.append("        <h1>🏰 PARQUE DE DIVERSIONES 🎡</h1>\n");
        html.append("        <p class='subtitulo-grande'>ESTADÍSTICAS</p>\n");
        html.append("    </div>\n");
        html.append("    <div class='resultado-card'>\n");
        html.append("        <div class='estadisticas-grid'>\n");
        html.append("            <div class='tarjeta'><div class='numero'>").append(arbol.contarAtracciones()).append("</div><div class='label'>Total atracciones</div></div>\n");
        html.append("            <div class='tarjeta'><div class='numero'>").append(grafo.listarCaminos().size()).append("</div><div class='label'>Total caminos</div></div>\n");
        html.append("        </div>\n");
        html.append("        <h3>📊 Distribución por zona</h3>\n");
        html.append("        <table class='tabla-zonas'>\n");
        html.append("            <tr><th>Zona</th><th>Cantidad</th></tr>\n");
        for (Zona zona : Zona.values()) {
            html.append("            <tr><td>").append(zona.name()).append("</td><td>").append(arbol.listarPorZona(zona).size()).append("</td></tr>\n");
        }
        html.append("        </table>\n");
        html.append("        <button class='btn-volver' onclick='window.location.href=\"/\"'>← Volver al menú</button>\n");
        html.append("    </div>\n");
        html.append("    <div class='footer'>\n");
        html.append("        <p>🎢 ¡Diviértete y disfruta tu visita! 🎢</p>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private static String getEmojiPorZona(Zona zona) {
        switch (zona) {
            case AVENTURA: return "🎢";
            case INFANTIL: return "👶";
            case ACUATICA: return "💧";
            case TERROR: return "👻";
            case COMIDA: return "🍔";
            default: return "📍";
        }
    }

    // ========== CARGA DE DATOS INICIALES ==========

    private static void cargarDatosIniciales() {
        System.out.println("📌 Cargando datos iniciales...");

        arbol.insertar(new Atraccion("MontañaRusa", Zona.AVENTURA));
        arbol.insertar(new Atraccion("TorreCaída", Zona.AVENTURA));
        arbol.insertar(new Atraccion("VueloDelta", Zona.AVENTURA));
        arbol.insertar(new Atraccion("RíoSalvaje", Zona.AVENTURA));
        arbol.insertar(new Atraccion("Carrusel", Zona.INFANTIL));
        arbol.insertar(new Atraccion("Trencito", Zona.INFANTIL));
        arbol.insertar(new Atraccion("SillasVoladoras", Zona.INFANTIL));
        arbol.insertar(new Atraccion("CastilloInflable", Zona.INFANTIL));
        arbol.insertar(new Atraccion("TobogánGigante", Zona.ACUATICA));
        arbol.insertar(new Atraccion("OlasArtificiales", Zona.ACUATICA));
        arbol.insertar(new Atraccion("CascadaMágica", Zona.ACUATICA));
        arbol.insertar(new Atraccion("RíoLento", Zona.ACUATICA));
        arbol.insertar(new Atraccion("HotelEmbrujado", Zona.TERROR));
        arbol.insertar(new Atraccion("TúnelDelMiedo", Zona.TERROR));
        arbol.insertar(new Atraccion("MansiónSiniestra", Zona.TERROR));
        arbol.insertar(new Atraccion("CriptaDelVampiro", Zona.TERROR));
        arbol.insertar(new Atraccion("FuenteSodas", Zona.COMIDA));
        arbol.insertar(new Atraccion("PizzasDoñaMarta", Zona.COMIDA));
        arbol.insertar(new Atraccion("HeladeríaPolar", Zona.COMIDA));
        arbol.insertar(new Atraccion("PalomitasMágicas", Zona.COMIDA));

        for (Atraccion a : arbol.recorridoInorden()) {
            grafo.agregarAtraccion(a);
        }

        // Anillo (lista enlazada circular)
        for (int i = 1; i <= 19; i++) {
            grafo.agregarCamino(i, i + 1, 5);
        }
        grafo.agregarCamino(20, 1, 5);

        // Atajos opcionales
        grafo.agregarCamino(1, 11, 15);
        grafo.agregarCamino(5, 17, 8);
        grafo.agregarCamino(10, 20, 6);

        System.out.println("✅ " + arbol.contarAtracciones() + " atracciones cargadas");
        System.out.println("✅ " + grafo.listarCaminos().size() + " caminos cargados");
    }
}