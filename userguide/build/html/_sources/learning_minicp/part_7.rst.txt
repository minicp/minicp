*****************************************************************
Part 7: Table Constraints
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

* `Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyqVAjb5pUWPUQnrzcZMosRe>`_

* `Table Constraints <https://www.icloud.com/keynote/03fzCreyFOl0H7M9JoqYeGh0A#07-table-constraints>`_

Theoretical Questions
=====================

* `table <https://inginious.org/course/minicp/table>`_

Table Constraint
================

The Table constraint (also called the Extension constraint)
specifies the list of solutions (tuples) to which a vector of
variables can be fixed.

More precisely, given an array `X` containing `n` variables, and an array `T` of size `m × n`, this constraint holds:

.. math::

    \exists i: \forall j: T_{i,j} = X_j

That is, each row of the table is a valid way of fixing `X`.

The following table with five tuples and four variables will be used as an example for the sequel of this part.

.. list-table::
    :widths: auto
    :header-rows: 1
    :stub-columns: 1

    * - Tuple index
      - `X[0]`
      - `X[1]`
      - `X[2]`
      - `X[3]`
    * - 1
      - `0`
      - `1`
      - `2`
      - `3`
    * - 2
      - `0`
      - `0`
      - `3`
      - `2`
    * - 3
      - `2`
      - `1`
      - `0`
      - `3`
    * - 4
      - `3`
      - `2`
      - `1`
      - `2`
    * - 5
      - `3`
      - `0`
      - `1`
      - `1`


In this example, `X = {2, 1, 0, 3}` is valid, but not `X = {4, 3, 3, 3}` as there is no
such row in the table.

Many algorithms exist for filtering Table constraints.

One of the fastest filtering algorithms nowadays is Compact Table (CT) [CT2016]_.
In this exercise you will implement a simple version of CT.

CT works in two steps:

1. Compute the list of supported tuples. A tuple `T[i]` is supported if, *for each* index `j` of the tuple, the
   domain of the variable `X[j]` contains the value `T[i][j]`.
2. Filter the domains. For each variable `X[j]` and value `v` in the domain of `X[j]`: `v` can be removed if it is not
   used by any supported tuple.

Your task is to finish the implementation in
`TableCT.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/TableCT.java?at=master>`_.

`TableCT` maintains for each variable-value pair an array of bitsets:

.. code-block:: java

    private BitSet[][] supports;


where `supports[j][v]` is the (bit)set containing the supported tuple indices for `X[j]=v`.

Example
-------

Given the previous example where `X[0]` has the domain `{0, 1, 3}`, some of the the values of `supports` are:
`supports[0][0] = {1, 2}`,
`supports[0][1] = {}`, and
`supports[0][3] = {4,5}`.

From this we can infer two things. First, the value `1` does not support any tuple indices, so it can be removed
from the domain of `X[0]`. Additionally, given the union over all tuple indices supported by values in the domain of
`X[0]`, the tuple index `3` is not supported by any value in the domain of `X[0]`: the tuple index `3` can be removed
from all bitsets in `supports`.

Continuing with the same example and where `X[2]` has the domain `{0, 1}`, we see that tuples with indices `1`
and `2` are not supported by any value in the domain of `X[2]`; the tuples with indices `1` and `2` can therefore
be removed from all bitsets in `supports`. From this, we can infer that the value
`0` can be removed from the domain of variable `X[0]` as the tuple indices the value supported are no longer supported
by a variable (namely `X[2]`).

Using Bitsets
--------------

You may have assumed that the type of `supports` would have been `List<Integer>[][] supportedByVarVal`.
This is not the approach used by CT.

CT uses the concept of bitsets. A bitset is an array-like data structure that stores bits. Each bit is accessible by
its index. A bitset is in fact composed of an array of `Long`, which we in this context refer to as a *word*.
Each of these words stores 64 bits from the bitset.

Using this structure is convenient for our goal:

* The tuple with tuple index `i` becomes the `i`th bit of the bitset
  and is encoded as a `1` if the tuple is supported, and
  otherwise it is encoded as a `0`. In the traditional list/array
  representation, each supported tuple requires 32 bits to be represented.
* Computing the intersection and union of bitsets (and these are the main operations that will be made on `supportedByVarVal`)
  is very easy, thanks to the usage of bitwise operators included in all modern CPUs.

Java provides a default implementation of bitsets in the class BitSet, which we will use in this exercise.
We encourage you to read its documentation before continuing.

A Basic Implementation
----------------------

You will implement a version of CT that makes no use of the stateful structure (therefore it is probably much less efficient than the real CT algorithm).

You have to implement the `propagate()` method of the class `TableCT`. All class variables have already been initialized
for you.

You "simply" have to compute, for each call to `propagate()`:

* The tuples supported by each variable, which are the union of the tuples supported by the values in the domain of the
  variable.
* The intersection of the tuples supported by each variable. This intersection is the set of globally supported tuples.
* You can now intersect the set of globally supported tuples with each variable-value pair in `supports`.
  If the value supports no tuples (i.e., if the intersection is empty), then the value can be removed.

Make sure your implementation passes all the tests `TableTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/TableTest.java?at=master>`_.

.. [CT2016] Demeulenaere, J., Hartert, R., Lecoutre, C., Perez, G., Perron, L., Régin, J.-C., & Schaus, P. (2016). Compact-table: Efficiently filtering table constraints with reversible sparse bit-sets. International Conference on Principles and Practice of Constraint Programming, pp. 207-223. Springer. (`PDF <https://doi.org/10.1007/978-3-319-44953-1_14>`_)

Eternity Problem
======================

Fill in all the gaps in order to solve the Eternity II problem.

Your task is to finish the implementation in
`Eternity.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/Eternity.java?at=master>`_:

* Create the table.
* Model the problem using Table constraints.
* Search for a feasible solution using branching combinators.


