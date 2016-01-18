# Caliko
The Caliko library is an implementation of the FABRIK inverse kinematics (IK) algorithm in the Java programming language.

It is released under the MIT software license. See LICENSE.txt for further details.

The FABRIK algorithm itself is explained in the following research paper:
Aristidou, A., & Lasenby, J. (2011). FABRIK: a fast, iterative solver for the inverse kinematics problem. Graphical Models, 73(5), 243-260.

## Structure

The library itself can be built into two separate .JAR files (one mandatory, one optional) suitable for inclusion in your IK projects:
- **caliko-_<version>_.jar**, and
- **caliko-visualisation-_<version>_**.jar.

The **caliko** folder contains source code for core IK portion of the library and is capable of running without any visualisation or external dependencies. 

The **caliko-visualisation** folder contains source code for the optional visualisation component of the library which provides the ability to draw various IK structures/chains/bones and depends on the 
core caliko functionality as well as the LWJGL3 library.

The **caliko-demo** folder contains source code for a demonstration of the library utilising both 2D and 3D IK chains in various configurations. It requires the caliko, caliko-visualisation and LWJGL3 
libraries.

## Build and Setup

You do not need to build the Caliko library yourself to use it, and may instead simply download the caliko and (optionally) caliko-visualisation .JAR files from this github location. Then, it's simply 
a matter of creating a new project, adding the libraries to your build path and using them. If you're using the visualisation component, then LWJGL3 must also be configured in your project's build path.

Alternatively, rather than using the caliko / caliko-visualisation .JARs, you could just copy the __au.edu.federation.*__ source files into your project, but you may then need to replace them with 
newer versions as changes/fixes are applied to Caliko - so using the latest .JAR versions is a cleaner and more hassle-free solution.

If you wish to build the Caliko library yourself, then it's configured to use maven for project / dependency management. Build scripts for both Linux and Windows are provided (obviously these depend 
upon you having maven installed and available in your execution path).
