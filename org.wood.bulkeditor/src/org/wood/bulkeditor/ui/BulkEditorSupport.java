/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wood.bulkeditor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOColorPrint;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.TopComponent;

/**
 *
 * @author u415714
 */
public class BulkEditorSupport {

    private List<ProjectItem> links;
    private String relativePath;
    private ChangeSupport changeSupport;
    private ProjectItem templateItem;
    private boolean showDiff;
    private TopComponent tc;

    public BulkEditorSupport() {
        links = new ArrayList<ProjectItem>();
        changeSupport = new ChangeSupport(this);
    }

    public List<Action> getActions(InputOutput inputOutput) {
        return Collections.emptyList();
    }

    void runSearch(String relativePath, InputOutput inputOutput) {
        this.relativePath = relativePath;
        links.clear();
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (Project project : openProjects) {
            ProjectItem link = new ProjectItem(project);
            links.add(link);
        }
        Collections.sort(links);
        refreshLinks(inputOutput);
    }

    public void refreshLinks(InputOutput inputOutput) {
        try {
            inputOutput.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        inputOutput.getOut().println(relativePath);
        int maxName = 0;
        for (ProjectItem link : links) {
            if (link.getProjectName().length() > maxName) {
                maxName = link.getProjectName().length();
            }
        }
        for (ProjectItem link : links) {
            link.writeLink(inputOutput, maxName);
        }
    }

    public void addChangeListener(ChangeListener list) {
        changeSupport.addChangeListener(list);
    }

    public void removeChangeListener(ChangeListener list) {
        changeSupport.removeChangeListener(list);
    }

    void setActive(ProjectItem projectItem) {
        ProjectItem old = this.templateItem;
        if (old != projectItem) {
            templateItem = projectItem;
            changeSupport.fireChange();
        }
    }

    void setShowDiff(boolean showDiff) {
        this.showDiff = showDiff;
        changeSupport.fireChange();
    }

    boolean equalsDefault(FileObject fo) throws IOException {
        FileObject def = templateItem.getRelativeFile();
        List<String> linesDef = def.asLines();
        List<String> linesOther = fo.asLines();
        if (linesDef.size() != linesOther.size()) {
            return false;
        }
        for (int i = 0; i < linesOther.size(); i++) {
            String lineDef = linesDef.get(i);
            String lineOther = linesOther.get(i);
            if (!lineOther.equals(lineDef)) {
                return false;
            }
        }
        return true;
    }

    class ProjectItem implements Comparable<ProjectItem> {

        private final Project project;

        public ProjectItem(Project project) {
            this.project = project;
        }

        public FileObject getProjectRoot() {
            return project.getProjectDirectory();
        }

        public FileObject getRelativeFile() {
            return project.getProjectDirectory().getFileObject(relativePath);
        }

        public FileObject createRelativeFile() throws IOException {
            if (templateItem == null) {
                return null;
            }
            FileObject src = templateItem.getRelativeFile();
            String[] parts = relativePath.split("[\\/]");
            FileObject newParent = getProjectRoot();
            for (int i = 0; i < parts.length - 1; i++) {
                FileObject existing = newParent.getFileObject(parts[i]);
                if (existing != null && existing.isData()) {
                    throw new IOException("Can't create folder file with same name exists.");
                } else if (existing == null) {
                    existing = newParent.createFolder(parts[i]);
                }
                newParent = existing;
            }

            FileObject copy = src.copy(newParent, src.getName(), src.getExt());
            return copy;
        }

        public String getProjectName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }

        public boolean isActive() {
            return (templateItem == this);
        }

        public void writeLink(InputOutput inputOutput, int maxName) {
            FileObject item = getRelativeFile();
            FileLink fileLink = new FileLink();
            Color linkColor = Color.blue;
            DefaultLink defaultLink = new DefaultLink();
            if (item == null) {
                linkColor = Color.red;
                fileLink = new NewFileLink();
                defaultLink = null;
            }
            try {
                IOColorPrint.print(inputOutput, getProjectName(), fileLink, true, linkColor);
                int remainder = maxName - getProjectName().length();
                String padding = new String(new char[remainder + 1]).replace('\0', ' ');
                inputOutput.getOut().print(padding);
                if (isActive()) {
                    inputOutput.getOut().print("  (***)");
                } else if (defaultLink != null) {
                    inputOutput.getOut().print("  (");
                    IOColorPrint.print(inputOutput, "---", defaultLink, true, Color.blue);
                    inputOutput.getOut().print(")");
                } else {
                    inputOutput.getOut().print("       ");
                }
                if (showDiff) {
                    if (item != null && templateItem != null) {
                        boolean different = !equalsDefault(item);
                        inputOutput.getOut().print("  (");
                        if (different) {
                            IOColorPrint.print(inputOutput, "!=", new DiffLink(), true, Color.blue);
                        } else {
                            inputOutput.getOut().print("==");
                        }
                        inputOutput.getOut().print(")");
                    } else if (defaultLink != null) {
                        inputOutput.getOut().print(" (??)");
                    } else {
                        inputOutput.getOut().print("     ");
                    }
                }

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            inputOutput.getOut().println();
        }

        @Override
        public int compareTo(ProjectItem otherProject) {
            return getProjectName().compareTo(otherProject.getProjectName());
        }

        class FileLink extends OutputAdapter {

            @Override
            public void outputLineAction(OutputEvent oe) {
                try {
                    FileObject item = getRelativeFile();
                    DataObject dao = DataObject.find(item);
                    LineCookie lc = dao.getCookie(LineCookie.class);
                    lc.getLineSet().getCurrent(0).show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FRONT);
                } catch (DataObjectNotFoundException dataObjectNotFoundException) {
                }
            }
        }

        class NewFileLink extends FileLink {

            @Override
            public void outputLineAction(OutputEvent oe) {
                try {
                    FileObject item = createRelativeFile();
                    if (item == null) {
                        return;
                    }
                    super.outputLineAction(oe);
                    changeSupport.fireChange();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        class DefaultLink extends OutputAdapter {

            @Override
            public void outputLineAction(OutputEvent oe) {
                setActive(ProjectItem.this);
            }
        }

        class DiffLink extends OutputAdapter {

            @Override
            public void outputLineAction(OutputEvent oe) {
                FileObject def = templateItem.getRelativeFile();
                FileObject other = getRelativeFile();
                final String baseText = getOriginalText(def);
                final StreamSource base = StreamSource.createSource("name1", "default", def.getMIMEType(), new StringReader(baseText));
                final StreamSource modified = EditableStreamSource.createEditableSource("name2", "Other", other.getMIMEType(), other);

                openDiffWindow(def, modified, base, "Diff of " + def.getNameExt() + " to original");
            }
        }
    }

    public void openDiffWindow(final FileObject localFile, final StreamSource local, final StreamSource remote, final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (tc != null && tc.isVisible()) {
                        tc.close();
                        tc = null;
                    }
                    tc = new TopComponent();
                    tc.setDisplayName(title);
                    tc.setLayout(new BorderLayout());
                    makeDiffWindowSaveable(tc, localFile);
                    tc.add(DiffController.create(remote, local).getJComponent(), BorderLayout.CENTER);
                    tc.open();
                    tc.requestActive();
                } catch (IOException ex) {
                }
            }
        });
    }

    /**
     * Put the node of dataObject of the fileObject into "globallookup". This
     * allows saving via CTRL-S shortkey from within the editable diff TC. See
     * http://netbeans.org/bugzilla/show_bug.cgi?id=223703
     *
     * @param tc
     * @param fileObject
     */
    private void makeDiffWindowSaveable(TopComponent tc, FileObject fileObject) {
        if (tc != null) {
            Node node;
            try {
                node = DataObject.find(fileObject).getNodeDelegate();
            } catch (DataObjectNotFoundException e) {
                node = new AbstractNode(Children.LEAF, Lookups.singleton(fileObject));
            }
            tc.setActivatedNodes(new Node[]{node});
        }
    }

    public String getOriginalText(FileObject file) {
        // TODO this is only a mockup
        // TODO get original text from other sources like SCM, DB, template files..
        try {
            return file.asText("UTF-8").replace("public ", "public final ");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return "";
    }

    public static class EditableStreamSource extends StreamSource {

        private String name, title, mimeType;
        private FileObject fileObject;

        private EditableStreamSource(String name, String title, String mimeType, FileObject fileObject) {
            this.name = name;
            this.title = title;
            this.mimeType = mimeType;
            this.fileObject = fileObject;
        }

        public static StreamSource createEditableSource(String name, String title, String mimeType, FileObject fileObject) {
            return new EditableStreamSource(name, title, mimeType, fileObject);
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(fileObject);
        }

        @Override
        public boolean isEditable() {
            return fileObject.canWrite();
        }

        @Override
        public String getMIMEType() {
            return mimeType;
        }

        @Override
        public Reader createReader() throws IOException {
            return new FileReader(FileUtil.toFile(fileObject));
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
