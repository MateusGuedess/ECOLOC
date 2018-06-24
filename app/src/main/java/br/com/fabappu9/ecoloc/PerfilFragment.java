package br.com.fabappu9.ecoloc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import br.com.fabappu9.ecoloc.data.SharedPreferenceHelper;

/**
 * Created by Geraldo on 06/06/2017.
 * Fixed by Guilherme on 12/05/2018
 */

public class PerfilFragment extends Fragment {

    private View view;
    private TextView apelido;
    private TextView nome;
    private TextView pontuacao;
    private Button btnLogout;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_perfil, container, false);

        iniciarComponentes();

        preencherCampos();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void preencherCampos() {
        SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper(getActivity());
        apelido.setText(sharedPreferenceHelper.getUsuarioLogin());
        nome.setText(sharedPreferenceHelper.getNomeLogin());
        pontuacao.setText("42 Ponto obtidos");
    }

    private void iniciarComponentes() {
        apelido =(TextView) view.findViewById(R.id.txtApelidoUsuario);
        nome = (TextView) view.findViewById(R.id.txtNomeCompleto);
        pontuacao =(TextView) view.findViewById(R.id.txtQuantidadePontos);
        btnLogout = (Button) view.findViewById(R.id.btnLogout);
    }

}
