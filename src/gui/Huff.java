package gui;
import compressor.IHuffProcessor;
import compressor.SimpleHuffProcessor;

/**
 * This software can compress (and uncompress) files using
 * <a href="http://en.wikipedia.org/wiki/Huffman_coding">Huffman coding</a>
 * algorithm.
 * 
 * Based on the <a href="http://www.cs.duke.edu/csed/poop/huff/info/">Huffman
 * Coding assignment</a> by Owen Astrachan at Duke University.
 * 
 * @author Volodymyr Zavidovych
 * 
 */
public class Huff {

    public static void main (String[] args) {
        HuffViewer sv = new HuffViewer("Huffman Compressor");
        IHuffProcessor proc = new SimpleHuffProcessor();
        sv.setModel(proc);
    }
}
