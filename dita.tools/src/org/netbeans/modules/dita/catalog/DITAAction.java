/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.catalog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public final class DITAAction implements ActionListener {

    public static int counter = 1;
    public static FileObject fo;

    public void actionPerformed(ActionEvent e) {


        if (DynamicLayerContent.hasContent()) {
            DynamicLayerContent.disable();
            if (fo != null) {
                try {
                    fo.delete();
                    fo = null;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            toggle();
            DynamicLayerContent.enable();
        }
    }

    private void toggle() {
        try {
            fo = DynamicLayerContent.dynamic.getRoot().getFileObject("Services/JavaHelp");
            if (fo == null) {
                fo = DynamicLayerContent.dynamic.getRoot().createFolder("Services").createFolder("JavaHelp");
            }
            fo = fo.createData("dita-helpset" + (counter++) + ".xml");
            File f = new File("/home/matt/java/dita/dita.tools/src/org/netbeans/modules/dita/catalog/dita-helpset.xml");
            fo.setAttribute("url", f.toURI().toURL());
            fo.setAttribute("position", 30);
        } catch (IOException iOException) {
        }
    }
}
