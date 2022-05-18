![build](https://github.com/minicp/minicp/actions/workflows/publish.yml/badge.svg)


# README #

* MiniCP technical documentation, exercises, etc 
can be found at <http://minicp.org>.
* MiniCP is a Java project built with Maven (<https://maven.apache.org>).




System Requirements
-------------------

* JDK:
 1.8 or above (this is to execute Maven; it still allows you to build against 1.3
 and prior JDKs).
* Memory:
 No minimum requirement.
* Disk:
 Approximately 10MB is required for the Maven installation itself. Additional disk space will be used for your local Maven repository. The size
 of your local repository will vary depending on usage, but expect at least 500MB.
* Operating system: 
    * Windows: Windows 2000 or above.
    * Unix-based operating systems (Linux, Solaris, and macOS) and others: No minimum requirement.

Installing Maven
----------------

1. Unpack the archive where you would like to store the binaries, e.g.:

    - Unix-based operating systems (Linux, Solaris, and macOS):
      ```
      tar zxvf apache-maven-3.x.y.tar.gz 
      ```
    - Windows:
      ```
      unzip apache-maven-3.x.y.zip
      ```

    A directory called `apache-maven-3.x.y` will be created.

2. Add the bin directory to your PATH, e.g.:

    - Unix-based operating systems (Linux, Solaris, and macOS):
      ```
      export PATH=/usr/local/apache-maven-3.x.y/bin:$PATH
      ```
    - Windows:
      ```
      set PATH="v:\program files\apache-maven-3.x.y\bin";%PATH%
      ```

3. Make sure `JAVA_HOME` is set to the location of your JDK.

4. Run `mvn --version` to verify that it is correctly installed.


For the complete documentation, see
<https://maven.apache.org/download.html#Installation>.


Commands for executing a model and running the test suite
---------------------------------------------------------

```
 cd minicp/
 mvn compile                                              # compile the project
 mvn exec:java -Dexec.mainClass="minicp.examples.NQueens" # execute the n-queens model
 mvn test                                                 # run the test suite
```

Using the IntelliJ IDEA editor
--------------------------------------------------

We recommend IntelliJ IDEA (<https://www.jetbrains.com/idea/download>).

Do > `File | Open Project (Alt + F + O)` and specify the path to `pom.xml`
as explained at
<https://blog.jetbrains.com/idea/2008/03/opening-maven-projects-is-easy-as-pie>.

Content
-------------

```
./src/main/java/                   # the implementation of MiniCP
./src/main/java/minicp/examples/   # model examples
./src/test/java/                   # the test suite
./data/                            # input instances
```




