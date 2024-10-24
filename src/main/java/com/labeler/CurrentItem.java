package src.main.java.com.labeler;

class CurrentItem extends ImageItem {
    private String filename;
    private boolean hasIsUrbanUpdated;
    private boolean hasFloodingUpdated;
    private boolean hasFloodDepthUpdated;

    public CurrentItem() {
    }

    public CurrentItem(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setIsUrban(boolean isUrban) {
        this.hasIsUrbanUpdated = true;
        super.setIsUrban(isUrban);
    }

    public void setHasFlooding(boolean hasFlooding) {
        this.hasFloodingUpdated = true;
        super.setHasFlooding(hasFlooding);
    }

    public void setFloodDepth(String floodDepth) {
        this.hasFloodDepthUpdated = floodDepth != null;
        super.setFloodDepth(floodDepth);
    }

    public ImageItem toImageItem() {
        ImageItem imageItem = new ImageItem();
        imageItem.setIsUrban(this.getIsUrban());
        imageItem.setHasFlooding(this.getHasFlooding());
        imageItem.setFloodDepth(this.getFloodDepth());
        return imageItem;
    }

    public boolean isComplete() {
        return hasIsUrbanUpdated && hasFloodingUpdated && (!this.getHasFlooding() || hasFloodDepthUpdated);
    }
}