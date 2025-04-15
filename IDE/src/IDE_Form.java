import javax.swing.*;
import java.awt.*;
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

    private static String getPositionInfo(int position, JTextArea textArea) {
        int lineNum = 0;
        int columnNum = 0;

        try {
            lineNum = textArea.getLineOfOffset(position);
            int lineStart = textArea.getLineStartOffset(lineNum);
            columnNum = position - lineStart;
            lineNum += 1; // Convert to 1-based numbering
        } catch (Exception e) {
            return "Posicao: " + position;
        }
        return "Linha: " + lineNum + ", Coluna: " + columnNum;
    }

    private static void mostrarErro(int position, JTextArea textArea) {
        textArea.setCaretPosition(position);
        textArea.grabFocus();
        textArea.select(position, position + 1);
    }

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
                    String posicao  = getPositionInfo(err.getPosition(), window.codeField);
                    window.compileResLabel.setText("Erro lexico na " + posicao + " | "  + timeString);
                    mostrarErro(err.getPosition(), window.codeField);
                }
                catch (SyntacticError err) {
                    String posicao  = getPositionInfo(err.getPosition(), window.codeField);
                    window.compileResLabel.setText("Erro sintatico na " + posicao + " | "  + timeString);
                    mostrarErro(err.getPosition(), window.codeField);
                }
                catch (SemanticError err) {
                    String posicao  = getPositionInfo(err.getPosition(), window.codeField);
                    window.compileResLabel.setText("Erro semantico na " + posicao + " | "  + timeString);
                    mostrarErro(err.getPosition(), window.codeField);
                }
            }
        });
    }
}
