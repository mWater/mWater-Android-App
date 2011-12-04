package ca.ilanguage.rhok.imageupload.db;

import java.util.regex.Pattern;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.net.Uri;
import android.provider.BaseColumns;


public class ImageUploadHistoryDatabase {
	 /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;
    
    public interface SyncColumns {
    	String UPDATED = "updated";
    }
    /**
     * An interface which states the columns in the ImageUpload History table
     */
    interface ImageUploadHistoryColumns{
	    String FILEPATH = "filepath"; //name will have timestamp and water sample code
	    String METADATA = "metadata"; //put fields in JSON instead of having to update the DB schema each time we want to add a field. 
	    String UPLOADED = "uploaded";  //0 not uploaded, 1 uploaded
	    
    }
    
    public static final String AUTHORITY = "ca.ilanguage.rhok.imageupload.db.ImageUploadHistory";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String IMAGE_UPLOAD_HISTORY_TABLE_NAME = "imageuploadhistory";
    
    
    private static final String PATH_EXPORT = "export";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

    public static class ImageUploadHistory implements ImageUploadHistoryColumns, BaseColumns{
    	//This class cannot be instantiated
    	private ImageUploadHistory(){
    		
    		
    	}
    	
    	//leads to the database file on the data directory of the device
    	public static final Uri CONTENT_URI =
    		BASE_CONTENT_URI.buildUpon().appendPath(IMAGE_UPLOAD_HISTORY_TABLE_NAME).build();
    	public static final Uri CONTENT_EXPORT_URI =
    		CONTENT_URI.buildUpon().appendPath(PATH_EXPORT).build();
    	
    	/**
         * The MIME type of {@link #CONTENT_URI} providing a directory of imageupload histories.
         */
    	public static final String CONTENT_TYPE = 
        	"vnd.android.cursor.dir/vnd.ilanguage.imageuploadhistory";
    	/**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single imageupload history.
         */
        public static final String CONTENT_ITEM_TYPE =
    		"vnd.android.cursor.item/vnd.ilanguage.imageuploadhistory";
    	
    	/** Default "ORDER BY" clause in SQL is by date modified */
        public static final String DEFAULT_SORT_ORDER = ImageUploadHistoryColumns.UPLOADED+ " ASC";
        //for alphabetical by title
        //public static final String DEFAULT_SORT = VendorsColumns.NAME + " COLLATE NOCASE ASC";
        

        /*
    	 * Needed for searchable Modules
    	 */
    	public static Uri buildSearchUri(String query){
    		return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
    	}
    	public static boolean isSearchUri(Uri uri){
    		//1 refers to the "search" position added in the buildSearchUri funciton above
    		return PATH_SEARCH.equals(uri.getPathSegments().get(1));
    	}
    	public static String getSearchQuery(Uri uri){
    		//2 refers to the "query" position added in the buildSearchUri function above
    		return uri.getPathSegments().get(2);
    	}
    	public static final String SEARCH_SNIPPET = "search_snippet";

        
    }
    /*
     * General classes and useful functions to be used with the Domain classes
     */
    public static class SearchSuggest{
    	public static final Uri CONTENT_URI = 
    		BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_SUGGEST).build();
    	public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
    		+ " COLLATE NOCASE ASC";
    }
    
    //null constructor, This class cannot be instantiated
    private ImageUploadHistoryDatabase(){
    	
    	
    	
    }
    
}
