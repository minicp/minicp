*****************************************************************
Part 1: Overview of CP, Filtering, Search, Consistency, Fixpoint
*****************************************************************

We propose a set of exercises to extend MiniCP with useful features.
By doing these exercises you will gradually progress in your understanding of CP.
For each exercise, we ask you to implement JUnit tests to make sure that
your implementation works as expected.
If you don't test each feature independently you take the risk to
lose a lot of time finding very difficult bugs.


*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

* `Lectures on Youtube <https://www.youtube.com/playlist?list=PLq6RpCDkJMyoH9ujmz6TBoAwT5Ax8RwqE>`_

* `Overview of CP, Filtering, Search, Consistency, Fix-point <https://www.icloud.com/keynote/0ccu9JZiD8ZEhSsB-LnQiRcwA#01-intro>`_

Theoretical Questions
=====================

* `Fixpoint Algorithm <https://inginious.org/course/minicp/fix-point>`_
* `Consistency <https://inginious.org/course/minicp/consistencies>`_

Forking MiniCP to Do the Programming Exercices
===============================================

`Follow the tutorial
<https://inginious.org/course/minicp/minicp-install-1>`_ and then clone your repository.

:ref:`Then follow this tutorial to import it in your IDE <install>`.

Less-or-equal Reified Constraint
================================

Implement `IsLessOrEqual.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/IsLessOrEqual.java?at=master>`_.

This is a propagator for the constraint `b iff x <= c`, which is called the `reified constraint` (or: `reification`) of the constraint `x <= c`: it holds if Boolean variable `b` is true if and only if variable `x` is less than or equal to value `c`.

For example, the constraint holds for

.. code-block:: java

    b = true , x = 4, c = 5
    b = false, x = 4, c = 2


but is violated for

.. code-block:: java

    b = true , x = 5, c = 4
    b = false, x = 2, c = 4

For an example of reification, you can look at `IsEqual.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/IsEqual.java?at=master>`_.

Check that your implementation passes the tests `IsLessOrEqualTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/IsEqualTest.java?at=master>`_.
