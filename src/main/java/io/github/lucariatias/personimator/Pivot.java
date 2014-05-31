package io.github.lucariatias.personimator;

import java.awt.*;

public class Pivot {

    private Point location;
    private int radius;
    private boolean selected;

    public Pivot(Point location) {
        this.location = location;
        this.radius = 1;
    }

    public Pivot(Pivot pivot) {
        this.location = new Point(pivot.getLocation());
        this.radius = pivot.getRadius();
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
        graphics.setColor(isSelected() ? Color.ORANGE : Color.DARK_GRAY);
        graphics.fillOval((int) getLocation().getX() - getRadius(), (int) getLocation().getY() - getRadius(), getRadius() * 2, getRadius() * 2);
    }
}
