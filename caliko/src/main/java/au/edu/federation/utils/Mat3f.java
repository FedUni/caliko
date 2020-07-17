package au.edu.federation.utils;

import java.text.DecimalFormat;

/** 
 * A custom 3x3 matrix.
 * <p>
 * Elements of this Mat3f class are:
 * m00  m10  m20
 * m01	m11  m21
 * m02	m12  m22
 * <p>
 * Matrices can be considered as are column-major - although when treating them as arrays this makes no difference.
 * <p>
 * mXX properties are publicly accessible for performance reasons.
 * 
 * @author Al Lansley
 * @version 0.4 - 28/10/2018
 */

public class Mat3f
{
	private static final DecimalFormat df   = new DecimalFormat("0.000");
	private static final String NEW_LINE    = System.lineSeparator();	
	private static final float DEGS_TO_RADS = (float)Math.PI / 180.0f;

	public float m00, m01, m02; // First  column - typically the direction of the positive X-axis
	public float m10, m11, m12; // Second column - typically the direction of the positive Y-axis
	public float m20, m21, m22; // Third  column - typically the direction of the positive Z-axis

	/** Default constructor - all matrix elements are set to 0.0f. */
	public Mat3f() { /* Java implicitly sets float primitives to 0.0f on creation */ }

	/** 
	 * Constructor which sets the given value across the diagonal and zeroes the rest of the matrix.
	 * <p>
	 * So for example to create an identity matrix you could just call: Mat3f m = new Mat3f(1.0f).
	 * 
	 * @param	value	The value to set across the diagonal of the constructed matrix (i.e. elements m00, m11, and m22).
	 */
	public Mat3f(float value)
	{
		// Set diagonal
		m00 = m11 = m22 = value;

		// All other mXX properties are implicitly set to zero by Java as that is the default value of a float primitive.
	}
	
	/**
	 * Constructor which sets the matrix from nine floats in the order x-axis, y-axis then z-axis.
	 *
	 * @param	m00	The first  element of the positive X-axis.
	 * @param	m01	The second element of the positive X-axis.
	 * @param	m02	The third  element of the positive X-axis.
	 * @param	m10	The first  element of the positive Y-axis.
	 * @param	m11	The second element of the positive Y-axis.
	 * @param	m12	The third  element of the positive Y-axis.
	 * @param	m20	The first  element of the positive Z-axis.
	 * @param	m21	The second element of the positive Z-axis.
	 * @param	m22	The third  element of the positive Z-axis.
	 */
	public Mat3f(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22)
	{
		this.m00 = m00;
		this.m01 = m01; 
		this.m02 = m02;
		
		this.m10 = m10;
		this.m11 = m11; 
		this.m12 = m12;
		
		this.m20 = m20;
		this.m21 = m21; 
		this.m22 = m22;
	}
	
	/**
	 * Constructor which sets the matrix from the three provided axes.
	 * 
	 * @param xAxis	The positive X-axis to set.
	 * @param yAxis	The positive Y-axis to set.
	 * @param zAxis	The positive Z-axis to set.
	 */
	public Mat3f(Vec3f xAxis, Vec3f yAxis, Vec3f zAxis)
	{
		m00 = xAxis.x;
		m01 = xAxis.y; 
		m02 = xAxis.z;
		
		m10 = yAxis.x;
		m11 = yAxis.y; 
		m12 = yAxis.z;
		
		m20 = zAxis.x;
		m21 = zAxis.y; 
		m22 = zAxis.z;
	}

	/** Zero all elements of this matrix. */
	public void zero() { m00 = m01 = m02 = m10 = m11 = m12 = m20 = m21 = m22 = 0.0f; }

	/** Reset this matrix to identity. */
	public void setIdentity()
	{
		// Set diagonal and then zero the rest of the matrix
		m00 = m11 = m22 = 1.0f;
		m01 = m02 = m10 = m12 = m20 = m21 = 0.0f;
	}

	/**
	 * Return a new matrix which is the transposed version of the provided matrix.
	 * 
	 * @param	m	The matrix which we will transpose (this matrix is not modified)
	 * @return		A transposed version of the provided matrix.
	 */
	public static Mat3f transpose(Mat3f m) { return new Mat3f(m.m00, m.m10, m.m20,   m.m01, m.m11, m.m21,   m.m02, m.m12, m.m22); }
		
