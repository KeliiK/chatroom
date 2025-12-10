import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public class RoundedLabel extends JLabel {

    private int radius;

    public RoundedLabel(String text, int radius) {
        super(text);
        this.radius = radius;

        // We paint the background ourselves → must be non-opaque
        setOpaque(false);

        // Set some default padding via custom empty border
        setBorder(new RoundedEmptyBorder(radius));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background bubble
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        g2.dispose();
        super.paintComponent(g); // draw text on top
    }

    // --- Inner class for the empty-rounded border (just insets) ---
    private static class RoundedEmptyBorder extends AbstractBorder {
        private final int radius;

        public RoundedEmptyBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int pad = radius / 2;
            return new Insets(pad, pad, pad, pad);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int pad = radius / 2;
            insets.top = pad;
            insets.left = pad;
            insets.bottom = pad;
            insets.right = pad;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            // Draw nothing → acts like an empty border
        }
    }
}
