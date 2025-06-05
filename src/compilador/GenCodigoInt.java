/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:        SEMESTRE: ______________            HORA: ______________ HRS
 *:                                   
 *:               
 *:    # Clase con la funcionalidad del Generador de Código Intermedio
 *                 
 *:                           
 *: Archivo       : GenCodigoInt.java
 *: Autor         : Fernando Gil  
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   :  
 *:                  
 *:           	     
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 23/05/2025  DanielJM, Karime    Se agregarón las variables y metodos necesarios
 *:             Alfredo, DanielM    para el GenCodInt.
 *:                                 Se copiaron los procedures 22
 *:                                 sin acciones semánticas con sus respectivos
 *:                                 metodos y variables para su función.
 *:                                 Se añadió la clase GFG para utilizar el 
 *:                                 metodo de conversion de infija a prefija.
 *:                                 Se agregarón las 11 acciones semanticas 
 *:                                 para el C3D desarrolladas.
 *: 05/06/2025 DanielJM, Karime,    Se modificó procesarPrefix() y proposicion()
               Alfredo, DanielM     para representar por medio de cuadruplos el
 *:                                 Código intermedio en la tabla de cuadruplos
 *:-----------------------------------------------------------------------------
 */


package compilador;
import general.Linea_BE;
import java.util.*;

public class GenCodigoInt {
 
    private Compilador cmp;
    private int        consecutivoTmp;
    private String     preAnalisis;
    
    private StringBuilder infixExp;
    
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //
	public GenCodigoInt ( Compilador c ) {
        cmp = c;
    }
    // Fin del Constructor
    //--------------------------------------------------------------------------
	
    private String tempnuevo () {
        return "t" + consecutivoTmp++;
    }    
        
    //--------------------------------------------------------------------------
    
    private void emite ( String c3d ) {
        cmp.iuListener.mostrarCodInt ( c3d + "\n" );
    }
    
    private boolean isNumber(String s) {
        return !s.equals("*") && !s.equals("+");
    }
    
    private String procesarPrefix(String prefix){//Codigo creado por nosotros
        Stack<String> st = new Stack<>();
        String arr[] = prefix.split(" ");
        for( int i = 1 ; i<arr.length; i++ ){
        //for (String c : prefix.split(" ")) {
            String str = arr[i];
            if (st.empty() || !isNumber(str)) {
                st.push(str);
            } else {
                String segundo = str;
                while (!st.isEmpty() && isNumber(st.peek())) {
                    String primero = st.pop();
                    if( st.empty() )
                       return "";
                    String operador = st.pop();
                    String derecho = primero + " " + operador + " " + segundo;
                    
                    String t = tempnuevo();
                    emite(t + " := " + derecho);
                    //-----------------------------------
                    cmp.cua.agregar(new Cuadruplo(operador,primero,segundo,t));
                    //----------------------------------=
                    segundo = t;
                }
                st.push(segundo);
            }
        }
        return (st.peek());
    }
    
