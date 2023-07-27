import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

public class AnalizadorLexico {
    // Variables de la clase
    private final String direccionArchivo;  // direccion del archivo a compilar
    private Nodo cabeza;                    // cabeza de la lista de tokens
    private Nodo cola;                      // cola de la lista de tokens
    private int estado;                     // estado del automata
    private int transicion;                 // valor de la matriz de transicion
    private int numRenglon;                 // numero de renglon actual del archivo
    private String lexema;                  // lexema actual
    private int caracter;                   // caracter actual
    boolean errorEncontrado;                // bandera de error encontrado
    private static final int[][] MATRIZ_TRANSICION = {  // matriz de transicion
            //l    d	_	 .    \    +    -    *	  /	   >    <    =    (	   )	,	 ;    :    {    }   eb   tab  nl   cr   oc
            {1  , 2  , 500, 500, 5  , 104, 105, 106, 107, 6  , 7  , 112, 114, 115, 116, 118, 8  , 9  , 500, 0  , 0  , 0  , 0  , 500},
            {1  , 1  , 1  , 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
            {101, 2  , 101, 3  , 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101, 101},
            {502, 4  , 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502, 502},
            {102, 4  , 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102, 102},
            {5  , 5  , 5,   5  , 103, 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 5  , 503, 5  , 5  },
            {109, 109, 109, 109, 109, 109, 109, 108, 108, 108, 108, 109, 108, 108, 108, 108, 108, 108, 108, 108, 108, 108, 108, 108},
            {110, 110, 110, 110, 110, 110, 110, 110, 110, 113, 110, 111, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110},
            {117, 117, 117, 117, 117, 117, 117, 117, 117, 117, 117, 119, 117, 117, 117, 117, 117, 117, 117, 117, 117, 117, 117, 117},
            {9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 9  , 0  , 9  , 9  , 501, 9  , 9  }
    };
    private static final String[][] PALABRAS_RESERVADAS = { // palabras reservadas
            //   Palabra     Token
            {"NOT"         , "200"},
            {"AND"         , "201"},
            {"OR"          , "202"},
            {"VERDADERO"   , "203"},
            {"FALSO"       , "204"},
            {"SI"          , "205"},
            {"ENTONCES"    , "206"},
            {"FIN_SI"      , "207"},
            {"SINO"        , "208"},
            {"MIENTRAS"    , "209"},
            {"HACER"       , "210"},
            {"FIN_MIENTRAS", "211"},
            {"ALGORITMO"   , "212"},
            {"INICIO"      , "213"},
            {"FIN"         , "214"},
            {"LEER"        , "215"},
            {"ESCRIBIR"    , "216"},
            {"ES"          , "217"},
            {"ENTERO"      , "218"},
            {"DECIMAL"     , "219"},
            {"CADENA"      , "220"},
            {"LOGICO"      , "221"}
    };
    private static final String[][] ERRORES_LEXICOS = { // errores lexicos
            //               Mensaje                  Token
            {"Símbolo no válido"                    , "500"},
            {"Se espera cierre de comentario"       , "501"},
            {"Se espera un dígito después del punto", "502"},
            {"Se espera cierre de cadena"           , "503"}
    };

    // Constructor
    public AnalizadorLexico(String direccionArchivo){
        this.direccionArchivo = direccionArchivo;
        this.cabeza = this.cola = null;
        this.estado = this.transicion = this.caracter = 0;
        this.numRenglon = 1;
        this.lexema = "";
        this.errorEncontrado = false;

        this.Analizar();
    }

    // Obtener la cabeza de la lista de nodos
    public Nodo getCabeza(){
        return cabeza;
    }

    // Analizar lexico
    private void Analizar() {
        int columna;

        try {
            RandomAccessFile file = new RandomAccessFile(direccionArchivo, "r");

            while (caracter != -1) {
                caracter = file.read();

                if (Character.isLetter(((char) caracter)))
                    columna = 0;
                else if (Character.isDigit((char) caracter))
                    columna = 1;
                else {
                    switch ((char) caracter) {
                        case '_' -> columna = 2;
                        case '.' -> columna = 3;
                        case '\'' -> columna = 4;
                        case '+' -> columna = 5;
                        case '-' -> columna = 6;
                        case '*' -> columna = 7;
                        case '/' -> columna = 8;
                        case '>' -> columna = 9;
                        case '<' -> columna = 10;
                        case '=' -> columna = 11;
                        case '(' -> columna = 12;
                        case ')' -> columna = 13;
                        case ',' -> columna = 14;
                        case ';' -> columna = 15;
                        case ':' -> columna = 16;
                        case '{' -> columna = 17;
                        case '}' -> columna = 18;
                        case ' ' -> columna = 19;
                        case '\t' -> columna = 20;
                        case '\n' -> {
                            columna = 21;
                            ++numRenglon;
                        }
                        case '\r' -> columna = 22;
                        default -> columna = 23;
                    }
                }

                this.transicion = MATRIZ_TRANSICION[this.estado][columna];

                if (this.transicion < 100) {
                    this.estado = this.transicion;

                    if (this.estado == 0)
                        this.lexema = "";
                    else
                        this.lexema += (char) caracter;
                } else if (this.transicion < 500) {
                    if (this.transicion == 100)
                        this.ValidarSiEsPalabraReservada();

                    if ((transicion == 100) || (transicion == 101) || (transicion == 102) || (transicion == 108)
                            || (transicion == 110) || (transicion == 117) || (transicion == 9) || (transicion >= 200))
                        file.seek(file.getFilePointer() - 1);
                    else
                        lexema += (char) caracter;

                    this.InsertarNodo();
                    this.estado = 0;
                    this.lexema = "";
                } else {
                    ImprimirMensajeError();
                    this.estado = 0;
                    this.lexema = "";
                }
            }

            imprimirNodos();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Imprimir mensaje de error
    private void ImprimirMensajeError() {
        if ((this.caracter != -1) && (this.transicion >= 500)) {

            for (String[] error : ERRORES_LEXICOS)

                if (transicion == Integer.parseInt(error[1])) {
                    System.out.println("\u001B[31m\n" + "[ ERROR SEMÁNTICO " + transicion + " ]" + "\u001B[39m");
                    System.out.println(error[0] + " \"" + (char) this.caracter + "\" en la línea " + this.numRenglon);
                }

            System.out.println("\u001B[31m\n" + "[ PROGRAMA FINALIZADO ]" + "\u001B[39m");
            System.out.println("El programa ha finalizado debido a un error de carácter léxico");
            System.exit(1);
        }
    }

    // Insertar nodo en la lista
    private void InsertarNodo() {
        Nodo nodo = new Nodo(this.lexema, this.transicion, this.numRenglon);

        if (cabeza == null)
            this.cola = this.cabeza = nodo;
        else {
            this.cola.sig = nodo;
            this.cola = nodo;
        }
    }

    // Imprimir nodos de la lista
    public void imprimirNodos() {
        System.out.println("\u001B[36m" + "[ TOKENS INGRESADOS ]" + "\u001B[39m");
        System.out.printf("%-5s %-15s %-5s\n", "Token", "Lexema", "Linea");

        this.cola = this.cabeza;

        while (this.cola != null) {
            System.out.printf("%-5d %-15s %-5d\n", cola.token, cola.lexema, cola.renglon);
            this.cola = this.cola.sig;
        }
    }

    // Validar si es una palabra reservada
    private void ValidarSiEsPalabraReservada() {
        for (String[] palabraReservada : PALABRAS_RESERVADAS)
            if (this.lexema.equals(palabraReservada[0]))
                this.transicion = Integer.parseInt(palabraReservada[1]);
    }
}
