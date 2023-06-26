

import java.io.IOException;
import java.io.RandomAccessFile;

public class AnalizadorLexico {
    private final static String direccionArchivo ="src\\codigo.txt";

    private Nodo cabeza = null, cola;
    private int estado = 0, columna, transicion, numRenglon = 1, caracter = 0;
    private String lexema = "";
    boolean errorEncontrado = false;
    private RandomAccessFile file=null;
    private static final int[][] MATRIZ_TRANSICION = {
        //       l      d	 _	 .       '      +        -        *	   /	  >      <       =        (	   )	   ,	  ;  	 :  	{      }     eb	  tab    nl   eof    oc
        ////     0      l        2       3       4       5       6       7         8       9       10      11      12      13      14      15   16      17    18     19  20     21    22    23  
        /*0*/ {  1,     2,      500,    500,     5,    104,     105,     106,     107,    6,     7,     112,     114,     115,    116,   118,    8,     9,    500,    0,   0,    0,    0,   500 },
        /*1*/ {  1,     1,       1,     100,    100,   100,     100,     100,     100,   100,   100,    100,     100,     100,    100,   100,   100,   100,   100,   100, 100,  100,  100,  100 },
        /*2*/ { 101,    2,      101,     3,     101,   101,     101,     101,     101,   101,   101,    101,     101,     101,    101,   101,   101,   101,   101,   101, 101,  101,  101,  101 },
        /*3*/ { 502,    4,      502,    502,    502,   502,     502,     502,     502,   502,   502,    502,     502,     502,    502,   502,   502,   502,   502,   502, 502,  502,  502,  502 },
        /*4*/ { 102,    4,      102,    102,    102,   102,     102,     102,     102,   102,   102,    102,     102,     102,    102,   102,   102,   102,   102,   102, 102,  102,  102,  102 },
        /*5*/ {  5,     5,       5,      5,     103,    5,       5,       5,       5,     5,     5,      5,       5,       5,      5,     5,     5,     5,     5,     5,   5,   503,   5,    5  },
        /*6*/ { 109,   109,     109,    109,    109,   109,     109,     108,     108,   108,   108,    109,     108,     108,    108,   108,   108,   108,   108,   108, 108,  108,  108,  108 },
        /*7*/ { 110,   110,     110,    110,    110,   110,     110,     110,     110,   113,   110,    111,     110,     110,    110,   110,   110,   110,   110,   110, 110,  110,  110,  110 },
        /*8*/ { 117,   117,     117,    117,    117,   117,     117,     117,     117,   117,   117,    119,     117,     117,    117,   117,   117,   117,   117,   117, 117,  117,  117,  117 },
        /*9*/ {  9,     9,       9,      9,      9,     9,       9,       9,       9,     9,     9,      9,       9,       9,      9,     9,     9,     9,     0,     9,   9,   501,   9,    9}
    };
    
    private static final String[][] PALABRAS_RESERVADAS = {
        //  0              1<--numero de columnas del arreglo 
        /*0*/{"NOT", "200"},
        /*1*/ {"AND", "201"},
        /*2*/ {"OR", "202"},
        /*3*/ {"VERDADERO", "203"},
        /*4*/ {"FALSO", "204"},
        /*5*/ {"SI", "205"},
        /*6*/ {"ENTONCES", "206"},
        /*7*/ {"FIN_SI", "207"},
        /*8*/ {"SINO", "208"},
        /*9*/ {"MIENTRAS", "209"},
        /*10*/ {"HACER", "210"},
        /*11*/ {"FIN_MIENTRAS", "211"},
        /*12*/ {"ALGORITMO", "212"},
        /*13*/ {"INICIO", "213"},
        /*14*/ {"FIN", "214"},
        /*15*/ {"LEER", "215"},
        /*16*/ {"ESCRIBIR", "216"},
        /*17*/ {"ES", "217"},
        /*18*/ {"ENTERO", "218"},
        /*19*/ {"DECIMAL", "219"},
        /*20*/ {"CADENA", "220"},
        /*20*/ {"LOGICO", "221"}
    };

    private static final String[][] ERRORES_LEXICOS = {
        //           0               1<--numero de columnas del arreglo 
        /*0*/{"Simbolo no valido", "500"},
        /*1*/ {"Se espera cierre de comentario", "501"},
        /*2*/ {"Se espera un digito despues del punto", "502"},
        /*3*/ {"Se espera cierre de cadena", "503"}
    };


