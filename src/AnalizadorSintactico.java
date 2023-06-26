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
            {"El nombre de la variable es igual al ID", "517"},
            {"Variable Multideclarada", "518"},
            {"Variable sin declarar", "519"},
            {"Incompatibilidad de datos", "520"}
    };

    private Nodo cola, aux, listaErrores, cabeza;
    private NodoVar nodo;
    boolean errorEncontrado = false;
    private String lexemaAux, Lexema = "", nombrePrograma, textoEnsamblador = "", operador = "", resultadoASM;
    private Stack<String> buscarPolish = new Stack<>(), VariablesStrings = new Stack<>();
    private Stack<String> VariablesCadenas = new Stack<>();
    private int tipo, ContadorIf, ContadorWhile, contador, contadorcadenas = 1;
    private SistemaTipos Tipos = new SistemaTipos();
    private Stack<Integer> tokens = new Stack<>();

    private void Error(int linea) {
        while (aux.sig != null) {
            if (((aux.token >= 101 && aux.token <= 113) || (aux.token == 221)
                    || (aux.token >= 200 && aux.token <= 202) || (aux.token == 119)) && (aux.renglon == linea)) {
                Lexema = Lexema + " " + aux.lexema + " ";
                tokens.add(aux.token);
            }
            aux = aux.sig;
        }
    }

    private void Error_semantico(int num_error) {
        for (String[] errores : ERRORES_SEMANTICOS) {
            if (num_error == Integer.parseInt(errores[1])) {
                if (aux != null) {
                    Error(aux.renglon);
                }
                System.out.println("Error: " + errores[0] + " (" + Lexema + ") " + ", Numero de error: " + num_error + " , " + "En la línea: " + " " + cola.renglon);
                Lexema = "";
            }
        }
        errorEncontrado = true;
    }

    

    private void ErrorMensaje(int num_error) {
        for (String[] errores : ERRORES_SINTACTICOS) {
            if (num_error == Integer.parseInt(errores[1])) {
                Nodo Nodo = new Nodo(errores[0], num_error, cola.renglon);
                
                if (cabeza == null) {
                    cabeza       = Nodo;
                    listaErrores = cabeza;
                } else {
                    listaErrores.sig = Nodo;
                    listaErrores     = Nodo;
                }

                System.out.println("Error: " + errores[0] + ", Numero de error: "
                        + num_error + " " + "" + "En la línea: " + " " + cola.renglon);
            }
        }
        errorEncontrado = true;
    }

    public AnalizadorSintactico(Nodo cabeza) {
        cola = cabeza;
        try {
            while (cola != null) {
                if (cola.token == 212) { // ALGORITMO
                    cola = cola.sig;

                    if (cola.token == 100) { // Nombre del programa
                        nombrePrograma = cola.lexema;
                        cola           = cola.sig;

                        if (cola.token == 114) { // (
                            cola = cola.sig;

                            if (cola.token == 115) { // )
                                cola = cola.sig;

                                if (cola.token == 217) { // ES (Declaracion de variables)
                                    cola = cola.sig;
                                    Dec_variable();

                                    if (cola.token == 213) { // INICIO (Codigo del programa)
                                        cola = cola.sig;
                                        Imprimir_Listavariables();
                                        Inicializar();
                                        Imprimir_ListaPolish();
                                        GenerarCodigoObjeto();

                                        if (cola.token == 214) //FIN
                                            break;
                                        else {
                                            ErrorMensaje(509);
                                            break;
                                        }
                                    } else {
                                        ErrorMensaje(508);
                                        break;
                                    }
                                } else {
                                    ErrorMensaje(510);
                                    break;
                                }
                            } else {
                                ErrorMensaje(507);
                                break;
                            }
                        } else {
                            ErrorMensaje(506);
                            break;
                        }
                    } else {
                        ErrorMensaje(505);
                        break;
                    }
                } else {
                    ErrorMensaje(504);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Fin de archivo inesperado. " + e);
            errorEncontrado = true;
        }
    }

    private void Inicializar() {
        switch (cola.token) {
            case 100 -> { // Variable
                aux = cola;
                Variable_sin_declarar();
                Push_pilaInicial(cola.token);
                EntradaPila(cola.token, cola.lexema);
                cola = cola.sig;
                Asignacion();
            }
            case 215 -> { //LEER
                EntradaPila(cola.token, cola.lexema);
                cola = cola.sig;
                Leer();
                CodigoIntermedio();
                if (cola.token == 118) {//;
                    cola = cola.sig;
                    Inicializar();
                }
            }
            case 216 -> { // ESCRIBIR
                EntradaPila(cola.token, cola.lexema);
                cola = cola.sig;
                Escribir();
                CodigoIntermedio();
                if (cola.token == 118) {
                    cola = cola.sig;
                    Inicializar();
                }
            }
            case 205 -> {//SI
                aux = cola;
                Si();
                if (cola.token == 118) {
                    cola = cola.sig;
                    Inicializar();
                }
            }
            case 209 -> {//MIENTRAS
                aux  = cola;
                cola = cola.sig;
                Mientras();
                if (cola.token == 118) {
                    cola = cola.sig;
                    Inicializar();
                }
            }
            default -> {
            }
        }
    }
    
     // cambios 
    private void Escribir() {
        boolean cadena = false;
        if (cola.token == 116) { // ,
            EntradaPila(cola.token, cola.lexema);
            cola = cola.sig;
            if (cola.token == 118) {
                cola = cola.sig;
                Escribir();
            }
        }
        if (cola.token == 103) { // CADENA o string
            cadena = true;
            EntradaPila(cola.token, cola.lexema);
            cola = cola.sig;
            if (cola.token == 116) { // ,
                EntradaPila(cola.token, cola.lexema);
                cola = cola.sig;
                if (cola.token == 118) /* ;*/ {
                    cola = cola.sig;
                    Escribir();
                }
            }
            if (cola.token == 118) {
                cola = cola.sig;
                Escribir();
            }
        }
        if (cola.token == 100) { // Variable
            EntradaPila(cola.token, cola.lexema);
            Variable_sin_declarar();
            cola = cola.sig;
            if (cola.token == 116) {
                cola = cola.sig;
                Escribir();
            }
        } else if (!cadena) {
            ErrorMensaje(601);
        }
    }

    private void Leer() {
        if (cola.token == 100) {
            EntradaPila(cola.token, cola.lexema);
            Variable_sin_declarar();
            cola = cola.sig;
            if (cola.token == 116) {
                cola = cola.sig;
                Leer();
            }
        }
    }

    private void Asignacion() {
        if (cola.token == 119) {// :=
            Push_pilaInicial(cola.token);
            EntradaPila(cola.token, cola.lexema);
            cola = cola.sig;
            expresion_numerica();
            Infijo_Postfijo();
            CodigoIntermedio();
            if (cola.token == 118) {// ;
                cola = cola.sig;
                Inicializar();
            }
        } else {
            ErrorMensaje(600);
        }
    }

    private void Mientras() {
        Expresion_logica();
        Infijo_Postfijo();
        Insertar_ListaPolish("D" + (++ContadorWhile), 0);
        CodigoIntermedio();
        Insertar_ListaPolish("Brf C" + (ContadorWhile), 0);
        if (cola.token == 210) { // HACER
            cola = cola.sig;
            Inicializar();
            Insertar_ListaPolish("Bri D" + (ContadorWhile), 0);
            if (cola.token == 211) { // FIN_MIENTRAS
                Insertar_ListaPolish("C" + (ContadorWhile), 0);
                --ContadorWhile;
                cola = cola.sig;
            } else {
                ErrorMensaje(516);
            }
        }
    }

    private void Si() {
        cola   = cola.sig;
        Lexema = cola.lexema;
        Expresion_logica();
        Infijo_Postfijo();
        CodigoIntermedio();
        Insertar_ListaPolish("Brf A" + (++ContadorIf), 0);
        if (cola.token == 206) {// ENTONCES
            cola = cola.sig;
            Inicializar();
            if (cola.token != 208) { // SINO
                Insertar_ListaPolish("A" + (ContadorIf), 0);
            }
            Insertar_ListaPolish("Bri B" + (ContadorIf), 0);
            if (cola.token == 208) {// SINO
                cola = cola.sig;
                Insertar_ListaPolish("A" + (ContadorIf), 0);
                Inicializar();
            }
            if (cola.token == 207) {// FIN_SI
                Insertar_ListaPolish("B" + (ContadorIf), 0);
                --ContadorIf;
                cola = cola.sig;
            } else {
                ErrorMensaje(511);
            }
        } else {
            ErrorMensaje(513);
        }
    }

    private void Expresion_logica() {
        switch (cola.token) {
            case 114:// (
                cola = cola.sig;
                Expresion_logica();
                if (cola.token == 115) {// )
                    cola = cola.sig;
                    Exp_logica_1();
                }
                break;

            case 200:// NOT
                Push_pilaInicial(cola.token);
                EntradaPila(cola.token, cola.lexema);
                cola = cola.sig;
                Expresion_logica();
                Exp_logica_1();
                break;

            case 100:// Identificadores
                if ((cola.sig.token >= 108) && (cola.sig.token <= 113)) {// Operadores Operadores_Relacionales
                    expresion_relacional();
                } else {
                    Exp_logica_1();
                }
                break;

            case 203:// true
                Exp_logica_1();
                break;

            case 204:// false
                Exp_logica_1();
                break;

            default:
                expresion_relacional();
                Exp_logica_1();
                break;
        }
    }

    private void expresion_relacional() {
        expresion_numerica();
        if (cola.token >= 108 && cola.token <= 113) {// Operadores Operadores_Relacionales
            Push_pilaInicial(cola.token);
            EntradaPila(cola.token, cola.lexema);
            cola = cola.sig;
            expresion_numerica();
        } else {
            ErrorMensaje(512);
        }
    }

    private void Exp_logica_1() {
        expresion_numerica();
        if (cola.token >= 200 && cola.token <= 202) {// NOT, AND, OR
            Push_pilaInicial(cola.token);
            EntradaPila(cola.token, cola.lexema);
            cola = cola.sig;
            Expresion_logica();
            Exp_logica_1();
        }
    }

    private void EntradaPila(Integer Token, String Lexemas) {
        Tokens.push(Token);
        this.Lexemas.push(Lexemas);
    }

    private void CodigoIntermedio() {
        Token_Invertidos.push(115);
        Lexemas_invertidos.push(")");
        while (!Lexemas.empty() || !Tokens.empty()) { // Se llenan las pilas invertidas.
            Token_Invertidos.push(Tokens.pop());
            Lexemas_invertidos.push(Lexemas.pop());
        }
        Token_Invertidos.push(114);
        Lexemas_invertidos.push("(");
        while (!Token_Invertidos.empty()) {
            
            switch (Jerarquia_Operaciones(Token_Invertidos.peek())) { // 221
                case 0:
                    Insertar_ListaPolish(Lexemas_invertidos.pop(), Token_Invertidos.pop());
                    break;

                case 1: // :=
                    Tokens_operandos.push(Token_Invertidos.pop());
                    Lexemas_operandos.push(Lexemas_invertidos.pop());
                    break;

                case 2: // (
                    Tokens_operandos.push(Token_Invertidos.pop());
                    Lexemas_operandos.push(Lexemas_invertidos.pop());
                    break;

                case 3: // )
                    while (!Tokens_operandos.peek().equals(114)) {
                        Insertar_ListaPolish(Lexemas_operandos.pop(), Tokens_operandos.pop());
                    }
                    Tokens_operandos.pop();
                    Lexemas_operandos.pop();
                    Token_Invertidos.pop();
                    Lexemas_invertidos.pop();
                    break;

                case 4: // AND OR

                case 5:// NOT

                case 6: // operadores relacionales

                case 7: // + - 

                case 8: // * /
                    while (Jerarquia_Operaciones(Tokens_operandos.peek()) >= Jerarquia_Operaciones(Token_Invertidos.peek())) {
                        Insertar_ListaPolish(Lexemas_operandos.pop(), Tokens_operandos.pop());
                    }
                    Tokens_operandos.push(Token_Invertidos.pop());
                    Lexemas_operandos.push(Lexemas_invertidos.pop());
                    break;
            }
        }
    }

    private void expresion_numerica() {
        switch (cola.token) {
            case 114 -> {
                Push_pilaInicial(cola.token);
                EntradaPila(cola.token, cola.lexema);
                expresion_numerica();
                if (cola.token == 115) {
                    Push_pilaInicial(cola.token);
                    EntradaPila(cola.token, cola.lexema);
                    expresion_numerica2();
                }
            }
            case 105 -> {// -
                Push_pilaInicial(cola.token);
                EntradaPila(cola.token, cola.lexema);
                expresion_numerica();
                expresion_numerica2();
            }
            case 100 -> {
                Variable_sin_declarar();
                Push_pilaInicial(cola.token);
                EntradaPila(cola.token, cola.lexema);
                cola = cola.sig;
                expresion_numerica2();
            }
            case 203, 204 -> {// TRUE
                Push_pilaInicial(221);
                Tokens.push(221);
                Lexemas.push(cola.lexema);
                cola = cola.sig;
                Exp_logica_1();
            }
            case 103, 102, 101 -> { // Cadena o string
                Push_pilaInicial(cola.token);
                EntradaPila(cola.token, cola.lexema);
                cola = cola.sig;
                expresion_numerica2();
            }
            // DECIMAL o Double
            // ENTERO o Int
            default -> {
            }
        }
    }

    private void expresion_numerica2() {
        if (cola.token >= 104 && cola.token <= 107) {// + - * /
            Push_pilaInicial(cola.token);
            EntradaPila(cola.token, cola.lexema);
            cola = cola.sig;
            expresion_numerica();
            expresion_numerica2();
        }
    }

    private void Dec_variable() {
        if (cola.token == 100) { // variable
            if (cola.lexema.equals(nombrePrograma)) {
                Error_semantico(517);
            } else {
                Variable_multideclarada();
                lexemaAux = cola.lexema;
            }
            cola = cola.sig;
            if (cola.token == 117) {// :
                cola = cola.sig;
                Nombre_tipo_simple();
                Insertar_Variables(lexemaAux, tipo);

                if (cola.token == 118) {// ;
                    cola = cola.sig;
                    Dec_variable();
                }
            } else {
                ErrorMensaje(515);
            }
        } else {
            ErrorMensaje(515);
        }
    }

    private void Nombre_tipo_simple() {
        switch (cola.token) {
            case 218://Entero
                cola.token = 101;
                tipo = cola.token;
                cola = cola.sig;
                break;

            case 219://Decimal
                cola.token = 102;
                tipo = cola.token;
                cola = cola.sig;
                break;

            case 220://Cadena
                cola.token = 103;
                tipo = cola.token;
                cola = cola.sig;
                break;

            case 221://Boolean
                tipo = cola.token;
                cola = cola.sig;
                break;

            default:
                ErrorMensaje(514);
                break;
        }
    }

    private void Infijo_Postfijo() {
        Invertida.push(115);//)
        while (!Inicial.empty())
            Push_pilaInvertida(Inicial.pop());

        Push_pilaInvertida(114);//(
        while (!Invertida.empty()) {
            switch (Jerarquia_Operaciones(Invertida.peek())) {
                case 0, 9 -> Push_pilaSalidas(Invertida.pop());
                case 1, 2 -> Push_pilaOperadores(Invertida.pop());
                case 3 -> { // )
                    while (!Operadores.peek().equals(114)) {
                        Push_pilaSalidas(Operadores.pop()); // postfijo
                    }
                    Operadores.pop();
                    Invertida.pop();
                } // AND OR

                // NOT

                // Operadores_Relacionales

                // + -

                case 4, 5, 6, 7, 8 -> { // * /
                    while (Jerarquia_Operaciones(Operadores.peek()) >= Jerarquia_Operaciones(Invertida.peek()))
                        Push_pilaSalidas(Operadores.pop());

                    Push_pilaOperadores(Invertida.pop());
                }
            }
        }
        IncompatibilidadTipos(Salida_lista);
        Salida_lista.removeAllElements();
        aux_salida.removeAllElements();
    }

    private void IncompatibilidadTipos(Stack lista) {
        boolean error = false;
        int columna_fila = 0, fila_lista = 0, operando1 = 0, operando2 = 0;
        for (int i = 0; i < Salida_lista.size(); i++) {
            int operador = (int) lista.get(i);
            if ((operador >= 104 && operador <= 113) || (operador == 119) || (operador >= 200 && operador <= 202)) {
                operando1 = aux_salida.pop();
                operando2 = aux_salida.pop();
                switch (operando1) {
                    case 101:
                        columna_fila = 0;
                        break;

                    case 102:
                        columna_fila = 1;
                        break;

                    case 103:
                        columna_fila = 2;
                        break;

                    case 221:
                        columna_fila = 3;
                        break;
                }
                switch (operando2) {
                    case 101:
                        fila_lista = 0;
                        break;

                    case 102:
                        fila_lista = 1;
                        break;

                    case 103:
                        fila_lista = 2;
                        break;

                    case 221:
                        fila_lista = 3;
                        break;

                }
                switch (operador) {
                    case 104:
                        if (Tipos.Sumas[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            aux_salida.push(Tipos.Sumas[fila_lista][columna_fila]);
                        }
                        break;
                    case 105:
                        if (Tipos.Resta_Multiplicacion[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            aux_salida.push(Tipos.Resta_Multiplicacion[fila_lista][columna_fila]);
                        }
                        break;
                    case 106:
                        if (Tipos.Resta_Multiplicacion[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            aux_salida.push(Tipos.Resta_Multiplicacion[fila_lista][columna_fila]);
                        }
                        break;
                    case 107:
                        if (Tipos.Divisiones[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            aux_salida.push(Tipos.Divisiones[fila_lista][columna_fila]);
                        }
                        break;
                    case 119:
                        if (!Tipos.Asignaciones[fila_lista][columna_fila]) {
                            error = true;
                        }
                        break;

                    case 110:
                    case 108:
                    case 109:
                    case 111:
                        if (Tipos.Operadores_Relacionales[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            aux_salida.push(Tipos.Operadores_Relacionales[fila_lista][columna_fila]);
                        }
                        break;

                    case 112:
                    case 113:
                        if (Tipos.Igual_Diferente[fila_lista][columna_fila] == 0) {
                            error = true;
                        } else {
                            aux_salida.push(Tipos.Igual_Diferente[fila_lista][columna_fila]);
                        }
                        break;

                    case 200:// NOT
                    case 201:// AND 
                    case 202:// OR
                        if (!Tipos.Operadores_Logicos[fila_lista][columna_fila]) {
                            error = true;
                        } else {
                            aux_salida.push(221);
                        }
                        break;
                }
            } else {
                aux_salida.push(operador);
            }
            if (error) { // p.linea
                Error_semantico(520);
                break;
            }
        }
    }

    private void Variable_multideclarada() {
        nodo = cab_var;
        while (nodo != null) {
            if (cola.lexema.equals(nodo.lexema)) {
                Error_semantico(518);
            }
            nodo = nodo.sig;
        }
        nodo = cab_var;
    }

    private void Variable_sin_declarar() {
        nodo = cab_var;
        boolean Validar_variable = false;
        while (nodo != null) {
            if (cola.lexema.equals(nodo.lexema)) {
                Validar_variable = true;
                cola.token       = nodo.token;
                break;
            }
            nodo = nodo.sig;
        }
        if (!Validar_variable) {
            Error_semantico(519);
        }
    }

    private void BuscarCadenaPolish() {
        String Cadena = "";
        NodoVar aux_polish = cab_polish;
        NodoVar aux_buscar = aux_polish;
        while (aux_buscar != null) {
            if (aux_buscar.token == 103) {
                Cadena = aux_buscar.lexema;
                aux_buscar = aux_buscar.sig;
                if (aux_buscar.token == 216) {
                    VariablesCadenas.add(Cadena);
                }
            }
            aux_buscar = aux_buscar.sig;
        }
    }

    private String BuscarLexemaPolish(String Lexema) {
        String lexema = "";
        NodoVar Aux_polish = cab_polish;
        NodoVar AuxBuscar = cab_polish;
        while (AuxBuscar != null) {
            if (AuxBuscar.lexema.equals(Lexema)) {
                Aux_polish = AuxBuscar.sig;
                if (Aux_polish.lexema.startsWith("'")) {
                    lexema = Aux_polish.lexema;
                    break;
                }
            }
            AuxBuscar = AuxBuscar.sig;
        }
        return lexema;
    }

    private void Pie_PaginaAsm() {
        textoEnsamblador += "ret\n"
                + "METODOS  ENDP\n"
                + "END";
    }

    private void CabezeraAsm() {
        textoEnsamblador = "INCLUDE macros.MAC\n"
                + ".MODEL SMALL\n"
                + ".STACK 100h\n"
                + ".DATA\n";
    }

    private void GenerarCodigoObjeto() {
        CabezeraAsm();
        p_var = cab_var;
        BuscarCadenaPolish();
        while (p_var != null) {
            if (p_var.token == 103) { // Cadena o string
                textoEnsamblador += p_var.lexema + " db " + BuscarLexemaPolish(p_var.lexema) + ", 13,10,'$'\n";
                VariablesStrings.push(p_var.lexema);
            } else {
                textoEnsamblador += p_var.lexema + " db" + " ?\n";
            }
            p_var = p_var.sig;
        }
        int contadorCadenas = 1;
        for (int i = 0; i < VariablesCadenas.size(); i++) {
            textoEnsamblador += "\n" + "Texto" + contadorCadenas++ + " db " + VariablesCadenas.get(i) + ", 13,10,'$' ";
        }

        textoEnsamblador += "impresion db ?\n"
                + ";/Var\n"
                + ".CODE\n"
                + "MOV     AX, @DATA\n"
                + "MOV     DS, AX\n"
                + "CALL    METODOS\n"
                + "MOV AX,4C00H\n"
                + "INT 21H\n"
                + "METODOS PROC\n";

        aux_polish = cab_polish;
        while (aux_polish != null) {
            if (aux_polish.token >= 104 && aux_polish.token < 114 || aux_polish.token == 119
                    || aux_polish.token >= 200 && aux_polish.token <= 202 || aux_polish.token == 215
                    || aux_polish.token == 216 || aux_polish.token == 0) {
                operador = aux_polish.lexema;

                switch (operador) {
                    // 119 >=, 114 (, 104 +, 202 AND, 215 Leer, 216 Escribir, 104 +, 105 -, 106 *, 107 /, 108 >
                    case "+" -> OperandosAsm("Sumar");
                    case "-" -> OperandosAsm("Restar");
                    case "*" -> OperandosAsm("Multi");
                    case "/" -> OperandosAsm("Dividir");
                    case ":=" -> OpAsignacionAsm("Asignar");
                    case "LEER" -> OpLeerAsm("Leer");
                    case "ESCRIBIR" -> OpEscribirAsm("LeerN", "Escribir");
                    case "=" -> OperandosAsm("Igual");
                    case ">" -> OperandosAsm("Mayor_que");
                    case "<" -> OperandosAsm("Menor_que");
                    case ">=" -> OperandosAsm("MayorIgual");
                    case "<=" -> OperandosAsm("MenorIgual");
                    case "<>" -> OperandosAsm("Diferentes");
                }
                Saltos_Etiquetas("JF", "JMP");
            } else {
                buscarPolish.push(aux_polish.lexema);
            }
            aux_polish = aux_polish.sig;
        }
        Pie_PaginaAsm();

        System.out.println("\n<<< CODIGO INTERMEDIO >>>");
        System.out.println(textoEnsamblador);

        GenerarArchivoAsm();
    }

    private void GenerarArchivoAsm() {
        final String direccionArchivo = direccionSalida + "\\" + nombrePrograma.toLowerCase() + ".asm"; // direccion absoluta

        // crear archivo
        try {
            File archivo = new File(direccionArchivo);  // crear archivo
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));

            bw.write(textoEnsamblador);                 // escribir codigo en el archivo
            bw.close();

            System.out.println("\nCodigo intermedio generado en: " + direccionArchivo + "\n");
        } catch (Exception e) {                                                             // error
            System.out.println("Ha ocurrido un error al generar el archivo: " + e.getMessage());
        }

        // compilar y ejecutar en dosbox
        try {
            String[] command = {                                    // comando a ejecutar
                    "dosbox",
                    "-c", "mount c " + direccionSalida,
                    "-c", "c:",
                    "-c", "masm " + nombrePrograma + ".asm;",
                    "-c", "link " + nombrePrograma + ".obj;",
                    "-c", nombrePrograma + ".exe"
            };

            ProcessBuilder builder = new ProcessBuilder(command);   // crear proceso
            builder.start();                                        // ejecutar comando
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error al intentar compilar el archivo: " + e.getMessage());
        }
    }

    private void OperandosAsm(String OperadoresMacro) {
        String operando2 = buscarPolish.pop();
        String operando1 = buscarPolish.pop();

        textoEnsamblador += "" + OperadoresMacro + " " + operando1 + "," + operando2 + ",resultado" + contador + "\n";

        resultadoASM = ("resultado" + contador);
        buscarPolish.push(resultadoASM);
        textoEnsamblador = Cambiar(textoEnsamblador, ";/Var", " resultado" + contador + " db " + " ? " + "\n;/Var");
    }

    private void OpAsignacionAsm(String Macro) {
        String operador2 = buscarPolish.pop();
        String operador1 = buscarPolish.pop();
        boolean bandera = false;
        for (int i = 0; i < VariablesStrings.size(); i++) {
            if (VariablesStrings.get(i).equals(operador1)) {
                textoEnsamblador = Cambiar(textoEnsamblador, "" + operador1 + ";/auxiliar ", "\n" + operador1 + " db " + operador2);
                bandera          = true;
                break;
            }
        }
        if (!bandera) {
            textoEnsamblador += "" + Macro + " " + operador1 + "," + operador2 + "\n";
        }
    }

    private void OpLeerAsm(String LeerMacro) {
        String operador1 = buscarPolish.pop();

        boolean bandera = false;
        for (int i = 0; i < VariablesStrings.size(); i++) {
            if (VariablesStrings.get(i).equals(operador1)) {
                bandera = true;
                break;
            }
        }
        if (bandera) {
            System.out.println(operador1);
            String Cadena = " " + operador1 + "";
            boolean ContieneCadena = textoEnsamblador.indexOf(Cadena) >= 0;
            if (ContieneCadena) {
                textoEnsamblador = Cambiar(textoEnsamblador, Cadena, "\n" + Cadena);
                textoEnsamblador = Cambiar(textoEnsamblador, ";/auxiliar", " db 0");
            }
        }
        textoEnsamblador += "" + LeerMacro + " " + operador1 + "\n";
    }

    private void OpEscribirAsm(String EscribirLinea, String Escribir) {
        String operador1 = buscarPolish.pop();

        boolean bandera = false;

        for (String variablesString : VariablesStrings) {
            if (variablesString.equals(operador1)) {
                bandera = true;
                break;
            }
        }

        if (bandera && !operador1.startsWith("'"))
            textoEnsamblador += "" + EscribirLinea + " " + operador1 + "\n";
        else if (!bandera && !operador1.startsWith("'"))
            textoEnsamblador += "" + Escribir + " " + operador1 + "\n";
        else
            textoEnsamblador += "" + EscribirLinea + " Texto" + contadorcadenas++ + "\n";
    }

    private void Saltos_Etiquetas(String JF, String JMP) {
        if (operador.startsWith("Brf")) {
            String p = operador.substring(4, 6);
            int auxiliar_Contador = contador;
            textoEnsamblador += JF + " Resultado" + (auxiliar_Contador - 1) + "," + p + "\n";
        } else if (operador.startsWith("Bri")) {
            String p = operador.substring(4, 6);
            textoEnsamblador += JMP + " " + p + "\n";
        } else if (operador.startsWith("A") || operador.startsWith("B")
                || operador.startsWith("C") || operador.startsWith("D")) {
            String p = operador.substring(0, 2);
            textoEnsamblador += " " + p + ":\n";
        }
        contador++;
    }

    private String Cambiar(String cadena, String buscar, String reemplazar) {
        return cadena.replaceAll(buscar, reemplazar);
    }
}
