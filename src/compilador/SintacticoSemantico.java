/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: ___________    HORA: ___________ HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un metodo por cada simbolo
 *:                 No-Terminal de la gramatica mas el metodo emparejar ().
 *:                 El analisis empieza invocando al metodo del simbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 22/Feb/2015 FGil                -Se mejoro errorEmparejar () para mostrar el
 *:                                 numero de linea en el codigo fuente donde 
 *:                                 ocurrio el error.
 *: 08/Sep/2015 FGil                -Se dejo lista para iniciar un nuevo analizador
 *:                                 sintactico.
 *: 20/FEB/2023 F.Gil, Oswi         -Se implementaron los procedures del parser
 *:                                  predictivo recursivo de leng BasicTec.
 *: 20/FEB/2025 DaniJM,Alfredo       -Se implementaron los 22 procedures de la 
 *:             Karime,DanielM       gramatica basitec.
 *:-----------------------------------------------------------------------------
 */
package compilador;

import general.Linea_BE;
import java.util.Set;

public class SintacticoSemantico {

    private Compilador cmp;
    private boolean    analizarSemantica = false;
    private String     preAnalisis;
    
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //

    public SintacticoSemantico(Compilador c) {
        cmp = c;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metodo que inicia la ejecucion del analisis sintactico predictivo.
    // analizarSemantica : true = realiza el analisis semantico a la par del sintactico
    //                     false= realiza solo el analisis sintactico sin comprobacion semantica

    public void analizar(boolean analizarSemantica) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *
        programa(new Atributos());
    }

