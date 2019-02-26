package ru.uniteller.phpstorm.plugin.ts.ui;


import com.intellij.ide.util.treeView.IndexComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.treeStructure.*;
import org.jetbrains.annotations.NotNull;
import ru.uniteller.phpstorm.plugin.ts.service.Config;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.NamedNode;
import ru.uniteller.phpstorm.plugin.ts.ui.subjectTree.RootNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


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

    SimpleTreeBuilder getTreeBuilder() {
        return myTreeBuilder;
    }


    static <T extends SimpleNode> List<T> getSelectedNodes(SimpleTree tree, Class<T> nodeClass) {
        final List<T> filtered = new ArrayList<>();
        for (SimpleNode node : getSelectedNodes(tree)) {
            if ((nodeClass != null) && (!nodeClass.isInstance(node))) {
                filtered.clear();
                break;
            }
            //noinspection unchecked
            filtered.add((T)node);
        }
        return filtered;
    }


    private static List<SimpleNode> getSelectedNodes(SimpleTree tree) {
        List<SimpleNode> nodes = new ArrayList<>();
        TreePath[] treePaths = tree.getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                nodes.add(tree.getNodeFor(treePath));
            }
        }
        return nodes;
    }
}
