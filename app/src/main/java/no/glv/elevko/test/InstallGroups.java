package no.glv.elevko.test;

import java.io.File;
import java.util.List;

import no.glv.elevko.app.ExcelReader;
import no.glv.elevko.data.CloudSQLDatabase;
import no.glv.elevko.intrfc.Group;

/**
 * Created by GleVoll on 10.04.2016.
 */
public class InstallGroups {

    public static void main( String[] args ) throws Exception {
        File f = new File( "app/src" );
        System.out.println( f.getAbsolutePath() );

        ExcelReader reader = new ExcelReader( "app/src/main/assets/" + ExcelReader
                .EXCEL_FILENAME );

        reader.loadWorkbook();
        List<Group> groups = reader.loadGroups();

        CloudSQLDatabase database = CloudSQLDatabase.GetInstance(
                "jdbc:mysql://173.194.229.217:3306",
                "pacu",
                "123456" );

        //database.insertGroup( groups.get( 0 ) );
    }
}
