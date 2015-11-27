/*
 * see http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
 * 
 * Modifications to the standard algorithm:
 * - the cost to minimize is stored in the nodes, not in the links between nodes.
 * - we target all the triangle leafs, not just the first one we reach
 * - Dijkstra only populates the triangle with additional information. We need
 * another step afterwards to reverse-engineer the minimal path from that information
 * (find a leaf with a minimal path and travel back up the triangle to the root)
 * 
 */


final class DijkstraAlgorithm {

	/**
	 * Neighbour nodes are stored in arrays large enough to contain all the nodes in the triangle.
	 * 
	 * This clearly wastes memory but the hope is that, on a modern CPU with a decent amount of cache,
	 * using System.arraycopy to keep the arrays sorted will be faster (or at least fast enough for up to 500 rows)
	 * than manipulating a Fibonacci heap (the usually preferred way of keeping track of neighbouring nodes)
	 * and the many cache-miss we expect it involves.
	 * 
	 */

	private final int[] iNeighbourRowCoordinateIntArray;
	private final int[] iNeighbourNodeCoordinateIntArray;
	private int iNeighbourNodeCount;

	/*package*/ DijkstraAlgorithm(int aRowCount) {
		final int nodeCount = aRowCount * (aRowCount + 1) / 2 ;
		iNeighbourRowCoordinateIntArray = new int[nodeCount];
		iNeighbourNodeCoordinateIntArray = new int[nodeCount];
	}

	/**
	 * Overwrites the content of iNeighbourNodeCoordinateIntArray
	 * 
	 * @return an int[] containing the node values of a minimal path to a leaf in aTriangle 
	 */

	/*package*/ int[] runAlgorithm(Triangle aTriangle) {
		// initialize the triangle root
		iNeighbourRowCoordinateIntArray[0]=1;
		iNeighbourNodeCoordinateIntArray[0]=1;
		iNeighbourNodeCount = 1;
		int visitedTargetCount = 0; // we stop when we have a minimal path for all the leafs.
		aTriangle.setNodePathValue(1, 1, 0);
		// 2 coordinates per node
		final int[] neighbourCoordinateIntArray = new int[2 * aTriangle.getAdjacentNodeArrayMaximumLength()];
		final int[] coordinateIntArray = new int[2];
		// Dijkstra loop
		while((visitedTargetCount < aTriangle.getRowCount()) && (0 < iNeighbourNodeCount)) {
			popBestNeighbourCoordinates(coordinateIntArray);
			// it was a neighbour. it is now the current node.
			final int currentPathValue = aTriangle.getNodePathValue(coordinateIntArray[0], coordinateIntArray[1]);
			final int neighbourCoordinateCount = aTriangle.getAdjacentNodeCoordinates(coordinateIntArray[0], coordinateIntArray[1], false, neighbourCoordinateIntArray);
			final int currentNodeValue = aTriangle.getNodeValue(coordinateIntArray[0], coordinateIntArray[1]);
			for(int ii = 0 ; ii < neighbourCoordinateCount * 2 ; ++ii) {
				final int neighbourRowCoordinate = neighbourCoordinateIntArray[ii];
				final int neighbourNodeCoordinate = neighbourCoordinateIntArray[++ii];
				final int neighbourPathValue = aTriangle.getNodePathValue(neighbourRowCoordinate, neighbourNodeCoordinate);
				final int tentativePathValue = currentPathValue + currentNodeValue;
				if(tentativePathValue < neighbourPathValue) {
					if(Integer.MAX_VALUE != neighbourPathValue) { // first time we examine the neighbour
						removeFromNeighbouringNodes(aTriangle, neighbourRowCoordinate, neighbourNodeCoordinate, neighbourPathValue);
					}
					insertIntoNeighbouringNodes(aTriangle, neighbourRowCoordinate, neighbourNodeCoordinate, tentativePathValue);
					aTriangle.setNodePathValue(neighbourRowCoordinate, neighbourNodeCoordinate, tentativePathValue);
				}
			}
			aTriangle.setNodeIsFinal(coordinateIntArray[0], coordinateIntArray[1]);
			if(coordinateIntArray[0] == aTriangle.getRowCount()) { // current node is a leaf
				++visitedTargetCount;
			}
		}
		return travelBackFromMinimalLeafToRoot(aTriangle);
	}

