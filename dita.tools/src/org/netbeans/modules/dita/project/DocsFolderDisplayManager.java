/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import javax.swing.SwingUtilities;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;

/**
 *
 * @author matt
 */
public class DocsFolderDisplayManager extends FileChangeAdapter {

    private final DocFilesNodeList docsNodeList;
    private Updater updater;

    DocsFolderDisplayManager(DocFilesNodeList docsNodeList) {
        this.docsNodeList = docsNodeList;
        updater = new Updater();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        updater.update();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        updater.update();
    }

    private class Updater implements Runnable {

        private boolean dirty = false;

        @Override
        public void run() {
            docsNodeList.updateFromEventThread();
        }

        public void update() {
            if (dirty) {
                return;
            }
            dirty = false;
            SwingUtilities.invokeLater(this);
        }
    }
}
