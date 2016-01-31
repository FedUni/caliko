package au.edu.federation.alansley;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikBone3D.BoneConnectionPoint3D;
import au.edu.federation.caliko.FabrikChain3D;
import au.edu.federation.caliko.FabrikChain3D.BaseboneConstraintType3D;
import au.edu.federation.caliko.FabrikStructure3D;
import au.edu.federation.caliko.FabrikJoint3D.JointType;
import au.edu.federation.caliko.utils.Colour4f;
import au.edu.federation.caliko.utils.Mat4f;
import au.edu.federation.caliko.utils.Utils;
import au.edu.federation.caliko.utils.Vec3f;
import au.edu.federation.caliko.visualisation.Axis;
import au.edu.federation.caliko.visualisation.Camera;
import au.edu.federation.caliko.visualisation.FabrikConstraint3D;
import au.edu.federation.caliko.visualisation.FabrikLine3D;
import au.edu.federation.caliko.visualisation.FabrikModel3D;
import au.edu.federation.caliko.visualisation.Grid;
import au.edu.federation.caliko.visualisation.MovingTarget3D;

/**
 * Class to demonstrate some of the features of the Caliko library in 3D.
 * 
 * @author Al Lansley
 * @version 0.7 - 09/01/2016
 */
public class CalikoDemo3D extends CalikoDemo
{
	// Define cardinal axes
	static final Vec3f X_AXIS = new Vec3f(1.0f, 0.0f, 0.0f);
	static final Vec3f Y_AXIS = new Vec3f(0.0f, 1.0f, 0.0f);
	static final Vec3f Z_AXIS = new Vec3f(0.0f, 0.0f, 1.0f);
	
	// Defaults
	// Note: Bone initial directions will be going 'into' the screen along the -Z axis
	static Vec3f defaultBoneDirection   = new Vec3f(Z_AXIS).negated();
	static float defaultBoneLength      = 10.0f;
	static float boneLineWidth          = 5.0f;
	static float constraintLineWidth    = 2.0f;	
	static float baseRotationAmountDegs = 0.3f;
	
	// Set yo a camera which we'll use to navigate. Params: location, orientation, width and height of window.
	static Camera camera = new Camera(new Vec3f(0.0f, 00.0f, 150.0f), new Vec3f(), Application.windowWidth, Application.windowHeight);
		
	// Setup some grids to aid orientation
	static float extent       = 1000.0f;
	static float gridLevel    = 100.0f;
	static int   subdivisions = 20;
	static Grid  lowerGrid    = new Grid(extent, extent, -gridLevel, subdivisions);
	static Grid  upperGrid    = new Grid(extent, extent,  gridLevel, subdivisions);
	
	// An axis to show the X/Y/Z orientation of each bone. Params: Axis length, axis line width
	static Axis axis = new Axis(3.0f, 1.0f);
		
	// A constraint we can use to draw any joint angle restrictions of ball and hinge joints
	static FabrikConstraint3D constraint = new FabrikConstraint3D();
		
	// A simple Wavefront .OBJ format model of a pyramid to display around each bone (set to draw with a 1.0f line width)
	static FabrikModel3D model = new FabrikModel3D("/pyramid.obj", 1.0f);

	// Setup moving target. Params: location, extents, interpolation frames, grid height for vertical bar
	static MovingTarget3D target = new MovingTarget3D(new Vec3f(), new Vec3f(60.0f), 200, gridLevel);
	
	public static FabrikStructure3D mStructure;
		
	/**
	 * Constructor.
	 * 
	 * @param	demoNumber	The number of the demo to set up.
	 */
	public CalikoDemo3D(int demoNumber) { setup(demoNumber); }
	
