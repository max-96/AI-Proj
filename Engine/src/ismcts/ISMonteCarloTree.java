package ismcts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import AI.AIGameState;
import MCTS.MCNode;

public class ISMonteCarloTree
{
	ISMCNode root;
	Random r;

	public ISMonteCarloTree(InformationSet infoset)
	{
		root = new ISMCNode(null, null, infoset, this);
		r = new Random();
	}

	public Integer execute(BlockingQueue<List<List<Integer>>> sols, int iterations)
	{

		for (int it = 0; it < iterations; it++)
		{
			// getdeterminization
			List<List<Integer>> det = Collections.emptyList();
			try
			{
				det = sols.take();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			if (det == Collections.EMPTY_LIST)
				break;

			// Select
			// Expand
			ISMCNode node = traverseAndExpand(det);
			// Simulate
			double outcome = node.playout(det);
			// Backtrack
			node.backpropagateStats(outcome > 0);
		}

		return root.getBestMove();
	}

	private ISMCNode traverseAndExpand(List<List<Integer>> det)
	{

		ISMCNode node = root;

		while (!node.isLeaf)
		{
			node.generateChildren(det);
			ISMCNode n = null;
			double bestPri = -1;

			for (ISMCNode i : node.children)
				if (i.getPriority() > bestPri && i.isCompatible(det))
				{
					n = i;
					bestPri = i.getPriority();
				}
			node = n;
		}

		if (node.isTerminal())
			return node;

		node.generateChildren(det);
		int i = r.nextInt(node.children.size());
		return node.children.get(i);
	}
}
