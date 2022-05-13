package tinycsp;

abstract class Constraint {
    /**
     * Propagate the constraint and return true if any value could be removed
     *
     * @return true if at least one value of one variable could be removed
     */
    abstract boolean propagate();
}