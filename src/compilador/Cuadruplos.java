/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:        SEMESTRE: ______________            HORA: ______________ HRS
 *:                                   
 *:               
 *:    # Clase con la funcionalidad del Generador de Cuadruplos
 *                 
 *:                           
 *: Archivo       : Cuadruplos.java
 *: Autor         : Fernando Gil
 *: Fecha         : 05/06/2025
 *: Compilador    : Java JDK 7
 *: Descripción   :  
 *:                  
 *:           	     
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 23/05/2025  DanielJM, Karime    Se creó la clase Cuadruplos. Se crearón dos
 *:             Alfredo, DanielM    atributos, el Compilador y un arreglo de 
 *:                                 la clase Cuadruplo. También se añadierón los 
 *:                                 métodos inicializar(), agregar(), vaciar(),
 *:                                 getTamano(), getCuadruplos() y el constructor
 *:-----------------------------------------------------------------------------
 */
package compilador;

import java.util.ArrayList;


public class Cuadruplos {
    public  ArrayList<Cuadruplo> cuadruplos;
    private Compilador cmp;
    
    
    public Cuadruplos ( Compilador c ) {
        cmp = c;
        cuadruplos = new ArrayList<> ();
    }
          
    public void inicializar () {
        vaciar ();
    }
    
    public void agregar ( Cuadruplo cuadruplo ) {
        cuadruplos.add ( cuadruplo );
    }
    
    public void vaciar () {
        cuadruplos.clear ();
    }
    
    public int getTamano () {
        return cuadruplos.size();
    }
    
    public ArrayList<Cuadruplo> getCuadruplos () {
        return cuadruplos;
    }
    
}
