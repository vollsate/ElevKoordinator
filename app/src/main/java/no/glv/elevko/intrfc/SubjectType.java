package no.glv.elevko.intrfc;

/**
 * Represents a subject or type to better specify the type of {@link Task} a
 * {@link Student} is involved in.
 * <p>
 * <p>
 * The two types are either <tt>subject</tt> or <tt>type</tt>. The subject may
 * be translated to curriculum, and the type is category: text, parent approval
 * or other types of tasks the student may be linked to.
 * <p>
 * <p>The system installs a few default subject/ types. Any custom type the user
 * installs, gets a specific flag.
 *
 * @author glevoll
 */
public interface SubjectType {

    /**
     * SubjectType THEME. Used to specify a certain action within a task,
     */
    int TYPE_THEME = 1;
    /**
     * SubjectType SUBJECT. Used to indicate witch subject the task is connected to.
     * Any SUBJECT can use any THEME subjecttype.
     */
    int TYPE_SUBJECT = 2;

    /**
     * Used to flag a custom type.
     */
    int TYPE_CUSTOM = 4;

    int getID();

    boolean isSystemSpecific();

    boolean isCustomSpecific();

    String getName();

    String getDescription();

    int getType();

    void setName( String newName );

    void setDescription( String newDesc );

    void setType( int type );
}
