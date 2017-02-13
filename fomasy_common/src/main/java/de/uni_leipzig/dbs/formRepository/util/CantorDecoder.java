package de.uni_leipzig.dbs.formRepository.util;



	public class CantorDecoder {

		/**
		 * The method codes the number pair a und b and returns the result. 
		 * @param a number of code number pair
		 * @param b number of code number pair
		 * @return coded number
		 */
		public static long code(long a, long b) {
			return (long)(0.5*(a+b)*(a+b+1)+b);
		}
		
		/**
		 * The method decodes the first number of the basic number pair 
		 * for which the code result is given. 
		 * @param c code result to be decoded
		 * @return first number of the number pair
		 */
		public static long decode_a(long c) {
			long a = (long) Math.floor(Math.sqrt(0.25 + 2*c) - 0.5);
			return a - (c - a*(a+1)/2);
			//return s(c)-decode_b(c);
		}
		
		/**
		 * The method decodes the second number of the basic number pair 
		 * for which the code result is given. 
		 * @param c code result to be decoded
		 * @return second number of the number pair
		 */
		public static long decode_b(long  c) {
			//long s = s(c);
			long b  = (long) Math.floor(Math.sqrt(0.25 + 2*c) - 0.5);
			return c -b*(b+1)/2;
		}
		
		/**
		 * The method returns the value for the s-function.
		 * @param n number to be decoded
		 * @return s-value
		 */
		private static long s(long n) {
			for (int i=1;;i++) {
				if ((0.5*i*(i+1))<=n) continue;
				else return i-1;
			}
		}
	}

