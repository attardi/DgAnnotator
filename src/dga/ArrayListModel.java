///
/// Copyright (c) 2005, Giuseppe Attardi (attardi@di.unipi.it).
///
package dga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * ArrayList which can be used as a ListModel in UI code
 * @author Phil Herold
 */
@SuppressWarnings("serial")
public class ArrayListModel<E> extends ArrayList<E> implements ListModel<Object> {
    /**
     * This class implements an empty ArrayListModel
     */
    private static class EmptyList extends ArrayListModel<Object>
                                   implements RandomAccess, Serializable {
        public int size() {
            return 0;
        }
        public boolean contains(Object obj) {
            return false;
        }
        public Object get(int index) {
            throw new IndexOutOfBoundsException("Index: " + index); //$NON-NLS-1$
        }
        // Preserves singleton property
        private Object readResolve() {
            return EMPTY_LIST;
        }
    }

    /** The empty list (immutable).  This list is serializable. */
    public static final ArrayListModel<?> EMPTY_LIST = new EmptyList();
    /** List of ListDataListeners */
	protected List<ListDataListener> listDataListeners;
	
	/**
	 * Augments superclass method to fire an appropriate event when an item is added to the
	 * collection. 
	 * @param index int
	 * @param obj Object
	 */
	@Override
	public void add(int index, E element) {
		super.add(index, element);
		fireIntervalAdded(index, index);
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when an item is added to the
	 * collection 
	 * @param o E
	 * @return boolean true (as per the contract of <code>Collection.add()</code>)
	 */
	@Override
	public boolean add(E o) {
		boolean ok = super.add(o);
		if (ok) {
			int index = size() - 1;
			fireIntervalAdded(index, index);
		}
		return ok;
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when a collection is added to
	 * this collection
	 * @param coll Collection
	 * @return boolean true if obj was successfully added
	 */
	@Override
	public boolean addAll(Collection<? extends E> coll) {
		int firstIndex = size() - 1;
		boolean ok = super.addAll(coll);
		if (ok) {
			fireIntervalAdded(firstIndex, size() - 1);
		}
		return ok;
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when the collection is emptied
	 */
	@Override
	public void clear() {
		int lastIndex = size() - 1;
		super.clear();
		fireIntervalRemoved(0, lastIndex);
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when the element at the given index
	 * is removed from the collection
	 * @param index int
	 * @return the element removed from the list
	 */
	@Override
	public E remove(int index) {
		E element = super.remove(index);
		if (element != null) {
			fireIntervalRemoved(index, index);
		}
		return element;
	}
	
	/**
	 * Overrides superclass method, forwarding it to the <code>remove(index)</code> method
	 * so the appropriate event can be fired.
	 * @param obj Object
	 * @return true if the element was removed, false otherwise
	 */
	@Override
	public boolean remove(Object obj) {
		boolean ok = true;
		int index = indexOf(obj);
		if (index != -1) {
			ok = remove(index) != null;
		}
		return ok;
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when a range of items is removed
	 * from the collection
	 * @param fromIndex int
	 * @param toIndex int 
	 */
	@Override
	public void removeRange(int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
		fireIntervalRemoved(fromIndex, toIndex);
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when the given collection is
	 * removed from this collection
	 * @param coll Collection
	 * @return boolean true if the collection was successfully removed
	 */
	@Override
	public boolean removeAll(Collection<?> coll) {
		int lastIndex = size() - 1;
		boolean ok = super.removeAll(coll);
		if (ok) {
			fireIntervalRemoved(0, lastIndex);
		}
		return ok;
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when the given collection is
	 * retained in this collection
	 * @param coll Collection
	 * @param boolean true if successful
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		int lastIndex = size() - 1;
		boolean ok = super.retainAll(c);
		if (ok) {
			fireIntervalRemoved(0, lastIndex);
			fireIntervalAdded(0, size() - 1);
		}
		return ok;
	}
	
	/**
	 * Augments superclass method to fire an appropriate event when an item in the collection 
	 * is modified
	 * @param index int
	 * @param element Object 
	 */
	@Override
	public E set(int index, E element) {
		element = super.set(index, element);
		fireIntervalUpdated(index, index);
		return element;
	}

	/**
	 * Implementation of the method in the ListModel interface
	 * @return int
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return super.size();
	}

	/**
	 * Implementation of the method in the ListModel interface
	 * @param index int
	 * @return Object
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return super.get(index);
	}

	/**
	 * Implementation of the method in the ListModel interface
	 * @param listener ListDataListener
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	public void addListDataListener(ListDataListener listener) {
		if (listDataListeners == null) {
			listDataListeners = new ArrayList<ListDataListener>();
		}
		if (!listDataListeners.contains(listener)) {
			listDataListeners.add(listener);
		}
	}

	/**
	 * Implementation of the method in the ListModel interface
	 * @param listener ListDataListener
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	public void removeListDataListener(ListDataListener listener) {
		if (listDataListeners != null) {
			listDataListeners.remove(listener);
		}
	}
	
	/**
	 * Helper method to fire an event to all listeners when an interval in the collection
	 * is added
	 * @param firstIndex  int
	 * @param lastIndex int
	 */
	protected void fireIntervalAdded(int firstIndex, int lastIndex) {
		if (listDataListeners != null) {
			ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, firstIndex, lastIndex);
			for (ListDataListener listener: listDataListeners) {
				listener.intervalAdded(e);
			}
		}
	}
	
	/**
	 * Helper method to fire an event to all listeners when an interval in the collection
	 * is removed
	 * @param firstIndex  int
	 * @param lastIndex int
	 */
	protected void fireIntervalRemoved(int firstIndex, int lastIndex) {
		if (listDataListeners != null) {
			ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, firstIndex, lastIndex);
			for (ListDataListener listener: listDataListeners) {
				listener.intervalRemoved(e);
			}
		}
	}
	
	/**
	 * Helper method to fire an event to all listeners when an interval in the collection
	 * is updated
	 * @param firstIndex  int
	 * @param lastIndex int
	 */
	protected void fireIntervalUpdated(int firstIndex, int lastIndex) {
		if (listDataListeners != null) {
			ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, firstIndex, lastIndex);
			for (ListDataListener listener: listDataListeners) {
				listener.contentsChanged(e);
			}
		}		
	}

}
