package ismcts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import AI.AIGameState;
import MCTS.MCNode;
import MCTS.MonteCarloTree;

public class ISMCNode implements Comparable<ISMCNode>
{

	private InformationSet infoset;
	public static final double C_PARAM = 1.25;
	public static final double EPS = 1e-8;
	private ISMCNode parent;
	private final Integer generatingAction;
	private final ISMonteCarloTree tree;
	protected boolean isLeaf;

	protected List<ISMCNode> children = Collections.emptyList();

	private int winCount = 0;
	private int visitCount = 0;
	private boolean isBlackNode;

	public ISMCNode(ISMCNode parent, Integer generatingAction, InformationSet infoset, ISMonteCarloTree tree)
	{
		this.parent = parent;
		this.generatingAction = generatingAction;
		this.tree = tree;
		isLeaf = true;
		this.infoset = infoset;
		isBlackNode = !infoset.maxNode;
	}

	public ISMCNode(ISMCNode parent, Integer generatingAction, ISMonteCarloTree tree)
	{
		this.parent = parent;
		this.generatingAction = generatingAction;
		this.tree = tree;
		isLeaf = true;
	}

	public void init()
	{
		if (infoset == null)
		{
			infoset = parent.infoset.genSuccessor(generatingAction);
			isBlackNode = !infoset.maxNode;
		}
	}

	public void generateChildren(List<List<Integer>> determin)
	{
		if (!children.isEmpty() || infoset.terminal)
			return;

		List<Integer> mosse = infoset.generateActions(determin);

		if(children == Collections.EMPTY_LIST)
			children = new ArrayList<>();
		
		
		for (Integer gaction : mosse)
		{
			ISMCNode child = new ISMCNode(this, gaction, tree);
			if (!children.contains(child))
				children.add(child);
		}
		isLeaf = false;
	}

	protected double playout(List<List<Integer>> determin)
	{
		init();
		InformationSet is = infoset;
		while (!is.terminal)
		{
			Integer mossa = is.genRandMossa(determin);
			is = is.genSuccessor(mossa);
		}
		return is.getScoreSoFar();
	}

	public double getPriority()
	{
		assert parent.visitCount > 0;
		return ((double) winCount) / (visitCount + EPS)
				+ C_PARAM * Math.sqrt(Math.log(parent.visitCount) / (visitCount + EPS));
	}

	protected void backpropagateStats(boolean isWin)
	{
		ISMCNode node = this;
		while (node != null)
		{
			if (node.isBlackNode)
			{
				if (isWin)
					node.winCount += 1;
			} else
			{
				if (!isWin)
					node.winCount += 1;
			}
			node.visitCount += 1;
			node = node.parent;
		}
	}

	public boolean isCompatible(List<List<Integer>> determ)
	{
		return infoset.isCompatible(determ);
		// return
		// determ.get(parent.infoset.getCurrentPlayer()).contains(generatingAction);
	}

	@Override
	public int compareTo(ISMCNode other)
	{
		return (int) Math.signum(getPriority() - other.getPriority());
	}

	public Integer getBestMove()
	{

		int maxVisite = 0;
		Integer bestAction = -1;
		for (ISMCNode c : children)
		{
			if (c.visitCount > maxVisite)
			{
				maxVisite = c.visitCount;
				bestAction = c.generatingAction;
			}
		}
		return bestAction;
	}

	public boolean isTerminal()
	{
		return infoset.terminal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((generatingAction == null) ? 0 : generatingAction.hashCode());
		result = prime * result + ((infoset == null) ? 0 : infoset.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ISMCNode other = (ISMCNode) obj;
		if (generatingAction == null)
		{
			if (other.generatingAction != null)
				return false;
		} else if (!generatingAction.equals(other.generatingAction))
			return false;
		if (infoset == null)
		{
			if (other.infoset != null)
				return false;
		} else if (!infoset.equals(other.infoset))
			return false;
		return true;
	}

}