	/**
	 * Set up a demo consisting of an arrangement of 3D IK chains with a given configuration.
	 * 
	 * @param	demoNumber	The number of the demo to set up.
	 */
	public void setup(int demoNumber)
	{
		String demoName = new String();
		
		switch (demoNumber)
		{
			case 1: {
				demoName            = "Demo 1 - Unconstrained bones";
				mStructure          = new FabrikStructure3D(demoName);
				Colour4f boneColour = new Colour4f(Utils.GREEN);
				
				// Create a new chain...				
				FabrikChain3D chain = new FabrikChain3D();
					
				// ...then create a basebone, set its draw colour and add it to the chain.
				Vec3f startLoc        = new Vec3f(0.0f, 0.0f, 40.0f);
				Vec3f endLoc          = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
				FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
				basebone.setColour(boneColour);
				chain.addBone(basebone);
				
				// Add additional consecutive, unconstrained bones to the chain				
				for (int boneLoop = 0; boneLoop < 7; boneLoop++)
				{
					boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
					chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
				}
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);					
				break;
			}	
				
			case 2: {
				demoName                  = "Demo 2 - Rotor / Ball Joint Constrained Bones";
				mStructure                = new FabrikStructure3D(demoName);
				int numChains             = 3;
				float rotStep             = 360.0f / (float)numChains;
				float constraintAngleDegs = 45.0f;
				Colour4f boneColour       = new Colour4f();
				
				for (int chainLoop = 0; chainLoop < numChains; ++chainLoop) 
				{
					// Create a new chain
					FabrikChain3D chain = new FabrikChain3D();
					
					// Choose the bone colour
					switch (chainLoop % numChains) 
					{
						case 0:	boneColour.set(Utils.MID_RED);   break;
						case 1:	boneColour.set(Utils.MID_GREEN); break;
						case 2:	boneColour.set(Utils.MID_BLUE);  break;
					}
					
					// Set up the initial base bone location...
					Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
					startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)chainLoop);
					Vec3f endLoc   = new Vec3f(startLoc);
					endLoc.z      -= defaultBoneLength;
					
					// ...then create a base bone, set its colour and add it to the chain.
					FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
					basebone.setColour(boneColour);					
					chain.addBone(basebone);
					
					// Add additional consecutive rotor (i.e. ball joint) constrained bones to the chain					
					for (int boneLoop = 0; boneLoop < 7; boneLoop++)
					{
						boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);						
						chain.addConsecutiveRotorConstrainedBone(defaultBoneDirection, defaultBoneLength, constraintAngleDegs, boneColour);						
					}
					
