# Caliko
The Caliko library is an implementation of the FABRIK inverse kinematics (IK) algorithm in the Java programming language.

It is released under the MIT software license. See LICENSE.txt for further details.

The FABRIK algorithm itself is explained in the following research paper:
Aristidou, A., & Lasenby, J. (2011). FABRIK: a fast, iterative solver for the inverse kinematics problem. Graphical Models, 73(5), 243-260.

## Structure

The library itself can be built into two separate .JAR files (one mandatory, one optional) suitable for inclusion in your IK projects:
- **caliko.jar**, and
- **caliko-visualisation.jar**.

The **caliko** folder contains source code for core IK portion of the library and is capable of running without any visualisation or external dependencies. 

The **caliko-visualisation** folder contains source code for the optional visualisation component of the library which provides the ability to draw various IK structures/chains/bones and depends on the 
core caliko functionality as well as the LWJGL3 library.

The **caliko-demo** folder contains source code for a demonstration of the library utilising both 2D and 3D IK chains in various configurations. It requires the caliko, caliko-visualisation and LWJGL3 
libraries.

## Build and Setup

You do not need to build the Caliko library yourself to use it, and may instead simply download a caliko release from this github location. 

Then, it's simply a matter of creating a new project, linking in the library .jars (and optionally the source code and javadoc archives)h and using the library functionality. If you're using the 
visualisation component, then the LWJGL3 library must also be configured for your project.

Alternatively, rather than using the caliko / caliko-visualisation .jar files, you could just copy the __au.edu.federation.*__ source files into your project, but you may then need to replace them with 
newer versions as changes/fixes are applied to Caliko - so using the latest .jar versions is a cleaner and more hassle-free solution.

If you wish to build the Caliko library yourself, then it's configured to use maven for project / dependency management and the entire library can be built by calling:
mvn package

This will create a file called **caliko.zip** in the top level directory, the contents of which are those from the created **RELEASE** folder.

## TODO

### High priority
- Add user documentation.
- Create and link to a high quality video demonstration of the library.

### Medium priority
- Add mechanism for individual IK chains in a structure to be updated to individual target locations (i.e. each chain holds a reference to a Vec2f/Vec3f which can be updated) so that a single call to 
structure.updateEmbeddedTargets() can then update each chain in the structure for its own specific target locations.

### Low priority
- Refactor entire library to use quaternions.
- Add parabolic constraint types.
- Streamline Model class object copying.
