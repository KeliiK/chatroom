import javax.swing.*;
import java.awt.*;
import java.util.Scanner;
import javax.swing.border.AbstractBorder;

public class ConsoleToLabels {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());

        // Read from terminal in main thread
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Type something: ");
            String input = scanner.nextLine();
            addLabel(input);
        }
    }

    private static JPanel panel;
    private static JPanel panel2;

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Console to JLabel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        panel2 = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 200));
        panel.setMaximumSize(new Dimension(350, 250));
        panel.setMinimumSize(new Dimension(250, 150));

        JScrollPane scrollPane = new JScrollPane(panel);
        frame.add(scrollPane);
        frame.add(panel2);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    // Must update Swing components on the EDT
    private static void addLabel(String text) {
        SwingUtilities.invokeLater(() -> {
            RoundedLabel label = new RoundedLabel(text, 30);
            label.setBackground(new Color(230, 230, 230));

            panel.add(Box.createVerticalStrut(10));
            panel.add(label);
            panel.revalidate(); // refresh layout
            panel.repaint();    // repaint window
        });
    }
}

