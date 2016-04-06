package no.glv.elevko.data;

import no.glv.elevko.intrfc.SubjectType;

public class SubjectTypeBean implements SubjectType {

    /**
     * The row in the database
     */
    int _id;

    private String mName;
    private String mDesc;
    private int mType;

    @Override
    public int getID() {
        return _id;
    }

    @Override
    public boolean isCustomSpecific() {
        return _id > mType;

    }

    @Override
    public boolean isSystemSpecific() {
        return _id < TYPE_CUSTOM;

    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getDescription() {
        return mDesc;
    }

    @Override
    public int getType() {
        return mType;
    }

    @Override
    public void setName( String newName ) {
        mName = newName;
    }

    @Override
    public void setDescription( String newDesc ) {
        mDesc = newDesc;
    }

    @Override
    public void setType( int type ) {
        mType = type;
    }

}