					// Finally, add the chain to the structure
					mStructure.addChain(chain);					
				}	
				break;				
			}		
			
			case 3:	{
				demoName                          = "Demo 3 - Rotor Constrained Base Bones";
				mStructure                        = new FabrikStructure3D(demoName);
				int numChains                     = 3;
				float rotStep                     = 360.0f / (float)numChains;
				float baseBoneConstraintAngleDegs = 20.0f;
				
				// ... and add multiple chains to it.
				Colour4f boneColour          = new Colour4f();
				Colour4f baseBoneColour      = new Colour4f();					
				Vec3f baseBoneConstraintAxis = new Vec3f();
				for (int chainLoop = 0; chainLoop < numChains; ++chainLoop) 
				{					
					// Choose the bone colours and base bone constraint axes
					switch (chainLoop % 3) 
					{
						case 0:
							boneColour.set(Utils.MID_RED);
							baseBoneColour.set(Utils.RED);
							baseBoneConstraintAxis = X_AXIS;
							break;
						case 1:
							boneColour.set(Utils.MID_GREEN);
							baseBoneColour.set(Utils.MID_GREEN);
							baseBoneConstraintAxis = Y_AXIS;
							break;
						case 2:
							boneColour.set(Utils.MID_BLUE);
							baseBoneColour.set(Utils.BLUE);
							baseBoneConstraintAxis = Z_AXIS.negated();
							break;
					}
				
					// Create a new chain
					FabrikChain3D chain = new FabrikChain3D();
					
					// Set up the initial base bone location...
					Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
					startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)chainLoop);					
					Vec3f endLoc   = startLoc.plus( baseBoneConstraintAxis.times( defaultBoneLength * 2.0f) );
					
					// ...then create a base bone, set its colour, add it to the chain and specify that it should be global rotor constrained.
					FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
					basebone.setColour(baseBoneColour);					
					chain.addBone(basebone);
					chain.setRotorBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_ROTOR, baseBoneConstraintAxis, baseBoneConstraintAngleDegs);
				
					// Add additional consecutive, unconstrained bones to the chain
					for (int boneLoop = 0; boneLoop < 7; boneLoop++)
					{	
						boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.5f) : boneColour.darken(0.5f);
						chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
					}
					
					// Finally, add the chain to the structure
					mStructure.addChain(chain);
				}
				break;				
			}
				
			case 4:	{
				demoName      = "Demo 4 - Freely Rotating Global Hinges";
				mStructure    = new FabrikStructure3D(demoName);
				int numChains = 3;
				float rotStep = 360.0f / (float)numChains;
				
				// We'll create a circular arrangement of 3 chains which are each constrained about different global axes.
				// Note: Although I've used the cardinal X/Y/Z axes here, any axis can be used.
				Vec3f globalHingeAxis = new Vec3f();
				for (int chainLoop = 0; chainLoop < numChains; ++chainLoop)
				{	
					// Set colour and axes							
					Colour4f chainColour = new Colour4f();
					switch (chainLoop % numChains)
					{
						case 0:
							chainColour.set(Utils.RED);
							globalHingeAxis = X_AXIS;
							break;
						case 1:
							chainColour.set(Utils.GREEN);
							globalHingeAxis = Y_AXIS;
							break;
						case 2:
							chainColour.set(Utils.BLUE);
							globalHingeAxis = Z_AXIS;
							break;
					}
					
					// Create a new chain
					FabrikChain3D chain = new FabrikChain3D();
					
					// Set up the initial base bone location...
					Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
					startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)chainLoop);
					Vec3f endLoc   = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
					
					// ...then create a base bone, set its colour, and add it to the chain.
					FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
					basebone.setColour(chainColour);
					chain.addBone(basebone);
					
					// Add alternating global hinge constrained and unconstrained bones to the chain
					for (int boneLoop = 0; boneLoop < 7; boneLoop++)
					{
						if (boneLoop % 2 == 0)
						{
							chain.addConsecutiveFreelyRotatingHingedBone(defaultBoneDirection, defaultBoneLength, JointType.GLOBAL_HINGE, globalHingeAxis, Utils.GREY);
						}
						else
						{
							chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, chainColour);
						}						
					}
					
					// Finally, add the chain to the structure
					mStructure.addChain(chain);					
				}					
				break;
			}
				
			case 5:	{
				demoName   = "Demo 5 - Global Hinges With Reference Axis Constraints";
				mStructure = new FabrikStructure3D(demoName);
				
				// Create a new chain				
				FabrikChain3D chain = new FabrikChain3D();
					
				// Set up the initial base bone location...
				Vec3f startLoc = new Vec3f(0.0f, 30f, -40.0f);
				Vec3f endLoc   = new Vec3f(startLoc);
				endLoc.y      -= defaultBoneLength;
					
				// ...then create a base bone, set its colour, and add it to the chain.
				FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
				basebone.setColour(Utils.YELLOW);
				chain.addBone(basebone);
					
				// Add alternating global hinge constrained and unconstrained bones to the chain
				float cwDegs  = 120.0f;
				float acwDegs = 120.0f;
				for (int boneLoop = 0; boneLoop < 8; ++boneLoop)
				{
					if (boneLoop % 2 == 0)
					{
						// Params: bone direction, bone length, joint type, hinge rotation axis, clockwise constraint angle, anticlockwise constraint angle, hinge constraint reference axis, colour
						// Note: There is a version of this method where you do not specify the colour - the default is to draw the bone in white.
						chain.addConsecutiveHingedBone(Y_AXIS.negated(), defaultBoneLength, JointType.GLOBAL_HINGE, Z_AXIS, cwDegs, acwDegs, Y_AXIS.negated(), Utils.GREY );
					}
					else
					{
						chain.addConsecutiveBone(Y_AXIS.negated(), defaultBoneLength, Utils.MID_GREEN);
					}
				}
					
				// Finally, add the chain to the structure
				mStructure.addChain(chain);	
				break;				
			}
				
			case 6: {
				demoName      = "Demo 6 - Freely Rotating Local Hinges";
				mStructure    = new FabrikStructure3D(demoName);
				int numChains = 3;
				
				// We'll create a circular arrangement of 3 chains with alternate bones each constrained about different local axes.
				// Note: Local hinge rotation axes are relative to the rotation matrix of the previous bone in the chain.
				Vec3f hingeRotationAxis  = new Vec3f();;
				
				float rotStep = 360.0f / (float)numChains;
				for (int loop = 0; loop < numChains; loop++)
				{	
					// Set colour and axes							
					Colour4f chainColour = new Colour4f();
					switch (loop % 3)
					{
						case 0:
							chainColour = Utils.RED;
							hingeRotationAxis  = new Vec3f(X_AXIS);
							break;
						case 1:
							chainColour = Utils.GREEN;
							hingeRotationAxis = new Vec3f(Y_AXIS);
							break;
						case 2:
							chainColour = Utils.BLUE;
							hingeRotationAxis = new Vec3f(Z_AXIS);
							break;
					}
					
					// Create a new chain
					FabrikChain3D chain = new FabrikChain3D();
					
					// Set up the initial base bone location...
					Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
					startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)loop);					
					Vec3f endLoc   = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
					
					// ...then create a base bone, set its colour, and add it to the chain.
					FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
					basebone.setColour(chainColour);
					chain.addBone(basebone);
					
					// Add alternating local hinge constrained and unconstrained bones to the chain
					for (int boneLoop = 0; boneLoop < 6; boneLoop++)
					{
						if (boneLoop % 2 == 0)
						{
							chain.addConsecutiveFreelyRotatingHingedBone(defaultBoneDirection, defaultBoneLength, JointType.LOCAL_HINGE, hingeRotationAxis, Utils.GREY);
						}
						else
						{
							chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, chainColour);
						}
					}
					
					// Finally, add the chain to the structure
					mStructure.addChain(chain);					
				}					
				break;
			}
			
			case 7: {
				demoName      = "Demo 7 - Local Hinges with Reference Axis Constraints";
				mStructure    = new FabrikStructure3D(demoName);
				int numChains = 3;
				
				// We'll create a circular arrangement of 3 chains with alternate bones each constrained about different local axes.
				// Note: Local hinge rotation axes are relative to the rotation matrix of the previous bone in the chain.
				Vec3f hingeRotationAxis  = new Vec3f();
				Vec3f hingeReferenceAxis = new Vec3f();
				
				float rotStep = 360.0f / (float)numChains;
				for (int loop = 0; loop < numChains; loop++)
				{	
					// Set colour and axes							
					Colour4f chainColour = new Colour4f();
					switch (loop % 3)
					{
						case 0:
							chainColour        = Utils.RED;
							hingeRotationAxis  = new Vec3f(X_AXIS);
							hingeReferenceAxis = new Vec3f(Y_AXIS);
							
							break;
						case 1:
							chainColour        = Utils.GREEN;
							hingeRotationAxis  = new Vec3f(Y_AXIS);
							hingeReferenceAxis = new Vec3f(X_AXIS);
							break;
						case 2:
							chainColour        = Utils.BLUE;
							hingeRotationAxis  = new Vec3f(Z_AXIS);
							hingeReferenceAxis = new Vec3f(Y_AXIS);
							break;
					}
					
					// Create a new chain
					FabrikChain3D chain = new FabrikChain3D();
					
					// Set up the initial base bone location...
					Vec3f startLoc = new Vec3f(0.0f, 0.0f, -40.0f);
					startLoc       = Vec3f.rotateYDegs(startLoc, rotStep * (float)loop);					
					Vec3f endLoc   = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
					
					// ...then create a base bone, set its colour, and add it to the chain.
					FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
					basebone.setColour(chainColour);
					chain.addBone(basebone);
					
					// Add alternating local hinge constrained and unconstrained bones to the chain
					float constraintAngleDegs = 90.0f;
					for (int boneLoop = 0; boneLoop < 6; boneLoop++)
					{
						if (boneLoop % 2 == 0)
						{
							chain.addConsecutiveHingedBone(defaultBoneDirection, defaultBoneLength, JointType.LOCAL_HINGE, hingeRotationAxis, constraintAngleDegs, constraintAngleDegs, hingeReferenceAxis, Utils.GREY);
						}
						else
						{
							chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, chainColour);
						}
					}
					
					// Finally, add the chain to the structure
					mStructure.addChain(chain);	
				}
				break;
			}
			
			case 8: {
				demoName            = "Demo 8 - Connected Chains";
				mStructure          = new FabrikStructure3D(demoName);
				Colour4f boneColour = new Colour4f(Utils.GREEN);
				
				// Create a new chain...				
				FabrikChain3D chain = new FabrikChain3D();
					
				// ...then create a basebone, set its draw colour and add it to the chain.
				Vec3f startLoc        = new Vec3f(0.0f, 0.0f, 40.0f);
				Vec3f endLoc          = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
				FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
				basebone.setColour(boneColour);
				chain.addBone(basebone);
				
				// Add additional consecutive, unconstrained bones to the chain				
				for (int boneLoop = 0; boneLoop < 5; boneLoop++)
				{
					boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
					chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
				}
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);		
					
				FabrikChain3D secondChain = new FabrikChain3D("Second Chain");
				FabrikBone3D base = new FabrikBone3D( new Vec3f(100.0f), new Vec3f(110.0f) );
				secondChain.addBone(base);
				secondChain.addConsecutiveBone(X_AXIS, 20.0f);
				secondChain.addConsecutiveBone(Y_AXIS, 20.0f);
				secondChain.addConsecutiveBone(Z_AXIS, 20.0f);
				
				// Set the colour of all bones in the chain in a single call, then connect it to the chain...
				secondChain.setColour(Utils.RED);
				mStructure.connectChain(secondChain, 0, 0, BoneConnectionPoint3D.START);
				
				// ...we can keep adding the same chain at various points if we like, because the chain we
				// connect is actually a clone of the one we provide, and not the original 'secondChain' argument.
				secondChain.setColour(Utils.WHITE);
				mStructure.connectChain(secondChain, 0, 2, BoneConnectionPoint3D.START);
				
				// We can also set connect the chain to the end of a specified bone (this overrides the START/END 
				// setting of the bone we connect to).
				secondChain.setColour(Utils.BLUE);
				mStructure.connectChain(secondChain, 0, 4, BoneConnectionPoint3D.END);
				break;
			}
			
			case 9: {
				demoName            = "Demo 9 - Global Rotor Constrained Connected Chains";
				mStructure          = new FabrikStructure3D(demoName);
				Colour4f boneColour = new Colour4f(Utils.GREEN);
				
				// Create a new chain...				
				FabrikChain3D chain = new FabrikChain3D();
					
				// ...then create a basebone, set its draw colour and add it to the chain.
				Vec3f startLoc        = new Vec3f(0.0f, 0.0f, 40.0f);
				Vec3f endLoc          = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
				FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
				basebone.setColour(boneColour);
				chain.addBone(basebone);
				
				// Add additional consecutive, unconstrained bones to the chain				
				for (int boneLoop = 0; boneLoop < 7; boneLoop++)
				{
					boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
					chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
				}
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);					
				
				FabrikChain3D secondChain = new FabrikChain3D("Second Chain");
				FabrikBone3D base = new FabrikBone3D( new Vec3f(), new Vec3f(15.0f, 0.0f, 0.0f) );
				secondChain.addBone(base);
				secondChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_ROTOR, X_AXIS, 45.0f);				
				
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.setColour(Utils.RED);
				
				mStructure.connectChain(secondChain, 0, 3, BoneConnectionPoint3D.START);
				
				FabrikChain3D thirdChain = new FabrikChain3D("Second Chain");
				base = new FabrikBone3D( new Vec3f(), new Vec3f(0.0f, 15.0f, 0.0f) );
				thirdChain.addBone(base);
				thirdChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_ROTOR, Y_AXIS, 45.0f);
								
				thirdChain.addConsecutiveBone(Y_AXIS, 15.0f);
				thirdChain.addConsecutiveBone(Y_AXIS, 15.0f);
				thirdChain.addConsecutiveBone(Y_AXIS, 15.0f);
				thirdChain.setColour(Utils.BLUE);
				
				mStructure.connectChain(thirdChain, 0, 6, BoneConnectionPoint3D.START);
				
				break;
			}
			
			case 10: {
				demoName            = "Demo 10 - Local Rotor Constrained Connected Chains";
				mStructure          = new FabrikStructure3D(demoName);
				Colour4f boneColour = new Colour4f(Utils.GREEN);
				
				// Create a new chain...				
				FabrikChain3D chain = new FabrikChain3D();
					
				// ...then create a basebone, set its draw colour and add it to the chain.
				Vec3f startLoc        = new Vec3f(0.0f, 0.0f, 40.0f);
				Vec3f endLoc          = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
				FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
				basebone.setColour(boneColour);
				chain.addBone(basebone);
				
				// Add additional consecutive, unconstrained bones to the chain				
				for (int boneLoop = 0; boneLoop < 7; boneLoop++)
				{
					boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
					chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
				}
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);		
				
				// Create a second chain which will have a relative (i.e. local) rotor basebone constraint about the X axis.
				FabrikChain3D secondChain = new FabrikChain3D("Second Chain");
				basebone = new FabrikBone3D( new Vec3f(), new Vec3f(15.0f, 0.0f, 0.0f) );
				secondChain.addBone(basebone);
				secondChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.LOCAL_ROTOR, X_AXIS, 45.0f);				
				
				// Add some additional bones
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.setColour(Utils.RED);
				
				// Connect this second chain to the start point of bone 3 in chain 0 of the structure
				mStructure.connectChain(secondChain, 0, 3, BoneConnectionPoint3D.START);
				break;
			}
			
			case 11: {
				demoName            = "Demo 11 - Connected Chains with Freely-Rotating Global Hinged Basebone Constraints";
				mStructure          = new FabrikStructure3D(demoName);
				Colour4f boneColour = new Colour4f(Utils.GREEN);
				
				// Create a new chain...				
				FabrikChain3D chain = new FabrikChain3D();
					
				// ...then create a basebone, set its draw colour and add it to the chain.
				Vec3f startLoc        = new Vec3f(0.0f, 0.0f, 40.0f);
				Vec3f endLoc          = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
				FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
				basebone.setColour(boneColour);
				chain.addBone(basebone);
				
				// Add additional consecutive, unconstrained bones to the chain				
				for (int boneLoop = 0; boneLoop < 7; boneLoop++)
				{
					boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
					chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
				}
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);		
				
				// Create a second chain which will have a relative (i.e. local) rotor basebone constraint about the X axis.
				FabrikChain3D secondChain = new FabrikChain3D("Second Chain");
				FabrikBone3D base = new FabrikBone3D( new Vec3f(), new Vec3f(15.0f, 0.0f, 0.0f) );
				secondChain.addBone(base);
				
				// Set this second chain to have a freely rotating global hinge which rotates about the Y axis
				// Note: We MUST add the basebone to the chain before we can set the basebone constraint on it.
				secondChain.setFreelyRotatingGlobalHingedBasebone(Y_AXIS);				
				
				// Add some additional bones
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.setColour(Utils.GREY);
				
				// Connect this second chain to the start point of bone 3 in chain 0 of the structure
				mStructure.connectChain(secondChain, 0, 3, BoneConnectionPoint3D.START);
				break;
			}
			
			case 12: {
				demoName            = "Demo 12 - Connected Chains with Non-Freely-Rotating Global Hinge Basebone Constraints";
				mStructure          = new FabrikStructure3D(demoName);
				Colour4f boneColour = new Colour4f(Utils.GREEN);
				
				// Create a new chain...				
				FabrikChain3D chain = new FabrikChain3D();
					
				// ...then create a basebone, set its draw colour and add it to the chain.
				Vec3f startLoc        = new Vec3f(0.0f, 0.0f, 40.0f);
				Vec3f endLoc          = startLoc.plus( defaultBoneDirection.times(defaultBoneLength) );
				FabrikBone3D basebone = new FabrikBone3D(startLoc, endLoc);
				basebone.setColour(boneColour);
				chain.addBone(basebone);
				
				// Add additional consecutive, unconstrained bones to the chain				
				for (int boneLoop = 0; boneLoop < 7; boneLoop++)
				{
					boneColour = (boneLoop % 2 == 0) ? boneColour.lighten(0.4f) : boneColour.darken(0.4f);
					chain.addConsecutiveBone(defaultBoneDirection, defaultBoneLength, boneColour);
				}
				
				// Finally, add the chain to the structure
				mStructure.addChain(chain);		
				
				// Create a second chain which will have a relative (i.e. local) rotor basebone constraint about the X axis.
				FabrikChain3D secondChain = new FabrikChain3D("Second Chain");
				FabrikBone3D base = new FabrikBone3D( new Vec3f(), new Vec3f(15.0f, 0.0f, 0.0f) );
				secondChain.addBone(base);
				
				// Set this second chain to have a freely rotating global hinge which rotates about the Y axis
				// Note: We MUST add the basebone to the chain before we can set the basebone constraint on it.				
				secondChain.setHingeBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_HINGE, Y_AXIS, 90.0f, 45.0f, X_AXIS);
				
				/** Other potential options for basebone constraint types **/
				//secondChain.setFreelyRotatingGlobalHingedBasebone(Y_AXIS);
				//secondChain.setFreelyRotatingLocalHingedBasebone(Y_AXIS);
				//secondChain.setHingeBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_HINGE, Y_AXIS, 90.0f, 45.0f, X_AXIS);
				//secondChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.GLOBAL_ROTOR, Z_AXIS, 30.0f, 60.0f, Y_AXIS);
				//secondChain.setRotorBaseboneConstraint(BaseboneConstraintType3D.LOCAL_ROTOR, Z_AXIS, 30.0f, 60.0f, Y_AXIS);
				
				// Add some additional bones
				secondChain.addConsecutiveBone(X_AXIS, 15.0f);
				secondChain.addConsecutiveBone(X_AXIS, 10.0f);
				secondChain.addConsecutiveBone(X_AXIS, 10.0f);
				secondChain.setColour(Utils.GREY);
				
				// Connect this second chain to the start point of bone 3 in chain 0 of the structure
				mStructure.connectChain(secondChain, 0, 3, BoneConnectionPoint3D.START);
				break;
			}
			
			default:
				throw new IllegalArgumentException("No such demo number: " + demoNumber);
			
		}
		
		// Set the appropriate window title and make an initial solve pass of the structure
		Application.window.setWindowTitle(demoName);
		//structure.updateTarget( target.getCurrentLocation() );
	}
	
	/** Set all chains in the structure to be in fixed-base mode whereby the base locations cannot move. */
	public void setFixedBaseMode(boolean value) { mStructure.setFixedBaseMode(value); }
		
	/** Handle the movement of the camera using the W/S/A/D keys. */
	public void handleCameraMovement(int key, int action) { camera.handleKeypress(key, action); }
	
	public void draw()
	{
		// Move the camera based on keypresses and mouse movement
		camera.move(1.0f / 60.0f);
			
		// Get the ModelViewProjection matrix as we use it multiple times
		Mat4f mvpMatrix = Application.window.getMvpMatrix();

		// Draw our grids
        lowerGrid.draw(mvpMatrix);
        upperGrid.draw(mvpMatrix);
        
        // If we're not paused then step the target and solve the structure for the new target location
        if (!Application.paused)
        {
        	target.step();
        	mStructure.updateTarget( target.getCurrentLocation() );
        }
        
        // If we're in rotate base mode then rotate the base location(s) of all chains in the structure
        if (Application.rotateBasesMode)
        {
        	int numChains = mStructure.getNumChains();
        	for (int loop = 0; loop < numChains; ++loop)
        	{
        		Vec3f base = mStructure.getChain(loop).getBaseLocation();
            	base       = Vec3f.rotateAboutAxisDegs(base, baseRotationAmountDegs, Y_AXIS);            
            	mStructure.getChain(loop).setBaseLocation(base);
        	}
        }
        
        // Draw the target
        target.draw(Utils.YELLOW, 8.0f, mvpMatrix);
        
        // Draw the structure as required
        // Note: bone lines are drawn in the bone colour, models are drawn in white by default but you can specify a colour to the draw method,
        //       axes are drawn X/Y/Z as Red/Green/Blue and constraints are drawn the colours specified in the FabrikConstraint3D class.
        if (Application.drawLines)       { FabrikLine3D.draw(mStructure, boneLineWidth, mvpMatrix);                                       }            
        if (Application.drawModels)      { model.drawStructure(mStructure, camera.getViewMatrix(), Application.window.mProjectionMatrix); }         
		if (Application.drawAxes)        { axis.draw(mStructure, camera.getViewMatrix(), Application.window.mProjectionMatrix);           }
		if (Application.drawConstraints) { constraint.draw(mStructure, constraintLineWidth, mvpMatrix);                                   }
	}
}
