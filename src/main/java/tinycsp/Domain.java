package tinycsp;

import java.util.BitSet;

/**
 * Implementation of a very basic domain
 * using the {@see java.util.BitSet} data structure
 * to store the values
 */
public class Domain {

    private BitSet values;

    /**
     * Initializes a domain with {0, ... ,n-1}
     *
     * @param n
     */
    public Domain(int n) {
        values = new BitSet(n);
        values.set(0, n);
    }

    private Domain(BitSet dom) {
        this.values = dom;
    }

    /**
     * Verifies if only one value left
     *
     * @return true if onnly one value left
     */
    public boolean isFixed() {
        return size() == 1;
    }

    /**
     * Gets the domain size
     *
     * @return the domain size
     */
    public int size() {
        return values.cardinality();
    }

    /**
     * Gets the minimum of the domain
     *
     * @return the minimum of the domain
     */
    public int min() {
        return values.nextSetBit(0);
    }

    /**
     * Removes value v
     *
     * @param v
     * @return if the value was present before
     */
    public boolean remove(int v) {
        if (0 <= v && v < values.length()) {
            if (values.get(v)) {
                values.clear(v);
                if (size() == 0) throw new TinyCSP.Inconsistency();
                return true;
            }
        }
        return false;
    }

    /**
     * Fixes the domain to value v
     *
     * @param v a value that is in the domain
     */
    public void fix(int v) {
        if (!values.get(v)) throw new TinyCSP.Inconsistency();
        values.clear();
        values.set(v);
    }

    @Override
    public Domain clone() {
        return new Domain((BitSet) values.clone());
    }

}