	/**
	 * Create a rotation matrix from a given direction.
	 * <p>
	 * The reference direction is aligned to the Z-Axis, and the X-Axis is generated via the 
	 * genPerpendicularVectorQuick() method. The Y-Axis is then the cross-product of those two axes.
	 * <p>
	 * This method uses the <a href="https://gist.github.com/roxlu/3082114">Frisvad technique</a> for generating perpendicular axes. 
	 * 
	 * @param	referenceDirection	The vector to use as the Z-Axis
	 * @return	The created rotation matrix.
	 *
	 * @see Vec3f#genPerpendicularVectorQuick(Vec3f) 
	 */
	/*public static Mat3f createRotationMatrix(Vec3f referenceDirection)
	{	
	    Vec3f zAxis = referenceDirection.normalised();
	    Vec3f xAxis = Vec3f.genPerpendicularVectorQuick(referenceDirection); // This is returned normalised
	    Vec3f yAxis = Vec3f.crossProduct(xAxis, zAxis).normalise();
	    return new Mat3f(xAxis, yAxis, zAxis);
	}*/
	
	/**
	 * Create a rotation matrix from a given direction.
	 * <p>
	 * The reference direction is aligned to the Z-Axis. Note: The singularity is on the positive Y-Axis.
	 * <p>
	 * This method uses the <a href="https://gist.github.com/roxlu/3082114">Frisvad technique</a> for generating perpendicular axes. 
	 * 
	 * @param	referenceDirection	The vector to use as the Z-Axis
	 * @return	The created rotation matrix.
	 *
	 * @see Vec3f#genPerpendicularVectorQuick(Vec3f) 
	 */
	public static Mat3f createRotationMatrix(Vec3f referenceDirection)
	{	
		/*** You may want to try this - but the generated rotation matrix will be a little different (see below):
		     Note: There is no difference in solve distance between these, performance varies slightly - see test details on build (i.e. "mvn package")
		
		--- Rotation matrix creation (Meaten fix) ---	--- Rotation matrix creation (Pixar) ---

		Rotation matrix generated from plusX:
		X Axis: 0.000,	0.000,	1.000			X Axis: 0.000,	-0.000,	-1.000
		Y Axis: 0.000,	1.000,	0.000			Y Axis: -0.000,	1.000,	-0.000
		Z Axis: 1.000,	0.000,	0.000			Z Axis: 1.000,	0.000,	0.000

		Rotation matrix generated from plusY:
		X Axis: 1.000,	0.000,	0.000			X Axis: 1.000,	-0.000,	-0.000
		Y Axis: 0.000,	0.000,	1.000			Y Axis: -0.000,	0.000,	-1.000
		Z Axis: 0.000,	1.000,	0.000			Z Axis: 0.000,	1.000,	0.000

		Rotation matrix generated from plusZ:
		X Axis: -1.000,	0.000,	0.000			X Axis: 1.000,	-0.000,	-0.000
		Y Axis: 0.000,	1.000,	-0.000			Y Axis: -0.000,	1.000,	-0.000
		Z Axis: 0.000,	0.000,	1.000			Z Axis: 0.000,	0.000,	1.000

		Rotation matrix generated from minusX:
		X Axis: 0.000,	0.000,	-1.000			X Axis: 0.000,	0.000,	1.000
		Y Axis: 0.000,	1.000,	0.000			Y Axis: 0.000,	1.000,	-0.000
		Z Axis: -1.000,	0.000,	0.000			Z Axis: -1.000,	0.000,	0.000

		Rotation matrix generated from minusY:
		X Axis: 1.000,	0.000,	0.000			1.000,	0.000,	-0.000
		Y Axis: 0.000,	0.000,	-1.000			Y Axis: 0.000,	0.000,	1.000
		Z Axis: 0.000,	-1.000,	0.000			Z Axis: 0.000,	-1.000,	0.000

		Rotation matrix generated from minusZ:
		X Axis: 1.000,	-0.000,	0.000			X Axis: 1.000,	-0.000,	0.000
		Y Axis: 0.000,	1.000,	0.000			Y Axis: 0.000,	-1.000,	-0.000
		Z Axis: 0.000,	0.000,	-1.000			Z Axis: 0.000,	0.000,	-1.000
		
		// Create an orthonormal basis using Pixar's method.
		// Source: https://graphics.pixar.com/library/OrthonormalB/paper.pdf		
		
		float sign = Math.copySign(1.0f, referenceDirection.z);
		float a = -1.0f / (sign + referenceDirection.z);
		float b = referenceDirection.x * referenceDirection.y * a;
		Vec3f xAxis = new Vec3f(1.0f + sign * referenceDirection.x * referenceDirection.x * a, sign * b, -sign * referenceDirection.x);
		Vec3f yAxis = new Vec3f(b, sign + referenceDirection.y * referenceDirection.y * a, -referenceDirection.y);
	
		Mat3f rotMat = new Mat3f();
		rotMat.setZBasis( referenceDirection );		
		rotMat.setXBasis( xAxis.normalised() );
		rotMat.setYBasis( yAxis.normalised() );		
		return rotMat;
		***/
	
		/*** OLD VERSION 1.3.4 and earlier
		Vec3f xAxis;
		Vec3f yAxis;
		Vec3f zAxis = referenceDirection.normalise();
			
		// Handle the singularity (i.e. bone pointing along negative Z-Axis)...
		if(referenceDirection.z < -0.9999999f)
		{
			xAxis = new Vec3f(1.0f, 0.0f, 0.0f); // ...in which case positive X runs directly to the right...
			yAxis = new Vec3f(0.0f, 1.0f, 0.0f); // ...and positive Y runs directly upwards.
		}
		else
		{
			float a = 1.0f/(1.0f + zAxis.z);
			float b = -zAxis.x * zAxis.y * a;		    
			xAxis = new Vec3f(1.0f - zAxis.x * zAxis.x * a, b, -zAxis.x).normalise();
			yAxis = new Vec3f(b, 1.0f - zAxis.y * zAxis.y * a, -zAxis.y).normalise();
		}
		 
		return new Mat3f(xAxis, yAxis, zAxis);
		***/

		/*** NEW VERSION - 1.3.8 onwards ***/		
		
		Mat3f rotMat = new Mat3f();
		
		// Singularity fix provided by meaten - see: https://github.com/FedUni/caliko/issues/19
		if (Math.abs(referenceDirection.y) > 0.9999f)
		{
			rotMat.setZBasis(referenceDirection);
			rotMat.setXBasis( new Vec3f(1.0f, 0.0f, 0.0f));
			rotMat.setYBasis( Vec3f.crossProduct( rotMat.getXBasis(), rotMat.getZBasis()).normalised());
		}
		else
		{
			rotMat.setZBasis( referenceDirection );		
			rotMat.setXBasis( Vec3f.crossProduct( referenceDirection, new Vec3f(0.0f, 1.0f, 0.0f) ).normalised() );
			rotMat.setYBasis( Vec3f.crossProduct( rotMat.getXBasis(), rotMat.getZBasis() ).normalised() );
		}

		return rotMat;
		
	}
		
