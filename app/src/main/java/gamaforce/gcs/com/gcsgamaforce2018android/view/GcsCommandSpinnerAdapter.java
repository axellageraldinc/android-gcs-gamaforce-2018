package gamaforce.gcs.com.gcsgamaforce2018android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import gamaforce.gcs.com.gcsgamaforce2018android.R;
import gamaforce.gcs.com.gcsgamaforce2018android.model.GcsCommand;

public class GcsCommandSpinnerAdapter extends ArrayAdapter<GcsCommand> {

    private Context context;
    private List<GcsCommand> gcsCommandList;

    public GcsCommandSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<GcsCommand> objects) {
        super(context, resource, objects);
        this.context = context;
        this.gcsCommandList = objects;
    }

    @Override
    public int getCount() {
        return gcsCommandList.size();
    }

    @Nullable
    @Override
    public GcsCommand getItem(int position) {
        return gcsCommandList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GcsCommand gcsCommand = getItem(position);
        GcsCommandSpinnerAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new GcsCommandSpinnerAdapter.ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gcs_command, parent, false);
            viewHolder.tvCommand = convertView.findViewById(R.id.tvCommand);
            viewHolder.tvText = convertView.findViewById(R.id.tvText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GcsCommandSpinnerAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.tvCommand.setText(String.valueOf(gcsCommand.getCommandToSend()));
        viewHolder.tvText.setText(gcsCommand.getCommandShownToUi());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GcsCommand gcsCommand = getItem(position);
        GcsCommandSpinnerAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new GcsCommandSpinnerAdapter.ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gcs_command, parent, false);
            viewHolder.tvCommand = convertView.findViewById(R.id.tvCommand);
            viewHolder.tvText = convertView.findViewById(R.id.tvText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GcsCommandSpinnerAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.tvCommand.setText(String.valueOf(gcsCommand.getCommandToSend()));
        viewHolder.tvText.setText(gcsCommand.getCommandShownToUi());
        return convertView;
    }

    private class ViewHolder{
        TextView tvCommand, tvText;
    }
}
