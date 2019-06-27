package au.edu.federation.caliko.demo;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

import au.edu.federation.caliko.demo2d.CalikoDemoStructure2DFactory.CalikoDemoStructure2DEnum;
import au.edu.federation.caliko.demo3d.CalikoDemoStructure3DFactory.CalikoDemoStructure3DEnum;
import au.edu.federation.utils.Mat4f;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec2f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Class to set up an OpenGL window.
 * 
 * @author Al Lansley
 * @version 0.3 - 07/12/2015
 */
public class OpenGLWindow
{
	// Mouse cursor locations in screen-space and world-space
	public static Vec2f screenSpaceMousePos = new Vec2f(Application.windowWidth / 2.0f, Application.windowHeight / 2.0f);
	public static Vec2f worldSpaceMousePos  = new Vec2f();
	
	// Window properties
    long  mWindowId;	
	int   mWindowWidth;
	int   mWindowHeight;	
	float mAspectRatio;
	
	// Matrices
	Mat4f mProjectionMatrix;
	Mat4f mModelMatrix = new Mat4f(1.0f);
	Mat4f mMvpMatrix   = new Mat4f();
	
	// Matrix properties
	boolean mOrthographicProjection; // Use orthographic projection? If false, we use a standard perspective projection
	float mVertFoVDegs;
	float mZNear;
	float mZFar;
	float mOrthoExtent;
	
	// We need to strongly reference callback instances so that they don't get garbage collected.
    private GLFWErrorCallback       errorCallback;
    private GLFWKeyCallback         keyCallback;
    private GLFWWindowSizeCallback  windowSizeCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWCursorPosCallback   cursorPosCallback;
    
    // Constructor
    public OpenGLWindow(int windowWidth, int windowHeight, float vertFoVDegs, float zNear, float zFar, float orthoExtent)
    {
    	// Set properties and create the projection matrix
    	mWindowWidth  = windowWidth <= 0 ? 1 : windowWidth;
    	mWindowHeight = windowHeight <= 0 ? 1 : windowHeight;
    	mAspectRatio  = (float)mWindowWidth / (float)mWindowHeight; 
    	
    	mVertFoVDegs = vertFoVDegs;
    	mZNear       = zNear;
    	mZFar        = zFar;
    	mOrthoExtent = orthoExtent; 
    	
    	if (Application.use3dDemo)
    	{
    		mOrthographicProjection = false;
    		mProjectionMatrix = Mat4f.createPerspectiveProjectionMatrix(mVertFoVDegs, mAspectRatio, mZNear, mZFar);
    	}
    	else
    	{
    		mOrthographicProjection = true;
    		mProjectionMatrix = Mat4f.createOrthographicProjectionMatrix(-mOrthoExtent, mOrthoExtent, mOrthoExtent, -mOrthoExtent, mZNear, mZFar);
    	}
    	
    	// Setup the error callback to output to System.err
    	glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
 
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() ) { throw new IllegalStateException("Unable to initialize GLFW"); }
 
