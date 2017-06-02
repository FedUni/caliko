package au.edu.federation.caliko;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.edu.federation.utils.Utils;
import au.edu.federation.utils.Vec3f;

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
	public void runTests() throws IOException
	{	
		System.out.println("---------- Caliko CPU Performance Analysis ----------");
		
		writer = new PrintWriter(folder.newFile("caliko-performance-test.txt"), "UTF-8");
		
		// Perform tests
		int numTests = 3;
		for (int loop = 1; loop <= numTests; ++loop)
		{
			performTest(loop);
		}
		
		// Save results file
		writer.close();		
	}
	
	public static void performTest(int testNumber)
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
		}
		System.out.println("*** Test completed. ***");		
	}
	
	static double solveChain(FabrikChain3D chain, int numIterations)
	{
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
				chain.solveForTarget(target);
				
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
		
		return averageMilliseconds;
	}

}