    public void generar () {
        preAnalisis = cmp.be.preAnalisis.complex;
        consecutivoTmp  = 1;
        
        programa();
    }    
    
    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;           
        } else {
            errorEmparejar( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }
    
    private void errorEmparejar(String _token, String _lexema, int numLinea ) {
        String msjError = "[emparejar] ";

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasig")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + ( _lexema.equals ( "$" )? "fin de archivo" : _lexema ) + 
                    ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }
    
    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }
    
    private void programa ()
    {
        if ( preAnalisis.equals( "dim" ) ||  preAnalisis.equals( "function" ) || preAnalisis.equals( "sub" )||
             preAnalisis.equals( "id" ) || preAnalisis.equals( "call" ) || preAnalisis.equals( "if" )|| 
             preAnalisis.equals( "do" ) || preAnalisis.equals( "end" ) )
        { 	//programa -> declaraciones declaraciones_subprogramas proposiciones_optativas end
            declaraciones();
            declaraciones_subprogramas();
            proposiciones_optativas();
            emparejar( "end" );
        }
        else
        {
            error( "[programa]: El programa debe iniciar con una declaracion, un subprograma o una proposicion, o solo terminar con la palabra end" );
        }
    }

    private void declaraciones ()
    {
        if ( preAnalisis.equals( "dim" ) )
        { 	//declaraciones -> dim lista_declaraciones declaraciones
            emparejar( "dim" );
            lista_declaraciones();
            declaraciones();
        }
        else
        {
            //declaraciones -> empty
        }
    }

    private void lista_declaraciones ()
    {
        if ( preAnalisis.equals( "id" ) )
        { 	//lista_declaraciones -> id as tipo lista_declaraciones'
            emparejar( "id" );
            emparejar( "as" );
            tipo();
            lista_declaraciones_();
        }
        else
        {
            error( "[lista_declaraciones]: Se esperaba un identificador" );
        }
    }

    private void tipo ()//Cambiamos la apostrofe por un guion bajo
    {
        if ( preAnalisis.equals( "integer" ) )
        { 	//tipo -> integer
            emparejar( "integer" );
        }
        else if ( preAnalisis.equals( "single" ) )
        {
            emparejar( "single" );
        }
        else if ( preAnalisis.equals( "string" ) )
        {
            emparejar( "string" );
        }
        else
        {
            error( "[tipo]: Se esperaba un tipo de dato" );
        }
    }

    private void lista_declaraciones_ ()
    {
        if ( preAnalisis.equals( "," ) )
        { 	//lista_declaraciones' -> , lista_declaraciones
            emparejar( "," );
            lista_declaraciones();
        }
        else
        {
            //lista_declaraciones' -> empty
        }
    }

    private void declaraciones_subprogramas ()
    {
        if ( preAnalisis.equals( "function" ) || preAnalisis.equals( "sub" ) )
        { 	//declaraciones_subprogramas -> declaracion_subprograma declaraciones_subprogramas
            declaracion_subprograma();
            declaraciones_subprogramas();
        }
        else
        {
            //declaraciones_subprogramas -> empty
        }
    }

    private void declaracion_subprograma ()
    {
        if ( preAnalisis.equals( "function" ) )
        { 	//declaracion_subprograma -> declaracion_funcion
            declaracion_funcion();
        }
        else if ( preAnalisis.equals( "sub" ) )
        { 	//declaracion_subprograma -> declaracion_subrutina
            declaracion_subrutina();
        }
        else
        {
            error( "[declaracion_subprograma]: Se esperaba una funcion o una subrutina" );
        }
    }

    private void declaracion_funcion ()
    {
        if ( preAnalisis.equals( "function" ) )
        { 	//declaracion_funcion -> function id argumentos as tipo proposiciones_optativas end function
            emparejar( "function" );
            emparejar( "id" );
            argumentos();
            emparejar( "as" );
            tipo();
            proposiciones_optativas();
            emparejar( "end" );
            emparejar( "function" );
        }
        else
        {
            error( "[declaracion_funcion]: Se esperaba la palabra reservada \"función\" para una funcion" );
        }
    }

    private void declaracion_subrutina ()
    {
        if ( preAnalisis.equals( "sub" ) )
        { 	//declaracion_subrutina -> sub id argumentos proposiciones_optativas end sub
            emparejar( "sub" );
            emparejar( "id" );
            argumentos();
            proposiciones_optativas();
            emparejar( "end" );
            emparejar( "sub" );
        }
        else
        {
            error( "[declaracion_subrutina]: Se esperaba la palabra reservada \"sub\" para una subrutina" );
        }
    }

    private void argumentos ()
    {
        if ( preAnalisis.equals( "(" ) )
        { 	//argumentos -> ( lista_declaraciones )
            emparejar( "(" );
            lista_declaraciones();
            emparejar( ")" );
        }
        else
        {
            //argumentos -> empty
        }
    }

    private void proposiciones_optativas ()
    {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "call" ) || preAnalisis.equals( "if" ) ||
             preAnalisis.equals( "do" ) )
        { 	//proposiciones_optativas -> proposicion proposiciones_optativas
            proposicion();
            proposiciones_optativas();
        }
        else
        {
            //proposiciones_optativas -> empty
        }
    }

    private void proposicion ()
    {
        Linea_BE id;
        if ( preAnalisis.equals( "id" ) )
        { 	//proposicion -> id opasig expresion
            id = cmp.be.preAnalisis;
            emparejar( "id" );
            emparejar( "opasig" );
            
            //Inicio de Accion semantica A
            infixExp = new StringBuilder();
            //Fin de Accion semantica A
            
            expresion();
            
            //Inicio de Accion Semantica K
            consecutivoTmp = 1;
            String prefix= GFG.infixToPrefix(infixExp.toString().toCharArray());
            String temporal = procesarPrefix(prefix);
            emite(id.lexema + " :=  " + temporal);
            //---------------------------------------------------------
            cmp.cua.agregar(new Cuadruplo(":=",temporal,"",id.lexema));
            //---------------------------------------------------------
            //Fin de Accion Semantica K
        }
        else if ( preAnalisis.equals( "call" ) )
        { 	//proposicion -> call id proposicion'
            emparejar( "call" );
            emparejar( "id" );
            proposicion_();
        }
        else if ( preAnalisis.equals( "if" ) )
        { 	//proposicion -> if condición then proposiciones_optativas else proposiciones_optativas end if
            emparejar( "if" );
            condicion();
            emparejar( "then" );
            proposiciones_optativas();
            emparejar( "else" );
            proposiciones_optativas();
            emparejar( "end" );
            emparejar( "if" );
        }
        else if ( preAnalisis.equals( "do" ) )
        { 	//proposicion -> do while condicion proposiciones_optativas loop
            emparejar( "do" );
            emparejar( "while" );
            condicion();
            proposiciones_optativas();
            emparejar( "loop" );
        }
        else
        {
            error( "[proposicion]: Se esperaba una proposición como un id, un call, un if o un do while" );
        }
    }

    private void proposicion_ ()//Cambiamos apostrofe por guien bajo
    {
        if ( preAnalisis.equals( "(" ) )
        { 	//proposicion' -> ( lista_expresiones )
            emparejar( "(" );
            lista_expresiones();
            emparejar( ")" );
        }
        else
        {
            //proposicion' -> empty
        }
    }

    private void lista_expresiones ()
    {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "literal" ) || preAnalisis.equals( "(" ))
        { 	//lista_expresiones -> expresion lista_expresiones' 
            expresion();
            lista_expresiones_();
        }
        else
        {
            //lista_expresiones -> empty
        }
    }

    private void lista_expresiones_ ()//Cambie apostrofe por guin bajo
    {
        if ( preAnalisis.equals( "," ) )
        { 	//lista_expresiones' -> , expresion lista_expresiones'
            emparejar( "," );
            expresion();
            lista_expresiones_();
        }
        else
        {
            //lista_expresiones' -> empty
        }
    }

    private void condicion ()
    {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "literal" ) || preAnalisis.equals( "(" ) )
        { 	//condicion -> expresion oprel expresion
            expresion();
            emparejar( "oprel" );
            expresion();
        }
        else
        {	
            error( "[condicion]: Se esperaba un expresion, un numero, un id o una literal" );
        }
    }

    private void expresion ()
    {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "(" ) )
        { 	//expresion -> termino expresion'
            termino();
            expresion_();
        }
        else if ( preAnalisis.equals( "literal" ) )
        { 	//expresion -> literal
            emparejar( "literal" );
        }
        else
        {	
            error( "[expresion]: Se esperaba un \"(\", un numero, un id o una literal" );
        }
    }

    private void expresion_ ()//Cambiamos apostrofe por guion bajo
    {
        Linea_BE opSuma;
        if ( preAnalisis.equals( "opsuma" ) )
        { 	//expresion' -> opsuma termino expresion'
            opSuma = cmp.be.preAnalisis;
            emparejar( "opsuma" );
            
            //Inicio de Accion semantica B
            infixExp.append(opSuma.lexema).append(" ");
            //Fin de Accion Semantica B
            
            termino();
            expresion_();
        }
        else
        {	
            //expresion' -> empty
        }
    }

    private void termino ()
    {
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "(" ) )
        { 	//termino -> factor termino'
            factor();
            termino_();
        }
        else
        {	
            error( "[termino]: Se esperaba un \"(\", un numero, un id o una literal" );
        }
    }

    private void termino_()//Cambiamos apostrofe por guion bajo
    {
        Linea_BE opMult;
        if ( preAnalisis.equals( "opmult" ) )
        { 	//termino' -> opmult factor termino'
            opMult = cmp.be.preAnalisis;
            emparejar( "opmult" );
            
            //Inicio de Accion Semantica C
            infixExp.append(opMult.lexema).append(" ");
            //Fin de accion semantica C
            
            factor();
            termino_();
        }
        else
        {	
            //termino -> empty
        }
    }

    private void factor ()
    {
        Linea_BE id;
        Linea_BE num;
        Linea_BE num_num;
        if ( preAnalisis.equals( "id" ) )
        { 	//factor -> id factor'
            id = cmp.be.preAnalisis;
            emparejar( "id" );
            
            //Inicio de Accion Semantica D
            infixExp.append(id.lexema).append(" ");
            //Fin de Accion Semantica D
            
            factor_();
        }
        else if ( preAnalisis.equals( "num" ) )
        { 	//factor -> num
            num = cmp.be.preAnalisis;
            emparejar( "num" );
            
            //Inicio de Accion Semantica E
            infixExp.append(num.lexema).append(" ");
            //Fin de Accion Semantica E
            
        }
        else if ( preAnalisis.equals( "num.num" ) )
        { 	//factor -> num.num
            num_num = cmp.be.preAnalisis;
            emparejar( "num.num" );
            
            //Inicio de Accion Semantica F
            infixExp.append(num_num.lexema).append(" ");
            //Fin de Accion Semantica F
            
        }
        else if ( preAnalisis.equals( "(" ) )
        { 	//factor -> ( expresion )
            emparejar( "(" );
            
            //Inicio de Accion Semantica G
            infixExp.append("(").append(" ");
            //Fin de Accion Semantica G
            
            expresion();
            emparejar( ")" );
            
            //Inicio de Accion Semantica H
            infixExp.append(")").append(" ");
            //Fin de Accion Semantica H
            
        }
        else
        {	
            error( "[factor]: Se esperaba un \"(\", un numero, un id o una literal" );
        }
    }

    private void factor_()//Cambiamos apostrofe por guion bajo
    {
        if ( preAnalisis.equals( "(" ) )
        { 	//factor' -> ( lista_expresiones )
            emparejar( "(" );
            
            //Inicio de Accion Semantica I
            infixExp.append("(").append(" ");
            //Fin de Accion Semantica I
            
            lista_expresiones();
            
            emparejar( ")" );
            
            //Inicio de Accion Semantica J
            infixExp.append(")").append(" ");
            //Fin de Accion Semantica J
        }
        else
        {	
            //factor' -> empty
        }
    }
}

