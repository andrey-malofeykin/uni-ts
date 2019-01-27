package ru.uniteller.phpstorm.plugin.ts.component;

import com.intellij.openapi.components.ProjectComponent ;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.util.Starter;
import ru.uniteller.phpstorm.plugin.ts.ui.TestStandPanel;

public class TestStandComponent implements ProjectComponent {
    private Project project;

    public TestStandComponent(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
        Starter.runWhenInitialized(project, this::initToolWindow);
    }

    private void initToolWindow() {
        Config config = ServiceManager.getService(Config.class);
        TestStandPanel panel = new TestStandPanel(project, config);

        final ToolWindowManagerEx manager = ToolWindowManagerEx.getInstanceEx(project);
        ToolWindowEx myToolWindow = (ToolWindowEx) manager.registerToolWindow(TestStandPanel.ID, false, ToolWindowAnchor.LEFT, project, true);

        //myToolWindow.setIcon(IconLoader.findIcon("/icons/jesterhat.png"));
        final ContentFactory contentFactory = ServiceManager.getService(ContentFactory.class);
        final Content content = contentFactory.createContent(panel, "", false);
        ContentManager contentManager = myToolWindow.getContentManager();
        contentManager.addContent(content);
    }
}
