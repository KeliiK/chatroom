import java.awt.*;
import javax.swing.*;


public class ClientGui extends JFrame {
    boolean dark = false;

    private JPanel chatArea;
    private JScrollPane scrollPane;

    private JTextField input;

    private JLabel pfp;
    private ImageIcon[] pfps = new ImageIcon[6];
    private int currentPfpIndex = 0;


    public ClientGui(String host, int port) {

        super("ClientGui");

        pfps[0] = loadAndScaleIcon("cats/cat1.png", 24);
        pfps[1] = loadAndScaleIcon("cats/cat2.png", 24);
        pfps[2] = loadAndScaleIcon("cats/cat3.png", 24);
        pfps[3] = loadAndScaleIcon("cats/cat4.png", 24);
        pfps[4] = loadAndScaleIcon("cats/cat5.png", 24);
        pfps[5] = loadAndScaleIcon("cats/cat6.png", 24);

        Color pastelPink = new Color(255, 182, 193);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 246, 250)); //grey
        main.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3, true));
        setContentPane(main);

        //Header
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(370, 50));
        header.setBackground(new Color(52, 73, 94)); //dark grey

        JLabel title = new JLabel("Chatroom");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        pfp = new JLabel(pfps[0]);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        leftHeader.setOpaque(false);
       // leftHeader.add(pfp);
        leftHeader.add(title);

        header.add(leftHeader, BorderLayout.WEST);

        //Cats
        JButton pfpButton = new JButton("Pfps");
        pfpButton.setForeground(Color.WHITE);
        pfpButton.setBackground(header.getBackground());
        pfpButton.setHorizontalAlignment(SwingConstants.RIGHT);
        //pfpButton.setBorder(BorderFactory.createLineBorder(darkGrey, 2, true));
        pfpButton.setContentAreaFilled(true);
        pfpButton.setOpaque(true);
        pfpButton.setBorderPainted(false);
        pfpButton.setFocusPainted(false);

        header.add(pfpButton, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);

        //chat area
        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(4,10,10,10));

        scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scrollPane, BorderLayout.CENTER);

        //Bottom and buttons
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottom.setBackground(main.getBackground());

        input = new JTextField();
        bottom.add(input);
        bottom.add(Box.createVerticalStrut(16));

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
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

        //dark mode button
        darkModeButton.addActionListener(e -> {
            if (dark == false) {
                main.setBackground(Color.DARK_GRAY);
                header.setBackground(Color.DARK_GRAY);
                chatArea.setBackground(Color.GRAY);
                chatArea.setForeground(Color.WHITE);
                //chatWrapper.setBackground(Color.GRAY);
                input.setBackground(Color.GRAY);
                input.setForeground(Color.WHITE);

                sendButton.setForeground(Color.WHITE);
                exitButton.setForeground(Color.WHITE);
                darkModeButton.setForeground(Color.WHITE);

                pfpButton.setBackground(header.getBackground());
                pfpButton.setForeground(Color.WHITE);
               // pfpButton.setOpaque(false);

                bottom.setBackground(Color.DARK_GRAY);
                buttons.setBackground(Color.DARK_GRAY);
                scrollPane.setBackground(Color.GRAY);
                scrollPane.getViewport().setBackground(Color.GRAY);
                input.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                darkModeButton.setText("Light Mode");
                dark = true;
            }
            else {
                main.setBackground(new Color(245,246,250));
                header.setBackground(new Color(52,73,94));
                chatArea.setBackground(Color.WHITE);
                chatArea.setForeground(Color.BLACK);
                //chatWrapper.setBackground(Color.WHITE);
                input.setBackground(Color.WHITE);
                input.setForeground(Color.BLACK);

                sendButton.setForeground(Color.BLACK);
                exitButton.setForeground(Color.BLACK);
                darkModeButton.setForeground(Color.BLACK);

                pfpButton.setBackground(header.getBackground());
                pfpButton.setForeground(Color.WHITE);
                //pfpButton.setOpaque(true);

                bottom.setBackground(new Color(245,246,250));
                buttons.setBackground(new Color(245,246,250));
                scrollPane.setBackground(Color.WHITE);
                scrollPane.getViewport().setBackground(Color.WHITE);

                input.setBorder(UIManager.getBorder("TextField.border"));

                darkModeButton.setText("Dark Mode");
                dark = false;
            }
        });

        sendButton.addActionListener(e -> {
            String text = input.getText().trim();
            if(!text.isEmpty()) {
                appendMessage(text);
                input.setText("");
            }
        });

        input.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                appendMessage(text);
                input.setText("");
            }
        });

        exitButton.addActionListener(e -> {
            dispose();
            System.exit(0);
        });

        pfpButton.addActionListener(e -> {
           Object [] options = new Object[pfps.length];
           for(int i = 0; i < pfps.length; i++) {
               options[i] = pfps [i];
           }

           int choice = JOptionPane.showOptionDialog(
                   this,
                   "Choose a uglee cat pfp",
                   "pfps",
                   JOptionPane.DEFAULT_OPTION,
                   JOptionPane.PLAIN_MESSAGE,
                   null,
                   options,
                   options[currentPfpIndex]
           );

           if (choice >= 0 && choice < pfps.length) {
               currentPfpIndex = choice;
               pfp.setIcon(pfps[currentPfpIndex]);
           }

        });

        setSize(370, 670);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setOpaque(false);

            JLabel iconLabel = new JLabel(pfps[currentPfpIndex]);
            JLabel textLabel = new JLabel(message);

            row.add(iconLabel);
            row.add(Box.createHorizontalStrut(8));
            row.add(textLabel);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            chatArea.add(row);
            chatArea.add(Box.createVerticalStrut(0));

            chatArea.revalidate();
            chatArea.repaint();

            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
            //chatArea.append(message + "\n");
            //chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }



    public void closeWindow() {
        SwingUtilities.invokeLater(() -> {
            dispose();
        });
    }

    private ImageIcon loadAndScaleIcon(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        //SwingUtilities.invokeLater(() -> new ClientGui("localhost", 8080));


    }
}
