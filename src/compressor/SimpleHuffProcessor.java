package compressor;
import gui.HuffViewer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Mediates between GUI input and compression/uncompression logic.
 * 
 * @author Volodymyr Zavidovych
 * 
 */
public class SimpleHuffProcessor implements IHuffProcessor {

    private static final int SUCCESS = 1;

    private HuffViewer myViewer;
    private HuffCompressor myCompressor = new HuffCompressor(this);

    public int preprocessCompress (InputStream in) throws IOException {
        try {
            myCompressor.preprocess(in);
            showString("Finished preprocessing");
            return SUCCESS;
        }
        catch (IOException e) {
            throw new IOException("preprocess failed");
        }
    }

    public int compress (InputStream in, OutputStream out, boolean force) throws IOException {
        try {
            myCompressor.compress(in, out);
            showString("Finished compression");
            if (myCompressor.bitsCompressed() < 0 && !force) {
                String msg =
                        "Compression uses " + Math.abs(myCompressor.bitsCompressed()) +
                                " more bits. Use 'force compression' to proceed anyways.";
                showString(msg);
                throw new IOException(msg);
            }
            return SUCCESS;
        }
        catch (IOException e) {
            throw new IOException("compress failed: " + e.getMessage());
        }
    }

    public int uncompress (InputStream in, OutputStream out) throws IOException {
        try {
            myCompressor.uncompress(in, out);
            showString("Finished uncompression");
            return SUCCESS;
        }
        catch (IOException e) {
            throw new IOException("uncompress failed: " + e.getMessage());
        }
    }

    /**
     * Show string in the display panel.
     * 
     * @param s String to show
     */
    public void showString (String s) {
        myViewer.update(s);
    }

    public void setViewer (HuffViewer viewer) {
        myViewer = viewer;
    }
}
