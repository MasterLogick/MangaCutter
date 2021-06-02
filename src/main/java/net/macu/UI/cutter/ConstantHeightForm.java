package net.macu.UI.cutter;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import net.macu.UI.Form;
import net.macu.UI.ViewManager;
import net.macu.settings.L;

import javax.swing.*;
import java.awt.*;

public class ConstantHeightForm implements Form {
    private JLabel heightLabel;
    private JTextField heightTextField;
    private JPanel root;

    public ConstantHeightForm() {
        heightLabel.setText(L.get("UI.cutter.ConstantHeightForm.height_label"));
    }

    public int getImgHeight() {
        return Integer.parseInt(heightTextField.getText());
    }

    @Override
    public boolean validateInput() {
        try {
            if (Integer.parseInt(heightTextField.getText()) <= 0) {
                ViewManager.showMessageDialog("UI.cutter.ConstantHeightForm.validateInput.positive_height", null);
            }
        } catch (NumberFormatException e) {
            ViewManager.showMessageDialog("UI.cutter.ConstantHeightForm.validateInput.nan_height", null);
        }
        return true;
    }

    @Override
    public JComponent getRootComponent() {
        return $$$getRootComponent$$$();
    }

    @Override
    public String getName() {
        return L.get("UI.cutter.ConstantHeightForm.description");
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
        root = new JPanel();
        root.setLayout(new GridLayoutManager(1, 1, new Insets(3, 0, 3, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        heightLabel = new JLabel();
        heightLabel.setText("Height:");
        panel1.add(heightLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        heightTextField = new JTextField();
        panel1.add(heightTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }

}
