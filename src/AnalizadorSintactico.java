import java.io.*;
import java.util.Stack;

public class AnalizadorSintactico extends AnalizadorSemantico {
    private static final String direccionSalida = "C:\\MP";
    private static final String[][] ERRORES_SINTACTICOS = {
            {"Simbolo no valido", "500"},
            {"Se espera cierre de comentario", "501"},
            {"Se espera un digito despues del punto", "502"},
            {"Se espera cierre de cadena", "503"},
            {"Debe empezar con ALGORITMO", "504"},
            {"Se espera el nombre del programa", "505"},
            {"Se espera el inicio del parentesis", "506"},
            {"Se espera el final del parentesis", "507"},
            {"Se espera la palabra INICIO", "508"},
            {"Se espera la palabra FIN", "509"},
            {"Se espera la palabra ES", "510"},
            {"Se espera un FIN_SI", "511"},
            {"Se espera un operador relacional", "512"},
            {"Se espera la palabra ENTONCES", "513"},
            {"Se espera nombre de tipo simple", "514"},
            {"Se espera declaracion de variable", "515"},
            {"Se espera FIN_MIENTRAS", "516"},
            {"Se espera una asignacion", "600"},
            {"Se esperaba algo que Escribir", "601"}
    };
    private static final String[][] ERRORES_SEMANTICOS = {
            {"El nombre de la variable es igual al nombre del programa", "517"},
            {"Variable multideclarada", "518"},
            {"Variable sin declarar", "519"},
            {"Incompatibilidad de tipos", "520"}
    };

    private Nodo colaLexemas, auxiliarLexemas, listaErrores, cabezaLista;
    private NodoVar nodoVariable;
    boolean errorEncontrado = false;
    private String lexemaAuxiliar;
    private String lexemaActual = "";
    private String nombrePrograma;
    private String codigoIntermedio = "", codigoEnsamblador = "";
    private String operadorActual = "";
    private final Stack<String> pilaOperandos = new Stack<>();
    private final Stack<String> pilaVariablesStrings = new Stack<>();
    private final Stack<String> pilaVariablesCadenas = new Stack<>();
    private int tipoActual, contadorIf, contadorWhile, contadorCadenas = 1;
    private final Stack<Integer> pilaTokens = new Stack<>();

    private void Error(int linea) {
        while (auxiliarLexemas.sig != null) {
            if (((auxiliarLexemas.token >= 101 && auxiliarLexemas.token <= 113) || (auxiliarLexemas.token == 221)
                    || (auxiliarLexemas.token >= 200 && auxiliarLexemas.token <= 202) || (auxiliarLexemas.token == 119))
                    && (auxiliarLexemas.renglon == linea)) {
                lexemaActual += " " + auxiliarLexemas.lexema + " ";
                pilaTokens.add(auxiliarLexemas.token);
            }
            auxiliarLexemas = auxiliarLexemas.sig;
        }
    }

    private void errorSemantico(int num_error) {
        for (String[] errores : ERRORES_SEMANTICOS) {
            if (num_error == Integer.parseInt(errores[1])) {
                if (auxiliarLexemas != null)
                    Error(auxiliarLexemas.renglon);

                System.out.println("\u001B[31m\n" + "[ ERROR SEMÁNTICO " + num_error + " ]" + "\u001B[39m");
                System.out.println(errores[0] + " en la línea " + colaLexemas.renglon);
            }
        }

        System.out.println("\u001B[31m\n" + "[ PROGRAMA FINALIZADO ]" + "\u001B[39m");
        System.out.println("El programa ha finalizado debido a un error de carácter semántico");
        System.exit(1);
    }

    private void errorSintactico(int num_error) {
        for (String[] errores : ERRORES_SINTACTICOS) {
            if (num_error == Integer.parseInt(errores[1])) {
                Nodo Nodo = new Nodo(errores[0], num_error, colaLexemas.renglon);
                
                if (cabezaLista == null) {
                    cabezaLista = Nodo;
                    listaErrores = cabezaLista;
                } else {
                    listaErrores.sig = Nodo;
                    listaErrores     = Nodo;
                }

                System.out.println("\u001B[31m\n" + "[ ERROR SINTÁCTICO " + num_error + " ]" + "\u001B[39m");
                System.out.println(errores[0] + " en la línea " + colaLexemas.renglon);
            }
        }

        System.out.println("\u001B[31m\n" + "[ PROGRAMA FINALIZADO ]" + "\u001B[39m");
        System.out.println("El programa ha finalizado debido a un error de carácter sintáctico");
        System.exit(1);
    }

