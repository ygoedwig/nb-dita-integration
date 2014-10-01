/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author matt
 */
public class DocsFolderNode extends FilterNode {

    public DocsFolderNode(Node orig) {
        super(orig);
    }

    @Override
    public String getDisplayName() {
        return "Dita Docs";
    }

    @Override
    public Action[] getActions(boolean context) {
        
        return super.getActions(context);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/dita/resources/dita.png");
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
}
