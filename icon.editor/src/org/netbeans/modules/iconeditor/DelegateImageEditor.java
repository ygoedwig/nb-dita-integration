/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iconeditor;

import org.openide.filesystems.FileObject;

/**
 *
 * @author matt
 */
public interface DelegateImageEditor {

    void convertImage(FileObject master, FileObject actual) throws Exception;
}
