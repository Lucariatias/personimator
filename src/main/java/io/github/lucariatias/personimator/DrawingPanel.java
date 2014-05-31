package io.github.lucariatias.personimator;

import io.github.lucariatias.personimator.gif.AnimatedGifEncoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DrawingPanel extends JPanel {

    private Personimator personimator;

    private Skeleton skeleton;
    private double xOffset;
    private double yOffset;
    private double scale;

    private int canvasWidth;
    private int canvasHeight;
    private int frame;
    private Map<Integer, AnimationFrame> frames = new HashMap<>();

    // Hacky stuff, working pivot, light source & mouse state
    private Pivot workingPivot;
    private LightSource workingLightSource;
    private boolean mousePressed;
    private int mouseX;
    private int mouseY;
    private int startX;
    private int startY;

    public DrawingPanel(Personimator personimator) {
        this.personimator = personimator;
        setSize(640, 480);
        setLayout(null);
        this.skeleton = new Skeleton();
        this.scale = 1D;
        this.canvasWidth = 16;
        this.canvasHeight = 32;
        this.xOffset = (getWidth() / 2D) - getCanvasWidth();
        this.yOffset = (getHeight() / 2D) - getCanvasHeight();
        setFrame(0);
        JScrollBar verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        verticalScrollBar.setMinimum(0);
        verticalScrollBar.setMaximum(getHeight());
        verticalScrollBar.setBounds((int) (getWidth() - verticalScrollBar.getPreferredSize().getWidth()), 0, (int) verticalScrollBar.getPreferredSize().getWidth(), (int) (getHeight() - verticalScrollBar.getPreferredSize().getWidth()));
        verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent event) {
                setYOffset(event.getValue());
                repaint();
            }
        });
        verticalScrollBar.setValue((int) yOffset);
        add(verticalScrollBar);
        JScrollBar horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        horizontalScrollBar.setMinimum(0);
        horizontalScrollBar.setMaximum(getWidth());
        horizontalScrollBar.setBounds(0, (int) (getHeight() - horizontalScrollBar.getPreferredSize().getHeight()), (int) (getWidth() - horizontalScrollBar.getPreferredSize().getHeight()), (int) horizontalScrollBar.getPreferredSize().getHeight());
        horizontalScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent event) {
                setXOffset(event.getValue());
                repaint();
            }
        });
        horizontalScrollBar.setValue((int) xOffset);
        add(horizontalScrollBar);
        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                int x = (int) Math.round((event.getX() - getXOffset()) / getScale());
                int y = (int) Math.round((event.getY() - getYOffset()) / getScale());
                mouseX = x;
                mouseY = y;
                mousePressed = true;
                if (SwingUtilities.isLeftMouseButton(event)) {
                    if (getToolboxPanel().isPivotSelected()) {
                        skeleton.addPivot(x, y);
                        getLogger().info("Pivot added at " + x + ", " + y);
                    }
                    if (getToolboxPanel().isConnectorSelected()) {
                        workingPivot = skeleton.getPivot(x, y);
                        getLogger().info("Starting connector from " + x + ", " + y + (workingPivot != null ? " (pivot found at " + (int) workingPivot.getLocation().getX() + ", " + (int) workingPivot.getLocation().getY() + ")" : " (pivot not found)"));
                    }
                    if (getToolboxPanel().isImageSelected()) {
                        Connector connector = skeleton.getConnector(x, y);
                        if (connector != null) {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics (PNG)", "png"));
                            fileChooser.showOpenDialog(DrawingPanel.this);
                            try {
                                connector.setImage(ImageIO.read(fileChooser.getSelectedFile()));
                                connector.setImageName(fileChooser.getSelectedFile().getName());
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }
                    }
                    if (getToolboxPanel().isLightSourceSelected()) {
                        startX = x;
                        startY = y;
                        getLogger().info("Starting light source from " + x + ", " + y);
                    }
                    if (getToolboxPanel().isMoveSelected()) {
                        skeleton.deselectAll();
                        workingPivot = skeleton.getPivot(x, y);
                        if (workingPivot != null) {
                            workingPivot.setSelected(true);
                            skeleton.selectChildNodes(workingPivot);
                        } else {
                            workingLightSource = frames.get(frame).getLightSource(x, y);
                            if (workingLightSource != null) {
                                workingLightSource.setSelected(true);
                            }
                        }
                    }
                    if (getToolboxPanel().isMoveIndependentlySelected()) {
                        skeleton.deselectAll();
                        workingPivot = skeleton.getPivot(x, y);
                        if (workingPivot != null) {
                            workingPivot.setSelected(true);
                        } else {
                            workingLightSource = frames.get(frame).getLightSource(x, y);
                            if (workingLightSource != null) {
                                workingLightSource.setSelected(true);
                            }
                        }
                    }
                    if (getToolboxPanel().isDeleteSelected()) {
                        Pivot pivot = skeleton.getPivot(x, y);
                        Connector connector = skeleton.getConnector(x, y);
                        LightSource lightSource = frames.get(frame).getLightSource(x, y);
                        if (pivot != null) {
                            skeleton.delete(pivot);
                        } else if (connector != null) {
                            skeleton.delete(connector);
                        } else if (lightSource != null) {
                            frames.get(frame).delete(lightSource);
                        }
                    }
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                int x = (int) Math.round((event.getX() - getXOffset()) / getScale());
                int y = (int) Math.round((event.getY() - getYOffset()) / getScale());
                mousePressed = false;
                if (getToolboxPanel().isPivotSelected()) {
                    workingPivot = null;
                }
                if (getToolboxPanel().isConnectorSelected()) {
                    Pivot pivot1 = workingPivot;
                    Pivot pivot2 = skeleton.getPivot(x, y);
                    if (pivot1 != null && pivot2 != null) {
                        skeleton.addConnector(pivot1, pivot2);
                    }
                    workingPivot = null;
                    getLogger().info("Finished connector at " + x + ", " + y + (pivot2 != null ? " (pivot found at " + (int) pivot2.getLocation().getX() + ", " + (int) pivot2.getLocation().getY() + ")" : " (pivot not found)"));
                }
                if (getToolboxPanel().isLightSourceSelected()) {
                    int dy = y - startY;
                    int dx = x - startX;
                    frames.get(frame).addLightSource(new LightSource(new Point(startX, startY), (int) Math.round(Math.sqrt(dx * dx + dy * dy))));
                }
                if (getToolboxPanel().isMoveSelected()) {
                    frames.get(frame).deselectAll();
                }
                if (getToolboxPanel().isMoveIndependentlySelected()) {
                    frames.get(frame).deselectAll();
                }
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                int x = (int) Math.round((event.getX() - getXOffset()) / getScale());
                int y = (int) Math.round((event.getY() - getYOffset()) / getScale());
                mouseX = x;
                mouseY = y;
                if (getToolboxPanel().isMoveSelected()) {
                    if (workingPivot != null) {
                        for (Pivot child : skeleton.getChildNodes(workingPivot)) {
                            child.setLocation(new Point((int) Math.round(child.getLocation().getX() + (x - workingPivot.getLocation().getX())), (int) Math.round(child.getLocation().getY() + (y - workingPivot.getLocation().getY()))));
                        }
                        workingPivot.setLocation(new Point(x, y));
                    } else if (workingLightSource != null) {
                        workingLightSource.setLocation(new Point(x, y));
                    }
                }
                if (getToolboxPanel().isMoveIndependentlySelected()) {
                    if (workingPivot != null) {
                        workingPivot.setLocation(new Point(x, y));
                    } else if (workingLightSource != null) {
                        workingLightSource.setLocation(new Point(x, y));
                    }
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                int x = (int) Math.round((event.getX() - getXOffset()) / getScale());
                int y = (int) Math.round((event.getY() - getYOffset()) / getScale());
                mouseX = x;
                mouseY = y;
                repaint();
            }
        });
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getScale() {
        return scale;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public void setCanvasWidth(int canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasHeight(int canvasHeight) {
        this.canvasHeight = canvasHeight;
    }

    public double getXOffset() {
        return xOffset;
    }

    public void setXOffset(double xOffset) {
        this.xOffset = xOffset;
    }

    public double getYOffset() {
        return yOffset;
    }

    public void setYOffset(double yOffset) {
        this.yOffset = yOffset;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        int xOrigin = (int) (xOffset - ((getCanvasWidth() * getScale()) / 2D));
        int yOrigin = (int) (yOffset - ((getCanvasHeight() * getScale()) / 2D));
        for (int x = 0; x < getCanvasWidth(); x += 8) {
            for (int y = 0; y < getCanvasHeight(); y += 8) {
                graphics.setColor(((x / 8) + ((y / 8) % 2 == 0 ? 1 : 0)) % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
                graphics.fillRect((int) (xOrigin + (x * getScale())), (int) (yOrigin + (y * getScale())), (int) (8 * getScale()), (int) (8 * getScale()));
            }
        }
        graphics.setColor(Color.BLACK);
        graphics.drawRect(xOrigin, yOrigin, (int) (canvasWidth * getScale()), (int) (canvasHeight * getScale()));

        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.translate(xOffset, yOffset);
        graphics2D.scale(scale, scale);

        frames.get(frame).render(graphics);

        if (workingPivot != null && mousePressed && getToolboxPanel().isConnectorSelected()) {
            graphics.setColor(Color.ORANGE);
            graphics.drawLine((int) workingPivot.getLocation().getX(), (int) workingPivot.getLocation().getY(), mouseX, mouseY);
        }

        if (mousePressed && getToolboxPanel().isLightSourceSelected()) {
            graphics.setColor(new Color(255, 255, 128));
            int dy = startY - mouseY;
            int dx = startX - mouseX;
            int radius = (int) Math.round(Math.sqrt(dy * dy + dx * dx));
            graphics.drawOval(startX - radius, startY - radius, radius * 2, radius * 2);
        }

        graphics2D.scale(1D / scale, 1D / scale);
        graphics2D.translate(-xOffset, -yOffset);
    }

    private ToolboxPanel getToolboxPanel() {
        return personimator.getToolboxPanel();
    }

    private SettingsPanel getSettingsPanel() {
        return personimator.getSettingsPanel();
    }

    private Logger getLogger() {
        return personimator.getLogger();
    }

    public void setFrame(int frame) {
        if (frames.get(frame) == null) {
            if (frames.get(this.frame) != null) {
                frames.put(frame, new AnimationFrame(frames.get(this.frame)));
            } else {
                frames.put(frame, new AnimationFrame());
            }
        }
        this.frame = frame;
        this.skeleton = frames.get(frame).getSkeleton();
        repaint();
    }

    public int getFrame() {
        return frame;
    }

    public BufferedImage[] renderFrames() {
        List<BufferedImage> images = new ArrayList<>();
        for (int i = 0; i < getSettingsPanel().getFrames(); i++) {
            BufferedImage image = new BufferedImage(getCanvasWidth(), getCanvasHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            double xOffset = getCanvasWidth() / 2D;
            double yOffset = getCanvasHeight() / 2D;
            graphics.translate(xOffset, yOffset);

            if (frames.get(i) != null) {
                frames.get(i).renderImage(graphics);
            }

            graphics.translate(-xOffset, -yOffset);
            graphics.dispose();

            frames.get(i).renderLight(image);

            images.add(image);
        }
        return images.toArray(new BufferedImage[images.size()]);
    }

    public void reset() {
        this.skeleton = new Skeleton();
        this.canvasWidth = 16;
        this.canvasHeight = 32;
        this.frame = 0;
        frames.clear();
        workingPivot = null;
        this.xOffset = (getWidth() / 2D) - getCanvasWidth();
        this.yOffset = (getHeight() / 2D) - getCanvasHeight();
    }

    public void load(File file) {
        reset();
        getSettingsPanel().reset();
        try {
            ZipFile zipFile = new ZipFile(file);
            Properties animationProperties = new Properties();
            animationProperties.load(zipFile.getInputStream(zipFile.getEntry("/animation.properties")));
            int frames = Integer.parseInt(animationProperties.getProperty("frames"));
            setCanvasWidth(Integer.parseInt(animationProperties.getProperty("width")));
            setCanvasHeight(Integer.parseInt(animationProperties.getProperty("height")));
            for (int i = 0; i < frames; i++) {
                this.frames.put(i, new AnimationFrame(zipFile, i));
            }
            getSettingsPanel().setFrames(frames);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        skeleton = frames.get(0).getSkeleton();
        repaint();
        getSettingsPanel().repaint();
    }

    public void save(File file) {
        try {
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            Map<String, BufferedImage> images = new HashMap<>();
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            for (Map.Entry<Integer, AnimationFrame> frame : frames.entrySet()) {
                ZipEntry skeletonPropertiesEntry = new ZipEntry("/frames/" + frame.getKey() + "/skeleton.properties");
                zipOutputStream.putNextEntry(skeletonPropertiesEntry);
                Properties skeletonProperties = frame.getValue().getSkeleton().asProperties();
                skeletonProperties.store(zipOutputStream, "");
                zipOutputStream.closeEntry();
                images.putAll(frame.getValue().getSkeleton().getImages());
                ZipEntry lightSourcesPropertiesEntry = new ZipEntry("/frames/" + frame.getKey() + "/lightsources.properties");
                zipOutputStream.putNextEntry(lightSourcesPropertiesEntry);
                Properties lightSourceProperties = frame.getValue().lightSourcesAsProperties();
                lightSourceProperties.store(zipOutputStream, "");
                zipOutputStream.closeEntry();
            }
            for (Map.Entry<String, BufferedImage> image : images.entrySet()) {
                if (!image.getKey().equals("null")) {
                    ZipEntry imageEntry = new ZipEntry("/images/" + image.getKey());
                    zipOutputStream.putNextEntry(imageEntry);
                    ImageIO.write(image.getValue(), "png", zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            }
            ZipEntry animationPropertiesEntry = new ZipEntry("/animation.properties");
            zipOutputStream.putNextEntry(animationPropertiesEntry);
            Properties animationProperties = new Properties();
            animationProperties.setProperty("frames", "" + getSettingsPanel().getFrames());
            animationProperties.setProperty("width", "" + getCanvasWidth());
            animationProperties.setProperty("height", "" + getCanvasHeight());
            animationProperties.store(zipOutputStream, "");
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void exportAnimation(File file, int delay) {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.setRepeat(0);
        encoder.setDelay(delay);
        Color transparentColour = new Color(255, 0, 128);
        encoder.setTransparent(transparentColour);
        encoder.start(file.getPath());
        BufferedImage[] renderedImages = renderFrames();
        for (BufferedImage image : renderedImages) {
            BufferedImage frame = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int pixel = image.getRGB(x, y);
                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;
                    if (alpha == 0) {
                        frame.setRGB(x, y, (transparentColour.getRed() << 16) | (transparentColour.getGreen() << 8) | transparentColour.getBlue());
                    } else {
                        frame.setRGB(x, y, (red << 16) | (green << 8) | blue);
                    }
                }
            }
            encoder.addFrame(frame);
        }
        encoder.finish();
        for (BufferedImage image : renderedImages) {
            image.flush();
        }
    }

    public void exportSheet(File file) {
        BufferedImage sheet = new BufferedImage(getCanvasWidth() * getSettingsPanel().getFrames(), getCanvasHeight(), BufferedImage.TYPE_INT_ARGB);
        int x = 0;
        Graphics2D graphics = sheet.createGraphics();
        for (BufferedImage image : renderFrames()) {
            graphics.drawImage(image, x, 0, null);
            image.flush();
            x += getCanvasWidth();
        }
        graphics.dispose();
        try {
            ImageIO.write(sheet, "png", file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        sheet.flush();
    }

}
