/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dita.help;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author matt
 */
@ServiceProvider(service = FileSystem.class)
public class DynamicHelpContent extends MultiFileSystem {

    private static final String URL_TOKEN = "__URL_2_REPLACE__";
    private static DynamicHelpContent INSTANCE;
    private String helpSetRefContent;
    private String dynamicLayerContent;

    public DynamicHelpContent() {
        // will be created on startup, exactly once
        INSTANCE = this;
        setPropagateMasks(true); // permit *_hidden masks to be used

        helpSetRefContent = read2String(getClass().getResourceAsStream("dynamicHelpContent.xml"));
        dynamicLayerContent = read2String(getClass().getResourceAsStream("dynamicLayerContent.xml"));
    }
//file:/home/matt/java/dita/dita.tools/ditadocs/build/javahelp/userdocs_helpset.hs

    static boolean hasContent() {
        return INSTANCE.getDelegates().length > 0;
    }

    public void reloadLayer(URL helpSetFileUrl) throws IOException {
        String helpSet = helpSetRefContent.replace(URL_TOKEN, helpSetFileUrl.toExternalForm());
        File file = File.createTempFile("temp-helpset-ref", ".xml");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(helpSet.getBytes());
        fos.close();
        String layerContent = dynamicLayerContent.replace(URL_TOKEN, Utilities.toURI(file).toURL().toExternalForm());
        file = File.createTempFile("temp-layer-ref", ".xml");
        fos = new FileOutputStream(file);
        fos.write(layerContent.getBytes());
        fos.close();
        try {
            INSTANCE.setDelegates(new XMLFileSystem(Utilities.toURI(file).toString()));
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void enable(URL helpSetFileUrl) throws IOException {
        INSTANCE.reloadLayer(helpSetFileUrl);
    }

    static public void disable() {
        INSTANCE.setDelegates();
    }

    private String read2String(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException iOException) {
        }
        return sb.toString();
    }
}
