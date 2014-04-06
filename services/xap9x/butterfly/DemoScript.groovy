import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.openspaces.admin.AdminFactory
import org.openspaces.admin.Admin
import java.util.concurrent.TimeUnit

import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;

import demo.EngineerPojo;

public class Demo
{


    SpaceDocument document;
    
    EngineerPojo engineer;

    private void writePojos(GigaSpace gigaSpace) {
		printCommands('gigaSpace.write(new EngineerPojo(123, "Me", "groovy"));'+
					   'gigaSpace.write(new EngineerPojo(345, "you", "java"));');
					   
        gigaSpace.write(new EngineerPojo(123, "Me", "groovy"));
        gigaSpace.write(new EngineerPojo(345, "you", "java"));
    }

    private void writeDocument1(GigaSpace gigaSpace) {
		printCommands('document.setProperty("id", 789);'+
			'document.setProperty("name", "he");'+
			'document.setProperty("age", 21);'+
			'gigaSpace.write(document);');
			
        document.setProperty("id", 789);
        document.setProperty("name", "he");
        document.setProperty("age", 21);
        gigaSpace.write(document);
    }

    private void writeDocument2(GigaSpace gigaSpace) {
		printCommands('document.setProperty("id", 743);'+
			'document.setProperty("name", "she");'+
			'document.setProperty("age", 21);'+
			'gigaSpace.write(document);');
			
        document.setProperty("id", 743);
        document.setProperty("name", "she");
        document.setProperty("age", 21);
        gigaSpace.write(document);
    }

    private void testReadPojo(GigaSpace gigaSpace) {
		printCommands('engineer = gigaSpace.read(new EngineerPojo(123));'+
			'System.out.println(engineer);');
	
        engineer = gigaSpace.read(new EngineerPojo(123));
        System.out.println(engineer);
    }

    private void testReadDocument(GigaSpace gigaSpace) {
		printCommands('document.setProperty("id", 345);'+
			'SpaceDocument engineerDoc = gigaSpace.read(document);'+
			'System.out.println(engineerDoc);');
			
        document.setProperty("id", 345);
        SpaceDocument engineerDoc = gigaSpace.read(document);
        System.out.println(engineerDoc);
    }

    private void testReadSQLQuery(GigaSpace gigaSpace) {
		printCommands('engineer = gigaSpace.read(new SQLQuery<EngineerPojo>(EngineerPojo.class,'+
					'"id=789 AND name=\'he\'"));'+
			'System.out.println(engineer);');
	
        engineer = gigaSpace.read(new SQLQuery<EngineerPojo>(EngineerPojo.class,
                "id=789 AND name='he'"));
        System.out.println(engineer);
    }

    private void testReadJDBC() throws Exception {
		printCommands('Class.forName("com.j_spaces.jdbc.driver.GDriver");'+
			'Connection connection = null;'+
			'connection = DriverManager.getConnection("jdbc:gigaspaces:url:" + getRemoteSpaceURL());'+
			'Statement statement = connection.createStatement();'+
			'statement.execute("SELECT * FROM demo.EngineerPojo WHERE age=21");');
		
        Class.forName("com.j_spaces.jdbc.driver.GDriver");
        Connection connection = null;
        connection = DriverManager.getConnection("jdbc:gigaspaces:url:" + getRemoteSpaceURL());
        Statement statement = connection.createStatement();
        statement.execute("SELECT * FROM demo.EngineerPojo WHERE age=21");

        ResultSet resultSet = statement.getResultSet();
        int count = 0;
        while (resultSet.next()) {
            count++;
            System.out.println("JDBC: id=" + resultSet.getInt("id") + " name=" + resultSet.getString("name") + " age=" + resultSet.getInt("age"));
        }
    }

    private String getRemoteSpaceURL() {
        return "jini://*/*/myDataGrid?locators="+System.getenv("LOOKUPLOCATORS");
    }

    private void testReadJDBC2() throws Exception {
		printCommands('Class.forName("com.j_spaces.jdbc.driver.GDriver");'+
			'String url = getRemoteSpaceURL();'+
			'System.out.println("permutation url == " + url);'+
			'Connection connection = DriverManager.getConnection("jdbc:gigaspaces:url:" + url);'+
			'Statement statement = connection.createStatement();'+
			'statement.execute("SELECT * FROM demo.EngineerPojo");');
	
        Class.forName("com.j_spaces.jdbc.driver.GDriver");
        String url = getRemoteSpaceURL();
        System.out.println("permutation url == " + url);
        Connection connection = DriverManager.getConnection("jdbc:gigaspaces:url:" + url);
        Statement statement = connection.createStatement();
        statement.execute("SELECT * FROM demo.EngineerPojo");

        ResultSet resultSet = statement.getResultSet();
        int count = 0;
        while (resultSet.next()) {
            count++;
            System.out.println("JDBC: id=" + resultSet.getInt("id") + " name=" + resultSet.getString("name") + " age=" + resultSet.getInt("age"));
        }
    }

	private void printCommands(String message) {
		for (String s : message.split(";")) {
			println "\t"+s+";"
		}
	}
	
    public void run(GigaSpace gigaSpace) throws Exception {

        this.document = new SpaceDocument("demo.EngineerPojo");
		gigaSpace.getTypeManager().registerTypeDescriptor(new SpaceTypeDescriptorBuilder("demo.EngineerPojo").idProperty("id").create())

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
		
		println "Press ENTER to writePojos"
		br.readLine()
		
        writePojos(gigaSpace);
		println ""
		
		println "Press ENTER to testReadPojo"
		br.readLine()
		
        testReadPojo(gigaSpace);
		println ""

		println "Press ENTER to testReadDocument"
		br.readLine()
		
        testReadDocument(gigaSpace);
		println ""
		
		println "Press ENTER to writeDocument1"
		br.readLine()
		
        writeDocument1(gigaSpace);
		println ""

		println "Press ENTER to testReadJDBC2"
		br.readLine()
		
        testReadJDBC2();
		println ""

		println "Press ENTER to testReadSQLQuery"
		br.readLine()
		
        testReadSQLQuery(gigaSpace);
		println ""

		println "Press ENTER to writeDocument2"
		br.readLine()
		
		
        writeDocument2(gigaSpace);
		println ""

		println "Press ENTER to testReadJDBC"
		br.readLine()
		
        testReadJDBC();
		println ""

		println "Press ENTER to exit"
		br.readLine()

    }
}

static def run_demo() {
    try {
        def lookuplocators = System.getenv("LOOKUPLOCATORS")
        def gridname = "myDataGrid"
        Admin admin = new AdminFactory().useDaemonThreads(true).addLocators(lookuplocators).createAdmin();
        def pus = admin.getProcessingUnits().waitFor(gridname, 10, TimeUnit.SECONDS);
        pus.waitFor(1)

        println "Found " + pus.getInstances().length + " space instances";

        def gigaSpace = admin.getProcessingUnits().getProcessingUnit(gridname).getSpace().getGigaSpace()
        def demo = new Demo()
        demo.run(gigaSpace)
    } catch (Exception e) {
        e.printStackTrace()
        println "Error occurred: "+e.toString()
    }
}