    public AnalizadorSintactico(Nodo cabezaLista) {
        colaLexemas = cabezaLista;
        try {
            while (colaLexemas != null) {
                if (colaLexemas.token == 212) { // ALGORITMO
                    colaLexemas = colaLexemas.sig;

                    if (colaLexemas.token == 100) { // Nombre del programa
                        nombrePrograma = colaLexemas.lexema;
                        colaLexemas = colaLexemas.sig;

                        if (colaLexemas.token == 114) { // (
                            colaLexemas = colaLexemas.sig;

                            if (colaLexemas.token == 115) { // )
                                colaLexemas = colaLexemas.sig;

                                if (colaLexemas.token == 217) { // ES (Declaracion de variables)
                                    colaLexemas = colaLexemas.sig;
                                    declaracionVariables();

                                    if (colaLexemas.token == 213) { // INICIO (Codigo del programa)
                                        colaLexemas = colaLexemas.sig;
                                        imprimirListaVariables();
                                        inicializar();
                                        imprimirListaPolish();
                                        generarCodigoIntermedio();
                                        generarCodigoEnsamblador();

                                        if (colaLexemas.token == 214) //FIN
                                            break;
                                        else {
                                            errorSintactico(509);
                                            break;
                                        }
                                    } else {
                                        errorSintactico(508);
                                        break;
                                    }
                                } else {
                                    errorSintactico(510);
                                    break;
                                }
                            } else {
                                errorSintactico(507);
                                break;
                            }
                        } else {
                            errorSintactico(506);
                            break;
                        }
                    } else {
                        errorSintactico(505);
                        break;
                    }
                } else {
                    errorSintactico(504);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Fin de archivo inesperado. " + e);
            errorEncontrado = true;
        }
    }

    private void inicializar() {
        switch (colaLexemas.token) {
            case 100 -> { // Variable
                auxiliarLexemas = colaLexemas;
                variableSinDeclarar();
                pushPilaInicial(colaLexemas.token);
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                colaLexemas = colaLexemas.sig;
                asignacion();
            }
            case 215 -> { //LEER
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                colaLexemas = colaLexemas.sig;
                leer();
                generarPolishFinal();
                if (colaLexemas.token == 118) {//;
                    colaLexemas = colaLexemas.sig;
                    inicializar();
                }
            }
            case 216 -> { // ESCRIBIR
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                colaLexemas = colaLexemas.sig;
                escribir();
                generarPolishFinal();
                if (colaLexemas.token == 118) {
                    colaLexemas = colaLexemas.sig;
                    inicializar();
                }
            }
            case 205 -> {//SI
                auxiliarLexemas = colaLexemas;
                si();
                if (colaLexemas.token == 118) {
                    colaLexemas = colaLexemas.sig;
                    inicializar();
                }
            }
            case 209 -> {//MIENTRAS
                auxiliarLexemas = colaLexemas;
                colaLexemas = colaLexemas.sig;
                mientras();
                if (colaLexemas.token == 118) {
                    colaLexemas = colaLexemas.sig;
                    inicializar();
                }
            }
            default -> {
            }
        }
    }
    
     // cambios 
    private void escribir() {
        if (colaLexemas.token == 116) { // ,
            entradaPila(colaLexemas.token, colaLexemas.lexema);

            colaLexemas = colaLexemas.sig;

            if (colaLexemas.token == 118) { // ;
                colaLexemas = colaLexemas.sig;
                escribir();
            }
        }

        if (colaLexemas.token == 103) { // CADENA o string
            entradaPila(colaLexemas.token, colaLexemas.lexema);

            colaLexemas = colaLexemas.sig;

            if (colaLexemas.token == 116) { // ,
                entradaPila(colaLexemas.token, colaLexemas.lexema);

                colaLexemas = colaLexemas.sig;

                if (colaLexemas.token == 118) { // ;
                    colaLexemas = colaLexemas.sig;
                    escribir();
                }
            }

            if (colaLexemas.token == 118) { // ;
                colaLexemas = colaLexemas.sig;
                escribir();
            }
        }

        if (colaLexemas.token == 100) { // variable
            entradaPila(colaLexemas.token, colaLexemas.lexema);

            variableSinDeclarar();

            colaLexemas = colaLexemas.sig;

            if (colaLexemas.token == 116) { // ,
                colaLexemas = colaLexemas.sig;
                escribir();
            }
        }
    }

    private void leer() {
        if (colaLexemas.token == 100) {
            entradaPila(colaLexemas.token, colaLexemas.lexema);
            variableSinDeclarar();
            colaLexemas = colaLexemas.sig;
            if (colaLexemas.token == 116) {
                colaLexemas = colaLexemas.sig;
                leer();
            }
        }
    }

    private void asignacion() {
        if (colaLexemas.token == 119) {// :=
            pushPilaInicial(colaLexemas.token);
            entradaPila(colaLexemas.token, colaLexemas.lexema);
            colaLexemas = colaLexemas.sig;
            expresionNumerica1();
            convertirInfijoPostfijo();
            generarPolishFinal();
            if (colaLexemas.token == 118) {// ;
                colaLexemas = colaLexemas.sig;
                inicializar();
            }
        } else {
            errorSintactico(600);
        }
    }

    private void mientras() {
        expresionLogica1();
        convertirInfijoPostfijo();
        insertarListaPolish("D" + (++contadorWhile), 0);
        generarPolishFinal();
        insertarListaPolish("Brf C" + (contadorWhile), 0);
        if (colaLexemas.token == 210) { // HACER
            colaLexemas = colaLexemas.sig;
            inicializar();
            insertarListaPolish("Bri D" + (contadorWhile), 0);
            if (colaLexemas.token == 211) { // FIN_MIENTRAS
                insertarListaPolish("C" + (contadorWhile), 0);
                --contadorWhile;
                colaLexemas = colaLexemas.sig;
            } else {
                errorSintactico(516);
            }
        }
    }

    private void si() {
        colaLexemas = colaLexemas.sig;
        lexemaActual = colaLexemas.lexema;
        expresionLogica1();
        convertirInfijoPostfijo();
        generarPolishFinal();
        insertarListaPolish("Brf A" + (++contadorIf), 0);
        if (colaLexemas.token == 206) {// ENTONCES
            colaLexemas = colaLexemas.sig;
            inicializar();
            if (colaLexemas.token != 208) { // SINO
                insertarListaPolish("A" + (contadorIf), 0);
            }
            insertarListaPolish("Bri B" + (contadorIf), 0);
            if (colaLexemas.token == 208) {// SINO
                colaLexemas = colaLexemas.sig;
                insertarListaPolish("A" + (contadorIf), 0);
                inicializar();
            }
            if (colaLexemas.token == 207) {// FIN_SI
                insertarListaPolish("B" + (contadorIf), 0);
                --contadorIf;
                colaLexemas = colaLexemas.sig;
            } else {
                errorSintactico(511);
            }
        } else {
            errorSintactico(513);
        }
    }

    private void expresionLogica1() {
        switch (colaLexemas.token) {
            case 114 -> {// (
                colaLexemas = colaLexemas.sig;
                expresionLogica1();
                if (colaLexemas.token == 115) {// )
                    colaLexemas = colaLexemas.sig;
                    expresionLogica2();
                }
            }
            case 200 -> {// NOT
                pushPilaInicial(colaLexemas.token);
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                colaLexemas = colaLexemas.sig;
                expresionLogica1();
                expresionLogica2();
            }
            case 100 -> {// Identificadores
                if ((colaLexemas.sig.token >= 108) && (colaLexemas.sig.token <= 113)) {// Operadores Operadores_Relacionales
                    expresionRelacional();
                } else {
                    expresionLogica2();
                }
            }
            case 203 ->// true
                    expresionLogica2();
            case 204 ->// false
                    expresionLogica2();
            default -> {
                expresionRelacional();
                expresionLogica2();
            }
        }
    }

    private void expresionRelacional() {
        expresionNumerica1();
        if (colaLexemas.token >= 108 && colaLexemas.token <= 113) {// Operadores Operadores_Relacionales
            pushPilaInicial(colaLexemas.token);
            entradaPila(colaLexemas.token, colaLexemas.lexema);
            colaLexemas = colaLexemas.sig;
            expresionNumerica1();
        } else {
            errorSintactico(512);
        }
    }

    private void expresionLogica2() {
        expresionNumerica1();
        if (colaLexemas.token >= 200 && colaLexemas.token <= 202) {// NOT, AND, OR
            pushPilaInicial(colaLexemas.token);
            entradaPila(colaLexemas.token, colaLexemas.lexema);
            colaLexemas = colaLexemas.sig;
            expresionLogica1();
            expresionLogica2();
        }
    }

    private void entradaPila(Integer Token, String Lexemas) {
        pilaTokens2.push(Token);
        this.pilaLexemas.push(Lexemas);
    }

    private void expresionNumerica1() {
        switch (colaLexemas.token) {
            case 114 -> {
                pushPilaInicial(colaLexemas.token);
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                expresionNumerica1();
                if (colaLexemas.token == 115) {
                    pushPilaInicial(colaLexemas.token);
                    entradaPila(colaLexemas.token, colaLexemas.lexema);
                    expresionNumerica2();
                }
            }
            case 105 -> {// -
                pushPilaInicial(colaLexemas.token);
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                expresionNumerica1();
                expresionNumerica2();
            }
            case 100 -> {
                variableSinDeclarar();
                pushPilaInicial(colaLexemas.token);
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                colaLexemas = colaLexemas.sig;
                expresionNumerica2();
            }
            case 203, 204 -> {// TRUE
                pushPilaInicial(221);
                pilaTokens2.push(221);
                pilaLexemas.push(colaLexemas.lexema);
                colaLexemas = colaLexemas.sig;
                expresionLogica2();
            }
            case 103, 102, 101 -> { // Cadena o string
                pushPilaInicial(colaLexemas.token);
                entradaPila(colaLexemas.token, colaLexemas.lexema);
                colaLexemas = colaLexemas.sig;
                expresionNumerica2();
            }
            // DECIMAL o Double
            // ENTERO o Int
            default -> {
            }
        }
    }

    private void expresionNumerica2() {
        if (colaLexemas.token >= 104 && colaLexemas.token <= 107) {// + - * /
            pushPilaInicial(colaLexemas.token);
            entradaPila(colaLexemas.token, colaLexemas.lexema);
            colaLexemas = colaLexemas.sig;
            expresionNumerica1();
            expresionNumerica2();
        }
    }

    private void declaracionVariables() {
        if (colaLexemas.token == 100) { // variable
            if (colaLexemas.lexema.equalsIgnoreCase(nombrePrograma)) {
                errorSemantico(517);
            } else {
                variableMultideclarada();
                lexemaAuxiliar = colaLexemas.lexema;
            }

            colaLexemas = colaLexemas.sig;

            if (colaLexemas.token == 117) {// :
                colaLexemas = colaLexemas.sig;
                nombreTipoSimple();
                insertarVariable(lexemaAuxiliar, tipoActual);

                if (colaLexemas.token == 118) {// ;
                    colaLexemas = colaLexemas.sig;
                    declaracionVariables();
                }
            } else {
                errorSintactico(515);
            }
        } else {
            errorSintactico(515);
        }
    }

    private void nombreTipoSimple() {
        switch (colaLexemas.token) {
            case 218 -> {//Entero
                colaLexemas.token = 101;
                tipoActual = colaLexemas.token;
                colaLexemas = colaLexemas.sig;
            }
            case 219 -> {//Decimal
                colaLexemas.token = 102;
                tipoActual = colaLexemas.token;
                colaLexemas = colaLexemas.sig;
            }
            case 220 -> {//Cadena
                colaLexemas.token = 103;
                tipoActual = colaLexemas.token;
                colaLexemas = colaLexemas.sig;
            }
            case 221 -> {//Boolean
                tipoActual = colaLexemas.token;
                colaLexemas = colaLexemas.sig;
            }
            default -> errorSintactico(514);
        }
    }

    private void incompatibilidadTipos(Stack lista) {
        boolean error = false;
        int columna_fila = 0, fila_lista = 0, operando1, operando2;

        for (int i = 0; i < pilaSalida.size(); i++) {
            int operador = (int) lista.get(i);
            if ((operador >= 104 && operador <= 113) || (operador == 119) || (operador >= 200 && operador <= 202)) {
                operando1 = pilaSalidaAux.pop();
                operando2 = pilaSalidaAux.pop();

                switch (operando1) {
                    case 101 -> columna_fila = 0;
                    case 102 -> columna_fila = 1;
                    case 103 -> columna_fila = 2;
                    case 221 -> columna_fila = 3;
                }
                switch (operando2) {
                    case 101 -> fila_lista = 0;
                    case 102 -> fila_lista = 1;
                    case 103 -> fila_lista = 2;
                    case 221 -> fila_lista = 3;
                }
                switch (operador) {
                    case 104 -> {
                        if (SistemaTipos.Sumas[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            pilaSalidaAux.push(SistemaTipos.Sumas[fila_lista][columna_fila]);
                        }
                    }
                    case 105 -> {
                        if (SistemaTipos.Resta_Multiplicacion[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            pilaSalidaAux.push(SistemaTipos.Resta_Multiplicacion[fila_lista][columna_fila]);
                        }
                    }
                    case 106 -> {
                        if (SistemaTipos.Resta_Multiplicacion[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            pilaSalidaAux.push(SistemaTipos.Resta_Multiplicacion[fila_lista][columna_fila]);
                        }
                    }
                    case 107 -> {
                        if (SistemaTipos.Divisiones[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            pilaSalidaAux.push(SistemaTipos.Divisiones[fila_lista][columna_fila]);
                        }
                    }
                    case 119 -> {
                        if (!SistemaTipos.Asignaciones[fila_lista][columna_fila]) {
                            error = true;
                        }
                    }
                    case 110, 108, 109, 111 -> {
                        if (SistemaTipos.Operadores_Relacionales[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            pilaSalidaAux.push(SistemaTipos.Operadores_Relacionales[fila_lista][columna_fila]);
                        }
                    }
                    case 112, 113 -> {
                        if (SistemaTipos.Igual_Diferente[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            pilaSalidaAux.push(SistemaTipos.Igual_Diferente[fila_lista][columna_fila]);
                        }
                    }// NOT
                    // AND
                    case 200, 201, 202 -> {// OR
                        if (!SistemaTipos.Operadores_Logicos[fila_lista][columna_fila]) {
                            error = true;
                        } else {
                            pilaSalidaAux.push(221);
                        }
                    }
                }
            } else {
                pilaSalidaAux.push(operador);
            }
            if (error) { // p.linea
                errorSemantico(520);
                break;
            }
        }
    }

    private void variableMultideclarada() {
        nodoVariable = cab_var;
        while (nodoVariable != null) {
            if (colaLexemas.lexema.equals(nodoVariable.lexema)) {
                errorSemantico(518);
            }
            nodoVariable = nodoVariable.sig;
        }
        nodoVariable = cab_var;
    }

    private void variableSinDeclarar() {
        nodoVariable = cab_var;
        boolean Validar_variable = false;
        while (nodoVariable != null) {
            if (colaLexemas.lexema.equals(nodoVariable.lexema)) {
                Validar_variable = true;
                colaLexemas.token       = nodoVariable.token;
                break;
            }
            nodoVariable = nodoVariable.sig;
        }
        if (!Validar_variable) {
            errorSemantico(519);
        }
    }

    // Metodo para convertir la expresión infija a postfija
    private void convertirInfijoPostfijo() {
        pilaInvertida.push(115); // Agregar el token ')' a la pila invertida

        // Llenar las pilas invertidas con los tokens y lexemas
        while (!pilaInicial.empty())
            insertarPilaInvertida(pilaInicial.pop());

        pilaInvertida.push(114); // Agregar el token '(' a la pila invertida

        // Procesar la pila invertida y generar la expresión postfija
        while (!pilaInvertida.empty()) {
            switch (obtenerJerarquiaOperacion(pilaInvertida.peek())) {
                case 0, 9 -> pushPilaSalidas(pilaInvertida.pop()); // Operandos y paréntesis
                case 1, 2 -> pushPilaOperadores(pilaInvertida.pop()); // Operadores :=
                case 3 -> { // )
                    // Desapilar operadores hasta encontrar el (
                    while (!pilaOperadores.peek().equals(114)) {
                        pushPilaSalidas(pilaOperadores.pop()); // Agregar operadores a la salida (postfijo)
                    }
                    pilaOperadores.pop(); // Desapilar (
                    pilaInvertida.pop(); // Desapilar )
                }
                // Operadores lógicos y aritméticos
                case 4, 5, 6, 7, 8 -> {
                    // Desapilar operadores de mayor jerarquía y agregarlos a la salida (postfijo)
                    while (obtenerJerarquiaOperacion(pilaOperadores.peek()) >= obtenerJerarquiaOperacion(pilaInvertida.peek())) {
                        pushPilaSalidas(pilaOperadores.pop());
                    }
                    pushPilaOperadores(pilaInvertida.pop()); // Apilar operador actual
                }
            }
        }

        incompatibilidadTipos(pilaSalida); // Realizar verificaciones de incompatibilidad de tipos
        pilaSalida.removeAllElements(); // Limpiar la lista de salida
        pilaSalidaAux.removeAllElements(); // Limpiar la lista auxiliar de salida
    }

    // Método para generar el código intermedio
    private void generarPolishFinal() {
        pilaTokensInvertidos.push(115); // Agregar el token ')' a la pila invertida
        pilaLexemasInvertidos.push(")");

        // Llenar las pilas invertidas con los tokens y lexemas
        while (!pilaLexemas.empty() || !pilaTokens2.empty()) {
            pilaTokensInvertidos.push(pilaTokens2.pop());
            pilaLexemasInvertidos.push(pilaLexemas.pop());
        }

        pilaTokensInvertidos.push(114); // Agregar el token '(' a la pila invertida
        pilaLexemasInvertidos.push("(");

        // Procesar la pila invertida y generar el código intermedio
        while (!pilaTokensInvertidos.empty()) {
            int jerarquia = obtenerJerarquiaOperacion(pilaTokensInvertidos.peek());

            switch (jerarquia) {
                case 0 -> insertarListaPolish(pilaLexemasInvertidos.pop(), pilaTokensInvertidos.pop()); // Operandos
                case 1, 2 -> { // :=
                    pilaTokensOperandos.push(pilaTokensInvertidos.pop());
                    pilaLexemasOperandos.push(pilaLexemasInvertidos.pop());
                }
                case 3 -> { // )
                    // Desapilar operandos hasta encontrar el (
                    while (!pilaTokensOperandos.peek().equals(114)) {
                        insertarListaPolish(pilaLexemasOperandos.pop(), pilaTokensOperandos.pop());
                    }
                    pilaTokensOperandos.pop(); // Desapilar (
                    pilaLexemasOperandos.pop();
                    pilaTokensInvertidos.pop(); // Desapilar )
                    pilaLexemasInvertidos.pop();
                }
                // Operadores lógicos y aritméticos
                case 4, 5, 6, 7, 8 -> {
                    // Desapilar operadores de mayor jerarquía y agregarlos a la lista Polish
                    while (obtenerJerarquiaOperacion(pilaTokensOperandos.peek()) >= obtenerJerarquiaOperacion(pilaTokensInvertidos.peek())) {
                        insertarListaPolish(pilaLexemasOperandos.pop(), pilaTokensOperandos.pop());
                    }
                    pilaTokensOperandos.push(pilaTokensInvertidos.pop()); // Apilar operador actual
                    pilaLexemasOperandos.push(pilaLexemasInvertidos.pop());
                }
            }
        }
    }

    //***********************************************************************************************************************
    // GENERACION DEL CODIGO ENSAMBLADOR
    //***********************************************************************************************************************

    // Reemplaza una cadena en el texto ensamblador
    private String reemplazar(String buscar, String reemplazar) {
        return codigoIntermedio.replaceAll(buscar, reemplazar);
    }

    // Buscar cadenas en la lista polish y apilarlas
    private void buscarCadenas() {
        String cadena = "";
        NodoVar nodoActual = cabeceraPolish;

        while (nodoActual != null) {

            if (nodoActual.token == 103) {  // si es cadena
                cadena = nodoActual.lexema;
                nodoActual = nodoActual.sig;

                if (nodoActual.token == 216)    // si el siguiente token es '
                    pilaVariablesCadenas.add(cadena);   // apilar cadena
            }

            nodoActual = nodoActual.sig;    // siguiente nodo
        }
    }

    // Buscar lexema en la lista polish
    private String buscarLexema(String Lexema) {
        String lexemaEncontrado = "";
        NodoVar nodoPolishActual;
        NodoVar nodoBuscar = this.cabeceraPolish;

        while (nodoBuscar != null) {

            if (nodoBuscar.lexema.equals(Lexema)) { // lexema encontrado
                nodoPolishActual = nodoBuscar.sig;  // avanzar nodo

                if (nodoPolishActual.lexema.startsWith("'")) {  // siguiente lexema empieza con '
                    lexemaEncontrado = nodoPolishActual.lexema; // almacenar lexema
                    break;
                }
            }

            nodoBuscar = nodoBuscar.sig;    // avanzar nodo
        }

        return lexemaEncontrado;
    }

    private void operacionAsignacionASM() {
        String op2 = pilaOperandos.pop();
        String op1 = pilaOperandos.pop();

        if (existeVariableString(op1))
            codigoIntermedio = reemplazar(op1 + ";/auxiliar ", "\n" + op1 + " db " + op2);
        else
            codigoIntermedio += "Asignar" + " " + op1 + ", " + op2 + "\n";

        codigoIntermedio = this.reemplazar("VERDADERO", "1");
        codigoIntermedio = this.reemplazar("FALSO", "0");
    }

    private boolean existeVariableString(String variable) {
        for (String variableString : this.pilaVariablesStrings)
            if (variableString.equals(variable))
                return true;

        return false;
    }

    private void operacionLecturaASM() {
        String op = pilaOperandos.pop();

        if (existeVariableString(op))
            codigoIntermedio += "LeerCadena" + " " + op + "\n";
        else
            codigoIntermedio += "LeerNumero" + " " + op + "\n";
    }

    private void operacionEscrituraASM() {
        String op = pilaOperandos.pop();

        if (existeVariableString(op) && !op.startsWith("'"))
            codigoIntermedio += "EscribirCadena" + " " + op + "\n";
        else if (!existeVariableString(op) && !op.startsWith("'"))
            codigoIntermedio += "EscribirNumero" + " " + op + "\n";
        else
            codigoIntermedio += "EscribirCadena" + " cadena" + (contadorCadenas++) + "\n";
    }

    private void etiquetasSaltos() {
        if (operadorActual.startsWith("Brf")) {
            String p = operadorActual.substring(4, 6);
            codigoIntermedio += "JF" + " resultadoAux" + ", " + p + "\n";
        } else if (operadorActual.startsWith("Bri")) {
            String p = operadorActual.substring(4, 6);
            codigoIntermedio += "JMP" + " " + p + "\n";
        } else if (operadorActual.startsWith("A") || operadorActual.startsWith("B")
                || operadorActual.startsWith("C") || operadorActual.startsWith("D")) {
            String p = operadorActual.substring(0, 2);
            codigoIntermedio += p + ":\n";
        }
    }

    // Generar codigo intermedio
    private void generarCodigoIntermedio() {
        this.p_var = this.cab_var;
        this.buscarCadenas();

        codigoIntermedio += "resultadoAux dw 0\n";

        while (p_var != null) {
            if (p_var.token == 103) { // cadena o string
                codigoIntermedio += p_var.lexema + " db " + buscarLexema(p_var.lexema) + ", 13, 10, '$', 0\n";
                pilaVariablesStrings.push(p_var.lexema);
            } else {    // numerica
                codigoIntermedio += p_var.lexema + " dw 0\n";
            }
            p_var = p_var.sig;
        }

        int contadorCadenas = 1;

        for (String pilaVariablesCadena : pilaVariablesCadenas) {
            codigoIntermedio += "cadena" + (contadorCadenas++) + " db " + pilaVariablesCadena + ", 13, 10, '$', 0\n";
        }

        this.codigoIntermedio += """
                ;/Var
                """;

        colaPolish = cabeceraPolish;
        while (colaPolish != null) {
            if (colaPolish.token >= 104 && colaPolish.token < 114 || colaPolish.token == 119
                    || colaPolish.token >= 200 && colaPolish.token <= 202 || colaPolish.token == 215
                    || colaPolish.token == 216 || colaPolish.token == 0) {
                operadorActual = colaPolish.lexema;

                switch (operadorActual) { // nombres de las macros
                    case "+" -> operacionSuma();
                    case "-" -> operacionResta();
                    case "*" -> operacionMultiplicacion();
                    case "/" -> operacionDivision();
                    case ":=" -> operacionAsignacionASM();
                    case "LEER" -> operacionLecturaASM();
                    case "ESCRIBIR" -> operacionEscrituraASM();
                    case "=" -> comparacionIgualQue();
                    case ">" -> comparacionMayorQue();
                    case "<" -> comparacionMenorQue();
                    case ">=" -> comparacionMayorIgualQue();
                    case "<=" -> comparacionMenorIgualQue();
                    case "<>" -> comparacionDistintoQue();
                }
                etiquetasSaltos();
            } else {
                pilaOperandos.push(colaPolish.lexema);
            }

            colaPolish = colaPolish.sig;
        }

        System.out.println("\u001B[36m" + "\n[ CÓDIGO INTERMEDIO OPTIMIZADO ]" + "\u001B[39m");
        System.out.println(codigoIntermedio);
    }

    private void operacionSuma() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        // folding
        /*try {
            int resultado = Integer.parseInt(operando1) + Integer.parseInt(operando2);
            this.pilaOperandos.push(String.valueOf(resultado));
        } catch (Exception ex) {
            this.codigoIntermedio += "Sumar " + operando1 + ", " + operando2 + ", resultado" + this.contadorGeneral + "\n";
            this.pilaOperandos.push("resultado" + this.contadorGeneral);
            //this.codigoIntermedio = reemplazar(";/Var",
                    //"resultado" + this.contadorGeneral + " dw ?\n;/Var");
        }*/

        // sin folding
        this.codigoIntermedio += "Sumar " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
        //this.codigoIntermedio = reemplazar(";/Var",
                //"resultado" + this.contadorGeneral + " dw ?\n;/Var");
    }

    private void operacionResta() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        // folding
        /*try {
            int resultado = Integer.parseInt(operando1) - Integer.parseInt(operando2);
            this.pilaOperandos.push(String.valueOf(resultado));
        } catch (Exception ex) {
            this.codigoIntermedio += "Restar " + operando1 + ", " + operando2 + ", resultado" + this.contadorGeneral + "\n";
            this.pilaOperandos.push("resultado" + this.contadorGeneral);
            //this.codigoIntermedio = reemplazar(";/Var",
                    //"resultado" + this.contadorGeneral + " dw ?\n;/Var");
        }*/

        // sin folding
        this.codigoIntermedio += "Restar " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
        //this.codigoIntermedio = reemplazar(";/Var",
                //"resultado" + this.contadorGeneral + " dw ?\n;/Var");
    }

    private void operacionMultiplicacion() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        // strength reduction
        /*try {
            int numSumas = Integer.parseInt(operando2);

            for (int i = 1; i <= numSumas - 1; i++) {
                NodoVar aux = colaPolish.sig;
                NodoVar nuevo = new NodoVar("+", 104);
                nuevo.sig = aux;
                colaPolish.sig = nuevo;
            }

            for (int i = 1; i <= numSumas; i++) {
                NodoVar aux = colaPolish.sig;
                NodoVar nuevo = new NodoVar(operando1, 101);
                nuevo.sig = aux;
                colaPolish.sig = nuevo;
            }

        } catch (Exception ex) {
            //this.codigoIntermedio += "Multiplicar " + operando1 + ", " + operando2 + ", resultado" + this.contadorGeneral + "\n";
            this.pilaOperandos.push("resultado" + this.contadorGeneral);
            this.codigoIntermedio = reemplazar(";/Var",
                    //"resultado" + this.contadorGeneral + " dw ?\n;/Var");
        }*/

        // sin strength reduction
        this.codigoIntermedio += "Multiplicar " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
        //this.codigoIntermedio = reemplazar(";/Var",
                //"resultado" + this.contadorGeneral + " dw ?\n;/Var");
    }

    private void operacionDivision() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        // folding
        /*try {
            if (Integer.parseInt(operando1) % Integer.parseInt(operando2) == 0) {
                int resultado = Integer.parseInt(operando1) / Integer.parseInt(operando2);
                this.pilaOperandos.push(String.valueOf(resultado));
            } else
                throw new Exception();

        } catch (Exception ex) {
            this.codigoIntermedio += "Dividir " + operando1 + ", " + operando2 + ", resultado" + this.contadorGeneral + "\n";
            this.pilaOperandos.push("resultado" + this.contadorGeneral);
            //this.codigoIntermedio = reemplazar(";/Var",
                    //"resultado" + this.contadorGeneral + " dw ?\n;/Var");
        }*/

        // sin folding
        this.codigoIntermedio += "Dividir " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
    }

    private void comparacionIgualQue() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        this.codigoIntermedio += "IgualQue " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
    }

    private void comparacionDistintoQue() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        this.codigoIntermedio += "DistintoQue " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
    }

    private void comparacionMayorQue() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        this.codigoIntermedio += "MayorQue " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
    }

    private void comparacionMayorIgualQue() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        this.codigoIntermedio += "MayorIgualQue " + operando1 + ", " + operando2 + ", resultadoAux";
        this.pilaOperandos.push("resultadoAux");
    }

