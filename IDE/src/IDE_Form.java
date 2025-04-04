import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

                    // TODO: Adicionar código Java do GALS p/ realizar compilação da TextArea

                    window.compileResLabel.setText("Compilado com sucesso!  | " + timeString);
                } catch (Exception ex) {
                    // TODO: Tratar exceptions geradas pelo GALS corretamente após adicionar compilador
                    window.compileResLabel.setText("Erro de compilação | " + timeString);
                }
            }
        });
    }
}
