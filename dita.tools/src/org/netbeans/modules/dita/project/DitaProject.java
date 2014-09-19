/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author u415714
 */
@AntBasedProjectRegistration(type = "org.netbeans.modules.dita.project.DitaProject",
iconResource = "org/netbeans/modules/dita/resources/dita.png",
sharedName = "data",
sharedNamespace = "http://www.netbeans.org/ns/dita-project/1",
privateName = "project-private",
privateNamespace = "http://www.netbeans.org/ns/dita-project-private/1")
public class DitaProject implements Project {

    private AntProjectHelper helper;
    private final ProjectState state;
    private Lookup lkp;
    private DitaMapHandler ditaMapHandler;

    public DitaProject(AntProjectHelper helper) {
        this.helper = helper;
        this.state = null;
        ditaMapHandler = new DitaMapHandler(helper.getProjectDirectory());
    }

    public DitaProject(ProjectState state) {
        this.state = state;
    }

    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public DitaMapHandler getDitaMapHandler() {
        return ditaMapHandler;
    }

    FileObject getTextFolder(boolean create) {
        FileObject result =
                getProjectDirectory().getFileObject("userdocs");
        if (result == null && create) {
            try {
                result = getProjectDirectory().createFolder("userdocs");
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }

    //The project type's capabilities are registered in the project's lookup:
    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[]{
                        // state, //allow outside code to mark the project as needing saving
                        new ActionProviderImpl(), //Provides standard actions like Build and Clean
                        new DitaDeleteOperation(),
                        new DitaCopyOperation(this),
                        new Info(), //Project information implementation
                        new DitaProjectLogicalView(this), //Logical view of project implementation
                        new DitaCustomizerProvider(this),});
        }
        return lkp;
    }

    private final class ActionProviderImpl implements ActionProvider {

        @Override
        public String[] getSupportedActions() {
            String[] ditaCommands = DitaProject.this.getDitaMapHandler().getDitaCommands();
            String[] commands = new String[ditaCommands.length + 2];
            commands[0] = ActionProvider.COMMAND_DELETE;
            commands[1] = ActionProvider.COMMAND_COPY;
            System.arraycopy(ditaCommands, 0, commands, 2, ditaCommands.length);
            return commands;
        }

        @Override
        public void invokeAction(String command, Lookup lookup) throws IllegalArgumentException {
            if (command.equalsIgnoreCase(ActionProvider.COMMAND_DELETE)) {
                DefaultProjectOperations.performDefaultDeleteOperation(DitaProject.this);
            } else if (command.equalsIgnoreCase(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(DitaProject.this);
            } else if (command.startsWith(DitaMapHandler.DITA_ACTION_MARKER)) {
                DitaProject.this.getDitaMapHandler().performDitaOperation(command);
            } else if (command.equalsIgnoreCase("build-all")) {
                DitaProject.this.getDitaMapHandler().buildAll();
            }
        }

        @Override
        public boolean isActionEnabled(String command, Lookup lookup) throws IllegalArgumentException {
            if ((command.equals(ActionProvider.COMMAND_DELETE))) {
                return true;
            } else if ((command.equals(ActionProvider.COMMAND_COPY))) {
                return true;
            } else if (command.startsWith(DitaMapHandler.DITA_ACTION_MARKER)) {
                return ditaMapHandler.isEnabled(command);
            } else if (command.equalsIgnoreCase("build-all")) {
                return true;
            } else {
                throw new IllegalArgumentException(command);
            }
        }
    }

    private final class DitaDeleteOperation implements DeleteOperationImplementation {

        public void notifyDeleting() throws IOException {
        }

        public void notifyDeleted() throws IOException {
        }

        public List<FileObject> getMetadataFiles() {
            List<FileObject> dataFiles = new ArrayList<FileObject>();
            return dataFiles;
        }

        public List<FileObject> getDataFiles() {
            List<FileObject> dataFiles = new ArrayList<FileObject>();
            return dataFiles;
        }
    }

    private final class DitaCopyOperation implements CopyOperationImplementation {

        private final DitaProject project;
        private final FileObject projectDir;

        public DitaCopyOperation(DitaProject project) {
            this.project = project;
            this.projectDir = project.getProjectDirectory();
        }

        public List<FileObject> getMetadataFiles() {
            return Collections.<FileObject>emptyList();
        }

        public List<FileObject> getDataFiles() {
            return Collections.<FileObject>emptyList();
        }

        public void notifyCopying() throws IOException {
        }

        public void notifyCopied(Project arg0, File arg1, String arg2) throws IOException {
        }
    }

    private final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(
                    "org/netbeans/modules/dita/resources/dita.png"));
        }

        @Override
        public String getName() {
            return helper.getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return DitaProject.this;
        }
    }
}
