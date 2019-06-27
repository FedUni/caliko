package au.edu.federation.utils;

import java.io.Serializable;


/**
 * Class to represent a RGBA colour as four floating point values.
 * <p>
 * The r, g, b and a properties of the class are publicly accessible for direct access, but
 * constructors and the 'set' method clamp the colour components to the valid range 0.0f
 * to 1.0f. Setting colour component values outside of this range may result in undefined
 * behaviour.
 *
 * @author Al Lansley
 * @version 1.0 - 20/06/2019
 */
public class Colour4f implements Serializable
{
	private static final long serialVersionUID = 1L;

	// ---------- Properties ----------

	/** The minimum valid value of any colour component. */
	private static final float MIN_COMPONENT_VALUE = 0.0f;

	/** The maximum valid value of any colour component. */
	private static final float MAX_COMPONENT_VALUE = 1.0f;

	/** Red component. Publicly accessible. */
	public float r;

	/** Green component. Publicly accessible. */
	public float g;

	/** Blue component. Publicly accessible. */
	public float b;

	/** Alpha (transparency) component. Publicly accessible. */
	public float a;

	// ---------- Constructors ----------

	/**
	 * Default constructor.
	 * <p>
	 * The constructed Colour4f has it's RGBA components set to default values of 1.0f, which equates to white at full opacity.
	 */
	public Colour4f() {	r = g = b = a = 1.0f; }

	/**
	 * Copy constructor.
	 *
	 * @param	source The source Colour4f object to copy the component values from.
	 */
	public Colour4f(Colour4f source) { r = source.r; g = source.g; b = source.b; a = source.a; }

	/**
	 * Array Constructor.
	 * <p>
	 * If the array size is not 4, then an IllegalArgument exception is thrown.
	 *
	 * @param	sourceValues	The array of floats to copy the values from.
	 */
	public Colour4f(float[] sourceValues)
	{
		if (sourceValues.length == 4)
		{
			this.r = Colour4f.clamp( sourceValues[0] );
			this.g = Colour4f.clamp( sourceValues[1] );
			this.b = Colour4f.clamp( sourceValues[2] );
			this.a = Colour4f.clamp( sourceValues[3] );
		}
		else
		{
			throw new IllegalArgumentException("Colour source array size must be precisely 4 elements.");
		}
	}

	/**
	 * Float Constructor.
	 * <p>
	 * The valid range of each component is 0.0f to 1.0f inclusive, any values outside of this range will be clamped.
	 *
	 * @param   red		The red component of this colour.
	 * @param   green	The green component of this colour.
	 * @param   blue	The blue component of this colour.
	 * @param   alpha	The alpha component of this colour.
	 */
	public Colour4f(float red, float green, float blue, float alpha)
	{
		this.r = Colour4f.clamp(red);
		this.g = Colour4f.clamp(green);
		this.b = Colour4f.clamp(blue);
		this.a = Colour4f.clamp(alpha);
	}

	// ---------- Public Methods ----------

	/**
	 * Set the RGBA values of this Colour4f object from a source Colour4f object.
	 * <p>
	 * Source values are clamped to the range 0.0f..1.0f.
	 *
	 * @param   source 	The source colour to set the values of this colour to.
	 */
	public void set(Colour4f source)
	{
		this.r = Colour4f.clamp(source.r);
		this.g = Colour4f.clamp(source.g);
		this.b = Colour4f.clamp(source.b);
		this.a = Colour4f.clamp(source.a);
	}

	/**
	 * Set the RGBA values of this Colour4f object from a red, green, blue and alpha value.
	 * <p>
	 * Any values outside the range 0.0f..1.0f are clamped to the nearest valid value.
	 * <p>
	 * @param   red		(float) The red   component to set for this colour.
	 * @param   green	(float) The green component to set for this colour.
	 * @param   blue	(float) The blue  component to set for this colour.
	 * @param   alpha	(float) The alpha component to set for this colour.
	 */
	public void set(final float red, final float green, final float blue, final float alpha)
	{
		this.r = Colour4f.clamp(red);
		this.g = Colour4f.clamp(green);
		this.b = Colour4f.clamp(blue);
		this.a = Colour4f.clamp(alpha);
	}

