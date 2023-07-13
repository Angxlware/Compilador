public class SistemaTipos {
    
     public static boolean[][] Asignaciones ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {true,   false,   false,    false},
            /*Decimal*/ {true,   true,    false,    false},
            /*String*/  {false,  false,   true,     false},
            /*Boolean*/  {false,  false,   false,    true }
    };

    public static int[][] Divisiones ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {102,   102,        0,       0},
            /*Decimal*/ {102,   102,        0,       0},
            /*String*/  {0,       0,        0,       0},
            /*Boolean*/  {0,       0,        0,       0}
    };

    public static int[][] Resta_Multiplicacion = {
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {101,   102,        0,       0},
            /*Decimal*/ {102,   102,        0,       0},
            /*String*/  {0,       0,        0,       0},
            /*Boolean*/  {0,       0,        0,       0}
    };

    public static int[][] Sumas ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {101,     102,     0,         0},
            /*Decimal*/ {102,     102,     0,         0},
            /*String*/  {0,       0,      103,        0},
            /*Boolean*/  {0,       0,       0,         0}
    };



    public static int[][] Operadores_Relacionales={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {221,   221,        0,       0},
            /*Decimal*/ {221,   221,        0,       0},
            /*String*/  {0,       0,        0,       0},
            /*Boolean*/  {0,       0,        0,       0}
    };

    public static int[][] Igual_Diferente ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {221,   221,        0,       0},
            /*Decimal*/ {221,   221,        0,       0},
            /*String*/  {0,       0,         0,      0},
            /*Boolean*/  {0,       0,         0,      221}
    };

    public static boolean[][] Operadores_Logicos={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {false,  false,    false,    false},
            /*Decimal*/ {false,  false,    false,    false},
            /*String*/  {false,  false,    false,    false},
            /*Boolean*/  {false,  false,    false,     true}
    };
}
