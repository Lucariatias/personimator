package io.github.lucariatias.personimator;

import javax.swing.*;

public class ToolboxPanel extends JPanel {

    private JRadioButton rdbtnPivot;
    private JRadioButton rdbtnConnector;
    private JRadioButton rdbtnImage;
    private JRadioButton rdbtnLightSource;
    private JRadioButton rdbtnMove;
    private JRadioButton rdbtnMoveIndependently;
    private JRadioButton rdbtnDelete;

    public ToolboxPanel() {
        setLayout(null);
        setSize(160, 480);

        ButtonGroup buttonGroup = new ButtonGroup();

        int y = 6;

        rdbtnPivot = new JRadioButton("Pivot");
        rdbtnPivot.setBounds(6, y, 141, 23);
        rdbtnPivot.setSelected(true);
        add(rdbtnPivot);
        buttonGroup.add(rdbtnPivot);
        y += 35;

        rdbtnConnector = new JRadioButton("Connector");
        rdbtnConnector.setBounds(6, y, 141, 23);
        add(rdbtnConnector);
        buttonGroup.add(rdbtnConnector);
        y += 35;

        rdbtnImage = new JRadioButton("Image");
        rdbtnImage.setBounds(6, y, 141, 23);
        add(rdbtnImage);
        buttonGroup.add(rdbtnImage);
        y += 35;

        rdbtnLightSource = new JRadioButton("Light source");
        rdbtnLightSource.setBounds(6, y, 141, 23);
        add(rdbtnLightSource);
        buttonGroup.add(rdbtnLightSource);
        y += 35;

        rdbtnMove = new JRadioButton("Move");
        rdbtnMove.setBounds(6, y, 141, 23);
        add(rdbtnMove);
        buttonGroup.add(rdbtnMove);
        y += 35;

        rdbtnMoveIndependently = new JRadioButton("Move independently");
        rdbtnMoveIndependently.setBounds(6, y, 141, 23);
        add(rdbtnMoveIndependently);
        buttonGroup.add(rdbtnMoveIndependently);
        y += 35;

        rdbtnDelete = new JRadioButton("Delete");
        rdbtnDelete.setBounds(6, y, 141, 23);
        add(rdbtnDelete);
        buttonGroup.add(rdbtnDelete);
    }

    public boolean isPivotSelected() {
        return rdbtnPivot.isSelected();
    }

    public boolean isConnectorSelected() {
        return rdbtnConnector.isSelected();
    }

    public boolean isImageSelected() {
        return rdbtnImage.isSelected();
    }

    public boolean isLightSourceSelected() {
        return rdbtnLightSource.isSelected();
    }

    public boolean isMoveSelected() {
        return rdbtnMove.isSelected();
    }

    public boolean isMoveIndependentlySelected() {
        return rdbtnMoveIndependently.isSelected();
    }

    public boolean isDeleteSelected() {
        return rdbtnDelete.isSelected();
    }

}
