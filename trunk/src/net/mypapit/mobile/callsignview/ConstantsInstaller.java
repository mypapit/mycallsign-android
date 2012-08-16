package net.mypapit.mobile.callsignview;
/*
 * The source code for ConstantInstaller.java is in the public domain.  You are free to use it
 * in any manner you wish.
 *
 */
 import android.database.sqlite.SQLiteDatabase;

public class ConstantsInstaller extends DatabaseInstaller  {

	
	public ConstantsInstaller(CallsignViewActivity context,String name, SQLiteDatabase.CursorFactory factory, int version, int resource){
		
		super(context,name,factory,version,resource);
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		db.execSQL("DROP TABLE IF EXISTS constants");
		db.execSQL("DROP TABLE IF EXISTS aa");
		onCreate(db);

	}

}