	/**
	 * Overwrites the content of iNeighbourNodeCoordinateIntArray
	 */
	private int[] travelBackFromMinimalLeafToRoot(Triangle aTriangle) {
		// find path from best leaf to root.
		// we can store it in iValueOrderedAdjacentNodeArray since we don't need it anymore
		final int[] adjacentCoordinateArray = new int[2 * aTriangle.getAdjacentNodeArrayMaximumLength()];
		final int[] currentCoordinate = new int[2];
		currentCoordinate[0] = aTriangle.getRowCount();
		currentCoordinate[1] = aTriangle.getMinimalPathValueLeafCoordinate();
		int pathNodeCount = 0;
		iNeighbourNodeCoordinateIntArray[pathNodeCount++] = aTriangle.getNodeValue(currentCoordinate[0], currentCoordinate[1]);
		int pathValue = aTriangle.getNodePathValue(currentCoordinate[0], currentCoordinate[1]);
		while(0 < pathValue) {
			final int coordinateCount = aTriangle.getAdjacentNodeCoordinates(currentCoordinate[0], currentCoordinate[1], true, adjacentCoordinateArray);
			boolean foundPreviousNode = false;
			for(int ii = 0 ; !foundPreviousNode && (ii < coordinateCount * 2) ; ++ii) {
				final int adjacentRow = adjacentCoordinateArray[ii];
				final int adjacentNode = adjacentCoordinateArray[++ii];
				final int adjacentNodeValue = aTriangle.getNodeValue(adjacentRow, adjacentNode);
				final int adjacentPathValue = aTriangle.getNodePathValue(adjacentRow, adjacentNode);
				if(pathValue - adjacentNodeValue == adjacentPathValue) {
					foundPreviousNode = true;
					currentCoordinate[0] = adjacentRow;
					currentCoordinate[1] = adjacentNode;
					pathValue = adjacentPathValue;
					iNeighbourNodeCoordinateIntArray[pathNodeCount++] = aTriangle.getNodeValue(adjacentRow, adjacentNode);
				}
			}
		}
		// reverse path into a new, smaller array
		final int[] result = new int[pathNodeCount];
		for(int ii = pathNodeCount ; ii > 0 ; --ii) {
			result[pathNodeCount - ii] = iNeighbourNodeCoordinateIntArray[ii-1];
		}
		return result;
	}

	/**
	 * @param aPathValue used to keep the neighbouring nodes sorted.
	 */
	private void removeFromNeighbouringNodes(Triangle aTriangle, int aRowCoordinate, int aNodeCoordinate, int aPathValue) {
		final int pathValueIndex = findIndex(aTriangle, aPathValue);
		boolean isFoundNeighbour = false;
		// look for specific neighbour, by its coordinates, from the index for a node with the correct path value
		for (int ii = pathValueIndex ; !isFoundNeighbour && (ii < iNeighbourNodeCount) && (aPathValue == aTriangle.getNodePathValue(iNeighbourRowCoordinateIntArray[ii], iNeighbourNodeCoordinateIntArray[ii])) ; ++ii) {
			if((iNeighbourRowCoordinateIntArray[ii] == aRowCoordinate) && (iNeighbourNodeCoordinateIntArray[ii] == aNodeCoordinate)) {
				isFoundNeighbour = true;
				System.arraycopy(iNeighbourRowCoordinateIntArray, ii + 1, iNeighbourRowCoordinateIntArray, ii, iNeighbourNodeCount - ii - 1);
				System.arraycopy(iNeighbourNodeCoordinateIntArray, ii + 1, iNeighbourNodeCoordinateIntArray, ii, iNeighbourNodeCount - ii - 1);
				--iNeighbourNodeCount;
			}
		}
		// if necessary, look before the same index as long as the path value is correct.
		for (int jj = pathValueIndex - 1 ; !isFoundNeighbour &&(0 <= jj) && (aPathValue == aTriangle.getNodePathValue(iNeighbourRowCoordinateIntArray[jj], iNeighbourNodeCoordinateIntArray[jj])) ; --jj) {
			if((iNeighbourRowCoordinateIntArray[jj] == aRowCoordinate) && (iNeighbourNodeCoordinateIntArray[jj] == aNodeCoordinate)) {
				isFoundNeighbour = true;
				System.arraycopy(iNeighbourRowCoordinateIntArray, jj + 1, iNeighbourRowCoordinateIntArray, jj, iNeighbourNodeCount - jj - 1);
				System.arraycopy(iNeighbourNodeCoordinateIntArray, jj + 1, iNeighbourNodeCoordinateIntArray, jj, iNeighbourNodeCount - jj - 1);
				--iNeighbourNodeCount;
			}
		}
	}