	/**
	 * Return whether this matrix consists of three orthogonal axes or not to within a cross-product of 0.01f.
	 *
	 * @return	Whether or not this matrix is orthogonal.
	 */
	public boolean isOrthogonal()
	{
		float xCrossYDot = Vec3f.dotProduct(this.getXBasis(), this.getYBasis());
		float xCrossZDot = Vec3f.dotProduct(this.getXBasis(), this.getZBasis());
		float yCrossZDot = Vec3f.dotProduct(this.getYBasis(), this.getZBasis());
		
		if ( Utils.approximatelyEquals(xCrossYDot, 0.0f,  0.01f) &&
		     Utils.approximatelyEquals(xCrossZDot, 0.0f,  0.01f) &&
		     Utils.approximatelyEquals(yCrossZDot, 0.0f,  0.01f) )
		{
			return true;
		}
		
		// implied else...
		return false;
	}

	/**
	 * Multiply this matrix by another matrix (in effect, combining them) and return the result as a new Mat3f.
	 * <p>
	 * Neither this matrix or the provided matrix argument are modified by this process - you must assign the result to your desired
	 * combined matrix.
	 * <p>
	 * To create a ModelView matrix using this method you would use viewMatrix.times(modelMatrix).
	 * To create a ModelViewProjection matrix using this method you would use projectionMatrix.times(viewMatrix).times(modelMatrix).
	 * 
	 * @param	m	The matrix to multiply this matrix by.
	 * @return		The resulting combined matrix.
	 */
	public Mat3f times(Mat3f m)
	{
		Mat3f temp = new Mat3f();

		temp.m00 = this.m00 * m.m00 + this.m10 * m.m01 + this.m20 * m.m02;
		temp.m01 = this.m01 * m.m00 + this.m11 * m.m01 + this.m21 * m.m02;
		temp.m02 = this.m02 * m.m00 + this.m12 * m.m01 + this.m22 * m.m02;

		temp.m10 = this.m00 * m.m10 + this.m10 * m.m11 + this.m20 * m.m12;
		temp.m11 = this.m01 * m.m10 + this.m11 * m.m11 + this.m21 * m.m12;
		temp.m12 = this.m02 * m.m10 + this.m12 * m.m11 + this.m22 * m.m12;

		temp.m20 = this.m00 * m.m20 + this.m10 * m.m21 + this.m20 * m.m22;
		temp.m21 = this.m01 * m.m20 + this.m11 * m.m21 + this.m21 * m.m22;
		temp.m22 = this.m02 * m.m20 + this.m12 * m.m21 + this.m22 * m.m22;

		return temp;
	}

