/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iconeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;

/**
 *
 * @author matt
 */
public class SizeArgsParser {

    public static final Pattern MY_SPECIAL_PATTERN =
            Pattern.compile("(.*)-(\\d+)x(\\d+)-svg");
    private String baseName;
    private Float width;
    private Float height;

    public SizeArgsParser() {
    }

    public void parseName(String nameWithDimensions) {
        Matcher m = MY_SPECIAL_PATTERN.matcher(nameWithDimensions);
        if (m.matches()) {
            baseName = m.group(1);
            width = Float.valueOf(m.group(2));
            height = Float.valueOf(m.group(3));
        } else {
            baseName = nameWithDimensions;
        }
    }

    public List<FileObject> getSiblings(FileObject fo) {
        FileObject parent = fo.getParent();
        FileObject[] children = parent.getChildren();
        List<FileObject> matchingChildren = new ArrayList<FileObject>();
        for (FileObject child : children) {
            if (child.getName().startsWith(baseName)) {
                matchingChildren.add(child);
            }
        }
        return matchingChildren;
    }

    /**
     * @return the baseName
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * @return the width
     */
    public Float getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public Float getHeight() {
        return height;
    }
}