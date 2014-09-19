/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.project;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.Action;
import org.netbeans.api.javahelp.Help;
import org.netbeans.modules.dita.help.DynamicHelpContent;
import org.openide.filesystems.FileObject;
import static org.netbeans.spi.project.ui.support.ProjectSensitiveActions.*;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author matt
 */
public class DitaMap {

    private String DEFAULT_FORMAT = "pdf";
    private final FileObject ditaMapFile;
    private final Properties ditaProperties;
    private List<Command> commands;

    public DitaMap(FileObject ditaMapFile, Properties ditaProperties) {
        this.ditaMapFile = ditaMapFile;
        this.ditaProperties = ditaProperties;
        commands = new ArrayList<Command>();
        commands.add(new BuildSingleCommand("BUILD", "Build"));
        if ("javahelp".equalsIgnoreCase(getFormat())) {
            //commands.add(new BuildTestJavaHelpCommand("TEST", "Test"));
        }
    }

    public FileObject getDitaMapFile() {
        return ditaMapFile;
    }

    public List<Command> getCommands() {
        return commands;
    }

    private String getFormat() {
        return ditaProperties.getProperty(getDitaMapFile().getName() + ".format", DEFAULT_FORMAT);
    }

    private String getDisplayName() {
        String mapFileName = getDitaMapFile().getName();
        return ditaProperties.getProperty(mapFileName + ".name", mapFileName);
    }
//
//    public List<Action> getActions() {
//        List<Action> actions = new ArrayList<Action>();
//        String ditaMapName = ditaMapFile.getName();
//        buildCommandId = DitaMapHandler.DITA_ACTION_MARKER + "BUILD." + ditaMapName;
//        actions.add(projectCommandAction(buildCommandId, "Build " + ditaMapName, null));
//        testCommandId = DitaMapHandler.DITA_ACTION_MARKER + "TEST." + ditaMapName;
//        actions.add(projectCommandAction(DitaMapHandler.DITA_ACTION_MARKER, "Test " + ditaMapName, null));
//        return actions;
//    }

    public interface Command {

        String getAntTargetName();

        String getCommandId();

        Action getAction();

        Properties getProperties();

        void postAnt();
    }

    public class BuildSingleCommand implements Command {

        String command;
        String commandDisplayName;

        public BuildSingleCommand(String commandType, String commandDisplayName) {
            this.command = DitaMapHandler.DITA_ACTION_MARKER + commandType + ".";
            this.commandDisplayName = commandDisplayName;
        }

        @Override
        public String getAntTargetName() {
            if ("javahelp".equalsIgnoreCase(getFormat())) {
                return "build-userdocs";
            }
            return "build-single";
        }

        @Override
        public String getCommandId() {
            return command + ditaMapFile.getName();
        }

        @Override
        public Action getAction() {
            return projectCommandAction(getCommandId(), commandDisplayName + " " + getDisplayName(), null);
        }

        public DitaMap getDitaMap() {
            return DitaMap.this;
        }

        @Override
        public Properties getProperties() {
            Properties props = new Properties();
            props.put("ditamapfile", getDitaMapFile().getPath());
            props.put("ditamap.basename", getDitaMapFile().getName());
            props.put("format", getFormat());
            return props;
        }

        @Override
        public void postAnt() {
        }
    }

    class BuildTestJavaHelpCommand extends BuildSingleCommand {

        public BuildTestJavaHelpCommand(String commandType, String commandDisplayName) {
            super(commandType, commandDisplayName);
        }

        //@Override
        public void postAntx() {

            Help help = Lookup.getDefault().lookup(Help.class);
            DynamicHelpContent.disable();
            help.showHelp(HelpCtx.DEFAULT_HELP);
            //runAntTarget("javahelp", null);
            FileObject helpSet = ditaMapFile.getFileObject("../build/javahelp/" + getDitaMapFile().getName() + "_helpset.hs");
            if (helpSet != null) {
                try {
                    showHelpSet(help, helpSet.getURL());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        private void showHelpSet(Help help, URL helpSetUrl) throws IOException {
            DynamicHelpContent.enable(helpSetUrl);
            String id = "tba_technical";

            if (help != null && help.isValidID(id, true)) {
                help.showHelp(new HelpCtx(id));
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}
