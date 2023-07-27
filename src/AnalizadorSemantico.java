import java.util.Stack;

public class AnalizadorSemantico {
    Stack<Integer> pilaInicial = new Stack<>(), pilaInvertida = new Stack<>(), pilaOperadores = new Stack<>();
    Stack<Integer> pilaSalida = new Stack<>(), pilaSalidaAux = new Stack<>();

    // Listas de Tokens
    Stack<Integer> pilaTokensInvertidos = new Stack<>(), pilaTokens2 = new Stack<>(), pilaTokensOperandos = new Stack<>();

    // Listas de Lexemas
    Stack<String> pilaLexemasInvertidos = new Stack<>(), pilaLexemas = new Stack<>(), pilaLexemasOperandos = new Stack<>();

    NodoVar p_var, cab_var, cabeceraPolish, colaPolish = null;

    void insertarVariable(String lexema, int token) {
        NodoVar nodo_variable = new NodoVar(lexema, token);
        if (cab_var == null) {
            cab_var = nodo_variable;
            p_var   = cab_var;
        } else {
            p_var.sig = nodo_variable;
            p_var     = p_var.sig;
        }
    }

    void imprimirListaVariables() {
        System.out.println("\u001B[36m" + "\n[ LISTA DE VARIABLES ]" + "\u001B[39m");
        System.out.printf("%-15s %-5s\n", "Nombre", "Tipo");

        p_var = cab_var;

        while (p_var != null) {
            System.out.printf("%-15s %-5d\n", (p_var.lexema == null) ? "Sin datos" : p_var.lexema, p_var.token);
            p_var = p_var.sig;
        }
    }

    int obtenerJerarquiaOperacion(int operador) {
        int importancia = 0;
        //*                   /
        if ((operador == 106) || (operador == 107)) {
            importancia = 8;//* /
        }
        //+                  -
        if ((operador == 104) || (operador == 105)) {
            importancia = 7;//+ -
        }
        if ((operador >= 108 && operador <= 113)) {
            importancia = 6;// operadores relacionales
        }
        //NOT
        if ((operador == 200)) {
            importancia = 5;// NOT
        }
        //AND                  //OR
        if ((operador == 201) || (operador == 202)) {
            importancia = 4;// AND OR
        }
        //)
        if ((operador == 115)) {
            importancia = 3;// )
        }
        //(
        if ((operador == 114)) {
            importancia = 2;// (
        }
        //                :=                    Leer                Escribir
        if ((operador == 119) || (operador == 215) || (operador == 216)) {
            importancia = 1;// := 
        }
        return importancia;
    }

    void pushPilaInicial(int token) {
        pilaInicial.push(token);
    }

    void pushPilaSalidas(int token) {
        pilaSalida.push(token);
    }

    void insertarPilaInvertida(int token) {
        pilaInvertida.push(token);
    }

    void pushPilaOperadores(int token) {
        pilaOperadores.push(token);
    }

    public void insertarListaPolish(String lexema, int token) {
        NodoVar NodoPolish = new NodoVar(lexema, token);
        if (cabeceraPolish == null) {
            cabeceraPolish = NodoPolish;
            colaPolish = cabeceraPolish;
        } else {
            colaPolish.sig = NodoPolish;
            colaPolish = NodoPolish;
        }
    }

    public void imprimirListaPolish() {
        System.out.println("\u001B[36m" + "\n[ LISTA POLACA ]" + "\u001B[39m");
        System.out.printf("%-15s %-3s\n", "Lexema", "Token");

        colaPolish = cabeceraPolish;

        while (colaPolish != null) {
            System.out.printf("%-15s %-3s\n", colaPolish.lexema, (colaPolish.token != 0) ? colaPolish.token : "");
            colaPolish = colaPolish.sig;
        }
    }
}
