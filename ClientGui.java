import java.awt.*;
import javax.swing.*;


public class ClientGui extends JFrame {
    boolean dark = false;

    public ClientGui(String host, int port) {
        super("ClientGui");

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 246, 250)); //grey
        main.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3, true));
        setContentPane(main);

        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(370, 50));
        header.setBackground(new Color(52, 73, 94)); //dark grey

        JLabel title = new JLabel("Gui");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel status = new JLabel("Status");
        status.setForeground(new Color(236, 240, 241));
        status.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);

        //chat area
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        main.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottom.setBackground(main.getBackground());

        JTextField input = new JTextField();
        bottom.add(input);
        bottom.add(Box.createVerticalStrut(16));

        Color pastelPink = new Color(255, 182, 193);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        JButton sendButton = new JButton("Send");
        JButton exitButton = new JButton("Exit");
        JButton darkModeButton = new JButton("Dark Mode");

        sendButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));
        exitButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));
        darkModeButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));

        buttons.add(sendButton);
        buttons.add(exitButton);
        buttons.add(darkModeButton);
        bottom.add(buttons);
        main.add(bottom, BorderLayout.SOUTH);

        darkModeButton.addActionListener(e -> {
            if (dark == false) {
                main.setBackground(Color.DARK_GRAY);
                header.setBackground(Color.DARK_GRAY);
                chatArea.setBackground(Color.GRAY);
                chatArea.setForeground(Color.WHITE);
                input.setBackground(Color.GRAY);
                input.setForeground(Color.WHITE);

                sendButton.setForeground(Color.WHITE);
                exitButton.setForeground(Color.WHITE);
                darkModeButton.setForeground(Color.WHITE);

                bottom.setBackground(Color.DARK_GRAY);
                buttons.setBackground(Color.DARK_GRAY);
                scroll.setBackground(Color.GRAY);
                scroll.getViewport().setBackground(Color.GRAY);
                input.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                darkModeButton.setText("Light Mode");
                dark = true;
            }
            else {
                main.setBackground(new Color(245,246,250));
                header.setBackground(new Color(52,73,94));
                chatArea.setBackground(Color.WHITE);
                chatArea.setForeground(Color.BLACK);
                input.setBackground(Color.WHITE);
                input.setForeground(Color.BLACK);

                sendButton.setForeground(Color.BLACK);
                exitButton.setForeground(Color.BLACK);
                darkModeButton.setForeground(Color.BLACK);

                bottom.setBackground(new Color(245,246,250));
                buttons.setBackground(new Color(245,246,250));
                scroll.setBackground(Color.WHITE);
                scroll.getViewport().setBackground(Color.WHITE);

                input.setBorder(UIManager.getBorder("TextField.border"));

                darkModeButton.setText("Dark Mode");
                dark = false;
            }
        });

        setSize(370, 670);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGui("localhost", 8080));


    }
}