// JAVA program to convert infix to prefix

class GFG {

    static boolean isalpha(String c) {
        return !(c.contains("+") || c.contains("*") || c.contains("(") || c.contains(")"));
    }

    static boolean isdigit(String c) {
        try {
            Double.parseDouble(c);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean isOperator(String c) {
        return (!isalpha(c) && !isdigit(c));
    }

    static int getPriority(String C) {
        if (C.equals("-")|| C.equals("+")) {
            return 1;
        } else if (C.equals("*") || C.equals("/")) {
            return 2;
        } else if (C.equals("^")) {
            return 3;
        }
        return 0;
    }
// Reverse the letters of the word

    static String reverse(char str[], int start, int end) {
// Temporary variable to store character
        char temp;
        while (start < end) {
// Swapping the first and last character
            temp = str[start];
            str[start] = str[end];
            str[end] = temp;
            start++;
            end--;
        }
        return String.valueOf(str);
    }

    static String infixToPostfix(char[] infix) {
        String infix1 = "( "+String.valueOf(infix)+" )";
        String[] arrInfix = new String(infix1).split(" ");
        arrInfix[1] = arrInfix[0];
        int l = arrInfix.length;
        Stack<String> char_stack = new Stack<>();
        String output = "";
        for (int i = 1; i < l; i++) {
// If the scanned character is an
// operand, add it to output.
            if (isalpha(arrInfix[i]) || isdigit(arrInfix[i])) {
                output += arrInfix[i] + " ";
            } // If the scanned character is an
            // ‘(‘, push it to the stack.
            else if (arrInfix[i].equals("(")) {
                char_stack.add("(");
            } // If the scanned character is an
            // ‘)’, pop and output from the stack
            // until an ‘(‘ is encountered.
            else if (arrInfix[i].equals(")")) {
                while (!char_stack.peek().equals("(")) {
                    output += char_stack.peek() + " ";
                    char_stack.pop();
                }
// Remove '(' from the stack
                char_stack.pop();
            } // Operator found
            else {
                if (isOperator(char_stack.peek())) {
                    while ((getPriority(arrInfix[i])
                            < getPriority(char_stack.peek()))
                            || (getPriority(arrInfix[i])
                            <= getPriority(char_stack.peek())
                            && arrInfix[i].equals("^"))) {
                        output += (char_stack.peek() + " ");
                        char_stack.pop();
                    }
// Push current Operator on stack
                    char_stack.add(arrInfix[i]);
                }
            }
        }
        while (!char_stack.empty()) {
            output += char_stack.pop()+ " ";
        }
        return output;
    }

    static String infixToPrefix(char[] infix) {
        /*
* Reverse String Replace ( with ) and vice versa Get Postfix Reverse Postfix *
         */
        int l = infix.length;
// Reverse infix
        String infix1 = reverse(infix, 0, l - 1);
        
        infix = infix1.toCharArray();
// Replace ( with ) and vice versa
        for (int i = 0; i < l; i++) {
            if (infix[i] == '(') {
                infix[i] = ')';
                //i++;
            } else if (infix[i] == ')') {
                infix[i] = '(';
                //i++;
            }
        }
        
        String prefix = infixToPostfix(infix);
// Reverse postfix
        //Reajustar el tamanio despues de quitar parentesis
        l = prefix.length();
        
        prefix = reverse(prefix.toCharArray(), 0, l - 1);
        
        return prefix;
    }
}
// This code is contributed by Rajput-Ji
