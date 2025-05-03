package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The huffman tree encodes values in the range 0--255 which would normally take
 * 8 bits. However, we also need to encode a special EOF character to denote the
 * end of a .grin file. Thus, we need 9 bits to store each byte value. This is
 * fine for file writing (modulo the need to write in byte chunks to the file),
 * but Java does not have a 9-bit data type. Instead, we use the next larger
 * primitive integral type, short, to store our byte values.
 */
public class HuffmanTree {

    /**
     * A class which creates a new Comparator for the purposes of a
     * PriorityQueue.
     *
     */
    public class SpecComp implements Comparator<Node> {

        /**
         * Specifies to compare two nodes by frequency
         *
         * @param nd1 The first node to compare
         * @param nd2 The second node to compare
         * @return a 1, 0, or -1 to specify larger, smaller, or equal
         */
        @Override
        public int compare(Node nd1, Node nd2) {
            if (nd1.freq < nd2.freq) {
                return -1;
            } else if (nd1.freq == nd2.freq) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /**
     * Creates a standard Node class
     *
     */
    public class Node {

        public Short ch;
        public int freq;
        public Node left;
        public Node right;

        /**
         * Constructs a new Node
         *
         * @param ch a short that maps to a character
         * @param freq the frequency attached to the character
         * @param left the left Node
         * @param right the right Node
         */
        public Node(Short ch, int freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        /**
         * Constructs a new Node
         *
         * @param ch a short that maps to a character
         * @param freq the frequency attached to the character
         */
        public Node(Short ch, int freq) {
            this.ch = ch;
            this.freq = freq;
            this.left = null;
            this.right = null;
        }

        /**
         * Constructs a new Node
         *
         * @param freq the frequency attached to the character
         * @param left the left Node
         * @param right the right Node
         */
        public Node(int freq, Node left, Node right) {
            this.ch = null;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Creates a standard Pair class
     *
     */
    public class Pair {

        public short ch;
        public int code;

        
        /**
         * Constructs a new Pair
         *
         * @param ch a short that maps to a character
         * @param code the coded HuffmanTree value attached to the character
         */
        public Pair(short ch, int code) {
            this.ch = ch;
            this.code = code;
        }

        /**
         * Sees if two pairs have the same character
         *
         * @param tmp a Pair to compare
         * @return Specifies if two pairs have the same Character
         */
        public boolean equalsCh(Pair tmp) {
            if (this.ch == tmp.ch) {
                return true;
            }
            return false;
        }

        /**
         * Sees if two pairs have the same code
         *
         * @param tmp a Pair to compare
         * @return Specifies if two pairs have the same code
         */
        public boolean equalsCode(Pair tmp) {
            if (this.code == tmp.code) {
                return true;
            }
            return false;
        }
    }

    public Node start;
    public ArrayList<Pair> codes;

    /**
     * Constructs a new HuffmanTree from a frequency map.
     *
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree(Map<Short, Integer> freqs) {
        PriorityQueue<Node> Davian = new PriorityQueue<Node>(new SpecComp());
        Set<Short> keys = freqs.keySet();
        Short[] keysIterable = keys.toArray(new Short[keys.size()]);
        for (int i = 0; i < keysIterable.length; i++) {
            Node tmp = new Node(keysIterable[i], freqs.get(keysIterable[i]));
            Davian.add(tmp);
        }
        Node tmp2 = null;
        Node tmp1 = null;
        Node tmp3 = null;
        while (!Davian.isEmpty()) {
            tmp1 = Davian.poll();
            tmp2 = Davian.poll();
            tmp3 = new Node(tmp1.freq + tmp2.freq, tmp1, tmp2);
            Davian.add(tmp3);
            tmp2 = null;
        }
        if (tmp2 != null) {
            this.start = new Node(tmp1.freq + tmp2.freq, tmp1, tmp2);
        } else {
            this.start = tmp1;
        }
        makeCodes();
    }

    /**
     * Constructs a new HuffmanTree from the given file.
     *
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree(BitInputStream in) {
        Map<Short, Integer> freqs = new HashMap<Short, Integer>();
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
        PriorityQueue<Node> Davian = new PriorityQueue<Node>(new SpecComp());
        Set<Short> keys = freqs.keySet();
        Short[] keysIterable = keys.toArray(new Short[keys.size()]);
        for (int i = 0; i < keysIterable.length; i++) {
            Node tmpOther = new Node(keysIterable[i], freqs.get(keysIterable[i]));
            Davian.add(tmpOther);
        }
        Node tmp2 = null;
        Node tmp1 = null;
        Node tmp3 = null;
        while (!Davian.isEmpty()) {
            tmp1 = Davian.poll();
            tmp2 = Davian.poll();
            tmp3 = new Node(tmp1.freq + tmp2.freq, tmp1, tmp2);
            Davian.add(tmp3);
            tmp2 = null;
        }
        if (tmp2 != null) {
            this.start = new Node(tmp1.freq + tmp2.freq, tmp1, tmp2);
        } else {
            this.start = tmp1;
        }
        makeCodes();
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     *
     * @param out the output file as a BitOutputStream
     */
    public void serialize(BitOutputStream out) {
        String stringBits = toListPreorder();
        while (stringBits.length() > 32) {
            String tmp = stringBits.substring(0, 32);
            int bits = Integer.parseInt(tmp);
            out.writeBits(bits, 32);

        }
        out.writeBits(Integer.parseInt(stringBits), stringBits.length());
    }

    /**
     * Specifies the bits associated with the tree in a specified order
     * 
     * @return the elements of this tree collected via a pre-order traversal
     */
    public String toListPreorder() {
        String bits = new String("");
        return toListPreorderHelper(bits, this.start);
    }

    /**
     * Helps to specify the bits associated with the tree in a specified order
     * 
     * @param bits The ever growing string of bits to return
     * @param cur The current node
     * @return the elements of this tree collected via a pre-order traversal
     */
    public String toListPreorderHelper(String bits, Node cur) {
        if (cur.ch == null) {
            bits = bits.concat("1");
        } else {
            bits = bits.concat("00");
            String temp = "" + Integer.toBinaryString(cur.ch);
            bits = bits.concat(temp);
        }
        if (cur != null) {
            if (cur.left != null) {
                toListPreorderHelper(bits, cur.left);
            }

            if (cur.right != null) {
                toListPreorderHelper(bits, cur.right);
            }
        }
        return bits;
    }

    /**
     * Encodes the file given as a stream of bits into a compressed format using
     * this Huffman tree. The encoded values are written, bit-by-bit to the
     * given BitOuputStream.
     *
     * @param in the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode(BitInputStream in, BitOutputStream out) throws IOException {
        byte[] bytearray = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0x07, (byte) 0x36};
        for (int i = 0; i < 4; i++) {
            out.writeBits(bytearray[i], 8);
        }
        String path = "Cereal.txt";
        BitOutputStream temp = new BitOutputStream(path);
        BitInputStream tempConvert = new BitInputStream(path);
        int bit = tempConvert.readBits(1);
        while (bit != -1) {
            out.writeBit(bit);
            bit = tempConvert.readBit();
        }
        short tmpVal = (short) in.readBits(8);
        while (tmpVal != -1) {
            Pair tmpPair = new Pair(tmpVal, -1);
            for (int i = 0; i < codes.size(); i++) {
                if (tmpPair.equalsCh(codes.get(i))) {
                    int j = codes.get(i).code;
                    String lgh = Integer.toString(j);
                    out.writeBits(j, lgh.length());
                }
            }
        }
        out.writeBits(100000000, 9);
    }

    /**
     * Helps to construct a list of Huffman Tree codes
     * 
     * @param cur The current node
     * @param treeKey The partly constructed code
     */
    private void makeCodesH(Node cur, ArrayList<Integer> treeKey) {
        if (cur.left == null && cur.right == null) {
            int code = 0;
            for (int i = 0; i < treeKey.size(); i++) {
                if (i == 0) {
                    code = treeKey.get(i);
                } else {
                    code = code * 10 + treeKey.get(i);
                }
            }
            Pair group = new Pair(cur.ch, code);
            this.codes.add(group);
        }
        if (cur.left != null) {
            treeKey.add(0);
            makeCodesH(cur.left, treeKey);
        }
        if (cur.right != null) {
            treeKey.add(1);
            makeCodesH(cur.right, treeKey);
        }
    }

    /**
     * Constructs a list of Huffman Tree codes
     * 
     */
    public void makeCodes() {
        ArrayList<Integer> treeKey = new ArrayList<Integer>();
        makeCodesH(start, treeKey);
    }

    /**
     * Decodes a stream of huffman codes from a file given as a stream of bits
     * into their uncompressed form, saving the results to the given output
     * stream. Note that the EOF character is not written to out because it is
     * not a valid 8-bit chunk (it is 9 bits).
     *
     * @param in the file to decompress.
     * @param out the file to write the decompressed output to.
     */
    public void decode(BitInputStream in, BitOutputStream out) {
        int recievedCode = in.readBit();
        int constructedCode = recievedCode;
        while (recievedCode != -1) {
            if (containsCode(constructedCode)) {
                Pair tmpPair = new Pair((short) -1, constructedCode);
                for (int i = 0; i < codes.size(); i++) {
                    if (tmpPair.equalsCode(codes.get(i))) {
                        int j = (int) codes.get(i).ch;
                        String lgh = Integer.toString(j);
                        out.writeBits(j, lgh.length());
                        recievedCode = in.readBit();
                        constructedCode = recievedCode;
                        i = codes.size();
                    }
                }
            } else {
                recievedCode = in.readBit();
                constructedCode = constructedCode * 10 + recievedCode;
            }
        }
    }

    /**
     * Checks if a code is contained within the Huffman Tree
     * 
     * @param codeTest The tested code
     * @return if it is contained within the Huffman Tree
     */
    public boolean containsCode(int codeTest) {
        for (int i = 0; i < codes.size(); i++) {
            if (codeTest == (codes.get(i).code)) {
                return true;
            }
        }
        return false;
    }
}