	/** 
	 * Multiply a vector by this matrix and return the result as a new Vec3f.
	 *
	 * @param	source	The source vector to transform.
	 * @return		The provided source vector transformed by this matrix. 
	 */
	public Vec3f times(Vec3f source)
	{
		return new Vec3f(this.m00 * source.x + this.m10 * source.y + this.m20 * source.z,
				 this.m01 * source.x + this.m11 * source.y + this.m21 * source.z,
				 this.m02 * source.x + this.m12 * source.y + this.m22 * source.z);
	}

	/**
	 * Calculate and return the determinant of this matrix.
	 *
	 * @return	The determinant of this matrix.
	 */
	public float determinant() { return m20 * m01 * m12 - m20  * m02 * m11 - m10 * m01 * m22 + m10 * m02 * m21 + m00 * m11 * m22 - m00 * m12 * m21;	}

	/**
	 * Return a matrix which is the inverse of the provided matrix.
	 *
	 * @param	m	The matrix to invert.
	 * @return		The inverse matrix of of the provided matrix argument.
	 */
	public static Mat3f inverse(Mat3f m)
	{
		float d = m.determinant();
		
		Mat3f temp = new Mat3f();
		
		temp.m00 =  (m.m11  * m.m22 - m.m12 * m.m21) / d;
		temp.m01 = -(m.m01  * m.m22 - m.m02 * m.m21) / d;
		temp.m02 =  (m.m01  * m.m12 - m.m02 * m.m11) / d;
		temp.m10 = -(-m.m20 * m.m12 + m.m10 * m.m22) / d;
		temp.m11 =  (-m.m20 * m.m02 + m.m00 * m.m22) / d;
		temp.m12 = -(-m.m10 * m.m02 + m.m00 * m.m12) / d;
		temp.m20 =  (-m.m20 * m.m11 + m.m10 * m.m21) / d;
		temp.m21 = -(-m.m20 * m.m01 + m.m00 * m.m21) / d;
		temp.m22 =  (-m.m10 * m.m02 + m.m00 * m.m11) / d;

		return temp;
	}

