import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * TriangleInputProcessor is in charge of verifying the validity of the standard input
 * and populating a Triangle object with its initial set of node values.
 *
 */

final class TriangleInputProcessor {

	private final static String INPUT_NODE_SEPARATOR = " ";

	/**
	 * @return a new <code>Triangle</code> containing nodes matching the standard input
	 * @throws <code>TriangleInputException</code> if the standard input doesn't represent a valid <code>Triangle</code>
	 * 
	 */

	/*package*/ Triangle makeTriangle() throws TriangleInputException {
		ArrayList<String> inputStringArrayList = readAllStandardInput();
		return makeTriangle(inputStringArrayList);
	}

	/**
	 * @return a new ArrayList<String> containing all the standard input
	 * @throws <code>TriangleInputException</code> if a line can't be read from standard input
	 * 
	 * readAllStandardInput vacuums the entire standard input into RAM before processing it.
	 * good for speed, bad for memory consumption.
	 * 
	 * TODO generate less garbage than BufferedReader if necessary
	 */

	private ArrayList<String> readAllStandardInput() throws TriangleInputException {
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> result = new ArrayList<String>(MinTrianglePath.ROW_COUNT_PERFORMANCE_TARGET);
		String inputLine = null;
		try {
			// readLine returns null if the line is empty
			while(null!=(inputLine = inputReader.readLine())) {
				result.add(inputLine);
			}
		} catch (IOException ioex) {
			try {
				inputReader.close();
			} catch (IOException ioex2) {
				//don't care
			}
			throw new TriangleInputException(TriangleInputException.BAD_LINE, result.size());
		}
		try {
			inputReader.close();
		} catch (IOException ioex2) {
			//don't care
		}
		return result;
	}

	/**
	 * @param aStringArrayList contains one String per Triangle row.
	 * @return a new <code>Triangle</code> containing node values taken from aStringArrayList
	 * @throws <code>TriangleInputException</code> if the standard input doesn't represent a valid <code>Triangle</code>
	 * 
	 */

	private Triangle makeTriangle(ArrayList<String> aStringArrayList) throws TriangleInputException {
		// allocate a big chunk of memory for the Triangle data
		Triangle result = new Triangle(aStringArrayList.size());
		int rowCount = 0;
		for(Iterator<String> iterator = aStringArrayList.iterator() ; iterator.hasNext();) {
			// for row n, we should find n-1 spaces separating n node values.
			String[] nodeValueStringArray = iterator.next().split(INPUT_NODE_SEPARATOR);
			if(nodeValueStringArray.length != ++rowCount) {
				throw new TriangleInputException(TriangleInputException.BAD_SPACING, rowCount);
			}
			int nodeCount = 0;
			for(int ii = 0 ; ii < nodeValueStringArray.length ; ++ii) {
				try {
					// set the value of 1 node in the triangle
					result.setNodeValue(rowCount, ++nodeCount, Integer.parseInt(nodeValueStringArray[ii]));
				} catch (NumberFormatException nfex) {
					throw new TriangleInputException(TriangleInputException.BAD_NUMBER, rowCount, nodeCount);
				}
			}
		}
		return result;
	}

}
