package src.main.java.com.labeler;

class ImageItem {
    private boolean isUrban;
    private boolean hasFlooding;
    private String floodDepth;

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
}