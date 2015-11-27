
/**
 * While the nodes are stored in large contiguous arrays inside this class,
 * they are accessed via coordinates from outside this class.
 * A set of coordinates is (row number, node number in the row), starting at (1,1)
 * for the root of the triangle
 */

final class Triangle {

	/**
	 * any node in a triangle is surrounded by at most 2 adjacent nodes.
	 * (either below or above, depending on the direction of the path)
	 * this is a class invariant.
	 */
	
	private final static int ADJACENT_NODE_ARRAY_MAXIMUM_LENGTH = 2;

	/**
	 * Since nodes are accessed via coordinates, we don't want to keep
	 * on recalculating where each row begins
	 */

	private final static int INVALID_ROW_INDEX_OFFSET = -1;
	private final int[] iRowIndexOffsetIntArray;

	/**
	 * The information associated with each node is:
	 * - a constant node value
	 * - a path value required to reach the node
	 * - whether the path value has actually become final 
	 * (i.e. whatever algorithm we're running, has it finished processing the node?) 
	 */

	private final int[] iNodeValueIntArray;
	private final int[] iPathValueIntArray;
	private final boolean[] isNodeFinalBoolArray;

	/**
	 * @param aRowCount number of rows in the Triangle.
	 * The triangle contains (aRowCount)*(aRowCount+1)/2 nodes.
	 * The last valid set of coordinate is (aRowCount, aRowCount)
	 * for the most bottom-right leaf of the triangle
	 */

	/*package*/ Triangle(int aRowCount) {
		iRowIndexOffsetIntArray = new int[aRowCount];
		for(int ii = 0 ; ii < aRowCount ; ++ii) {
			iRowIndexOffsetIntArray[ii] = INVALID_ROW_INDEX_OFFSET;
		}
		final int nodeCount = (aRowCount * (aRowCount + 1)) / 2;
		iNodeValueIntArray = new int[nodeCount];
		iPathValueIntArray = new int[nodeCount];
		isNodeFinalBoolArray = new boolean[nodeCount];
		for(int jj = 0 ; jj < nodeCount ; ++jj) {
			iPathValueIntArray[jj] = Integer.MAX_VALUE; // reasonable assumption
			isNodeFinalBoolArray[jj] = false;
		}
	}

	/*package*/ int getRowCount() {
		return iRowIndexOffsetIntArray.length;
	}

	/*package*/ int getAdjacentNodeArrayMaximumLength() {
		return ADJACENT_NODE_ARRAY_MAXIMUM_LENGTH;
	}
	
	/*package*/ void setNodeValue(int aRowCount, int aNodeCount, int aNodeValue) {
		iNodeValueIntArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aRowCount)] + aNodeCount - 1] = aNodeValue;
	}
	
	/*package*/ int getNodeValue(int aRowCount, int aNodeCount) {
		return iNodeValueIntArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aRowCount)] + aNodeCount - 1];
	}

	/*package*/ void setNodeIsFinal(int aRowCount, int aNodeCount) {
		isNodeFinalBoolArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aRowCount)] + aNodeCount - 1] = true;
	}

	/*package*/ void setNodePathValue(int aRowCount, int aNodeCount, int aPathValue) {
		iPathValueIntArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aRowCount)] + aNodeCount - 1] = aPathValue;
	}

	/*package*/ int getNodePathValue(int aRowCount, int aNodeCount) {
		return iPathValueIntArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aRowCount)] + aNodeCount - 1];
	}

	/**
	 * @return second coordinate of triangle leaf with minimal (path value + node value)
	 */

	/*package*/ int getMinimalPathValueLeafCoordinate() {
		int minimalPathValue = Integer.MAX_VALUE;
		int result = 0;
		for(int ii = iRowIndexOffsetIntArray[iRowIndexOffsetIntArray.length - 1] ; ii < iNodeValueIntArray.length ; ++ii)
		{
			final int pathValue = iPathValueIntArray[ii] + iNodeValueIntArray[ii];
			if(minimalPathValue > pathValue) {
				result = ii;
				minimalPathValue = pathValue;
			}
		}
		// result is an index. turn it into a coordinate
		return result - iRowIndexOffsetIntArray[iRowIndexOffsetIntArray.length - 1] + 1;
	}

	/**
	 * 
	 * finds the adjacent nodes of a given node in the triangle.
	 * 
	 * @param aCoordinateRow first coordinate of the node to examine
	 * @param aCoordinateNode second coordinate of the node to examine
	 * @param aIsAdjacentFinal used to search for adjacent nodes that have been marked as final or adjacent nodes that are still non-final.
	 * @param aResultCoordinateArray where to store the coordinates of the adjacent nodes. content to be overwritten. length must be at least 4
	 * @return number of set of 2 coordinates stored by this method in aNodeCoordinateArray
	 */

	/*package*/ int getAdjacentNodeCoordinates(int aCoordinateRow, int aCoordinateNode, boolean aIsAdjacentFinal, int[] aResultCoordinateArray) {
		int resultIndex = 0;
		// 2 possible adjacent nodes. either above or below.
		if(aIsAdjacentFinal) {
			if(aCoordinateRow > 1) {
				if(aCoordinateNode > 1) { //above, left
					if(aIsAdjacentFinal == isNodeFinalBoolArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aCoordinateRow - 1)] + aCoordinateNode - 2]) {
						aResultCoordinateArray[resultIndex++] = aCoordinateRow - 1;
						aResultCoordinateArray[resultIndex++] = aCoordinateNode - 1;
					}
				}
				if(aCoordinateNode < aCoordinateRow) { // above, right
					if(aIsAdjacentFinal == isNodeFinalBoolArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aCoordinateRow - 1)] + aCoordinateNode - 1]) {
						aResultCoordinateArray[resultIndex++] = aCoordinateRow - 1;
						aResultCoordinateArray[resultIndex++] = aCoordinateNode;
					}
				}
			}
		} else {
			if(aCoordinateRow < iRowIndexOffsetIntArray.length) {
				//below, left
				if(aIsAdjacentFinal == isNodeFinalBoolArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aCoordinateRow+1)] + aCoordinateNode - 1]) {
					aResultCoordinateArray[resultIndex++] = aCoordinateRow + 1;
					aResultCoordinateArray[resultIndex++] = aCoordinateNode;
				}
				// below, right
				if(aIsAdjacentFinal == isNodeFinalBoolArray[iRowIndexOffsetIntArray[ensureRowIndexOffset(aCoordinateRow+1)] + aCoordinateNode]) {
					aResultCoordinateArray[resultIndex++] = aCoordinateRow + 1;
					aResultCoordinateArray[resultIndex++] = aCoordinateNode + 1;
				}
			}
		}
		return resultIndex/2;
	}

	/**
	 * Calculates and caches in iRowIndexOffsetIntArray the index where a triangle row begins in iNodeValueIntArray, iPathValueIntArray and isNodeFinalBoolArray
	 *  
	 * @return (aCoordinateRow - 1) which is the index for the row in iRowIndexOffsetIntArray, so the method call can be chained.
	 */

	private int ensureRowIndexOffset(int aCoordinateRow) {
		final int rowIndex = aCoordinateRow - 1;
		if(INVALID_ROW_INDEX_OFFSET == iRowIndexOffsetIntArray[rowIndex]) {
			iRowIndexOffsetIntArray[rowIndex] = (aCoordinateRow * rowIndex)/2;
		}
		return rowIndex;
	}

}
