package ru.uniteller.phpstorm.plugin.ts.ui;


import com.intellij.ide.util.treeView.IndexComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.NamedNode;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.RootNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.Comparator;


public class SubjectsTreeStructure extends SimpleTreeStructure {
    private final RootNode myRoot;
    private final SimpleTreeBuilder myTreeBuilder;

    SubjectsTreeStructure(Project project, Config config, JTree tree) {
        this.myRoot = new RootNode(project, config);
        myTreeBuilder = new SimpleTreeBuilder(tree, (DefaultTreeModel) tree.getModel(), this, (Comparator<SimpleNode>) (o1, o2) -> {
            if(o1 instanceof NamedNode && o2 instanceof NamedNode)
                return o1.getName().compareTo(o2.getName());
            else
                return IndexComparator.INSTANCE.compare(o1, o2);
        });
        Disposer.register(project, myTreeBuilder);
        myTreeBuilder.initRoot();
        myTreeBuilder.expand(myRoot, null);
    }

    public void updateFromRoot(){
        myTreeBuilder.addSubtreeToUpdateByElement(myRoot);
    }

    @NotNull
    @Override
    public Object getRootElement() {
        return myRoot;
    }


}
