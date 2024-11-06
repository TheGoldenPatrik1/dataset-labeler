package src.main.java.com.labeler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.*;
import javax.imageio.ImageIO;

public class Labeler extends JFrame {
    private String directoryPath;

    private JLabel filenameLabel;
    private JLabel counterLabel;

    private ImagePanel imagePanel;

    private List<String> imagePaths;
    private int currentIndex = -1;
    private Map<String, ImageItem> imageItems;
    private CurrentItem currentItem;

    private JButton previousButton;
    private JButton nextButton;

    private JPanel controlPanel;

    private List<List<JButton>> buttonGroups = new ArrayList<>();

    private static final String ARIAL = "Arial";

    private final transient Border paddingBorder = BorderFactory.createEmptyBorder(5, 10, 5, 10);
    private final Border defaultBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(), paddingBorder);
    private final Border selectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 2), paddingBorder);

    public Labeler() {
        // Set the title of the JFrame
        super("Dataset Labeler");

        // Select the image directory
        directoryPath = selectImageDirectory();

        // Exit if no directory is selected
        if (directoryPath == null) {
            System.exit(0);
        }

        // Initialize the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Create a JPanel to hold the image on the left side and the button on the right
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create a JLabel for displaying the filename (on top of the image)
        filenameLabel = new JLabel("", SwingConstants.CENTER);
        filenameLabel.setFont(new Font(ARIAL, Font.PLAIN, 24));
        mainPanel.add(filenameLabel, BorderLayout.NORTH);

        // Create a JLabel for displaying the counter (on the bottom)
        counterLabel = new JLabel("", SwingConstants.CENTER);
        counterLabel.setFont(new Font(ARIAL, Font.PLAIN, 24));
        mainPanel.add(counterLabel, BorderLayout.SOUTH);

        // Create a JPanel to display images
        imagePanel = new ImagePanel();
        mainPanel.add(imagePanel, BorderLayout.CENTER);

        // Create a JPanel to hold the control buttons
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(400, controlPanel.getPreferredSize().height));

        // Create buttons for urban status
        controlPanel.add(createUrbanPanel());

        // Question label asking about flood status
        JLabel questionLabel = new JLabel("Is there flooding?");
        questionLabel.setFont(new Font(ARIAL, Font.PLAIN, 20));
        controlPanel.add(questionLabel);

        // Create buttons for flood status, water depth, and object labels
        controlPanel.add(createFloodingButtonPanel());
        controlPanel.add(createFloodDepthPanel());
        controlPanel.add(createObjectLabelPanel());

        // Create navigation panel
        createNavigationPanel();

        // Add the control panel to the main panel
        mainPanel.add(controlPanel, BorderLayout.EAST);

        // Add the main panel to the frame
        add(mainPanel);

        // Load labels from the JSON file
        loadLabels();

        // Load images from the selected directory
        loadImages();

        // Display the first image
        nextImage();
        
        // Make the frame visible
        setVisible(true);

        // Add shutdown hook to save JSON data before exiting
        Runtime.getRuntime().addShutdownHook(new Thread(this::onExit));
    }

    private String selectImageDirectory() {
        // Create a JFileChooser for directory selection
        JFileChooser directoryChooser = new JFileChooser();
        File workingDirectory = new File(System.getProperty("user.dir"));
        directoryChooser.setCurrentDirectory(workingDirectory);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Show the dialog
        int returnValue = directoryChooser.showOpenDialog(this);

        // Return the selected directory path
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = directoryChooser.getSelectedFile();
            return selectedDirectory.getAbsolutePath();
        }

        // Return null if no directory was selected
        return null;
    }

    private JPanel createUrbanPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel urbanLabel = new JLabel("Is the scene urban?");
        urbanLabel.setFont(new Font(ARIAL, Font.PLAIN, 20));
        panel.add(urbanLabel);

        JPanel urbanButtonPanel = new JPanel();

        String[] urbanStatuses = {"Yes", "No"};
        List<JButton> urbanButtons = new ArrayList<>();
        for (String status : urbanStatuses) {
            JButton button = new JButton(status);
            button.setFont(new Font(ARIAL, Font.BOLD, 20));
            button.setBorder(defaultBorder);
            urbanButtons.add(button);
        }

        for (JButton button : urbanButtons) {
            button.addActionListener(e -> {
                for (JButton b : urbanButtons) {
                    b.setBorder(b == button ? selectedBorder : defaultBorder);
                }
                setUrbanLabel(button.getText().equalsIgnoreCase("yes"));
            });
            urbanButtonPanel.add(button, BorderLayout.CENTER);
        }

        buttonGroups.add(urbanButtons);
        panel.add(urbanButtonPanel);

        return panel;
    }

    private JPanel createFloodingButtonPanel() {
        JPanel floodingButtonPanel = new JPanel();

        String[] floodingStatuses = {"Yes", "No"};
        List<JButton> floodingButtons = new ArrayList<>();
        for (String status : floodingStatuses) {
            JButton button = new JButton(status);
            button.setFont(new Font(ARIAL, Font.BOLD, 20));
            button.setBorder(defaultBorder);
            button.addActionListener(e -> {
                for (JButton b : floodingButtons) {
                    b.setBorder(b == button ? selectedBorder : defaultBorder);
                }
                setFloodingLabel(button.getText().equalsIgnoreCase("yes"));
            });
            floodingButtons.add(button);
            floodingButtonPanel.add(button, BorderLayout.CENTER);
        }

        buttonGroups.add(floodingButtons);

        return floodingButtonPanel;
    }

    private JPanel createFloodDepthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel floodDepthLabel = new JLabel("Estimate the flood depth:");
        floodDepthLabel.setFont(new Font(ARIAL, Font.PLAIN, 20));
        panel.add(floodDepthLabel);

        JPanel floodDepthButtonPanel = new JPanel();

        String[] floodDepths = {"Low", "Medium", "High"};
        List<JButton> floodDepthButtons = new ArrayList<>();
        for (String floodDepth : floodDepths) {
            JButton floodDepthButton = new JButton(floodDepth);
            floodDepthButton.setFont(new Font(ARIAL, Font.BOLD, 20));
            floodDepthButton.setBorder(defaultBorder);
            floodDepthButton.setOpaque(false);
            floodDepthButton.setEnabled(false);
            floodDepthButton.addActionListener(e -> {
                if (currentItem.getFloodDepth() != null && currentItem.getFloodDepth().equals(floodDepthButton.getText().toLowerCase())) {
                    floodDepthButton.setBorder(defaultBorder);
                    setFloodDepthLabel(null);
                } else {
                    for (JButton button : floodDepthButtons) {
                        button.setBorder(button == floodDepthButton ? selectedBorder : defaultBorder);
                    }
                    setFloodDepthLabel(floodDepthButton.getText().toLowerCase());
                }
            });
            floodDepthButtons.add(floodDepthButton);
            floodDepthButtonPanel.add(floodDepthButton);
        }

        buttonGroups.add(floodDepthButtons);
        panel.add(floodDepthButtonPanel);

        return panel;
    }

    private JPanel createObjectLabelPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel objectLabel = new JLabel("Select objects:");
        objectLabel.setFont(new Font(ARIAL, Font.PLAIN, 20));
        panel.add(objectLabel);

        JPanel objectButtonPanel = new JPanel();

        String[] objectLabels = {"Buildings", "People", "Street", "Vegetation", "Vehicles"};
        List<JButton> objectButtons = new ArrayList<>();
        for (String object : objectLabels) {
            JButton objectButton = new JButton(object);
            objectButton.setFont(new Font(ARIAL, Font.BOLD, 20));
            objectButton.setBorder(defaultBorder);
            objectButton.addActionListener(e -> {
                boolean isRemoval = objectButton.getBorder().equals(selectedBorder);
                if (isRemoval) {
                    objectButton.setBorder(defaultBorder);
                    currentItem.removeObject(objectButton.getText());
                } else {
                    objectButton.setBorder(selectedBorder);
                    currentItem.addObject(objectButton.getText());
                }
            });
            objectButtons.add(objectButton);
            objectButtonPanel.add(objectButton);
        }

        panel.add(objectButtonPanel);
        buttonGroups.add(objectButtons);
        
        return panel;
    }

    private void createNavigationPanel() {
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        previousButton = new JButton("Previous");
        previousButton.setFont(new Font(ARIAL, Font.BOLD, 20));
        previousButton.addActionListener(e -> previousImage());
        previousButton.setEnabled(false);
        navigationPanel.add(previousButton);

        nextButton = new JButton("Next");
        nextButton.setFont(new Font(ARIAL, Font.BOLD, 20));
        nextButton.addActionListener(e -> nextImage());
        nextButton.setEnabled(false);
        navigationPanel.add(nextButton);

        add(navigationPanel, BorderLayout.SOUTH);
    }

    private void loadImages() {
        List<String> paths = new ArrayList<>();
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                // Filter image files (you can extend this list of formats)
                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".jpeg")) && !imageItems.containsKey(file.getName())) {
                    paths.add(file.getAbsolutePath());
                }
            }
        }

        imagePaths = paths;
    }

    private void displayImage(String imagePath) {
        try {
            // Update the filename label with the image's filename
            File file = new File(imagePath);
            filenameLabel.setText(file.getName());
            
            // Set the filename label
            filenameLabel.setText(new File(imagePath).getName());
            
            // Repaint the panel to display the new image
            BufferedImage image = ImageIO.read(file);
            imagePanel.setText(null);
            imagePanel.setImage(image);
            imagePanel.repaint();

            // Update the counter label
            counterLabel.setText((currentIndex + 1) + " / " + imagePaths.size());

            // Update current item
            if (imageItems.containsKey(file.getName())) {
                currentItem = imageItems.get(file.getName()).toCurrentItem(file.getName());
                setFloodingLabel(currentItem.getHasFlooding());
            } else {
                currentItem = new CurrentItem(file.getName());
            }

            // Disable the next button until all labels are set
            nextButton.setEnabled(currentItem.isComplete());
            previousButton.setEnabled(currentIndex > 0);
            controlPanel.setVisible(true);

            // Set focus to another component to prevent other buttons from being focused
            SwingUtilities.invokeLater(() -> nextButton.requestFocusInWindow());

            // Set the button borders
            int index = 0;
            for (List<JButton> buttonGroup : buttonGroups) {
                for (JButton button : buttonGroup) {
                    boolean condition;
                    if (index == 0) {
                        condition = (currentItem.getIsUrban() && button.getText().equalsIgnoreCase("yes")) || (!currentItem.getIsUrban() && currentItem.isComplete() && button.getText().equalsIgnoreCase("no"));
                    } else if (index == 1) {
                        condition = (currentItem.getHasFlooding() && button.getText().equalsIgnoreCase("yes")) || (!currentItem.getHasFlooding() && currentItem.isComplete() && button.getText().equalsIgnoreCase("no"));
                    } else if (index == 2) {
                        condition = currentItem.getFloodDepth() != null && currentItem.getFloodDepth().equalsIgnoreCase(button.getText());
                    } else {
                        condition = currentItem.hasObject(button.getText());
                    }
                    button.setBorder(condition ? selectedBorder : defaultBorder);
                }
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextImage() {
        updateImageItems();
        currentItem = null;
        currentIndex++;

        if (imagePaths.isEmpty()) {
            displayMessage("No images available to label. Please check the folder you selected.");
        } else if (currentIndex < imagePaths.size()) {
            displayImage(imagePaths.get(currentIndex));
        } else {
            displayMessage("No more images left to label. You may exit the program.");
        }
    }

    private void previousImage() {
        updateImageItems();

        if (currentIndex > 0) {
            currentIndex--;
            displayImage(imagePaths.get(currentIndex));
        }
    }

    private void displayMessage(String message) {
        imagePanel.setImage(null);
        imagePanel.setText(message);
        filenameLabel.setText("");
        controlPanel.setVisible(false);
        nextButton.setEnabled(false);
        previousButton.setEnabled(currentIndex > 0);
    }

    private void setUrbanLabel(boolean isUrban) {
        if (currentItem != null) {
            currentItem.setIsUrban(isUrban);
            nextButton.setEnabled(currentItem.isComplete());
        }
    }

    private void setFloodingLabel(boolean hasFlooding) {
        if (currentItem != null) {
            currentItem.setHasFlooding(hasFlooding);
            for (JButton button : buttonGroups.get(2)) {
                button.setOpaque(hasFlooding);
                button.setEnabled(hasFlooding);
                if (!hasFlooding) {
                    button.setBorder(defaultBorder);
                    currentItem.setFloodDepth(null);
                }
            }
            nextButton.setEnabled(currentItem.isComplete());
        }
    }

    private void setFloodDepthLabel(String floodDepth) {
        if (currentItem != null) {
            currentItem.setFloodDepth(floodDepth);
            nextButton.setEnabled(currentItem.isComplete());
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
        updateImageItems();
        System.out.println("Saving " + imageItems.size() + " labels to labels.json...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("labels.json");
            mapper.writeValue(file, imageItems);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateImageItems() {
        if (currentItem != null && currentItem.isComplete()) {
            imageItems.put(currentItem.getFilename(), currentItem.toImageItem());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(Labeler::new);
    }
}
