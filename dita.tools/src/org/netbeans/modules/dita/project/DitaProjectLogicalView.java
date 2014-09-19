/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.dita.project.DitaMap.Command;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author u415714
 */
public class DitaProjectLogicalView implements LogicalViewProvider {

    private final DitaProject project;

    public DitaProjectLogicalView(DitaProject project) {
        this.project = project;

    }

    @Override
    public org.openide.nodes.Node createLogicalView() {
        try {
            //Get the DataObject that represents it
            DataFolder rootDataObject = DataFolder.findFolder(project.getProjectDirectory());

            //Get its default node-we'll wrap our node around it to change the
            //display name, icon, etc
            Node rootNode = rootDataObject.getNodeDelegate();

            //This FilterNode will be our project node
            return new DitaNode(rootNode, project);

        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
            //Fallback-the directory couldn't be created -
            //read-only filesystem or something evil happened
            return new AbstractNode(Children.LEAF);
        }
    }

    /**
     * This is the node you actually see in the project tab for the project
     */
    private static final class DitaNode extends FilterNode {

        final DitaProject project;

        public DitaNode(Node node, DitaProject project) throws DataObjectNotFoundException {
            super(node, new DitaChildren(node),
                    //The projects system wants the project in the Node's lookup.
                    //NewAction and friends want the original Node's lookup.
                    //Make a merge of both
                    new ProxyLookup(new Lookup[]{Lookups.singleton(project),
                        node.getLookup()
                    }));
            this.project = project;
        }

        @Override
        public Action[] getActions(boolean arg0) {
            List<Action> actions = new ArrayList<Action>();
            actions.add(CommonProjectActions.newFileAction());

            List<DitaMap> ditaMaps = project.getDitaMapHandler().getDitaMaps();

            List<Command> allCommands = project.getDitaMapHandler().getMainCommands();
            for (Command command : allCommands) {
                actions.add(command.getAction());
            }
            actions.add(null);
            for (DitaMap ditaMap : ditaMaps) {
                List<Command> commands = ditaMap.getCommands();
                for (Command command : commands) {
                    actions.add(command.getAction());
                }
                actions.add(null);
            }
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[0]);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/modules/dita/resources/dita.png");
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return "Documentation " + project.getProjectDirectory().getName();
        }
    }

    private static class DitaChildren extends FilterNode.Children {

        public DitaChildren(Node node) {
            super(node);
        }

        @Override
        protected Node copyNode(Node arg0) {
            Node n = super.copyNode(arg0);

            return n;
        }

        @Override
        protected Node[] createNodes(Node node) {
            if ("build".equalsIgnoreCase(node.getName())) {
                return null;
            }
            if ("nbproject".equalsIgnoreCase(node.getName())) {
                return null;
            }
            if (".svn".equalsIgnoreCase(node.getName())) {
                return null;
            }
            return new Node[]{new FilterNode(node, new DitaChildren(node))};
        }
    }

    @Override
    public Node findPath(Node root, Object target) {
        //leave unimplemented for now
        return null;
    }
}
