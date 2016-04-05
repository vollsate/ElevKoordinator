package no.glv.elevko.xml;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

public class XMLGoogleLoader {

    private static final String XML_LINK = "https://drive.google.com/file/d/0B4Z9jfzjq3_GcmMtM3ZqbjhocE0/view?usp=sharing";

    public XMLGoogleLoader() {
    }

    public static void main( String[] args ) {
	System.out.println( "Test" );
	
	XmlPullParser parser = Xml.newPullParser();

	try {
	    parser.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, false );

	    URL url = new URL( XML_LINK );
	    URLConnection con = url.openConnection();
	    parser.setInput( con.getInputStream(), null );

	    parser.nextTag();
	    parseFeed( parser );
	}
	catch ( Exception e ) {
	    e.printStackTrace();
	}
    }

    private static List<XMLData> parseFeed( XmlPullParser parser ) throws Exception {
	LinkedList<XMLData> data = new LinkedList<XMLGoogleLoader.XMLData>();

	parser.require( XmlPullParser.START_TAG, null, "swf" );
	while ( parser.next() != XmlPullParser.END_TAG ) {
	    if ( parser.getEventType() != XmlPullParser.START_TAG ) {
		continue;
	    }

	    String name = parser.getName();
	    if ( "class".equals( name ) ) {
		XMLData xml = new XMLData();
		xml.name = null;
		xml.url = null;

		data.add( xml );
	    }
	}

	return data;
    }

    static class XMLData {
	String name;
	String url;

    }
}
