
final class PathOutputProcessor {
	
	private final static String OUTPUT_PREFIX = "Minimal path is: ";
	private final static String OUTPUT_NODE_SEPARATOR = " + ";
	private final static String OUTPUT_PATH_VALUE_SEPARATOR = " = ";
	
	/*package*/ void processPath(int[] aPath) {
		if (null != aPath && (0 < aPath.length)) {
			int pathValue = 0;
			System.out.print(OUTPUT_PREFIX);
			for(int ii = 0 ; ii < aPath.length - 1 ; ++ii) {
				pathValue += aPath[ii];
				System.out.print(aPath[ii]);
				System.out.print(OUTPUT_NODE_SEPARATOR);
			}
			pathValue += aPath[aPath.length-1];
			System.out.print(aPath[aPath.length-1]);
			System.out.print(OUTPUT_PATH_VALUE_SEPARATOR);
			System.out.println(pathValue);
		}
	}

}
