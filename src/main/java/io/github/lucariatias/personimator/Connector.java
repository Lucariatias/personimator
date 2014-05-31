package io.github.lucariatias.personimator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Connector {

    private Pivot from;
    private Pivot to;
    private boolean selected;

    private String imageName;
    private BufferedImage image;

    public Connector(Pivot from, Pivot to) {
        this.from = from;
        this.to = to;
    }

    public Pivot getFrom() {
        return from;
    }

    public Pivot getTo() {
        return to;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public void render(Graphics graphics) {
        graphics.setColor(isSelected() ? Color.ORANGE : Color.GRAY);
        graphics.drawLine((int) getFrom().getLocation().getX(), (int) getFrom().getLocation().getY(), (int) getTo().getLocation().getX(), (int) getTo().getLocation().getY());
        renderImage(graphics);
    }

    public void renderImage(Graphics graphics) {
        if (image != null) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            double angle = Math.atan2(to.getLocation().getX() - from.getLocation().getX(), to.getLocation().getY() - from.getLocation().getY());
            double dx = to.getLocation().getX() - from.getLocation().getX();
            double dy = to.getLocation().getY() - from.getLocation().getY();
            double yScale = Math.sqrt(dx * dx + dy * dy) / (double) image.getHeight();
            graphics2D.rotate(-angle, from.getLocation().getX(), from.getLocation().getY());
            graphics2D.translate(from.getLocation().getX(), from.getLocation().getY());
            graphics2D.scale(1D, yScale);
            graphics2D.translate(-from.getLocation().getX(), -from.getLocation().getY());
            graphics.drawImage(image, (int) Math.round(from.getLocation().getX() - image.getWidth() / 2D), (int) Math.round(from.getLocation().getY()), null);
            graphics2D.translate(from.getLocation().getX(), from.getLocation().getY());
            graphics2D.scale(1D, 1D / yScale);
            graphics2D.translate(-from.getLocation().getX(), -from.getLocation().getY());
            graphics2D.rotate(angle, from.getLocation().getX(), from.getLocation().getY());
        }
    }

}
