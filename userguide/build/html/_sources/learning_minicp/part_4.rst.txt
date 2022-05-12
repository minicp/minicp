*****************************************************************
Part 4: Sum and Element Constraints
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

* `Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyrUvtxIwsgTQn2PZr55Bp2i>`_

* `Sum Constraint <https://www.icloud.com/keynote/02bmdG7fW7LWPuuiOlm9vlf_g#04a-sum-constraint>`_

* `Element Constraint <https://www.icloud.com/keynote/013s60X8I6SEv_tG9OWbYj2qA#04b-element-constraints>`_

Theoretical Questions
=====================

* `Element Constraints <https://inginious.org/course/minicp/element>`_


Element1D Constraint
=================================

Given an array `T` of integers and the variables `y` and `z`, the `Element1D` constraint enforces that `z` takes the value at index
`y` of `T`: the relation `T[y]=z` must hold (where indexing starts from 0).

Assuming `T=[1,3,5,7,3]`, the constraint holds for

.. code-block:: java

    y = 1, z = 3
    y = 3, z = 7


but is violated for

.. code-block:: java

    y = 0, z = 2
    y = 3, z = 3

Implement a propagator
`Element1D.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Element1D.java?at=master>`_
by following the ideas (also in the slides) for `Element2D`,
which however do not lead to domain consistency for both variables.
Check that your propagator passes the tests
`Element1DTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/Element1DTest.java?at=master>`_.

Also implement a propagator
`Element1DDomainConsistent.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Element1DDomainConsistent.java?at=master>`_
that achieves domain consistency for both variables.
Check that your propagator passes the tests
`Element1DDCTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/Element1DDCTest.java?at=master>`_.


Element1DVar Constraint with an Array of Variables
==================================================

Implement a propagator
`Element1DVar.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Element1DVar.java?at=master>`_.
This constraint is more general than `Element1D` above,
since `T` is here an array of variables.

The filtering algorithm is nontrivial,
at least if you want to do it efficiently.
Two directions of implementation are:

* The hybrid domain-bound consistent propagator
  achieves bounds consistency for `z` and all the `T[i]`
  but domain consistency for `y`.
* The domain-consistent propagator
  achieves domain consistency for `y`, `z`, and all the `T[i]`.

Check that your propagator passes the tests
`Element1DVarTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/Element1DVarTest.java?at=master>`_.
Those tests do not check that your propagator achieves domain
consistency for all the variables, so you have to write additional tests
in order to help convince yourself that it does so, if you take that direction.


The Stable Matching Problem
===========================

Complete the partial model `StableMatching.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/StableMatching.java?at=master>`_.
It makes use of the `Element1DVar` constraint you have just
implemented and is also a good example of the manipulation of logical and reified constraints.
Ensure that your implementation discovers all 6 solutions to the provided instance.