    //--------------------------------------------------------------------------

    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;           
        } else {
            errorEmparejar( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }
    
    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
 
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

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico

    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }
    // Fin de error
    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------
    
    private void programa (Atributos programa) {
        Set<String> validos = Set.of("dim", "function", "sub", "id", "call", "if", "do", "end");
        if (validos.contains(preAnalisis)) {
            Atributos declaraciones = new Atributos();
            Atributos declaraciones_subprogramas = new Atributos();
            Atributos proposiciones_optativas = new Atributos();
            
            //programa -> declaraciones declaraciones_subprogramas proposiciones_optativas end
            declaraciones(declaraciones);
            declaraciones_subprogramas();
            proposiciones_optativas(proposiciones_optativas);
            emparejar( "end" );
            
            //accio1
            boolean sonVacios = sonTipoVacio(declaraciones, declaraciones_subprogramas, proposiciones_optativas);
            programa.tipo = sonVacios ? VACIO : ERROR_TIPO;
        } else {
            error( "[programa]: El programa debe iniciar con una declaracion, un subprograma o una proposicion, o solo terminar con la palabra end" );
        }
    }
    
    private void declaraciones(Atributos declaraciones) {
        if (preAnalisis.equals("dim")) {    
            Atributos lista_declaraciones = new Atributos();
            Atributos declaracionesHijo = new Atributos();
            
            //declaraciones -> dim lista_declaraciones declaraciones
            emparejar("dim");
            lista_declaraciones(lista_declaraciones);
            declaraciones(declaracionesHijo);
            
            //Accion2
            boolean sonVacios = sonTipoVacio(lista_declaraciones, declaracionesHijo);
            declaraciones.tipo = sonVacios ? VACIO : ERROR_TIPO;
            
        //declaraciones -> empty
        } else {
            //Accion3
            declaraciones.tipo = VACIO;
        }
    }
    
    private void lista_declaraciones(Atributos lista_declaraciones) {
        if (preAnalisis.equals("id")) {
            Atributos tipo = new Atributos();
            Atributos lista_declaraciones_ = new Atributos();
            Linea_BE id = cmp.be.preAnalisis;

            //lista_declaraciones -> id as tipo lista_declaraciones'
            emparejar("id");
            emparejar("as");
            tipo(tipo);

            //Accion4
            if (buscaTipo(id).equals(VACIO)) {
                cmp.ts.anadeTipo(id.entrada, tipo.tipo);
                lista_declaraciones.aux = VACIO;
                lista_declaraciones.dominio = tipo.tipo;
            } else {
                lista_declaraciones.aux = ERROR_TIPO;
                lista_declaraciones.dominio = VACIO;
                error(" el identificador " + id.lexema + " ya esta declarado");
            }
            
            lista_declaraciones_(lista_declaraciones_);
            
            
            //acciont5
            if (sonVacios(lista_declaraciones.aux, lista_declaraciones_.tipo)) {
                lista_declaraciones.tipo = VACIO;
                if (!lista_declaraciones.dominio.equals(VACIO)) {
                    lista_declaraciones.dominio += " x " + lista_declaraciones_.dominio;
                }
            } else {
                lista_declaraciones.tipo = ERROR_TIPO;
                lista_declaraciones.dominio = VACIO;
            }
        } else {
            error("[lista_declaraciones]: Se esperaba un identificador");
        }
    }

    private void tipo(Atributos tipo) {
        Set<String> validos = Set.of(INTEGER, SINGLE, STRING);
        if (validos.contains(preAnalisis)) {
            //Accion 8, 9, 10
            tipo.tipo = preAnalisis;
            
            //tipo -> integer  | single | string 
            emparejar(preAnalisis);
            
        } else {
            error("[tipo]: Se esperaba un tipo de dato");
        }
    }

    private void lista_declaraciones_(Atributos lista_declaraciones_) {
        if (preAnalisis.equals(",")) {
            Atributos lista_declaraciones = new Atributos();
            
            //lista_declaraciones' -> , lista_declaraciones
            emparejar(",");
            lista_declaraciones(lista_declaraciones);
            
            //Accion6
            lista_declaraciones_.tipo = lista_declaraciones.tipo;
            lista_declaraciones_.dominio = lista_declaraciones.dominio;
        } else {
            //lista_declaraciones' -> empty Accion 7
            lista_declaraciones_.tipo = VACIO;
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
            Atributos declaracion_funcion = new Atributos();
            declaracion_funcion(declaracion_funcion);
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

    private void declaracion_funcion(Atributos declaracion_funcion) {
        if (preAnalisis.equals("function")) {
            //declaracion_funcion -> function id argumentos as tipo proposiciones_optativas end function
            Atributos argumentos = new Atributos();
            Atributos proposiciones_optativas = new Atributos();
            Atributos tipo = new Atributos();

            emparejar("function");
            Linea_BE id = cmp.be.preAnalisis;
            emparejar("id");
            argumentos(argumentos);
            emparejar("as");
            tipo(tipo);

            //Accion semantica 15
            if (buscaTipo(id).equals(VACIO)) {
                declaracion_funcion.aux = VACIO;
                anadeTipo(id.entrada, argumentos.dominio + " -> " + tipo.tipo);
            } else {
                declaracion_funcion.aux = ERROR_TIPO;
            }

            proposiciones_optativas(proposiciones_optativas);
            emparejar("end");
            emparejar("function");

            //Accion semantica 16
            if (sonVacios(declaracion_funcion.aux, proposiciones_optativas.tipo, argumentos.tipo)) {
                declaracion_funcion.tipo = VACIO;
            } else {
                if (!declaracion_funcion.aux.equals(VACIO)) {
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion_funcion]: La variable " + id.lexema + " ya esta declarada.");
                } else {
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion_funcion]: Error en la declaracion de la funcion");
                }
                declaracion_funcion.tipo = ERROR_TIPO;
            }
        } else {
            error("[declaracion_funcion]: Se esperaba la palabra reservada \"función\" para una funcion");
        }
    }
    
    private void declaracion_subrutina ()
    {
        if ( preAnalisis.equals( "sub" ) )
        { 	//declaracion_subrutina -> sub id argumentos proposiciones_optativas end sub
            emparejar( "sub" );
            emparejar( "id" );
            //argumentos();
            //proposiciones_optativas();
            emparejar( "end" );
            emparejar( "sub" );
        }
        else
        {
            error( "[declaracion_subrutina]: Se esperaba la palabra reservada \"sub\" para una subrutina" );
        }
    }

    private void argumentos(Atributos argumentos) {
        if (preAnalisis.equals("(")) {
            //argumentos -> ( lista_declaraciones )
            Atributos lista_declaraciones = new Atributos();
            emparejar("(");
            lista_declaraciones(lista_declaraciones);
            emparejar(")");
            
            //Accion semantica 19
            argumentos.tipo = lista_declaraciones.tipo;
            argumentos.dominio = lista_declaraciones.dominio;
        } else {
            //argumentos -> empty
            //Accion20
            argumentos.tipo = VACIO;
            argumentos.dominio = VACIO;
        }
    }

    private void proposiciones_optativas(Atributos proposiciones_optativas) {
        Set<String> validos = Set.of("id", "call", "if", "do");
        if (validos.contains(preAnalisis)) {
            //proposiciones_optativas -> proposicion proposiciones_optativas
            proposicion();
            //proposiciones_optativas();
        } else {
            proposiciones_optativas.tipo = VACIO;
        }
    }

    private void proposicion ()
    {
        if ( preAnalisis.equals( "id" ) )
        { 	//proposicion -> id opasig expresion
            emparejar( "id" );
            emparejar( "opasig" );
            expresion();
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
            //proposiciones_optativas();
            emparejar( "else" );
            //proposiciones_optativas();
            emparejar( "end" );
            emparejar( "if" );
        }
        else if ( preAnalisis.equals( "do" ) )
        { 	//proposicion -> do while condicion proposiciones_optativas loop
            emparejar( "do" );
            emparejar( "while" );
            condicion();
            //proposiciones_optativas();
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
        if ( preAnalisis.equals( "opsuma" ) )
        { 	//expresion' -> opsuma termino expresion'
            emparejar( "opsuma" );
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
        if ( preAnalisis.equals( "opmult" ) )
        { 	//termino' -> opmult factor termino'
            emparejar( "opmult" );
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
        if ( preAnalisis.equals( "id" ) )
        { 	//factor -> id factor'
            emparejar( "id" );
            factor_();
        }
        else if ( preAnalisis.equals( "num" ) )
        { 	//factor -> num
            emparejar( "num" );
        }
        else if ( preAnalisis.equals( "num.num" ) )
        { 	//factor -> num.num
            emparejar( "num.num" );
        }
        else if ( preAnalisis.equals( "(" ) )
        { 	//factor -> ( expresion )
            emparejar( "(" );
            expresion();
            emparejar( ")" );
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
            lista_expresiones();
            emparejar( ")" );
        }
        else
        {	
            //factor' -> empty
        }
    }
    
    
    
    private boolean esNum(String tipo) {
        return INTEGER.equals(tipo) || SINGLE.equals(tipo);
    }
    
    private String combinarTipos(String tipo1, String tipo2) {
        if (tipo1.equals(tipo2)) {
            return tipo1;
        }
        
        if (esNum(tipo1) && esNum(tipo2)) {
            return SINGLE;
        }
        
        return ERROR_TIPO;
    }
    
    private boolean sonTipoVacio(Atributos ...atributos) {
        for (Atributos elemento : atributos) {
            if (!elemento.tipo.equals(VACIO)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean sonVacios(String ...args) {
        for (String arg : args) {
            if (!arg.equals(VACIO)) {
                return false;
            }
        }
        
        return true;
    }
    
    private String buscaTipo(Linea_BE id) {
        return cmp.ts.buscaTipo(id.entrada);
    }
    
    private void anadeTipo(int entrada, String tipo) {
        cmp.ts.anadeTipo(entrada, tipo);
    }
    
    
    
    private static final String INTEGER = "integer";
    private static final String SINGLE = "single";
    private static final String STRING = "string";
    private static final String ERROR_TIPO = Atributos.ERROR_TIPO;
    private static final String VACIO = Atributos.VACIO;
}
//------------------------------------------------------------------------------
//::