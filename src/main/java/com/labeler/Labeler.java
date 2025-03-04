package src.main.java.com.labeler;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.*;
import java.util.Iterator;
import javax.imageio.ImageIO;

public class Labeler extends JFrame {
    private Map<String, InputOption> inputOptions;

    private static String directoryPath;
    private static String labelPath = "labels.json";
    private static String optionPath = "options.json";

    private JLabel filenameLabel;
    private JLabel counterLabel;

    private ImagePanel imagePanel;

    private List<String> imagePaths;
    private int currentIndex = -1;
    private Map<String, ImageItem> imageItems;
    private ImageItem currentItem;

    private JButton previousButton;
    private JButton nextButton;

    private JPanel controlPanel;

    private Map<String, List<JButton>> buttonGroups = new HashMap<>();

    private boolean shouldPreserveSelections = false;

    private static final String ARIAL = "Arial";

    private final transient Border paddingBorder = BorderFactory.createEmptyBorder(5, 10, 5, 10);
    private final Border defaultBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(), paddingBorder);
    private final Border selectedBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 2), paddingBorder);

    public Labeler() {
        // Set the title of the JFrame
        super("Dataset Labeler");

        // Load options from JSON file
        loadOptions();

        // Select the image directory
        if (directoryPath == null) {
            directoryPath = selectImageDirectory();
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

        // Add panels to the control panel based on the pre-loaded options
        loadControlPanelOptions();

        // Create preserve selections checkbox panel
        createPreserveSelectionsPanel();

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

    private void loadOptions() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(optionPath);
            if (file.exists()) {
                inputOptions = mapper.readValue(file, mapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, InputOption.class));
                System.out.println("Loaded " + inputOptions.size() + " options from " + optionPath + "...");
            } else {
                System.out.println("No options.json file found, exiting...");
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        // Exit if no directory was selected
        System.exit(0);
        return null;
    }

    private void loadControlPanelOptions() {
        for (Map.Entry<String, InputOption> entry : inputOptions.entrySet()) {
            InputOption option = entry.getValue();
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel label = new JLabel(option.getDescription());
            label.setFont(new Font(ARIAL, Font.PLAIN, 20));
            panel.add(label);

            buildControlPanelOption(entry.getKey(), option, panel);

            controlPanel.add(panel);
        }
    }

    private void buildControlPanelOption(String key, InputOption option, JPanel panel) {
        switch (option.getType()) {
            case "boolean":
                buildBooleanOption(key, panel);
                break;
            case "select-one":
                buildSelectOneOption(key, option, panel);
                break;
            case "select-many":
                buildSelectManyOption(key, option, panel);
                break;
            default:
                System.out.println("Invalid option type: " + option.getType());
                System.exit(0);
                break;
        }
    }

    private void buildBooleanOption(String key, JPanel panel) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        String[] statuses = {"Yes", "No"};
        List<JButton> buttons = new ArrayList<>();
        for (String status : statuses) {
            JButton button = buildButton(key, status);
            button.addActionListener(e -> {
                for (JButton b : buttons) {
                    b.setBorder(b == button ? selectedBorder : defaultBorder);
                }
                setOption(key, button.getText().equalsIgnoreCase("yes"));
            });
            buttons.add(button);
            buttonPanel.add(button);
        }

        buttonGroups.put(key, buttons);
        panel.add(buttonPanel);
    }

    private void buildSelectOneOption(String key, InputOption option, JPanel panel) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        List<JButton> buttons = new ArrayList<>();
        for (String value : option.getOptions()) {
            JButton button = buildButton(key, value);
            button.addActionListener(e -> {
                for (JButton b : buttons) {
                    b.setBorder(b == button ? selectedBorder : defaultBorder);
                }
                setOption(key, button.getText());
            });
            buttons.add(button);
            buttonPanel.add(button);
        }

        buttonGroups.put(key, buttons);
        panel.add(buttonPanel);
    }

    private void buildSelectManyOption(String key, InputOption option, JPanel panel) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        List<JButton> buttons = new ArrayList<>();
        for (String value : option.getOptions()) {
            JButton button = buildButton(key, value);
            button.addActionListener(e -> {
                boolean isRemoval = button.getBorder().equals(selectedBorder);
                if (isRemoval) {
                    button.setBorder(defaultBorder);
                    removeOption(key, button.getText());
                } else {
                    button.setBorder(selectedBorder);
                    setOption(key, button.getText());
                }
            });
            buttons.add(button);
            buttonPanel.add(button);
        }

        buttonGroups.put(key, buttons);
        panel.add(buttonPanel);
    }

    private JButton buildButton(String key, String value) {
        JButton button = new JButton(value);
        button.setFont(new Font(ARIAL, Font.BOLD, 20));
        button.setBorder(defaultBorder);

        Boolean shouldDisable = isButtonDisabled(key);
        button.setOpaque(!shouldDisable);
        button.setEnabled(!shouldDisable);

        return button;
    }

    private boolean isButtonDisabled(String key) {
        InputOption option = inputOptions.get(key);
        if (option.getDisabled() != null) {
            if (currentItem == null) {
                return true;
            }
            for (Map.Entry<String, Object> disabled : option.getDisabled().entrySet()) {
                Object value = currentItem.getOption(disabled.getKey());
                if (value == null || value.equals(disabled.getValue().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setOption(String key, Object value) {
        if (currentItem != null) {
            currentItem.setOption(key, value.toString());
            nextButton.setEnabled(currentItem.isComplete());
            for (Map.Entry<String, InputOption> entry : inputOptions.entrySet()) {
                if (!entry.getKey().equals(key) && entry.getValue().getDisabled() != null) {
                    handleDisabledOption(entry, key, value);
                }
            }
        }
    }

    private void handleDisabledOption(Map.Entry<String, InputOption> entry, String key, Object value) {
        for (Map.Entry<String, Object> disabled : entry.getValue().getDisabled().entrySet()) {
            if (disabled.getKey().equals(key)) {
                updateDisabledButtonGroup(disabled, value, entry);
            }
        }
    }

    private void updateDisabledButtonGroup(Map.Entry<String, Object> disabled, Object value, Map.Entry<String, InputOption> entry) {
        buttonGroups.get(entry.getKey()).forEach(button -> {
            Boolean condition = value.toString().equals(disabled.getValue().toString());
            button.setOpaque(!condition);
            button.setEnabled(!condition);
            if (condition) {
                button.setBorder(defaultBorder);
                removeOption(entry.getKey(), button.getText());
            }
        });
    }

    private void removeOption(String key, String value) {
        if (currentItem != null) {
            currentItem.removeOption(key, value);
            nextButton.setEnabled(currentItem.isComplete());
        }
    }

    private void createPreserveSelectionsPanel() {
        JPanel preserveSelectionsPanel = new JPanel();

        JLabel checkboxLabel = new JLabel("Preserve selections");
        checkboxLabel.setFont(new Font(ARIAL, Font.PLAIN, 20));
        preserveSelectionsPanel.add(checkboxLabel);

        JCheckBox checkbox = new JCheckBox();
        checkbox.addItemListener(e -> shouldPreserveSelections = e.getStateChange() == ItemEvent.SELECTED);
        preserveSelectionsPanel.add(checkbox);

        controlPanel.add(preserveSelectionsPanel);
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
                // Filter image files
                if (
                    file.isFile() &&
                    (
                        file.getName().endsWith(".jpg") ||
                        file.getName().endsWith(".png") ||
                        file.getName().endsWith(".jpeg")
                    ) &&
                    !imageItems.containsKey(file.getName())
                ) {
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
            boolean isNew = false;
            if (imageItems.containsKey(file.getName())) {
                currentItem = imageItems.get(file.getName());
            } else {
                currentItem = new ImageItem(file.getName(), inputOptions);
                isNew = true;
            }

            // Disable the next button until all labels are set
            nextButton.setEnabled(currentItem.isComplete());
            previousButton.setEnabled(currentIndex > 0);
            controlPanel.setVisible(true);

            // Set focus to another component to prevent other buttons from being focused
            SwingUtilities.invokeLater(() -> nextButton.requestFocusInWindow());

            // Update the buttons based on the current item
            updateButtons(isNew);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateButtons(boolean isNew) {
        // Optionally preserve the selections from the previous image
        // Only do this if the current item has not already been labeled
        if (shouldPreserveSelections && isNew) {
            for (Map.Entry<String, List<JButton>> buttonGroup : buttonGroups.entrySet()) {
                for (JButton button : buttonGroup.getValue()) {
                    updateIndividualButton(button, buttonGroup);
                }
            }
            return;
        }

        // If we aren't preserving the selections, we must set the button borders
        for (Map.Entry<String, List<JButton>> buttonGroup : buttonGroups.entrySet()) {
            for (JButton button : buttonGroup.getValue()) {
                String type = inputOptions.get(buttonGroup.getKey()).getType();
                Object value = currentItem.getOption(buttonGroup.getKey());
                boolean condition = isButtonSelected(type, value, button.getText());
                button.setBorder(condition ? selectedBorder : defaultBorder);
            }
        }
    }

    private void updateIndividualButton(JButton button, Map.Entry<String, List<JButton>> buttonGroup) {
        if (button.getBorder().equals(selectedBorder)) {
            // We need to handle select-many separately
            InputOption option = inputOptions.get(buttonGroup.getKey());
            if (option.getType().equals("select-many")) {
                currentItem.setOption(buttonGroup.getKey(), button.getText());
            } else {
                button.doClick();
            }
        }
    }

    private boolean isButtonSelected(String type, Object value, String buttonText) {
        if (value == null) {
            return false;
        } else if (type.equals("boolean")) {
            Boolean boolValue = Boolean.parseBoolean(value.toString());
            return (boolValue && buttonText.equalsIgnoreCase("yes")) || (!boolValue && buttonText.equalsIgnoreCase("no"));
        } else if (type.equals("select-one")) {
            return value.equals(buttonText);
        } else {
            return ((List<String>) value).contains(buttonText);
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

    private void loadLabels() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(labelPath);
            imageItems = new HashMap<>();

            if (file.exists()) {
                JsonNode rootNode = mapper.readTree(file);
                if (rootNode.isObject()) {
                    System.out.println("Loaded " + rootNode.size() + " items from " +  labelPath + "...");
                    decodeJSON(rootNode);
                } else {
                    System.out.println("Invalid JSON structure in " + labelPath + ".");
                }
            } else {
                System.out.println("No labels found, creating " + labelPath + "...");
                mapper.writeValue(file, new ArrayList<>());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decodeJSON(JsonNode rootNode) {
        // Iterate through raw JSON key-value pairs
        for (Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            ImageItem item = new ImageItem(key, inputOptions);
            
            for (Iterator<Map.Entry<String, JsonNode>> fields = value.fields(); fields.hasNext(); ) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();

                if (fieldValue.isArray()) {
                    for (JsonNode node : fieldValue) {
                        item.setOption(fieldName, node.asText());
                    }
                } else if (fieldValue.isBoolean()) {
                    // If the value is a boolean, store it as Boolean
                    item.setOption(fieldName, fieldValue.asBoolean());
                } else {
                    // Otherwise, store it as a string
                    item.setOption(fieldName, fieldValue.asText());
                }
            }

            imageItems.put(key, item);
        }
    }

    private void onExit() {
        updateImageItems();
        System.out.println("Saving " + imageItems.size() + " labels to " + labelPath + "...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();

            encodeJSON(mapper, root);

            File file = new File(labelPath);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void encodeJSON(ObjectMapper mapper, ObjectNode root) {
        for (Map.Entry<String, ImageItem> entry : imageItems.entrySet()) {
            String imageName = entry.getKey();
            ImageItem imageItem = entry.getValue();

            // Create JSON structure for the current image
            ObjectNode imageNode = mapper.createObjectNode();

            for (Map.Entry<String, InputOptionValue> labelEntry : imageItem.getOptions().entrySet()) {
                String key = labelEntry.getKey();
                InputOptionValue value = labelEntry.getValue();
                Object inputValue = value.getValue();

                if (inputValue == null) {
                    continue;
                }

                switch (value.getType()) {
                    case "boolean":
                        imageNode.put(key, Boolean.parseBoolean(inputValue.toString())); // Convert string to boolean
                        break;
                    case "select-one":
                        imageNode.put(key, inputValue.toString()); // Store as a string
                        break;
                    case "select-many":
                        ArrayNode arrayNode = mapper.createArrayNode();
                        for (String option : (ArrayList<String>)inputValue) {
                            arrayNode.add(option);
                        }
                        imageNode.set(key, arrayNode); // Store as an array
                        break;
                }
            }

            // Add the imageNode to the root JSON with the image filename as the key
            root.set(imageName, imageNode);
        }
    }

    public void updateImageItems() {
        if (currentItem != null && currentItem.isComplete()) {
            imageItems.put(currentItem.getFilename(), currentItem);
        }
    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-i") && i + 1 < args.length) {
                parseImageArg(args[i + 1]);
            } else if (args[i].equalsIgnoreCase("-l") && i + 1 < args.length) {
                parseLabelArg(args[i + 1]);
            } else if (args[i].equalsIgnoreCase("-o") && i + 1 < args.length) {
                parseOptionsArg(args[i + 1]);
            }
        }
    }

    private static void parseImageArg(String arg) {
        File directory = new File(arg);
        if (directory.exists() && directory.isDirectory()) {
            directoryPath = arg;
            System.out.println("Image directory: " + directoryPath);
        }
    }

    private static void parseLabelArg(String arg) {
        File file = new File(arg);
        if (file.exists() && file.isFile() && arg.endsWith(".json")) {
            labelPath = arg;
        }
    }

    private static void parseOptionsArg(String arg) {
        File file = new File(arg);
        if (file.exists() && file.isFile() && arg.endsWith(".json")) {
            optionPath = arg;
        }
    }

    public static void main(String[] args) {
        parseArgs(args);

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(Labeler::new);
    }
}
