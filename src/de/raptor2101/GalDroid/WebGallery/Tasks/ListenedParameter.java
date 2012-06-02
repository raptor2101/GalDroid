package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.lang.ref.WeakReference;

class ListenedParameter<ObjectType,ListenerType> {
	private final WeakReference<ListenerType> mListener;
	private final ObjectType mObject;
	
	public ListenedParameter(ObjectType object, ListenerType listener) {
		mListener = new WeakReference<ListenerType>(listener);
		mObject = object;
	}

	public ListenerType getListener() {
		return mListener.get();
	}

	public ObjectType getObject() {
		return mObject;
	}
	
	@Override
	public String toString() {
		return mObject.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mObject == null) ? 0 : mObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		ListenedParameter<ObjectType,ListenerType> other = (ListenedParameter<ObjectType,ListenerType>) obj;
		if (mObject == null) {
			if (other.mObject != null)
				return false;
		} else if (!mObject.equals(other.mObject))
			return false;
		return true;
	}
}
