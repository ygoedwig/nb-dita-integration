/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.dita.options.DitaPreferences;
import org.netbeans.modules.dita.project.DitaMap.Command;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;

import static org.netbeans.spi.project.ui.support.ProjectSensitiveActions.*;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author matt
 */
public class DitaMapHandler {

    public static final String DITA_ACTION_MARKER = "DITA.";
    private final FileObject projectDirectory;
    private List<DitaMap> ditaMaps;
    private BuildAllCommand buildAll;
    private List<Command> mainCommands;
    private Properties ditaProperties;

    public DitaMapHandler(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
        ditaMaps = new ArrayList<DitaMap>();
        ditaProperties = new Properties();
        mainCommands = new ArrayList<Command>();
        buildAll = new BuildAllCommand();
        mainCommands.add(buildAll);

        projectDirectory.addFileChangeListener(new DitaMapFileListener());


        loadDitaProperties();
        reloadDitaMaps();
    }

    private FileObject getDitaPropertiesFile() {
        FileObject propFile = projectDirectory.getFileObject("nbproject/dita.properties");
        if (propFile != null) {
            propFile.addFileChangeListener(new DitaPropsFileListener());
//            FileObject parent = projectDirectory.getFileObject("nbproject");
//            try {
//                propFile = parent.createData("dita", "properties");
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
        }
        return propFile;
    }

    public List<DitaMap> getDitaMaps() {
        return ditaMaps;
    }

    private void loadDitaProperties() {
        ditaProperties.clear();
        InputStream is = null;
        FileObject ditaPropertiesFile = getDitaPropertiesFile();
        if (ditaPropertiesFile == null) {
            return;
        }
        try {
            is = ditaPropertiesFile.getInputStream();
            ditaProperties.load(is);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void reloadDitaMaps() {
        ditaMaps.clear();

        FileObject[] children = projectDirectory.getChildren();
        for (FileObject child : children) {
            if (child.getExt().equalsIgnoreCase("ditamap")) {
                ditaMaps.add(new DitaMap(child, ditaProperties));
            }
        }
    }

    public void performDitaOperation(String commandId) {
        final Command command = findCommand(commandId);
        ExecutorTask task = runAntTarget(command);
        if (task == null) {
            return;
        }
        task.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(Task task) {
                command.postAnt();
            }
        });

    }

    private ExecutorTask runAntTarget(Command command) {
        Properties propsx = command.getProperties();
        if (propsx == null) {
            propsx = new Properties();
        }
        try {
            FileObject buildImpl = projectDirectory.getFileObject("build.xml");
            FileObject builddir = projectDirectory.getFileObject("build");
            if (builddir == null) {
                builddir = projectDirectory.createFolder("build");
            }
            File ditaHome = DitaPreferences.getDitaHome();
            if (ditaHome != null) {
                propsx.put("dita.dir", ditaHome.getAbsolutePath());
            }
            propsx.put("dita.temp.dir", builddir.getPath());
            return ActionUtils.runTarget(buildImpl, new String[]{command.getAntTargetName()}, propsx);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private Command findCommand(String commandId) {
        for (DitaMap ditaMap : ditaMaps) {
            for (Command command : ditaMap.getCommands()) {
                if (command.getCommandId().equals(commandId)) {
                    return command;
                }
            }
        }
        return null;
    }

    String[] getDitaCommands() {
        List<String> commandNames = new ArrayList<String>();
        commandNames.add(buildAll.getCommandId());
        for (DitaMap ditaMap : ditaMaps) {
            List<Command> commands = ditaMap.getCommands();
            for (Command command : commands) {
                commandNames.add(command.getCommandId());
            }
        }
        return commandNames.toArray(new String[0]);
    }

    boolean isEnabled(String command) {
        return true;
    }

    List<Command> getMainCommands() {
        return mainCommands;
    }

    void buildAll() {
        runAntTarget(buildAll);
    }

    class BuildAllCommand implements Command {

        @Override
        public String getCommandId() {
            return "build-all";
        }

        @Override
        public String getAntTargetName() {
            return "build-all";
        }

        @Override
        public Properties getProperties() {
            return new Properties();
        }

        @Override
        public Action getAction() {
            return projectCommandAction(getCommandId(), "Build All", null);
        }

        @Override
        public void postAnt() {
        }
    }

    class DitaMapFileListener extends FileChangeAdapter {

        private void processFileChangeEvent(FileObject file) {
            if (file.getExt().equalsIgnoreCase("ditamap")) {
                reloadDitaMaps();
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            processFileChangeEvent(fe.getFile());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            processFileChangeEvent(fe.getFile());
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            processFileChangeEvent(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fre) {
            processFileChangeEvent(fre.getFile());
        }
    }

    class DitaPropsFileListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            loadDitaProperties();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            loadDitaProperties();
        }
    }
}
