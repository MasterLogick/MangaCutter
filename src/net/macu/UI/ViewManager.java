package net.macu.UI;

import net.macu.core.JobManager;
import net.macu.core.Main;
import net.macu.service.ServiceManager;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class ViewManager {
    private static JFrame frame = null;
    private static MainView view;
    private static JFileChooser singleFileChooser;
    private static JFileChooser dirChooser;
    private static SettingsFrame settingsFrame;

    public static void startProgress(int max, String message) {
        view.startProgress(max, message);
    }

    public static void incrementProgress(String message) {
        view.incrementProgress(message);
    }

    public static void resetProgress() {
        view.resetProgress();
    }

    public static void showMessageDialog(String s) {
        if (!s.startsWith("<html>")) {
            s = "<html>" + s.replaceAll("\n", "<br>") + "</html>";
        }
        JEditorPane ep = new JEditorPane("text/html", s);
        ep.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        ep.setEditable(false);
        ep.setBackground(new JLabel().getBackground());
        JOptionPane.showMessageDialog(frame, ep);
    }

    public static boolean showConfirmDialog(String s) {
        return JOptionPane.showConfirmDialog(frame, s, "Внимание!", JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    public static void createView() {
        constructFileChoosers();
        constructFrame();
        settingsFrame = new SettingsFrame();
        frame.setJMenuBar(constructMenu());
        view = new MainView();
        frame.setContentPane(view.$$$getRootComponent$$$());
        frame.pack();
        frame.setVisible(true);
    }

    public static String requestSelectSingleFile(String approveButtonText) {
        String path = null;
        if (singleFileChooser.showDialog(frame, approveButtonText) == JFileChooser.APPROVE_OPTION) {
            path = singleFileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".png")) {
                path += ".png";
            }
        }
        return path;
    }

    public static String requestSelectDir(String approveButtonText) {
        if (dirChooser.showDialog(frame, approveButtonText) == JFileChooser.APPROVE_OPTION) {
            return dirChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public static void packFrame() {
        frame.pack();
    }

    public static JFrame getFrame() {
        return frame;
    }

    private static void constructFileChoosers() {
        singleFileChooser = new JFileChooser();
        singleFileChooser.setDialogTitle("Куда сохранить?");
        singleFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        singleFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        singleFileChooser.setAcceptAllFileFilterUsed(false);
        singleFileChooser.setMultiSelectionEnabled(false);
        singleFileChooser.setFileSystemView(FileSystemView.getFileSystemView());
        singleFileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG file";
            }
        });
        dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("Куда сохранить?");
        dirChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setAcceptAllFileFilterUsed(false);
        dirChooser.setMultiSelectionEnabled(false);
        dirChooser.setFileSystemView(FileSystemView.getFileSystemView());
    }

    private static void constructFrame() {
        if (frame == null) {
            frame = new JFrame("MaCu");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    JobManager.cancel();
                    frame.dispose();
                    System.exit(0);
                }
            });
        }
    }

    private static JMenuBar constructMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu helpManu = new JMenu("Справка");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(actionEvent -> ViewManager.showMessageDialog(
                "MaCu - MangaCutter\n" +
                        "Программа для скачивания и склейки сканов с корейского вебтуна\n" +
                        "Автор: MasterLogick\n" +
                        Main.getVersion() + "\n" +
                        "<a href=\"https://github.com/MasterLogick/MangaCutter\">https://github.com/MasterLogick/MangaCutter</a>"));

        JMenuItem supportedServicesItem = new JMenuItem("Поддерживаемые сервисы");
        supportedServicesItem.addActionListener(actionEvent -> ViewManager.showMessageDialog("Список поддерживаемых сервисов:\n" + ServiceManager.getSupportedServicesList()));
        helpManu.add(aboutItem);
        helpManu.add(supportedServicesItem);
        bar.add(helpManu);
        JMenuItem settingsMenu = new JMenuItem("Настройки");
        settingsMenu.addActionListener(e -> {
            settingsFrame.setVisible(true);
        });
        bar.add(settingsMenu);
        return bar;
    }
}