        // ----- Specify window hints -----
        // Note: Window hints must be specified after glfwInit() (which resets them) and before glfwCreateWindow where the context is created.
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);                 // Request OpenGL version 3.3 (the minimum we can get away with)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // We want a core profile without any deprecated functionality...
        //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);         // ...however we do NOT want a forward compatible profile as they've removed line widths!
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);                       // We want the window to be resizable
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);                         // We want the window to be visible (false makes it hidden after creation)
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);                         // We want the window to take focus on creation
        glfwWindowHint(GLFW_SAMPLES, 4);                               // Ask for 4x anti-aliasing (this doesn't mean we'll get it, though) 
                
        // Create the window
        mWindowId = glfwCreateWindow(mWindowWidth, mWindowHeight, "LWJGL3 Test", NULL, NULL);        
        if (mWindowId == NULL) { throw new RuntimeException("Failed to create the GLFW window"); }
        
        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode( glfwGetPrimaryMonitor() );
        int windowHorizOffset = (vidmode.width()  - mWindowWidth)  / 2;
        int windowVertOffset  = (vidmode.height() - mWindowHeight) / 2;
                
        glfwSetWindowPos(mWindowId, windowHorizOffset, windowVertOffset); // Center our window
        glfwMakeContextCurrent(mWindowId);                                // Make the OpenGL context current
        glfwSwapInterval(1);                                              // Swap buffers every frame (i.e. enable vSync)
        
        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread, creates the ContextCapabilities instance and makes
        // the OpenGL bindings available for use.
        glfwMakeContextCurrent(mWindowId);
        
        // Enumerate the capabilities of the current OpenGL context, loading forward compatible capabilities
        GL.createCapabilities(true);
        
        // Setup our keyboard, mouse and window resize callback functions
        setupCallbacks();
        
        // ---------- OpenGL settings -----------

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        // Specify the size of the viewport. Params: xOrigin, yOrigin, width, height
     	glViewport(0, 0, mWindowWidth, mWindowHeight);

     	// Enable depth testing
     	glDepthFunc(GL_LEQUAL);
     	glEnable(GL_DEPTH_TEST);

     	// When we clear the depth buffer, we'll clear the entire buffer
     	glClearDepth(1.0f);

     	// Enable blending to use alpha channels
     	// Note: blending must be enabled to use transparency / alpha values in our fragment shaders.
     	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
     	glEnable(GL_BLEND);
     	
     	glfwShowWindow(mWindowId); // Make the window visible
    }
    
    // Constructor with some sensible projection matrix values hard-coded
    public OpenGLWindow(int width, int height) { this(width, height, 35.0f, 1.0f, 5000.0f, 120.0f); }
	
    /** Return a calculated ModelViewProjection matrix.
     * <p>
     * This MVP matrix is the result of multiplying the projection matrix by the view matrix obtained from the camera, and
     * as such is really a ProjectionView matrix or 'identity MVP', however you'd like to term it.
     * 
     * If you want a MVP matrix specific to your model, simply multiply this matrix by your desired model matrix to create
     * a MVP matrix specific to your model.
     *  
     * @return	A calculate ModelViewProjection matrix.
     */
	public Mat4f getMvpMatrix() { return mProjectionMatrix.times( CalikoDemo3D.camera.getViewMatrix() ); }
	
	/**
	 * Return the projection matrix.
	 *
	 * @return	The projection matrix.
	 */
	public Mat4f getProjectionMatrix() { return mProjectionMatrix; }
	
	/** Swap the front and back buffers to update the display. */
	public void swapBuffers() { glfwSwapBuffers(mWindowId); }
	
	/**
	 * Set the window title to the specified String argument.
	 * 
	 * @param	title	The String that will be used as the title of the window.
	 */
	public void setWindowTitle(String title)   { glfwSetWindowTitle(mWindowId, title); }
	
	/** Destroy the window, finish up glfw and release all callback methods. */
	public void cleanup()
	{
		// Free the window callbacks and destroy the window
		//glfwFreeCallbacks(mWindowId);
		cursorPosCallback.close();
		mouseButtonCallback.close();
		windowSizeCallback.close();
        keyCallback.close();   
		
		glfwDestroyWindow(mWindowId);
		
		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	// Setup keyboard, mouse cursor, mouse button and window resize callback methods.
	private void setupCallbacks()
	{	
        // Key callback
		glfwSetKeyCallback(mWindowId, keyCallback = GLFWKeyCallback.create( (long window, int key, int scancode, int action, int mods) ->        
        {        	
           	if (action == GLFW_PRESS)
        	{
        		switch (key)
        		{            	
        			// Setup demos
	            	case GLFW_KEY_RIGHT:						
	            		if (Application.use3dDemo)
	            		{
	            			if (Application.demoNumber < CalikoDemoStructure3DEnum.values().length) { Application.demoNumber++; }
	            		}
	            		else // 2D Demo mode
	            		{
	            			if (Application.demoNumber < CalikoDemoStructure2DEnum.values().length) { Application.demoNumber++; }
	            		}
						Application.demo.setup(Application.demoNumber);	            		
						break;	
	            	case GLFW_KEY_LEFT:						
	            		if (Application.demoNumber > 1) { Application.demoNumber--; }
						Application.demo.setup(Application.demoNumber);	            		
						break;				
						
					// Toggle fixed base mode
					case GLFW_KEY_F:
						Application.fixedBaseMode = !Application.fixedBaseMode;
						Application.demo.setFixedBaseMode(Application.fixedBaseMode);
						break;
					// Toggle rotating bases
					case GLFW_KEY_R:
						Application.rotateBasesMode = !Application.rotateBasesMode;
						break;
						
					// Various drawing options
					case GLFW_KEY_C:
						Application.drawConstraints = !Application.drawConstraints;
						break;					
					case GLFW_KEY_L:
						Application.drawLines = !Application.drawLines;
						break;
					case GLFW_KEY_M:
						Application.drawModels = !Application.drawModels;
						break;
					case GLFW_KEY_P:
						mOrthographicProjection = !mOrthographicProjection;
					 	if (mOrthographicProjection)
					 	{
					 		mProjectionMatrix = Mat4f.createOrthographicProjectionMatrix(-mOrthoExtent, mOrthoExtent, mOrthoExtent, -mOrthoExtent, mZNear, mZFar);
					 	}
					 	else
					 	{
					 		mProjectionMatrix = Mat4f.createPerspectiveProjectionMatrix(mVertFoVDegs, mAspectRatio, mZNear, mZFar);
					 	}
						break;
					case GLFW_KEY_X:
						Application.drawAxes = !Application.drawAxes;
						break;
						
					// Camera controls
					case GLFW_KEY_W:
            		case GLFW_KEY_S:
            		case GLFW_KEY_A:
            		case GLFW_KEY_D:
            			if (Application.use3dDemo) { Application.demo.handleCameraMovement(key, action); }
            			break;
            			
            		// Close the window
            		case GLFW_KEY_ESCAPE:
            			glfwSetWindowShouldClose(window, true);
            			break;
            			
            		// Cycle through / switch between 2D and 3D demos with the up and down cursors
            		case GLFW_KEY_UP:
            		case GLFW_KEY_DOWN:
            			Application.use3dDemo = !Application.use3dDemo;
            			Application.demoNumber = 1;
            			
            			// Viewing 2D demos?
            			if (!Application.use3dDemo)
            			{
            				mOrthographicProjection = true;
            				mProjectionMatrix = Mat4f.createOrthographicProjectionMatrix(-mOrthoExtent, mOrthoExtent, mOrthoExtent, -mOrthoExtent, mZNear, mZFar);            				
            				Application.demo = new CalikoDemo2D(Application.demoNumber);
            			}
            			else // Viewing 3D demos
            			{            			
            				mOrthographicProjection = false;
            				mProjectionMatrix = Mat4f.createPerspectiveProjectionMatrix(mVertFoVDegs, mAspectRatio, mZNear, mZFar);            				
            				Application.demo = new CalikoDemo3D(Application.demoNumber);
            			}
            			break;
            			
            		// Dynamic add/remove bones for first demo
//            		case GLFW_KEY_COMMA:
//            			if (Application.demoNumber == 1 && Application.structure.getChain(0).getNumBones() > 1)
//            			{
//            				Application.structure.getChain(0).removeBone(0);
//            			}
//            			break;
//            		case GLFW_KEY_PERIOD:
//            			if (Application.demoNumber == 1)
//            			{
//            				Application.structure.getChain(0).addConsecutiveBone(Application.X_AXIS, Application.defaultBoneLength);
//            			}
//            			break;
            			
            		case GLFW_KEY_SPACE:
            			Application.paused = !Application.paused;
            			break;
            			
        		} // End of switch
        		
        	}         	
           	else if (action == GLFW_REPEAT || action == GLFW_RELEASE) // Camera must also handle repeat or release actions
        	{
        		switch (key)
        		{
	            	case GLFW_KEY_W:
	        		case GLFW_KEY_S:
	        		case GLFW_KEY_A:
	        		case GLFW_KEY_D:
	        			if (Application.use3dDemo) { Application.demo.handleCameraMovement(key, action); }
	        			break;
        		}
        	}
        }));
        
        // Mouse cursor position callback
        glfwSetCursorPosCallback(mWindowId, cursorPosCallback = GLFWCursorPosCallback.create( (long windowId, double mouseX,  double mouseY) ->
        {   
        	// Update the screen space mouse location
        	screenSpaceMousePos.set( (float)mouseX, (float)mouseY );
        	
        	// If we're holding down the LMB, then...
        	if (Application.leftMouseButtonDown)
        	{
        		// ...in the 3D demo we update the camera look direction...
        		if (Application.use3dDemo)
        		{
    				CalikoDemo3D.camera.handleMouseMove(mouseX, mouseY);
    			}
        		else // ...while in the 2D demo we update the 2D target.
        		{	
        			// Convert the mouse position in screen-space coordinates to our orthographic world-space coordinates
					worldSpaceMousePos.set(  Utils.convertRange(screenSpaceMousePos.x, 0.0f,  mWindowWidth, -mOrthoExtent, mOrthoExtent),
                                             -Utils.convertRange(screenSpaceMousePos.y, 0.0f, mWindowHeight, -mOrthoExtent, mOrthoExtent) );
					
					CalikoDemo2D.mStructure.solveForTarget(worldSpaceMousePos);
        		}
        	}    		
        }));
        
        // Mouse button callback
        glfwSetMouseButtonCallback(mWindowId, mouseButtonCallback = GLFWMouseButtonCallback.create( (long windowId, int button, int action, int mods) ->
        {
			// If the left mouse button was the button that invoked the callback...
			if (button == GLFW_MOUSE_BUTTON_1)
			{	
				// ...then set the LMB status flag accordingly
				// Note: We cannot simply toggle the flag here as double-clicking the title bar to fullscreen the window confuses it and we
				// then end up mouselook-ing without the LMB being held down!
				if (action == GLFW_PRESS) { Application.leftMouseButtonDown = true; } else { Application.leftMouseButtonDown = false; }
				
				if (Application.use3dDemo)
				{	
					// Immediately set the cursor position to the centre of the screen so our view doesn't "jump" on first cursor position change
					glfwSetCursorPos(windowId, ((double)mWindowWidth / 2), ((double)mWindowHeight / 2) );
					
					switch (action)
					{
						case GLFW_PRESS:
							// Make the mouse cursor hidden and put it into a 'virtual' mode where its values are not limited
					        glfwSetInputMode(mWindowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
					        break;

						case GLFW_RELEASE:
							// Restore the mouse cursor to normal and reset the camera last cursor position to be the middle of the window
					        glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
					        CalikoDemo3D.camera.resetLastCursorPosition();
					        break;
					}						
				}
				else
				{	
        			// Convert the mouse position in screen-space coordinates to our orthographic world-space coordinates
					worldSpaceMousePos.set(  Utils.convertRange(screenSpaceMousePos.x, 0.0f,  mWindowWidth, -mOrthoExtent, mOrthoExtent),
                                             -Utils.convertRange(screenSpaceMousePos.y, 0.0f, mWindowHeight, -mOrthoExtent, mOrthoExtent) );
					
					CalikoDemo2D.mStructure.solveForTarget(worldSpaceMousePos);
				}
				
				// Nothing needs be done in 2D demo mode - the Application.leftMouseButtonDown flag plus the mouse cursor handler take care of it.				
			}
		}));
        
        // Window size callback
        glfwSetWindowSizeCallback(mWindowId, windowSizeCallback = GLFWWindowSizeCallback.create( (long windowId, int windowWidth,  int windowHeight) ->
        {   
    		// Update our window width and height and recalculate the aspect ratio
    		if (windowWidth  <= 0) { windowWidth  = 1; }
    		if (windowHeight <= 0) { windowHeight = 1; }        		
    		mWindowWidth  = windowWidth;
    		mWindowHeight = windowHeight;
    		mAspectRatio  = (float)mWindowWidth / (float)mWindowHeight;
    		
    		// Let our camera know about the new size so it can correctly recentre the mouse cursor
    		CalikoDemo3D.camera.updateWindowSize(windowWidth, windowHeight);
    		
    		// Update our viewport
    		glViewport(0, 0, mWindowWidth, mWindowHeight);

    		// Recalculate our projection matrix
    		if (mOrthographicProjection)
		 	{
		 		mProjectionMatrix = Mat4f.createOrthographicProjectionMatrix(-mOrthoExtent, mOrthoExtent, mOrthoExtent, -mOrthoExtent, mZNear, mZFar);
		 	}
		 	else
		 	{
		 		mProjectionMatrix = Mat4f.createPerspectiveProjectionMatrix(mVertFoVDegs, mAspectRatio, mZNear, mZFar);
		 	}
        }));
        
	} // End of setupCallbacks method
	
} // End of OpenGLWindow class