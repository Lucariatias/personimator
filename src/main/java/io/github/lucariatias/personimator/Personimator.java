package io.github.lucariatias.personimator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Logger;

public class Personimator extends JFrame {

    private ToolboxPanel toolboxPanel;
    private DrawingPanel drawingPanel;
    private SettingsPanel settingsPanel;
    private Logger logger;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException exception) {
            exception.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Personimator personimator = new Personimator();
                personimator.setVisible(true);
            }
        });
    }

    public Personimator() {
        logger = Logger.getLogger("Personimator");
        setTitle("Personimator");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null);
        toolboxPanel = new ToolboxPanel();
        toolboxPanel.setLocation(640, 0);
        getContentPane().add(toolboxPanel);
        drawingPanel = new DrawingPanel(this);
        drawingPanel.setLocation(0, 0);
        getContentPane().add(drawingPanel);
        settingsPanel = new SettingsPanel(this);
        settingsPanel.setLocation(0, 480);
        getContentPane().add(settingsPanel);
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New...");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        newMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final JFrame newFrame = new JFrame("New");
                        newFrame.setSize(240, 160);
                        newFrame.setLayout(null);
                        final JSpinner spinnerWidth = new JSpinner();
                        spinnerWidth.setModel(new SpinnerNumberModel(16, 1, Integer.MAX_VALUE, 1));
                        spinnerWidth.setBounds(80, 16, 64, 24);
                        newFrame.add(spinnerWidth);
                        JLabel lblWidth = new JLabel("Width: ");
                        lblWidth.setBounds(16, 16, 64, 24);
                        newFrame.add(lblWidth);
                        final JSpinner spinnerHeight = new JSpinner();
                        spinnerHeight.setModel(new SpinnerNumberModel(32, 1, Integer.MAX_VALUE, 1));
                        spinnerHeight.setBounds(80, 50, 64, 24);
                        newFrame.add(spinnerHeight);
                        JLabel lblHeight = new JLabel("Height: ");
                        lblHeight.setBounds(16, 50, 64, 24);
                        newFrame.add(lblHeight);
                        JButton okButton = new JButton("OK");
                        okButton.setBounds(104, 114, 32, 24);
                        okButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                getDrawingPanel().reset();
                                getDrawingPanel().setFrame(0);
                                getDrawingPanel().setCanvasHeight((int) spinnerHeight.getValue());
                                getDrawingPanel().setCanvasWidth((int) spinnerWidth.getValue());
                                newFrame.dispose();
                            }
                        });
                        newFrame.add(okButton);
                        newFrame.setVisible(true);
                    }
                });
            }
        });
        fileMenu.add(newMenuItem);
        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Personimator animations (*.psnmtr)", "psnmtr"));
                if (fileChooser.showOpenDialog(Personimator.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    getDrawingPanel().load(file);
                }
            }
        });
        fileMenu.add(openMenuItem);
        fileMenu.addSeparator();
        JMenuItem saveMenuItem = new JMenuItem("Save...");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Personimator animations (*.psnmtr)", "psnmtr"));
                if (fileChooser.showSaveDialog(Personimator.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().endsWith(".psnmtr")) file = new File(file.getPath() + ".psnmtr");
                    getDrawingPanel().save(file);
                }
            }
        });
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        JMenuItem exportSheetMenuItem = new JMenuItem("Export sheet...");
        exportSheetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics (*.png)", "png"));
                if (fileChooser.showSaveDialog(Personimator.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().endsWith(".png")) file = new File(file.getPath() + ".png");
                    getDrawingPanel().exportSheet(file);
                }
            }
        });
        fileMenu.add(exportSheetMenuItem);
        JMenuItem exportAnimationMenuItem = new JMenuItem("Export animation...");
        exportAnimationMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Animated GIF (*.gif)", "gif"));
                if (fileChooser.showSaveDialog(Personimator.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().endsWith(".gif")) file = new File(file.getPath() + ".gif");
                    final File finalFile = file;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final JFrame animationExportOptions = new JFrame("Animation options");
                            animationExportOptions.setSize(240, 160);
                            animationExportOptions.setLayout(null);
                            final JSpinner spinnerDelay = new JSpinner();
                            spinnerDelay.setModel(new SpinnerNumberModel(250, 1, 10000, 1));
                            spinnerDelay.setBounds(80, 16, 64, 24);
                            animationExportOptions.add(spinnerDelay);
                            JLabel lblDelay = new JLabel("Delay: ");
                            lblDelay.setBounds(16, 16, 64, 24);
                            animationExportOptions.add(lblDelay);
                            JButton okButton = new JButton("OK");
                            okButton.setBounds(104, 50, 32, 24);
                            okButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent event) {
                                    getDrawingPanel().exportAnimation(finalFile, (int) spinnerDelay.getValue());
                                    animationExportOptions.dispose();
                                }
                            });
                            animationExportOptions.add(okButton);
                            animationExportOptions.setVisible(true);
                        }
                    });
                }
            }
        });
        fileMenu.add(exportAnimationMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    public ToolboxPanel getToolboxPanel() {
        return toolboxPanel;
    }

    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    public Logger getLogger() {
        return logger;
    }
}
