/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iconeditor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;

public final class EditSvgImageAction implements ActionListener {

    private final DataObject context;

    public EditSvgImageAction(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        try {
            String cmd = "inkscape " + context.getPrimaryFile().getPath() + " &";
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(ex.getMessage());
        }
    }
}
