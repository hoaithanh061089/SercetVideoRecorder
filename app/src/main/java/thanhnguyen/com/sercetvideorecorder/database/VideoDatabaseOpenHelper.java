package thanhnguyen.com.sercetvideorecorder.database;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VideoDatabaseOpenHelper extends SQLiteOpenHelper{
    private static final String DBNAME = "contactlist";
	
	private static final int VERSION = 1;
	private static final String TABLE_NAME = "hiddenvideodatabse";
	private static final String FIELD_ID = "_id";
	private static final String FIELD_DATE = "date";
	private static final String FIELD_DURATION = "duration";
	private static final String FIELD_IMAGEPATH = "imagepath";
	private static final String FIELD_THUMBNAILPATH = "thumbnailpath";
	private static final String FIELD_LONGTITUDE = "longtitude";
	private static final String FIELD_LATITUDE = "latitude";
		
	public VideoDatabaseOpenHelper(Context context)
									 {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "";
		
		// Defining table structure
		sql = "create table IF NOT EXISTS " + "" + TABLE_NAME + "" +
										" ( " +
											FIELD_ID + " integer primary key autoincrement, " + 
											FIELD_DATE + " text , " +
											FIELD_DURATION + " text , " +
									    	FIELD_LONGTITUDE + " text , " + 
											FIELD_LATITUDE + " text , " + 
											FIELD_THUMBNAILPATH + " text , " + 
											FIELD_IMAGEPATH + " text " + 
											
											
											") " ;
		
		// Creating table
		db.execSQL(sql);			
		
						
		}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {



}
}