/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over non-negative integers.
 */
//jonathanz1 204331375
//ornechemia 316295161
public class FibonacciHeap {
	private HeapNode min; // points to the minimum node of the heap, which is a root
	private int size; // number of nodes in the heap
	private static int totalLinks;
	private static int totalCuts;
	private int numMarked; // number of marked nodes in the heap

	/**
	 * public boolean empty()
	 *
	 * precondition: none The method returns true if and only if the heap is empty.
	 */
	public boolean empty() {
		return (this.min == null);
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts
	 * it into the heap.
	 */
	public HeapNode insert(int key) {
		HeapNode x = new HeapNode(key);
		if (this.empty()) {
			this.min = x;
		} else {
			x.left = this.min;
			x.right = this.min.right;
			this.min.right = x;
			x.right.left = x;

			if (key < this.min.key) {
				this.min = x;
			}
		}
		this.size++;
		return x;
	}

	/**
	 * public void deleteMin()
	 *
	 * Delete the node containing the minimum key.
	 *
	 */
	public void deleteMin() {
		if (this.size == 1) { //the minimum is the only node in the heap
			this.min = null;
			this.size = 0;
			return;
		}
		moveChildrenUp(this.min); // adds the child list of the minimal node to the list of roots
		HeapNode temp = this.min;
		removeRoot(this.min, true); // deletes the minimal node
		this.min = temp.right; // changed the min pointer, although it's not updated at this stage
		consolidate(); // linkes the roots until there are no roots of the same rank
	}

	/**
	 * linkes the roots until there are no roots of the same rank and the minimum is
	 * updated
	 */
	private void consolidate() {
		int maxRank = this.getMaxRank();
		// the array will assist by keeping a pointer from the i'th cell to a root with
		// rank i
		HeapNode[] rankArr = new HeapNode[maxRank];

		// makes sure that the iterations will stop after the last root that haven't
		// been checked
		boolean isFinalIter = false;

		HeapNode temp = this.min;
		HeapNode finalNode = this.min.left; // the last root we need to check
		HeapNode nextTemp = this.min;
		while (!isFinalIter) {
			// indicates we are at the last root we need to check,
			// therefore this is the last iteration of the loop.
			if (temp == finalNode) {
				isFinalIter = true;
			}
			// nextTemp keeps the next root we will check, so it won't get lost after the
			// iterations on temp
			nextTemp = temp.right;
			int r = temp.rank;

			// if the r'th cell is not empty, we will link the current root with the subtree
			// of this cell
			// and keep the linked subtree in temp. then, temp rank will become r+1
			while (rankArr[r] != null && r < rankArr.length) {
				temp = link(temp, rankArr[r]);
				rankArr[r] = null;
				r++; // temp rank will become r+1
			}
			rankArr[r] = temp;
			temp = nextTemp;
		}
		getNewMin(); // updates the minimum to be accurate
		return;
	}

	/**
	 * separate root from it's children and add them to the root list of this heap
	 * @pre root is in the root list of this heap
	 * @param root - the root that it's children will be separated
	 */
	private void moveChildrenUp(HeapNode root) {
		if (root.childListHead == null) { // no children to move up
			return;
		}
		HeapNode temp = root.childListHead;

		// the children list will be added between this.min and term1
		HeapNode term1 = this.min.right;
		// the children list will be added as linear list,
		// as if temp is the first node of this list and term2 is the last one.
		HeapNode term2 = temp.left;

		// cancels the connections between root and all the nodes in child list
		disconnectParent(root.childListHead);

		// adds the children, as a list, from child list to roots list
		this.min.right = temp;
		temp.left = this.min;
		term2.right = term1;
		term1.left = term2;
		return;
	}

	/**
	 * cancels the connections between the parent of childListHead to it's children,
	 * and updates every child's properties so it can become an accurate root (no
	 * parent)
	 * 
	 * @pre childListHead is not null and it has a parent
	 * @param childListHead - the list of children we disconnect
	 */
	private void disconnectParent(HeapNode childListHead) {
		HeapNode temp = childListHead;
		if (temp != null) { // updates the parent of childListHead to have no children
			childListHead.parent.childListHead = null;
		}
		HeapNode finalNode = temp.left;// the last child to handle
		// makes sure that the iterations will stop after the last root that haven't
		// been checked
		boolean isFinalIter = false;

		while (!isFinalIter) {
			// indicates we are at the last child we need to check,
			// therefore this is the last iteration of the loop.
			if (temp == finalNode) {
				isFinalIter = true;
			}
			temp.parent = null; // disconnect child from it's parent
			temp = temp.right; // move to the next child
		}
		return;
	}

	/**
	 * if rootDeleted is true, we delete the root from the heap. otherwise, we remove root
	 * from the list of roots, but the size of the heap stays as if the root and it's children
	 * are still a part of the heap. Therefore - the method that called removeRoot should keep a
	 * pointer to root.
	 * 
	 * @pre root is a root in this heap. if rootDeleted is true, root has no children.
	 *  
	 * @param root - the root to be removed from the root list of the heap.
	 * @param rootDeleted - true value indicates that we delete the root from the heap.
	 * otherwise, we remove root from the list of roots, but assume it will be meld into
	 * other root's subtree
	 *
	 */
	private void removeRoot(HeapNode root, boolean rootDeleted) {
		if (rootDeleted) { //we delete the root permanently.
			this.size--;
		}
		if (this.min == root) {
			this.min = root.right; // the minimum is not updated yet
		}
		//connect the left&right siblings of root together directly, 
		//so the connection to root is removed.
		root.left.right = root.right;
		root.right.left = root.left;
	}
	/**
	 * @pre node1, node2 are both roots belong to this heap. 
	 * @param node1 - first root and it's subtree
	 * @param node2 - second root and it's subtree
	 * 
	 * adds the root with the larger key (with all it's children) as a child to the other root.
	 * @return the new subtree after linking
	 */
	private HeapNode link(HeapNode node1, HeapNode node2) {
		//make sure node1 is pointing to the node with the smaller key
		if (node1.key > node2.key) { 
			HeapNode temp = node1;
			node1 = node2;
			node2 = temp;
		}
		//remove the root with the larger key from the root list it's belong to
		removeRoot(node2, false); 
		//add the root with the larger key as a child to the other root, with all it's children
		addChild(node1, node2);

		FibonacciHeap.totalLinks++; //link was made
		return node1;
	}
	/**adds child into the child list of parent.
	 * 
	 * @pre child and parent are two roots in the same heap
	 * @param parent - the node we add child to it's childen list
	 * @param child - the node we add as a child to parent
	 */
	private void addChild(HeapNode parent, HeapNode child) {
		parent.rank++; //parent's rank is increased as we add him a child
		
		//put child in it's new parent child list
		child.parent = parent;
		if (parent.childListHead == null) {//if parent has no children yet
			parent.childListHead = child;
			child.right = child;
			child.left = child;
			return;
		}
		//if parent has children in child list, we add child into the existing list
		HeapNode ch = parent.childListHead;
		HeapNode temp = ch.left;
		temp.right = child;
		ch.left = child;
		child.right = ch;
		child.left = temp;
		return;
	}
	/**
	 * Update the the minimum of the heap to point to the real minimum.
	 * @pre $prev min points to a node in the root list of the heap
	 */
	private void getNewMin() {
		HeapNode temp = this.min;
		HeapNode finalNode = this.min.left;
		boolean isFinalIter = false; // indicates if this is the last iteration of the loop
		
		//check all the roots' values to find the new minimum
		while (!isFinalIter) {
			if (temp == finalNode) {
				//temp is the last node we havn't check, 
				//so this is the last iteration of the loop
				isFinalIter = true; 
			}
			if (this.min.key > temp.key) {
				this.min = temp;
			}
			temp = temp.right;
		}
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Return the node of the heap whose key is minimal.
	 */
	public HeapNode findMin() {
		return this.min;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2) {
		if (heap2.empty()) { //there are no nodes to add to this heap
			return;
		}
		//updates the properties of this heap
		this.numMarked += heap2.numMarked;
		this.size += heap2.size;
		if (this.empty()) { //is this heap is empty, it gets heap2 properties
			this.min = heap2.min;
			this.size = heap2.size;
			return;
		}
		//add heap2's root list to this heap root list
		HeapNode term1 = this.min.right;
		HeapNode term2 = heap2.min.left;

		this.min.right = heap2.min;
		heap2.min.left = this.min;
		term2.right = term1;
		term1.left = term2;
		
		//updates the minimum if necessary 
		if (this.min.key > heap2.min.key) {
			this.min = heap2.min;
		}
		return;
	}

	/**
	 * public int size()
	 *
	 * Return the number of elements in the heap
	 */
	public int size() {
		return this.size;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return a counters array, where the value of the i-th entry is the number of
	 * trees of order i in the heap.
	 * 
	 */
	public int[] countersRep() {
		if (this.empty()) { //returns empty array if the heap is empty
			return new int[0];
		}
		int maxRank = getMaxRank();
		int[] arr = new int[maxRank];//build an array the size of this heap maximum rank
		HeapNode temp = this.min;
		//goes over all the root list of the heap and counts the ranks of the roots
		do {
			arr[temp.rank]++;
			temp = temp.right;
		} while (temp != this.min);

		return arr;
	}
	/**
	 * a maximum rank of a root in fibonacci heap is calculated as if all the nodes of the heap 
	 * are in one subtree. let n be the number of nodes in the heap, so the maximum rank is log(n).
	 *  
	 * @return the maximum possible rank of a root in this heap
	 */
	private int getMaxRank() {
		return (1 + (int) Math.floor(Math.log(this.size) / Math.log(2)));
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap.
	 *
	 */
	public void delete(HeapNode x) {
		//the node to be deleted gets a value of -1. Therefore it becomes the minimum 
		// (the keys of the heap are non-negative)
		decreaseKey(x, x.key + 1);  
		deleteMin(); // the minimum, which is x, is deleted.
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * The function decreases the key of the node x by delta. The structure of the
	 * heap should be updated to reflect this chage (for example, the cascading cuts
	 * procedure should be applied if needed).
	 */
	public void decreaseKey(HeapNode x, int delta) {
		if (delta < 0) { // delta<0 is not decreasing
			System.out.println("Invalid delta!");
			return;
		}
		x.key -= delta; //update x's key

		if (x.parent != null && x.parent.key > x.key) {
			//if x has a parent and the parent's key is larger than x's key, we cut x from
			//it's parent by move the problem up.
			cascadingCuts(x);  
		}
		// x is now a root, so we update the minimum if necessary
		if (this.min.key > x.key) {
			this.min = x;
		}
	}
	/**
	 * Recursive method that will cut node from it's parent if necessary and move the problem up,
	 * so there will be cuts until there is an unmarked parent, that we mark and end the iterations.
	 * @param node - the node to be cut from it's parent/
	 */
	private void cascadingCuts(HeapNode node) {
		HeapNode parent = node.parent; //save a pointer to node's parent, before the cutting
		cut(node); //cut node from it's parent and add it to the root list of the heap
		
		//node's parent is not a root (if it is, no need to move the problem upwards)
		if (parent.parent != null) { 
			if (!parent.mark) { //if the parent is unmarked, mark it and end the iterations
				mark(parent);
			} else { 
				//if the parent is marked, we need to cut is from it's parent,
				//move the problem upwards
				cascadingCuts(parent); 
			}
		}
	}
	/**
	 * Cut node from it's parent and add it to the root list of the heap.
	 * @param node - node to be cut.
	 */
	private void cut(HeapNode node) {
		HeapNode parent = node.parent;
		FibonacciHeap.totalCuts++; //cut has been made
		node.parent = null;
		if (node.mark) { //node becomes a root, therefore it's unmarked
			unmark(node);
		}
		parent.rank--; //node's parent has one less child
		if (node.right == node) { //if node was his parent's only child
			parent.childListHead = null;
		} else { //disconnect node from it's siblings
			parent.childListHead = node.right;
			node.left.right = node.right;
			node.right.left = node.left;
		}
		//add node to root list
		HeapNode temp = this.min.right;
		this.min.right = node;
		node.left = this.min;
		node.right = temp;
		temp.left = node;
		
		return;
	}

	/**
	 * @pre node unmarked
	 * @param node - node to be marked
	 */
	private void mark(HeapNode node) {
		node.mark = true;
		this.numMarked++;
	}
	/**
	 * @pre node is marked
	 * @param node - node to be unmarked
	 */
	private void unmark(HeapNode node) {
		node.mark = false;
		this.numMarked--;
	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is: Potential
	 * = #trees + 2*#marked The potential equals to the number of trees in the heap
	 * plus twice the number of marked nodes in the heap.
	 */
	public int potential() {
		return numOfTrees() + 2 * this.numMarked;
	}
	/**
	 * @return the number of trees in this heap
	 */
	private int numOfTrees() {
		if (this.empty()) {
			return 0;
		}
		//starts from the minimum (which we have a pointer to), 
		//and goes over all the root list until it gets back to the minimum,
		//that we started counting from (the root list is circular).
		HeapNode temp = this.min; 
		int numOfTrees = 1;
		while (temp.right != this.min) {
			numOfTrees++; //counts every root in the circular list
			temp = temp.right;
		}
		return numOfTrees;
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during
	 * the run-time of the program. A link operation is the operation which gets as
	 * input two trees of the same rank, and generates a tree of rank bigger by one,
	 * by hanging the tree which has larger value in its root on the tree which has
	 * smaller value in its root.
	 */
	public static int totalLinks() {
		return FibonacciHeap.totalLinks;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during
	 * the run-time of the program. A cut operation is the operation which
	 * diconnects a subtree from its parent (during decreaseKey/delete methods).
	 */
	public static int totalCuts() {
		return FibonacciHeap.totalCuts;
	}

	/*// a method that helped us in the measures
	 * public void resetCounts() { this.totalCuts = 0; this.totalLinks = 0; }
	 */

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap (for example
	 * HeapNode), do it in this file, not in another file
	 * 
	 */
	public class HeapNode {
		public int key;
		private int rank;
		private boolean mark;
		private HeapNode parent;
		//the child list is a circular list, so we need a pointer only to one of the children
		private HeapNode childListHead;
		
		//pointers to the siblings of this node 
		//(the sibling list is circular, and its in fact the child list of the node's parent)
		private HeapNode left;
		private HeapNode right;
		/**
		 * constructor that creates a new node with key = key
		 * @param key - the key of the new node created
		 */
		public HeapNode(int key) { 
			this.key = key;
			
			//updates all the properties of a single node that has:
			//no children, no reason to be marked, no parent and therefore no siblings
			this.mark = false;
			this.rank = 0;
			this.childListHead = null;
			
			//the sibling list is circular therefore if there are no siblings yet, 
			//the node will point to itself from both sides
			this.right = this;
			this.left = this;
			this.parent = null;
		}

		public int getKey() {
			return this.key;
		}

	}
}
