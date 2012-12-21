import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.PriorityQueue;


/**
 * Mediates between GUI input and compression/uncompression logic.
 * 
 * @author Volodymyr Zavidovych
 * 
 */
public class SimpleHuffProcessor implements IHuffProcessor {

    private static final int SUCCESS = 1;
    private static final int NON_LEAF_NODE_VALUE = -1;

    private HuffViewer myViewer;
    private BitInputStream myInput;
    private BitOutputStream myOutput;
    private int[] myFrequency;
    private TreeNode myHuffTree;
    private TwoWayMap<Integer, String> myHuffMap;
    private int myInputSize;
    private int myOutputSize;
    private HuffCompressor myCompressor = new HuffCompressor();

    public int preprocessCompress (InputStream in) throws IOException {
        try {
            myCompressor.preprocess(in);
            
            myInput = new BitInputStream(in);
            countFrequency();
            buildHuffTree();
            buildHuffMap();
            myInput.close();
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
            myInputSize = 0;
            myOutputSize = 0;
            myInput = new BitInputStream(in);
            myOutput = new BitOutputStream(out);
            writeHeader();
            writeContent();
            writeEOF();
            myInput.close();
            myOutput.close();
            showString("Finished compression");
            if (myCompressor.wasUseless() && !force) {
            if (myInputSize <= myOutputSize && !force) {
                String msg =
                        "Compression uses " + (myOutputSize - myInputSize) +
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
            myInput = new BitInputStream(in);
            myOutput = new BitOutputStream(out);
            readHeader();
            buildHuffTree();
            buildHuffMap();
            translateContent();
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
    private void showString (String s) {
        myViewer.update(s);
    }
    
    public void setViewer (HuffViewer viewer) {
        myViewer = viewer;
    }
    
    private void countFrequency () throws IOException {
        myFrequency = new int[ALPH_SIZE];
        int inbits;
        while ((inbits = myInput.readBits(BITS_PER_WORD)) != -1) {
            myFrequency[inbits]++;
        }
    }

    private void buildHuffTree () {
        PriorityQueue<TreeNode> huffForest = buildForest();
        myHuffTree = reduceToSingleTree(huffForest);
        showString("Finished building Huffman tree");
    }

    private PriorityQueue<TreeNode> buildForest () {
        PriorityQueue<TreeNode> huffForest = new PriorityQueue<TreeNode>();
        for (int i = 0; i < myFrequency.length; i++) {
            int weight = myFrequency[i];
            if (weight != 0) {
                huffForest.add(new TreeNode(i, weight));
            }
        }
        // adding EOF
        huffForest.add(new TreeNode(PSEUDO_EOF, 1));
        return huffForest;
    }

    private TreeNode reduceToSingleTree (PriorityQueue<TreeNode> huffForest) {
        while (huffForest.size() > 1) {
            TreeNode first = huffForest.poll();
            TreeNode second = huffForest.poll();
            TreeNode parent = new TreeNode(NON_LEAF_NODE_VALUE, first.myWeight + second.myWeight);
            parent.myLeft = first;
            parent.myRight = second;
            huffForest.add(parent);
        }
        return huffForest.poll();
    }

    private void buildHuffMap () {
        myHuffMap = new TwoWayMap<Integer, String>();
        traverse(myHuffTree.myLeft, "0");
        traverse(myHuffTree.myRight, "1");
        showString("Finished building map");
    }

    private void traverse (TreeNode node, String path) {
        if (node == null) { return; }
        // check if leaf
        if (node.myValue != NON_LEAF_NODE_VALUE) {
            myHuffMap.put(node.myValue, path);
        }
        else {
            traverse(node.myLeft, path + "0");
            traverse(node.myRight, path + "1");
        }
    }

    

    private void writeHeader () {
        myOutput.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        myOutputSize += BITS_PER_INT;
        // start of frequency counts
        for (int k = 0; k < ALPH_SIZE; k++) {
            myOutput.writeBits(BITS_PER_INT, myFrequency[k]);
            myOutputSize += BITS_PER_INT;
        }
        // end of frequency counts
        myOutput.writeBits(BITS_PER_INT, STORE_COUNTS);
        myOutputSize += BITS_PER_INT;
        showString("Finished writing header");
    }

    private void writeContent () throws IOException {
        int inbits;
        while ((inbits = myInput.readBits(BITS_PER_WORD)) != -1) {
            myInputSize += BITS_PER_WORD;
            writeBitString(myHuffMap.getForward(inbits));
        }
        showString("Finished writing content");
    }

    private void writeEOF () {
        writeBitString(myHuffMap.getForward(PSEUDO_EOF));
    }

    private void writeBitString (String sequence) {
        for (int i = 0; i < sequence.length(); i++) {
            int bit = sequence.charAt(i) - '0';
            myOutput.writeBits(1, bit);
            myOutputSize += 1;
        }
    }    

    private void readHeader () throws IOException {
        // check magic number
        int magic = myInput.readBits(BITS_PER_INT);
        if (magic != MAGIC_NUMBER) { throw new IOException("magic number not right"); }
        // read counts
        myFrequency = new int[ALPH_SIZE];
        for (int k = 0; k < ALPH_SIZE; k++) {
            int bits = myInput.readBits(BITS_PER_INT);
            myFrequency[k] = bits;
        }
        // read end of counts
        int endOfCounts = myInput.readBits(BITS_PER_INT);
        if (endOfCounts != STORE_COUNTS) { throw new IOException("invalid header"); }
        showString("Finished reading header");
    }

    private void translateContent () throws IOException {
        showString("Started writing uncompressed file");
        String nextBitSeq = "";
        while (true) {
            int nextBit = myInput.readBits(1);
            if (nextBit == -1) { throw new IOException("error reading bits, no PSEUDO-EOF"); }
            nextBitSeq += nextBit + "";
            Integer nextChar = myHuffMap.getBackward(nextBitSeq);
            if (nextChar != null) {
                // check if EOF
                if (nextChar == PSEUDO_EOF) {
                    break;
                }
                // write output
                myOutput.writeBits(BITS_PER_WORD, nextChar);
                nextBitSeq = "";
            }
        }
    }
}
