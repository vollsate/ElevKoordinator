package no.glv.elevko.intrfc;

/**
 * Listener that defines change to the DataHandler data set. The interfaces
 * will only be called when there is a change to the state of the
 * DataHandler, and not when there's a change to any intern state of an
 * instance.
 * <p/>
 * <p/>
 * The interfaces will be called when a new object is added or removed.
 *
 * @author glevoll
 */
public interface OnChange {
    int MODE_ADD = 1;
    int MODE_DEL = 2;
    int MODE_UPD = 4;

    int MODE_CLS = 8;
}

