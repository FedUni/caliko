package au.edu.federation.caliko.demo;

import au.edu.federation.utils.Mat4f;

/**
 * @author jsalvo
 */
public interface CalikoDemoStructure {
	
	void setup();
	
	void drawTarget(Mat4f mvpMatrix);
	
}
