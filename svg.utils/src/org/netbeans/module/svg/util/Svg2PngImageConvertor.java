/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.svg.util;

import java.io.OutputStream;
import java.util.List;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.netbeans.modules.iconeditor.DelegateImageEditor;
import org.netbeans.modules.iconeditor.SizeArgsParser;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author matt
 */
@ServiceProvider(service = DelegateImageEditor.class)
public class Svg2PngImageConvertor implements DelegateImageEditor {

    @Override
    public void convertImage(FileObject master, FileObject actual) throws Exception {
        SizeArgsParser sizeArgsParser = new SizeArgsParser();
        sizeArgsParser.parseName(actual.getName());
        List<FileObject> matchingChildren = sizeArgsParser.getSiblings(actual);
        for (FileObject child : matchingChildren) {
            sizeArgsParser.parseName(child.getName());
            convertImage(master, child, sizeArgsParser.getWidth(), sizeArgsParser.getHeight());
        }
    }

    private void convertImage(FileObject master, FileObject actual, Float width, Float height) throws Exception {

        PNGTranscoder t = new PNGTranscoder();

        // Set the transcoding hints.
        if (width != null) {
            t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        }
        if (height != null) {
            t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
        }
        // Create the transcoder input.
        TranscoderInput input = new TranscoderInput(master.getInputStream());

        // Create the transcoder output.
        OutputStream ostream = actual.getOutputStream();
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
    }
}
