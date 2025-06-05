/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: Ene - Jun 2025    HORA: 08:00 HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 21
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
 *: 20/FEB/2025 DaniJM,Alfredo      -Se implementaron los 22 procedures de la 
 *:             Karime,DanielM       gramatica basitec.
 *: 01/Mayo/2025 DanielJM, Alfredo  -Se implementaron las 50 acciones semanticas 
                Karime, DanielM      del lenguaje basictec.
                                    -Se agregaron las distintas constantes que se
                                     Utilizan en las acciones semánticas (VACIO, ERROR_TIPO,
                                     INTEGER, SINGLE, STRING)
                                    -Se agregaron algunos métodos auxiliares para
                                     Simplificar el código
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
        Atributos proposiciones_optativas = new Atributos();
        Atributos declaraciones = new Atributos();
        Atributos declaraciones_subprogramas = new Atributos();
        
        Set<String> validos = Set.of("dim", "function", "sub", "id", "call", "if", "do", "end");
        if (validos.contains(preAnalisis)) {
            //Atributos proposiciones_optativas = new Atributos();
            
            //programa -> declaraciones declaraciones_subprogramas proposiciones_optativas end
            declaraciones(declaraciones);
            declaraciones_subprogramas(declaraciones_subprogramas);
            proposiciones_optativas(proposiciones_optativas);
            emparejar( "end" );
            //==========================================================================
            //Acción semántica 1
            if( analizarSemantica ){
                boolean sonVacios = sonTipoVacio(declaraciones, declaraciones_subprogramas, proposiciones_optativas);
                programa.tipo = sonVacios ? VACIO : ERROR_TIPO;
            }
            //Fin Acción semántica 1
        } else {
            error( "[programa]: El programa debe iniciar con una declaracion, un subprograma o una proposicion, o solo terminar con la palabra end." );
        }
    }
    
    private void declaraciones(Atributos declaraciones) {
        Atributos lista_declaraciones = new Atributos();
        Atributos declaracionesHijo = new Atributos();
        if (preAnalisis.equals("dim")) {    
            //declaraciones -> dim lista_declaraciones declaraciones
            emparejar("dim");
            lista_declaraciones(lista_declaraciones);
            declaraciones(declaracionesHijo);
            
            //Acción semántica 2
            if( analizarSemantica ){
                boolean sonVacios = sonTipoVacio(lista_declaraciones, declaracionesHijo);
                declaraciones.tipo = sonVacios ? VACIO : ERROR_TIPO;
            }
            //Fin Acción semántica 2
            
        //declaraciones -> empty
        } else {
            ////Acción semántica 3
            if( analizarSemantica )
                declaraciones.tipo = VACIO;
            //Fin Acción semántica 3
        }
    }
    
    private void lista_declaraciones(Atributos lista_declaraciones) {
        Atributos tipo = new Atributos();
        Atributos lista_declaraciones_ = new Atributos();
        Linea_BE id;
        if (preAnalisis.equals("id")) {
            id = cmp.be.preAnalisis;
            
            //lista_declaraciones -> id as tipo lista_declaraciones'
            emparejar("id");
            emparejar("as");
            tipo(tipo);
            
            //Accion semántica 4
            if( analizarSemantica ){
                if( cmp.ts.buscaTipo(id.entrada).equals("")){
                    cmp.ts.anadeTipo(id.entrada, tipo.tipo);
                    lista_declaraciones.aux = VACIO;
                    lista_declaraciones.dominio = tipo.tipo;
                }else{
                    lista_declaraciones.aux = ERROR_TIPO;
                    lista_declaraciones.dominio = "";
                }
            }
            //Fin Acción semántica 4
            
            lista_declaraciones_(lista_declaraciones_);
            
            //Accion semántica 5
            if( analizarSemantica ) {
                if( lista_declaraciones.aux.equals(VACIO) && lista_declaraciones_.tipo.equals(VACIO) ){
                    lista_declaraciones.tipo = VACIO;
                    if(! lista_declaraciones_.dominio.equals("") ){
                        lista_declaraciones.dominio = lista_declaraciones.dominio + " x " + lista_declaraciones_.dominio;
                    }
                } else {
                    if( lista_declaraciones.aux.equals(VACIO) )
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_declaraciones]: Error en la declaracion de variables");
                    else
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_declaraciones]: La variable " + id.lexema + " ya esta declarada.");
                    
                    lista_declaraciones.tipo = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 5
        } else {
            error("[lista_declaraciones]: Se esperaba un identificador");
        }
    }

    private void tipo(Atributos tipo) {
        if(preAnalisis.equals("integer")){
            //tipo -> integer            
            emparejar("integer");
            //Accion semantica 8
            if( analizarSemantica ){
                tipo.tipo = "integer";
            }
            //Fin de Accion semantica 8
        }else if(preAnalisis.equals("single")){
            //tipo -> single  
            emparejar("single");
            //Accion semantica 9
            if( analizarSemantica ){
                tipo.tipo = "single";
            }
            //Fin de Accion semantica 9
        }else if( preAnalisis.equals("string") ){
            //tipo -> string  
            emparejar("string");
            //Accion semantica 10
            if( analizarSemantica ){
                tipo.tipo = "string";
            }
            //Fin de Accion semantica 10
        }else {
            error("[tipo]: Se esperaba un tipo de dato");
        }
    }

    private void lista_declaraciones_(Atributos lista_declaraciones_) {
        Atributos lista_declaraciones = new Atributos();
        if (preAnalisis.equals(",")) {            
            //lista_declaraciones' -> , lista_declaraciones
            emparejar(",");
            lista_declaraciones(lista_declaraciones);
            
            //Accion semántica 6
            lista_declaraciones_.tipo = lista_declaraciones.tipo;
            lista_declaraciones_.dominio = lista_declaraciones.dominio;
            //Fin Acción semántica 6
        } else {
            //lista_declaraciones' -> empty
            //Acción semántica 7
            lista_declaraciones_.tipo = VACIO;
            //Fin Acción semántica 7
        }
    }

    private void declaraciones_subprogramas (Atributos declaraciones_subprogramas)
    {
        Atributos declaracion_subprograma = new Atributos();
        Atributos declaraciones_subprogramas1 = new Atributos();
        if ( preAnalisis.equals( "function" ) || preAnalisis.equals( "sub" ) )
        { 	//declaraciones_subprogramas -> declaracion_subprograma declaraciones_subprogramas
            declaracion_subprograma(declaracion_subprograma);
            declaraciones_subprogramas(declaraciones_subprogramas1);
            
            //Accion semántica 11
            if( analizarSemantica ){
                if( declaraciones_subprogramas1.tipo.equals(VACIO) && declaracion_subprograma.tipo.equals(VACIO) ){
                    declaraciones_subprogramas.tipo = VACIO;
                }else{
                    declaraciones_subprogramas.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[declaraciones_subprogramas]: Error en la declaracion de subprogramas");    
                }
            }
            //Fin Acción semántica 11
        }
        else
        {
            //Accion semántica 12
            if( analizarSemantica ){
                declaraciones_subprogramas.tipo = VACIO;
            }
            //Fin Acción semántica 12
            //declaraciones_subprogramas -> empty
        }
    }

    private void declaracion_subprograma (Atributos declaracion_subprograma)
    {
        Atributos declaracion_subrutina = new Atributos();
        Atributos declaracion_funcion = new Atributos();
        if ( preAnalisis.equals( "function" ) )
        { 	//declaracion_subprograma -> declaracion_funcion
            declaracion_funcion(declaracion_funcion);
            
            //Accion semántica 13
            if( analizarSemantica ){
                declaracion_subprograma.tipo = declaracion_funcion.tipo;
            }
            //Fin Acción semántica 13
        }
        else if ( preAnalisis.equals( "sub" ) )
        { 	//declaracion_subprograma -> declaracion_subrutina
            
            declaracion_subrutina(declaracion_subrutina);
            //Accion semantica 14
            if( analizarSemantica ){
                declaracion_subprograma.tipo = declaracion_subrutina.tipo;
            }
            //Fin Acción semántica 
        }
        else
        {
            error( "[declaracion_subprograma]: Se esperaba una funcion o una subrutina" );
        }
    }

    private void declaracion_funcion (Atributos declaracion_funcion)
    {
        Atributos argumentos = new Atributos();
        Atributos proposiciones_optativas = new Atributos();
        Atributos tipo = new Atributos();
        Linea_BE id;
        if ( preAnalisis.equals( "function" ) )
        { 	//declaracion_funcion -> function id argumentos as tipo proposiciones_optativas end function
            emparejar( "function" );
            id = cmp.be.preAnalisis;
            emparejar( "id" );
            argumentos(argumentos);
            emparejar( "as" );
            tipo(tipo);
            
            //Accion semantica 15
            if( analizarSemantica ){
                if( buscaTipo(id).equals("")){
                    declaracion_funcion.aux = VACIO;
                    cmp.ts.anadeTipo(id.entrada, argumentos.dominio + " -> " + tipo.tipo);
                }else{
                    declaracion_funcion.aux = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 15
            
            proposiciones_optativas(proposiciones_optativas);
            emparejar( "end" );
            emparejar( "function" );
            
            //Accion semantica 16
            if( analizarSemantica ){
                if( declaracion_funcion.aux.equals(VACIO) && proposiciones_optativas.tipo.equals(VACIO) && argumentos.tipo.equals(VACIO) ){
                    declaracion_funcion.tipo = VACIO;
                }else{
                    if(! declaracion_funcion.aux.equals(VACIO) ){
                      cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion_funcion]: La variable " + id.lexema + " ya esta declarada.");  
                    }else{
                      cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion_funcion]: Error en la declaracion de la funcion");    
                    }
                    declaracion_funcion.tipo = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 16
        }
        else
        {
            error( "[declaracion_funcion]: Se esperaba la palabra reservada \"función\" para una funcion" );
        }
    }

    private void declaracion_subrutina (Atributos declaracion_subrutina)
    {
        Atributos argumentos = new Atributos();
        Atributos proposiciones_optativas = new Atributos();
        Linea_BE id;
        if ( preAnalisis.equals( "sub" ) )
        { 	//declaracion_subrutina -> sub id argumentos proposiciones_optativas end sub
            emparejar( "sub" );
            id = cmp.be.preAnalisis;
            emparejar( "id" );
            argumentos(argumentos);
            
            //Accion semantica 17
            if( analizarSemantica ){
                if( buscaTipo(id).equals("") && argumentos.tipo.equals(VACIO)){
                    declaracion_subrutina.aux = VACIO;
                    cmp.ts.anadeTipo(id.entrada, argumentos.dominio + " -> " + "nil");
                }else{
                    declaracion_subrutina.aux = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 17
            
            proposiciones_optativas(proposiciones_optativas);
            emparejar( "end" );
            emparejar( "sub" );
            
            //Accion semantica 18
            if( analizarSemantica ){
                if( declaracion_subrutina.aux.equals(VACIO)&&proposiciones_optativas.tipo.equals(VACIO) ){
                    declaracion_subrutina.tipo = VACIO;
                }else{
                    if(! declaracion_subrutina.aux.equals(VACIO) ){
                      cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion_subrutina]: La variable " + id.lexema + " ya esta declarada.");
                    }else{
                      cmp.me.error(Compilador.ERR_SEMANTICO, "[declaracion_subrutina]: Error en la declaracion de la subrutina");
                    }
                    declaracion_subrutina.tipo = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 18
        }
        else
        {
            error( "[declaracion_subrutina]: Se esperaba la palabra reservada \"sub\" para una subrutina" );
        }
    }

    private void argumentos (Atributos argumentos)
    {
        Atributos lista_declaraciones = new Atributos();
        if ( preAnalisis.equals( "(" ) )
        { 	//argumentos -> ( lista_declaraciones )
            emparejar( "(" );
            lista_declaraciones(lista_declaraciones);
            emparejar( ")" );
            
            //Accion semantica 19
            if( analizarSemantica ){
                argumentos.tipo = lista_declaraciones.tipo;
                argumentos.dominio = lista_declaraciones.dominio;
            }
            //Fin Acción semántica 19
        }
        else
        {
            //Accion semantica 20
            if( analizarSemantica ){
                argumentos.tipo = VACIO;
                argumentos.dominio = "";
            }
            //Fin Acción semántica 20
            //argumentos -> empty
        }
    }

    private void proposiciones_optativas(Atributos proposiciones_optativas) {
        Atributos proposiciones_optativas1 = new Atributos();
        Atributos proposicion = new Atributos();
        Set<String> validos = Set.of("id", "call", "if", "do");
        if (validos.contains(preAnalisis)) {
            //proposiciones_optativas -> proposicion proposiciones_optativas
            proposicion(proposicion);
            proposiciones_optativas(proposiciones_optativas1);
            
            //Accion semantica 21
            if( analizarSemantica ){
                if( proposicion.tipo.equals(VACIO) && proposiciones_optativas1.tipo.equals(VACIO) ){
                    proposiciones_optativas.tipo = VACIO;
                }else{
                    proposiciones_optativas.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[proposiciones_optativas]: Error en proposiciones optativas");
                }
            }
            //Fin Acción semántica 21
        }else{
            //Accion semantica 22
            if( analizarSemantica ){
                proposiciones_optativas.tipo = VACIO;
            }
            //Fin Acción semántica 22
            //proposiciones_optativas -> empty
        }
    }

    private void proposicion (Atributos proposicion)
    {
        Atributos proposiciones_optativas1 = new Atributos();
        Atributos proposiciones_optativas2 = new Atributos();
        Atributos condicion = new Atributos();
        Atributos expresion = new Atributos();
        Atributos proposicion_ = new Atributos();
        Linea_BE id;
        if ( preAnalisis.equals( "id" ) )
        { 	//proposicion -> id opasig expresion
            id = cmp.be.preAnalisis;
            emparejar( "id" );
            emparejar( "opasig" );
            expresion(expresion);
            
            //Acction semantica 23
            if( analizarSemantica ){
                if(!expresion.tipo.equals(ERROR_TIPO) && !buscaTipo(id).equals("")){
                    if( expresion.tipo.equals(buscaTipo(id)) || ( buscaTipo(id).equals("single") && !expresion.tipo.equals("string") )){
                        proposicion.tipo = VACIO;
                    }else{
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Incompatibilidad de tipos ");
                        proposicion.tipo = ERROR_TIPO;
                    }
                }else{
                    if( buscaTipo(id).equals("") )
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: No se encuentra el id "+id.lexema);
                    else
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Incompatibilidad de tipos ");
                    proposicion.tipo = ERROR_TIPO;
                }
            }   
            //Fin Acción semántica 23
        }
        else if ( preAnalisis.equals( "call" ) )
        { 	//proposicion -> call id proposicion'
            //System.out.println("Token: "+cmp.be.preAnalisis.lexema);
            emparejar( "call" );
            id = cmp.be.preAnalisis;
            emparejar( "id" );
            //System.out.println("Token: "+id.lexema);
            proposicion_(proposicion_);
            
            //Accion semantica 24
            if(analizarSemantica){
                if(buscaTipo(id).contains("->") && proposicion_.tipo.equals(VACIO)){//Es una funcion
                    String partes[] = buscaTipo(id).split("->");
                    String D = partes[0].trim();
                    String R = partes[1].trim();
                    if( R.equals("nil") ){   
                        if(proposicion_.dominio.equals(D) )
                            proposicion.tipo = VACIO;
                        else{
                            proposicion.tipo = ERROR_TIPO;
                            cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Incompatibilidad en los parametros");
                        }
                    }else{
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: El id "+id.lexema+" no es una subrutina");
                        proposicion.tipo = ERROR_TIPO;
                    }
                }else{
                    proposicion.tipo = ERROR_TIPO;
                    if(! buscaTipo(id).contains("->") )
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: El id "+id.lexema+" no es una subrutina");
                    else
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Ocurrio un error en los parametros");
                }
            }
            //Fin Acción semántica 24
        }
        else if ( preAnalisis.equals( "if" ) )
        { 	//proposicion -> if condición then proposiciones_optativas else proposiciones_optativas end if
            emparejar( "if" );
            condicion(condicion);
            emparejar( "then" );
            proposiciones_optativas(proposiciones_optativas1);
            emparejar( "else" );
            proposiciones_optativas(proposiciones_optativas2);
            emparejar( "end" );
            emparejar( "if" );
            
            //Accion semantica 25
            if(analizarSemantica){
                if( condicion.tipo.equals(VACIO) && proposiciones_optativas1.tipo.equals(VACIO) && proposiciones_optativas2.tipo.equals(VACIO) )
                    proposicion.tipo = VACIO;
                else{
                    proposicion.tipo = ERROR_TIPO;
                    if( condicion.tipo.equals(ERROR_TIPO) )
                     cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Ocurrio un error al evaluar la condicion");
                    else
                     cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Ocurrio un error dentro del cuerpo de la sentencia if");
                }
            }
            //Fin Acción semántica 25
        }
        else if ( preAnalisis.equals( "do" ) )
        { 	//proposicion -> do while condicion proposiciones_optativas loop
            emparejar( "do" );
            emparejar( "while" );
            condicion(condicion);
            proposiciones_optativas(proposiciones_optativas1);
            emparejar( "loop" );
            
            //Accion semantica 26
            if( analizarSemantica ){
                if( condicion.tipo.equals(VACIO) && proposiciones_optativas1.tipo.equals(VACIO) )
                    proposicion.tipo = VACIO;
                else{
                    proposicion.tipo = ERROR_TIPO;
                    if( condicion.tipo.equals(ERROR_TIPO) )
                     cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Ocurrio un error al evaluar la condicion");
                    else
                     cmp.me.error(Compilador.ERR_SEMANTICO, "[proposicion]: Ocurrio un error dentro del cuerpo de la sentencia do while");    
                }
            }
            //Fin Acción semántica 26
        }
        else
        {
            error( "[proposicion]: Se esperaba una proposición como un id, un call, un if o un do while" );
        }
    }

    private void proposicion_ (Atributos proposicion_)//Cambiamos apostrofe por guien bajo
    {
        Atributos lista_expresiones = new Atributos();
        if ( preAnalisis.equals( "(" ) )
        { 	//proposicion' -> ( lista_expresiones )
            emparejar( "(" );
            lista_expresiones(lista_expresiones);
            emparejar( ")" );
            
            //Accion semantica 27
            if(analizarSemantica){
                proposicion_.tipo = lista_expresiones.tipo;
                proposicion_.dominio = lista_expresiones.dominio;
            }
            //Fin Acción semántica 27
        }
        else
        {
            //Accion semantica 28
            if(analizarSemantica){
                proposicion_.tipo = VACIO;
                proposicion_.dominio = "";
            }
            //Fin Acción semántica 28
            //proposicion' -> empty
        }
    }

    private void lista_expresiones (Atributos lista_expresiones)
    {
        Atributos expresion = new Atributos();
        Atributos lista_expresiones_ = new Atributos();
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "literal" ) || preAnalisis.equals( "(" ))
        { 	//lista_expresiones -> expresion lista_expresiones' 
            expresion(expresion);
            lista_expresiones_(lista_expresiones_);
//====================================================================================================================================
            //Accion semantica 29
            if( analizarSemantica ){
                if( !expresion.tipo.equals(ERROR_TIPO) && lista_expresiones_.tipo.equals(VACIO)){
                    lista_expresiones.dominio = expresion.tipo;
                    if( !lista_expresiones_.dominio.equals("") )
                        lista_expresiones.dominio = lista_expresiones.dominio + " x " + lista_expresiones_.dominio;
                    
                    lista_expresiones.tipo = VACIO;
                }else{
                    lista_expresiones.tipo = ERROR_TIPO;
                    lista_expresiones.dominio= "";
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_expresiones]: Ocurrio un error en las expresiones"); 
                }
            }
            //Fin Acción semántica 29
        }
        else
        {
            //Accion semantica 30
            if(analizarSemantica){
                lista_expresiones.tipo = VACIO;
                lista_expresiones.dominio = "";
            }
            //Fin Acción semántica 30
            //lista_expresiones -> empty
        }
    }

    private void lista_expresiones_ (Atributos lista_expresiones_)
    {
        Atributos expresion = new Atributos();
        Atributos lista_expresiones_1 = new Atributos();
        if ( preAnalisis.equals( "," ) )
        { 	//lista_expresiones' -> , expresion lista_expresiones'
            emparejar( "," );
            expresion(expresion);
            lista_expresiones_(lista_expresiones_1);
            
            //Accion semantica 31
            if( analizarSemantica ){
                if( !expresion.tipo.equals(ERROR_TIPO) && lista_expresiones_1.tipo.equals(VACIO)){
                    lista_expresiones_.dominio = expresion.tipo;
                    if( !lista_expresiones_1.dominio.equals("") )
                        lista_expresiones_.dominio = lista_expresiones_.dominio + " x " + lista_expresiones_1.dominio;
                    lista_expresiones_.tipo = VACIO;
                }else{
                    lista_expresiones_.tipo = ERROR_TIPO;
                    lista_expresiones_.dominio = "";
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[lista_expresiones']: Ocurrio un error en las expresiones");
                }
            }
            //Fin Acción semántica 31
        }
        else
        {
            //Accion semantica 32
            if( analizarSemantica ){
                lista_expresiones_.tipo = VACIO;
                lista_expresiones_.dominio = "";
            }
            //Fin Acción semántica 32
            //lista_expresiones' -> empty
        }
    }

    private void condicion (Atributos condicion)
    {
        Atributos expresion1 = new Atributos();
        Atributos expresion2 = new Atributos();
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "literal" ) || preAnalisis.equals( "(" ) )
        { 	//condicion -> expresion oprel expresion
            expresion(expresion1);
            emparejar( "oprel" );
            expresion(expresion2);
            //Accion semantica 33
            if( analizarSemantica ){
                if(! expresion1.tipo.equals(ERROR_TIPO) && !expresion2.tipo.equals(ERROR_TIPO)){
                    if( expresion1.tipo.equals(expresion2.tipo) )
                        condicion.tipo = VACIO;
                    else if( !expresion1.tipo.equals("string") && !expresion2.tipo.equals("string"))
                        condicion.tipo = VACIO;
                    else{
                        condicion.tipo = ERROR_TIPO;
                        cmp.me.error(Compilador.ERR_SEMANTICO, "[condicion]: Incompatibilidad al comparar las expresiones");
                    }
                }else{
                    condicion.tipo = ERROR_TIPO;
                    cmp.me.error(Compilador.ERR_SEMANTICO, "[condicion]: Ocurrio un error en las expresiones");
                }
            }
            //Fin Acción semántica 33
        }
        else
        {	
            error( "[condicion]: Se esperaba un expresion, un numero, un id o una literal" );
        }
    }

    private void expresion (Atributos expresion)
    {
        Atributos termino = new Atributos();
        Atributos expresion_ = new Atributos();
        Linea_BE literal;
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "(" ) )
        { 	//expresion -> termino expresion'
            termino(termino);
            
            //Accion semantica 34
            if( analizarSemantica ){
                expresion_.h = termino.tipo;
            }
            //Fin Acción semántica 34
            expresion_(expresion_);
            
            //Accion semantica 35
            if( analizarSemantica ){
                expresion.tipo = expresion_.tipo;
            }
            //Fin Acción semántica 35
        }
        else if ( preAnalisis.equals( "literal" ) )
        { 	//expresion -> literal
            literal = cmp.be.preAnalisis;
            emparejar( "literal" );
            
            //Accion Semantica 36
            if ( analizarSemantica ) {
                if (buscaTipo(literal).equals("")) {
                    cmp.ts.anadeTipo(literal.entrada, STRING);
                }
                expresion.tipo = buscaTipo(literal);
            }
            //Fin Acción semántica 36            
        }
        else
        {	
            error( "[expresion]: Se esperaba un \"(\", un numero, un id o una literal" );
        }
    }

    private void expresion_ (Atributos expresion_)//Cambiamos apostrofe por guion bajo
    {
        Atributos expresion_1 = new Atributos();
        Atributos termino = new Atributos();
        if ( preAnalisis.equals( "opsuma" ) )
        { 	//expresion' -> opsuma termino expresion'
            emparejar( "opsuma" );
            termino(termino);
            
            //Acción semantica 37
            if( analizarSemantica ){
                if ( !termino.tipo.equals(ERROR_TIPO) && !expresion_.h.equals(ERROR_TIPO)){
                    if( termino.tipo.equals(expresion_.h) ){
                        expresion_1.h = termino.tipo;
                    }else if( !termino.tipo.equals("string") && !expresion_.h.equals("string") ){
                        expresion_1.h = "single";
                    }else{
                        expresion_1.h = ERROR_TIPO;
                    }
                }else{
                    expresion_1.h = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 37
            
            expresion_(expresion_1);
            
            //Acción semantica 38
            if( analizarSemantica ){
                expresion_.tipo = expresion_1.tipo;
            }
            //Fin Acción semántica 38
        }
        else
        {	
            //Acción semantica 39
            if( analizarSemantica ){
                expresion_.tipo = expresion_.h;
            }
            //Fin Acción semántica 39
            //expresion' -> empty
        }
    }

    private void termino (Atributos termino)
    {
        Atributos termino_ = new Atributos();
        Atributos factor = new Atributos();
        if ( preAnalisis.equals( "id" ) || preAnalisis.equals( "num" ) || preAnalisis.equals( "num.num" ) ||
             preAnalisis.equals( "(" ) )
        { 	//termino -> factor termino'
            factor(factor);
            
            //Accion semantica 40
            if( analizarSemantica ){
                termino_.h = factor.tipo;
            }
            //Fin Acción semántica 40
            
            termino_(termino_);
            
            //Accion semantica 41
            if( analizarSemantica ){
                termino.tipo = termino_.tipo;
            }
            //Fin Acción semántica 41
        }
        else
        {	
            error( "[termino]: Se esperaba un \"(\", un numero, un id o una literal" );
        }
    }

    private void termino_(Atributos termino_)//Cambiamos apostrofe por guion bajo
    {
        Atributos termino_1 = new Atributos();
        Atributos factor = new Atributos();
        if ( preAnalisis.equals( "opmult" ) )
        { 	//termino' -> opmult factor termino'
            emparejar( "opmult" );
            factor(factor);
            
            //Accion semantica 42
            if( analizarSemantica ){
                if(! factor.tipo.equals(ERROR_TIPO) && !termino_.h.equals(ERROR_TIPO) && !factor.tipo.equals("string") && !termino_.h.equals("string") ){
                    if( factor.tipo.equals(termino_.h) )
                        termino_1.h = factor.tipo;
                    else
                        termino_1.h = "single";
                }else{
                    termino_1.h = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 42
            
            termino_(termino_1);
            
            //Accion semantica 43
            if( analizarSemantica ){
                termino_.tipo = termino_1.tipo;
            }
            //Fin Acción semántica 43
        }
        else
        {	
            //Accion semantica 44
            if( analizarSemantica ){
                termino_.tipo = termino_.h;
            }
            //Fin Acción semántica 44
            //termino -> empty
        }
    }

    private void factor (Atributos factor)
    {
        Atributos expresion = new Atributos();
        Atributos factor_ = new Atributos();
        Linea_BE id;
        Linea_BE num;
        Linea_BE num_num;
        if ( preAnalisis.equals( "id" ) )
        { 	//factor -> id factor'
            id = cmp.be.preAnalisis;
            emparejar( "id" );
            factor_(factor_);
            
            //Accion semantica 45
            if( analizarSemantica ){
                if( !factor_.tipo.equals(ERROR_TIPO) && !buscaTipo(id).equals("") ){
                    if( buscaTipo(id).contains("->") ){
                        String partes[] = buscaTipo(id).split("->");
                        String D = partes[0].trim();
                        String R = partes[1].trim();
                        if( factor_.tipo.equals(D) && !R.equals("nil")){
                            factor.tipo = R;
                        }else{
                            factor.tipo = ERROR_TIPO;
                        }
                    }else if( factor_.tipo.equals(VACIO) )
                        factor.tipo = buscaTipo(id);
                    else
                        factor.tipo = ERROR_TIPO;
                }else{
                    factor.tipo = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 45
        }
        else if ( preAnalisis.equals( "num" ) )
        { 	//factor -> num
            num = cmp.be.preAnalisis;
            emparejar( "num" );
            
            //Accion semantica 46
            if( analizarSemantica ){
                if (buscaTipo(num).equals("")) {
                    cmp.ts.anadeTipo(num.entrada, INTEGER);
                }
                factor.tipo = buscaTipo(num);
            }
            //Fin Acción semántica 46

        }
        else if ( preAnalisis.equals( "num.num" ) )
        { 	//factor -> num.num
            num_num = cmp.be.preAnalisis;
            emparejar( "num.num" );
            
            //Acción semantica 47
            if( analizarSemantica ){
                if (buscaTipo(num_num).equals("")) {
                    cmp.ts.anadeTipo(num_num.entrada, SINGLE);
                }
                factor.tipo = buscaTipo(num_num);
            }
            //Fin Acción semántica 47
        }
        else if ( preAnalisis.equals( "(" ) )
        { 	//factor -> ( expresion )
            emparejar( "(" );
            expresion(expresion);
            emparejar( ")" );
            
            //Acción semantica 48
            if( analizarSemantica ){
                factor.tipo = expresion.tipo;
            }
            //Fin Acción semántica 48
        }
        else
        {	
            error( "[factor]: Se esperaba un \"(\", un numero, un id o una literal" );
        }
    }

    private void factor_(Atributos factor_)//Cambiamos apostrofe por guion bajo
    {
        Atributos lista_expresiones = new Atributos();
        if ( preAnalisis.equals( "(" ) )
        { 	//factor' -> ( lista_expresiones )
            emparejar( "(" );
            lista_expresiones(lista_expresiones);
            emparejar( ")" );
            
            //Acción semantica 49
            if( analizarSemantica ){
                if( !lista_expresiones.tipo.equals(ERROR_TIPO) ){
                    factor_.tipo = lista_expresiones.dominio;
                }else{
                    factor_.tipo = ERROR_TIPO;
                }
            }
            //Fin Acción semántica 49
        }
        else
        {
            //Acción semantica 50
            if( analizarSemantica ){
                factor_.tipo = VACIO;
            }
            //Fin Acción semántica 50
            //factor' -> empty
        }
    }
    
    private boolean sonTipoVacio(Atributos ...atributos) {
        for (Atributos elemento : atributos) {
            if (!elemento.tipo.equals(VACIO)) {
                return false;
            }
        }
        
        return true;
    }
    
    private String buscaTipo(Linea_BE id) {
        return cmp.ts.buscaTipo(id.entrada);
    }
    
    
    private static final String INTEGER = "integer";
    private static final String SINGLE = "single";
    private static final String STRING = "string";
    private static final String ERROR_TIPO = Atributos.ERROR_TIPO;
    private static final String VACIO = Atributos.VACIO;
}