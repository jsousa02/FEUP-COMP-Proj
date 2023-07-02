# Compilers Project

Project development team and contributions:
 - André Lima <[up202008169@edu.fe.up.pt](mailto:up202008169@edu.fe.up.pt)> (34%)
 - Jorge Sousa <[up202006140@edu.fe.up.pt](mailto:up202006140@edu.fe.up.pt)> (33%) 
 - Mariana Lobão <[up202004260@edu.fe.up.pt](mailto:up202004260@edu.fe.up.pt)> (33%)
 
Self-assessment: 20.0

Extra elements:
 - Optional else  statement in conditional statements.
 - In if statements, if the condition is always true or always false, the corresponding branch is the only one that is kept (the condition is also removed).

For this project, you need to install [Java](https://jdk.java.net/), [Gradle](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Project setup

There are some import folders in the repository. Your development source code is inside the subfolder named ``src/main``. Specifically, your initially application is in the folder ``src/main/pt/up/fe/comp2023``, and the grammar is in the subfolder ``src/main/antlr/comp2023/grammar``. Finally, the subfolder named ``test`` contains your unit tests.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/jmm/bin``. For convenience, there are two script files in the root folder, one for Windows (``jmm.bat``) and another for Linux (``jmm``), that call this launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.
