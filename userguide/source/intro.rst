.. _intro:



*******
Preface
*******

This document is made for anyone who wants to learn
constraint programming using MiniCP as a support.

This tutorial will continuously evolve.
Don't hesitate to give us feedback or suggestions for improvement.
You are also welcome to report any mistake or bug.


What is MiniCP
==============
The success of the MiniSAT solver has largely contributed to the dissemination of (CDCL) SAT solvers.
The MiniSAT solver has a neat and minimalist architecture that is well documented.
We believe the CP community is currently missing such a solver that would permit newcomers to demystify the internals of CP technology. 
We introduce MiniCP, a white-box bottom-up teaching framework for CP implemented in Java. 
MiniCP is voluntarily missing many features that you would find in a commercial or complete open-source solver. 
The implementation, although inspired by state-of-the-art solvers, is not focused on efficiency but rather on readability to convey the concepts as clearly as possible.
MiniCP is small and well tested.


Javadoc
=======

The `Javadoc API <https://minicp.bitbucket.io/apidocs/>`_.


.. _install:

Install MiniCP
==============

.. raw:: html

    <iframe width="560" height="315" src="https://www.youtube.com/embed/VF_vkCnOp88?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>


MiniCP source code is available from bitbucket_.

**Using an IDE**

We recommend using IntelliJ_ or Eclipse_.

From IntelliJ_ you can import the project:

.. code-block:: none

    Open > (select pom.xml in the minicp directory and open as new project)


From Eclipse_ you can import the project:

.. code-block:: none

    Import > Maven > Existing Maven Projects (select minicp directory)


**From the command line**

Using maven_ command line you can do:


.. code-block:: none

    $mvn compile # compile all the project
    $mvn test    # run all the test suite

Some other useful commands:

.. code-block:: none

    $mvn checkstyle:checktyle   # generates a report in target/site/checkstyle.html
    $mvn findbugs:gui           # opens a gui with potential source of bugs in your code
    $mvn jacoco:report          # creates a cover report in target/site/jacoco/index.html
    $mvn javadoc:javadoc        # creates javadoc in target/site/apidocs/index.html


.. _bitbucket: https://bitbucket.org/minicp/minicp
.. _IntelliJ: https://www.jetbrains.com/idea/
.. _Eclipse: https://www.eclipse.org
.. _maven: https://maven.apache.org


Getting Help with MiniCP
========================

You'll get the greatest chance of getting answers to your questions by using the MiniCP usergroup_.

.. _usergroup: https://groups.google.com/g/mini-cp


Who Uses MiniCP?
================

If you use it for teaching or for research, please let us know and we will add you in this list.

* UCLouvain, Belgium, `INGI2365 <https://uclouvain.be/cours-2017-LINGI2365>`_, teacher: Pierre Schaus.
* ACP, `Summer School 2017 <https://school.a4cp.org/summer2017/>`_, Porquerolles, France, teacher: Pierre Schaus.
* Université de Nice, France, `Master in CS <http://unice.fr/formation/formation-initiale/sminf1212>`_, teachers: Arnaud Malapert and Marie Pelleau.


Citing MiniCP
=============

If you find MiniCP useful for your research or teaching, then you can
cite the following paper (`PDF <https://doi.org/10.1007/s12532-020-00190-7>`_):

.. code-block:: latex

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




