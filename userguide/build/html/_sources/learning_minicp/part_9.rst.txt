*****************************************************************
Part 9: Cumulative Scheduling
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

`Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyoBRelEqivRod4V9nT-2xR0>`_

`Cumulative <https://www.icloud.com/keynote/0303F7s6hq8mynUi9pVzBGw_w#09-cumulative-scheduling>`_

Theoretical Questions
=====================

* `Cumulative <https://inginious.org/course/minicp/cumulative>`_



Cumulative Constraint: Decomposition
====================================

The `Cumulative` constraint models a scheduling resource with fixed capacity.
It has the following signature:

.. code-block:: java

    public Cumulative(IntVar[] start, int[] duration, int[] requirement, int capa)

where `capa` is the capacity of the resource and `start`, `duration`,
and `requirement` are arrays of equal size representing
the following properties of the activities:

* `start[i]` is the variable specifying the start time of activity `i`,
* `duration[i]` is the duration of activity `i`, and
* `requirement[i]` is the resource requirement of activity `i`.




The constraint ensures that the cumulative resource requirement of activities (also called the requirement profile)
never exceeds the capacity:

.. math:: \forall t: \sum_{i : t \in \left [start[i]..start[i]+duration[i]-1 \right ]} requirement[i] \le capa



The following example depicts three activities and their corresponding
requirement profile. As can be observed, the profile never exceeds
the capacity 4:


.. image:: ../_static/scheduling.svg
    :scale: 50
    :width: 400
    :alt: scheduling cumulative
    :align: center

It corresponds to the instantiation of the following `Cumulative` constraint:

.. code-block:: java

    Cumulative(start = [1, 2, 3], duration = [8, 3, 3], requirement = [1, 2, 2], capa = 4)



Implement `CumulativeDecomposition.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/CumulativeDecomposition.java?at=master>`_.
This is a decomposition or reformulation of the `Cumulative` constraint
in terms of simple arithmetic and logical constraints as
used in the equation above to describe its semantics.


At any point in time `t` the `BoolVar overlaps[i]`
designates whether activity `i` overlaps, potentially being performed at, `t` or not.
The overall resource requirement at `t` can then be obtained by:

.. math:: \sum_{i} overlaps[i] \cdot requirement[i] \le capa


First make sure you understand the following code, and then
add the few lines in its `TODO` task in order to make
sure `overlaps` has the intended meaning:



