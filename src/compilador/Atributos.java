/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: Ene - Jun 2025    HORA: 08:00 HRS
 *:                                   
 *:               
 *:         Clase Atributos
 *                 
 *:                           
 *: Archivo       : Atributos.java
 *: Autor         : Alfredo Palacios, Daniel Juarez, Daniel Marin, Veronica Rios
 *:                 Grupo de Lenguajes y Automatas II
 *: Fecha         : 01/MAYO/2025
 *: Compilador    : Java JDK 21
 *: Descripción   : Representa los atributos sintáctico-semánticos que se utilizan en 
                    la case SintacticoSemantico.
                    Cada instancia de esta clase se utiliza para almacenar y transferir
                    información entre las producciones de la gramática

 *: Ult.Modif.    :
 *:  Fecha      Modificó                     Modificacion
 *:=============================================================================
 *: 01/05/2025  Alfredo, Daniel Juarez       Se agregaron los atributos (tipo, dominio, aux y h)
                Karime, Daniel Marin         y constantes VACIO y ERROR_TIPO
 *:-----------------------------------------------------------------------------
 */


package compilador;

public class Atributos {
    public String tipo    = VACIO;
    public String dominio = "";
    public String aux     = VACIO;
    public String h       = "";
    
    public static final String VACIO       = "";
    public static final String ERROR_TIPO  = "ERROR_TIPO";
}
