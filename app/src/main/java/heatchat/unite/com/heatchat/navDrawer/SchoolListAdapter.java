package heatchat.unite.com.heatchat.navDrawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import heatchat.unite.com.heatchat.R;

/**
 * Created by jaybell on 05/11/17.
 */

public class SchoolListAdapter extends RecyclerView.Adapter {

    private List<School> schoolList;
    private Context mContext;

    public SchoolListAdapter(List<School> schoolList) {
        this.schoolList = schoolList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.school_item, parent, false);
        return new SchoolHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 int position) {
        School school = schoolList.get(position);
        ((SchoolListAdapter.SchoolHolder) holder).bind(school);
    }

    @Override
    public int getItemCount() { return schoolList.size(); }

    public class SchoolHolder extends RecyclerView.ViewHolder {
        public TextView name;

        SchoolHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.school_name);
        }

        void bind(School school) {
            this.name.setText(school.getName());
        }
    }
}
