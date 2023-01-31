/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.state;

import java.util.Arrays;
import minicp.util.exception.NotImplementedException;


/**
 * Class to represent a bit-set that can be saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 */
public class StateSparseBitSet {

    /* Variables used to store value of the bitset */
    private final int nWords;
    private final State<Long>[] words;

    /* Variables used to make set sparse */
    private final int[] nonZeroIdx;
    private final StateInt nonZeroSize;


    /**
     * Bitset of the same capacity as the outer {@link StateSparseBitSet}.
     * It is not synchronized with  {@link StateManager}.
     * It is rather intended to be used as parameter to the
     * {@link #and(BitSet)} method to modify the outer {@link StateSparseBitSet}.
     */
    public class BitSet {
        protected long[] words;
        /**
         * Initializes a bit-set with the same capacity as the outer {@link StateSparseBitSet}.
         * All the bits are initially unset. The set it represents is thus empty.
         */
        public BitSet() {
            words = new long[nWords];
        }

        /**
         * As for the {@link java.util.BitSet#set(int)}
         * Sets the bit at the specified index to true
         *
         * @param i the bit to set
         */
        public void set(int i) {
            long v = 1L << i;
            words[i >>> 6] |= v; // << is a cyclic shift, (1L << 64) == 1L
        }

        /**
         * As for the {@link java.util.BitSet#get(int)}
         * Gives the bit at the specified index
         *
         * @param i the bit to return
         * @return true if bit at index i is set
         */
        public boolean get(int i) {
            int wordIndex = i >>> 6;
            return wordIndex < nWords && (this.words[wordIndex] & 1L << i) != 0L;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < nWords; i++) {
                res.append(" w").append(i).append("=").append(Long.toBinaryString(words[i]));
            }
            return res.toString();
        }
    }

    public class SupportBitSet extends BitSet{
        protected int residue;
        public SupportBitSet() {
            super();
            residue = 0;
        }
    }
    public class MaskBitSet extends BitSet{

        /**
         * Initializes a bit-set with the same capacity as the outer {@link StateSparseBitSet}.
         * All the bits are initially unset. The set it represents is thus empty.
         */
        public MaskBitSet() {
            super();
        }


        /**
         * As for {@link java.util.BitSet#clear()}:
         * Sets all the bits in this BitSet to false
         * <p>
         * The clear is optimized to ignore the empty words in the
         * associated Reversible Sparse Bit Set
         */
        public void clear() {
            for (int i = 0; i < nonZeroSize.value(); i++) {
                this.words[nonZeroIdx[i]] = 0L;
            }
        }

        /**
         * As for {@link java.util.BitSet#or(java.util.BitSet)}:
         * Performs a logical OR of this bit set with the bit set argument. This
         * bit set is modified so that a bit in it has the value true if and only
         * if it either already had the value true or the corresponding bit in the
         * bit set argument has the value true.
         * <p>
         * The logical OR is optimized to ignore the empty words in the
         * associated Reversible Sparse Bit Set
         *
         * @param other the other bit-set to make the union with
         */
        public void or(BitSet other) {
            for (int i = 0; i < nonZeroSize.value(); i++) {
                this.words[nonZeroIdx[i]] |= other.words[nonZeroIdx[i]];
            }
        }

        /**
         * As for {@link java.util.BitSet#and(java.util.BitSet)}:
         * Performs a logical AND of this target bit set with the argument
         * bit set. This bit set is modified so that each bit in it has the value
         * true if and only if it both initially had the value true and the
         * corresponding bit in the bit set argument also had the value true.
         * <p>
         * The logical AND is optimized to ignore the empty words in the
         * associated Reversible Sparse Bit Set
         *
         * @param other the other bit-set to make the intersection with
         */
        public void and(BitSet other) {
            for (int i = 0; i < nonZeroSize.value(); i++) {
                this.words[nonZeroIdx[i]] &= other.words[nonZeroIdx[i]];
            }
        }
    }


    /**
     * Creates a StateSparseSet with n bits, initially all set
     *
     * @param sm the state manager
     * @param n  the number of bits
     */
    public StateSparseBitSet(StateManager sm, int n) {
        nWords = (n + 63) >>> 6; // divided by 64
        words = new State[nWords];
        Arrays.setAll(words, i -> sm.makeStateRef(0xFFFFFFFFFFFFFFFFL));
        nonZeroIdx = new int[nWords];
        Arrays.setAll(nonZeroIdx, i -> i);
        nonZeroSize = sm.makeStateInt(nWords);
    }

    /**
     * As for {@link java.util.BitSet#and(java.util.BitSet)}:
     * Performs a logical AND of this target bit set with the argument
     * bit set. This bit set is modified so that each bit in it has the value
     * true if and only if it both initially had the value true and the
     * corresponding bit in the bit set argument also had the value true.
     * <p>
     * The logical AND is optimized to ignore the empty words in the
     * associated Reversible Sparse Bit Set
     *
     * @param bs the sparset-set to intersect with
     */
    public void and(BitSet bs) {
        for (int i = nonZeroSize.value() - 1; i >= 0; i--) {
            State<Long> w = words[nonZeroIdx[i]];
            long wn = w.value() & bs.words[nonZeroIdx[i]];
            w.setValue(wn);
            if (wn == 0L) { // swap with last non-zero word
                nonZeroSize.decrement();
                int tmp = nonZeroIdx[i];
                nonZeroIdx[i] = nonZeroIdx[nonZeroSize.value()];
                nonZeroIdx[nonZeroSize.value()] = tmp;
            }
        }
    }

    /**
     * As for the {@link java.util.BitSet#isEmpty()} function:
     * Returns true if this BitSet contains no bits that are set to true.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return nonZeroSize.value() == 0;
    }


    /**
     * Returns true if, for the given {@link SupportBitSet#words} id stored in by the {@link SupportBitSet#residue} of
     * the specified {@link SupportBitSet} bs,
     * bs has any bits set to true
     * that are also set to true in the corresponding {@link StateSparseBitSet#words} of this BitSet.
     * <p>
     *
     * @param bs the bitset to test the intersection with
     * @return true if the intersection is empty
     */
    protected boolean intersectsResidueOnly(SupportBitSet bs) {
        // TODO 1: use the residue to test if the non-empty intersection stored is still non-empty
         throw new NotImplementedException("StateSparseBitSet");
    }

    /**
     * As for the {@link java.util.BitSet#intersects(java.util.BitSet)} function:
     * Returns true if the specified {@link SupportBitSet} has any bits set to true
     * that are also set to true in this {@link StateSparseBitSet}.
     * <p>
     * The intersection test is optimized to ignore the empty words in the
     * associated Reversible Sparse Bit Set
     *
     * @param bs the bitset to test the intersection with
     * @return true if the intersection is empty
     */
    public boolean intersects(SupportBitSet bs) {
        if (intersectsResidueOnly(bs)) {
            return true;
        }
        for (int i = nonZeroSize.value() - 1; i >= 0; i--) {
            int idx = nonZeroIdx[i];
            State<Long> w = words[idx];
            if ((w.value() & bs.words[idx]) != 0L) {
                // TODO 2: store the new non-empty intersection using residue of bs
                
                return true;
            }
        }
        return false;
    }

    /**
     * As for the {@link java.util.BitSet#get(int)}
     * Gives the bit at the specified index
     *
     * @param i the bit to return
     * @return true if bit at index i is set
     */
    public boolean get(int i) {
        int wordIndex = i >>> 6;
        return wordIndex < nonZeroSize.value() && (this.words[wordIndex].value() & 1L << i) != 0L;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < nonZeroSize.value(); i++) {
            res.append(" w").append(nonZeroIdx[i]).append("=").append(Long.toBinaryString(words[nonZeroIdx[i]].value()));
        }
        return res.toString();
    }
}
