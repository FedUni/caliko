package au.edu.federation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.edu.federation.caliko.FabrikBone3D;
import au.edu.federation.caliko.FabrikChain3D;

/**
 * @author jsalvo / alansley
 */
public class MarshallingUtilTest {
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();	

	@Test
	public void testMarshalling() throws Exception {
		Vec3f RIGHT      = new Vec3f(1.0f, 0.0f, 0.0f);
		float boneLength = 10.0f;		
		int bonesToAdd   = 100;
		
		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D basebone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0.0f, 0.0f) );
		chain.addBone(basebone);
		for (int loop = 1; loop < bonesToAdd; ++loop)
		{
			chain.addConsecutiveBone(RIGHT, boneLength);
		}
		solveChain(chain);
		
		File file = folder.newFile();
		FileOutputStream fos = new FileOutputStream(file);
		MarshallingUtil.marshall(chain, fos);
		
		FileInputStream fis = new FileInputStream(file);
		FabrikChain3D unmarhsalledChain = MarshallingUtil.unmarshallChain(fis, FabrikChain3D.class);
		
		Assert.assertEquals(chain, unmarhsalledChain);
	}

	/**
	 * Unit-test that we can successfully unmarshall a chain from an XML source. 
	 */
	@Test
	public void testUnmarshalling() throws Exception {
		InputStream is = MarshallingUtilTest.class.getResourceAsStream("/sampleMarshalledChain.xml");
		FabrikChain3D unmarhsalledChain = MarshallingUtil.unmarshallChain(is, FabrikChain3D.class);
		
		Assert.assertNotNull(unmarhsalledChain);
	}

	/** Test for pitch / yaw functionality 
	 */
	@Test
	public void testPitchAndYaw() throws Exception
	{
		Vec3f X_AXIS = new Vec3f(1.0f, 0.0f, 0.0f);
		Vec3f Y_AXIS = new Vec3f(0.0f, 1.0f, 0.0f);

		Vec3f boneDirection = new Vec3f(0.0f, 0.0f, -1.0f); // Bone direction (obtain via 'getDirectionUV()') points directly INTO screen
		
		// Test pitch around global X-axis in 30 degree increments		
		System.out.println("---------- Pitch ----------");
		float pitch = 0.0f;
		for (int loop = 0; loop < 12; loop++)
		{	
			boneDirection = Vec3f.rotateXDegs(boneDirection, 30.0f).normalise();
			pitch = boneDirection.getGlobalPitchDegs();			
			System.out.println("After " + (loop + 1) * 30 + " degrees rotation bone pitch is: " + pitch + " degrees. Direction: " + boneDirection);
		}
		
		// Test yaw around global Y-axis in 30 degree increments
		System.out.println("\n---------- Yaw ----------");
		float yaw = 0.0f;		
		for (int loop = 0; loop < 12; loop++)
		{	
			// Rotate bone by 30 degrees around Y-axis
			boneDirection = Vec3f.rotateYDegs(boneDirection, 30.0f).normalise();
			yaw = boneDirection.getGlobalYawDegs();
			System.out.println("After " + (loop + 1) * 30 + " degrees rotation bone yaw is: " + yaw + " degrees. Direction: " + boneDirection);
		}
	}

	void solveChain(FabrikChain3D chain) throws Exception {
		Utils.setSeed((int)System.currentTimeMillis());
		// Get half the length of the chain (to ensure target can be reached)
		float len = chain.getChainLength() / 2.0f;
		
		// Generate a new random target location
		Vec3f target = new Vec3f(Utils.randRange(-len, len), Utils.randRange(-len, len), Utils.randRange(-len, len));
		
		// Solve for target
		chain.solveForTarget(target);
	}
	
	
}
