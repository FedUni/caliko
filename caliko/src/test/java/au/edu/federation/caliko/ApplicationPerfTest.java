package au.edu.federation.caliko;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.edu.federation.utils.SerializationUtil;
import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;
import au.edu.federation.utils.Mat3f;

public class ApplicationPerfTest
{	
	// We'll run this many cycles
	static int totalCycles = 10;
			
	// Each run will solve the chain for a pseudo-random location this many times
	static int iterationsPerCycle = 50;
			
	// Number of bones to add per cycle
	static int bonesToAdd = 100;
	
	// Dump results to textfile for import into excel
	static PrintWriter writer;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void runTests() throws IOException, Exception
	{	
		System.out.println("---------- Caliko Performance Analysis ----------");
		
		writer = new PrintWriter(folder.newFile("caliko-performance-test.txt"), "UTF-8");
		
		System.out.println("\n--- Rotation matrix creation ---\n");
		writer.println("\n--- Rotation matrix creation ---\n");
		
		Mat3f m;
	
		Vec3f plusX  = new Vec3f(1.0f, 0.0f, 0.0f);
		Vec3f plusY  = new Vec3f(0.0f, 1.0f, 0.0f);
		Vec3f plusZ  = new Vec3f(0.0f, 0.0f, 1.0f);
		
		Vec3f minusX = new Vec3f(-1.0f,  0.0f,  0.0f);
		Vec3f minusY = new Vec3f( 0.0f, -1.0f,  0.0f);
		Vec3f minusZ = new Vec3f( 0.0f,  0.0f, -1.0f);
		
		m = Mat3f.createRotationMatrix(plusX);
		System.out.println("Rotation matrix generated from plusX:\n" + m.toString() );		
		writer.println("Rotation matrix generated from plusX:\n" + m.toString() );		
		m = Mat3f.createRotationMatrix(plusY);
		System.out.println("Rotation matrix generated from plusY:\n" + m.toString() );		
		writer.println("Rotation matrix generated from plusY:\n" + m.toString() );		
		m = Mat3f.createRotationMatrix(plusZ);
		System.out.println("Rotation matrix generated from plusZ:\n" + m.toString() );
		writer.println("Rotation matrix generated from plusZ:\n" + m.toString() );
		
		m = Mat3f.createRotationMatrix(minusX);
		System.out.println("Rotation matrix generated from minusX:\n" + m.toString() );
		writer.println("Rotation matrix generated from minusX:\n" + m.toString() );				
		m = Mat3f.createRotationMatrix(minusY);
		System.out.println("Rotation matrix generated from minusY:\n" + m.toString() );
		writer.println("Rotation matrix generated from minusY:\n" + m.toString() );				
		m = Mat3f.createRotationMatrix(minusZ);
		System.out.println("Rotation matrix generated from minusZ:\n" + m.toString() );
		writer.println("Rotation matrix generated from minusZ:\n" + m.toString() );
		
		System.out.println("\n -----------------\n");
		writer.println("\n -----------------\n");
		
		// Perform tests
		int numTests = 3;
		for (int loop = 1; loop <= numTests; ++loop)
		{
			performTest(loop);
		}
		
		// Save results file
		writer.close();		
	}
	
