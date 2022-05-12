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


/**
 * Class to represent a bit-set that can be saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 */
public class StateSparseBitSet {

    /* Variables used to store value of the bitset */
    private int nWords;
    private State<Long>[] words;

    /* Variables used to make set sparse */
    private int[] nonZeroIdx;
    private StateInt nNonZero;


    /**
     * Bitset of the same capacity as the outer {@link StateSparseBitSet}.
     * It is not synchronized with  {@link StateManager}.
     * It is rather intended to be used as parameter to the
     * {@link #intersect(BitSet)} method to modify the outer {@link StateSparseBitSet}.
     */
    public class BitSet {

        private long[] words;

        /**
         * Initializes a bit-set with the same capacity as the outer {@link StateSparseBitSet}.
         * All the bits are initially unset. The set it represents is thus empty.
         */
        public BitSet() {
            words = new long[nWords];
        }

        /**
         * Set the ith bit
         *
         * @param i the bit to set
         */
        public void set(int i) {
            words[i >>> 6] |= 1L << i; // << is a cyclic shift, (1L << 64) == 1L
        }


        /**
         * Unset all the bits
         */
        public void clear() {
            for (int i = 0; i < nNonZero.value(); i++) {
                words[nonZeroIdx[i]] = 0L;
            }
        }

        /**
         * Makes the union with another bit-set but
         * only on non zero-words of the outer sparse-bit-set.
         *
         * @param other the other bit-set to make the union with
         */
        public void union(BitSet other) {
            for (int i = 0; i < nNonZero.value(); i++) {
                words[nonZeroIdx[i]] |= other.words[nonZeroIdx[i]];
            }
        }

        /**
         * Makes the intersection with another bit-set but
         * only on non zero-words of the outer sparse-bit-set.
         *
         * @param other the other bit-set to make the intersection with
         */
        public void intersect(BitSet other) {
            for (int i = 0; i < nNonZero.value(); i++) {
                words[nonZeroIdx[i]] &= other.words[nonZeroIdx[i]];
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
        //System.out.println("nwords:"+nWords);
        words = new State[nWords];
        Arrays.setAll(words, i -> sm.makeStateRef(Long.valueOf(0xFFFFFFFFFFFFFFFFL)));
        nonZeroIdx = new int[nWords];
        Arrays.setAll(nonZeroIdx, i -> i);
        nNonZero = sm.makeStateInt(nWords);
    }

    /**
     * Intersect this sparset-set with bs
     *
     * @param bs the sparset-set to intersect with
     */
    public void intersect(BitSet bs) {
        for (int i = nNonZero.value() - 1; i >= 0; i--) {
            State<Long> w = words[nonZeroIdx[i]];
            long wn = w.value() & bs.words[nonZeroIdx[i]];
            if (wn == 0L) {
                nNonZero.decrement();
                int tmp = nonZeroIdx[i];
                nonZeroIdx[i] = nonZeroIdx[nNonZero.value()];
                nonZeroIdx[nNonZero.value()] = tmp;
            } else {
                w.setValue(wn);
            }
        }
    }

    public boolean isEmpty() {
        return nNonZero.value() == 0;
    }

    public boolean hasEmptyIntersection(BitSet bs) {
        //System.out.println("nonNonZero:"+nNonZero.value());
        for (int i = nNonZero.value() - 1; i >= 0; i--) {
            State<Long> w = words[nonZeroIdx[i]];
            //System.out.println("intersectino word" + nonZeroIdx[i] +" = "+(w.value() & bs.words[nonZeroIdx[i]]));
            if ((w.value() & bs.words[nonZeroIdx[i]]) != 0L) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < nNonZero.value(); i++) {
            res += " w" + nonZeroIdx[i] + "=" + Long.toBinaryString(words[nonZeroIdx[i]].value());
        }
        return res;
    }
}
