package org.example;

import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Impresora {

    // Método para imprimir COMANDA (Cocina) - Sin precios
    public static void imprimirComandaCocina(String nombreMesa, Map<String, Integer> resumen) {
        StringBuilder ticket = new StringBuilder();

        ticket.append("\n********************************\n");
        ticket.append("       ORDEN DE COCINA\n");
        ticket.append("********************************\n\n");
        ticket.append("MESA: ").append(nombreMesa).append("\n");
        ticket.append("HORA: ").append(horaActual()).append("\n");
        ticket.append("--------------------------------\n");

        // Listar productos
        for (Map.Entry<String, Integer> entry : resumen.entrySet()) {
            String linea = String.format("%-4s %s\n", entry.getValue() + "x", entry.getKey());
            ticket.append(linea);
        }
        ticket.append("--------------------------------\n\n\n\n"); // Espacio al final para cortar papel

        enviarAImpresora(ticket.toString());
    }

    // Método para imprimir BOLETA (Cliente) - Con precios y total
    public static void imprimirBoleta(String nombreMesa, List<Producto> lista, double total) {
        StringBuilder ticket = new StringBuilder();

        ticket.append("\n================================\n");
        ticket.append("       RESTAURANTE JAVA\n");
        ticket.append("================================\n\n");
        ticket.append("MESA: ").append(nombreMesa).append("\n");
        ticket.append("FECHA: ").append(horaActual()).append("\n");
        ticket.append("--------------------------------\n");
        ticket.append(String.format("%-18s %10s\n", "PRODUCTO", "PRECIO"));
        ticket.append("--------------------------------\n");

        for (Producto p : lista) {
            // Formato: Nombre a la izquierda, Precio a la derecha
            // Cortamos el nombre si es muy largo para que no rompa la linea
            String nombreCorto = p.getNombre().length() > 18 ? p.getNombre().substring(0, 18) : p.getNombre();
            String linea = String.format("%-18s $%9.0f\n", nombreCorto, p.getPrecio());
            ticket.append(linea);
        }

        ticket.append("--------------------------------\n");
        ticket.append(String.format("TOTAL A PAGAR:      $%9.0f\n", total));
        ticket.append("================================\n");
        ticket.append("      ¡GRACIAS POR SU VISITA!   \n\n\n\n");

        enviarAImpresora(ticket.toString());
    }

    // Lógica interna para mandar a la impresora del sistema
    private static void enviarAImpresora(String textoTicket) {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            // Verificamos si hay impresora instalada en el sistema
            boolean proceed = job.showPrintDialog(null); // Muestra diálogo para elegir impresora (Opcional: quitar si quieres directo)

            if (proceed) {
                // Creamos un objeto visual simple para imprimir
                Text textoVisual = new Text(textoTicket);
                textoVisual.setFont(Font.font("Monospaced", 10)); // Fuente tipo ticket

                // Imprimimos ese objeto
                boolean printed = job.printPage(textoVisual);
                if (printed) {
                    job.endJob();
                    System.out.println("Impresión enviada correctamente.");
                } else {
                    System.out.println("Fallo al imprimir.");
                }
            }
        } else {
            System.out.println("No se encontró ninguna impresora en el sistema.");
        }
    }

    private static String horaActual() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }
}