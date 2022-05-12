*****************************************************************
Part 2: Domains, Variables, Constraints
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

* `Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMypEq5qeLBz8xFTdtAkNr56I>`_

* `Domains and Variables <https://www.icloud.com/keynote/0d8bL8fMVoxodgnQvYx_LHwxA#02-domains-variables-constraints>`_

Theoretical Questions
=====================

* `Domains and Sparse Sets <https://inginious.org/course/minicp/domains>`_

Domain with an Arbitrary Set of Values
=================================================================================

Implement the missing constructor in `IntVarImpl.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/core/IntVarImpl.java?at=master>`_:


.. code-block:: java

    public IntVarImpl(Solver cp, Set<Integer> values) {
        throw new NotImplementedException();
    }

This exercise is straightforward: just create a dense domain and then remove the values not present in the set.

Check that your implementation passes the tests `IntVarTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/core/IntVarTest.java?at=master>`_.

Implement a Domain Iterator
======================================

Many filtering algorithms require iteration over the values of a domain.

A naive (but correct) way of iterating over a domain is:


.. code-block:: java

    for (int v = x.min(); v <= x.max(); x++) {
        if (x.contains(i)) {
            // do something
        }
    }

This method is rather inefficient because it will also consider the values that are not present in the domain.
Instead, the `fillArray` method from `StateSparseSet.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/state/StateSparseSet.java?at=master>`_
allows filling an array with all the values present in the sparse set.
In case of an offset value of 0, you could even use the very efficient `System.arraycopy`.

The main advantage over the iterator mechanism is that no object is created (and thus garbage collected).
Indeed `dest` is typically a container array stored as an instance variable and reused many times.
It is important for efficiency to avoid creating objects on the heap at each execution of a propagator.
Never forget that a `propagate()` method of `Constraint` may be called thousands of times per second.
This implementation using `fillArray` avoids the `ConcurrentModificationException` discussion
when implementing an Iterator: should we allow modifying a domain while iterating on it?
The answer here is very clear: you get a snapshot of the domain at the time of the call to `fillArray` and you can thus
safely iterate over this `dest` array and modify the domain at the same time.

To do:

* Improve the efficiency of `fillArray` from `StateSparseSet.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/state/StateSparseSet.java?at=master>`_ in order to use `System.arraycopy` when possible.
* Implement `public int fillArray(int [] dest)` in `IntVarImpl.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/core/IntVarImpl.java?at=master>`_.
* Check that your implementation passes the tests `IntVarTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/core/IntVarTest.java?at=master>`_ and `StateSparseSetTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/state/StateSparseSetTest.java?at=master>`_. Additionally, add more tests to `IntVarTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/core/IntVarTest.java?at=master>`_.

The Absolute Value Constraint
==============================

Implement `Absolute.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Absolute.java?at=master>`_.

Several directions of implementation are possible:

1. The full domain-consistent version (use the `fillArray` method to iterate over domains).
2. A hybrid domain-bound consistent one.

Check that your implementation passes the tests `AbsoluteTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/AbsoluteTest.java?at=master>`_.


The Maximum Constraint
==============================

Implement `Maximum.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Maximum.java?at=master>`_.

Implement a bound-consistent filtering algorithm.

Check that your implementation passes the tests `MaximumTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/MaximumTest.java?at=master>`_.
