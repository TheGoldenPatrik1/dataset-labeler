package src.main.java.com.labeler;

class CurrentItem extends ImageItem {
    private String filename;
    private boolean hasFloodingUpdated;

    public CurrentItem() {
    }

    public CurrentItem(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setHasFlooding(boolean hasFlooding) {
        this.hasFloodingUpdated = true;
        super.setHasFlooding(hasFlooding);
    }

    public ImageItem toImageItem() {
        ImageItem imageItem = new ImageItem();
        imageItem.setHasFlooding(this.getHasFlooding());
        return imageItem;
    }

    public boolean isComplete() {
        return hasFloodingUpdated;
    }
}