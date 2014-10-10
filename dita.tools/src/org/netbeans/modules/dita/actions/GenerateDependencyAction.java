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
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
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

        private final List<Project> p;
        private Map<String, String> moduleNames;

        public ContextAction(Lookup context) {
            moduleNames = new HashMap<String, String>();
            p = new ArrayList<Project>(context.lookupAll(Project.class));

            if (p.isEmpty()) {
                return;
            }
            setEnabled(true);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            putValue(NAME, "&Generate Dependency Info ");
        }

        public @Override
        void actionPerformed(ActionEvent e) {

            Map<Project, List<String>> allProjDeps = new HashMap<Project, List<String>>();

            for (Project basep : p) {
                List<String> deps = findAllDependencies(basep);
                allProjDeps.put(basep, deps);
            }

            for (Project basep : p) {
                try {
                    writeOutDependencyPUML(allProjDeps.get(basep), basep);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        private void writeOutDependencyPUML(List<String> deps, Project p) throws IOException {
            File docdir = new File(p.getProjectDirectory().getPath(), "docs");

            if (!docdir.exists()) {
                docdir.mkdirs();
            }

            File imagedir = new File(docdir.getPath(), "images");
            if (!imagedir.exists()) {
                imagedir.mkdirs();
            }

            File out = new File(imagedir, "dependencies.puml");
            StringBuilder b = new StringBuilder();

            writeStateOut(deps, b, out, p);
            //openFileInEditor(out);
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
                        moduleNames.put(valNode.getNodeValue(), ProjectUtils.getInformation(p).getDisplayName());
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

        private void openFileInEditor(File out) {
            FileObject item = FileUtil.toFileObject(out);
            DataObject dao;
            try {
                dao = DataObject.find(item);
            } catch (DataObjectNotFoundException ex) {
                return;
            }
            LineCookie lc = dao.getCookie(LineCookie.class);
            lc.getLineSet().getCurrent(0).show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FRONT);
        }

        private void writeStateOut(List<String> deps, StringBuilder b, File out, Project p) throws IOException {
            b.append("@startuml\n");
            b.append("skinparam state {\n");
            b.append("ArrowColor White\n");
            b.append("FontName Impact\n");
            b.append("}\n");

            b.append("state \"" + ProjectUtils.getInformation(p).getDisplayName() + "\" as " + fixUpString(ProjectUtils.getInformation(p).getDisplayName()) + " {\n}\n\n");
            List<String> states = addStates(deps, b);
            if (states.size() > 0) {
                b.append(fixUpString(ProjectUtils.getInformation(p).getDisplayName()) + " --> " + states.get(0) + "\n");
            }
            for (int i = 1; i < states.size(); i++) {
                b.append(states.get(i - 1) + " --> " + states.get(i) + "\n");
            }

            b.append("@enduml\n");
            if (out.exists()) {
                out.delete();
            }

            FileOutputStream fos = null;
            try {
                out.createNewFile();
                fos = new FileOutputStream(out);
                fos.write(b.toString().getBytes());
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }

        private List<String> addStates(List<String> deps, StringBuilder b) {
            List<String> stateList = new ArrayList<String>();
            if (addState(deps, b, DependencyCategory.PHOBOS, "Phobos")) {
                stateList.add("Phobos");
            }

            if (addState(deps, b, DependencyCategory.NETBEANSUI, "NetbeansUI")) {
                stateList.add("NetbeansUI");
            }

            if (addState(deps, b, DependencyCategory.NETBEANS, "Netbeans")) {
                stateList.add("Netbeans");
            }

            if (addState(deps, b, DependencyCategory.OTHER, "Other")) {
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

        private boolean addState(List<String> deps, StringBuilder b, DependencyCategory dependencyCategory, String displayName) {
            List<String> catDeps = getDependenciesOfType(deps, dependencyCategory);

            if (catDeps.isEmpty()) {
                return false;
            }
            b.append("state \"" + displayName + "\" as " + displayName + " {\n");
            for (int i = 0; i < catDeps.size(); i++) {
                //We don't want more than 5 columns
                int depth = (catDeps.size() / 5) + 1;

                b.append("   state \"" + getRealName(catDeps.get(i)) + "\" as " + fixUpString(getRealName(catDeps.get(i))) + "\n");
                if (i % depth != 0) {
                    b.append("   " + fixUpString(getRealName(catDeps.get(i - 1))) + " --> " + fixUpString(getRealName(catDeps.get(i))) + "\n");
                }
            }
            b.append("}\n\n");
            return true;
        }

        private String fixUpString(String in) {
            return in.replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        }

        private String getRealName(String codeBaseName) {
            if (moduleNames.containsKey(codeBaseName)) {
                return moduleNames.get(codeBaseName);
            }
            return codeBaseName;
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
