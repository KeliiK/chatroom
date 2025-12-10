import java.awt.*;
import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;


public class ClientGui extends JFrame {
    boolean dark = false;
    private JPanel messagePanel;
    private ImageIcon[] pfps = new ImageIcon[6];
    private Map<String, Integer> userPfpMap = new HashMap<>();
    private Client client;

    public ClientGui(String host, int port) {
        super("ClientGui");

        pfps[0] = loadAndScaleIcon("cats/cat1.png", 24);
        pfps[1] = loadAndScaleIcon("cats/cat2.png", 24);
        pfps[2] = loadAndScaleIcon("cats/cat3.png", 24);
        pfps[3] = loadAndScaleIcon("cats/cat4.png", 24);
        pfps[4] = loadAndScaleIcon("cats/cat5.png", 24);
        pfps[5] = loadAndScaleIcon("cats/cat6.png", 24);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 246, 250));
        main.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3, true));
        setContentPane(main);

        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(370, 50));
        header.setBackground(new Color(52, 73, 94));

        JLabel title = new JLabel("Chatroom");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        leftHeader.setOpaque(false);
        leftHeader.add(title);

        JButton pfpButton = new JButton("Pfps");
        pfpButton.setForeground(Color.WHITE);
        pfpButton.setBackground(header.getBackground());
        pfpButton.setHorizontalAlignment(SwingConstants.RIGHT);
        pfpButton.setContentAreaFilled(true);
        pfpButton.setOpaque(true);
        pfpButton.setBorderPainted(false);
        pfpButton.setFocusPainted(false);

        pfpButton.addActionListener(e -> {
            Object[] options = new Object[pfps.length];
            for (int i = 0; i < pfps.length; i++) {
                options[i] = pfps[i];
            }

            JOptionPane.showOptionDialog(
                    this,
                    "Choose a uglee cat pfp",
                    "pfps",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );
        });

        header.add(leftHeader, BorderLayout.WEST);
        header.add(pfpButton, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);

        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(new Color(245, 246, 250));

        JScrollPane scroll = new JScrollPane(messagePanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        main.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottom.setBackground(main.getBackground());

        JTextField input = new JTextField();
        bottom.add(input);
        bottom.add(Box.createVerticalStrut(16));

        Color pastelPink = new Color(255, 182, 193);

        JPanel buttons = new JPanel(new GridLayout(1, 5, 8, 0));
        JButton sendButton = new JButton("Send");
        JButton readButton = new JButton("Read");
        JButton nameButton = new JButton("Name");
        JButton exitButton = new JButton("Exit");
        JButton darkModeButton = new JButton("Dark Mode");

        sendButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));
        readButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));
        nameButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));
        exitButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));
        darkModeButton.setBorder(BorderFactory.createLineBorder(pastelPink, 2, true));

        buttons.add(sendButton);
        buttons.add(readButton);
        buttons.add(nameButton);
        buttons.add(exitButton);
        buttons.add(darkModeButton);
        bottom.add(buttons);
        main.add(bottom, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty() && client != null) {
                client.send("MSG", text);
                input.setText("");
            }
        });

        input.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty() && client != null) {
                client.send("MSG", text);
                input.setText("");
            }
        });

        readButton.addActionListener(e -> {
            if (client != null) {
                client.send("READ", "");
            }
        });

        nameButton.addActionListener(e -> {
            if (client != null) {
                String newName = JOptionPane.showInputDialog(this, "Enter new name:", "Change Name", JOptionPane.PLAIN_MESSAGE);
                if (newName != null && !newName.trim().isEmpty()) {
                    client.send("NAME", newName.trim());
                }
            }
        });

        exitButton.addActionListener(e -> {
            if (client != null) {
                client.send("QUIT", "");
            }
            dispose();
        });

        darkModeButton.addActionListener(e -> {
            if (dark == false) {
                main.setBackground(Color.DARK_GRAY);
                header.setBackground(Color.DARK_GRAY);
                messagePanel.setBackground(Color.GRAY);
                input.setBackground(Color.GRAY);
                input.setForeground(Color.WHITE);

                sendButton.setForeground(Color.BLACK);
                readButton.setForeground(Color.BLACK);
                nameButton.setForeground(Color.BLACK);
                exitButton.setForeground(Color.BLACK);
                darkModeButton.setForeground(Color.BLACK);

                pfpButton.setBackground(header.getBackground());
                pfpButton.setForeground(Color.WHITE);

                bottom.setBackground(Color.DARK_GRAY);
                buttons.setBackground(Color.DARK_GRAY);
                scroll.setBackground(Color.GRAY);
                scroll.getViewport().setBackground(Color.GRAY);
                input.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                darkModeButton.setText("Light Mode");
                dark = true;

                updateMessageContainersBackground();
                updateUsernameLabelsColor(Color.WHITE);
            }
            else {
                main.setBackground(new Color(245,246,250));
                header.setBackground(new Color(52,73,94));
                messagePanel.setBackground(new Color(245,246,250));
                input.setBackground(Color.WHITE);
                input.setForeground(Color.BLACK);

                sendButton.setForeground(Color.BLACK);
                readButton.setForeground(Color.BLACK);
                nameButton.setForeground(Color.BLACK);
                exitButton.setForeground(Color.BLACK);
                darkModeButton.setForeground(Color.BLACK);

                pfpButton.setBackground(header.getBackground());
                pfpButton.setForeground(Color.WHITE);

                bottom.setBackground(new Color(245,246,250));
                buttons.setBackground(new Color(245,246,250));
                scroll.setBackground(Color.WHITE);
                scroll.getViewport().setBackground(Color.WHITE);

                input.setBorder(UIManager.getBorder("TextField.border"));

                darkModeButton.setText("Dark Mode");
                dark = false;

                updateMessageContainersBackground();
                updateUsernameLabelsColor(Color.GRAY);
            }
        });

        setSize(370, 670);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public void addLabel(String text) {
        SwingUtilities.invokeLater(() -> {
            String username = null;
            String messageText = text;
            boolean isJoinMessage = text.endsWith(" joined");
            boolean isNameChangeMessage = text.contains(" has changed their name to ");
            boolean isLeaveMessage = text.endsWith(" has left :(");

            if (text.contains(":\t")) {
                int separatorIndex = text.indexOf(":\t");
                username = text.substring(0, separatorIndex);
                messageText = text.substring(separatorIndex + 2);
            }

            messagePanel.add(Box.createVerticalStrut(10));

            JPanel messageContainer = new JPanel();
            messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.X_AXIS));
            messageContainer.setBackground(messagePanel.getBackground());
            messageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (!isJoinMessage && !isNameChangeMessage && !isLeaveMessage) {
                RoundImageLabel profilePic = new RoundImageLabel(30);
                profilePic.setPreferredSize(new Dimension(30, 30));
                profilePic.setMaximumSize(new Dimension(30, 30));
                profilePic.setMinimumSize(new Dimension(30, 30));
                int pfpIndex = getPfpIndexForUser(username);
                if (pfps[pfpIndex] != null) {
                    profilePic.setImage(pfps[pfpIndex].getImage());
                }
                messageContainer.add(profilePic);
                messageContainer.add(Box.createHorizontalStrut(8));
            }

            JPanel textContainer = new JPanel();
            textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
            textContainer.setBackground(messagePanel.getBackground());
            textContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel usernameLabel = new JLabel(username != null ? username : "");
            usernameLabel.setFont(new Font(usernameLabel.getFont().getName(), Font.PLAIN, 10));
            usernameLabel.setForeground(dark ? Color.WHITE : Color.BLACK);
            usernameLabel.setBackground(messagePanel.getBackground());
            usernameLabel.setOpaque(false);
            usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            textContainer.add(usernameLabel);

            RoundedLabel label = new RoundedLabel(messageText, 20);
            label.setBackground(new Color(230, 230, 230));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            textContainer.add(label);

            messageContainer.add(textContainer);
            messagePanel.add(messageContainer);

            messagePanel.revalidate();
            Dimension prefSize = messageContainer.getPreferredSize();
            messageContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefSize.height));
            Dimension textPrefSize = textContainer.getPreferredSize();
            textContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, textPrefSize.height));

            messagePanel.revalidate();
            messagePanel.repaint();

            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messagePanel);
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private void updateMessageContainersBackground() {
        for (Component comp : messagePanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setBackground(messagePanel.getBackground());
                updateContainerBackgrounds(panel);
            }
        }
        messagePanel.revalidate();
        messagePanel.repaint();
    }

    private void updateContainerBackgrounds(JPanel container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setBackground(messagePanel.getBackground());
                updateContainerBackgrounds(panel);
            }
        }
    }

    private void updateUsernameLabelsColor(Color color) {
        for (Component comp : messagePanel.getComponents()) {
            if (comp instanceof JPanel) {
                updateUsernameLabelsInContainer((JPanel) comp, color);
            }
        }
        messagePanel.repaint();
    }

    private void updateUsernameLabelsInContainer(JPanel container, Color color) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel && !(comp instanceof RoundedLabel)) {
                ((JLabel) comp).setForeground(color);
            } else if (comp instanceof JPanel) {
                updateUsernameLabelsInContainer((JPanel) comp, color);
            }
        }
    }

    private int getPfpIndexForUser(String username) {
        if (username == null || username.isEmpty()) {
            return 0;
        }
        if (!userPfpMap.containsKey(username)) {
            int hash = username.hashCode();
            int assignedIndex = Math.abs(hash) % pfps.length;
            userPfpMap.put(username, assignedIndex);
        }
        return userPfpMap.get(username);
    }

    public void updateUserName(String oldName, String newName) {
        if (oldName != null && !oldName.isEmpty() && userPfpMap.containsKey(oldName)) {
            int pfpIndex = userPfpMap.remove(oldName);
            userPfpMap.put(newName, pfpIndex);
        }
    }

    public void appendMessage(String message) {
        addLabel(message);
    }

    public void appendHistoryMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            String username = null;
            String messageText = text;

            if (text.contains(":\t")) {
                int separatorIndex = text.indexOf(":\t");
                username = text.substring(0, separatorIndex);
                messageText = text.substring(separatorIndex + 2);
            }

            messagePanel.add(Box.createVerticalStrut(10));

            JPanel messageContainer = new JPanel();
            messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.X_AXIS));
            messageContainer.setBackground(messagePanel.getBackground());
            messageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            RoundImageLabel profilePic = new RoundImageLabel(30);
            profilePic.setPreferredSize(new Dimension(30, 30));
            profilePic.setMaximumSize(new Dimension(30, 30));
            profilePic.setMinimumSize(new Dimension(30, 30));
            int pfpIndex = getPfpIndexForUser(username);
            if (pfps[pfpIndex] != null) {
                profilePic.setImage(pfps[pfpIndex].getImage());
            }
            messageContainer.add(profilePic);
            messageContainer.add(Box.createHorizontalStrut(8));

            JPanel textContainer = new JPanel();
            textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
            textContainer.setBackground(messagePanel.getBackground());
            textContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel usernameLabel = new JLabel(username != null ? username : "");
            usernameLabel.setFont(new Font(usernameLabel.getFont().getName(), Font.PLAIN, 10));
            usernameLabel.setForeground(dark ? Color.WHITE : Color.BLACK);
            usernameLabel.setBackground(messagePanel.getBackground());
            usernameLabel.setOpaque(false);
            usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            textContainer.add(usernameLabel);

            RoundedLabel label = new RoundedLabel(messageText, 20);
            label.setBackground(new Color(200, 220, 240));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            textContainer.add(label);

            messageContainer.add(textContainer);
            messagePanel.add(messageContainer);

            messagePanel.revalidate();
            Dimension prefSize = messageContainer.getPreferredSize();
            messageContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, prefSize.height));
            Dimension textPrefSize = textContainer.getPreferredSize();
            textContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, textPrefSize.height));

            messagePanel.revalidate();
            messagePanel.repaint();

            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messagePanel);
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    public void setClient(Client client) {
        this.client = client;
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
        SwingUtilities.invokeLater(() -> new ClientGui("localhost", 8080));


    }
}

class RoundImageLabel extends JPanel {
    private Image image;
    private int size;

    public RoundImageLabel(int size) {
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    public void setImage(String imagePath) {
        image = new ImageIcon(imagePath).getImage();
        repaint();
    }

    public void setImage(Image img) {
        image = img;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.fillOval(0, 0, size, size);
            g2.dispose();
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Shape circle = new Ellipse2D.Double(0, 0, size, size);
        g2.setClip(circle);

        g2.drawImage(image, 0, 0, size, size, this);

        g2.dispose();
    }
}