package src.main.java.com.labeler;

import java.util.List;
import java.util.Map;

class InputOption {
    private String description;
    private String type;
    private boolean required = false;
    private List<String> options;
    private Map<String, Object> disabled;

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

    public void setOptions(List<String> options) {
        this.options = options;
    }
    public List<String> getOptions() {
        return this.options;
    }

    public void setDisabled(Map<String, Object> disabled) {
        this.disabled = disabled;
    }
    public Map<String, Object> getDisabled() {
        return this.disabled;
    }
}