package src.main.java.com.labeler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class ImagePanel extends JPanel {
    private transient BufferedImage image;
    private String text;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            // Get the parent container dimensions (JPanel in this case)
            int containerWidth = getWidth();
            int containerHeight = getHeight();

            // Get the image dimensions
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            // Calculate the scaling factor while maintaining the aspect ratio
            double imageAspect = (double) imageWidth / imageHeight;
            double containerAspect = (double) containerWidth / containerHeight;

            int newWidth;
            int newHeight;

            if (containerAspect < imageAspect) {
                // Scale to fit the width of the container
                newWidth = containerWidth;
                newHeight = (int) (containerWidth / imageAspect);
            } else {
                // Scale to fit the height of the container
                newHeight = containerHeight;
                newWidth = (int) (containerHeight * imageAspect);
            }

            // Center the image in the panel
            int x = (containerWidth - newWidth) / 2;
            int y = (containerHeight - newHeight) / 2;

            // Draw the scaled image
            g.drawImage(image, x, y, newWidth, newHeight, this);
        } else if (text != null) {
            // Set text font and style
            g.setFont(new Font("Arial", Font.BOLD, 40));

            // Set text color
            g.setColor(Color.BLACK);

            // Calculate text width and height
            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            int textHeight = metrics.getHeight();

            // Center the text in the panel
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() - textHeight) / 2 + metrics.getAscent();

            // Draw the text
            g.drawString(text, x, y);
        }
    }
}
