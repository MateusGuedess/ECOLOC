package br.com.fabappu9.ecoloc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.List;
import java.util.Objects;

import br.com.fabappu9.ecoloc.DTO.MaterialDto;
import br.com.fabappu9.ecoloc.R;

public class TiposMaterialAdapter extends ArrayAdapter<MaterialDto> {

    private int resource;

    public TiposMaterialAdapter(Context context , int resource , List<MaterialDto> objects) {
        super(context ,resource,objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position , View convertView , @NonNull ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            row = inflater.inflate(this.resource , parent , false);
        }

        String text = Objects.requireNonNull(getItem(position)).getDescricao();

        CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
        checkBox.setText(text);
        checkBox.setTag(getItem(position));

        //-------
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDto p = (MaterialDto) v.getTag();
                p.setMarcado(((CheckBox) v).isChecked());
            }
        });
        return row;
    }
}
