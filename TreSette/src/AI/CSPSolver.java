package AI;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Solver ad hoc per generare le possibili combinazioni di stati possibili
 * 
 * @author Administrator
 *
 */
public class CSPSolver {

	public BlockingQueue<HashMap<Integer, Integer>> soluzioni;
	private int maxStati = 10;

	// carte non assegnate
	private LinkedList<Integer> carte = new LinkedList<>();
	// domini di tali carte
	private HashMap<Integer, LinkedList<Integer>> domini;

	// assegnamento delle carte sicure
	private HashMap<Integer, Integer> asseg = new HashMap<Integer, Integer>();
	private int[] rCards = new int[4];
	private boolean[][] piombi = new boolean[4][4];

	public CSPSolver(int player, Set<Integer> Ex, List<Integer> pCards, int[] rCards, int maxStati) {

		soluzioni = new ArrayBlockingQueue<HashMap<Integer, Integer>>(maxStati);
		this.maxStati = maxStati;
		for (int i : pCards)
			asseg.put(i, player);

		// se avviene un assegnamento e un rcards va a 0
		LinkedList<Integer> toCheck = new LinkedList<>();

		for (int c = 0; c < 40; c++) {
			if (Ex.contains(c) || pCards.contains(c))
				continue;

			carte.add(c);
			LinkedList<Integer> dom = new LinkedList<>();

			for (int p = 0; p < 4; p++) {
				// se p � il nostro player, non ha pi� carte o ha avuto un piombo
				// per questo seme, ignora
				if (p == player || rCards[p] <= 0 || piombi[p][c / 10])
					continue;

				// altrimenti aggiungi al dominio

				dom.add(p);
			}

			// Se il dominio di c � solo un player, lo assegna
			if (dom.size() == 1) {
				int p = dom.get(0);
				// toglie da carte, diminuisce rCards, assegna
				carte.remove(c);
				rCards[p] = rCards[p] - 1;
				asseg.put(c, p);

				assert rCards[p] >= 0;

				// ricontrollare la consistenza!
				if (rCards[p] == 0)
					toCheck.add(p);

			} else {
				// aggiunge il dominio
				Collections.shuffle(dom);
				domini.put(c, dom);
			}

		}

		while (!toCheck.isEmpty()) {
			int p = toCheck.pop();
			// per ogni dominio controlliamo che non sia presente p
			for (Entry<Integer, LinkedList<Integer>> k : domini.entrySet()) {
				if (k.getValue().contains(p)) {

					// Se lo contiene lo dobbiamo togliere
					LinkedList<Integer> dom = k.getValue();
					dom.remove((Integer) p);
					assert dom.size() > 0;
					// Ora controlliamo se � diventato un assegnamento
					//
					if (dom.size() == 1) {
						int pToCheck = dom.getFirst();
						int carta = k.getKey();
						carte.remove(carta);
						domini.remove(carta);
						asseg.put(carta, pToCheck);
						rCards[pToCheck] = rCards[pToCheck] - 1;

						assert rCards[pToCheck] >= 0;

						// non ha pi� assegnamenti possibili e va controllato
						if (rCards[pToCheck] == 0)
							toCheck.add(pToCheck);

					}
				}

			}

		} // ENDWHILE

	} // ENDCOSTR

	public void produce() {

	}

	private void prod_rec2(HashMap<Integer, Integer> ass, HashMap<Integer, LinkedList<Integer>> doms,
			List<Integer> carte, int[] rimCarte, Random r, int carta, int player) {

		carte.remove((Integer) carta);
		ass.put(carta, player);
		doms.remove(carta);
		rimCarte[player] = rimCarte[player] - 1;
		assert rimCarte[player] >= 0;

		if (rimCarte[player] == 0) {
			// posso toglierla dagli altri domini e inferire
			LinkedList<Integer> toCheck = new LinkedList<>();
			toCheck.add(player);

			// START WHILE

			while (!toCheck.isEmpty()) {
				int p = toCheck.pop();
				// per ogni dominio controlliamo che non sia presente p
				for (Entry<Integer, LinkedList<Integer>> k : doms.entrySet()) {
					if (k.getValue().contains(p)) {

						// Se lo contiene lo dobbiamo togliere
						LinkedList<Integer> dom = k.getValue();
						dom.remove((Integer) p);
						assert dom.size() > 0;
						// Ora controlliamo se � diventato un assegnamento
						//
						if (dom.size() == 1) {
							int pToCheck = dom.getFirst();
							int cartaa = k.getKey();
							carte.remove(cartaa);
							doms.remove(cartaa);
							ass.put(cartaa, pToCheck);
							rimCarte[pToCheck] = rimCarte[pToCheck] - 1;

							assert rimCarte[pToCheck] >= 0;

							// non ha pi� assegnamenti possibili e va controllato
							if (rimCarte[pToCheck] == 0)
								toCheck.add(pToCheck);
						}
					}

				}

			}
			// ENDWHILE
			// inferenza finita
			if (carte.size() == 0) {
				// finito, carico la cosa nella lista bloccante
				//TODO
			} else {
				Collections.shuffle(carte);
				//altrimenti genero tutti i successori
				for (int c : carte) {
					Collections.shuffle(doms.get(c));
					for (int p : doms.get(c)) {
						//TODO controllare che non siano shallowcopies 
						prod_rec2(new HashMap<>(ass), new HashMap<>(doms), new LinkedList<>(carte), Arrays.copyOf(rimCarte, 4), r, c, p);
					}
				}
			}

		}

	}

	private void prod_rec(HashMap<Integer, Integer> ass, HashMap<Integer, LinkedList<Integer>> doms,
			List<Integer> carte, int[] rimCarte, Random r) {
		// mischia le carte rimanenti
		Collections.shuffle(carte);

		int carta = carte.remove(0);
		LinkedList<Integer> domCarta = doms.get(carta);

		int player = domCarta.get(r.nextInt(domCarta.size()));

		ass.put(carta, player);
		doms.remove(carta);
		rimCarte[player] = rimCarte[player] - 1;
		assert rimCarte[player] >= 0;

		if (rimCarte[player] == 0) {
			// posso toglierla dagli altri domini e inferire
			LinkedList<Integer> toCheck = new LinkedList<>();
			toCheck.add(player);

			// START WHILE

			while (!toCheck.isEmpty()) {
				int p = toCheck.pop();
				// per ogni dominio controlliamo che non sia presente p
				for (Entry<Integer, LinkedList<Integer>> k : doms.entrySet()) {
					if (k.getValue().contains(p)) {

						// Se lo contiene lo dobbiamo togliere
						LinkedList<Integer> dom = k.getValue();
						dom.remove((Integer) p);
						assert dom.size() > 0;
						// Ora controlliamo se � diventato un assegnamento
						//
						if (dom.size() == 1) {
							int pToCheck = dom.getFirst();
							int cartaa = k.getKey();
							carte.remove(cartaa);
							doms.remove(cartaa);
							ass.put(cartaa, pToCheck);
							rimCarte[pToCheck] = rimCarte[pToCheck] - 1;

							assert rimCarte[pToCheck] >= 0;

							// non ha pi� assegnamenti possibili e va controllato
							if (rimCarte[pToCheck] == 0)
								toCheck.add(pToCheck);
						}
					}

				}

			}

			// END WHILE

		}

	}

} // ENDCLASS
