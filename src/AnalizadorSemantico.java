import java.util.Stack;

public class AnalizadorSemantico {
    Stack<Integer> Inicial = new Stack<>(), Invertida = new Stack<>(), Operadores = new Stack<>();
    Stack<Integer> Salida_lista = new Stack<>(), aux_salida = new Stack<>();

    // Listas de Tokens
    Stack<Integer> Token_Invertidos = new Stack<>(), Tokens = new Stack<>(), Tokens_operandos = new Stack<>();

    // Listas de Lexemas
    Stack<String> Lexemas_invertidos = new Stack<>(), Lexemas = new Stack<>(), Lexemas_operandos = new Stack<>();

    NodoVar p_var, cab_var, cab_polish, aux_polish = null;

    void Insertar_Variables(String lexema, int token) {
        NodoVar nodo_variable = new NodoVar(lexema, token);
        if (cab_var == null) {
            cab_var = nodo_variable;
            p_var   = cab_var;
        } else {
            p_var.sig = nodo_variable;
            p_var     = p_var.sig;
        }
    }

    void Imprimir_Listavariables() {
        System.out.println("\n<<< VARIABLES DECLARADAS >>>");
        System.out.printf("%-15s %-5s\n", "Nombre", "Tipo");

        p_var = cab_var;

        while (p_var != null) {
            System.out.printf("%-15s %-5d\n", (p_var.lexema == null) ? "Sin datos" : p_var.lexema, p_var.token);
            p_var = p_var.sig;
        }
    }

    int Jerarquia_Operaciones(int operador) {
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

    void Push_pilaInicial(int token) {
        Inicial.push(token);
    }

    void Push_pilaSalidas(int token) {
        Salida_lista.push(token);
    }

    void Push_pilaInvertida(int token) {
        Invertida.push(token);
    }

    void Push_pilaOperadores(int token) {
        Operadores.push(token);
    }

    void Insertar_ListaPolish(String lexema, int token) {
        NodoVar NodoPolish = new NodoVar(lexema, token);
        if (cab_polish == null) {
            cab_polish = NodoPolish;
            aux_polish = cab_polish;
        } else {
            aux_polish.sig = NodoPolish;
            aux_polish     = NodoPolish;
        }
    }

    void Imprimir_ListaPolish() {
        System.out.println("\n<<< LISTA POLISH >>>");
        System.out.printf("%-15s %-3s\n", "Lexema", "Token");

        aux_polish = cab_polish;
        int i = 0;

        while (aux_polish != null) {
            System.out.printf("%-15s %-3s\n", aux_polish.lexema, (aux_polish.token != 0) ? aux_polish.token : "");

            aux_polish = aux_polish.sig;
        }
    }
}
