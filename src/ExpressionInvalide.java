package calculator.generic;

/**
 * La classe <code>ExpressionInvalide</code> est levee lorsque l'expression a
 * evaluer n'est pas correcte du point de vue syntaxique.
 */
public class ExpressionInvalide extends Exception {

	public ExpressionInvalide() {
	}

	public ExpressionInvalide(Exception cause) {
		super(cause);
	}

	public ExpressionInvalide(String message) {
		super(message);
	}
}
