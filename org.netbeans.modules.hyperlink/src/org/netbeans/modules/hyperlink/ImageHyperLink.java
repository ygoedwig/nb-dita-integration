/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.hyperlink;

import java.awt.Dialog;
import java.io.File;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author matt
 */
public class ImageHyperLink implements HyperlinkProvider {

    private String target;
    private int targetStart;
    private int targetEnd;

    @Override
    public boolean isHyperlinkPoint(Document document, int offset) {
        return verifyState(document, offset);
    }

    @Override
    public int[] getHyperlinkSpan(Document document, int offset) {
        if (verifyState(document, offset)) {
            return new int[]{targetStart, targetEnd};
        } else {
            return null;
        }
    }

    private void openFile(File currentDir, String relativeFilePath) {
        File f = new File(currentDir, relativeFilePath);
        if (!f.exists()) {
            CreateNewUI createNew = new CreateNewUI(f);
            DialogDescriptor dest = new DialogDescriptor(createNew, "New file selector.");
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dest);
            dialog.setVisible(true);
            if (dest.getValue() == DialogDescriptor.OK_OPTION) {
                createNew.create();
            } else {
                return;
            }
        }
        FileObject fob = FileUtil.toFileObject(FileUtil.normalizeFile(f));
        if (fob.hasExt("png")) {
            FileObject svg = fob.getParent().getFileObject("svg");
            if (svg != null) {
                svg = svg.getFileObject(fob.getName(), "svg");
            }
            if (svg != null) {
                fob = svg;
            }
        }
        try {
            DataObject dob = DataObject.find(fob);
            OpenCookie oc = dob.getCookie(OpenCookie.class);
            if (oc != null) {
                oc.open();
            } else {
            }
        } catch (DataObjectNotFoundException dataObjectNotFoundException) {
        }

    }

    @Override
    public void performClickAction(Document document, int offset) {
        if (verifyState(document, offset)) {
            String file = (String) document.getProperty("title");
            File f = new File(file);
            openFile(f.getParentFile(), target);
        }
    }
    int counter = 0;

    public boolean verifyState(Document document, int offset) {
        TokenHierarchy hi = TokenHierarchy.get(document);
        TokenSequence<XMLTokenId> ts = hi.tokenSequence(XMLTokenId.language());
        Token<XMLTokenId> lastToken = null;
        if (ts != null) {
            ts.move(offset);
            ts.moveNext();
            Token<XMLTokenId> tok = ts.token();
            int newOffset = ts.offset();
            String matcherText = tok.text().toString();
            //System.out.println((counter++) + matcherText);
            ts.movePrevious(); // Should be equals
            ts.movePrevious(); // Should be href

            String attributeName = ts.token().text().toString();
            // The first covers html format and the second ant.
            if ("href".equalsIgnoreCase(attributeName) || "file".equalsIgnoreCase(attributeName)) {
                target = matcherText;
                if (target.startsWith("\"")) {
                    target = target.substring(1);
                }
                if (target.endsWith("\"")) {
                    target = target.substring(0, target.length() - 1);
                }
                int idx = matcherText.indexOf(target);
                targetStart = newOffset + idx;
                targetEnd = targetStart + target.length();
                return true;
            }
            lastToken = tok;
        }
        return false;
    }
}
