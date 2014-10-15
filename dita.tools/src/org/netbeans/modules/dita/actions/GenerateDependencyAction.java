/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.actions;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author u8018815 For every selected project, the codebasename will be
 * replaced by the Module name when generating other module puml files
 */
@ActionID(id = "GenerateDependencyAction", category = "ProjectActions")
@ActionRegistration(displayName = "GenerateDependencyAction", lazy = false)
@ActionReference(path = "Projects/Actions")
public class GenerateDependencyAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private final List<Project> selectedProjects;
        private Map<String, String> moduleNames;
        private Map<String, String> codeNameBases;

        public ContextAction(Lookup context) {
            moduleNames = new HashMap<String, String>();
            codeNameBases = new HashMap<String, String>();

            selectedProjects = new ArrayList<Project>(context.lookupAll(Project.class));

            if (selectedProjects.isEmpty()) {
                return;
            }
            setEnabled(true);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            putValue(NAME, "&Generate Dependency Info");
        }

        public @Override
        void actionPerformed(ActionEvent e) {

            Map<Project, List<String>> allProjDeps = new HashMap<Project, List<String>>();

            for (Project project : selectedProjects) {
                allProjDeps.put(project, findAllDependencies(project));
            }

            Map<Project, List<String>> reversedProjDeps = getDependentModuleInfo(allProjDeps);

            for (Project project : selectedProjects) {
                try {
                    writeOutDependencyPUML(allProjDeps.get(project), project, "dependencies.puml", false);
                    writeOutDependencyPUML(reversedProjDeps.get(project), project, "dependents.puml", true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        private void writeOutDependencyPUML(List<String> deps, Project p, String fileName, boolean reverse) throws IOException {
            File docdir = new File(p.getProjectDirectory().getPath(), "docs");

            if (!docdir.exists()) {
                docdir.mkdirs();
            }

            File imagedir = new File(docdir.getPath(), "images");
            if (!imagedir.exists()) {
                imagedir.mkdirs();
            }

            File out = new File(imagedir, fileName);

            writeStateBasedPUML(deps, out, p, reverse);
        }

        private List<String> findAllDependencies(Project p) {
            List<String> res = new ArrayList<String>();
            File nbfolder = new File(p.getProjectDirectory().getPath(), "nbproject");
            if (!nbfolder.exists()) {
                return lookupLocalDependencies(p);
            }
            File projxml = new File(nbfolder.getPath(), "project.xml");
            if (!projxml.exists()) {
                return lookupLocalDependencies(p);
            }

            DOMParser parser = new DOMParser();
            try {

                parser.parse(projxml.toURI().toString());

            } catch (Exception ex) {
                return lookupLocalDependencies(p);
            }

            Document document = parser.getDocument();

            { //Add to moduleNames mapping
                NodeList dataNode = document.getElementsByTagName("data");
                NodeList childNodes = dataNode.item(0).getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    String nodeName = childNodes.item(j).getLocalName();
                    if (nodeName != null && nodeName.equals("code-name-base")) {
                        Node valNode = childNodes.item(j).getFirstChild();
                        moduleNames.put(valNode.getNodeValue(), getProjectDisplayString(p));
                        codeNameBases.put(getProjectDisplayString(p), valNode.getNodeValue());
                    }
                }
            }

            NodeList depNodes = document.getElementsByTagName("dependency");
            for (int i = 0; i < depNodes.getLength(); i++) {
                NodeList childNodes = depNodes.item(i).getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    String nodeName = childNodes.item(j).getLocalName();
                    if (nodeName != null && nodeName.equals("code-name-base")) {
                        Node valNode = childNodes.item(j).getFirstChild();
                        res.add(valNode.getNodeValue());
                    }
                }
            }

            return res;
        }

        private List<String> lookupLocalDependencies(Project p) {
            List<String> res = new ArrayList<String>();
            SubprojectProvider prov = p.getLookup().lookup(SubprojectProvider.class);

            if (prov != null) {
                for (Object o : prov.getSubprojects()) {
                    Project s = (Project) o;
                    res.add(ProjectUtils.getInformation(s).getDisplayName());
                }
            }

            return res;
        }

        private String getProjectDisplayString(Project p) {
            return ProjectUtils.getInformation(p).getDisplayName();
        }

        private String getFixedProjectDisplayString(Project p) {
            return fixUpString(getProjectDisplayString(p));
        }

        private void writeStateBasedPUML(List<String> dependencies, File out, Project p, boolean reverse) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("@startuml\n");
            sb.append("skinparam state {\n");
            sb.append("ArrowColor White\n");
            sb.append("FontName Impact\n");
            sb.append("}\n");

            sb.append("state \"");
            sb.append(getProjectDisplayString(p));
            sb.append("\" as ");
            sb.append(getFixedProjectDisplayString(p));
            sb.append(" {\n}\n\n");
            List<String> states = writeSubStates(dependencies, sb);
            writeSubStateConnections(states, p, reverse, sb);

            sb.append("@enduml\n");

            writeStringToFile(out, sb);
        }

        private List<String> writeSubStates(List<String> deps, StringBuilder b) {
            List<String> stateList = new ArrayList<String>();
            if (addState(getDependenciesOfType(deps, DependencyCategory.PHOBOS), b, "Phobos")) {
                stateList.add("Phobos");
            }

            if (addState(getDependenciesOfType(deps, DependencyCategory.NETBEANSUI), b, "NetbeansUI")) {
                stateList.add("NetbeansUI");
            }

            if (addState(getDependenciesOfType(deps, DependencyCategory.NETBEANS), b, "Netbeans")) {
                stateList.add("Netbeans");
            }

            if (addState(getDependenciesOfType(deps, DependencyCategory.OTHER), b, "Other")) {
                stateList.add("Other");
            }

            return stateList;
        }

        private List<String> getDependenciesOfType(List<String> deps, DependencyCategory type) {
            List<String> res = new ArrayList<String>();
            for (String s : deps) {
                if (getCategory(s) == type) {
                    res.add(s);
                }
            }
            return res;
        }

        private boolean addState(List<String> dependencies, StringBuilder sb, String stateDisplayName) {
            if (dependencies.isEmpty()) {
                return false;
            }
            sb.append("state \"");
            sb.append(stateDisplayName);
            sb.append("\" as ");
            sb.append(stateDisplayName);
            sb.append(" {\n");
            for (int i = 0; i < dependencies.size(); i++) {
                //We don't want more than 5 columns
                int depth = (dependencies.size() / 5) + 1;

                sb.append("   state \"");
                sb.append(getModuleName(dependencies.get(i)));
                sb.append("\" as ");
                sb.append(getFixedModuleName(dependencies.get(i)));
                sb.append("\n");

                if (i % depth != 0) {
                    sb.append("   ");
                    sb.append(getFixedModuleName(dependencies.get(i - 1)));
                    sb.append(" --> ");
                    sb.append(getFixedModuleName(dependencies.get(i)));
                    sb.append("\n");
                }
            }
            sb.append("}\n\n");
            return true;
        }

        private String fixUpString(String in) {
            return in.replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        }

        private String getCodeNameBase(Project proj) {
            return getCodeNameBase(getProjectDisplayString(proj));
        }

        private String getCodeNameBase(String moduleName) {
            if (codeNameBases.containsKey(moduleName)) {
                return codeNameBases.get(moduleName);
            }
            return moduleName;
        }

        private String getFixedModuleName(String codeBaseName) {
            return fixUpString(getModuleName(codeBaseName));
        }

        private String getModuleName(String codeBaseName) {
            if (moduleNames.containsKey(codeBaseName)) {
                return moduleNames.get(codeBaseName);
            }
            return codeBaseName;
        }

        private Map<Project, List<String>> getDependentModuleInfo(Map<Project, List<String>> allProjDeps) {
            Map<Project, List<String>> reverseDeps = new HashMap<Project, List<String>>();
            for (Project p : allProjDeps.keySet()) {
                reverseDeps.put(p, new ArrayList<String>());
                String codeNameBase = getCodeNameBase(getCodeNameBase(p));
                for (Project depProj : allProjDeps.keySet()) {
                    if (allProjDeps.get(depProj).contains(codeNameBase)) {
                        reverseDeps.get(p).add(getCodeNameBase(depProj));
                    }
                }
            }
            return reverseDeps;
        }

        private void writeSubStateConnections(List<String> states, Project p, boolean reverse, StringBuilder sb) {
            if (states.size() > 0) {
                if (reverse) {
                    sb.append(states.get(0));
                    sb.append(" --> ");
                    sb.append(getFixedProjectDisplayString(p));
                    sb.append("\n");
                } else {
                    sb.append(getFixedProjectDisplayString(p));
                    sb.append(" --> ");
                    sb.append(states.get(0));
                    sb.append("\n");
                }
            }
            for (int i = 1; i < states.size(); i++) {
                if (reverse) {
                    sb.append(states.get(i));
                    sb.append(" --> ");
                    sb.append(states.get(i - 1));
                    sb.append("\n");
                } else {
                    sb.append(states.get(i - 1));
                    sb.append(" --> ");
                    sb.append(states.get(i));
                    sb.append("\n");
                }
            }
        }

        private void writeStringToFile(File out, StringBuilder sb) throws IOException {
            if (out.exists()) {
                out.delete();
            }

            FileOutputStream fos = null;
            try {
                out.createNewFile();
                fos = new FileOutputStream(out);
                fos.write(sb.toString().getBytes());
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    private static DependencyCategory getCategory(String dep) {
        if (dep.contains("com.rr")) {
            return DependencyCategory.PHOBOS;
        }

        if (dep.contains("org.openide.windows")
                || dep.contains("org.openide.dialogs")
                || dep.contains("org.openide.awt")) {
            return DependencyCategory.NETBEANSUI;
        }

        if (dep.contains("org.openide")) {
            return DependencyCategory.NETBEANS;
        }
        return DependencyCategory.OTHER;
    }

    private enum DependencyCategory {

        PHOBOS, NETBEANS, NETBEANSUI, OTHER
    }
}
