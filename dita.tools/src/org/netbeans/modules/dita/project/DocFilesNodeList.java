/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 *
 * @author matt
 */
public class DocFilesNodeList implements NodeList<FileObject> {

    private static final String DOCS_FOLDERNAME = "docs";
    private ChangeSupport changeSupport;
    private final Project project;

    DocFilesNodeList(Project project) {
        this.project = project;
        changeSupport = new ChangeSupport(this);
    }

    @Override
    public List<FileObject> keys() {
        FileObject prjDir = project.getProjectDirectory();
        FileObject resDir = prjDir.getFileObject(DOCS_FOLDERNAME);
        if (resDir == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(resDir);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public Node node(FileObject key) {
        DataFolder folder = DataFolder.findFolder(key);
        Node folderNode = folder.getNodeDelegate();

        return new DocsFolderNode(folderNode);
    }

    @Override
    public void addNotify() {
    }

    @Override
    public void removeNotify() {
    }

    void updateFromEventThread() {
        changeSupport.fireChange();
    }
}
