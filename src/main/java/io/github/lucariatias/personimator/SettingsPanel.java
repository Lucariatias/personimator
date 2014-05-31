package io.github.lucariatias.personimator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SettingsPanel extends JPanel {

    private Personimator personimator;

    private double maxScale;

    private JSlider sliderScale;
    private JSpinner spinnerMax;
    private JSpinner spinnerMin;
    private JSpinner spinnerFrames;
    private JSlider sliderFrame;

    public SettingsPanel(Personimator personimator) {
        this.personimator = personimator;
        setSize(800, 120);
        setLayout(null);

        this.maxScale = 10D;

        spinnerMin = new JSpinner();
        spinnerMin.setBounds(6, 7, 64, 28);
        spinnerMin.setModel(new SpinnerNumberModel(1D, 0.01D, 1000D, 0.01D));
        spinnerMin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                sliderScale.setMinimum((int) Math.round((double) spinnerMin.getValue() * (sliderScale.getMaximum() / getMaxScale())));
            }
        });
        add(spinnerMin);
        
        spinnerMax = new JSpinner();
        spinnerMax.setBounds(730, 6, 64, 28);
        spinnerMax.setModel(new SpinnerNumberModel(10D, 0.01D, 1000D, 0.01D));
        spinnerMax.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                setMaxScale((int) Math.round((double) spinnerMax.getValue()));
            }
        });
        add(spinnerMax);

        sliderScale = new JSlider();
        sliderScale.setBounds(70, 6, 660, 29);
        sliderScale.setMaximum(1000);
        sliderScale.setMinimum((int) Math.round((double) spinnerMin.getValue() * (sliderScale.getMaximum() / getMaxScale())));
        sliderScale.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                double scale = (double) sliderScale.getValue() / (sliderScale.getMaximum() / getMaxScale());
                getDrawingPanel().setScale(scale);
                SettingsPanel.this.personimator.repaint();
            }
        });
        add(sliderScale);

        spinnerFrames = new JSpinner();
        spinnerFrames.setBounds(730, 40, 64, 28);
        spinnerFrames.setModel(new SpinnerNumberModel(4, 0, Integer.MAX_VALUE, 1));
        spinnerFrames.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                sliderFrame.setMaximum((int) spinnerFrames.getValue() - 1);
            }
        });
        add(spinnerFrames);

        sliderFrame = new JSlider();
        sliderFrame.setBounds(70, 40, 660, 29);
        sliderFrame.setMinimum(0);
        sliderFrame.setMaximum(3);
        sliderFrame.setSnapToTicks(true);
        sliderFrame.setValue(0);
        sliderFrame.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                getDrawingPanel().setFrame(sliderFrame.getValue());
            }
        });
        add(sliderFrame);
    }

    public double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(double maxScale) {
        this.maxScale = maxScale;
    }

    private DrawingPanel getDrawingPanel() {
        return personimator.getDrawingPanel();
    }

    private ToolboxPanel getToolboxPanel() {
        return personimator.getToolboxPanel();
    }

    public int getFrames() {
        return (int) spinnerFrames.getValue();
    }

    public void setFrames(int frames) {
        spinnerFrames.setValue(frames);
        sliderFrame.setMaximum(frames - 1);
    }

    public void reset() {
        sliderFrame.setValue(0);
    }

}
