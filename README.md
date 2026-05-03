# Min-Cost Flow Visualizer
## Project Overview
The Min-Cost Flow Visualizer is an interactive, educational desktop application designed to demonstrate advanced network flow algorithms. It allows users to step through the execution of Maximum Flow and Minimum Cost pathfinding algorithms (Ford-Fulkerson, Bellman-Ford, Dijkstra, and Floyd-Warshall) in real-time. By visualizing residual graphs, node potentials, augmenting paths, and distance matrices, the tool bridges the gap between theoretical graph mathematics and practical algorithmic implementation.

## Technologies Used
Language: Java (JDK 14+)
GUI Framework: Java Swing & AWT


## Prerequisites
- Java Development Kit (JDK): Version 14 or higher.

## Compilation & Execution (Standard)
Compile:

``
mkdir -p out
javac -d out src/main/Main.java
``

Run:

```
cd out
java main.Main
```

### Building a Native Executable (jpackage):
To create a standalone binary (Linux) or .exe (Windows):

Package your compiled classes into a Project.jar.

Place the JAR and your Graphs/ folder into a clean directory (e.g., deploy/).

Run:

``` 
jpackage --input ./deploy \
--name FlowAlgoVisualizer \
--main-jar Project.jar \
--main-class main.Main \
--type app-image
``` 

## Functionalities
![image](/annotated)

- 1: This is the Graph canvas here the Graphs are visually represented
- 2: Show Min cut button is clickable at the end of the algorithm to show visually the minCut
- 3: Start and pause to s run the Algorithm the speed can be changed with the slider (8)
- 4: Go one step further in the Algorithm
- 5: Run the Algorithm until the next Major Step
- 6: Step one step back
- 7: Reset the Graph to 0
- 8: Slider to stepped up or slow down the Algorithm running
- 9: Current Algorithm
- 10-13: The buttons to start the different Algorithms
- 14: Export the current Graph in a dot file (in current state)
- 15: Load a Graph in the following structure: \
(1ere ligne : #nodes #arcs s t) \
(extremité initiale, extremité terminale, capacité, coût, unitaire)\
(... , ... , ... , ... , ...)
- 16: Explanation of the current step
- 17: All current variables; Algo data shows important data for the current Algos