	private void performTest(int testNumber) throws IOException, Exception
	{
		// Set a fixed random seed for repeatability across cycles
		Utils.setSeed(123);
		
		String testDescription = "";
		switch (testNumber)
		{
		case 1:
			testDescription = "Test 1: Unconstrained 3D chain.";			
			break;
		case 2:
			testDescription = "Test 2: Rotor constrained 3D chain - 45 degrees.";
			break;
		case 3:
			testDescription = "Test 3: Rotor constrained 3D chain - 90 degrees.";
			break;
		}
		
		writer.println("----- " + testDescription + " -----");
		System.out.println("----- " + testDescription + " -----");
		
		Vec3f RIGHT      = new Vec3f(1.0f, 0.0f, 0.0f);
		float boneLength = 10.0f;		
		int bonesToAdd   = 100;
		
		// Initial chain setup (requires a basebone)
		FabrikChain3D chain = new FabrikChain3D();
		FabrikBone3D basebone = new FabrikBone3D(new Vec3f(), new Vec3f(boneLength, 0.0f, 0.0f) );
		chain.addBone(basebone);
		for (int loop = 1; loop < bonesToAdd; ++loop)
		{
			switch (testNumber)
			{
				case 1:
					chain.addConsecutiveBone(RIGHT, boneLength);
					break;
				case 2:
					chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 45.0f);
					break;
				case 3:
					chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 90.0f);
					break;
			}
		}
		System.out.println("Cycle 1 - " + chain.getNumBones() + " bones.");
		double averageMS = solveChain(chain, iterationsPerCycle);
		
		writer.println(chain.getNumBones() + "\t" + averageMS);
		
		// ----- Adding additional bones -----				
		
		// Cycles 1 onwards...
		for (int loop = 1; loop < totalCycles; ++loop)
		{
			// Add 'bonesToAdd' bones
			for (int boneLoop = 0; boneLoop < bonesToAdd; ++boneLoop)
			{
				switch (testNumber)
				{
					case 1:
						chain.addConsecutiveBone(RIGHT, boneLength);
						break;
					case 2:
						chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 45.0f);
						break;
					case 3:
						chain.addConsecutiveRotorConstrainedBone(RIGHT, boneLength, 90.0f);
						break;
				}
			}
		
			// Run test
			System.out.println("Cycle " + (loop + 1) + " - " + chain.getNumBones() + " bones.");
			averageMS = solveChain(chain, iterationsPerCycle);
			
			System.out.println("Average solve duration (Milliseconds): " + averageMS);			
			writer.println(chain.getNumBones() + "\t" + averageMS);
			
		} // End of perftest loop
		
		/*String s = "C:\\users\\r3dux\\Desktop\\serialization-presaved-chain" + Integer.toString(testNumber) + ".bin";
		File file = new File(s);
		FileOutputStream fos = new FileOutputStream(file);
		SerializationUtil.serializeChain(chain, fos);		
		System.out.println("*** Written chain. ***");
		*/
		
		assertChain(chain, testNumber);
		
		System.out.println("*** Serialization / Unserialization success ***");
		System.out.println("*** Test completed. ***");		
	}
	
	private void assertChain(FabrikChain3D chain, int testNumber) throws IOException, Exception
	{
		// Serialize the provided chain
		SerializationUtil.serializeChain( chain, new FileOutputStream( folder.newFile("perftest-serialized-" + testNumber + ".bin") ) );
					
		// Unserialize presaved chain
		InputStream is = ApplicationPerfTest.class.getResourceAsStream("/serialization-presaved-chain-" + testNumber + ".bin");		
		FabrikChain3D unserializedPresavedChain = SerializationUtil.unserializeChain(is, FabrikChain3D.class);
		
		// Assert theat the original and
		Assert.assertEquals(chain, unserializedPresavedChain);
	}
	
	private double solveChain(FabrikChain3D chain, int numIterations)
	{
		float avgSolveDistance = 0.0f;
	
		// Get half the length of the chain (to ensure target can be reached)
		float len = chain.getChainLength() / 2.0f;
		
		// Instantiate a Vec3f to use as the solve target
		Vec3f target = new Vec3f();
		
		long startNanos, endNanos, combinedMicrosecondsForIteration = 0L;
		double averageMilliseconds = 0.0;
		for (int loop = 0; loop < numIterations; ++loop)
		{
			// Generate a new random target location
			target.set(Utils.randRange(-len, len), Utils.randRange(-len, len), Utils.randRange(-len, len) );
			
			// Get start time
			startNanos = System.nanoTime();
			
			// Solve for target
			avgSolveDistance += chain.solveForTarget(target);
				
			writer.println(chain.toString());
			writer.flush();
			
			// Get end time
			endNanos = System.nanoTime();
			
			// Increment total time for this cycle
			combinedMicrosecondsForIteration += (endNanos - startNanos) / 1000;			
		}
		
		// Calculate and display average solve duration for this chain across all iterations
		long averageMicrosecondsPerIteration = combinedMicrosecondsForIteration / (long)numIterations;
		averageMilliseconds = (double)averageMicrosecondsPerIteration / 1000.0;
		
		// Calculate average solve distance & display it
		avgSolveDistance /= numIterations;
		System.out.println("Average solve distance: " + avgSolveDistance);
		
		return averageMilliseconds;
	}

}
