package src.main.java.com.labeler;

import java.util.Map;
import java.util.HashMap;

class ImageItem {
    private String filename;
    private Map<String, InputOptionValue> options = new HashMap<>();

    public ImageItem() {
    }

    public ImageItem(String filename, Map<String, InputOption> options) {
        this.filename = filename;
        for (Map.Entry<String, InputOption> entry : options.entrySet()) {
            InputOptionValue optionValue = new InputOptionValue();
            optionValue.setDescription(entry.getValue().getDescription());
            optionValue.setType(entry.getValue().getType());
            optionValue.setRequired(entry.getValue().getRequired());
            optionValue.setOptions(entry.getValue().getOptions());
            optionValue.setDisabled(entry.getValue().getDisabled());
            this.options.put(entry.getKey(), optionValue);
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setOption(String key, Object value) {
        options.get(key).setValue(value);
    }

    public void removeOption(String key, String value) {
        options.get(key).removeValue(value);
    }

    public Object getOption(String key) {
        return options.get(key).getValue();
    }

    public Map<String, InputOptionValue> getOptions() {
        return options;
    }

    public boolean isComplete() {
        for (InputOptionValue option : options.values()) {
            Boolean required = option.getRequired();

            if (option.getDisabled() != null) {
                required = true;
                for (Map.Entry<String, Object> entry : option.getDisabled().entrySet()) {
                    Object value = options.get(entry.getKey()).getValue();
                    if (value != null && value.equals(entry.getValue().toString())) {
                        required = false;
                        break;
                    }
                }
            }

            if (!option.hasUpdated() && required) {
                return false;
            }
        }
        return true;
    }
}