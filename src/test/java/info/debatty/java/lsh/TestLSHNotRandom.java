package info.debatty.java.lsh;

import junit.framework.TestCase;

public class TestLSHNotRandom extends TestCase {

	public void testNotRandomLSHExample() {
		int count = 10;
		int n = 5;
		int b = 2;

		boolean[][] vectors = { { false, true, false, false, false }, { true, true, true, false, false },
				{ false, false, true, true, false }, { false, false, true, false, true },
				{ false, false, true, true, false }, { false, false, false, false, false },
				{ false, false, false, false, false }, { false, false, false, false, true },
				{ true, false, false, false, false }, { true, false, false, false, false } };

		for (int stages = 1; stages <= b; stages++) {
			// Compute the LSH hash of each vector
			LSHMinHash lsh = new LSHMinHash(stages, b, n);
			int[][] hashes = new int[count][];
			for (int i = 0; i < count; i++) {
				boolean[] vector = vectors[i];
				hashes[i] = lsh.hash(vector);
			}

			// We now have the LSH hash for each input set
			// Let's have a look at how similar sets (according to Jaccard
			// index) were binned...
			int[][] results = new int[11][2];
			for (int i = 0; i < vectors.length; i++) {
				boolean[] vector1 = vectors[i];
				int[] hash1 = hashes[i];

				for (int j = 0; j < i; j++) {
					boolean[] vector2 = vectors[j];
					int[] hash2 = hashes[j];

					// We compute the similarity between each pair of sets
					double similarity = MinHash.jaccardIndex(vector1, vector2);

					// We count the number of pairs with similarity 0.1, 0.2,
					// 0.3, etc.
					results[(int) (10 * similarity)][0]++;

					// Do they fall in the same bucket for one of the stages?
					for (int stage = 0; stage < stages; stage++) {
						if (hash1[stage] == hash2[stage]) {
							results[(int) (10 * similarity)][1]++;
							break;
						}
					}
				}
			}

			// Now we can display (and plot in Gnuplot) the result:
			// For pairs that have a similarity x, the probability of falling
			// in the same bucket for at least one of the stages is y
			for (int i = 0; i < results.length; i++) {
				double similarity = (double) i / 10;

				double probability = 0;
				if (results[i][0] != 0) {
					probability = (double) results[i][1] / results[i][0];
				}
				System.out.println("" + similarity + "\t" + probability + "\t" + stages);
			}

			// Separate the series for Gnuplot...
			System.out.print("\n");
		}
	}

	public void testNoRandomSimpleLSH() {
		// Size of vectors
		int n = 5;

		// LSH parameters
		// the number of stages is also sometimes called the number of bands
		int stages = 3;

		// Attention: to get relevant results, the number of elements per bucket
		// should be at least 100
		int buckets = 10;

		boolean[][] vectors = { { false, true, false, false, false }, { true, true, true, false, false },
				{ false, false, true, true, false }, { false, false, true, false, true },
				{ false, false, true, true, false }, { false, false, false, false, false },
				{ false, false, false, false, false }, { false, false, false, false, true },
				{ true, false, false, false, false }, { true, false, false, false, false } };

		// Create and configure LSH algorithm
		LSHMinHash lsh = new LSHMinHash(stages, buckets, n);

		int[][] counts = new int[stages][buckets];
		String[][] bucket = new String[stages][buckets];

		// Perform hashing
		for (boolean[] vector : vectors) {
			int[] hash = lsh.hash(vector);

			for (int i = 0; i < hash.length; i++) {
				StringBuffer sb = new StringBuffer();
				counts[i][hash[i]]++;
				if(bucket[i][hash[i]] != null) {
					bucket[i][hash[i]] = sb.append(bucket[i][hash[i]]).append(",").append(toString(vector)).toString();					
				} else {
					bucket[i][hash[i]] = toString(vector);
				}
			}

			print(vector);
			System.out.print(" : ");
			print(hash);
			System.out.print("\n");
		}

		System.out.println("Number of elements per bucket at each stage:");
		for (int i = 0; i < stages; i++) {
			print(counts[i]);
			System.out.print("\n");
		}

		System.out.println("Elements in bucket at each stage:");
		int i = 1;
		for (String[] v : bucket) {
			for (String r : v) {
				if (r != null) {
					System.out.print(r);;
					System.out.print("\n");
				} else {
					System.out.print("null\n");
				}

			}
			System.out.print("Stage "+i+"\n");
			i++;
		}

	}

	static void print(int[] array) {
		System.out.print("[");
		for (int v : array) {
			System.out.print("" + v + " ");
		}
		System.out.print("]");
	}

	static void print(boolean[] array) {
		System.out.print("[");
		for (boolean v : array) {
			System.out.print(v ? "1" : "0");
		}
		System.out.print("]");
	}
	
	static String toString(boolean[] array) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(boolean v: array) {
			sb.append(v ? "1" : "0");
		}
		sb.append("]");
		return sb.toString();
	}
}
