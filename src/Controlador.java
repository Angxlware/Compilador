public class Controlador {
    private static final String DIRECCION_ARCHIVO_ENTRADA = "src\\codigo.txt";
    private static final String DIRECCION_ARCHIVO_SALIDA = "";

    public static void main(String[] args) {
        AnalizadorLexico lexico = new AnalizadorLexico(DIRECCION_ARCHIVO_ENTRADA);
        AnalizadorSintactico sintactico = new AnalizadorSintactico(lexico.getCabeza());
        
        if(!lexico.errorEncontrado)
            System.out.println("Analisis lexico terminado");
        
        if(!sintactico.errorEncontrado)
            System.out.println("Analisis sintactico terminado");
    }
}
