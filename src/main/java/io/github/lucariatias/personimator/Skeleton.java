package io.github.lucariatias.personimator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipFile;

public class Skeleton {

    private Set<Pivot> pivots = new HashSet<>();
    private Set<Connector> connectors = new HashSet<>();

    public Skeleton() {}

    public Skeleton(Skeleton skeleton) {
        Map<Pivot, Pivot> newPivots = new HashMap<>();
        for (Pivot pivot : skeleton.pivots) {
            Pivot newPivot = newPivots.containsKey(pivot) ? newPivots.get(pivot) : new Pivot(pivot);
            pivots.add(newPivot);
            newPivots.put(pivot, newPivot);
            for (Pivot child : skeleton.getDirectChildNodes(pivot)) {
                Pivot newChild = newPivots.containsKey(child) ? newPivots.get(child) : new Pivot(child);
                pivots.add(newChild);
                newPivots.put(child, newChild);
                Connector newConnector = new Connector(newPivot, newChild);
                for (Connector connector : skeleton.connectors) {
                    if (connector.getFrom() == pivot && connector.getTo() == child) {
                        newConnector.setImage(connector.getImage());
                        newConnector.setImageName(connector.getImageName());
                        break;
                    }
                }
                connectors.add(newConnector);
            }
        }
    }

