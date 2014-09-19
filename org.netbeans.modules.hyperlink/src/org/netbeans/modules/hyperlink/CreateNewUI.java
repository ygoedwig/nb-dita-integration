/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CreateNewUI.java
 *
 * Created on 11-May-2013, 20:51:33
 */
package org.netbeans.modules.hyperlink;

import java.io.File;
import java.io.IOException;
import javax.swing.DefaultListModel;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author matt
 */
public class CreateNewUI extends javax.swing.JPanel implements ExplorerManager.Provider {

    private File newFile;
    private ExplorerManager explorerManager;

    /**
     * Creates new form CreateNewUI
     */
    public CreateNewUI() {
        initComponents();
        explorerManager = new ExplorerManager();
    }

    public CreateNewUI(File newFile) {
        this();
        this.newFile = newFile;

        FileObject fos = FileUtil.getConfigFile(getTemplateFolder(newFile.getName()));

        DefaultListModel model = new DefaultListModel();
        try {
            DataObject dataObject = DataObject.find(fos);
            explorerManager.setRootContext(dataObject.getNodeDelegate());
        } catch (DataObjectNotFoundException dataObjectNotFoundException) {
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    private String getTemplateFolder(String nameExt) {
        if (nameExt.endsWith(".dita") || nameExt.endsWith(".ditamap")) {
            return "Templates/DITA";
        } else if (nameExt.endsWith(".png")) {
            return "Templates/Icons";
        } else {
            return "Templates";
        }
    }

    private FileObject mkdirs(File directory) throws IOException {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(directory));
        if (fo == null) {
            File parent = directory.getParentFile();
            fo = mkdirs(parent);
            fo = fo.createFolder(directory.getName());
        }
        return fo;
    }

    void create() {
        Node[] nodes = explorerManager.getSelectedNodes();
        Node node = nodes[0];
        FileObject selectedTemplate = node.getLookup().lookup(FileObject.class);
        File parent = newFile.getParentFile();
        FileObject parentFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(parent));
        try {
            if (parentFileObject == null) {
                parentFileObject = mkdirs(parent);
            }

            String name = newFile.getName();
            String ext = "";
            int indexExt = name.lastIndexOf('.');
            if (indexExt != -1) {
                name = name.substring(0, indexExt);
                ext = newFile.getName().substring(indexExt + 1);
            }
            //FileObject fob = parentFileObject.createData(name, ext);
            selectedTemplate.copy(parentFileObject, name, ext);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        beanTreeView1 = new org.openide.explorer.view.BeanTreeView();

        jLabel1.setText("Select the DITA template to use.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(beanTreeView1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(beanTreeView1, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.BeanTreeView beanTreeView1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
