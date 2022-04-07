package internationalproject.medrawumeasure;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class EditDataAdapter extends ArrayAdapter<String> {

    EditDataAdapter(Context context, String[] arr) {
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
            view = inflater.inflate(R.layout.edit_data_list_item, null);
        }

        EditText editText = view.findViewById(R.id.edit_data_list_entry);
        editText.setText(getItem(position));

        return view;
    }
}
