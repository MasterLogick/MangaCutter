package net.macu.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import net.macu.cutter.Cutter;
import net.macu.cutter.pasta.PastaCutter;
import net.macu.settings.L;

import javax.swing.*;
import java.awt.*;

public class MultiPageForm implements Form {
    private JTextField perfectHeightTextField;
    private JCheckBox saveGradientCheckBox;
    private JLabel toleranceStaticLabel;
    private JSlider toleranceSlider;
    private JLabel toleranceLabel;
    private JPanel form;
    private JLabel perfectHeightLabel;
    private int savedTolerance = 15;

    public MultiPageForm() {
        toleranceLabel.setText(String.format("%3d", toleranceSlider.getValue()));
        saveGradientCheckBox.addActionListener(e -> onGradientCheckBoxSwitched());
        toleranceSlider.addChangeListener(e -> toleranceLabel.setText(String.format("%3d", toleranceSlider.getValue())));
        onGradientCheckBoxSwitched();
        perfectHeightLabel.setText(L.get("UI.MultiPageForm.perfect_height_label"));
        saveGradientCheckBox.setText(L.get("UI.MultiPageForm.save_gradient"));
        toleranceStaticLabel.setText(L.get("UI.MultiPageForm.tolerance_label"));
    }

    @Override
    public boolean validateInput() {
        if (perfectHeightTextField.getText().isEmpty()) {
            ViewManager.showMessageDialog("UI.MultiPageForm.validateInput.empty_height", null);
            return false;
        }
        try {
            if (Integer.parseInt(perfectHeightTextField.getText()) < 0) {
                ViewManager.showMessageDialog("UI.MultiPageForm.validateInput.negative_height", null);
                return false;
            }
        } catch (NumberFormatException e) {
            ViewManager.showMessageDialog("UI.MultiPageForm.validateInput.nan_height", null, perfectHeightTextField.getText());
            return false;
        }
        if (saveGradientCheckBox.isSelected() && toleranceSlider.getValue() > 0) {
            ViewManager.showMessageDialog("UI.MultiPageForm.validateInput.conflicted_parameters", null);
            return false;
        }
        return true;
    }

    @Override
    public JComponent getRootComponent() {
        return $$$getRootComponent$$$();
    }

    public String getDescription() {
        return L.get("UI.MultiPageForm.description");
    }

    @Override
    public Cutter createPreparedCutter() {
        return new PastaCutter(Integer.parseInt(perfectHeightTextField.getText()),
                saveGradientCheckBox.isSelected(), toleranceSlider.getValue());
    }

    @Override
    public boolean isReturnsSingleFile() {
        return false;
    }

    private void onGradientCheckBoxSwitched() {
        if (saveGradientCheckBox.isSelected()) {
            toleranceLabel.setEnabled(false);
            toleranceSlider.setEnabled(false);
            toleranceStaticLabel.setEnabled(false);
            savedTolerance = toleranceSlider.getValue();
            toleranceSlider.setValue(0);
        } else {
            toleranceLabel.setEnabled(true);
            toleranceSlider.setEnabled(true);
            toleranceStaticLabel.setEnabled(true);
            toleranceSlider.setValue(savedTolerance);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        form = new JPanel();
        form.setLayout(new GridLayoutManager(3, 3, new Insets(3, 10, 3, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        form.add(panel1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        perfectHeightTextField = new JTextField();
        panel1.add(perfectHeightTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        perfectHeightLabel = new JLabel();
        perfectHeightLabel.setText("Perfect height:");
        panel1.add(perfectHeightLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        form.add(panel2, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        saveGradientCheckBox = new JCheckBox();
        saveGradientCheckBox.setInheritsPopupMenu(false);
        saveGradientCheckBox.setLabel("Save gradient");
        saveGradientCheckBox.setRequestFocusEnabled(true);
        saveGradientCheckBox.setRolloverEnabled(true);
        saveGradientCheckBox.setSelected(false);
        saveGradientCheckBox.setText("Save gradient");
        panel2.add(saveGradientCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        form.add(panel3, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        toleranceStaticLabel = new JLabel();
        toleranceStaticLabel.setText("Tolerance:");
        panel3.add(toleranceStaticLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toleranceSlider = new JSlider();
        toleranceSlider.setMajorTickSpacing(128);
        toleranceSlider.setMaximum(256);
        toleranceSlider.setMinorTickSpacing(1);
        toleranceSlider.setPaintLabels(true);
        toleranceSlider.setPaintTicks(true);
        toleranceSlider.setValue(20);
        toleranceSlider.setValueIsAdjusting(true);
        panel3.add(toleranceSlider, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toleranceLabel = new JLabel();
        toleranceLabel.setText("000");
        panel3.add(toleranceLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return form;
    }

}
