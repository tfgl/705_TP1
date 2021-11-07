package calculator.generic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Cette classe est l'implementation d'un moteur de calcul pour expressions
 * simple. La technique de programmation utilisee est un arbre binaire de calcul
 * qui est parcouru recursivement pour obtenir le resultat de l'expression
 * numerique.
 */

public class MoteurCalculSimple extends Thread implements IMoteurCalcul {
    Socket s;
    DataInputStream dis;
    DataOutputStream dout;

    public MoteurCalculSimple( Socket s ) {
        this.s = s;

        try {
            dis = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

	/**
	 * Methode statique appelee recursivement pour creer les noeuds de l'arbre.
	 * 
	 * @param expression
	 *            l'expression numerique a evaluer
	 * @return le noeud cree
	 * @exception ExpressionInvalide
	 *                si la syntaxe de l'expression est incorrecte
	 */

	private static Cellule creerCellule(String expression)
			throws ExpressionInvalide {
		Cellule temp = null;

		try {
			// suppression des parentheses exterieures
			expression = supprimerParentheses(expression);
			// recherche de l'operateur de plus basse priorite
			int pos = rechercherOperateur(expression);
			// creation d'un noeud contenant l'operateur
			temp = new Cellule(Cellule.OPER, new Character(expression
					.charAt(pos)));
			// appel recursif pour evaluer les deux operandes
			temp.gauche = creerCellule(expression.substring(0, pos));
			temp.droite = creerCellule(expression.substring(pos + 1));
		}

		// operateur non trouve ou mal place
		catch (StringIndexOutOfBoundsException e) {
			try {
				// on considere les caracteres restants comme un nombre
				temp = new Cellule(Cellule.NBR, new Double(expression));
			} catch (NumberFormatException ex) {
				// l'expression est invalide en cas d'echec
				throw new ExpressionInvalide();
			}
		}
		return temp;
	}

	/**
	 * Methode statique supprimant les parentheses exterieures inutiles.
	 * 
	 * @param expression
	 *            l'expression numerique a evaluer
	 * @return l'expression numerique epuree
	 * @exception ExpressionInvalide
	 *                si le parenthesage est incorrect
	 */

	private static String supprimerParentheses(String expression)
			throws ExpressionInvalide {
		int i;
		boolean b = true;
		int ouvre;

		// tant que l'expression commence et finit par des parentheses
		// on cherche a savoir si la derniere parentheses est
		// la parenthese fermante de la premiere

		while (expression.startsWith("(") && expression.endsWith(")") && b) {
			ouvre = 1;
			i = 1;
			while (i < expression.length() && ouvre > 0) {
				if (expression.charAt(i) == '(')
					ouvre++;
				if (expression.charAt(i) == ')')
					ouvre--;
				i++;
			}
			if (ouvre != 0)
				throw new ExpressionInvalide();
			else if (i == expression.length())
				expression = expression.substring(1, expression.length() - 1);
			else
				b = false;
		}
		return expression;
	}

	/**
	 * Methode statique supprimant tous les espaces.
	 * 
	 * @param expression
	 *            l'expression numerique a evaluer
	 * @return l'expression numerique epuree
	 */

	private static String supprimerEspaces(String expression) {
		if (expression != null && expression.indexOf(' ') > -1) {
			StringBuffer temp = new StringBuffer();

			for (int i = 0; i < expression.length(); i++)
				if (expression.charAt(i) != ' ')
					temp.append(expression.charAt(i));
			return (temp.toString());
		} else
			return expression;
	}

	/**
	 * Methode retournant la position de l'operateur de plus basse priorite.
	 * 
	 * @param expression
	 *            l'expression numerique a evaluer
	 * @return la position de l'operateur de plus basse priorite ou -1 si aucun
	 *         operateur n'a ete trouve
	 */

	private static int rechercherOperateur(String expression) {

		// la priorite de + et - est 10
		// la priorite de * et / est 20
		// la priorite initiale est 100 (aucun operateur trouve)
		int priorite = 100;
		// nombre de parentheses ouvrantes et fermantes rencontrees
		int ouvre = 0, ferme = 0;
		// indice courant et indice de l'operateur de plus basse priorite
		int i = 0, indice = -1;
		// caractere courant
		char c;
		// dans le cas ou on ne pourra trouver mieux, trouve = true
		boolean trouve = false;

		while (i < expression.length() && trouve == false) {
			c = expression.charAt(i);
			switch (c) {
			case '(':
				ouvre++;
				break;
			case ')':
				ferme++;
				break;
			case '+':
			case '-':
				if (ouvre == ferme && priorite > 10) {
					// on a trouve le premier operateur + ou -
					// en dehors de toute paranthese
					priorite = 10;
					indice = i;
					trouve = true;
				}
				break;
			case '*':
			case '/':
				if (ouvre == ferme && priorite > 20) {
					// on a trouve le premier operateur * ou /
					// en dehors de toute paranthese alors
					// qu'aucun operateur + ou - n'a ete trouve
					priorite = 20;
					indice = i;
				}
				break;
			}
			i++;
		}
		return indice;
	}

	/**
	 * programme principal de test
	 * 
	 * @param arg
	 *            ligne de commance (non utilise)
	 */

	public static void main(String[] arg) {
		try {
            ServerSocket ss = new ServerSocket(54000);
            while(true) {
                Socket s = ss.accept();
                MoteurCalculSimple m = new MoteurCalculSimple( s );
                m.start();
            }

		} catch (Exception e) {
            System.out.println(e);
			System.exit(2);
		}

	}

    public void run() {
        String expression;

        try {
            expression = (String)dis.readUTF();
            dout.writeUTF( expression + " = " + this.calculer(expression) );
            dout.flush();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

	/**
	 * Methode retournant le resultat d'une expression arithmetique.
	 * 
	 * @param expression
	 *            l'expression numerique a evaluer
	 * @return la valeur numerique de l'expression
	 * @exception ExpressionInvalide
	 *                si la syntaxe de l'expression est incorrecte
	 */
	public Double calculer(String expression) throws ExpressionInvalide {
		Cellule arbre = creerCellule(supprimerEspaces(expression));
		double resultat = arbre.calcul();
		return new Double(resultat);
	}

}

/**
 * La classe <code>Cellule</code> represente un noeud de l'arbre binaire. Elle
 * peut contenir un operateur ou une constante.
 * 
 */

class Cellule extends Object {

	/**
	 * Reference du fils a gauche
	 */
	Cellule gauche;

	/**
	 * Reference du fils a droite
	 */
	Cellule droite;

	/**
	 * Type de cellule : operateur ou nombre
	 * 
	 * @see Cellule.OPER
	 * @see Cellule.NBR
	 */
	int type;

	/**
	 * Constante indiquant que la cellule contient un nombre
	 */
	static final int NBR = 1;

	/**
	 * Constante indiquant que la cellule contient un operateur
	 */
	static final int OPER = 2;

	/**
	 * Contenu de la cellule (operateur ou nombre)
	 */
	Object info;

	/**
	 * Constructeur a partir du type et du contenu
	 * 
	 * @param type
	 *            type de la cellule
	 * @param info
	 *            contenu de la cellule
	 */

	Cellule(int type, Object info) {
		this.type = type;
		this.info = info;
	}

	/**
	 * Evaluation recursive de l'expression stockee dans l'arbre
	 * 
	 * @return la valeur de l'expression
	 * @throws ExpressionInvalide
	 *             si la syntaxe de l'expression est incorrecte
	 */

	double calcul() throws ExpressionInvalide {
		try {
			if (type == NBR) {
				return ((Double) info).doubleValue();
			} else {
				Character C = (Character) info;
				switch (C.charValue()) {
				case '+':
					return gauche.calcul() + droite.calcul();
				case '-':
					return gauche.calcul() - droite.calcul();
				case '*':
					return gauche.calcul() * droite.calcul();
				case '/':
					return gauche.calcul() / droite.calcul();
				default:
					throw new ExpressionInvalide();
				}
			}
		} catch (Exception e) {
			throw new ExpressionInvalide(e);
		}
	}

}
