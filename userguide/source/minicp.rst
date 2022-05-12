.. _minicp:


************
Learn MiniCP
************

This tutorial is based on the course "LINFO2365 Constraint Programming" given at UCLouvain by Pierre Schaus.

.. toctree::
        :maxdepth: 2

        learning_minicp/part_1
        learning_minicp/part_2
        learning_minicp/part_3
        learning_minicp/part_4
        learning_minicp/part_5
        learning_minicp/part_6
        learning_minicp/part_7
        learning_minicp/part_8
        learning_minicp/part_9
        learning_minicp/part_10
        learning_minicp/part_11

Outcomes
========

Learning outcomes by studying MiniCP:

From a state and inference perspective, specific learning outcomes include:

* Trailing and state reversion
* Domain and variable implementation – Propagation queue
* Arithmetic Constraints
* Logical Constraints
* Reified Constraints
* Global Constraints (including for scheduling)
* Views


From a search perspective, the outcomes include:

* Backtracking algorithms and depth first search
* Branch and Bound for Constraint Optimization
* Incremental Computation
* Variable and Value Heuristics implementation
* Searching with phases
* Large Neighborhood Search

While, from a modeling perspective, the outcomes include:

* Redundant constraints
* Bad smells and good smells: model preferably with Element constraints instead of 0/1 variables
* Breaking symmetries
* Scheduling: producer consumer problems, etc.
* Design problem specific heuristics and search


MiniCP Article
===============

The complete architecture of MiniCP is described in this `paper <_static/mini-cp.pdf>`_ (`publisher PDF <https://doi.org/10.1007/s12532-020-00190-7>`_) (`errata and delta with current source code of MiniCP <http://user.it.uu.se/~pierref/courses/COCP/slides/delta.txt>`_):

.. code-block:: text


        @article{cite-key,
                Author = {Michel, L. and Schaus, P. and Van Hentenryck, P.},
                Doi = {10.1007/s12532-020-00190-7},
                Id = {Michel2021},
                Isbn = {1867-2957},
                Journal = {Mathematical Programming Computation},
                Number = {1},
                Pages = {133-184},
                Title = {MiniCP: a lightweight solver for constraint programming},
                Ty = {JOUR},
                Url = {https://doi.org/10.1007/s12532-020-00190-7},
                Volume = {13},
                Year = {2021}}




MiniCP XCSP3 Mini Solver
========================

We provide under the form of a student project the possibility to participate to the XCSP3 MiniSolver Competition with MiniCP.
All the interfacing with XCSP3 tools and the parsing of XCSP3 format is done for you.
You can focus on the only interesting part: make your solver as efficient as possible.

* `XCSP3 Website <http://xcsp3.org/competition>`_
