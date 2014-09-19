/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.svg.util.type;

import java.io.IOException;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

/**
 *
 * @author u415714
 */
public class EditSvg implements EditCookie, OpenCookie {

    private final FileObject fileObj;

    EditSvg(FileObject fileObj) {
        this.fileObj = fileObj;
    }

    @Override
    public void open() {
        edit();
    }

    @Override
    public void edit() {
        System.out.println(fileObj);
        try {
            String cmd = "inkscape " + fileObj.getPath();
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
            fileObj.addFileChangeListener(new FileChangeAdapter() {
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