	/**
	 * Add to the RGB components of this colour by the given amounts and return this modified colour for chaining.
	 * <p>
	 * When adding, colour values are clamped to a maximum value of 1.0f.
	 *
	 * @param   red		The red   component to add to this colour.
	 * @param   green	The green component to add to this colour.
	 * @param   blue	The blue  component to add to this colour.
	 * @return			This modified colour.
	 */
	public Colour4f addRGB(float red, float green, float blue)
	{
		this.r = Colour4f.clamp(this.r + red);
		this.g = Colour4f.clamp(this.g + green);
		this.b = Colour4f.clamp(this.b + blue);
		return this;
	}

	/** Subtract from the RGB components of this colour by the given amounts and return this modified colour for chaining.
	 * <p>
	 * When subtracting, colour values are clamped to a minimum value of 1.0f.
	 *
	 * @param   red		The red   component to add to this colour.
	 * @param   green	The green component to add to this colour.
	 * @param   blue	The blue  component to add to this colour.
	 * @return			This modified colour.
	 */
	public Colour4f subtractRGB(float red, float green, float blue)
	{
		this.r = Colour4f.clamp(this.r - red);
		this.g = Colour4f.clamp(this.g - green);
		this.b = Colour4f.clamp(this.b - blue);
		return this;
	}

	/**
	 * Lighten the RGB components of this colour by a given amount.
	 * <p>
	 * Resulting colour components are clamped to the range 0.0f..1.0f.
	 *
	 * @param	amount	The value to add to each (RGB only) component of the colour.
	 * @return			The 'lightened' colour with the amount added to each component.
	 */
	public Colour4f lighten(float amount) { return addRGB(amount, amount, amount); }

	/**
	 * Darken the RGB components of this colour by a given amount.
	 * <p>
	 * Resulting colour components are clamped to the range 0.0f..1.0f.
	 *
	 * @param	amount	The value to subtract from each (RGB only) component of the colour.
	 * @return			The 'darkened' colour with the amount subtracted from each component.
	 */
	public Colour4f darken(float amount) { return subtractRGB(amount, amount, amount); }

	/**
	 * Return this colour as an array of four floats.
	 *
	 * @return	This colour as an array of four floats.
	 */
	public float[] toArray() { return new float[] { r, g, b, a }; }

	/** Return a concise, human-readable description of the Colour4f object. */
	@Override
	public String toString() { return "Red: " + r + ", Green: " + g  + ", Blue: " + b + ", Alpha: " + a; }

	// ---------- Static Methods -----------

	/**
	 * Return a random colour with an alpha value of 1.0f (i.e. fully opaque)
	 *
	 * @return	The random opaque colour.
	 */
	public static Colour4f randomOpaqueColour()
	{
		return new Colour4f(Utils.random.nextFloat(), Utils.random.nextFloat(), Utils.random.nextFloat(), 1.0f);
	}

	// ---------- Private Methods ----------

	private static float clamp(final float componentValue)
	{
		if      (componentValue > MAX_COMPONENT_VALUE) { return MAX_COMPONENT_VALUE; }
		else if (componentValue < MIN_COMPONENT_VALUE) { return MIN_COMPONENT_VALUE; }
		else    { return componentValue; }
	}

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(a);
    result = prime * result + Float.floatToIntBits(b);
    result = prime * result + Float.floatToIntBits(g);
    result = prime * result + Float.floatToIntBits(r);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Colour4f other = (Colour4f) obj;
    if (Float.floatToIntBits(a) != Float.floatToIntBits(other.a)) {
      return false;
    }
    if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b)) {
      return false;
    }
    if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g)) {
      return false;
    }
    if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r)) {
      return false;
    }
    return true;
  }
}
