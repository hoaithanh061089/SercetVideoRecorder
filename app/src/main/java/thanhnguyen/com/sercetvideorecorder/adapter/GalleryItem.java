package thanhnguyen.com.sercetvideorecorder.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;

import thanhnguyen.com.sercetvideorecorder.R;

public class GalleryItem extends CursorAdapter {
	
	 Context context;
	 boolean mResolvingError = false;
     private static final int DIALOG_ERROR_CODE =100; 
     File fileuploadtodrive = null;
     String fileuploadname;
	 String imagepath;
	 String id;
	 String duration;
	 String date;
	 String latitude;
	 String longtitude;
	 String thumbpath;


	public GalleryItem(Context context, Cursor c) {
		super(context, c);
		this.context = context;


	}
	@Override
	public int getCount() {

	return this.getCursor().getCount();     }

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final MyViewHolder viewHolder = (MyViewHolder) view.getTag();
	    final File file = new File(imagepath);
	    fileuploadname=imagepath;
	    if(file.exists()){


			if (viewHolder.durationtextv != null) {
				viewHolder.durationtextv.setText(duration);
			}


			if (viewHolder.datetextv != null) {
				viewHolder.datetextv.setText(date);
			}
	    	
	    	File tfile = new File(thumbpath);

			Uri uri = Uri.fromFile(tfile);
		    
		    if(tfile.exists()){


				viewHolder.imagev.setImageURI(uri);


		    
		    } else {

		    	AsyncTask<Void, Void, Bitmap> Bitmap = new AsyncTask<Void, Void, Bitmap>() {

					@Override
					protected Bitmap doInBackground(Void... params) {
						
						Bitmap thumb = ThumbnailUtils.createVideoThumbnail(imagepath,
		    		    	    MediaStore.Images.Thumbnails.MINI_KIND);
						
						return thumb;
					
					}

					@Override
					protected void onPostExecute(Bitmap result) {


						viewHolder.imagev.setImageBitmap(result);

					}
		    	}.execute();
		    	
		    }

	    }  else {


				view.setVisibility(View.GONE);

				return;



	    }


	    
	}


@Override
public View newView(Context context, Cursor cursor, ViewGroup parent) {
	 imagepath = cursor.getString(cursor.getColumnIndex("imagepath"));
	 id = cursor.getString(cursor.getColumnIndex("_id"));
	 duration = cursor.getString(cursor.getColumnIndex("duration"));
	 date = cursor.getString(cursor.getColumnIndex("date"));
	 latitude = cursor.getString(cursor.getColumnIndex("latitude"));
	 longtitude = cursor.getString(cursor.getColumnIndex("longtitude"));
	 thumbpath = cursor.getString(cursor.getColumnIndex("thumbnailpath"));
	LayoutInflater inflater= (LayoutInflater)context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
	View convertView = inflater.inflate(R.layout.galleryitemlayout, parent, false);
	MyViewHolder viewHolder = new MyViewHolder(convertView);
	convertView.setTag(viewHolder);
    return convertView;
}

	public class MyViewHolder  {
		public TextView durationtextv;
		public TextView datetextv;
		public SimpleDraweeView imagev;

		public MyViewHolder(View view) {
			imagev =
					(SimpleDraweeView) view.findViewById(R.id.thumbnail);
			durationtextv =
					(TextView) view.findViewById(R.id.duration);
			datetextv =
					(TextView) view.findViewById(R.id.date);

		}
	}





}