    private void comparacionMenorQue() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        this.codigoIntermedio += "MenorQue " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
    }

    private void comparacionMenorIgualQue() {
        String operando2 = this.pilaOperandos.pop();
        String operando1 = this.pilaOperandos.pop();

        this.codigoIntermedio += "MenorIgualQue " + operando1 + ", " + operando2 + ", resultadoAux\n";
        this.pilaOperandos.push("resultadoAux");
    }

    private void generarCodigoEnsamblador() {
        codigoEnsamblador += """
                INCLUDE macros.MAC
                .MODEL SMALL
                .STACK 100h
                
                .DATA
                """;

        codigoEnsamblador += codigoIntermedio;
        codigoEnsamblador = codigoEnsamblador.replaceAll(";/Var", """
                
                .CODE
                MOV AX, @DATA
                MOV DS, AX
                CALL METODOS
                MOV AX, 4C00H
                INT 21H
                
                METODOS PROC""");

        codigoEnsamblador += """
                RET
                METODOS ENDP
                
                END
                """;

        System.out.println("\u001B[36m" + "[ CÓDIGO ENSAMBLADOR ]" + "\u001B[39m");
        System.out.println(codigoEnsamblador);

        generarArchivoEnsamblador();
        compilarYEjecutar();
    }

    // Generar archivo ensamblador resultante
    private void generarArchivoEnsamblador() {
        final String direccionArchivo = direccionSalida + "\\" + nombrePrograma.toLowerCase() + ".asm"; // direccion absoluta

        // crear archivo
        try {
            File archivo = new File(direccionArchivo);  // crear archivo
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));

            bw.write(codigoEnsamblador);                // escribir codigo en el archivo
            bw.close();

            System.out.println("\u001B[36m" + "[ ARCHIVO GENERADO ]" + "\u001B[39m");
            System.out.println("Archivo ensamblador generado en: " + direccionArchivo);
        } catch (Exception e) {                         // error
            System.out.println("Ha ocurrido un error al generar el archivo: " + e.getMessage());
        }
    }

    // Compilar archivo previamente generado y ejecutarlo
    private void compilarYEjecutar() {
        // compilar y ejecutar en dosbox
        try {
            String[] cmd = {                                    // comando a ejecutar
                    "dosbox",
                    "-c", "mount c " + direccionSalida,
                    "-c", "c:",
                    "-c", "masm " + nombrePrograma + ".asm;",
                    "-c", "link " + nombrePrograma + ".obj;",
                    "-c", nombrePrograma + ".exe"
            };

            ProcessBuilder builder = new ProcessBuilder(cmd);   // crear proceso
            builder.start();                                    // ejecutar comando
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error al intentar compilar el archivo: " + e.getMessage());
        }
    }

    //***********************************************************************************************************************
}
