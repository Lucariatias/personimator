package io.github.lucariatias.personimator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipFile;

public class AnimationFrame {

    private Skeleton skeleton;
    private Set<LightSource> lightSources = new HashSet<>();

    public AnimationFrame() {
        this.skeleton = new Skeleton();
    }

    public AnimationFrame(AnimationFrame frame) {
        this.skeleton = new Skeleton(frame.getSkeleton());
        for (LightSource lightSource : frame.lightSources) {
            lightSources.add(new LightSource(lightSource));
        }
    }

    public AnimationFrame(ZipFile zipFile, int frame) {
        this.skeleton = new Skeleton(zipFile, frame);
        if (zipFile.getEntry("/frames/" + frame + "/lightsources.properties") != null) {
            Properties properties = new Properties();
            try {
                properties.load(zipFile.getInputStream(zipFile.getEntry("/frames/" + frame + "/lightsources.properties")));
                String lightSourcesString = properties.getProperty("lightsources");
                for (int i = 0; i < lightSourcesString.split(",").length - 1; i += 3) {
                    if (Integer.parseInt(lightSourcesString.split(",")[i+2]) > 0) lightSources.add(new LightSource(new Point(Integer.parseInt(lightSourcesString.split(",")[i]), Integer.parseInt(lightSourcesString.split(",")[i+1])), Integer.parseInt(lightSourcesString.split(",")[i+2])));
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public void addLightSource(LightSource lightSource) {
        if (lightSource.getRadius() > 0) lightSources.add(lightSource);
    }

    public LightSource getLightSource(int x, int y) {
        for (LightSource lightSource : lightSources) {
            double dx = lightSource.getLocation().getX() - x;
            double dy = lightSource.getLocation().getY() - y;
            if (dx * dx + dy * dy <= lightSource.getRadius() * lightSource.getRadius()) return lightSource;
        }
        return null;
    }

    public void renderImage(Graphics graphics) {
        skeleton.renderImage(graphics);
    }

    public void renderLight(BufferedImage image) {
        for (LightSource lightSource : lightSources) {
            lightSource.renderLight(image);
        }
    }

    public void render(Graphics graphics) {
        skeleton.render(graphics);
        for (LightSource lightSource : lightSources) {
            lightSource.render(graphics);
        }
    }

    public Properties lightSourcesAsProperties() {
        Properties properties = new Properties();
        StringBuilder lightSourceStringBuilder = new StringBuilder();
        for (LightSource lightSource : lightSources) {
            lightSourceStringBuilder.append((int) lightSource.getLocation().getX()).append(",").append((int) lightSource.getLocation().getY()).append(",").append(lightSource.getRadius()).append(",");
        }
        properties.setProperty("lightsources", lightSourceStringBuilder.toString());
        return properties;
    }

    public void delete(LightSource lightSource) {
        lightSources.remove(lightSource);
    }

    public void deselectAll() {
        skeleton.deselectAll();
        for (LightSource lightSource : lightSources) {
            lightSource.setSelected(false);
        }
    }

}
