package src.main.java.com.labeler;

import java.util.ArrayList;

class InputOptionValue extends InputOption {
    private Object value;
    private boolean hasUpdated = false;

    public InputOptionValue() {
    }

    public void setValue(Object val) {
        if (this.getType().equals("select-many")) {
            if (this.value == null) {
                this.value = new ArrayList<String>();
            }
            ((ArrayList<String>) this.value).add(val.toString());
        } else {
            this.value = val;
        }
        this.hasUpdated = true;
    }

    public Object getValue() {
        return this.value;
    }

    public void removeValue(String val) {
        if (this.getType().equals("select-many")) {
            ((ArrayList<String>) this.value).remove(val);
            this.hasUpdated = !((ArrayList<String>) this.value).isEmpty();
        } else {
            this.value = null;
            this.hasUpdated = false;
        }
    }

    public boolean hasUpdated() {
        return this.hasUpdated;
    }
}