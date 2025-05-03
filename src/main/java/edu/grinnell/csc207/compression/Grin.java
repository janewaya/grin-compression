package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The driver for the Grin compression program.
 */
public class Grin {

    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     *
     * @param infile the file to decode
     * @param outfile the file to ouptut to
     */
    public static void decode(String infile, String outfile) throws IOException {
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        if (1846 != in.readBits(32)) {
            throw new IllegalArgumentException("Not a grin file.");
        }
        HuffmanTree de = new HuffmanTree(createFrequencyMap(infile));
        de.decode(in, out);
    }

    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of those
     * sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     *
     * @param file the file to read
     * @return a freqency map for the given file
     */
    public static Map<Short, Integer> createFrequencyMap(String file) throws IOException {
        Map<Short, Integer> freqs = new HashMap<Short, Integer>();
        BitInputStream in = new BitInputStream(file);
        int tmp = in.readBits(8);
        while (tmp != -1) {
            byte pls = (byte) tmp;
            short letter = (short) pls;
            if (!freqs.containsKey(letter)) {
                freqs.put(letter, 1);
            } else {
                freqs.replace(letter, freqs.get(letter) + 1);
            }
        }
        return freqs;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     *
     * @param infile the file to encode.
     * @param outfile the file to write the output to.
     */
    public static void encode(String infile, String outfile) throws IOException {
        HuffmanTree en = new HuffmanTree(createFrequencyMap(infile));
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        en.encode(in, out);
    }

    /**
     * The entry point to the program.
     *
     * @param args the command-line arguments.
     */
    public static void main(String[] args) throws IOException {
        if (args[0].equals("encode")) {
            encode(args[1], args[2]);
        } else if (args[0].equals("decode")) {
            decode(args[1], args[2]);
        } else {
            System.out.println("Error: encode or decode not specified as the"
                    + "first arguement.");
        }
    }
}