	/**
	 * @param aPathValue used to keep the neighbouring nodes sorted.
	 */
	private void insertIntoNeighbouringNodes(Triangle aTriangle, int aRowCoordinate, int aNodeCoordinate, int aPathValue) {
		final int pathValueIndex = findIndex(aTriangle, aPathValue);
		System.arraycopy(iNeighbourRowCoordinateIntArray, pathValueIndex, iNeighbourRowCoordinateIntArray, pathValueIndex +1, iNeighbourNodeCount - pathValueIndex);
		System.arraycopy(iNeighbourNodeCoordinateIntArray, pathValueIndex, iNeighbourNodeCoordinateIntArray, pathValueIndex +1, iNeighbourNodeCount - pathValueIndex);
		++iNeighbourNodeCount;
		iNeighbourRowCoordinateIntArray[pathValueIndex] = aRowCoordinate;
		iNeighbourNodeCoordinateIntArray[pathValueIndex] = aNodeCoordinate;
	}

	/**
	 * binary search for the coordinates of a neighbouring node with a given path value
	 * 
	 * @return an index in iNeighbourRowCoordinateIntArray and iNeighbourNodeCoordinateIntArray where such coordinates are
	 * or the index where we can insert them if no such coordinate exists.
	 * 
	 * */

    private int findIndex(Triangle aTriangle, int aPathValueTarget) {
		int searchAreaBeginIndex = 0;
		int searchAreaEndIndex = iNeighbourNodeCount - 1;
		int result = searchAreaBeginIndex;
		boolean isFoundValue = false;
		while (!isFoundValue && searchAreaBeginIndex <= searchAreaEndIndex) {
			result = searchAreaBeginIndex + (searchAreaEndIndex - searchAreaBeginIndex) / 2;
			int midVal = aTriangle.getNodePathValue(iNeighbourRowCoordinateIntArray[result], iNeighbourNodeCoordinateIntArray[result]);

			if (midVal < aPathValueTarget)
				searchAreaBeginIndex = result + 1;
			else if (midVal > aPathValueTarget)
				searchAreaEndIndex = result - 1;
			else
				isFoundValue = true;
		}
		if(!isFoundValue) {
			result = searchAreaBeginIndex;
		}
		return result;
    }

	/**
	 * 
	 * Extracts the unvisited neighbour with the minimal tentative path value
	 * from iNeighbourRowCoordinateIntArray and iNeighbourNodeCoordinateIntArray
	 * 
	 * @param aCoordinateIntArray where the neighbour coordinates are stored
	 */

    private void popBestNeighbourCoordinates(int[] aCoordinateIntArray) {
		aCoordinateIntArray[0] = iNeighbourRowCoordinateIntArray[0];
		aCoordinateIntArray[1] = iNeighbourNodeCoordinateIntArray[0];
		--iNeighbourNodeCount;
		System.arraycopy(iNeighbourRowCoordinateIntArray, 1, iNeighbourRowCoordinateIntArray, 0, iNeighbourNodeCount);
		System.arraycopy(iNeighbourNodeCoordinateIntArray, 1, iNeighbourNodeCoordinateIntArray, 0, iNeighbourNodeCount);
	}

}
