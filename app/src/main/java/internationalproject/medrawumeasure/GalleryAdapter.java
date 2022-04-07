package internationalproject.medrawumeasure;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GalleryAdapter extends ArrayAdapter<String> {

    GalleryAdapter(Context context, String[] arr) {
        super(context, -1, arr);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            view = inflater.inflate(R.layout.gallery_list_item, null);
        }

        TextView textView = view.findViewById(R.id.gallery_list_entry);
        textView.setText(getItem(position));

        return view;
    }
}
