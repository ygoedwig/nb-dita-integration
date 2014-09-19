/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.svg.util.actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;

public final class EditSvgImageAction implements ActionListener {

    private final DataObject context;

    public EditSvgImageAction(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        try {
            String cmd = "inkscape " + context.getPrimaryFile().getPath();
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
            context.getPrimaryFile().addFileChangeListener(new FileChangeAdapter() {

                @Override
                public void fileChanged(FileEvent fe) {
                    System.out.println("changed");
                }
            });
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(ex.getMessage());

        }
    }
}
