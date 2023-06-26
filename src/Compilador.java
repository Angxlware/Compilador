public class Compilador {
    public static void main(String[] args) {
        AnalizadorLexico lexico = new AnalizadorLexico();
        AnalizadorSintactico sintactico = new AnalizadorSintactico(lexico.getCabeza());
        
        if(!lexico.errorEncontrado)
            System.out.println("Analisis lexico terminado");
        
        if(!sintactico.errorEncontrado)
            System.out.println("Analisis sintactico terminado");
    }
}
