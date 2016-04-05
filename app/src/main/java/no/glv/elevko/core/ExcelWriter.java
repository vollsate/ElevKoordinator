package no.glv.elevko.core;

import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import no.glv.elevko.base.BaseActivity;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentClass;
import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.Task;

/**
 * Writes the entire DB to excel.
 * 
 * @author glevoll
 *
 */
public class ExcelWriter {

	private static final String TAG = ExcelWriter.class.getSimpleName();

	public static final HSSFCellStyle headerStyle = null;

	private HSSFWorkbook workbook;

	public ExcelWriter() {
		workbook = new HSSFWorkbook();
	}

	public File writeToFile( String fileName ) throws IOException {
		File externalDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
		String fName = externalDir.getAbsolutePath() + "/" + fileName;

		FileOutputStream stream = null;
		File file = new File( fName );

		try {
			stream = new FileOutputStream( file );
		}
		catch ( FileNotFoundException fnfEx ) {
			Log.e( TAG, "LoadStudentClass(): File not found: " + fName, fnfEx );
			throw fnfEx;
		}
		catch ( RuntimeException e ) {
			Log.e( TAG, "LoadStudentClass(): Unknown error: " + fName, e );
			throw e;
		}

		workbook.write( stream );
		return file;
	}

	public void writeToStream( OutputStream ops ) {

	}

	public HSSFWorkbook getWorkBook() {
		return workbook;
	}

	/**
	 * 
	 * @param stdClasses
	 */
	public void addStudentClasses( List<StudentClass> stdClasses ) {
		HSSFSheet sheet = workbook.createSheet( "Installerte klasser" );

		int rowNum = 0, colNum;

		for ( StudentClass stdClass : stdClasses ) {
			colNum = 0;
			HSSFRow row = sheet.createRow( rowNum++ );
			
			HSSFCell cell = row.createCell( colNum );
			cell.setCellValue( stdClass.getName() );
			
			row = sheet.createRow( rowNum++ );
			cell = row.createCell( colNum++ );
			cell.setCellValue( "IDENT" );
			
			cell = row.createCell( colNum++ );
			cell.setCellValue( "FORNAVN" );
			
			cell = row.createCell( colNum++ );
			cell.setCellValue( "ETTERNAVN" );
			
			cell = row.createCell( colNum++ );
			cell.setCellValue( "FØDT" );

			cell = row.createCell( colNum++ );
			cell.setCellValue( "ADRESSE" );

			for ( Student std : stdClass.getStudents() ) {
				colNum = 0;
				row = sheet.createRow( rowNum++ );

				cell = row.createCell( colNum++ );
				cell.setCellValue( std.getIdent() );

				cell = row.createCell( colNum++ );
				cell.setCellValue( std.getFirstName() );

				cell = row.createCell( colNum++ );
				cell.setCellValue( std.getLastName() );

				cell = row.createCell( colNum++ );
				cell.setCellValue( std.getBirth() );

				cell = row.createCell( colNum++ );
				cell.setCellValue( std.getAdress() );
			}
		}
	}

	/**
	 * 
	 * @param stdTasks
	 */
	public void addStudentTasks( List<StudentTask> stdTasks ) {
		HSSFSheet sheet = workbook.createSheet( "Oppgaver og studenter" );

		int rowNum = 0, colNum = 0;

		HSSFRow row = sheet.createRow( rowNum++ );
		HSSFCell cell = row.createCell( colNum++ );
		cell.setCellValue( "OPPGAVE" );

		cell = row.createCell( colNum++ );
		cell.setCellValue( "OPPG ID" );

		cell = row.createCell( colNum++ );
		cell.setCellValue( "STUDENT ID" );

		cell = row.createCell( colNum++ );
		cell.setCellValue( "INNLEVERT" );

		cell = row.createCell( colNum++ );
		cell.setCellValue( "STATUS" );

		cell = row.createCell( colNum++ );
		cell.setCellValue( "ID" );

		for ( StudentTask st : stdTasks ) {
			colNum = 0;
			row = sheet.createRow( rowNum++ );
			cell = row.createCell( colNum++ );
			cell.setCellValue( st.getTaskName() );

			cell = row.createCell( colNum++ );
			cell.setCellValue( st.getTaskID() );

			cell = row.createCell( colNum++ );
			cell.setCellValue( st.getIdent() );

			cell = row.createCell( colNum++ );
			Date d = st.getHandInDate();
			if ( d != null )
				cell.setCellValue( BaseActivity.GetDateAsString( d ) );
			else
				cell.setCellValue( "ikke levert" );

			cell = row.createCell( colNum++ );
			cell.setCellValue( st.getModeAsString() );

			
			cell = row.createCell( colNum++ );
			cell.setCellValue( st.getID() );
		}
	}

	/**
	 * 
	 * @param tasks
	 */
	public void addTasks( List<Task> tasks ) {
		HSSFSheet sheet = workbook.createSheet( "Oppgaver" );

		int rowNum = 0;
		int colNum = 0;
		
		HSSFRow row = sheet.createRow( rowNum++ );

		HSSFCell cell = row.createCell( colNum++ );
		cell.setCellValue( "NAVN" );

		cell = row.createCell( colNum++ );
		cell.setCellValue( "BESKRIVELSE" );
		
		cell = row.createCell( colNum++ );
		cell.setCellValue( "STATUS" );
		
		cell = row.createCell( colNum++ );
		cell.setCellValue( "ANTALL STUDENTER" );
		
		cell = row.createCell( colNum++ );
		cell.setCellValue( "ID" );
		
		for ( Task t : tasks ) {
			colNum = 0;
			row = sheet.createRow( rowNum++ );
			
			cell = row.createCell( colNum++ );
			cell.setCellValue( t.getName() );

			cell = row.createCell( colNum++ );
			cell.setCellValue( t.getDesciption() );

			cell = row.createCell( colNum++ );
			cell.setCellValue( t.getStateAsString() );

			cell = row.createCell( colNum++ );
			cell.setCellValue( t.getStudentCount() );

			cell = row.createCell( colNum++ );
			cell.setCellValue( t.getID() );
		}
	}

	public static class WorkbookData {

		public List<StudentClass> stdClasses;
		public List<Task> tasks;
		public List<StudentTask> stdInTasks;
	}
}
