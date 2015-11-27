
/**
 * 
 * @author michael aubert
 * 
 * The Dijkstra algorithm seems to be a solution to the basic problem in the exercise.
 * 
 * For the first time in this developer's career we are going to use a liberal amount
 * of contiguous memory instead of linked objects (i.e. triangle nodes, Fibonacci heap) as an experiment
 * to see if increased use of the CPU cache and instruction pipelines can achieve the
 * required amount of performance.
 * 
 * Thanks for giving me an exercise that lends itself to this approach.
 *
 * Test result: 10 different 500 rows triangle, all processed in less than 500ms
 * (not counting VM initialization time. I only measured the time the main() method takes to run)
 * on my early 2011 laptop with an i7 Q740 1.73GHz CPU. Cygwin cat used to pipe a file containing
 * the triangle. No Solid State Drive.
 *
 * This took me close to 20 hours, mostly because I did not even know what the Dijkstra
 * algorithm was for (or how to spell it) when I started.
 *
 */

public class MinTrianglePath {

	/**
	 * MinTrianglePath will work with all Triangle size that fit in the computer memory
	 * (keeping in mind this program is NOT optimised for memory usage)
	 * but ROW_COUNT_PERFORMANCE_TARGET can be used to ensure that the speed requirement is achieved.
	 */
	
	/*package*/ final static int ROW_COUNT_PERFORMANCE_TARGET = 500;


	public static void main(String[] args) {
		Triangle triangle = null;
		TriangleInputProcessor inputProcessor = new TriangleInputProcessor();
		try {
			triangle = inputProcessor.makeTriangle();
		} catch(TriangleInputException tiex) {
			tiex.printStackTrace();
		}
		if (null != triangle) {
			DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(triangle.getRowCount());
			int[] result = dijkstra.runAlgorithm(triangle);		
			PathOutputProcessor outputProcessor = new PathOutputProcessor();
			outputProcessor.processPath(result);
		}
	}

}
