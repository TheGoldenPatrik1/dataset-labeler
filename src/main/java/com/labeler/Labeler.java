package src.main.java.com.labeler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Labeler extends JFrame {
    private JLabel imageLabel;
    private JLabel filenameLabel;
    private JLabel counterLabel;
    private List<String> imagePaths;
    private int currentIndex = -1;
    private JButton nextButton;
    private Map<String, ImageItem> imageItems;
    private CurrentItem currentItem;
    private JPanel controlPanel;
    private JLabel questionLabel;
    private JButton floodingButton;
    private JButton noFloodingButton;

    public Labeler(String directoryPath) {
        // Initialize the JFrame
        super("Dataset Labeler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Create a JPanel to hold the image on the left side and the button on the right
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create a JLabel for displaying the filename (on top of the image)
        filenameLabel = new JLabel("", SwingConstants.CENTER);
        filenameLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        mainPanel.add(filenameLabel, BorderLayout.NORTH);

        // Create a JLabel for displaying the counter (on the bottom)
        counterLabel = new JLabel("", SwingConstants.CENTER);
        counterLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        mainPanel.add(counterLabel, BorderLayout.SOUTH);

        // Create a JLabel to display images
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.LEFT);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        mainPanel.add(imageLabel, BorderLayout.CENTER);

        // Create a JPanel to hold the control buttons
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        // Question label asking about flood status
        questionLabel = new JLabel("Does this image contain flooding?");
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        controlPanel.add(questionLabel);

        // Create buttons for flood status selection
        JPanel floodingButtonPanel = new JPanel();

        floodingButton = new JButton("Yes");
        floodingButton.setFont(new Font("Arial", Font.BOLD, 20));
        floodingButton.addActionListener(e -> setFloodingLabel(true));
        floodingButtonPanel.add(floodingButton);

        noFloodingButton = new JButton("No");
        noFloodingButton.setFont(new Font("Arial", Font.BOLD, 20));
        noFloodingButton.addActionListener(e -> setFloodingLabel(false));
        floodingButtonPanel.add(noFloodingButton);

        controlPanel.add(floodingButtonPanel);

        // Create a JButton to go to the next image (on the right side)
        nextButton = new JButton("Next Image");
        nextButton.setPreferredSize(new Dimension(200, 100));
        nextButton.setFont(new Font("Arial", Font.BOLD, 20));
        controlPanel.add(nextButton, BorderLayout.EAST);
        nextButton.addActionListener(e -> nextImage());
        nextButton.setEnabled(false);

        // Set focus to another component to prevent floodingButton or noFloodingButton from being focused
        SwingUtilities.invokeLater(() -> nextButton.requestFocusInWindow());

        // Add the control panel to the main panel
        mainPanel.add(controlPanel, BorderLayout.EAST);

        // Add the main panel to the frame
        add(mainPanel);

        // Load images from the given directory
        imagePaths = loadImagesFromDirectory(directoryPath);

        // Load labels from the JSON file
        loadLabels();

        // Display the first image
        if (!imagePaths.isEmpty()) {
            nextImage();
        } else {
            displayMessage("No images available in the directory.");
        }
        
        // Make the frame visible
        setVisible(true);

        // Add shutdown hook to save JSON data before exiting
        Runtime.getRuntime().addShutdownHook(new Thread(this::onExit));
    }

    private List<String> loadImagesFromDirectory(String directoryPath) {
        List<String> paths = new ArrayList<>();
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                // Filter image files (you can extend this list of formats)
                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".jpeg"))) {
                    paths.add(file.getAbsolutePath());
                }
            }
        }

        return paths;
    }

    private void displayImage(String imagePath) {
        try {
            // Update the filename label with the image's filename
            File file = new File(imagePath);
            filenameLabel.setText(file.getName());

            // Load and scale the image
            BufferedImage img = ImageIO.read(new File(imagePath));
            ImageIcon icon = new ImageIcon(img);
            imageLabel.setIcon(icon);

            // Clear the text and enable the button
            imageLabel.setText("");

            // Update the counter label
            counterLabel.setText((currentIndex + 1) + " / " + imagePaths.size());

            // Update current item
            currentItem = new CurrentItem(file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextImage() {
        if (currentItem != null && !imageItems.containsKey(currentItem.getFilename()) && currentItem.isComplete()) {
            imageItems.put(currentItem.getFilename(), currentItem.toImageItem());
        }
        currentItem = null;

        if (imagePaths.isEmpty()) {
            displayMessage("No images available to label. Please check the directory.");
        } else if (currentIndex + 1 < imagePaths.size()) {
            currentIndex++;
            String imagePath = imagePaths.get(currentIndex);
            File file = new File(imagePath);
            if (imageItems.containsKey(file.getName())) {
                nextImage();
                return;
            }
            displayImage(imagePaths.get(currentIndex));
        } else {
            displayMessage("No more images left to label. You've reached the end.");
        }
    }

    private void displayMessage(String message) {
        imageLabel.setIcon(null);
        imageLabel.setText(message);
        imageLabel.setFont(new Font("Arial", Font.BOLD, 30));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        filenameLabel.setText("");
        controlPanel.setVisible(false);
    }

    private void setFloodingLabel(boolean hasFlooding) {
        if (currentItem != null) {
            currentItem.setHasFlooding(hasFlooding);
            if (currentItem.isComplete()) {
                nextButton.setEnabled(true);
            }
        }
    }

    private void loadLabels() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("labels.json");
            if (file.exists()) {
                imageItems = mapper.readValue(file, mapper.getTypeFactory().constructMapType(HashMap.class, String.class, ImageItem.class));
                System.out.println("Loaded " + imageItems.size() + " from labels.json...");
            } else {
                imageItems = new HashMap<>();
                System.out.println("No labels found, creating labels.json...");
                mapper.writeValue(file, new ArrayList<>());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onExit() {
        if (currentItem != null && !imageItems.containsKey(currentItem.getFilename()) && currentItem.isComplete()) {
            imageItems.put(currentItem.getFilename(), currentItem.toImageItem());
        }
        System.out.println("Adding " + imageItems.size() + " labels to labels.json...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("labels.json");
            mapper.writeValue(file, imageItems);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        String directoryPath = "./images";
        SwingUtilities.invokeLater(() -> new Labeler(directoryPath));
    }
}
