import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class that does all compression and uncompression work.
 * 
 * @author Volodymyr Zavidovych
 *
 */
public class HuffCompressor {

    public void preprocess (InputStream in) {
        myInput = new BitInputStream(in);
        countFrequency();
        buildHuffTree();
        buildHuffMap();
        myInput.close();
    }

    public void compress (InputStream in, OutputStream out) {
        // TODO Auto-generated method stub
        
    }

    public boolean wasUseless () {
        // TODO Auto-generated method stub
        return false;
    }

    public void uncompress (InputStream in, OutputStream out) {
        // TODO Auto-generated method stub
        
    }

}
