package no.glv.paco.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.List;

/**
 * Represents an {@link ExpandableListView} where the child of an item is more
 * detailed information found in the same object that displays the parent item.
 * 
 * <p>
 * The <tt>T</tt> object is the generic class that holds the data used to
 * display both the list item and the child item.
 * 
 * @author glevoll
 *
 */
public abstract class ExpandableListViewBase<T> extends BaseExpandableListAdapter {

	private Context mContext;

	private List<T> data;

	public ExpandableListViewBase( Context context, List<T> data ) {
		this.mContext = context;
		this.data = data;
	}

	protected Context getContext() {
		return mContext;
	}

	protected T getItem( int pos ) {
		return data.get( pos );
	}

	@Override
	public int getGroupCount() {
		return data.size();
	}

	@Override
	public int getChildrenCount( int groupPosition ) {
		return 1;
	}

	@Override
	public T getGroup( int groupPosition ) {
		return getItem( groupPosition );
	}

	@Override
	public T getChild( int groupPosition, int childPosition ) {
		return getItem( groupPosition );
	}

	@Override
	public long getGroupId( int groupPosition ) {
		return groupPosition;
	}

	@Override
	public long getChildId( int groupPosition, int childPosition ) {
		return groupPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public abstract View getGroupView( int groupPosition, boolean isExpanded, View convertView, ViewGroup parent );

	@Override
	public abstract View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent );

	@Override
	public boolean isChildSelectable( int groupPosition, int childPosition ) {
		return false;
	}

}
