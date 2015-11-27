
public class GeonomicsTriangleGenerator {

	private final static int EXPECTED_ARGUMENT_COUNT = 2;

	public static void main(String[] args) {
		boolean isPrintUsage = true;
		if(EXPECTED_ARGUMENT_COUNT == args.length) {
			int rowCount = 0;
			int maxNodeValue = 0;
			try {
				rowCount = Integer.parseInt(args[0]);
				maxNodeValue = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfex) {
				nfex.printStackTrace();
			}
			if((0 < rowCount) && (0 < maxNodeValue)) {
				isPrintUsage = false;
				for(int ii = 0 ; ii < rowCount ; ++ii) {
					for (int jj = 0 ; jj < ii ; ++jj) {
						System.out.print(Math.round(Math.random() * maxNodeValue));
						System.out.print(" ");
					}
					System.out.println(Math.round(Math.random() * maxNodeValue));
				}
			}
		}
		if (isPrintUsage) {
			System.out.println("Usage: java GeonomicsTriangleGenerator <rowCount> <maxNodeValue>");
			System.out.println("<rowCount> mandatory, strictly positive integer");
			System.out.println("<maxNodeValue> mandatory, strictly positive integer");
		}
	}

}
