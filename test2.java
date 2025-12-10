import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class test2 extends JFrame {

    public test2() {
        setTitle("Round Image Demo");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new RoundImagePanel("./cats/cat1.png")); // your image
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new test2().setVisible(true));
    }
}

// Panel that paints a circular image
class RoundImagePanel extends JPanel {
    private Image image;

    public RoundImagePanel(String path) {
        image = new ImageIcon(path).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int size = Math.min(getWidth(), getHeight()); // keep it square

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // create a circular clip
        Shape circle = new Ellipse2D.Double(0, 0, size, size);
        g2.setClip(circle);

        // draw the image inside the circular clip
        g2.drawImage(image, 0, 0, size, size, this);

        g2.dispose();
    }
}
