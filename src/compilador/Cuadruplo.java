/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:        SEMESTRE: ______________            HORA: ______________ HRS
 *:                                   
 *:               
 *:    # Clase con la funcionalidad de generar un Cuadruplo
 *                 
 *:                           
 *: Archivo       : Cuadruplo.java
 *: Autor         : Fernando Gil
 *: Fecha         : 05/06/2025
 *: Compilador    : Java JDK 7
 *: Descripción   :  
 *:                  
 *:           	     
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 23/05/2025  DanielJM, Karime    Se creó la clase Cuadruplo para su uso en
 *:             Alfredo, DanielM    en la clase Cuadruplos. Se creó su
 *:                                 constructor y sus atributos solamente
 *:-----------------------------------------------------------------------------
 */
package compilador;


public class Cuadruplo {
  
    public String op;
    public String arg1;
    public String arg2;
    public String resultado;

    public Cuadruplo ( String op, String arg1, String arg2, String resultado) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.resultado = resultado;
    }
    
    
}
