package src.main.java.com.labeler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class InputOption {
    private String description;
    private String type;
    private boolean required = false;
    private Map<String, String> options;
    private Map<String, Object> disabled;
    private Map<String, Object> keybinds;

    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return this.description;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return this.type;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
    public boolean getRequired() {
        return this.required;
    }

    public void setOptions(Object options) {
        if (options instanceof List) {
            this.options = ((List<String>) options)
                .stream()
                .collect(Collectors.toMap(option -> option, option -> ""));
        } else {
            this.options = (Map<String, String>) options;
        }
    }
    public Map<String, String> getOptions() {
        return this.options;
    }

    public void setDisabled(Map<String, Object> disabled) {
        this.disabled = disabled;
    }
    public Map<String, Object> getDisabled() {
        return this.disabled;
    }

    public void setKeybinds(Map<String, Object> keybinds) {
        this.keybinds = keybinds;
    }
    public Map<String, Object> getKeybinds() {
        return this.keybinds;
    }
}