    public AnalizadorLexico(){
        try {
            file = new RandomAccessFile(direccionArchivo, "r");
            while (caracter != -1) {//leer caracter por caracter mientras no sea eof, es decir, mientras el caracter sea el ultimo leido
                caracter = file.read();
                //es posicionar la columna de la matriz de acuerdo al caracter leido
                if (Character.isLetter(((char) caracter))) {//pregunta si es un caracter
                    columna = 0;//esto es de acuerdo a la matriz de transicion
                } else if (Character.isDigit((char) caracter)) {//pregunta si es un digito
                    columna = 1;
                } else {
                    switch ((char) caracter) {//las columnas son de acuerdo a las columnas de la matriz de transicion
                        case '_':
                            columna = 2;
                            break;
                        case '\'':
                            columna = 4;
                            break;
                        case '+':
                            columna = 5;
                            break;
                        case '-':
                            columna = 6;
                            break;
                        case '*':
                            columna = 7;
                            break;
                        case '/':
                            columna = 8;
                            break;
                        case '>':
                            columna = 9;
                            break;
                        case '<':
                            columna = 10;
                            break;
                        case '=':
                            columna = 11;
                            break;
                        case '.':
                            columna = 3;
                            break;
                        case '(':
                            columna = 12;
                            break;
                        case ')':
                            columna = 13;
                            break;
                        case ',':
                            columna = 14;
                            break;
                        case ';':
                            columna = 15;
                            break;
                        case ':':
                            columna = 16;
                            break;
                        case '{':
                            columna = 17;
                            break;
                        case '}':
                            columna = 18;
                            break;
                        case ' '://espacio
                            columna = 19;
                            break;
                        case 9://tab
                            columna = 20;
                            break;
                        case 10:// nuevalinea
                            columna = 21;
                            numRenglon = numRenglon + 1;
                            break;
                        case 13:// retorno de carro
                            columna = 22;
                            break;
                        default:
                            columna = 23;
                            break;
                    }//switch
                }//if character

                transicion = MATRIZ_TRANSICION[estado][columna];
                
                if (transicion < 100) {//cambiar de estado
                    estado = transicion;

                    if (estado == 0) {
                        lexema = "";
                    } else {
                        lexema = lexema + (char) caracter;
                    }
                } else if (transicion >= 100 && transicion < 500) {//estado final
                    if (transicion == 100) {
                        validarSiEsPalabraReservada();
                    }

                    if (transicion == 100 || transicion == 101 || transicion == 102 || transicion == 108 || transicion == 110
                            || transicion == 117 || transicion == 9 || transicion >= 200) {

                        file.seek(file.getFilePointer() - 1);
                    } else {
                        lexema = lexema + (char) caracter;
                    }
                    InsertarNodo();
                    estado = 0;
                    lexema = "";
                } else {
                    imprimirMensajeError();
                    break;
                }
            }
            ImprimirNodos();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void imprimirMensajeError() {
        if (caracter != -1 && transicion >= 500) {
            for (String[] errore : ERRORES_LEXICOS) {
                if (transicion == Integer.valueOf(errore[1])) {
                    System.out.println("El error encontrado es: " + errore[0] + " error " + transicion + " caracter " + (char) caracter + " en el renglon  "
                            + numRenglon);
                    
                }
            }
            errorEncontrado = true;
        }
    }

    private void InsertarNodo() {
        Nodo nodo = new Nodo(lexema, transicion, numRenglon);

        if (cabeza == null) {
            this.cola = this.cabeza = nodo;
        } else {
            this.cola.sig = nodo;
            this.cola = nodo;
        }
    }

    public void ImprimirNodos() {
        System.out.println("<<< TOKENS INGRESADOS >>>");

        this.cola = this.cabeza;

        while (this.cola != null) {
            System.out.printf("%-3d %-15s %-3d\n", cola.token, cola.lexema, cola.renglon);
            this.cola = this.cola.sig;
        }
    }

    private void validarSiEsPalabraReservada() {
        for (String[] palReservada : PALABRAS_RESERVADAS) {
            if (lexema.equals(palReservada[0])) {
                transicion = Integer.valueOf(palReservada[1]);
            }
        }
    }
    
    public Nodo getCabeza(){
        return cabeza;
    }
}

