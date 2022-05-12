*****************************************************************
Part 10: Disjunctive Scheduling
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

* `Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyrAHSnNczQgftZO83TNJG_k>`_


* `Disjunctive Scheduling Slides <https://www.icloud.com/keynote/0afShCNGJqQiScHO6b3iHaUzA#10-disjunctive-scheduling>`_


Theoretical Questions
=====================

* `Disjunctive Scheduling <https://inginious.org/course/minicp/disjunctive>`_

* `Theta Trees <https://inginious.org/course/minicp/theta-trees>`_

Decomposing the Disjunctive Constraint
=======================================================

Your task is to make the Disjunctive constraint more efficient than by using the Cumulative constraint with unit capacity:

* Implement the constraint `IsLessOrEqualVar.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/IsLessOrEqualVar.java?at=master>`_
  for the reification `b iff x <= y`, where `b`, `x`, and `y` are variables.
  This will be useful for implementing the decomposition of the Disjunctive constraint.
* Test your implementation in `IsLessOrEqualVarTest.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/IsLessOrEqualVarTest.java?at=master>`_.
* Implement the decomposition with reified constraints for `Disjunctive.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* Make sure your implementation passes all the tests *except* `testOverloadChecker`, `testDetectablePrecedence`, and `testNotLast` (those are for the programming exercise below) in `DisjunctiveTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/DisjunctiveTest.java?at=master>`_.
* Test whether, as expected, this decomposition prunes more than the formulation with timetable filtering for the Cumulative constraint.
  For example, observe on `JobShop.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/JobShop.java?at=master>`_ that the number of backtracks is reduced with the decomposition compared to the formulation with Cumulative.
  Test for instance on the small instance `data/jobshop/sascha/jobshop-4-4-2` with 4 jobs, 4 machines, and 16 activities.


The Global Disjunctive Constraint: Overload Checker, Detectable Precedence, and Not-First-Not-Last
=========================================================================================================================

* Read and make sure you understand the implementation `ThetaTree.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/ThetaTree.java?at=master>`_.
  Some unit tests are implemented in `ThetaTreeTest.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/ThetaTreeTest.java?at=master>`_.
  To make sure you understand it, add a unit test with 4 activities and compare the results with a manual computation.
* Overload checking, detectable precedences, not-first-not-last, and edge finding only filter one side of the activities.
  To get the symmetrical filtering, implement the mirroring activities trick similarly to `Cumulative.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Cumulative.java?at=master>`_.
* Implement the overload checker in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* The overload checker should already make a big difference to prune the search tree.  Make sure that larger job-shop instances are now solved faster; for instance, `data/jobshop/sascha/jobshop-6-6-0` should now become easy to solve.
* Implement detectable precedences in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* Implement not-first-not-last in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* Make sure your implementation passes the tests `DisjunctiveTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/DisjunctiveTest.java?at=master>`_.
* (optional) Implement edge finding in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_ (you will also need to implement the ThetaLambdaTree data structure).
