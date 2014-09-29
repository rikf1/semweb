/**
 * Easy workaround for System.out.print() and System.out.println()
 * @author Rik
 */

package core;

public class Echo {
	
	/**
	 * Prints object followed by line break
	 * @param s - Text to print
	 */
	public void ln(Object o)
	{
		System.out.println(o);
	}

	/**
	 * Prints string followed by three dots and no line break
	 * @param s	- Text to print
	 */
	public void dot3(String s)
	{
		System.out.print(s+"... ");
	}
	
	/**
	 * Prints string followed by three dots and line break
	 * @param s	- Text to print
	 */
	public void dot3ln(String s)
	{
		System.out.println(s+"... ");
	}
	
	/**
	 * Prints OK followed by linebreak
	 */
	public void ok()
	{
		System.out.println("OK");
	}
	
	/**
	 * Prints FAILED followed by line break
	 */
	public void fail()
	{
		System.out.println("FAILED");
	}
}
