//101 son enteros
//102 son doubles
//103 son strings
//221 son true

public class SistemaTipos {
    
     boolean[][] Asignaciones ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {true,   false,   false,    false},
            /*Decimal*/ {true,   true,    false,    false},
            /*String*/  {false,  false,   true,     false},
            /*Boolean*/  {false,  false,   false,    true }
    };
     
     int[][] Divisiones ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {102,   102,        0,       0},
            /*Decimal*/ {102,   102,        0,       0},
            /*String*/  {0,       0,        0,       0},
            /*Boolean*/  {0,       0,        0,       0}
    };
     
     int[][] Resta_Multiplicacion = {
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {101,   102,        0,       0},
            /*Decimal*/ {102,   102,        0,       0},
            /*String*/  {0,       0,        0,       0},
            /*Boolean*/  {0,       0,        0,       0}
    };

    int[][] Sumas ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {101,     102,     0,         0},
            /*Decimal*/ {102,     102,     0,         0},
            /*String*/  {0,       0,      103,        0},
            /*Boolean*/  {0,       0,       0,         0}
    };

     

     int[][] Operadores_Relacionales={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {221,   221,        0,       0},
            /*Decimal*/ {221,   221,        0,       0},
            /*String*/  {0,       0,        0,       0},
            /*Boolean*/  {0,       0,        0,       0}
    };

     int[][] Igual_Diferente ={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {221,   221,        0,       0},
            /*Decimal*/ {221,   221,        0,       0},
            /*String*/  {0,       0,         0,      0},
            /*Boolean*/  {0,       0,         0,      221}
    };

     boolean[][] Operadores_Logicos={
            //          Entero   Decimal  String    Boolean
            /*Entero*/  {false,  false,    false,    false},
            /*Decimal*/ {false,  false,    false,    false},
            /*String*/  {false,  false,    false,    false},
            /*Boolean*/  {false,  false,    false,     true}
    };
}