    public Skeleton(ZipFile zipFile, int frame) {
        Properties properties = new Properties();
        try {
            properties.load(zipFile.getInputStream(zipFile.getEntry("/frames/" + frame + "/skeleton.properties")));
            String pivotString = properties.getProperty("pivots");
            Map<String, Pivot> pivotIds = new HashMap<>();
            for (int i = 0; i < pivotString.split(",").length - 1; i += 3) {
                Pivot pivot = new Pivot(new Point(Integer.parseInt(pivotString.split(",")[i+1]), Integer.parseInt(pivotString.split(",")[i+2])));
                pivots.add(pivot);
                pivotIds.put(pivotString.split(",")[i], pivot);
            }
            String connectorString = properties.getProperty("connectors");
            for (int i = 0; i < connectorString.split(",").length - 1; i += 3) {
                Connector connector = new Connector(pivotIds.get(connectorString.split(",")[i]), pivotIds.get(connectorString.split(",")[i+1]));
                if (!connectorString.split(",")[i+2].equals("null")) {
                    BufferedImage image = ImageIO.read(zipFile.getInputStream(zipFile.getEntry("/images/" + connectorString.split(",")[i+2])));
                    connector.setImage(image);
                    connector.setImageName(connectorString.split(",")[i+2]);
                }
                connectors.add(connector);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public Properties asProperties() {
        Properties properties = new Properties();
        StringBuilder pivotStringBuilder = new StringBuilder();
        Map<Pivot, Integer> pivotIds = new HashMap<>();
        int i = 0;
        for (Pivot pivot : pivots) {
            pivotStringBuilder.append(i).append(",").append((int) Math.round(pivot.getLocation().getX())).append(",").append((int) Math.round(pivot.getLocation().getY())).append(",");
            pivotIds.put(pivot, i);
            i++;
        }
        properties.setProperty("pivots", pivotStringBuilder.toString());
        StringBuilder connectorStringBuilder = new StringBuilder();
        for (Connector connector : connectors) {
            connectorStringBuilder.append(pivotIds.get(connector.getFrom())).append(",").append(pivotIds.get(connector.getTo())).append(",").append(connector.getImage() == null ? "null" : connector.getImageName()).append(",");
        }
        properties.setProperty("connectors", connectorStringBuilder.toString());
        return properties;
    }

    public Map<String, BufferedImage> getImages() {
        Map<String, BufferedImage> images = new HashMap<>();
        for (Connector connector : connectors) {
            if (connector.getImage() != null) {
                images.put(connector.getImageName(), connector.getImage());
            }
        }
        return images;
    }

    public void addPivot(int x, int y) {
        pivots.add(new Pivot(new Point(x, y)));
    }

    public Pivot getPivot(int x, int y) {
        for (Pivot pivot : pivots) {
            double dx = pivot.getLocation().getX() - x;
            double dy = pivot.getLocation().getY() - y;
            if (dx * dx + dy * dy <= pivot.getRadius() * pivot.getRadius()) return pivot;
        }
        return null;
    }

    public void addConnector(Pivot pivot1, Pivot pivot2) {
        if (pivot1 != pivot2) connectors.add(new Connector(pivot1, pivot2));
    }

    public Connector getConnector(int x, int y) {
        for (Connector connector : connectors) {
            double ax = connector.getFrom().getLocation().getX();
            double ay = connector.getFrom().getLocation().getY();
            double bx = connector.getTo().getLocation().getX();
            double by = connector.getTo().getLocation().getY();
            double rn = ((double) x - ax) * (bx - ax) + ((double) y - ay) * (by - ay);
            double rd = (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
            double r = rn / rd;
            double s = ((ay - (double) y) * (bx - ax) - (ax - (double) x) * (by - ay) ) / rd;
            double distanceLine = Math.abs(s) * Math.sqrt(rd);
            double distanceSegment;
            if ((r >= 0) && (r <= 1)) {
                distanceSegment = distanceLine;
            } else {
                double dist1 = ((double) x - ax) * ((double) x - ax) + ((double) y - ay) * ((double) y - ay);
                double dist2 = ((double) x - bx) * ((double) x - bx) + ((double) y - by) * ((double) y - by);
                if (dist1 < dist2) {
                    distanceSegment = Math.sqrt(dist1);
                } else {
                    distanceSegment = Math.sqrt(dist2);
                }
            }
            if (distanceSegment <= 1) return connector;
        }
        return null;
    }

    public void render(Graphics graphics) {
        for (Connector connector : connectors) {
            connector.render(graphics);
        }
        for (Pivot pivot : pivots) {
            pivot.render(graphics);
        }
    }

    public void renderImage(Graphics graphics) {
        for (Connector connector : connectors) {
            connector.renderImage(graphics);
        }
    }

    public void deselectAll() {
        for (Connector connector : connectors) {
            connector.setSelected(false);
        }
        for (Pivot pivot : pivots) {
            pivot.setSelected(false);
        }
    }

    public void delete(Pivot pivot) {
        for (Iterator<Connector> iterator = connectors.iterator(); iterator.hasNext(); ) {
            Connector connector = iterator.next();
            if (connector.isSelected() || connector.getFrom() == pivot || connector.getTo() == pivot) {
                iterator.remove();
            }
        }
        pivots.remove(pivot);
    }

    public void delete(Connector connector) {
        connectors.remove(connector);
    }

    public void deleteSelected() {
        for (Iterator<Connector> iterator = connectors.iterator(); iterator.hasNext(); ) {
            Connector connector = iterator.next();
            if (connector.isSelected() || connector.getFrom().isSelected() || connector.getTo().isSelected()) {
                iterator.remove();
            }
        }
        for (Iterator<Pivot> iterator = pivots.iterator(); iterator.hasNext(); ) {
            Pivot pivot = iterator.next();
            if (pivot.isSelected()) {
                iterator.remove();
            }
        }
    }

    public Set<Pivot> getChildNodes(Pivot pivot) {
        return getChildNodes(pivot, new HashSet<Pivot>());
    }

    private Set<Pivot> getChildNodes(Pivot pivot, Set<Pivot> previouslyFound) {
        Set<Pivot> children = new HashSet<>();
        for (Connector connector : connectors) {
            if (connector.getFrom() == pivot && !previouslyFound.contains(connector.getTo())) {
                children.add(connector.getTo());
                Set<Pivot> found = new HashSet<>();
                found.addAll(children);
                found.addAll(previouslyFound);
                children.addAll(getChildNodes(connector.getTo(), found));
            }
        }
        return children;
    }

    private Set<Pivot> getDirectChildNodes(Pivot pivot) {
        Set<Pivot> children = new HashSet<>();
        for (Connector connector : connectors) {
            if (connector.getFrom() == pivot) {
                children.add(connector.getTo());
            }
        }
        return children;
    }

    public void selectChildNodes(Pivot pivot) {
        for (Pivot child : getChildNodes(pivot)) {
            child.setSelected(true);
        }
    }
}
