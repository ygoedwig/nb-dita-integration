/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.svg.util.type;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;

import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class SvgFileDataObject extends MultiDataObject {
    
    public SvgFileDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        getCookieSet().add(new EditSvg(pf));
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
