/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.catalog;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

/**
 *
 * @author matt
 */
//@ServiceProvider(service = FileSystem.class)
public class DynamicLayerContent extends MultiFileSystem {

    private static DynamicLayerContent INSTANCE;
    public static FileSystem dynamic;
    private static int counter = 1;

    public DynamicLayerContent() {
        // will be created on startup, exactly once
        INSTANCE = this;
        setPropagateMasks(true); // permit *_hidden masks to be used
        dynamic = FileUtil.createMemoryFileSystem();
        //FileUtil.getConfigRoot().createFolder
        try {
            FileObject fo = dynamic.getRoot().createFolder("xml").createFolder("endities");
            fo = fo.createFolder("DITA_Catalog");
            fo = fo.createData("DTD_DITA_Topic");

            //fo = fo.createData("-//OASIS//DTD DITA Topic//EN");
            File f = new File("/opt/DITA-OT1.7.3/catalog-dita.xml");
            f = new File("/opt/DITA-OT1.7.3/dtd/base/dtd/topic.mod");
            fo.setAttribute("url", f.toURI().toURL());

            fo = dynamic.getRoot().createFolder("Toolbars").createFolder("XXX");
            fo.setAttribute("position", 1239);
            fo = fo.createData("action.shadow");
            fo.setAttribute("originalFile", "Actions/Tools/org-netbeans-modules-dita-catalog-DITAAction.instance");
            fo.setAttribute("position", 19);
           

        } catch (IOException ioe) {
        }
    }

    static boolean hasContent() {
        return INSTANCE.getDelegates().length > 0;
    }

    static void enable() {
        if (!hasContent()) {
            try {

                INSTANCE.setDelegates(new XMLFileSystem(
                        DynamicLayerContent.class.getResource(
                        "dynamicContent.xml")), dynamic);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    static void disable() {
        INSTANCE.setDelegates();
    }
}
