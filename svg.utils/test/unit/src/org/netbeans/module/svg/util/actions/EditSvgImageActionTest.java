/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.module.svg.util.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author matt
 */
public class EditSvgImageActionTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testActionPerformed() throws IOException, TranscoderException {
        // Create a JPEG transcoder
        PNGTranscoder t = new PNGTranscoder();

        // Set the transcoding hints.
        t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(100));
        t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(100));

        // Create the transcoder input.
        String svgURI = new File("/home/matt/java/developer-utils/developer.utils/dita.tools/ditadocs/userdocs/images/svg/basic-64x64.svg").toURL().toString();
        TranscoderInput input = new TranscoderInput(svgURI);

        // Create the transcoder output.
        OutputStream ostream = new FileOutputStream("/home/matt/java/developer-utils/out.png");
        TranscoderOutput output = new TranscoderOutput(ostream);

        // Save the image.
        t.transcode(input, output);

        // Flush and close the stream.
        ostream.flush();
        ostream.close();
    }
}