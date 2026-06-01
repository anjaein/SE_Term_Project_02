package ui.swing;

import com.issuetracker.Main;
import com.issuetracker.global.common.Backend;

import javax.swing.*;

public class SwingMain {
    public static void main(String[] args) {
        Backend backend = Main.createBackend();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainFrame mainFrame = new MainFrame(
                    backend.accountController,
                    backend.projectController,
                    backend.issueController,
                    backend.commentController,
                    backend.issueStatisticsController,
                    backend.sessionManager,
                    backend.recommendController
            );
            mainFrame.setVisible(true);
        });
    }
}
