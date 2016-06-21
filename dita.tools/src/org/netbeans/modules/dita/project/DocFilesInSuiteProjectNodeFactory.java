/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;

/**
 *
 * @author matt
 */
@NodeFactory.Registration(projectType =
        "org-netbeans-modules-apisupport-project-suite",
        position = 173)
public class DocFilesInSuiteProjectNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project p) {
        DocFilesNodeList docsNodeList = new DocFilesNodeList(p);
        p.getProjectDirectory().addFileChangeListener(
                new DocsFolderDisplayManager(docsNodeList));

        return docsNodeList;
    }
}
