package calculator.generic;


/**
 * Interface dcrivant un moteur de calcul d'expressions arithmtiques simples.
 * 
 */
public interface IMoteurCalcul {

	/**
	 * Methode retournant le resultat d'une expression arithmetique.
	 * 
	 * @param expression
	 *            l'expression numerique a evaluer
	 * @return la valeur numerique de l'expression
	 * @exception ExpressionInvalide
	 *                si la syntaxe de l'expression est incorrecte
	 */
	public abstract Double calculer(String text) throws ExpressionInvalide;
}
