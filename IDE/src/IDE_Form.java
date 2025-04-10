import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import GALS.*;

public class IDE_Form extends JFrame{
    private JButton compileBtn;
    private JLabel compileResLabel;
    private JTextArea codeField;
    private JPanel mainPanel;

    public static void main(String[] args){
        IDE_Form window = new IDE_Form();
        window.setContentPane(window.mainPanel);
        window.setTitle("UNIVALI IDE v1.0.0");
        window.setSize(800,600);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.compileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtém hora atual p/ mostrar no resultado
                LocalTime currentTime = LocalTime.now();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String timeString = currentTime.format(timeFormatter);

                try {

                    Lexico lex = new Lexico();
                    Sintatico sint = new Sintatico();
                    Semantico sem = new Semantico();

                    lex.setInput(window.codeField.getText());
                    sint.parse(lex, sem);

                    window.compileResLabel.setText("Compilado com sucesso!  | " + timeString);
                } catch (LexicalError err) {
                    window.compileResLabel.setText("Erro lexico na posição " + err.getPosition() + " | "  + timeString);
                }
                catch (SyntacticError err) {
                    window.compileResLabel.setText("Erro Sintatico na posição " + err.getPosition() + " | "  + timeString);
                }
                catch (SemanticError err) {
                    window.compileResLabel.setText("Erro Semantico na posição " + err.getPosition() + " | "  + timeString);
                }
            }
        });
    }
}
