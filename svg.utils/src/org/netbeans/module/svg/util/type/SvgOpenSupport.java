/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.svg.util.type;

import java.io.IOException;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;

/**
 *
 * @author matt
 */
public class SvgOpenSupport implements OpenCookie {

    private final DataObject svgFile;

    public SvgOpenSupport(DataObject svgFile) {
        this.svgFile = svgFile;
    }

    public void open() {
        try {
            String cmd = "inkscape " + svgFile.getPrimaryFile().getPath();
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(ex.getMessage());

        }
    }
}
