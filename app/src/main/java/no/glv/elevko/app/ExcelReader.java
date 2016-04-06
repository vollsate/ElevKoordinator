package no.glv.elevko.app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import no.glv.elevko.intrfc.Group;
import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.data.ParentBean;
import no.glv.elevko.data.PhoneBean;
import no.glv.elevko.data.StudentBean;
import no.glv.elevko.data.GroupBean;

/**
 * 
 * @author glevoll
 *
 */
public class ExcelReader extends AsyncTask<Void, Integer, List<String>> {

	public static final String EXCEL_FILENAME = "harestuaskole.xls";

	Context context;
	private HSSFWorkbook workbook ;
	private String fileName;
	private OnExcelWorkbookLoadedListener listener;
	
	private List<String> availClasses;

	/**
	 * 
	 * @param ctx
	 */
	public ExcelReader( Context ctx, String fName, OnExcelWorkbookLoadedListener l ) {
		context = ctx;
		listener = l;
				
		if ( fName == null ) this.fileName = EXCEL_FILENAME;
		else this.fileName = fName;
	}
	
	/**
	 * 
	 */
	public List<String> doInBackground(Void... voids) {
		try {
			workbook = getWorkbook();
			return getAvailableClasses();
		}
		catch ( IOException e ) {
			Log.e( getClass().getSimpleName(), "Cannot load Excel file: " + fileName, e );
		}

		return null;
	}
	
	@Override
	protected void onPostExecute( List<String> result ) {
		listener.onWorkbookLoaded( result );
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private HSSFWorkbook getWorkbook() throws IOException {
		if ( workbook != null ) return workbook;
		
		
		InputStream s = context.getAssets().open( fileName );
		HSSFWorkbook workbook = new HSSFWorkbook( s );

		return workbook;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getAvailableClasses() {
		if ( availClasses != null ) return availClasses;

		int i = workbook.getNumberOfSheets();
		List<String> list = new ArrayList<String>(i);
		for ( int j = 0; j < i; j++ ) {
			list.add( workbook.getSheetName( j ) );
		}
		
		availClasses = list;
		return availClasses;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Group> loadClasses( ) throws IOException {
		LinkedList<Group> list = new LinkedList<Group>();

		int iSheet = workbook.getNumberOfSheets();
		for ( int i = 0; i < iSheet; i++ ) {
			Group stdClass = loadOneClass( workbook.getSheetAt( i ) );
			list.add( stdClass );
		}

		workbook.close();

		return list;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public Group loadClass( String name ) {
		HSSFSheet sheet = workbook.getSheet( name );
		if ( sheet == null ) return null;
		
		return loadOneClass( sheet );
	}

	/**
	 * 
	 * @param sheet
	 * @return
	 */
	private Group loadOneClass( HSSFSheet sheet ) {
		Group stdClass = new GroupBean( sheet.getSheetName() );

		int rows = sheet.getLastRowNum();

		// We need to skip the first row, thats the headers
		for ( int i = 1; i < rows; i++ ) {
			HSSFRow row = sheet.getRow( i );
			Student std = loadOneStudent( new RowReader( row ), stdClass.getName() );
			stdClass.add( std );
		}

		return stdClass;
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
		ph.setNumber( (long) cell.getNumericCellValue() );

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
		
		public RowReader( HSSFRow row) {
			this.row = row;
			index = 0;
		}
		
		public HSSFCell nextCell() {
			return row.getCell( index++ );
		}
	}
	
	public interface OnExcelWorkbookLoadedListener {
		
		void onWorkbookLoaded( List<String> fileNames );
	}
}