.. code-block:: java

    public void post() throws InconsistencyException {

        int min = Arrays.stream(start).map(s -> s.getMin()).min(Integer::compare).get();
        int max = Arrays.stream(end).map(e -> e.getMax()).max(Integer::compare).get();

        for (int t = min; t < max; t++) {

            BoolVar[] overlaps = new BoolVar[start.length];
            for (int i = 0; i < start.length; i++) {
                overlaps[i] = makeBoolVar(cp);

                // TODO
                // post the constraints to enforce
                // that overlaps[i] is true iff start[i] <= t && t < start[i] + duration[i]
                // hint: use IsLessOrEqual, introduce BoolVar, use views minus, plus, etc.
                //       logical constraints (such as logical and can be modeled with sum)

            }

            IntVar[] overlapHeights = makeIntVarArray(cp, start.length, i -> mul(overlaps[i], requirement[i]));
            IntVar cumHeight = sum(overlapHeights);
            cumHeight.removeAbove(capa);

        }


Check that your implementation passes the tests `CumulativeDecompTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/CumulativeDecompTest.java?at=master>`_.




Cumulative Constraint: Time-Table Filtering
==============================================

The timetable filtering algorithm introduced in  [TT2015]_
is an efficient yet simple filtering algorithm for `Cumulative`.

It is a two-stage algorithm:

1. Build an optimistic profile of the resource requirement and check that it does not exceed the capacity.
2. Filter the earliest start of the activities such that they are not in conflict with the profile.

Consider in the next example the depicted activity that can be executed anywhere between
the two solid brackets.
It cannot execute at its earliest start since this would
violate the capacity of the resource.
We thus need to postpone the activity until a point in time
where it can execute over its entire duration
without being in conflict with the profile and the capacity.
The earliest point in time is 7:


.. image:: ../_static/timetable2.svg
    :scale: 50
    :width: 600
    :alt: scheduling timetable1
    :align: center

**Profiles**


We provide a class `Profile.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Profile.java?at=master>`_
that is able to efficiently build a resource profile given an array of rectangles as input.
A rectangle has three attributes: `start`, `end`, and `height`, as shown next:

.. image:: ../_static/rectangle.svg
    :scale: 50
    :width: 250
    :alt: rectangle
    :align: center

Indeed, a profile is nothing more than a sequence of rectangles.
An example profile is given next. It is built from three input rectangles provided to the constructor of `Profile.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Profile.java?at=master>`_.

The profile consists of 7 contiguous rectangles.
The first rectangle, `R0`, starts at `Integer.MIN_VALUE` with a height of zero,
and the last rectangle, `R6`, ends at `Integer.MAX_VALUE`, also with a height of zero.
These two dummy rectangles are convenient because they guarantee
that there exists a rectangle in the profile for any point in time:


.. image:: ../_static/profile.svg
    :scale: 50
    :width: 650
    :alt: profile
    :align: center

Make sure you understand how to build and manipulate
`Profile.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Profile.java?at=master>`_.

Have a look at `ProfileTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/ProfileTest.java?at=master>`_
for some examples of profile construction.


**Filtering**



Implement `Cumulative.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Cumulative.java?at=master>`_.
You have three TODO tasks:

1. Build the optimistic profile from the mandatory parts.
2. Check that the profile is not exceeding the capacity.
3. Filter the earliest start of activities.

*TODO 1* is to build the optimistic profile
from the mandatory parts of the activities.
As can be seen in the next example, the mandatory part of an activity
is a part that is always executed whatever the start time of the activity
will be in its current domain.
It is the rectangle starting at `start[i].getMax()` that ends in `start[i].getMin()+duration[i]`
with a height equal to the resource requirement of the activity.
Be careful because not every activity has a mandatory part:

.. image:: ../_static/timetable1.svg
    :scale: 50
    :width: 600
    :alt: scheduling timetable1
    :align: center

*TODO 2* is to check that the profile is not exceeding the capacity.
You can check that each rectangle of the profile is not exceeding the capacity;
otherwise you throw an `InconsistencyException`.

*TODO 3* is to filter the earliest start of unfixed activities by postponing each
activity (if needed) to the earliest slot when it can be executed without exceeding the capacity.


.. code-block:: java

    for (int i = 0; i < start.length; i++) {
            if (!start[i].isFixed()) {
                // j is the index of the profile rectangle overlapping t
                int j = profile.rectangleIndex(start[i].getMin());
                // TODO 3: postpone i to a later point in time
                // hint:
                // Check that at every point in the interval
                // [start[i].getMin() ... start[i].getMin()+duration[i]-1]
                // there is enough remaining capacity.
                // You may also have to check the following profile rectangle(s).
                // Note that the activity you are currently postponing
                // may have contributed to the profile.
            }
        }


Check that your implementation passes the tests `CumulativeTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/CumulativeTest.java?at=master>`_.


.. [TT2015] Gay, S., Hartert, R., & Schaus, P. (2015). Simple and scalable time-table filtering for the cumulative constraint. International Conference on Principles and Practice of Constraint Programming, pp. 149-157. Springer. (`PDF <https://doi.org/10.1007/978-3-319-23219-5_11>`_)


The Resource-Constrained Project Scheduling Problem (RCPSP)
================================================================

A set of activities must be executed on a set of resources.

Your task is to terminate the implementation in
`RCPSP.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/RCPSP.java?at=master>`_:

* Post the Cumulative constraint
* Post the precedence constraints
* Add instructions to minimize the makespan
* Minimize the makespan

Several instances of increasing size are available, with 30, 60, 90, and 120 activities.
In order to test your model, note that the instance ``j30_1_1.rcp`` should have a minimum makespan of 43.
Do not expect to prove optimality for large-size instances, but you should reach it easily for 30 activities.
