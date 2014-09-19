/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iconeditor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.modules.iconeditor.options.ImageEditorPreferences;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public final class EditImageAction implements ActionListener {

    private final DataObject context;

    public EditImageAction(DataObject context) {
        this.context = context;
    }

    private FileObject getDelegate(FileObject fileObject) {
        String name = fileObject.getName();

        DelegateImageEditor delelegate = Lookup.getDefault().lookup(DelegateImageEditor.class);

        if (!name.endsWith("-svg") || delelegate == null) {
            return fileObject;
        }
        SizeArgsParser sizeArgsParser = new SizeArgsParser();
        sizeArgsParser.parseName(name);
        FileObject parent = fileObject.getParent();
        FileObject folder = parent.getFileObject("svg");
        String newName = sizeArgsParser.getBaseName();
        FileObject svgFile = folder.getFileObject(newName, "svg");
        if (svgFile.isData()) {
            return svgFile;
        }
        return fileObject;
    }

    public void actionPerformed(ActionEvent ev) {
        try {
            final FileObject original = context.getPrimaryFile();
            final FileObject delegate = getDelegate(original);
            if (2 == 1) {
                return;
            }
            if(original.isFolder()) {
                return;
            }
            String editorCmd = null;
            if (delegate.getExt().equalsIgnoreCase("svg")) {
                editorCmd = ImageEditorPreferences.getSvgEditor();
            }
            if (editorCmd == null) {
                editorCmd = ImageEditorPreferences.getOtherEditor();
            }
            String cmd;
            if (Utilities.isUnix()) {
                cmd = editorCmd + " " + delegate.getPath();
            } else {
                String path = delegate.getPath();
                path = path.replace('/', '\\');
                cmd = editorCmd + " \"" + path + "\"";
            }
            System.out.println(cmd);
            Runtime.getRuntime().exec(cmd);
            if (original == delegate) {
                return;
            }
            final DelegateImageEditor delegateIO = Lookup.getDefault().lookup(DelegateImageEditor.class);
            delegate.addFileChangeListener(new FileChangeAdapter() {

                @Override
                public void fileChanged(FileEvent fe) {
                    try {
                        delegateIO.convertImage(delegate, original);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });


        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(ex.getMessage());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
