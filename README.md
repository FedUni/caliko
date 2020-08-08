[![](https://jitpack.io/v/FedUni/caliko.svg)](https://jitpack.io/#FedUni/caliko)

# Caliko
The Caliko library is an implementation of the FABRIK inverse kinematics (IK) algorithm in the Java programming language, and is released under the MIT software license. See LICENSE.txt for further details.

The FABRIK algorithm is explained in the following research paper:
Aristidou, A., & Lasenby, J. (2011). FABRIK: a fast, iterative solver for the inverse kinematics problem. Graphical Models, 73(5), 243-260.

You can watch a short video outlining the setup and functionality of the Caliko library here:
[https://www.youtube.com/watch?v=wEtp4P2ucYk](https://www.youtube.com/watch?v=wEtp4P2ucYk)

If referencing this project in your research, APA style referencing for the accompaniying research paper is:
Lansley, A., Vamplew, P., Smith, P., & Foale, C. (2016). Caliko: An inverse kinematics software library implementation of the FABRIK algorithm. Journal of Open Research Software, 4(1).

## Structure

The library is a Maven multi-module project with the following modules:

The **caliko** module contains the core IK portion of the library and is capable of running without any visualisation or external dependencies. 

The **caliko-visualisation** module contains the optional visualisation component of the library which provides the ability to draw various IK structures/chains/bones and depends on the core caliko functionality as well as the LWJGL 3.2.2 library.

The **caliko-demo** module contains a demonstration of the library utilising both 2D and 3D IK chains in various configurations. It requires the caliko, caliko-visualisation and LWJGL 3.2.2 libraries.

## Build and Setup ##

To build yourself:

`git clone https://github.com/FedUni/caliko`

`mvn clean package` or `mvn clean install`

Alternatively, download a release from: 
[https://github.com/FedUni/caliko/releases](https://github.com/FedUni/caliko/releases)

## Documentation ##

Downloaded releases come packaged with a user guide and JavaDoc API documentation in the **docs** folder.

Alternatively, if you'd just like to take a peek at the user guide, it can be found here: [Caliko User Guide.pdf](https://github.com/FedUni/caliko/blob/master/caliko-distribution/src/site/docs/caliko-user-guide.pdf)

## Usage ##

To use the library in your own Maven project, declare the following dependencies:

1) If you only need the IK algorithm and do not need any visualisation:

```xml
    <dependency>
      <groupId>au.edu.federation.caliko</groupId>
      <artifactId>caliko</artifactId>
      <version>1.3.8</version>
    </dependency> 
```

2) If you need the IK algorithm and the visualisation:

```xml
    <dependency>
      <groupId>au.edu.federation.caliko.visualisation</groupId>
      <artifactId>caliko-visualisation</artifactId>
      <version>1.3.8</version>
    </dependency> 
```

## Demo controls ##

- Left mouse button sets target in 2D mode and enables mouse-look in 3D mode.
- Up/Down cursors - Toggle 2D/3D mode.
- Left/Right cursors - Prev/Next demo.
- Space - Pause/Resume target movement (3D).
- L - Toggle display lines.
- M - Toggle display models (3D).
- X - Toggle display axes (3D).
- F - Toggle fixed base mode.
- P - Toggle perspective / orthographic projection (3D)
- R - Toggle rotating base locations (3D)
- Esc - Quit.

## TODO ##

### High priority ##
- Nil.

### Medium priority ##
- Nil.

### Low priority ##
- Refactor entire library to use quaternions.
- Add parabolic constraint types.
- Streamline Model class object copying.
