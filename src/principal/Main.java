package principal;

import gui.CompiladorFrame;
import compilador.Compilador;
import javax.swing.JFrame;

public class Main {
    
    //--------------------------------------------------------------------------
    
    public static void main ( String [] args ) {
        /* Set the Windows look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                CompiladorFrame cmpFrame = new CompiladorFrame ( new Compilador () );
                cmpFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                cmpFrame.setColaboradoresAcercaDe ( colaboradores );
                cmpFrame.setVisible ( true );
            }
        });
        
    }

    //--------------------------------------------------------------------------
    // Nombres a desplegar en el "Acerca de" como colaboradores .
    
    public static final String [] colaboradores = { 
        "Lenguajes y Automatas II",
        "Grupo 08:00am - 09:00am :: Semestre Ene-Jun/2025",
        "20130799 Daniel Arnulfo Juarez Martinez",
        "22130550 Alfredo Alberto Palacios Rodríguez",
        "C20130864 Daniel Marín Ibarra",
        "21130567 Verónica Karime Hernández Ríos"
    };    
}
