package src.main.java.com.labeler;

import java.util.ArrayList;
import java.util.List;

class ImageItem {
    private boolean isUrban;
    private boolean hasFlooding;
    private String floodDepth;
    private List<String> objects = new ArrayList<>();

    public ImageItem() {
    }

    public void setIsUrban(boolean isUrban) {
        this.isUrban = isUrban;
    }

    public boolean getIsUrban() {
        return isUrban;
    }

    public void setHasFlooding(boolean hasFlooding) {
        this.hasFlooding = hasFlooding;
        if (!hasFlooding) {
            this.floodDepth = null;
        }
    }

    public boolean getHasFlooding() {
        return hasFlooding;
    }

    public void setFloodDepth(String floodDepth) {
        this.floodDepth = floodDepth;
    }

    public String getFloodDepth() {
        return floodDepth;
    }

    public void addObject(String object) {
        objects.add(object.toLowerCase());
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void removeObject(String object) {
        objects.remove(object.toLowerCase());
    }

    public boolean hasObject(String object) {
        return objects.contains(object.toLowerCase());
    }

    public CurrentItem toCurrentItem(String filename) {
        CurrentItem currentItem = new CurrentItem(filename);
        currentItem.setIsUrban(this.getIsUrban());
        currentItem.setHasFlooding(this.getHasFlooding());
        currentItem.setFloodDepth(this.getFloodDepth());
        currentItem.setObjects(this.getObjects());
        return currentItem;
    }
}