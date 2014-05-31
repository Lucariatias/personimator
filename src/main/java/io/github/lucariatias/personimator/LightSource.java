package io.github.lucariatias.personimator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LightSource {

    private Point location;
    private int radius;
    private boolean selected;

    public LightSource(Point location, int radius) {
        this.location = location;
        this.radius = radius;
    }

    public LightSource(LightSource lightSource) {
        this.location = lightSource.location;
        this.radius = lightSource.radius;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void render(Graphics graphics) {
        graphics.setColor(isSelected() ? new Color(255, 255, 128) : Color.YELLOW);
        graphics.drawOval((int) location.getX() - radius, (int) location.getY() - radius, 2 * radius, 2 * radius);
    }

    public void renderLight(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage light = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int xLocation = (int) Math.round(location.getX() + (width / 2D));
        int yLocation = (int) Math.round(location.getY() + (height / 2D));
        for (int x = 0; x < light.getWidth(); x++) {
            for (int y = 0; y < light.getHeight(); y++) {
                if ((image.getRGB(x, y) >> 24 & 0xff) != 0) {
                    int red = 0;
                    int green = 0;
                    int blue = 0;
                    int dy = yLocation - y;
                    int dx = xLocation - x;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    int alpha = (int) Math.round(Math.min((dist / (double) radius), 1) * 192);
                    light.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
                }
            }
        }
        Graphics graphics = image.createGraphics();
        graphics.drawImage(light, 0, 0, null);
        graphics.dispose();
        light.flush();
    }

}
