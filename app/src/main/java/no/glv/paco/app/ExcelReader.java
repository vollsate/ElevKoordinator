package no.glv.paco.app;

import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import no.glv.paco.beans.GroupBean;
import no.glv.paco.beans.ParentBean;
import no.glv.paco.beans.PhoneBean;
import no.glv.paco.beans.StudentBean;
import no.glv.paco.intrfc.Group;
import no.glv.paco.intrfc.Parent;
import no.glv.paco.intrfc.Phone;
import no.glv.paco.intrfc.Student;

/**
 *
 * @author glevoll
 *
 */
public class ExcelReader {

    public static final String EXCEL_FILENAME = "harestuaskole.xls";

    private HSSFWorkbook workbook;
    private File file;

    private InputStream fis;

    private List<String> availClasses;

    /**
     * @param fName Name of file (full path) to the Excel file to read.
     */
    public ExcelReader( String fName ) {
        this( fName == null ? new File( EXCEL_FILENAME ) : new File( fName ) );
    }

    public ExcelReader( File f ) {
        this.file = f;
    }

    public ExcelReader( InputStream f ) {
        this.fis = f;
    }

    /**
     *
     */
    public ExcelReader loadWorkbook() {
        try {
            workbook = getWorkbook();
        } catch ( IOException e ) {
            Log.e( getClass().getSimpleName(), "Cannot load Excel file: " + file.getAbsolutePath(), e );
        }

        return this;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private HSSFWorkbook getWorkbook() throws IOException {
        if ( workbook != null ) return workbook;

        if ( fis == null ) {
            fis = new FileInputStream( file );
        }

        HSSFWorkbook workbook = new HSSFWorkbook( fis );

        return workbook;
    }

    /**
     *
     * @return List of all available groups in this Excel workbook
     */
    public List<String> getAvailableGroups() {
        if ( availClasses != null ) return availClasses;

        int i = workbook.getNumberOfSheets();
        List<String> list = new ArrayList<String>( i );
        for ( int j = 0; j < i; j++ ) {
            list.add( workbook.getSheetName( j ) );
        }

        availClasses = list;
        return availClasses;
    }

    /**
     *
     * @return List of all Group instances created by the Excel workbook
     */
    public List<Group> loadGroups() {
        LinkedList<Group> list = new LinkedList<>();

        int iSheet = workbook.getNumberOfSheets();
        for ( int i = 0; i < iSheet; i++ ) {
            Group group = loadOneGroup( i );
            list.add( group );
        }

        return list;
    }

    /**
     *
     * @param name
     * @return
     */
    public Group loadGroup( String name ) {
        HSSFSheet sheet = workbook.getSheet( name );
        if ( sheet == null ) return null;

        return loadOneGroup( workbook.getNameIndex( name ) );
    }

    /**
     *
     * @param num The sheet at num
     *
     * @return The group loaded
     */
    private Group loadOneGroup( int num ) {
        HSSFSheet sheet = workbook.getSheetAt( num );

        Group group = new GroupBean( num) ;
        group.setName(  sheet.getSheetName() );

        int rows = sheet.getLastRowNum();

        // We need to skip the first row, thats the headers
        for ( int i = 1; i < rows; i++ ) {
            HSSFRow row = sheet.getRow( i );
            Student std = loadOneStudent( new RowReader( row ), group.getName() );
            group.add( std );
        }

        return group;
    }

    /**
     *
     * @param row
     * @param stdClass
     * @return
     */
    private Student loadOneStudent( RowReader row, String stdClass ) {
        Student std = new StudentBean( stdClass );

        std.setFirstName( row.nextCell().getStringCellValue() );
        std.setLastName( row.nextCell().getStringCellValue() );
        std.setBirth( row.nextCell().getDateCellValue() );
        std.setAdress( row.nextCell().getStringCellValue() );

        std.setIdent( DataHandler.CreateStudentID( std ) );

        std.addParent( loadOneParent( row, std.getIdent(), Parent.PRIMARY ) );
        std.addParent( loadOneParent( row, std.getIdent(), Parent.SECUNDARY ) );

        return std;
    }

    /**
     *
     * @param row
     * @param ident
     * @param type
     * @return A {@link Parent} instance if possible, or NULL if no data exist.
     */
    private Parent loadOneParent( RowReader row, String ident, int type ) {
        HSSFCell cell = row.nextCell();

        // No parent will be loaded!
        if ( cell == null ) return null;

        Parent p = new ParentBean( null, type );
        p.setStudentID( ident );
        p.setLastName( cell.getStringCellValue() );
        p.setFirstName( row.nextCell().getStringCellValue() );

        Phone ph = loadOnePhone( row, Phone.MOBIL );
        if ( ph != null ) {
            ph.setStudentID( ident );
            p.addPhone( ph );
        }

        ph = loadOnePhone( row, Phone.WORK );
        if ( ph != null ) {
            ph.setStudentID( ident );
            p.addPhone( ph );
        }

        ph = loadOnePhone( row, Phone.HOME );
        if ( ph != null ) {
            ph.setStudentID( ident );
            p.addPhone( ph );
        }

        cell = row.nextCell();
        if ( cell != null )
            p.setMail( cell.getStringCellValue() );

        return p;
    }

    /**
     *
     * @param row The current row we are working on
     * @param type
     */
    private Phone loadOnePhone( RowReader row, int type ) {
        HSSFCell cell = row.nextCell();
        if ( cell == null ) return null;

        Phone ph = new PhoneBean( type );
        ph.setNumber( ( long ) cell.getNumericCellValue() );

        return ph;
    }

    /**
     * Used as a helper class to easier traverse through the rows in
     * a sheet.
     *
     * @author glevoll
     *
     */
    private static class RowReader {

        private HSSFRow row;
        private int index = 0;

        public RowReader( HSSFRow row ) {
            this.row = row;
            index = 0;
        }

        public HSSFCell nextCell() {
            return row.getCell( index++ );
        }
    }

}
