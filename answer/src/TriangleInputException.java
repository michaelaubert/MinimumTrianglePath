
import java.io.IOException;

final class TriangleInputException extends IOException {

	// don't care about TriangleInputException implementing Serializable.
	//private static final long serialVersionUID = 1L;

	/*package*/ final static String BAD_SPACING = "Illegal Input. Line has unexpected number of spaces. Row number = %d";
	/*package*/ final static String BAD_LINE = "Illegal Input. Faulty line reading. Row number =  %d";
	/*package*/ final static String BAD_NUMBER = "Illegal Input. Node value is not a number. Row number = %d, Node number = %d";


	TriangleInputException(String anErrorMessage, int aLocationIndicator) {
		super(String.format(anErrorMessage, aLocationIndicator));
	}

	TriangleInputException(String anErrorMessage, int aFirstLocationIndicator, int aSecondLocationIndicator) {
		super(String.format(anErrorMessage, aFirstLocationIndicator, aSecondLocationIndicator));
	}

}