	/**
	 *  Rotate this matrix by the provided angle about the specified axis.
	 *  
	 *  @param	angleRads		The angle to rotate the matrix, specified in radians.
	 *  @param	rotationAxis	The axis to rotate this matrix about, relative to the current configuration of this matrix.
	 *  @return					The rotated version of this matrix.
	 */ 
	public Mat3f rotateRads(Vec3f rotationAxis, float angleRads)
	{
		// Note: we need this temporary matrix because we cannot perform this operation 'in-place'.
		Mat3f dest = new Mat3f();

		float sin         = (float)Math.sin(angleRads);
		float cos         = (float)Math.cos(angleRads);		
		float oneMinusCos = 1.0f - cos;

		float xy = rotationAxis.x * rotationAxis.y;
		float yz = rotationAxis.y * rotationAxis.z;
		float xz = rotationAxis.x * rotationAxis.z;
		float xs = rotationAxis.x * sin;
		float ys = rotationAxis.y * sin;
		float zs = rotationAxis.z * sin;

		float f00 = rotationAxis.x * rotationAxis.x * oneMinusCos + cos;
		float f01 = xy * oneMinusCos + zs;
		float f02 = xz * oneMinusCos - ys;

		float f10 = xy * oneMinusCos - zs;
		float f11 = rotationAxis.y * rotationAxis.y * oneMinusCos + cos;
		float f12 = yz * oneMinusCos + xs;

		float f20 = xz * oneMinusCos + ys;
		float f21 = yz * oneMinusCos - xs;
		float f22 = rotationAxis.z * rotationAxis.z * oneMinusCos + cos;

		float t00 = m00 * f00 + m10 * f01 + m20 * f02;
		float t01 = m01 * f00 + m11 * f01 + m21 * f02;
		float t02 = m02 * f00 + m12 * f01 + m22 * f02;

		float t10 = m00 * f10 + m10 * f11 + m20 * f12;
		float t11 = m01 * f10 + m11 * f11 + m21 * f12;
		float t12 = m02 * f10 + m12 * f11 + m22 * f12;

		// Construct and return rotation matrix
		dest.m20 = m00 * f20 + m10 * f21 + m20 * f22;
		dest.m21 = m01 * f20 + m11 * f21 + m21 * f22;
		dest.m22 = m02 * f20 + m12 * f21 + m22 * f22;

		dest.m00 = t00;
		dest.m01 = t01;
		dest.m02 = t02;

		dest.m10 = t10;
		dest.m11 = t11;
		dest.m12 = t12;

		return dest;
	}

	/**
	 *  Rotate this matrix by the provided angle about the specified axis.
	 *  
	 *  @param	angleDegs	The angle to rotate the matrix, specified in degrees.
	 *  @param	localAxis	The axis to rotate this matrix about, relative to the current configuration of this matrix.
	 *  @return			The rotated version of this matrix.
	 *  */ 
	public Mat3f rotateDegs(float angleDegs, Vec3f localAxis) { return this.rotateRads(localAxis, angleDegs * DEGS_TO_RADS); }

	/**
	 * Set the X basis of this matrix.
	 *
	 * @param	v	The vector to use as the X-basis of this matrix.
	 */
	public void setXBasis(Vec3f v) { m00 = v.x; m01 = v.y; m02 = v.z; }
	
	/**
	 * Get the X basis of this matrix.
	 * 
	 * @return The X basis of this matrix as a Vec3f
	 **/
	public Vec3f getXBasis() { return new Vec3f(m00, m01, m02); }

	/**
	 * Set the Y basis of this matrix.
	 *
	 * @param	v	The vector to use as the Y-basis of this matrix.
	 */
	public void setYBasis(Vec3f v) { m10 = v.x; m11 = v.y; m12 = v.z; }
	
	/**
	 * Get the Y basis of this matrix.
	 * 
	 * @return The Y basis of this matrix as a Vec3f
	 **/
	public Vec3f getYBasis() { return new Vec3f(m10, m11, m12); }

	/**
	 * Set the Z basis of this matrix.
	 *
	 * @param	v	The vector to use as the Z-basis of this matrix.
	 */
	public void setZBasis(Vec3f v) { m20 = v.x; m21 = v.y; m22 = v.z; }

	/**
	 * Get the Z basis of this matrix.
	 * 
	 * @return The Z basis of this matrix as a Vec3f
	 **/
	public Vec3f getZBasis() { return new Vec3f(m20, m21, m22); }

	/**
	 * Return this Mat3f as an array of 9 floats.
	 * 
	 * @return	This Mat3f as an array of 9 floats.
	 */
	public float[] toArray() { return new float[] { m00, m01, m02, m10, m11, m12, m20, m21, m22 }; }

	// Note: Displays output in COLUMN-MAJOR format!
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();		
		sb.append("X Axis: " + df.format(m00) + ",\t" + df.format(m01) + ",\t" + df.format(m02) + NEW_LINE);
		sb.append("Y Axis: " + df.format(m10) + ",\t" + df.format(m11) + ",\t" + df.format(m12) + NEW_LINE);
		sb.append("Z Axis: " + df.format(m20) + ",\t" + df.format(m21) + ",\t" + df.format(m22) + NEW_LINE);
		return sb.toString();
	}
}
